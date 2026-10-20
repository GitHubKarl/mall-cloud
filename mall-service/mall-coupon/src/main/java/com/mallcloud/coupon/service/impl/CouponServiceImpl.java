package com.mallcloud.coupon.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mallcloud.api.coupon.dto.CouponUseDTO;
import com.mallcloud.api.coupon.dto.CouponUseResultDTO;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.coupon.entity.Coupon;
import com.mallcloud.coupon.entity.UserCoupon;
import com.mallcloud.coupon.mapper.CouponMapper;
import com.mallcloud.coupon.mapper.UserCouponMapper;
import com.mallcloud.coupon.service.CouponService;
import com.mallcloud.coupon.vo.UserCouponVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 优惠券服务实现。
 * <p>
 * 抵扣口径统一在此计算，商品服务、订单服务、结算页都调同一份逻辑，避免"三处算三个价"。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    private static final String TYPE_FULL_REDUCTION = "FULL_REDUCTION";
    private static final String TYPE_DISCOUNT = "DISCOUNT";
    private static final int UNUSED = 0;

    @Override
    public List<UserCouponVO> listAvailable(Long userId) {
        return listMine(userId, UNUSED).stream()
                .filter(vo -> vo.expireTime() == null || vo.expireTime().isAfter(LocalDateTime.now()))
                .toList();
    }

    @Override
    public List<UserCouponVO> listMine(Long userId, Integer status) {
        List<UserCoupon> mine = userCouponMapper.selectList(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(status != null, UserCoupon::getStatus, status)
                .orderByDesc(UserCoupon::getId));
        if (mine.isEmpty()) {
            return List.of();
        }

        Map<Long, Coupon> couponMap = couponMapper.selectByIds(
                        mine.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet()))
                .stream().collect(Collectors.toMap(Coupon::getId, Function.identity(), (a, b) -> a));

        return mine.stream()
                .map(uc -> {
                    Coupon coupon = couponMap.get(uc.getCouponId());
                    if (coupon == null) {
                        return null;
                    }
                    return new UserCouponVO(uc.getId(), coupon.getId(), coupon.getName(), coupon.getType(),
                            coupon.getThreshold(), coupon.getDiscountAmount(), coupon.getDiscountRate(),
                            uc.getStatus(), uc.getExpireTime(), null);
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long receive(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null) {
            throw new BizException(ResultCode.COUPON_NOT_FOUND);
        }
        Long received = userCouponMapper.selectCount(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId));
        int perLimit = coupon.getPerLimit() == null ? 1 : coupon.getPerLimit();
        if (received != null && received >= perLimit) {
            throw new BizException(ResultCode.COUPON_RECEIVED);
        }
        // 原子占用发行量：并发下不会超发
        if (couponMapper.increaseReceived(couponId) == 0) {
            throw new BizException(ResultCode.COUPON_NOT_FOUND, "优惠券已被领完");
        }

        UserCoupon userCoupon = new UserCoupon();
        userCoupon.setUserId(userId);
        userCoupon.setCouponId(couponId);
        userCoupon.setStatus(UNUSED);
        userCoupon.setReceiveTime(LocalDateTime.now());
        userCoupon.setExpireTime(coupon.getEndTime());
        userCouponMapper.insert(userCoupon);
        return userCoupon.getId();
    }

    @Override
    public CouponUseResultDTO calculate(Long userId, Long couponId, BigDecimal orderAmount) {
        UserCoupon userCoupon = requireUsable(userId, couponId);
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        return new CouponUseResultDTO(coupon.getId(), computeDiscount(coupon, orderAmount));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CouponUseResultDTO lock(CouponUseDTO dto) {
        UserCoupon userCoupon = requireUsable(dto.userId(), dto.couponId());
        Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
        BigDecimal discount = computeDiscount(coupon, dto.orderAmount());

        int rows = userCouponMapper.use(userCoupon.getId(), dto.orderNo());
        if (rows == 0) {
            throw new BizException(ResultCode.COUPON_USED);
        }
        return new CouponUseResultDTO(coupon.getId(), discount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean release(String orderNo) {
        int rows = userCouponMapper.release(orderNo);
        log.info("退回优惠券 orderNo={} rows={}", orderNo, rows);
        return true;
    }

    /* ---------------- 内部方法 ---------------- */

    private UserCoupon requireUsable(Long userId, Long couponId) {
        List<UserCoupon> list = userCouponMapper.selectList(Wrappers.<UserCoupon>lambdaQuery()
                .eq(UserCoupon::getUserId, userId)
                .eq(UserCoupon::getCouponId, couponId)
                .eq(UserCoupon::getStatus, UNUSED)
                .orderByAsc(UserCoupon::getId));
        if (list.isEmpty()) {
            throw new BizException(ResultCode.COUPON_USED);
        }
        UserCoupon userCoupon = list.get(0);
        if (userCoupon.getExpireTime() != null && userCoupon.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BizException(ResultCode.COUPON_EXPIRED);
        }
        return userCoupon;
    }

    /** 满减与折扣的统一口径：金额向下取整到分，且最高不超过订单金额 */
    private BigDecimal computeDiscount(Coupon coupon, BigDecimal orderAmount) {
        if (orderAmount == null) {
            throw new BizException(ResultCode.BAD_REQUEST, "订单金额不能为空");
        }
        BigDecimal threshold = coupon.getThreshold() == null ? BigDecimal.ZERO : coupon.getThreshold();
        if (orderAmount.compareTo(threshold) < 0) {
            throw new BizException(ResultCode.COUPON_NOT_MATCH);
        }

        BigDecimal discount;
        if (TYPE_FULL_REDUCTION.equals(coupon.getType())) {
            discount = coupon.getDiscountAmount() == null ? BigDecimal.ZERO : coupon.getDiscountAmount();
        } else if (TYPE_DISCOUNT.equals(coupon.getType())) {
            BigDecimal rate = coupon.getDiscountRate() == null ? BigDecimal.ONE : coupon.getDiscountRate();
            discount = orderAmount.multiply(BigDecimal.ONE.subtract(rate));
        } else {
            discount = BigDecimal.ZERO;
        }
        discount = discount.setScale(2, RoundingMode.DOWN);
        return discount.min(orderAmount);
    }
}
