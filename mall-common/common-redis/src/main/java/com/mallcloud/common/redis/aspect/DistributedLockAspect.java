package com.mallcloud.common.redis.aspect;

import com.mallcloud.common.core.constant.CommonConstants;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.common.redis.annotation.DistributedLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

/**
 * {@link DistributedLock} 切面。加锁范围只覆盖方法体，异常时保证 finally 释放。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class DistributedLockAspect {

    private final RedissonClient redissonClient;

    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    @Around("@annotation(distributedLock)")
    public Object around(ProceedingJoinPoint joinPoint, DistributedLock distributedLock) throws Throwable {
        String lockKey = CommonConstants.CACHE_PREFIX + "lock:" + resolveKey(joinPoint, distributedLock.key());
        RLock lock = redissonClient.getLock(lockKey);

        boolean acquired = false;
        try {
            acquired = lock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(),
                    distributedLock.timeUnit());
            if (!acquired) {
                throw new BizException(ResultCode.TOO_MANY_REQUESTS, distributedLock.message());
            }
            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException(ResultCode.INTERNAL_ERROR, "获取分布式锁被中断");
        } finally {
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private String resolveKey(ProceedingJoinPoint joinPoint, String keyExpression) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String[] paramNames = NAME_DISCOVERER.getParameterNames(method);
        Object[] args = joinPoint.getArgs();

        EvaluationContext context = new StandardEvaluationContext();
        if (paramNames != null) {
            for (int i = 0; i < paramNames.length; i++) {
                context.setVariable(paramNames[i], args[i]);
                context.setVariable("p" + i, args[i]);
            }
        }
        try {
            String value = PARSER.parseExpression(keyExpression).getValue(context, String.class);
            return value == null ? "default" : value;
        } catch (Exception e) {
            log.warn("分布式锁 SpEL 解析失败，退化为方法名作为 key: {}", keyExpression, e);
            return method.getDeclaringClass().getSimpleName() + ":" + method.getName();
        }
    }

    /** 供业务侧手动加锁使用 */
    public boolean tryLock(String key, long waitSeconds, long leaseSeconds) throws InterruptedException {
        return redissonClient.getLock(CommonConstants.CACHE_PREFIX + "lock:" + key)
                .tryLock(waitSeconds, leaseSeconds, TimeUnit.SECONDS);
    }

    public void unlock(String key) {
        RLock lock = redissonClient.getLock(CommonConstants.CACHE_PREFIX + "lock:" + key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
