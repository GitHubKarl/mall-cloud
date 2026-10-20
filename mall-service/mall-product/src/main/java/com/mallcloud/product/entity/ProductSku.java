package com.mallcloud.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

/**
 * 商品 SKU（最小库存单元）。库存数量不在此表，由 ware 服务独立维护。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_sku")
public class ProductSku extends BaseEntity {

    private Long spuId;

    private String skuName;

    private String skuCode;

    private BigDecimal price;

    private String image;

    /** 规格 JSON，如 {"颜色":"白色","内存":"256G"} */
    private String attrs;

    /** 0-禁用 1-启用 */
    private Integer status;
}
