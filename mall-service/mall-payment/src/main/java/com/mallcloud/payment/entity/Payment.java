package com.mallcloud.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付单。一个订单可以有多条支付单（失败后重新发起），但只有一条能成功。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_payment")
public class Payment extends BaseEntity {

    private String paymentNo;

    private String orderNo;

    private Long userId;

    private BigDecimal amount;

    /** WECHAT_NATIVE / ALIPAY / MOCK */
    private String channel;

    /** 0-待支付 1-支付成功 2-支付失败 3-已退款 */
    private Integer status;

    /** 渠道交易号 */
    private String tradeNo;

    private LocalDateTime payTime;

    /** 渠道原始回调报文，排查纠纷时必备 */
    private String callbackBody;
}
