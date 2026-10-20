package com.mallcloud.ware.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.mallcloud.common.mybatis.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 库存流水。既是审计记录，也是 lock/unlock/deduct 的幂等依据（orderNo + type 唯一）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_ware_stock_log")
public class WareStockLog extends BaseEntity {

    private String orderNo;

    private Long skuId;

    /** LOCK-锁定 UNLOCK-解锁 DEDUCT-实扣 */
    private String type;

    private Integer quantity;

    /** 0-未处理 1-已处理 */
    private Integer status;
}
