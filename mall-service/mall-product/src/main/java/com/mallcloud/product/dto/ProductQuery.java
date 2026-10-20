package com.mallcloud.product.dto;

import com.mallcloud.common.core.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品列表查询条件。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQuery extends PageQuery implements Serializable {

    private String keyword;

    private Long categoryId;

    private Long brandId;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    /** null 表示不限，0-下架 1-上架 */
    private Integer status;

    /** 排序字段：sales / price / createTime */
    private String sortField = "createTime";

    private boolean asc = false;
}
