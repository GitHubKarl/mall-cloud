package com.mallcloud.ware.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.mallcloud.api.ware.dto.StockDTO;
import com.mallcloud.api.ware.dto.StockItemDTO;
import com.mallcloud.common.core.exception.BizException;
import com.mallcloud.common.core.result.ResultCode;
import com.mallcloud.ware.entity.WareStock;
import com.mallcloud.ware.entity.WareStockLog;
import com.mallcloud.ware.mapper.WareStockLogMapper;
import com.mallcloud.ware.mapper.WareStockMapper;
import com.mallcloud.ware.service.WareService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 库存服务实现。
 * <p>
 * 幂等设计：所有写操作以 {@code orderNo + type} 作为业务唯一键，重复消息/重复请求直接返回成功。
 * 这是 MQ 消费、Feign 重试、Seata 回滚三处场景共同的安全网。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WareServiceImpl implements WareService {

    private final WareStockMapper stockMapper;
    private final WareStockLogMapper stockLogMapper;

    private static final String TYPE_LOCK = "LOCK";
    private static final String TYPE_UNLOCK = "UNLOCK";
    private static final String TYPE_DEDUCT = "DEDUCT";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean lock(List<StockItemDTO> items, String orderNo) {
        if (items == null || items.isEmpty()) {
            throw new BizException(ResultCode.ORDER_ITEM_EMPTY);
        }
        if (existsLog(orderNo, TYPE_LOCK)) {
            log.info("库存已锁定，忽略重复请求 orderNo={}", orderNo);
            return true;
        }

        for (StockItemDTO item : items) {
            int rows = stockMapper.lockStock(item.skuId(), item.quantity());
            if (rows == 0) {
                log.warn("库存不足 orderNo={} skuId={} quantity={}", orderNo, item.skuId(), item.quantity());
                throw new BizException(ResultCode.WARE_STOCK_INSUFFICIENT);
            }
            saveLog(orderNo, item.skuId(), TYPE_LOCK, item.quantity());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unlock(String orderNo) {
        if (existsLog(orderNo, TYPE_UNLOCK) || existsLog(orderNo, TYPE_DEDUCT)) {
            log.info("库存已解锁或已实扣，忽略 orderNo={}", orderNo);
            return true;
        }
        List<WareStockLog> locks = listLog(orderNo, TYPE_LOCK);
        for (WareStockLog lockLog : locks) {
            stockMapper.unlockStock(lockLog.getSkuId(), lockLog.getQuantity());
            saveLog(orderNo, lockLog.getSkuId(), TYPE_UNLOCK, lockLog.getQuantity());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deduct(String orderNo) {
        if (existsLog(orderNo, TYPE_DEDUCT)) {
            return true;
        }
        List<WareStockLog> locks = listLog(orderNo, TYPE_LOCK);
        for (WareStockLog lockLog : locks) {
            int rows = stockMapper.deductStock(lockLog.getSkuId(), lockLog.getQuantity());
            if (rows == 0) {
                // 走到这里说明库存已被其它流程释放，属于严重数据不一致，交由对账任务处理
                log.error("实扣库存失败，锁定记录缺失 orderNo={} skuId={}", orderNo, lockLog.getSkuId());
                throw new BizException(ResultCode.WARE_LOCK_FAILED, "库存实扣失败，请走人工对账");
            }
            saveLog(orderNo, lockLog.getSkuId(), TYPE_DEDUCT, lockLog.getQuantity());
        }
        return true;
    }

    @Override
    public StockDTO getStock(Long skuId) {
        WareStock stock = stockMapper.selectOne(Wrappers.<WareStock>lambdaQuery()
                .eq(WareStock::getSkuId, skuId)
                .last("limit 1"));
        if (stock == null) {
            return new StockDTO(skuId, 0, 0, 0);
        }
        int total = stock.getTotal() == null ? 0 : stock.getTotal();
        int locked = stock.getLocked() == null ? 0 : stock.getLocked();
        return new StockDTO(skuId, total, locked, Math.max(total - locked, 0));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initStock(Long skuId, Integer total, Integer warnStock) {
        WareStock exists = stockMapper.selectOne(Wrappers.<WareStock>lambdaQuery()
                .eq(WareStock::getSkuId, skuId).last("limit 1"));
        if (exists == null) {
            WareStock stock = new WareStock();
            stock.setSkuId(skuId);
            stock.setWarehouseId(1L);
            stock.setTotal(total);
            stock.setLocked(0);
            stock.setWarnStock(warnStock == null ? 10 : warnStock);
            stockMapper.insert(stock);
            return;
        }
        exists.setTotal(total);
        stockMapper.updateById(exists);
    }

    private boolean existsLog(String orderNo, String type) {
        Long count = stockLogMapper.selectCount(Wrappers.<WareStockLog>lambdaQuery()
                .eq(WareStockLog::getOrderNo, orderNo)
                .eq(WareStockLog::getType, type));
        return count != null && count > 0;
    }

    private List<WareStockLog> listLog(String orderNo, String type) {
        return stockLogMapper.selectList(Wrappers.<WareStockLog>lambdaQuery()
                .eq(WareStockLog::getOrderNo, orderNo)
                .eq(WareStockLog::getType, type));
    }

    private void saveLog(String orderNo, Long skuId, String type, Integer quantity) {
        WareStockLog log = new WareStockLog();
        log.setOrderNo(orderNo);
        log.setSkuId(skuId);
        log.setType(type);
        log.setQuantity(quantity);
        log.setStatus(1);
        stockLogMapper.insert(log);
    }
}
