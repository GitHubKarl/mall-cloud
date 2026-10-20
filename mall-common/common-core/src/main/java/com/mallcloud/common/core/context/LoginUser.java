package com.mallcloud.common.core.context;

import java.io.Serializable;
import java.util.Set;

/**
 * 当前登录用户。由网关验签后写入请求头，下游拦截器还原到 {@link UserContext}。
 *
 * @param userId   用户主键
 * @param username 登录名
 * @param userType 1-会员 2-后台管理员
 * @param roles    角色编码
 * @param perms    权限标识
 */
public record LoginUser(Long userId, String username, Integer userType, Set<String> roles, Set<String> perms)
        implements Serializable {

    public static final int TYPE_MEMBER = 1;
    public static final int TYPE_ADMIN = 2;

    public boolean isAdmin() {
        return userType != null && userType == TYPE_ADMIN;
    }
}
