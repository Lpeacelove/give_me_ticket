package com.lxy.gmt_mono.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxy.gmt_mono.common.BusinessException;
import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.config.RabbitMQConfig;
import com.lxy.gmt_mono.dto.OrderListResponse;
import com.lxy.gmt_mono.dto.OrderMessage;
import com.lxy.gmt_mono.entity.Order;
import com.lxy.gmt_mono.entity.Ticket;
import com.lxy.gmt_mono.mapper.OrderMapper;
import com.lxy.gmt_mono.service.OrderService;
import com.lxy.gmt_mono.service.PaymentService;
import com.lxy.gmt_mono.service.TicketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String TICKET_STOCK_KEY_PREFIX = "ticket:stock:";

    /**
     * 只是负责创建订单
     *
     * @param orderMessage 订单信息
     */
    @Override
    @Transactional
    public void createOrder(OrderMessage orderMessage) {
        // 1. 获取票务信息
        Ticket ticket = ticketService.getTicketById(orderMessage.getTicketId());
        if (ticket == null) {
            throw new BusinessException(ResponseCode.TICKET_NOT_FOUND);
        }

        // 2. 创建订单
        Order order = new Order();
        order.setTicketId(orderMessage.getTicketId());
        order.setUserId(orderMessage.getUserId());
        order.setOrderNumber(orderMessage.getOrderNumber());
        order.setQuantity(orderMessage.getQuantity());
        order.setTicketTitle(ticket.getTitle());
        order.setTicketPlace(ticket.getPlace());
        order.setTicketShowTime(ticket.getShowTime());
        order.setOrderPrice(ticket.getPrice() * orderMessage.getQuantity());
        order.setCreateOrderTime(LocalDateTime.now());
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        order.setStatus(0);

        orderMapper.insert(order);
    }

    @Override
    @RabbitListener(queues = RabbitMQConfig.ORDER_QUEUE)
    public void createOrderByMessage(String jsonMessage) {
        log.info("收到消息：{}", jsonMessage);
        try {
            OrderMessage orderMessage = objectMapper.readValue(jsonMessage, OrderMessage.class);
            createOrder(orderMessage);
        } catch (JsonProcessingException e) {
            log.error("反序列化订单消息失败", e);
            // todo 需要考虑如何处理这种毒信（无法解析的消息），比如存到死信队列
        } catch (Exception e) {
            log.error("异步创建订单时发生未知错误: message={}", jsonMessage, e);
            // todo 这里也需要有补偿机制，比如记录失败日志，人工干预等
        }
    }

    @Override
    public Page<OrderListResponse> listOrdersByPage(Long pageNum, Long pageSize, Long userId) {
        // 1. 创建分页对象
        Page<Order> orderPage = new Page<>(pageNum, pageSize);
        // 2. 创建查询条件构造器
        LambdaUpdateWrapper<Order> queryWrapper = new LambdaUpdateWrapper<>();
        // 3. 添加查询条件，筛选指定用户下的订单
        queryWrapper.eq(Order::getUserId, userId);
        // 4. 添加排序条件，按照更新时间降序排序
        queryWrapper.orderByDesc(Order::getUpdateTime);
        // 5. 执行分页查询
        orderMapper.selectPage(orderPage, queryWrapper);

        // 6. 创建一个新的Page对象，用于存放返回结果
        Page<OrderListResponse> orderListResponsePage = new Page<>();
        // 7. 将查询结果转换为OrderListResponse对象列表
        BeanUtils.copyProperties(orderPage, orderListResponsePage, "records");
        orderListResponsePage.setRecords(orderPage.getRecords().stream().map(order -> {
            OrderListResponse orderListResponse = new OrderListResponse();
            BeanUtils.copyProperties(order, orderListResponse);
            return orderListResponse;
        }).collect(Collectors.toList()));
        return orderListResponsePage;
    }

    @Override
    public Order getOrderDetailById(String orderNumber, Long userId) {
        // 1. 创建查询条件
        LambdaUpdateWrapper<Order> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(Order::getOrderNumber, orderNumber);
        queryWrapper.eq(Order::getUserId, userId);

        // 2. 执行查询
        Order order = orderMapper.selectOne(queryWrapper);

        // 3. 判断该订单是否存在
        if (order == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    @Override
    public String payOrder(String orderNumber, Long userId) {
        // 订单校验的逻辑已经在createPayment中实现了
        // 状态更新的逻辑，将会在回调处理器中实现
        // 这里只负责调用支付服务创建支付链接
        return paymentService.createPayment(orderNumber, userId);
    }

    /**
     * 订单关闭处理
     * 监听死信处理队列，处理订单关闭逻辑
     *
     * @param orderNumber 订单号
     */
    @Override
    @RabbitListener(queues = RabbitMQConfig.ORDER_RELEASE_QUEUE)
    public void handleOrderClose(String orderNumber) {
        log.info("订单关闭处理，订单号：{}", orderNumber);

        try{
            this.closeOvertimeOrder(orderNumber);
        } catch (Exception e) {
            log.error("订单关闭处理失败：{}", e.getMessage());
        }

    }

    @Override
    @Transactional
    public void closeOvertimeOrder(String orderNumber) {
        // 1. 获取订单信息
        LambdaUpdateWrapper<Order> queryWrapper = new LambdaUpdateWrapper<>();
        queryWrapper.eq(Order::getOrderNumber, orderNumber);
        Order order = orderMapper.selectOne(queryWrapper);

        // 2. 判断该订单是否存在
        if (order == null) {
            log.warn("超时订单不存在，无需处理，订单号：{}", orderNumber);
            return;
        }

        // 3. 判断订单是否已支付
        if (order.getStatus() == 0) {
            log.warn("订单未支付，订单号：{}", orderNumber);
            // 3.1 将订单状态更新为已取消
            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            updateOrder.setStatus(2);
            updateOrder.setCancelOrderTime(LocalDateTime.now());
            updateOrder.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(updateOrder);
            // 3.2 回补库存
            String stockKey = TICKET_STOCK_KEY_PREFIX + order.getTicketId();
            stringRedisTemplate.opsForValue().increment(stockKey, order.getQuantity());
            log.info("订单由于超时，取消成功，订单号：{}，补充库存: {}", orderNumber, order.getQuantity());
        } else {
            log.info("订单状态为[{}],已经变更过，无需处理", order.getStatus());
        }
    }

    @Override
    @Transactional
    public void cancelOrder(String orderNumber, Long userId) {
        // 1. 查询订单并进行归属权和存在性检验
        Order order = getOrderDetailById(orderNumber, userId);

        // 2. 判断订单状态
        if (order.getStatus() != 0) {
            throw new BusinessException(ResponseCode.ORDER_STATUS_ERROR, "订单已支付或已取消，请勿重复操作");
        }

        // 3. 取消订单
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(2);
        updateOrder.setCancelOrderTime(LocalDateTime.now());
        int updateCount = orderMapper.updateById(updateOrder);
        if (updateCount == 0) {
            throw new BusinessException(ResponseCode.INTERNAL_SERVER_ERROR, "订单取消失败，请检查订单状态");
        }

        // 4. 回补库存
        String stockKey = TICKET_STOCK_KEY_PREFIX + order.getTicketId();
        stringRedisTemplate.opsForValue().increment(stockKey, order.getQuantity());
        log.info("订单取消成功，订单号：{}，补充库存: {}", orderNumber, order.getQuantity());
    }

    @Override
    public void processPaidOrder(String orderNumber) {
        // 这个方法是被异步调用的，因此也需要幂等性
        LambdaUpdateWrapper<Order> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .set(Order::getStatus, 1)
                .set(Order::getPayTime, LocalDateTime.now())
                .eq(Order::getOrderNumber, orderNumber)
                .eq(Order::getStatus, 0);
        int updateCount = orderMapper.update(null, updateWrapper);
        if (updateCount == 0) {
            log.warn("支付宝回调：订单支付成功，但订单状态未更新，订单号：{}", orderNumber);
        } else {
            log.info("支付宝回调：订单支付成功，订单号：{}", orderNumber);
        }

    }


}
