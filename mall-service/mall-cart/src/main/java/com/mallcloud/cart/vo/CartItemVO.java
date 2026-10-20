package com.mallcloud.cart.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车行展示。
 *
 * @param valid 商品是否仍可售（下架/删除时为 false，前端置灰）
 */
public record CartItemVO(Long skuId,
                         String skuName,
                         String image,
                         BigDecimal price,
                         Integer quantity,
                         Boolean checked,
                         BigDecimal subtotal,
                         boolean valid) implements Serializable {
}
