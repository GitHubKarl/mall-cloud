package com.mallcloud.product.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品详情。SKU 列表随详情一起返回，前端无需二次请求。
 */
public record ProductDetailVO(Long id,
                              Long categoryId,
                              Long brandId,
                              String name,
                              String subtitle,
                              String mainImage,
                              String detail,
                              BigDecimal price,
                              Integer sales,
                              Integer status,
                              List<ProductSkuVO> skuList) implements Serializable {
}
