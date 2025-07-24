//package com.lxy.gmt_mono.service.impl;
//
//import com.lxy.gmt_mono.entity.Order;
//import com.lxy.gmt_mono.service.PaymentService;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//@Slf4j
//public class MockPaymentServiceImpl implements PaymentService {
//    /**
//     * 模拟支付服务
//     */
//    @Override
//    public boolean processPayment(Order order) {
//        log.info("模拟调用第三方支付服务支付[{}]", order.getOrderNumber());
//        try {
//            // 模拟支付服务耗时
//            Thread.sleep(1000);
//            // 模拟支付失败
//            if (order.getOrderNumber().endsWith("1")) {
//                log.warn("模拟支付失败[{}]", order.getOrderNumber());
//                return false;
//            }
//            log.info("模拟支付成功[{}]", order.getOrderNumber());
//            return true;
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            log.error("模拟支付服务异常[{}]，被中断", order.getOrderNumber(), e);
//            return false;
//        }
//    }
//
//    @Override
//    public String createPayment(String orderNumber, Long userId) {
//        return "";
//    }
//}
