package com.mallcloud.ware.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * SKU 库存。available 由 total - locked 实时计算，不落库，避免双写不一致。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ware_stock")
public class WareStock extends BaseEntity {

    private Long skuId;

    private Long warehouseId;

    /** 总库存（含已锁定） */
    private Integer total;

    /** 已锁定库存 */
    private Integer locked;

    /** 预警阈值 */
    private Integer warnStock;
}
