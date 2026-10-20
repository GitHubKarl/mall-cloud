package com.mallcloud.product.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品列表项。
 */
public record ProductListVO(Long id,
                            String name,
                            String subtitle,
                            String mainImage,
                            BigDecimal price,
                            Integer sales,
                            Integer status) implements Serializable {
}
