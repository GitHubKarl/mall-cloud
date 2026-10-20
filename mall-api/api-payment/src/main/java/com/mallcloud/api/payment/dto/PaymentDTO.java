package com.mallcloud.api.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 支付单。
 *
 * @param status 0-待支付 1-支付成功 2-支付失败 3-已退款
 * @param payUrl 收银台跳转地址或二维码内容
 */
public record PaymentDTO(String paymentNo,
                         String orderNo,
                         BigDecimal amount,
                         Integer status,
                         String channel,
                         String payUrl) implements Serializable {
}
