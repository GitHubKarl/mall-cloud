package com.mallcloud.payment.mq;

import com.mallcloud.api.common.event.PaymentResultEvent;
import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * 支付结果事件生产者。订单服务订阅该事件推进状态机。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishResult(PaymentResultEvent event) {
        try {
            rabbitTemplate.convertAndSend(MqConstants.EXCHANGE_PAYMENT, MqConstants.RK_PAYMENT_RESULT,
                    JsonUtils.toJson(event), message -> {
                        message.getMessageProperties().setContentType("application/json");
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    });
            log.info("支付结果事件已发送 paymentNo={} orderNo={} success={}",
                    event.paymentNo(), event.orderNo(), event.success());
        } catch (Exception e) {
            // 消息发不出去会导致订单永远待支付，必须告警；由 mall-job 的对账任务兜底补偿
            log.error("支付结果事件发送失败 paymentNo={}", event.paymentNo(), e);
        }
    }
}
