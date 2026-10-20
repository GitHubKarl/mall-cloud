package com.mallcloud.notice.config;

import com.mallcloud.common.core.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 通知队列。所有业务服务把"要发消息"投到 mall.notice.exchange，
 * 由本服务统一处理渠道差异，业务方不需要知道短信还是邮件。
 */
@Configuration(proxyBeanMethods = false)
public class NoticeMqConfig {

    @Bean
    public TopicExchange noticeExchange() {
        return new TopicExchange(MqConstants.EXCHANGE_NOTICE, true, false);
    }

    @Bean
    public Queue noticeSendQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_NOTICE_SEND).build();
    }

    @Bean
    public Binding noticeSendBinding(Queue noticeSendQueue, TopicExchange noticeExchange) {
        return BindingBuilder.bind(noticeSendQueue).to(noticeExchange).with(MqConstants.RK_NOTICE_SEND);
    }
}
