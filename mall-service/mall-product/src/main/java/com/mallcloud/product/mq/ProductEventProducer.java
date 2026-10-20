package com.mallcloud.product.mq;

import com.mallcloud.api.common.event.ProductChangedEvent;
import com.mallcloud.api.common.event.ProductChangeType;
import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * 商品事件生产者。
 * <p>
 * 消息体统一用 JSON 字符串发送，不依赖 Jackson2JsonMessageConverter：
 * 避免"生产者用转换器、消费者没配"导致的类型头解析失败，也让消息在 MQ 控制台里可读。
 * <p>
 * 可靠性约定：本方法在<b>数据库事务提交之后</b>被调用（见 Service 层 @TransactionalEventListener），
 * 避免"消息已发、事务回滚"造成的脏索引。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProductEventProducer {

    private final RabbitTemplate rabbitTemplate;

    public void publishChanged(Long spuId, ProductChangeType type) {
        ProductChangedEvent event = new ProductChangedEvent(spuId, type.name(),
                Instant.now().toEpochMilli(), Instant.now());
        String payload = JsonUtils.toJson(event);
        try {
            rabbitTemplate.convertAndSend(MqConstants.EXCHANGE_PRODUCT, MqConstants.RK_PRODUCT_CHANGED, payload,
                    message -> {
                        message.getMessageProperties().setContentType("application/json");
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        return message;
                    });
            log.info("商品变更事件已发送 spuId={} type={}", spuId, type);
        } catch (Exception e) {
            // 索引同步失败不应影响商品主流程，交由 mall-job 的索引对账任务兜底
            log.error("商品变更事件发送失败 spuId={} type={}", spuId, type, e);
        }
    }
}
