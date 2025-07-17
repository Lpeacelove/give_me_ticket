package com.lxy.gmt_mono.service;

import com.lxy.gmt_mono.dto.UserLoginRequest;
import com.lxy.gmt_mono.dto.UserRegisterRequest;

public interface UserService {

    /**
     * 用户注册
     * @param request 用户注册请求参数
     */
    void register(UserRegisterRequest request);

    /**
     * 用户登录
     * @param request 用户登录请求参数
     * @return 登录成功返回用户信息
     */
    String login(UserLoginRequest request);
}
