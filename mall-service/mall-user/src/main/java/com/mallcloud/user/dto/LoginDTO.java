package com.mallcloud.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;

/**
 * 登录入参。account 支持用户名 / 手机号。
 */
@Data
public class LoginDTO implements Serializable {

    @NotBlank(message = "账号不能为空")
    private String account;

    @NotBlank(message = "密码不能为空")
    private String password;
}
