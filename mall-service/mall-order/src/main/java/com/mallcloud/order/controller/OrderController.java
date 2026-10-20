package com.mallcloud.order.controller;

import com.mallcloud.common.core.query.PageQuery;
import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresLogin;
import com.mallcloud.order.dto.OrderCreateDTO;
import com.mallcloud.order.service.OrderService;
import com.mallcloud.order.vo.OrderCreateVO;
import com.mallcloud.order.vo.OrderDetailVO;
import com.mallcloud.order.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单前台接口。
 */
@Tag(name = "订单")
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "提交订单")
    @RequiresLogin
    @PostMapping("/create")
    public R<OrderCreateVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return R.ok(orderService.create(dto));
    }

    @Operation(summary = "我的订单")
    @RequiresLogin
    @GetMapping("/page")
    public R<PageResult<OrderVO>> page(@RequestParam(value = "status", required = false) Integer status,
                                       @Valid PageQuery query) {
        return R.ok(orderService.pageMine(status, query));
    }

    @Operation(summary = "订单详情")
    @RequiresLogin
    @GetMapping("/detail/{orderNo}")
    public R<OrderDetailVO> detail(@PathVariable("orderNo") String orderNo) {
        return R.ok(orderService.detail(orderNo));
    }

    @Operation(summary = "取消订单")
    @RequiresLogin
    @PutMapping("/cancel/{orderNo}")
    public R<Void> cancel(@PathVariable("orderNo") String orderNo) {
        orderService.cancel(orderNo);
        return R.ok();
    }

    @Operation(summary = "确认收货")
    @RequiresLogin
    @PutMapping("/finish/{orderNo}")
    public R<Void> finish(@PathVariable("orderNo") String orderNo) {
        orderService.finish(orderNo);
        return R.ok();
    }
}
