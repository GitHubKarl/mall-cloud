package com.mallcloud.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SPU（标准产品单元）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_spu")
public class ProductSpu extends BaseEntity {

    private Long categoryId;

    private Long brandId;

    private String name;

    private String subtitle;

    private String mainImage;

    /** 展示价，取所有 SKU 最低价，冗余字段避免列表页 JOIN */
    private BigDecimal price;

    private Integer sales;

    /** 0-下架 1-上架 */
    private Integer status;

    /** 详情富文本 */
    private String detail;
}
