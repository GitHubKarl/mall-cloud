package com.mallcloud.api.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * SKU 快照。下单时订单服务据此校验价格并冻结金额，库存数量不在这里（属于 ware 服务）。
 */
public record SkuDTO(Long skuId,
                     Long spuId,
                     String skuName,
                     String spuName,
                     BigDecimal price,
                     String image,
                     Integer status) implements Serializable {
}
