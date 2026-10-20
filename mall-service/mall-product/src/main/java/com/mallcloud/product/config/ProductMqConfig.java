package com.mallcloud.product.config;

import com.mallcloud.common.core.constant.MqConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 商品交换机声明。队列与绑定由消费方（mall-search）声明，生产者只保证交换机存在。
 */
@Configuration(proxyBeanMethods = false)
public class ProductMqConfig {

    @Bean
    public TopicExchange productExchange() {
        return new TopicExchange(MqConstants.EXCHANGE_PRODUCT, true, false);
    }
}
