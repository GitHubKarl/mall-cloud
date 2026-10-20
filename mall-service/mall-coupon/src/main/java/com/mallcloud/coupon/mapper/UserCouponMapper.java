package com.mallcloud.coupon.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.coupon.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {

    /** 核销：未使用 → 已使用，同时写入订单号 */
    @Update("""
            UPDATE t_user_coupon
               SET status = 1, order_no = #{orderNo}, use_time = NOW(), update_time = NOW()
             WHERE id = #{id}
               AND status = 0
               AND deleted = 0
            """)
    int use(@Param("id") Long id, @Param("orderNo") String orderNo);

    /** 退回：已使用且属于该订单 → 未使用 */
    @Update("""
            UPDATE t_user_coupon
               SET status = 0, order_no = NULL, use_time = NULL, update_time = NOW()
             WHERE order_no = #{orderNo}
               AND status = 1
               AND deleted = 0
            """)
    int release(@Param("orderNo") String orderNo);
}
