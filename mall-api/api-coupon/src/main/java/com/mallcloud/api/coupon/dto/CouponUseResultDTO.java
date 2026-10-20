package com.mallcloud.api.coupon.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 优惠券核销结果。discountAmount 为实际抵扣金额，订单服务据此计算应付金额。
 */
public record CouponUseResultDTO(Long couponId, BigDecimal discountAmount) implements Serializable {
}
