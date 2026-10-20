package com.mallcloud.payment.config;

import com.mallcloud.common.core.constant.MqConstants;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 支付交换机。队列由 order 服务声明并绑定（消费方负责拓扑）。
 */
@Configuration(proxyBeanMethods = false)
public class PaymentMqConfig {

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(MqConstants.EXCHANGE_PAYMENT, true, false);
    }
}
