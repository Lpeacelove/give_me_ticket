package com.lxy.gmt_mono.common;

import lombok.Getter;

@Getter
public enum ResponseCode {

    // --- 通用成功 ---
    SUCCESS(200, "操作成功"),

    // --- 客户端错误(4xx) ---
    BAD_REQUEST(400, "请求错误"),
    UNAUTHORIZED(401, "需要认证"),
    FORBIDDEN(403, "无权访问"),
    NOT_FOUND(404, "资源不存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),

    // --- 服务端错误(5xx) ---
    INTERNAL_SERVER_ERROR(500, "系统开小差了，请稍后再试~"),

    // --- 自定义错误 ---
    USERNAME_ALREADY_EXIST(10001, "该用户名已被注册"),
    PASSWORD_NOT_MATCH(10002, "两次输入的密码不一致"),
    TICKET_NOT_FOUND(10003, "票务不存在"),
    STOCK_NOT_ENOUGH(10004, "库存不足"),
    DEDUCT_STOCK_FAILED(10005, "扣减库存失败"),

    ORDER_STATUS_ERROR(12001, "订单状态异常，无法进行操作"),
    PAYMENT_FAILED(12002, "订单支付失败"),
    USER_REPEAT_BUY(12003, "重复购买，订单创建失败"),
    TOKEN_INVALID(12004, "下单令牌无效");



    private final int code;
    private final String message;
    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
