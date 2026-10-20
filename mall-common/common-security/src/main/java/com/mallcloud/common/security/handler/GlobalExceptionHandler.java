package com.mallcloud.common.security.handler;

import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.core.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * 全局异常处理。保证任何异常都以统一 {@link R} 结构返回，前端不需要区分成功/失败两套解析逻辑。
 * <p>
 * 放在 common-security 的原因：只有它依赖 spring-webmvc；common-core 必须保持零 Web 依赖以便网关复用。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBiz(BizException e, HttpServletRequest request) {
        log.warn("业务异常 uri={} code={} msg={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public R<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST, msg.isBlank() ? ResultCode.BAD_REQUEST.getMessage() : msg);
    }

    @ExceptionHandler(BindException.class)
    public R<Void> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST, msg.isBlank() ? ResultCode.BAD_REQUEST.getMessage() : msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public R<Void> handleConstraintViolation(ConstraintViolationException e) {
        String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining("; "));
        return R.fail(ResultCode.BAD_REQUEST, msg);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public R<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return R.fail(ResultCode.BAD_REQUEST, "缺少必要参数：" + e.getParameterName());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public R<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return R.fail(ResultCode.BAD_REQUEST, "参数格式错误：" + e.getName());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public R<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return R.fail(ResultCode.BAD_REQUEST, "请求体格式错误");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public R<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return R.fail(ResultCode.METHOD_NOT_ALLOWED, "不支持 " + e.getMethod() + " 请求");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public R<Void> handleNoResource(NoResourceFoundException e) {
        return R.fail(ResultCode.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("未捕获异常 uri={}", request.getRequestURI(), e);
        return R.fail(ResultCode.INTERNAL_ERROR);
    }
}
