package com.lxy.gmt_mono.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "用户注册请求参数")
public class UserRegisterRequest {
    @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED, example = "jimmy")
    private String userName;
    @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String password;
    @Schema(description = "确认密码", requiredMode = Schema.RequiredMode.REQUIRED, example = "123456")
    private String checkPassword; // 确认密码
}
