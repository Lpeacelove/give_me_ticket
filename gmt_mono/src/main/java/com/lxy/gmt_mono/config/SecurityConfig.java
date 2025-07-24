package com.lxy.gmt_mono.config;

import com.lxy.gmt_mono.handler.AccessDeniedHandlerImpl;
import com.lxy.gmt_mono.handler.AuthenticationEntryPointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity  // 启用Spring Security的功能

public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private AuthenticationEntryPointImpl authenticationEntryPoint;

    @Autowired
    private AccessDeniedHandlerImpl accessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // 创建BCryptPasswordEncoder对象
        // 此处在配置中注册bean，而不是在service中注册，是为了解耦，避免service依赖security，在后面想要修改加密方式，也可以直接修改配置
        // 配置类的存在，是替代传统的xml配置文件的，将配置信息放在配置类中，而不是xml文件中
    }

    /**
     * 配置安全过滤器链，用于控制哪些请求需要认证，哪些可以放行
     * @param http HttpSecurity对象
     * @return SecurityFilterChain对象
     * @throws Exception 抛出异常
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1. 禁用CSRF
                .csrf(csrf -> csrf.disable())
                .httpBasic(httpBasic -> httpBasic.disable())

                // 2. 【核心修改】明确设置会话管理策略为“无状态”(STATELESS)
                // 这对于JWT认证至关重要，它告诉Spring Security不要创建HttpSession
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

//                .authorizeHttpRequests(auth -> auth
//                        .anyRequest().permitAll())
                // 3. 配置URL授权规则（【核心修改】调整顺序）
                .authorizeHttpRequests(auth -> auth
                        // a. 将最具体的、需要放行的路径放在最前面
                        .requestMatchers(
                                "/doc.html",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/api/v1/user/register",
                                "/api/v1/user/login",
                                "/api/v1/payments/alipay/notify"
                        ).permitAll()

                        // b. 对于票务列表的GET请求，也放行
                        .requestMatchers(HttpMethod.GET, "/api/v1/tickets/**").permitAll()

                        // c. 【重要】将管理员接口的规则放在前面，因为它比 /api/** 更具体
                        // 暂时还是放行，方便测试
                        .requestMatchers("/admin/api/**").permitAll()

                        // d. 最后，配置所有其他需要认证的请求
                        // 所有其他以 /api/ 开头的请求，都需要认证
                        .requestMatchers("/api/**").authenticated()

                        // 注意：我们暂时去掉了 .anyRequest().denyAll()
                        // 因为在开发阶段，它可能会导致一些Spring Boot Actuator等内部端点也被拒绝，难以调试。
                        // 生产环境中可以再加回来，作为一道最终的安全屏障。
                        .anyRequest().authenticated() // 默认所有未匹配的都需要认证，比denyAll更温和
                )
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint(authenticationEntryPoint)
                    .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 创建SecurityFilterChain
     * @param authenticationConfiguration AuthenticationConfiguration对象
     * @return AuthenticationManager对象
     * @throws Exception 抛出异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
