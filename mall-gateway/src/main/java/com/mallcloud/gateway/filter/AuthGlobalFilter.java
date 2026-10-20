package com.mallcloud.gateway.filter;

import com.mallcloud.common.core.constant.CommonConstants;
import com.mallcloud.common.core.context.LoginUser;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.core.security.JwtUtils;
import com.mallcloud.common.core.util.JsonUtils;
import com.mallcloud.gateway.config.GatewayAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * 全局鉴权过滤器（唯一鉴权点）。
 * <p>
 * 三件事：
 * <ol>
 *     <li>拒绝外部访问 /inner/** 内部接口</li>
 *     <li>白名单直接放行；其余校验 JWT</li>
 *     <li>验签成功后把用户信息写入请求头，并<b>先清除客户端伪造的同名头</b></li>
 * </ol>
 * 注意：WebFlux 下不能使用 ThreadLocal 传用户上下文，只能用 request header。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final GatewayAuthProperties properties;

    private static final AntPathMatcher MATCHER = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (matches(path, properties.getDenyList())) {
            return reject(exchange, ResultCode.FORBIDDEN);
        }

        // 无论是否白名单，都先剥离客户端自带的身份头，防止越权伪造
        ServerHttpRequest.Builder builder = request.mutate()
                .headers(headers -> {
                    headers.remove(CommonConstants.HEADER_USER_ID);
                    headers.remove(CommonConstants.HEADER_USERNAME);
                    headers.remove(CommonConstants.HEADER_USER_TYPE);
                    headers.remove(CommonConstants.HEADER_USER_ROLES);
                    headers.remove(CommonConstants.HEADER_USER_PERMS);
                    headers.set(CommonConstants.HEADER_TRACE_ID, traceId(headers));
                });

        if (matches(path, properties.getWhiteList())) {
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        if (!properties.isAuthEnabled()) {
            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        String token = JwtUtils.resolveToken(
                request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION), CommonConstants.BEARER_PREFIX);
        if (token == null) {
            return reject(exchange, ResultCode.UNAUTHORIZED);
        }

        LoginUser user = jwtUtils.parseAccess(token);
        if (user == null || user.userId() == null) {
            return reject(exchange, ResultCode.UNAUTHORIZED);
        }

        builder.header(CommonConstants.HEADER_USER_ID, String.valueOf(user.userId()));
        builder.header(CommonConstants.HEADER_USER_TYPE, String.valueOf(user.userType()));
        if (user.username() != null) {
            builder.header(CommonConstants.HEADER_USERNAME, user.username());
        }
        if (user.roles() != null && !user.roles().isEmpty()) {
            builder.header(CommonConstants.HEADER_USER_ROLES, String.join(",", user.roles()));
        }
        if (user.perms() != null && !user.perms().isEmpty()) {
            builder.header(CommonConstants.HEADER_USER_PERMS, String.join(",", user.perms()));
        }

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private boolean matches(String path, List<String> patterns) {
        return patterns != null && patterns.stream().anyMatch(pattern -> MATCHER.match(pattern, path));
    }

    private String traceId(HttpHeaders headers) {
        String traceId = headers.getFirst(CommonConstants.HEADER_TRACE_ID);
        return traceId == null || traceId.isBlank() ? UUID.randomUUID().toString().replace("-", "") : traceId;
    }

    private Mono<Void> reject(ServerWebExchange exchange, ResultCode resultCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        byte[] body = JsonUtils.toBytes(R.fail(resultCode));
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
