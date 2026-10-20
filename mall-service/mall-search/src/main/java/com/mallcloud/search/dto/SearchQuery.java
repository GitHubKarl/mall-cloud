package com.mallcloud.search.dto;

import com.mallcloud.common.core.query.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 搜索入参。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SearchQuery extends PageQuery implements Serializable {

    private String keyword;

    private Long categoryId;

    private Long brandId;

    private Double minPrice;

    private Double maxPrice;

    /** sales / price / updateTime */
    private String sortField = "sales";

    private boolean asc = false;
}
