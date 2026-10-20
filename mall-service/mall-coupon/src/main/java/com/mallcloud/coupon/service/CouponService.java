package com.mallcloud.coupon.service;

import com.mallcloud.api.coupon.dto.CouponUseDTO;
import com.mallcloud.api.coupon.dto.CouponUseResultDTO;
import com.mallcloud.coupon.vo.UserCouponVO;

import java.util.List;

/**
 * 优惠券服务。
 */
public interface CouponService {

    List<UserCouponVO> listAvailable(Long userId);

    List<UserCouponVO> listMine(Long userId, Integer status);

    Long receive(Long userId, Long couponId);

    /** 结算页试算：不核销，只返回可抵扣金额 */
    CouponUseResultDTO calculate(Long userId, Long couponId, java.math.BigDecimal orderAmount);

    /** 核销（Seata 参与者） */
    CouponUseResultDTO lock(CouponUseDTO dto);

    /** 退回（幂等） */
    boolean release(String orderNo);
}
