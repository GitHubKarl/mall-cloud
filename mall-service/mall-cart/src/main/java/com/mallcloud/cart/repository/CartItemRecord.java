package com.mallcloud.cart.repository;

import java.io.Serializable;

/**
 * 购物车存储模型（Redis Hash 的 value）。
 * <p>
 * 只存"用户的选择"，不存价格与库存——这两样随时会变，必须实时回源，否则会出现
 * "购物车里是 99，下单变 129" 的纠纷。
 */
public record CartItemRecord(Long skuId, Integer quantity, Boolean checked) implements Serializable {
}
