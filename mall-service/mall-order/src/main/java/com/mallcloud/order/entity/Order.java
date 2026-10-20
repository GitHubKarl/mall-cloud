package com.mallcloud.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单主表。
 * <p>
 * 收货信息做<b>快照</b>而非外键：用户改地址、删地址都不影响历史订单。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order")
public class Order extends BaseEntity {

    private String orderNo;

    private Long userId;

    /** 0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-已关闭 */
    private Integer status;

    /** 商品总额 */
    private BigDecimal totalAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 应付 / 实付金额 */
    private BigDecimal payAmount;

    private Long couponId;

    private String receiver;

    private String receiverPhone;

    private String receiverAddress;

    private String remark;

    /** 支付超时时间，到此时间未支付则关单 */
    private LocalDateTime expireTime;

    private LocalDateTime payTime;

    private LocalDateTime shipTime;

    private LocalDateTime finishTime;

    private LocalDateTime closeTime;

    /** 关闭原因：TIMEOUT / CANCEL / PAY_FAILED */
    private String closeReason;
}
