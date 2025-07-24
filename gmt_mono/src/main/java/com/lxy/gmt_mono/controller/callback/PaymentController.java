package com.lxy.gmt_mono.controller.callback;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.common.Result;
import com.lxy.gmt_mono.config.AlipayConfig;
import com.lxy.gmt_mono.service.OrderService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")  // 支付回调接口
public class PaymentController {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderService orderService;

    @PostMapping("/alipay/notify")
    public Result<String> handleAlipayNotify(HttpServletRequest request) {
        log.info("收到支付宝回调请求...");
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        // 获取参数到map中
        for (String name: requestParams.keySet()) {
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            params.put(name, valueStr);
        }

        try {
            // 1. 进行签名验证
            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType());
            if (!signVerified) {
                log.error("支付宝回调签名验证失败...");
                return Result.error(ResponseCode.PAYMENT_FAILED);
            }

            // 2. 验签成功后，进行业务逻辑处理
            // 2.1 获取订单号和交易状态
            String orderNumber = params.get("out_trade_no");
            String tradeStatus = params.get("trade_status");
            // 2.2 只处理交易状态为成功或完成的通知
            if (tradeStatus.equals("TRADE_SUCCESS") || tradeStatus.equals("TRADE_FINISHED")) {
                orderService.processPaidOrder(orderNumber);
            }
            return Result.success();
        } catch (AlipayApiException e) {
            return Result.error(ResponseCode.PAYMENT_FAILED);
        }
    }
}
