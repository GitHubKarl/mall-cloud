package com.mallcloud.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.payment.entity.Payment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    /**
     * CAS 更新支付状态，防止渠道重复回调导致重复入账。
     *
     * @return 0 表示已被处理过
     */
    @Update("""
            UPDATE t_payment
               SET status = #{toStatus}, trade_no = #{tradeNo}, pay_time = NOW(), update_time = NOW()
             WHERE payment_no = #{paymentNo}
               AND status = #{fromStatus}
               AND deleted = 0
            """)
    int casStatus(@Param("paymentNo") String paymentNo,
                  @Param("fromStatus") Integer fromStatus,
                  @Param("toStatus") Integer toStatus,
                  @Param("tradeNo") String tradeNo);
}
