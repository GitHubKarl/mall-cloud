package com.mallcloud.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

/**
 * 下单入参。商品来源为购物车勾选项，避免前端传价格造成篡改。
 */
@Data
public class OrderCreateDTO implements Serializable {

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** 可选，使用优惠券 */
    private Long couponId;

    private String remark;
}
