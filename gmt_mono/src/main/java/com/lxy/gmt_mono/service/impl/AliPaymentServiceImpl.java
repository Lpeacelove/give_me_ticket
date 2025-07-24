package com.lxy.gmt_mono.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.lxy.gmt_mono.common.BusinessException;
import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.config.AlipayConfig;
import com.lxy.gmt_mono.entity.Order;
import com.lxy.gmt_mono.mapper.OrderMapper;
import com.lxy.gmt_mono.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AliPaymentServiceImpl implements PaymentService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 创建支付宝支付
     *
     * @param orderNumber 订单编号
     * @param userId      用户ID
     * @return 支付宝支付链接
     */
    @Override
    public String createPayment(String orderNumber, Long userId) {
        // 1. 根据订单号查询订单信息，以及获取金额等
        // 需要做归属权校验
        LambdaUpdateWrapper<Order> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Order::getOrderNumber, orderNumber)
                .eq(Order::getUserId, userId);
        Order order = orderMapper.selectOne(wrapper);
        if (order == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND, "订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException(ResponseCode.ORDER_STATUS_ERROR, "订单已支付或已取消，无法支付");
        }

        // 2. 创建支付宝支付请求对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        // 3. 填充业务参数
        request.setBizContent(String.format(
                "{\"out_trade_no\":\"%s\"," +
                        "\"total_amount\":\"%s\"," +
                        "\"subject\":\"%s\"," +
                        "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}",
                orderNumber,
                order.getOrderPrice().toString(),
                "GMT 订单-" + order.getTicketTitle()
        ));

        try {
            // 4. 调用SDK生成支付表单HTML
            log.info("正在为订单[{}]生成支付宝支付链接...", orderNumber);
            return alipayClient.pageExecute(request).getBody();
        } catch (AlipayApiException e) {
            log.error("调用支付宝SDK异常[{}]", orderNumber, e);
            throw new BusinessException(ResponseCode.PAYMENT_FAILED, "调用支付宝SDK异常");
        }
    }
}
