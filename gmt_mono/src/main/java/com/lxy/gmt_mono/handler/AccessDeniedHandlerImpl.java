package com.lxy.gmt_mono.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.common.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 拒绝访问处理类
 * @author lxy
 */
@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    /**
     * 拒绝访问处理
     * @param request 请求
     * @param response 响应
     * @param accessDeniedException 拒绝访问异常
     * @throws IOException 抛出IO异常
     * @throws ServletException 抛出Servlet异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 创建一个表示拒绝访问的对象
        Result<Void> result = Result.error(ResponseCode.FORBIDDEN);

        // 设置响应状态码为403
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");

        // 响应结果
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
