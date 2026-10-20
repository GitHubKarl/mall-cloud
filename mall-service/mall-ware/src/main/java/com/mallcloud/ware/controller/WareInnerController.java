package com.mallcloud.ware.controller;

import com.mallcloud.api.ware.dto.StockDTO;
import com.mallcloud.api.ware.dto.StockItemDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.ware.service.WareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部接口：由 order 服务在 Seata 全局事务中调用。
 */
@RestController
@RequestMapping("/ware/inner")
@RequiredArgsConstructor
public class WareInnerController {

    private final WareService wareService;

    @PostMapping("/lock")
    public R<Boolean> lock(@RequestBody List<StockItemDTO> items, @RequestParam("orderNo") String orderNo) {
        return R.ok(wareService.lock(items, orderNo));
    }

    @PostMapping("/unlock")
    public R<Boolean> unlock(@RequestParam("orderNo") String orderNo) {
        return R.ok(wareService.unlock(orderNo));
    }

    @PostMapping("/deduct")
    public R<Boolean> deduct(@RequestParam("orderNo") String orderNo) {
        return R.ok(wareService.deduct(orderNo));
    }

    @GetMapping("/stock/{skuId}")
    public R<StockDTO> getStock(@PathVariable("skuId") Long skuId) {
        return R.ok(wareService.getStock(skuId));
    }
}
