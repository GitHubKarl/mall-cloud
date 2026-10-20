package com.mallcloud.order.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 下单结果。前端据此跳到收银台并开始倒计时。
 */
public record OrderCreateVO(String orderNo,
                            BigDecimal totalAmount,
                            BigDecimal discountAmount,
                            BigDecimal payAmount,
                            LocalDateTime expireTime) implements Serializable {
}
