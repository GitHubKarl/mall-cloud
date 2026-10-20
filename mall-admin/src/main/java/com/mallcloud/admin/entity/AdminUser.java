package com.mallcloud.admin.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 后台管理员。
 * <p>
 * 与 C 端会员完全隔离：不同的表、不同的服务、不同的 Token 载荷（userType=2）。
 * 现实中后台账号一旦与前台混用，越权风险极高。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_admin_user")
public class AdminUser extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private String avatar;

    /** 1-正常 0-禁用 */
    private Integer status;

    /** 角色编码，如 SUPER_ADMIN / OPERATOR（简化版，生产建议拆角色权限表） */
    private String roleCode;

    /** 权限标识，逗号分隔，如 product:spu:save,order:deliver */
    private String perms;

    private LocalDateTime lastLoginTime;
}
