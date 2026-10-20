package com.mallcloud.order.mq;

import com.mallcloud.api.common.event.OrderClosedEvent;
import com.mallcloud.api.common.event.OrderCreatedEvent;
import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单事件生产者。
 * <p>
 * 超时关单采用「TTL + 死信」而不是延迟插件：绝大多数环境不会预装
 * rabbitmq_delayed_message_exchange，用死信可以在零插件依赖下实现同样的效果。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendDelayClose(String orderNo, long delayMillis) {
        String payload = JsonUtils.toJson(new DelayPayload(orderNo));
        rabbitTemplate.convertAndSend(MqConstants.EXCHANGE_ORDER_DELAY, MqConstants.RK_ORDER_CLOSE_DELAY,
                payload, message -> {
                    message.getMessageProperties().setContentType("application/json");
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    // 单条消息级 TTL，避免与队列级 TTL 互相影响
                    message.getMessageProperties().setExpiration(String.valueOf(delayMillis));
                    return message;
                });
        log.info("超时关单消息已投递 orderNo={} delay={}ms", orderNo, delayMillis);
    }

    public void publishCreated(OrderCreatedEvent event) {
        send(MqConstants.EXCHANGE_ORDER, "order.created", event);
    }

    public void publishClosed(OrderClosedEvent event) {
        send(MqConstants.EXCHANGE_ORDER, MqConstants.RK_ORDER_CLOSED, event);
    }

    private void send(String exchange, String routingKey, Object event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, JsonUtils.toJson(event), message -> {
                message.getMessageProperties().setContentType("application/json");
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            });
            log.info("订单事件已发送 exchange={} rk={}", exchange, routingKey);
        } catch (Exception e) {
            log.error("订单事件发送失败 exchange={} rk={}", exchange, routingKey, e);
        }
    }

    /** 延迟消息体：只需要订单号 */
    public record DelayPayload(String orderNo) {
    }

    /** 批量发布（预留） */
    public void publishClosedBatch(List<OrderClosedEvent> events) {
        events.forEach(this::publishClosed);
    }
}
