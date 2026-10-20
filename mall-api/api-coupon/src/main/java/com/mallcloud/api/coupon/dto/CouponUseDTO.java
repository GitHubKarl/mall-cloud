package com.mallcloud.api.coupon.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券核销入参。
 */
public record CouponUseDTO(Long userId,
                           Long couponId,
                           String orderNo,
                           BigDecimal orderAmount) implements Serializable {
}
