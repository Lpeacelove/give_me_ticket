package com.lxy.gmt_mono.common;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private final ResponseCode responseCode;

    /**
     *  构造函数，接收ResponseCode枚举，并设置异常信息为ResponseCode枚举的message
     * @param responseCode 响应码枚举
     */
    public BusinessException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }

    /**
     *  构造函数，接收ResponseCode枚举和自定义的异常信息
     * @param responseCode 响应码枚举
     * @param message 自定义的异常信息
     */
    public BusinessException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
    }
}
