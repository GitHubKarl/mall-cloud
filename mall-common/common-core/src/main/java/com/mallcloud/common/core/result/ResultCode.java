package com.mallcloud.common.core.result;

import lombok.Getter;

/**
 * 全局状态码。业务码段规划：
 * <ul>
 *     <li>0            成功</li>
 *     <li>400 / 401 / 403 / 404 / 405 / 429 / 500 / 503   协议级错误</li>
 *     <li>11xxx 用户 12xxx 商品 13xxx 库存 14xxx 订单 15xxx 支付</li>
 *     <li>16xxx 购物车 17xxx 优惠券 18xxx 搜索 19xxx 评价 20xxx 通知</li>
 * </ul>
 */
@Getter
public enum ResultCode {

    SUCCESS(0, "操作成功"),

    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "登录已失效，请重新登录"),
    FORBIDDEN(403, "没有操作权限"),
    NOT_FOUND(404, "请求资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方式不支持"),
    TOO_MANY_REQUESTS(429, "请求过于频繁，请稍后再试"),
    INTERNAL_ERROR(500, "系统繁忙，请稍后再试"),
    SERVICE_UNAVAILABLE(503, "依赖服务不可用"),

    /* 用户 11xxx */
    USER_NOT_FOUND(11001, "用户不存在"),
    USER_PASSWORD_ERROR(11002, "用户名或密码错误"),
    USER_DISABLED(11003, "账号已被禁用"),
    USER_ALREADY_EXISTS(11004, "账号已存在"),
    USER_NOT_LOGIN(11005, "请先登录"),
    ADDRESS_NOT_FOUND(11006, "收货地址不存在"),

    /* 商品 12xxx */
    PRODUCT_NOT_FOUND(12001, "商品不存在"),
    PRODUCT_OFF_SHELF(12002, "商品已下架"),
    PRODUCT_SKU_NOT_FOUND(12003, "商品规格不存在"),
    PRODUCT_STOCK_INSUFFICIENT(12004, "商品库存不足"),

    /* 库存 13xxx */
    WARE_STOCK_INSUFFICIENT(13001, "库存不足"),
    WARE_LOCK_FAILED(13002, "库存锁定失败"),
    WARE_UNLOCK_FAILED(13003, "库存解锁失败"),

    /* 订单 14xxx */
    ORDER_NOT_FOUND(14001, "订单不存在"),
    ORDER_STATUS_ERROR(14002, "订单状态不允许该操作"),
    ORDER_CREATE_FAILED(14003, "订单创建失败"),
    ORDER_ALREADY_CLOSED(14004, "订单已关闭"),
    ORDER_ITEM_EMPTY(14005, "订单商品不能为空"),

    /* 支付 15xxx */
    PAYMENT_NOT_FOUND(15001, "支付单不存在"),
    PAYMENT_ALREADY_PAID(15002, "订单已支付"),
    PAYMENT_CHANNEL_UNSUPPORTED(15003, "不支持的支付渠道"),
    PAYMENT_SIGN_ERROR(15004, "支付回调验签失败"),

    /* 购物车 16xxx */
    CART_EMPTY(16001, "购物车为空"),
    CART_ITEM_NOT_FOUND(16002, "购物车中不存在该商品"),

    /* 优惠券 17xxx */
    COUPON_NOT_FOUND(17001, "优惠券不存在"),
    COUPON_RECEIVED(17002, "该优惠券已领取"),
    COUPON_EXPIRED(17003, "优惠券已过期"),
    COUPON_USED(17004, "优惠券已使用"),
    COUPON_NOT_MATCH(17005, "订单不满足优惠券使用条件"),

    /* 搜索 18xxx */
    SEARCH_INDEX_ERROR(18001, "商品索引操作失败"),

    /* 评价 19xxx */
    COMMENT_NOT_ALLOWED(19001, "该订单暂不可评价"),
    COMMENT_ALREADY_EXISTS(19002, "该订单已评价"),

    /* 通知 20xxx */
    NOTICE_SEND_FAILED(20001, "消息发送失败");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
