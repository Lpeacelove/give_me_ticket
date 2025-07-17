package com.lxy.gmt_mono.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success() {
        return build(ResponseCode.SUCCESS.getCode(),ResponseCode.SUCCESS.getMessage(), null);
    }
    public static <T> Result<T> success(T data) {
        return build(ResponseCode.SUCCESS.getCode(),ResponseCode.SUCCESS.getMessage(), data);
    }

    public static <T> Result<T> error(ResponseCode responseCode) {
        return build(responseCode.getCode(), responseCode.getMessage(), null);
    }

    private static <T> Result<T> build(int code, String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }
}
