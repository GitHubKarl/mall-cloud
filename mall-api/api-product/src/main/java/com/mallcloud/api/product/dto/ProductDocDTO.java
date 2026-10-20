package com.mallcloud.api.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品索引文档。product 服务变更时发给 search 服务写入 ES。
 */
public record ProductDocDTO(Long spuId,
                            String name,
                            String subtitle,
                            Long categoryId,
                            String categoryName,
                            Long brandId,
                            String brandName,
                            BigDecimal price,
                            String mainImage,
                            Integer sales,
                            Integer status,
                            LocalDateTime updateTime) implements Serializable {
}
