package com.mallcloud.common.feign.decoder;

import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.core.util.JsonUtils;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Feign 错误解码器：把下游返回的 {@link R} 反序列化后以业务异常抛出，
 * 避免调用方拿到 500 却丢失下游的真实错误码。
 */
@Slf4j
public class InnerErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if (response.body() == null) {
            return defaultDecoder.decode(methodKey, response);
        }
        try {
            String body = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
            R<?> result = JsonUtils.parse(body, R.class);
            if (result != null && !result.isSuccess()) {
                log.warn("Feign 调用失败 method={} status={} code={} msg={}",
                        methodKey, response.status(), result.code(), result.message());
                return new BizException(result.code(), result.message());
            }
            return new BizException(ResultCode.SERVICE_UNAVAILABLE,
                    "下游服务返回异常状态：" + response.status());
        } catch (IOException | RuntimeException e) {
            log.warn("Feign 错误响应解析失败 method={}", methodKey, e);
            return defaultDecoder.decode(methodKey, response);
        }
    }
}
