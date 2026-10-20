package com.mallcloud.order.mq;

import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import com.mallcloud.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 超时关单消费者。
 * <p>
 * 消息来源有两条：
 * <ol>
 *     <li>下单时投递的 TTL 消息，30 分钟后死信到这里</li>
 *     <li>mall-job 的兜底扫描任务（防止消息丢失导致订单永远挂着）</li>
 * </ol>
 * 两条路都调用幂等的 {@code closeTimeout}，重复消费无副作用。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCloseConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = MqConstants.QUEUE_ORDER_CLOSE)
    public void onMessage(String payload) {
        String orderNo = extractOrderNo(payload);
        if (orderNo == null) {
            log.warn("无法解析关单消息，丢弃: {}", payload);
            return;
        }
        log.info("处理关单消息 orderNo={}", orderNo);
        orderService.closeTimeout(orderNo);
    }

    /** 消息体可能是 {"orderNo": "..."}，也可能带更多字段，统一只取 orderNo */
    private String extractOrderNo(String payload) {
        try {
            return JsonUtils.readTree(payload).path("orderNo").asText(null);
        } catch (Exception e) {
            return null;
        }
    }
}
