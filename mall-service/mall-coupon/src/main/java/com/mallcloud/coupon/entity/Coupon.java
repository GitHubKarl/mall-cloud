package com.mallcloud.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_coupon")
public class Coupon extends BaseEntity {

    private String name;

    /** FULL_REDUCTION-满减 DISCOUNT-折扣 */
    private String type;

    /** 使用门槛，0 表示无门槛 */
    private BigDecimal threshold;

    /** 满减金额 */
    private BigDecimal discountAmount;

    /** 折扣率，如 0.85 表示 85 折 */
    private BigDecimal discountRate;

    /** 发行量 */
    private Integer total;

    /** 已领取量 */
    private Integer received;

    /** 每人限领 */
    private Integer perLimit;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;
}
