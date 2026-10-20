package com.mallcloud.product.controller;

import com.mallcloud.common.core.result.R;
import com.mallcloud.common.security.annotation.RequiresPerm;
import com.mallcloud.product.dto.ProductQuery;
import com.mallcloud.product.dto.ProductSaveDTO;
import com.mallcloud.product.service.ProductService;
import com.mallcloud.product.vo.ProductDetailVO;
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
 * 商品管理接口。走网关 /admin/** 路由时会被 mall-admin 透传过来，
 * 也可以由 mall-admin 直接 Feign 调用。
 */
@Tag(name = "商品管理")
@RestController
@RequestMapping("/product/manage")
@RequiredArgsConstructor
public class ProductManageController {

    private final ProductService productService;

    @Operation(summary = "商品分页（含下架）")
    @RequiresPerm("product:spu:list")
    @GetMapping("/page")
    public R<com.mallcloud.common.core.result.PageResult<com.mallcloud.product.vo.ProductListVO>> page(
            @Valid ProductQuery query) {
        return R.ok(productService.page(query));
    }

    @Operation(summary = "商品详情（含下架）")
    @RequiresPerm("product:spu:list")
    @GetMapping("/detail/{spuId}")
    public R<ProductDetailVO> detail(@PathVariable("spuId") Long spuId) {
        return R.ok(productService.detail(spuId));
    }

    @Operation(summary = "新增或编辑商品")
    @RequiresPerm("product:spu:save")
    @PostMapping("/save")
    public R<Long> save(@Valid @RequestBody ProductSaveDTO dto) {
        return R.ok(productService.save(dto));
    }

    @Operation(summary = "上架 / 下架")
    @RequiresPerm("product:spu:shelf")
    @PutMapping("/status")
    public R<Void> changeStatus(@RequestParam("spuId") Long spuId,
                                @RequestParam("status") Integer status) {
        productService.changeStatus(spuId, status);
        return R.ok();
    }
}
