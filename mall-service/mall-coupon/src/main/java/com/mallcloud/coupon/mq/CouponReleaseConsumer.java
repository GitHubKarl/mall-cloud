package com.mallcloud.coupon.mq;

import com.mallcloud.api.common.event.OrderClosedEvent;
import com.mallcloud.common.core.constant.MqConstants;
import com.mallcloud.common.core.util.JsonUtils;
import com.mallcloud.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * 订单关闭 → 退回优惠券。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CouponReleaseConsumer {

    private final CouponService couponService;

    @RabbitListener(queues = MqConstants.QUEUE_COUPON_RELEASE)
    public void onMessage(String payload) {
        OrderClosedEvent event = JsonUtils.parse(payload, OrderClosedEvent.class);
        if (event == null || event.orderNo() == null) {
            log.warn("收到空的订单关闭事件，丢弃: {}", payload);
            return;
        }
        if (event.couponId() == null) {
            return;
        }
        log.info("退回优惠券 orderNo={} couponId={}", event.orderNo(), event.couponId());
        couponService.release(event.orderNo());
    }
}
