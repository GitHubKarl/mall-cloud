package com.mallcloud.order.config;

import com.mallcloud.common.core.constant.MqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单相关 MQ 拓扑。
 * <pre>
 * 支付结果：payment ──▶ mall.payment.exchange ──(payment.result)──▶ mall.order.payment.result.queue
 * 超时关单：order ──▶ mall.order.delay.exchange ──(TTL 30min)──▶ 死信到 mall.order.exchange
 *                                                              └─(order.closed)─┬─▶ mall.order.close.queue
 *                                                                                ├─▶ mall.ware.unlock.queue
 *                                                                                └─▶ mall.coupon.release.queue
 * </pre>
 */
@Configuration(proxyBeanMethods = false)
public class OrderMqConfig {

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(MqConstants.EXCHANGE_ORDER, true, false);
    }

    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(MqConstants.EXCHANGE_ORDER_DELAY, true, false);
    }

    @Bean
    public TopicExchange paymentExchange() {
        return new TopicExchange(MqConstants.EXCHANGE_PAYMENT, true, false);
    }

    /** 延迟队列：消息到期后以 order.closed 路由键死信回业务交换机 */
    @Bean
    public Queue orderCloseDelayQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_ORDER_CLOSE_DELAY)
                .deadLetterExchange(MqConstants.EXCHANGE_ORDER)
                .deadLetterRoutingKey(MqConstants.RK_ORDER_CLOSED)
                .build();
    }

    @Bean
    public Binding orderCloseDelayBinding(Queue orderCloseDelayQueue, DirectExchange orderDelayExchange) {
        return BindingBuilder.bind(orderCloseDelayQueue).to(orderDelayExchange)
                .with(MqConstants.RK_ORDER_CLOSE_DELAY);
    }

    @Bean
    public Queue orderCloseQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_ORDER_CLOSE).build();
    }

    @Bean
    public Binding orderCloseBinding(Queue orderCloseQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCloseQueue).to(orderExchange).with(MqConstants.RK_ORDER_CLOSED);
    }

    @Bean
    public Queue paymentResultQueue() {
        return QueueBuilder.durable(MqConstants.QUEUE_ORDER_PAYMENT_RESULT).build();
    }

    @Bean
    public Binding paymentResultBinding(Queue paymentResultQueue, TopicExchange paymentExchange) {
        return BindingBuilder.bind(paymentResultQueue).to(paymentExchange).with(MqConstants.RK_PAYMENT_RESULT);
    }
}
