package com.mallcloud.api.user.dto;

import java.io.Serializable;

/**
 * 会员基础信息（跨服务传输用，禁止直接暴露实体）。
 */
public record UserDTO(Long id,
                      String username,
                      String nickname,
                      String phone,
                      String avatar,
                      Integer status,
                      Integer memberLevel) implements Serializable {
}
