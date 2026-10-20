-- ============================================================
-- mall_ware 库存库（Seata AT 参与者，必须有 undo_log）
-- ============================================================
USE `mall_ware`;

DROP TABLE IF EXISTS `t_ware_stock`;
CREATE TABLE `t_ware_stock` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT,
  `sku_id`       BIGINT   NOT NULL,
  `warehouse_id` BIGINT   NOT NULL DEFAULT 1,
  `total`        INT      NOT NULL DEFAULT 0 COMMENT '总库存（含锁定）',
  `locked`       INT      NOT NULL DEFAULT 0 COMMENT '锁定库存',
  `warn_stock`   INT      NOT NULL DEFAULT 10 COMMENT '预警阈值',
  `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT  NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sku_warehouse` (`sku_id`, `warehouse_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='SKU 库存';

DROP TABLE IF EXISTS `t_ware_stock_log`;
CREATE TABLE `t_ware_stock_log` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `order_no`    VARCHAR(64) NOT NULL COMMENT '订单号（幂等键）',
  `sku_id`      BIGINT      NOT NULL,
  `type`        VARCHAR(16) NOT NULL COMMENT 'LOCK/UNLOCK/DEDUCT',
  `quantity`    INT         NOT NULL,
  `status`      TINYINT     NOT NULL DEFAULT 1,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_type_sku` (`order_no`, `type`, `sku_id`),
  KEY `idx_sku` (`sku_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='库存流水';

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `branch_id`     BIGINT       NOT NULL COMMENT 'branch transaction id',
  `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
  `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context',
  `rollback_info` LONGBLOB     NOT NULL COMMENT 'rollback info',
  `log_status`    INT          NOT NULL COMMENT '0:normal status,1:defense status',
  `log_created`   DATETIME(6)  NOT NULL COMMENT 'create datetime',
  `log_modified`  DATETIME(6)  NOT NULL COMMENT 'modify datetime',
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='Seata AT 回滚日志';
