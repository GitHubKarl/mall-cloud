package com.mallcloud.ware.mq;

import com.mallcloud.api.common.event.OrderClosedEvent;
import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import com.mallcloud.ware.service.WareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单关闭消费者：释放锁定库存。
 * <p>
 * 消费端幂等由 {@link WareService#unlock(String)} 内部保证（按 orderNo 查流水）；
 * 异常直接抛出交由 RabbitMQ 重试（application.yml 配置了重试次数与死信）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderClosedConsumer {

    private final WareService wareService;

    @RabbitListener(queues = MqConstants.QUEUE_WARE_UNLOCK)
    public void onMessage(String payload) {
        OrderClosedEvent event = JsonUtils.parse(payload, OrderClosedEvent.class);
        if (event == null || event.orderNo() == null) {
            log.warn("收到空的订单关闭事件，丢弃: {}", payload);
            return;
        }
        log.info("释放库存 orderNo={} reason={}", event.orderNo(), event.reason());
        wareService.unlock(event.orderNo());
    }
}
