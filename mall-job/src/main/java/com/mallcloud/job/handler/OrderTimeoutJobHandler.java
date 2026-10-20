package com.mallcloud.job.handler;

import com.mallcloud.api.order.OrderFeignClient;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 超时关单兜底任务。
 * <p>
 * 正常情况下延迟消息会在 30 分钟后关闭订单；这个任务只是保险丝，
 * 每分钟扫一次"已过期但仍待支付"的订单，防止 MQ 丢消息导致库存被永久占用。
 * <p>
 * 注意：真正的关单逻辑在 order 服务里，本任务只负责触发，不复制业务规则。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJobHandler {

    private final OrderFeignClient orderFeignClient;

    @XxlJob("orderTimeoutHandler")
    public void execute() {
        int limit = XxlJobHelper.getJobParam() == null ? 200 : Integer.parseInt(XxlJobHelper.getJobParam());
        XxlJobHelper.log("开始扫描超时未支付订单, limit={}", limit);
        try {
            Integer closed = orderFeignClient.closeExpired(limit).getDataOrThrow();
            String msg = "本次兜底关闭订单数：" + closed;
            XxlJobHelper.log(msg);
            log.info(msg);
        } catch (Exception e) {
            XxlJobHelper.handleFail("调用订单服务失败：" + e.getMessage());
            log.error("超时关单任务执行失败", e);
        }
    }
}
