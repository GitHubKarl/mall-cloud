package com.mallcloud.api.ware;

import com.mallcloud.api.ware.dto.StockDTO;
import com.mallcloud.api.ware.dto.StockItemDTO;
import com.mallcloud.common.core.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 库存服务内部接口。
 * <p>
 * lock / unlock 是 Seata 全局事务的参与者，必须保证接口幂等（orderNo 作为幂等键）。
 */
@FeignClient(name = "mall-ware", path = "/ware/inner", contextId = "wareFeignClient")
public interface WareFeignClient {

    /** 下单锁定库存（幂等：同一 orderNo 重复调用不会重复扣减） */
    @PostMapping("/lock")
    R<Boolean> lock(@RequestBody List<StockItemDTO> items, @RequestParam("orderNo") String orderNo);

    /** 释放锁定库存（订单关闭 / 支付失败） */
    @PostMapping("/unlock")
    R<Boolean> unlock(@RequestParam("orderNo") String orderNo);

    /** 支付成功后把锁定库存转为实扣 */
    @PostMapping("/deduct")
    R<Boolean> deduct(@RequestParam("orderNo") String orderNo);

    @GetMapping("/stock/{skuId}")
    R<StockDTO> getStock(@PathVariable("skuId") Long skuId);
}
