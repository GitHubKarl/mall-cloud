package com.mallcloud.product.convert;

import com.mallcloud.api.product.dto.ProductDocDTO;
import com.mallcloud.api.product.dto.SkuDTO;
import com.mallcloud.product.entity.ProductSku;
import com.mallcloud.product.entity.ProductSpu;
import com.mallcloud.product.vo.ProductDetailVO;
import com.mallcloud.product.vo.ProductListVO;
import com.mallcloud.product.vo.ProductSkuVO;

import java.util.List;

/**
 * 商品对象转换。集中在一处，避免 Controller / Service 里散落 set 赋值。
 */
public final class ProductConvert {

    private ProductConvert() {
    }

    public static ProductListVO toListVO(ProductSpu spu) {
        return new ProductListVO(spu.getId(), spu.getName(), spu.getSubtitle(), spu.getMainImage(),
                spu.getPrice(), spu.getSales(), spu.getStatus());
    }

    public static ProductSkuVO toSkuVO(ProductSku sku) {
        return new ProductSkuVO(sku.getId(), sku.getSkuName(), sku.getPrice(), sku.getImage(),
                sku.getAttrs(), sku.getStatus());
    }

    public static ProductDetailVO toDetailVO(ProductSpu spu, List<ProductSku> skus) {
        return new ProductDetailVO(spu.getId(), spu.getCategoryId(), spu.getBrandId(), spu.getName(),
                spu.getSubtitle(), spu.getMainImage(), spu.getDetail(), spu.getPrice(), spu.getSales(),
                spu.getStatus(), skus.stream().map(ProductConvert::toSkuVO).toList());
    }

    public static SkuDTO toSkuDTO(ProductSku sku, ProductSpu spu) {
        return new SkuDTO(sku.getId(), sku.getSpuId(), sku.getSkuName(),
                spu == null ? null : spu.getName(), sku.getPrice(), sku.getImage(), sku.getStatus());
    }

    public static ProductDocDTO toDocDTO(ProductSpu spu) {
        return new ProductDocDTO(spu.getId(), spu.getName(), spu.getSubtitle(), spu.getCategoryId(), null,
                spu.getBrandId(), null, spu.getPrice(), spu.getMainImage(), spu.getSales(), spu.getStatus(),
                spu.getUpdateTime());
    }
}
