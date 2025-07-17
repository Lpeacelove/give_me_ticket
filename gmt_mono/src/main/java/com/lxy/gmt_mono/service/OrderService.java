package com.lxy.gmt_mono.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lxy.gmt_mono.dto.OrderListResponse;
import com.lxy.gmt_mono.dto.OrderMessage;
import com.lxy.gmt_mono.entity.Order;

public interface OrderService {

    void createOrder(OrderMessage orderMessage);

    /**
     * 通过消息队列创建订单
     * @param message 消息队列中的消息
     */
    void createOrderByMessage(String message);

    /**
     * 获取用户订单列表
     *
     * @param pageNum  页码
     * @param pageSize 页大小
     * @param userId   用户ID
     * @return 用户订单列表
     */
    Page<OrderListResponse> listOrdersByPage(Long pageNum, Long pageSize, Long userId);

    /**
     * 获取订单详情
     * @param orderNumber 订单ID
     * @return 订单详情
     */
    Order getOrderDetailById(String orderNumber, Long userId);

    /**
     * 支付订单
     * @param orderNumber 订单ID
     * @param userId  用户ID
     */
    void payOrder(String orderNumber, Long userId);
}
