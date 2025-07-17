package com.lxy.gmt_mono.common;

import com.lxy.gmt_mono.entity.LoginUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 封装Security相关操作
 */
public class SecurityUtils {

    /**
     * 获取当前用户信息
     * @return 当前用户信息
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 获取当前用户
     * @return 当前用户
     */
    public static LoginUser getCurrentUser() {
        Authentication authentication = getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof LoginUser) {
            return (LoginUser) principal;
        }
        return null;
    }

    /**
     * 获取当前用户ID
     * @return 用户ID
     *
     */
    public static Long getCurrentUserId() {
        LoginUser loginUser = getCurrentUser();
        return loginUser != null ? loginUser.getUserId() : null;
    }

    /**
     * 获取当前用户名
     * @return 当前用户名，如果未登录，则返回null
     */
    public static String getCurrentUserName() {
        LoginUser loginUser = getCurrentUser();
        return loginUser != null ? loginUser.getUsername() : null;
    }
}
