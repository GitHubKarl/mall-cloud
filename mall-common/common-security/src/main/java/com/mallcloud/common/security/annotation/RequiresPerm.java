package com.mallcloud.common.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验。权限标识建议采用「模块:资源:动作」，如 {@code product:spu:add}。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequiresPerm {

    /** 需要的权限标识，满足任意一个即放行 */
    String[] value();

    /** 是否要求全部满足 */
    boolean requireAll() default false;
}
