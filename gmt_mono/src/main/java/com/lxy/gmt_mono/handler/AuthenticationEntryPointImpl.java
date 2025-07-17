package com.lxy.gmt_mono.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.common.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 认证失败处理类
 * @author lxy
 */
@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    /**
     * 认证失败处理
     * @param request 请求
     * @param response 响应
     * @param authException 认证异常
     * @throws IOException 抛出IO异常
     * @throws ServletException 抛出Servlet异常
     */
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 创建一个表示需要认证的对象
        Result<Void> result = Result.error(ResponseCode.UNAUTHORIZED);
        // 设置响应状态码为401
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        // 设置响应内容类型为JSON
        response.setContentType("application/json;charset=UTF-8");

        // 使用ObjectMapper将结果转换为JSON字符串并写入响应中
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
