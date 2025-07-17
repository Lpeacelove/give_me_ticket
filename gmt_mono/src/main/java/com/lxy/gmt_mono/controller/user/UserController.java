package com.lxy.gmt_mono.controller.user;

import com.lxy.gmt_mono.common.ResponseCode;
import com.lxy.gmt_mono.common.Result;
import com.lxy.gmt_mono.dto.UserLoginRequest;
import com.lxy.gmt_mono.dto.UserLoginResponse;
import com.lxy.gmt_mono.dto.UserRegisterRequest;
import com.lxy.gmt_mono.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController // @Controller + @ResponseBody 标记为控制器，且是restful风格, 返回json数据
@RequestMapping("/api/v1/user")  // 给该controller下的所有请求添加前缀
@Tag(name = "用户端-认证接口", description = "用户注册与登录")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     * @param request 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "提供用户名和密码进行新用户注册")
    public Result<Void> register(@RequestBody UserRegisterRequest request) {
        // @RequestBody注解表示从请求体中获取JSON数据并转换为UserRegisterRequest对象
        userService.register(request);
        return Result.success();

    }

    /**
     * 用户登录
     * @param request 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "提供用户名和密码进行用户登录")
    public Result<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        String token = userService.login(request);
        return Result.success(new UserLoginResponse(token));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的信息")
    public Result<String> getCurrentUser(Authentication  authentication) {
        if (authentication == null) {
            return Result.error(ResponseCode.UNAUTHORIZED);
        }
        return Result.success("当前登录的用户为：" + authentication.getName());
    }
}
