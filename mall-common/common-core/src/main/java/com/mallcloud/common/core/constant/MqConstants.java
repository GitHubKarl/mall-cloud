package com.mallcloud.common.core.constant;

/**
 * 消息队列常量。命名规范：{@code mall.<域>.exchange} / {@code mall.<消费方>.<事件>.queue}。
 * <p>
 * 全链路异步约定：
 * <ul>
 *     <li>商品变更 → 搜索索引同步</li>
 *     <li>订单创建 → 延迟队列超时关单</li>
 *     <li>支付结果 → 订单状态推进（支付服务只发消息，不 Feign 回调订单，避免循环依赖）</li>
 *     <li>订单关闭 → 库存解锁、优惠券退回</li>
 *     <li>业务事件 → 通知中心统一发送</li>
 * </ul>
 */
public interface MqConstants {

    /* ==================== 商品 ==================== */
    String EXCHANGE_PRODUCT = "mall.product.exchange";
    String QUEUE_PRODUCT_SEARCH_SYNC = "mall.search.product.sync.queue";
    String RK_PRODUCT_CHANGED = "product.changed";

    /* ==================== 订单 ==================== */
    String EXCHANGE_ORDER = "mall.order.exchange";
    /** 延迟交换机（需安装 rabbitmq_delayed_message_exchange 插件） */
    String EXCHANGE_ORDER_DELAY = "mall.order.delay.exchange";
    String QUEUE_ORDER_CLOSE_DELAY = "mall.order.close.delay.queue";
    String RK_ORDER_CLOSE_DELAY = "order.close.delay";
    String QUEUE_ORDER_PAYMENT_RESULT = "mall.order.payment.result.queue";
    String RK_PAYMENT_RESULT = "payment.result";

    /** 订单关闭事件：一个 routing key，多个队列各自独立绑定（广播语义，互不影响） */
    String RK_ORDER_CLOSED = "order.closed";
    /** 超时关单消息最终落地的队列（order 自己消费） */
    String QUEUE_ORDER_CLOSE = "mall.order.close.queue";

    /* ==================== 支付 ==================== */
    String EXCHANGE_PAYMENT = "mall.payment.exchange";

    /* ==================== 库存 ==================== */
    String QUEUE_WARE_UNLOCK = "mall.ware.unlock.queue";

    /* ==================== 优惠券 ==================== */
    String QUEUE_COUPON_RELEASE = "mall.coupon.release.queue";

    /* ==================== 通知 ==================== */
    String EXCHANGE_NOTICE = "mall.notice.exchange";
    String QUEUE_NOTICE_SEND = "mall.notice.send.queue";
    String RK_NOTICE_SEND = "notice.send";

    /* ==================== 公共 Header ==================== */
    String HEADER_MESSAGE_ID = "messageId";
    String HEADER_RETRY_COUNT = "retryCount";
}
