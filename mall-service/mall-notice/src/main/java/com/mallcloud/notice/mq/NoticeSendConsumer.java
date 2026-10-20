package com.mallcloud.notice.mq;

import com.mallcloud.api.common.event.NoticeSendEvent;
import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import com.mallcloud.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 通知事件消费者。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NoticeSendConsumer {

    private final NoticeService noticeService;

    @RabbitListener(queues = MqConstants.QUEUE_NOTICE_SEND)
    public void onMessage(String payload) {
        NoticeSendEvent event = JsonUtils.parse(payload, NoticeSendEvent.class);
        if (event == null || event.template() == null) {
            log.warn("收到空的通知事件，丢弃: {}", payload);
            return;
        }
        noticeService.send(event);
    }
}
