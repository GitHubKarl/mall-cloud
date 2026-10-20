package com.mallcloud.api.common.event;

import java.io.Serializable;
import java.time.Instant;

/**
 * 商品变更事件。product 服务在事务提交后发出，search 服务消费并增量同步 ES。
 *
 * @param spuId     商品 SPU 主键
 * @param changeType 变更类型，见 {@link ProductChangeType}
 * @param version   数据版本号（毫秒时间戳或递增版本），消费端据此做幂等：小于已存版本的消息直接丢弃
 * @param occurredAt 事件产生时间
 */
public record ProductChangedEvent(Long spuId, String changeType, Long version, Instant occurredAt)
        implements Serializable {

    public static ProductChangedEvent of(Long spuId, ProductChangeType type, Long version) {
        return new ProductChangedEvent(spuId, type.name(), version, Instant.now());
    }
}
