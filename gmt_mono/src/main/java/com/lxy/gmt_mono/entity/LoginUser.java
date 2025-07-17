package com.lxy.gmt_mono.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser implements UserDetails {
    // 登录用户
    private User user;

    /**
     * 权限信息
     * @return 权限信息
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 返回用户的权限集合，暂时给一个空的集合
        return Collections.emptyList();
    }

    // 获取用户ID
    public Long getUserId() {
        return user.getId();
    }
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserName();
    }

    // todo ---- 以下为账户状态相关的方法，为了简便，统一返回true  ----
    // 账户是否未过期，默认是false，如果过期了，则返回true
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 账户是否未锁定，默认是false，如果锁定了，则返回true
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 密码是否未过期，默认是false，如果过期了，则返回true
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 账户是否可用，默认是true，如果禁用了，则返回false
    @Override
    public boolean isEnabled() {
        return true;
    }
}
