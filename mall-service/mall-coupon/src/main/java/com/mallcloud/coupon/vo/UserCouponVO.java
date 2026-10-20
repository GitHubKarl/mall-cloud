package com.mallcloud.coupon.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 我的优惠券。
 *
 * @param status     0-未使用 1-已使用 2-已过期
 * @param applicable 当前订单是否可用（结算页用），无订单上下文时返回 null
 */
public record UserCouponVO(Long userCouponId,
                           Long couponId,
                           String name,
                           String type,
                           BigDecimal threshold,
                           BigDecimal discountAmount,
                           BigDecimal discountRate,
                           Integer status,
                           LocalDateTime expireTime,
                           Boolean applicable) implements Serializable {
}
