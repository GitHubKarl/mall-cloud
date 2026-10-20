package com.mallcloud.order.mq;

import com.mallcloud.api.common.event.PaymentResultEvent;
import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import com.mallcloud.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 支付结果消费者。
 * <p>
 * 这是替代「支付服务 Feign 回调订单」的关键一环：支付侧只负责发事件，
 * 订单侧决定如何推进状态机，两边没有编译期依赖，也就不可能形成循环依赖。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentResultConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = MqConstants.QUEUE_ORDER_PAYMENT_RESULT)
    public void onMessage(String payload) {
        PaymentResultEvent event = JsonUtils.parse(payload, PaymentResultEvent.class);
        if (event == null || event.orderNo() == null) {
            log.warn("收到空的支付结果消息，丢弃: {}", payload);
            return;
        }
        log.info("收到支付结果 orderNo={} success={}", event.orderNo(), event.success());
        orderService.handlePaymentResult(event);
    }
}
