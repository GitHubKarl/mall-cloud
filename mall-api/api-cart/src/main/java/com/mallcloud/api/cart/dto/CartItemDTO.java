package com.mallcloud.api.cart.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车行。价格不随车存储，下单时以商品服务实时价格为准。
 */
public record CartItemDTO(Long skuId, Integer quantity, Boolean checked, BigDecimal price) implements Serializable {
}
