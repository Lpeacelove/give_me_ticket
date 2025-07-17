package com.lxy.gmt_mono.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxy.gmt_mono.common.JwtUtils;
import com.lxy.gmt_mono.dto.UserLoginRequest;
import com.lxy.gmt_mono.dto.UserRegisterRequest;
import com.lxy.gmt_mono.entity.User;
import com.lxy.gmt_mono.mapper.UserMapper;
import com.lxy.gmt_mono.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户注册
     * @param request 注册请求
     */
    @Override
    public void register(UserRegisterRequest request) {

        String userName = request.getUserName();
        String password = request.getPassword();
        String checkPassword = request.getCheckPassword();

        // 1. 参数校验（虽然前端也会有自己的参数校验，但是作为后端业务核心，也需要有自己的校验）
        Assert.notNull(userName, "用户名不能为空");
        Assert.notNull(password, "密码不能为空");
        Assert.notNull(checkPassword, "确认密码不能为空");
        Assert.isTrue(password.equals(checkPassword), "两次输入密码不一致");

        // 2. 业务逻辑校验（检验用户是否已经存在）
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, userName);
        Long count = userMapper.selectCount(queryWrapper);
        Assert.isTrue(count == 0, "用户已存在");

        // 3. 创建实体对象，准备入库
        User user = new User();
        user.setUserName(userName);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());

        // 4. 密码加密
        String encryptedPassword = passwordEncoder.encode(password); // 模拟密码加密
        user.setPassword(encryptedPassword);

        // 5. 数据入库，持久化
        userMapper.insert(user);
    }

    /**
     * 用户登录
     * @param request 登录请求参数
     * @return 登录成功后的token
     */
    @Override
    public String login(UserLoginRequest request) {
        // 1. 使用AuthenticationManager进行认证
        //    这会触发之前实现的UserDetailsServiceImpl中的loadUserByUsername方法
        //    同时，它会使自动使用配置的PasswordEncoder进行密码校验
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword())
        );

        // 2. 如果认证通过，将认证信息存入SecurityContextHolder中
        //    为后续请求能够获得当前登录用户信息
        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        System.out.println("用户名：" + userDetails.getUsername() + "认证信息正在存入，准备返回令牌");
        // 3. 返回JWT并返回给前端
        return jwtUtils.generateToken(userDetails);
    }
}
