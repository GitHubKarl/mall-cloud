package com.mallcloud.ware.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mallcloud.ware.entity.WareStock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WareStockMapper extends BaseMapper<WareStock> {

    /**
     * 原子锁定库存。用 {@code total - locked >= #{quantity}} 作为条件，由数据库保证并发安全，
     * 不需要先查后改，也不依赖分布式锁。
     *
     * @return 影响行数，0 表示库存不足
     */
    @Update("""
            UPDATE t_ware_stock
               SET locked = locked + #{quantity}, update_time = NOW()
             WHERE sku_id = #{skuId}
               AND deleted = 0
               AND total - locked >= #{quantity}
            """)
    int lockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    /** 解锁：把锁定量还回去 */
    @Update("""
            UPDATE t_ware_stock
               SET locked = GREATEST(locked - #{quantity}, 0), update_time = NOW()
             WHERE sku_id = #{skuId}
               AND deleted = 0
            """)
    int unlockStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);

    /** 实扣：总库存与锁定量同时减少 */
    @Update("""
            UPDATE t_ware_stock
               SET total = GREATEST(total - #{quantity}, 0),
                   locked = GREATEST(locked - #{quantity}, 0),
                   update_time = NOW()
             WHERE sku_id = #{skuId}
               AND deleted = 0
               AND locked >= #{quantity}
            """)
    int deductStock(@Param("skuId") Long skuId, @Param("quantity") Integer quantity);
}
