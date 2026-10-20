package com.mallcloud.api.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * 支付结果事件。由 payment 服务在收到渠道回调并更新支付单后发出，order 服务消费。
 * <p>
 * 关键设计：payment **不**通过 Feign 回调 order，避免 order ⇄ payment 形成编译期循环依赖。
 *
 * @param paymentNo 支付单号
 * @param orderNo   业务订单号
 * @param userId    付款用户
 * @param success   是否支付成功
 * @param tradeNo   渠道交易号
 * @param amount    实付金额
 * @param paidAt    支付完成时间
 */
public record PaymentResultEvent(String paymentNo,
                                 String orderNo,
                                 Long userId,
                                 boolean success,
                                 String tradeNo,
                                 BigDecimal amount,
                                 Instant paidAt) implements Serializable {
}
