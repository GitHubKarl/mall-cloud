package com.mallcloud.common.feign.interceptor;

import com.mallcloud.common.core.constant.CommonConstants;
import com.mallcloud.common.core.context.LoginUser;
import com.mallcloud.common.core.context.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.StringJoiner;

/**
 * Feign 请求拦截器：把当前请求的 Token 与用户上下文透传给下游服务。
 * <p>
 * 优先取原始 HTTP 请求头（保证与网关签发的内容完全一致）；
 * 若当前不在 HTTP 线程（@Async、MQ 消费者、定时任务），则从 TransmittableThreadLocal 还原。
 */
public class FeignRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        template.header(CommonConstants.HEADER_INNER_CALL, "true");

        HttpServletRequest request = currentRequest();
        if (request != null) {
            copyHeader(request, template, CommonConstants.HEADER_AUTHORIZATION);
            copyHeader(request, template, CommonConstants.HEADER_USER_ID);
            copyHeader(request, template, CommonConstants.HEADER_USERNAME);
            copyHeader(request, template, CommonConstants.HEADER_USER_TYPE);
            copyHeader(request, template, CommonConstants.HEADER_USER_ROLES);
            copyHeader(request, template, CommonConstants.HEADER_USER_PERMS);
            copyHeader(request, template, CommonConstants.HEADER_TRACE_ID);
            return;
        }

        applyFromContext(template);
    }

    private void applyFromContext(RequestTemplate template) {
        LoginUser user = UserContext.get();
        if (user.userId() == null) {
            return;
        }
        template.header(CommonConstants.HEADER_USER_ID, String.valueOf(user.userId()));
        Optional.ofNullable(user.username())
                .ifPresent(name -> template.header(CommonConstants.HEADER_USERNAME, name));
        Optional.ofNullable(user.userType())
                .ifPresent(type -> template.header(CommonConstants.HEADER_USER_TYPE, String.valueOf(type)));
        if (user.roles() != null && !user.roles().isEmpty()) {
            template.header(CommonConstants.HEADER_USER_ROLES, join(user.roles()));
        }
        if (user.perms() != null && !user.perms().isEmpty()) {
            template.header(CommonConstants.HEADER_USER_PERMS, join(user.perms()));
        }
    }

    private void copyHeader(HttpServletRequest request, RequestTemplate template, String headerName) {
        String value = request.getHeader(headerName);
        if (value != null && !value.isBlank()) {
            template.header(headerName, value);
        }
    }

    private String join(Iterable<String> values) {
        StringJoiner joiner = new StringJoiner(",");
        values.forEach(joiner::add);
        return joiner.toString();
    }

    private HttpServletRequest currentRequest() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes.getRequest();
        }
        return null;
    }
}
