package com.lxy.gmt_mono.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("g_user")  // 表名
public class User {
    private Long id;
    private String userName;
    private String realName;
    private String phone;
    private Integer gender;  // 0: 未知 1: 男 2: 女
    private String password;
    private Integer emailStatus;
    private String email;
    private Integer realAuthenticationStatus;  // 0: 未认证 1: 认证中 2: 认证成功 3: 认证失败
    private String idNumber;
    private String address;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer status;  // 0: 删除 1: 正常
}
