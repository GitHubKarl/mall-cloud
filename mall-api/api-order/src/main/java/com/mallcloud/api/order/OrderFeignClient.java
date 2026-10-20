package com.mallcloud.api.order;

import com.mallcloud.api.order.dto.OrderDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 订单服务内部接口。
 * <p>
 * 注意：payment 服务不调用本接口（支付结果通过 MQ 事件回传），此处不提供"标记已支付"方法，
 * 从契约层面杜绝 order ⇄ payment 的循环依赖。
 */
@FeignClient(name = "mall-order", path = "/order/inner", contextId = "orderFeignClient")
public interface OrderFeignClient {

    @GetMapping("/detail/{orderNo}")
    R<OrderDTO> getByOrderNo(@PathVariable("orderNo") String orderNo);

    /** 评价前置校验：订单属于该用户且已完成 */
    @GetMapping("/can-comment")
    R<Boolean> canComment(@RequestParam("orderNo") String orderNo,
                          @RequestParam("userId") Long userId);

    /** 兜底关单：扫描已超时的待支付订单并关闭，由 mall-job 定时触发 */
    @PostMapping("/close-expired")
    R<Integer> closeExpired(@RequestParam("limit") int limit);
}
