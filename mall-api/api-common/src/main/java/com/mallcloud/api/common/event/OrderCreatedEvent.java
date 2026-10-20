package com.mallcloud.api.common.event;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * 订单创建成功事件。下游：通知中心（下单提醒）、数据统计、营销自动发券。
 */
public record OrderCreatedEvent(String orderNo,
                                Long userId,
                                BigDecimal payAmount,
                                List<OrderItemPayload> items,
                                Instant occurredAt) implements Serializable {

    public record OrderItemPayload(Long skuId, String skuName, Integer quantity, BigDecimal price) {
    }
}
