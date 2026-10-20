package com.mallcloud.user.vo;

import java.io.Serializable;

/**
 * 登录结果。前端拿到 token 后放入 Authorization: Bearer xxx。
 *
 * @param expiresIn 访问令牌有效期（秒），前端据此提前刷新
 */
public record LoginVO(String accessToken,
                      String refreshToken,
                      long expiresIn,
                      Long userId,
                      String username,
                      String nickname,
                      String avatar) implements Serializable {
}
