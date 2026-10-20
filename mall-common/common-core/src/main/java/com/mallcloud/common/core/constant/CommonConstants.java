package com.mallcloud.common.core.constant;

/**
 * 全局常量。请求头中携带的用户信息由网关写入，内网互信，业务服务不再二次验签。
 */
public interface CommonConstants {

    String HEADER_AUTHORIZATION = "Authorization";
    String BEARER_PREFIX = "Bearer ";

    String HEADER_USER_ID = "X-User-Id";
    String HEADER_USERNAME = "X-Username";
    String HEADER_USER_TYPE = "X-User-Type";
    String HEADER_USER_ROLES = "X-User-Roles";
    String HEADER_USER_PERMS = "X-User-Perms";
    String HEADER_TRACE_ID = "X-Trace-Id";
    String HEADER_INNER_CALL = "X-Inner-Call";

    /** 逻辑删除：未删除 / 已删除 */
    int NOT_DELETED = 0;
    int DELETED = 1;

    /** 通用状态：启用 / 禁用 */
    int STATUS_ENABLED = 1;
    int STATUS_DISABLED = 0;

    /** Redis key 前缀 */
    String CACHE_PREFIX = "mall:";
    String CACHE_CART = CACHE_PREFIX + "cart:";
    String CACHE_PRODUCT = CACHE_PREFIX + "product:";
    String CACHE_USER = CACHE_PREFIX + "user:";
    String CACHE_COUPON = CACHE_PREFIX + "coupon:";
    String CACHE_TOKEN_BLACKLIST = CACHE_PREFIX + "token:blacklist:";

    long CACHE_PRODUCT_TTL_MINUTES = 30L;
    long CACHE_CART_TTL_DAYS = 30L;
}
