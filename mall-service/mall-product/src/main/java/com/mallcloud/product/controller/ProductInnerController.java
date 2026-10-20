package com.mallcloud.product.controller;

import com.mallcloud.api.product.dto.ProductDocDTO;
import com.mallcloud.api.product.dto.SkuDTO;
import com.mallcloud.common.core.result.R;
import com.mallcloud.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 内部接口：供 order（校验价格）、search（回源详情）、cart 调用。
 */
@RestController
@RequestMapping("/product/inner")
@RequiredArgsConstructor
public class ProductInnerController {

    private final ProductService productService;

    @GetMapping("/sku/{skuId}")
    public R<SkuDTO> getSku(@PathVariable("skuId") Long skuId) {
        return R.ok(productService.getSku(skuId));
    }

    @PostMapping("/sku/batch")
    public R<List<SkuDTO>> listSku(@RequestBody List<Long> skuIds) {
        return R.ok(productService.listSku(skuIds));
    }

    @GetMapping("/doc/{spuId}")
    public R<ProductDocDTO> getProductDoc(@PathVariable("spuId") Long spuId) {
        return R.ok(productService.getDoc(spuId));
    }

    @GetMapping("/spu/ids")
    public R<List<Long>> allSpuIds() {
        return R.ok(productService.listAllSpuIds());
    }
}
