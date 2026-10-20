package com.mallcloud.api.product;

import com.mallcloud.api.product.dto.ProductDocDTO;
import com.mallcloud.api.product.dto.SkuDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 商品服务内部接口。
 */
@FeignClient(name = "mall-product", path = "/product/inner", contextId = "productFeignClient")
public interface ProductFeignClient {

    @GetMapping("/sku/{skuId}")
    R<SkuDTO> getSku(@PathVariable("skuId") Long skuId);

    @PostMapping("/sku/batch")
    R<List<SkuDTO>> listSku(@RequestBody List<Long> skuIds);

    /** 搜索服务回源取详情（ES 只存检索字段，详情以商品库为准） */
    @GetMapping("/doc/{spuId}")
    R<ProductDocDTO> getProductDoc(@PathVariable("spuId") Long spuId);

    /** 全量重建索引时使用：一次性拿到所有 SPU 主键 */
    @GetMapping("/spu/ids")
    R<List<Long>> listAllSpuIds();
}
