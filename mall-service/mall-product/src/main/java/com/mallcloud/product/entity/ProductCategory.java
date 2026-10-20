package com.mallcloud.product.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 商品类目（三级树）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_product_category")
public class ProductCategory extends BaseEntity {

    private Long parentId;

    private String name;

    private Integer level;

    private String icon;

    private Integer sort;

    private Integer status;
}
