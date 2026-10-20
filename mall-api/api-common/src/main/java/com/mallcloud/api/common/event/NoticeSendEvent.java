package com.mallcloud.api.common.event;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;

/**
 * 通知发送事件。所有业务服务把"要发消息"这件事丢进队列，notice 服务统一处理渠道差异。
 *
 * @param userId  接收人，可为 null（如系统公告）
 * @param channel 渠道：SITE / SMS / EMAIL
 * @param template 模板编码，如 ORDER_PAID、ORDER_SHIPPED
 * @param params  模板变量
 */
public record NoticeSendEvent(Long userId,
                              String channel,
                              String template,
                              Map<String, Object> params,
                              Instant occurredAt) implements Serializable {
}
