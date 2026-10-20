package com.mallcloud.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户持有的优惠券。
 * <p>
 * orderNo 既是使用凭证也是退回依据：状态从 1 回退到 0 时清空 orderNo。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_user_coupon")
public class UserCoupon extends BaseEntity {

    private Long userId;

    private Long couponId;

    /** 0-未使用 1-已使用 2-已过期 */
    private Integer status;

    private String orderNo;

    private LocalDateTime receiveTime;

    private LocalDateTime useTime;

    private LocalDateTime expireTime;
}
