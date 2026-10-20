package com.mallcloud.product.controller;

import com.mallcloud.common.core.result.PageResult;
import com.mallcloud.common.core.result.R;
import com.mallcloud.product.dto.ProductQuery;
import com.mallcloud.product.service.ProductService;
import com.mallcloud.product.vo.ProductDetailVO;
import com.mallcloud.product.vo.ProductListVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 商品前台接口（网关白名单放行，未登录也可浏览）。
 */
@Tag(name = "商品浏览")
@RestController
@RequestMapping("/product/public")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "商品列表（仅上架）")
    @GetMapping("/list")
    public R<PageResult<ProductListVO>> list(@Valid ProductQuery query) {
        query.setStatus(1);
        return R.ok(productService.page(query));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/detail/{spuId}")
    public R<ProductDetailVO> detail(@PathVariable("spuId") Long spuId) {
        return R.ok(productService.detail(spuId));
    }
}
