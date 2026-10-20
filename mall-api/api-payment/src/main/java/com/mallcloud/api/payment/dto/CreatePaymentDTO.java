package com.mallcloud.api.payment.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 创建支付单入参。
 */
public record CreatePaymentDTO(String orderNo,
                               Long userId,
                               BigDecimal amount,
                               String channel) implements Serializable {
}
