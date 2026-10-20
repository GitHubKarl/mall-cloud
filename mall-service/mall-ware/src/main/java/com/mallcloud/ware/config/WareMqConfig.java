package com.mallcloud.ware.config;

import com.mallcloud.common.core.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 库存解锁队列。
 * <p>
 * 与 order 服务「同步 Feign 释放」形成双保险：正常链路走 Feign（快），
 * MQ 兜底防止 order 在释放库存时宕机导致库存永久占用。
 */
@Configuration(proxyBeanMethods = false)
public class WareMqConfig {

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(MqConstants.EXCHANGE_ORDER, true, false);
    }

    @Bean
    public Queue wareUnlockQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_WARE_UNLOCK).build();
    }

    @Bean
    public Binding wareUnlockBinding(Queue wareUnlockQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(wareUnlockQueue).to(orderExchange).with(MqConstants.RK_ORDER_CLOSED);
    }
}
