package com.mallcloud.api.common.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * 订单关闭事件（超时未支付 / 用户主动取消 / 支付失败）。
 * <p>
 * 下游动作互不依赖，各自订阅独立队列：ware 解锁库存、coupon 退回优惠券、notice 发关单提醒。
 * 这样任一消费者故障不会阻塞其它补偿，也不会因共享队列产生消息争抢。
 *
 * @param orderNo  订单号
 * @param userId   下单用户
 * @param reason   关闭原因：TIMEOUT / CANCEL / PAY_FAILED
 * @param skuIds   需要解锁库存的 SKU 列表
 * @param couponId 需要退回的优惠券 ID，可为 null
 */
public record OrderClosedEvent(String orderNo,
                               Long userId,
                               String reason,
                               List<StockItemPayload> skuIds,
                               Long couponId,
                               Instant occurredAt) implements Serializable {

    public record StockItemPayload(Long skuId, Integer quantity) {
    }
}
