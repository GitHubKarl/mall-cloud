package com.mallcloud.common.core.result;

import java.io.Serializable;
import java.time.Instant;

/**
 * 统一响应体。所有 Controller 一律返回该结构，网关与 Feign 解码器也依赖它。
 *
 * @param code      状态码，0 表示成功，见 {@link ResultCode}
 * @param message   提示信息
 * @param data      业务数据
 * @param timestamp 服务端时间戳（毫秒）
 */
public record R<T>(int code, String message, T data, long timestamp) implements Serializable {

    public static <T> R<T> ok() {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null, Instant.now().toEpochMilli());
    }

    public static <T> R<T> ok(T data) {
        return new R<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data, Instant.now().toEpochMilli());
    }

    public static <T> R<T> ok(T data, String message) {
        return new R<>(ResultCode.SUCCESS.getCode(), message, data, Instant.now().toEpochMilli());
    }

    public static <T> R<T> fail(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null, Instant.now().toEpochMilli());
    }

    public static <T> R<T> fail(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null, Instant.now().toEpochMilli());
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null, Instant.now().toEpochMilli());
    }

    /** 供 Feign 降级、网关熔断直接构造 */
    public static <T> R<T> fail(ResultCode resultCode, T data) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), data, Instant.now().toEpochMilli());
    }

    public boolean isSuccess() {
        return this.code == ResultCode.SUCCESS.getCode();
    }

    /** 取数据，失败或空时抛异常，供服务间调用方简化判空 */
    public T getDataOrThrow() {
        if (!isSuccess()) {
            throw new com.mallcloud.common.core.exception.BizException(this.code, this.message);
        }
        return this.data;
    }
}
