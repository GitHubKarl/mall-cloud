package com.mallcloud.common.security.interceptor;

import com.mallcloud.common.core.constant.CommonConstants;
import com.mallcloud.common.core.context.LoginUser;
import com.mallcloud.common.core.context.UserContext;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.security.annotation.RequiresLogin;
import com.mallcloud.common.security.annotation.RequiresPerm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录态还原拦截器。
 * <p>
 * 设计要点：<b>本拦截器不解析 JWT</b>。网关已完成验签并写入 X-User-* 请求头，服务内直接信任，
 * 避免每个服务重复做一遍密码学运算。仅当服务被内网直连（无网关）时，头部缺失会导致
 * {@link RequiresLogin} 校验失败，起到兜底作用。
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        restoreUser(request);
        checkLogin(handlerMethod);
        checkPerm(handlerMethod);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 线程池复用，必须清理，否则 A 用户的身份会串到 B 请求
        UserContext.clear();
    }

    private void restoreUser(HttpServletRequest request) {
        String userIdHeader = request.getHeader(CommonConstants.HEADER_USER_ID);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return;
        }
        try {
            Long userId = Long.valueOf(userIdHeader);
            String username = request.getHeader(CommonConstants.HEADER_USERNAME);
            String userTypeHeader = request.getHeader(CommonConstants.HEADER_USER_TYPE);
            Integer userType = userTypeHeader == null || userTypeHeader.isBlank()
                    ? LoginUser.TYPE_MEMBER : Integer.valueOf(userTypeHeader);

            UserContext.set(new LoginUser(userId, username, userType,
                    splitToSet(request.getHeader(CommonConstants.HEADER_USER_ROLES)),
                    splitToSet(request.getHeader(CommonConstants.HEADER_USER_PERMS))));
        } catch (NumberFormatException e) {
            log.warn("非法的用户请求头: X-User-Id={}", userIdHeader);
        }
    }

    private void checkLogin(HandlerMethod handlerMethod) {
        boolean required = AnnotatedElementUtils.hasAnnotation(handlerMethod.getMethod(), RequiresLogin.class)
                || AnnotatedElementUtils.hasAnnotation(handlerMethod.getBeanType(), RequiresLogin.class);
        if (required && UserContext.getUserIdOrNull() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
    }

    private void checkPerm(HandlerMethod handlerMethod) {
        RequiresPerm requiresPerm = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(), RequiresPerm.class);
        if (requiresPerm == null) {
            requiresPerm = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(), RequiresPerm.class);
        }
        if (requiresPerm == null) {
            return;
        }

        LoginUser user = UserContext.get();
        if (user.userId() == null) {
            throw new BizException(ResultCode.UNAUTHORIZED);
        }
        // 超级管理员直接放行
        if (user.roles() != null && user.roles().contains("SUPER_ADMIN")) {
            return;
        }

        Set<String> owned = user.perms() == null ? Set.of() : user.perms();
        String[] required = requiresPerm.value();
        boolean pass = requiresPerm.requireAll()
                ? owned.containsAll(Arrays.asList(required))
                : Arrays.stream(required).anyMatch(owned::contains);

        if (!pass) {
            throw new BizException(ResultCode.FORBIDDEN);
        }
    }

    private Set<String> splitToSet(String header) {
        if (header == null || header.isBlank()) {
            return Set.of();
        }
        return new HashSet<>(Arrays.asList(header.split(",")));
    }
}
