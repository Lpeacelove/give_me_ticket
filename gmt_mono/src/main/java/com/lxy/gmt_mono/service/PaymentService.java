package com.lxy.gmt_mono.service;

import com.lxy.gmt_mono.entity.Order;

public interface PaymentService {

    /**
     * 创建一次支付请求，并返回支付凭证（如支付HTML）
     *
     * @param orderNumber 订单号
     * @param userId      用户ID
     * @return 支付宝支付页面的完整HTML内容
     */
    String createPayment(String orderNumber, Long userId);
}
