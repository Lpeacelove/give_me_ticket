package com.lxy.gmt_mono.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户登录响应, 返回jwt")
public class UserLoginResponse {
    @Schema(description = "认证令牌(JWT)")
    private String token;
}
