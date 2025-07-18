package com.lxy.gmt_mono.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxy.gmt_mono.common.BusinessException;
import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.config.RabbitMQConfig;
import com.lxy.gmt_mono.dto.OrderCreateRequest;
import com.lxy.gmt_mono.dto.OrderMessage;
import com.lxy.gmt_mono.entity.Ticket;
import com.lxy.gmt_mono.mapper.TicketMapper;
import com.lxy.gmt_mono.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class SecKillServiceImpl implements SecKillService{

    private static final String TICKET_STOCK_KEY_PREFIX = "ticket:stock:";
    private static final String SECKILL_USER_SET_KEY_PREFIX = "seckill:ticket:users";

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TicketMapper ticketMapper;
    @Autowired
    private RedisScript<Long> redisScript;

    /**
     * 秒杀
     *
     * @param userId  用户ID
     * @param request 订单创建请求
     * @return 秒杀是否成功
     */
    @Override
    public String executeSecKill(Long userId, OrderCreateRequest request) {

        Long ticketId = request.getTicketId();
        Integer quantity = request.getQuantity();

        // 1. 准备Lua脚本需要的缓存键
        String stockKey = TICKET_STOCK_KEY_PREFIX + ticketId;
        String userSetKey = SECKILL_USER_SET_KEY_PREFIX + ticketId;

        // 2. 执行Lua脚本
        Long result = stringRedisTemplate.execute(
                redisScript,
                Collections.list(Collections.enumeration(Arrays.asList(stockKey, userSetKey))),
                String.valueOf(userId),
                String.valueOf(quantity)
        );

        // 3. 判断脚本执行结果
        if (result == null) {
            log.error("秒杀失败，请稍后再试");
            return null;
        }
        if (result == -1L) {
            log.error("用户{} 重复购买，操作被拒绝。", userId);
            // 抛出业务异常
            throw new BusinessException(ResponseCode.USER_REPEAT_BUY, "您已经抢购过了，请勿重复下单");
        }
        if (result == 0L) {
            log.error("库存不足，请稍后再试");
            return null;
        }

        // 4. 执行成功
        log.info("秒杀成功，用户id: {}, 秒杀的票务id: {}", userId, ticketId);
        // 4.1 秒杀成功，发送消息到MQ
        String orderNumber = UUID.randomUUID().toString().replace("-", "");
        OrderMessage orderMessage = new OrderMessage(userId, ticketId, quantity, orderNumber);
        try {
            String message = objectMapper.writeValueAsString(orderMessage);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.ORDER_EXCHANGE,
                    RabbitMQConfig.ORDER_ROUTING_KEY,
                    message
            );
        } catch (JsonProcessingException e) {
            log.error("订单创建失败：{}", e.getMessage());
            // 如果发送MQ失败，则将库存加回Redis
            stringRedisTemplate.opsForValue().increment(stockKey, quantity);
            // 并且把用户从已购集合中移除
            stringRedisTemplate.opsForSet().remove(userSetKey, String.valueOf(userId));
            throw new BusinessException(ResponseCode.DEDUCT_STOCK_FAILED, "秒杀失败");
        }
        return orderNumber;
    }

    @Override
    public void warmupStock(Long ticketId) {
        // todo 预热可以为秒杀前手动预热库存，也可以通过定时任务进行预热，暂时设置为手动预热
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket != null && ticket.getStock() > 0) {
            String stockKey = TICKET_STOCK_KEY_PREFIX + ticketId;
            // 将库存写入Redis
            stringRedisTemplate.opsForValue().set(stockKey, ticket.getStock().toString());
            log.info("预热库存成功，ticketId: {}, stock: {}", ticketId, ticket.getStock());
        }
    }

    @Override
    public void syncStockToDB(Long ticketId) {
        // todo 目前为手动同步库，后续会在秒杀后添加定时任务，进行同步
        String stockKey = TICKET_STOCK_KEY_PREFIX + ticketId;
        String remainingStockStr = stringRedisTemplate.opsForValue().get(stockKey);

        if (StringUtils.hasText(remainingStockStr)) {
            try {
                long remainingStock = Long.parseLong(remainingStockStr);
                Ticket ticket = new Ticket();
                ticket.setId(ticketId);
                ticket.setStock(remainingStock);

                // 只更新库存字段
                ticketMapper.updateById(ticket);
                log.info("票务[ID:{}]库存已成功同步到数据库，当前库存为：{}", ticketId, remainingStock);
            } catch (NumberFormatException e) {
                log.error("同步库存失败，Redis中的库存值格式不正确: {}", remainingStockStr);
            }
        } else {
            log.warn("票务[ID:{}]库存同步失败，原因：缓存中未找到库存信息", ticketId);
        }
    }
}
