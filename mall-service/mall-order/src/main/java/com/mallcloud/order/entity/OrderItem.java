package com.mallcloud.order.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 订单明细。价格与名称均做快照。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_order_item")
public class OrderItem extends BaseEntity {

    private String orderNo;

    private Long spuId;

    private Long skuId;

    private String skuName;

    private String skuImage;

    private BigDecimal price;

    private Integer quantity;

    private BigDecimal totalAmount;
}
