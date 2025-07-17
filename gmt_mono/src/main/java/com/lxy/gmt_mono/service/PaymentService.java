package com.lxy.gmt_mono.service;

import com.lxy.gmt_mono.entity.Order;

public interface PaymentService {

    /**
     * 处理支付
     *
     * @param order 待支付订单
     * @return 处理结果
     */
    boolean processPayment(Order order);
}
