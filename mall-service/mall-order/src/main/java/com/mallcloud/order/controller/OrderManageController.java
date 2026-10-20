package com.mallcloud.order.controller;

import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresPerm;
import com.mallcloud.order.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单管理接口（后台）。发货也是状态机的一步，必须走 CAS 更新。
 */
@Tag(name = "订单管理")
@RestController
@RequestMapping("/order/manage")
@RequiredArgsConstructor
public class OrderManageController {

    private final OrderService orderService;

    @Operation(summary = "发货")
    @RequiresPerm("order:deliver")
    @PutMapping("/ship/{orderNo}")
    public R<Void> ship(@PathVariable("orderNo") String orderNo) {
        orderService.ship(orderNo);
        return R.ok();
    }
}
