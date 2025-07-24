package com.lxy.gmt_mono.config;

import com.lxy.gmt_mono.common.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * JWT认证过滤器
 */
@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsService userDetailsService;

    // ------------- 白名单和路径匹配器 -----------------
    // 创建一个路径匹配器实例，用于支持通配符匹配
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 定义一个白名单列表，这里的路径应该和SecurityConfig中permitAll()的路径保持一致
    private final List<String> whiteList = Arrays.asList(
            "/doc.html",
            "/webjars/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/api/v1/user/register",
            "/api/v1/user/login",
            "/admin/api/**",
            "/favicon.ico",
            "/api/v1/payments/alipay/notify"
    );

    /**
     * 拦截请求，进行JWT认证
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        // 0. 获取请求头的uri
        String requestURI = request.getRequestURI();

        // 1. 从请求中获取Authentication字段
        final String authHeader = request.getHeader("Authorization");

        // 2. 如果请求头中没有Authentication字段，或者Authentication字段的值不是以Bearer开头，则直接放行
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 删除Bearer前缀，得到JWT
        final String jwt = authHeader.substring(7);
        final String userName;

        try {
            // 4. 验证JWT
            userName = jwtUtils.extractUsername(jwt);
        } catch (Exception e) {
            // 如果解析失败（比如token伪造，token过期）则直接放行，后续过滤器会将其作为匿名请求处理
            filterChain.doFilter(request, response);
            return;
        }

        // 5. 检查userName不为空，且SecurityContextHolder中不存在用户信息
        if (userName != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 6. 根据用户名加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

            // 7. 验证token是否有效
            if (jwtUtils.validateToken(jwt, userDetails.getUsername())) {
                // 8. 如果token有效，创建一个令牌
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // 密码我们不需要，因为已经是认证过的了
                        userDetails.getAuthorities()); // 权限信息
                // 9. 设置令牌的详细信息，如用户名、密码、权限等信息
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // 10. 将令牌设置到Spring Security的SecurityContext中
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 11. 无论如何，放行请求
        filterChain.doFilter(request, response);
    }

    /**
     * 重写这个方法，告诉SpringSecurity那些请求根本不需要经过这个JWT过滤器
     * @param request 当前的HTTP请求
     * @return true 表示不需要过滤，false 表示需要过滤
     * @throws ServletException 抛出ServletException
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // 1. 获取当前请求的URI路径
        String path = request.getRequestURI();

        // 2. 检查这个路径是否匹配上我们白名单中的任何一个模式
        boolean shouldNotFilter = whiteList.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        // 3. 打印一条日志，方便调试，看看它对每个请求的决策是什么
        if (shouldNotFilter) {
            log.info("JwtAuthenticationFilter:请求 [{}] 在白名单中，跳过JWT校验", path);
        }
        return shouldNotFilter;
    }
}
