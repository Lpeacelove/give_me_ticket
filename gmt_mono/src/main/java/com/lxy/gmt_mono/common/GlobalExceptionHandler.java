package com.lxy.gmt_mono.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j // 日志
@RestControllerAdvice // 声明这是一个RESTful风格的全局异常处理器
public class GlobalExceptionHandler {


    /**
     * 处理业务异常
     * @param e 业务异常
     * @return 错误结果
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常：code={}, message={}", e.getResponseCode().getCode(), e.getResponseCode().getMessage());
        return Result.error(e.getResponseCode());
    }
//    /**
//     * 处理参数校验异常
//     * @param e 参数校验异常
//     * @return 返回错误结果
//     */
//    @ExceptionHandler(IllegalArgumentException.class)
//    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
//        log.warn("参数校验失败错误：{}", e.getMessage());
//        return Result.error(ResponseCode.BAD_REQUEST);
//    }

    /**
     * 处理系统异常
     * @param e 系统异常
     * @return 错误结果
     */
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：{}", e.getMessage());
        return Result.error(ResponseCode.INTERNAL_SERVER_ERROR);
    }

}
