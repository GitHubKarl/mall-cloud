package com.mallcloud.api.ware.dto;

import java.io.Serializable;

/**
 * 库存快照。
 *
 * @param available 可售库存 = 总库存 - 锁定库存
 * @param locked    已锁定库存
 */
public record StockDTO(Long skuId, Integer total, Integer locked, Integer available) implements Serializable {
}
