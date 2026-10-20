package com.mallcloud.api.ware.dto;

import java.io.Serializable;

/**
 * 库存操作明细。
 */
public record StockItemDTO(Long skuId, Integer quantity) implements Serializable {
}
