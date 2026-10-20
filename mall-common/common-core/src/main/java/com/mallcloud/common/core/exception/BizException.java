package com.mallcloud.common.core.exception;

import com.mallcloud.common.core.result.ResultCode;
import lombok.Getter;

/**
 * 业务异常。由全局异常处理器统一转换为 {@code R.fail(...)}，不打印堆栈。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BizException of(ResultCode resultCode) {
        return new BizException(resultCode);
    }

    /** 条件成立时抛出，替代大量 if-throw 样板 */
    public static void isTrue(boolean condition, ResultCode resultCode) {
        if (condition) {
            throw new BizException(resultCode);
        }
    }

    public static void notNull(Object obj, ResultCode resultCode) {
        if (obj == null) {
            throw new BizException(resultCode);
        }
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        // 业务异常不需要堆栈，提升性能
        return this;
    }
}
