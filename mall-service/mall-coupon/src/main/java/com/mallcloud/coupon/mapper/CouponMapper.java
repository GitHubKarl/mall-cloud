package com.mallcloud.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.coupon.entity.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CouponMapper extends BaseMapper<Coupon> {

    /** 原子占用发行量，防止超发 */
    @Update("""
            UPDATE t_coupon
               SET received = received + 1, update_time = NOW()
             WHERE id = #{couponId}
               AND deleted = 0
               AND received < total
            """)
    int increaseReceived(@Param("couponId") Long couponId);
}
