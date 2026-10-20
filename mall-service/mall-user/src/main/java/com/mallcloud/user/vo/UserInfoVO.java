package com.mallcloud.user.vo;

import java.io.Serializable;

/**
 * 会员中心展示信息。手机号做脱敏，避免前端页面直接暴露完整号码。
 */
public record UserInfoVO(Long id,
                         String username,
                         String nickname,
                         String phone,
                         String email,
                         String avatar,
                         Integer memberLevel,
                         Long totalAmount) implements Serializable {

    public static UserInfoVO of(Long id, String username, String nickname, String phone,
                                String email, String avatar, Integer memberLevel, Long totalAmount) {
        return new UserInfoVO(id, username, nickname, maskPhone(phone), email, avatar, memberLevel, totalAmount);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
