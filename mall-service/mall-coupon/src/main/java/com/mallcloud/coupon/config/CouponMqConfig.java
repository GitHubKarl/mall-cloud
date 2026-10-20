package com.mallcloud.coupon.config;

import com.mallcloud.common.core.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 优惠券退回队列：订阅订单关闭事件。
 */
@Configuration(proxyBeanMethods = false)
public class CouponMqConfig {

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(MqConstants.EXCHANGE_ORDER, true, false);
    }

    @Bean
    public Queue couponReleaseQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_COUPON_RELEASE).build();
    }

    @Bean
    public Binding couponReleaseBinding(Queue couponReleaseQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(couponReleaseQueue).to(orderExchange).with(MqConstants.RK_ORDER_CLOSED);
    }
}
