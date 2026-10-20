package com.mallcloud.order.controller;

import com.mallcloud.api.order.dto.OrderDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 内部接口：供 comment（评价校验）、admin（后台查询）、notice 调用。
 */
@RestController
@RequestMapping("/order/inner")
@RequiredArgsConstructor
public class OrderInnerController {

    private final OrderService orderService;

    @GetMapping("/detail/{orderNo}")
    public R<OrderDTO> getByOrderNo(@PathVariable("orderNo") String orderNo) {
        return R.ok(orderService.getByOrderNo(orderNo));
    }

    @GetMapping("/can-comment")
    public R<Boolean> canComment(@RequestParam("orderNo") String orderNo,
                                 @RequestParam("userId") Long userId) {
        return R.ok(orderService.canComment(orderNo, userId));
    }

    /** 供 mall-job 定时调用：兜底关闭超时未支付订单 */
    @PostMapping("/close-expired")
    public R<Integer> closeExpired(@RequestParam(value = "limit", defaultValue = "200") int limit) {
        return R.ok(orderService.closeExpired(limit));
    }
}
