package com.mallcloud.search.config;

import com.mallcloud.common.core.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 搜索索引同步队列。独立队列 + 独立路由键，商品事件不会干扰订单链路。
 */
@Configuration(proxyBeanMethods = false)
public class SearchMqConfig {

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(MqConstants.EXCHANGE_PRODUCT, true, false);
    }

    @Bean
    public Queue productSearchSyncQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_PRODUCT_SEARCH_SYNC).build();
    }

    @Bean
    public Binding productSearchSyncBinding(Queue productSearchSyncQueue, TopicExchange productExchange) {
        return BindingBuilder.bind(productSearchSyncQueue).to(productExchange)
                .with(MqConstants.RK_PRODUCT_CHANGED);
    }
}
