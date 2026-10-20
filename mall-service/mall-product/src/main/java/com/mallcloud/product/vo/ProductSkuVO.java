package com.mallcloud.product.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SKU 展示项。
 */
public record ProductSkuVO(Long skuId,
                           String skuName,
                           BigDecimal price,
                           String image,
                           String attrs,
                           Integer status) implements Serializable {
}
