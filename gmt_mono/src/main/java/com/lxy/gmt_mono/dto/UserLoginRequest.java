package com.lxy.gmt_mono.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户登录请求")
public class UserLoginRequest {
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "jimmy")
    private String userName;
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;
}
