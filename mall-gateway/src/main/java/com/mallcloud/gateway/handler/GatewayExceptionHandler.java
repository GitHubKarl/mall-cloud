package com.mallcloud.gateway.handler;

import com.mallcloud.common.core.result.R;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.core.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeoutException;

/**
 * 网关统一异常处理。下游不可达、路由超时时返回标准结构，而不是 Spring 默认的 error 页面。
 */
@Slf4j
@Order(-1)
@Component
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        if (response.isCommitted()) {
            return Mono.error(ex);
        }

        ResultCode resultCode = resolve(ex);
        if (resultCode == ResultCode.SERVICE_UNAVAILABLE) {
            log.error("网关转发失败 path={}", exchange.getRequest().getURI().getPath(), ex);
        } else {
            log.warn("网关拦截 path={} msg={}", exchange.getRequest().getURI().getPath(), ex.getMessage());
        }

        response.setStatusCode(HttpStatus.OK);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        DataBuffer buffer = response.bufferFactory().wrap(JsonUtils.toBytes(R.fail(resultCode)));
        return response.writeWith(Mono.just(buffer));
    }

    private ResultCode resolve(Throwable ex) {
        if (ex instanceof ResponseStatusException statusException) {
            return switch (statusException.getStatusCode().value()) {
                case 404 -> ResultCode.NOT_FOUND;
                case 401 -> ResultCode.UNAUTHORIZED;
                case 403 -> ResultCode.FORBIDDEN;
                case 429 -> ResultCode.TOO_MANY_REQUESTS;
                default -> ResultCode.INTERNAL_ERROR;
            };
        }
        if (ex instanceof TimeoutException) {
            return ResultCode.SERVICE_UNAVAILABLE;
        }
        return ResultCode.INTERNAL_ERROR;
    }
}
