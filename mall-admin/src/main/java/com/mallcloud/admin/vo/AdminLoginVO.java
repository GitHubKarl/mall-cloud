package com.mallcloud.admin.vo;

import java.io.Serializable;
import java.util.Set;

/**
 * 后台登录结果。
 */
public record AdminLoginVO(String accessToken,
                           String refreshToken,
                           long expiresIn,
                           Long adminId,
                           String username,
                           String nickname,
                           Set<String> roles,
                           Set<String> perms) implements Serializable {
}
