package com.mallcloud.common.core.context;

import com.alibaba.ttl.TransmittableThreadLocal;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;

import java.util.Set;

/**
 * 用户上下文。使用 TransmittableThreadLocal，保证 @Async、线程池、MQ 消费线程能拿到父线程的登录态。
 * 必须在请求结束（拦截器 afterCompletion）与消费者 finally 中清理，避免线程池串号。
 */
public final class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new TransmittableThreadLocal<>();

    private static final LoginUser ANONYMOUS = new LoginUser(null, null, null, Set.of(), Set.of());

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        LoginUser user = HOLDER.get();
        return user == null ? ANONYMOUS : user;
    }

    /** 可能为 null，用于可选登录场景 */
    public static Long getUserIdOrNull() {
        return get().userId();
    }

    /** 必须登录，否则抛 401 */
    public static Long requireUserId() {
        Long userId = getUserIdOrNull();
        if (userId == null) {
            throw new BizException(ResultCode.USER_NOT_LOGIN);
        }
        return userId;
    }

    public static String getUsername() {
        return get().username();
    }

    public static boolean isAdmin() {
        return get().isAdmin();
    }

    public static boolean hasPerm(String perm) {
        LoginUser user = get();
        return user.perms() != null && user.perms().contains(perm);
    }

    public static void clear() {
        HOLDER.remove();
    }
}
