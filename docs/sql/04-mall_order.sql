-- ============================================================
-- mall_order 订单库（Seata AT 发起方，也需要 undo_log）
-- ============================================================
USE `mall_order`;

DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
  `id`                BIGINT         NOT NULL AUTO_INCREMENT,
  `order_no`          VARCHAR(64)    NOT NULL COMMENT '订单号',
  `user_id`           BIGINT         NOT NULL,
  `status`            TINYINT        NOT NULL DEFAULT 0 COMMENT '0待支付 1待发货 2已发货 3已完成 4已取消 5已关闭',
  `total_amount`      DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  `discount_amount`   DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  `pay_amount`        DECIMAL(10, 2) NOT NULL DEFAULT 0.00,
  `coupon_id`         BIGINT         DEFAULT NULL,
  `receiver`          VARCHAR(32)    DEFAULT NULL COMMENT '收货人快照',
  `receiver_phone`    VARCHAR(20)    DEFAULT NULL,
  `receiver_address`  VARCHAR(255)   DEFAULT NULL,
  `remark`            VARCHAR(255)   DEFAULT NULL,
  `expire_time`       DATETIME       DEFAULT NULL COMMENT '支付超时时间',
  `pay_time`          DATETIME       DEFAULT NULL,
  `ship_time`         DATETIME       DEFAULT NULL,
  `finish_time`       DATETIME       DEFAULT NULL,
  `close_time`        DATETIME       DEFAULT NULL,
  `close_reason`      VARCHAR(32)    DEFAULT NULL COMMENT 'TIMEOUT/CANCEL/PAY_FAILED',
  `create_time`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT        NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_status_expire` (`status`, `expire_time`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单';

DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
  `id`           BIGINT         NOT NULL AUTO_INCREMENT,
  `order_no`     VARCHAR(64)    NOT NULL,
  `spu_id`       BIGINT         NOT NULL,
  `sku_id`       BIGINT         NOT NULL,
  `sku_name`     VARCHAR(128)   DEFAULT NULL COMMENT '名称快照',
  `sku_image`    VARCHAR(255)   DEFAULT NULL,
  `price`        DECIMAL(10, 2) NOT NULL,
  `quantity`     INT            NOT NULL,
  `total_amount` DECIMAL(10, 2) NOT NULL,
  `create_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT        NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='订单明细';

DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log` (
  `branch_id`     BIGINT       NOT NULL,
  `xid`           VARCHAR(128) NOT NULL,
  `context`       VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB     NOT NULL,
  `log_status`    INT          NOT NULL,
  `log_created`   DATETIME(6)  NOT NULL,
  `log_modified`  DATETIME(6)  NOT NULL,
  UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='Seata AT 回滚日志';
