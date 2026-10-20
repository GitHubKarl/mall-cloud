package com.mallcloud.search.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 搜索结果项。
 */
public record SearchItemVO(Long spuId,
                           String name,
                           String subtitle,
                           Double price,
                           String mainImage,
                           Integer sales,
                           LocalDateTime updateTime) implements Serializable {
}
