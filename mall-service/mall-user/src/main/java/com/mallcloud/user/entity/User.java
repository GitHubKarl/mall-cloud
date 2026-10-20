package com.mallcloud.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 会员。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user")
public class User extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String phone;

    private String email;

    private String avatar;

    /** 1-正常 0-禁用 */
    private Integer status;

    /** 会员等级 1~5 */
    private Integer memberLevel;

    /** 累计消费金额，用于自动升级 */
    private Long totalAmount;

    private LocalDateTime lastLoginTime;
}
