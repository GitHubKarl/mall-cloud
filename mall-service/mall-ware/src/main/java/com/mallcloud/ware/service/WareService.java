package com.mallcloud.ware.service;

import com.mallcloud.api.ware.dto.StockDTO;
import com.mallcloud.api.ware.dto.StockItemDTO;

import java.util.List;

/**
 * 库存服务。
 * <p>
 * 三个动作的语义必须严格区分：
 * <ul>
 *     <li>lock：下单预扣，只加 locked</li>
 *     <li>unlock：订单关闭，减 locked</li>
 *     <li>deduct：支付成功，total 与 locked 同时减</li>
 * </ul>
 */
public interface WareService {

    boolean lock(List<StockItemDTO> items, String orderNo);

    boolean unlock(String orderNo);

    boolean deduct(String orderNo);

    StockDTO getStock(Long skuId);

    void initStock(Long skuId, Integer total, Integer warnStock);
}
