package com.lxy.gmt_mono.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lxy.gmt_mono.entity.LoginUser;
import com.lxy.gmt_mono.entity.User;
import com.lxy.gmt_mono.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    /**
     * Spring Security进行认证时，会调用这个方法
     * @param username 前端登录时传入的用户名
     * @return UserDetails对象，包含了用户的完整信息（用户名、加密密码、权限等）
     * @throws UsernameNotFoundException 如果用户不存在，必须抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 根据用户名去数据库查找用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUserName, username);
        User user = userMapper.selectOne(queryWrapper);

        // 2. 如果用户不存在，则抛出UsernameNotFoundException异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }

        // 3. 如果用户存在，将我们的User对象，封装成Spring Security认识的UserDetails对象
        // org.springframework.security.core.userdetails.User是UserDetails的一个实现类
        // 参数：用户名、加密后的密码、权限集合（我们暂时给一个空的集合）
        return new LoginUser(user);
    }
}
