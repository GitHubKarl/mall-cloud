package com.mallcloud.common.feign.fallback;

import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.core.result.ResultCode;
import org.springframework.cloud.openfeign.FallbackFactory;

/**
 * Sentinel 降级工厂基类。各服务的 Feign 降级实现继承它，统一处理兜底返回与错误语义。
 *
 * @param <T> Feign 接口类型
 */
public abstract class BaseFallbackFactory<T> implements FallbackFactory<T> {

    /** 明确"拿不到数据"的调用方，用它抛异常，由全局异常处理器转成 503 */
    protected BizException unavailable(Throwable cause) {
        return new BizException(ResultCode.SERVICE_UNAVAILABLE,
                "依赖服务暂时不可用：" + (cause == null ? "unknown" : cause.getMessage()));
    }

    /** 允许降级为空结果的调用方，直接返回统一失败体 */
    protected R<?> degraded() {
        return R.fail(ResultCode.SERVICE_UNAVAILABLE);
    }
}
