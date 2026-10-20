package com.mallcloud.ware.controller;

import com.mallcloud.api.ware.dto.StockDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresPerm;
import com.mallcloud.ware.service.WareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 库存前台/后台接口。
 */
@Tag(name = "库存")
@RestController
@RequestMapping("/ware")
@RequiredArgsConstructor
public class WareController {

    private final WareService wareService;

    @Operation(summary = "查询 SKU 可售库存")
    @GetMapping("/public/stock/{skuId}")
    public R<StockDTO> stock(@PathVariable("skuId") Long skuId) {
        return R.ok(wareService.getStock(skuId));
    }

    @Operation(summary = "初始化 / 覆盖库存")
    @RequiresPerm("ware:stock:init")
    @PostMapping("/manage/init")
    public R<Void> init(@RequestParam("skuId") Long skuId,
                        @RequestParam("total") Integer total,
                        @RequestParam(value = "warnStock", required = false) Integer warnStock) {
        wareService.initStock(skuId, total, warnStock);
        return R.ok();
    }
}
