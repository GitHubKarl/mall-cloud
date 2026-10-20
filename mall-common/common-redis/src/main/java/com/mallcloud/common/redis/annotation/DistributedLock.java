package com.mallcloud.common.redis.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

/**
 * 分布式锁。基于 Redisson 可重入锁实现，支持 SpEL 拼装 key。
 * <pre>
 * &#64;DistributedLock(key = "'order:create:' + #userId", waitTime = 3)
 * public Long createOrder(Long userId, OrderCreateDTO dto) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DistributedLock {

    /** 锁 key，支持 SpEL，可用 #参数名、#p0 引用方法入参 */
    String key();

    /** 获取锁最长等待秒数，超时抛业务异常 */
    long waitTime() default 3L;

    /** 锁自动释放秒数，防止死锁 */
    long leaseTime() default 30L;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    /** 获取锁失败时的提示语 */
    String message() default "操作过于频繁，请稍后再试";
}
