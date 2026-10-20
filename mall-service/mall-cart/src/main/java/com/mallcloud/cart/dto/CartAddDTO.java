package com.mallcloud.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 加入购物车。
 */
@Data
public class CartAddDTO implements Serializable {

    @NotNull(message = "skuId 不能为空")
    private Long skuId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为 1")
    @Max(value = 999, message = "单个商品最多 999 件")
    private Integer quantity;
}
