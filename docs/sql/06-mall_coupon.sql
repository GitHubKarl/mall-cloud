-- ============================================================
-- mall_coupon 优惠券库（Seata AT 参与者，必须有 undo_log）
-- ============================================================
USE `mall_coupon`;

DROP TABLE IF EXISTS `t_coupon`;
CREATE TABLE `t_coupon` (
  `id`              BIGINT         NOT NULL AUTO_INCREMENT,
  `name`            VARCHAR(64)    NOT NULL,
  `type`            VARCHAR(16)    NOT NULL COMMENT 'FULL_REDUCTION/DISCOUNT',
  `threshold`       DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '使用门槛',
  `discount_amount` DECIMAL(10, 2) DEFAULT NULL COMMENT '满减金额',
  `discount_rate`   DECIMAL(4, 2)  DEFAULT NULL COMMENT '折扣率，如 0.85',
  `total`           INT            NOT NULL DEFAULT 0 COMMENT '发行量',
  `received`        INT            NOT NULL DEFAULT 0 COMMENT '已领取',
  `per_limit`       INT            NOT NULL DEFAULT 1 COMMENT '每人限领',
  `start_time`      DATETIME       DEFAULT NULL,
  `end_time`        DATETIME       DEFAULT NULL,
  `status`          TINYINT        NOT NULL DEFAULT 1,
  `create_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT        NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='优惠券模板';

DROP TABLE IF EXISTS `t_user_coupon`;
CREATE TABLE `t_user_coupon` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`      BIGINT      NOT NULL,
  `coupon_id`    BIGINT      NOT NULL,
  `status`       TINYINT     NOT NULL DEFAULT 0 COMMENT '0未使用 1已使用 2已过期',
  `order_no`     VARCHAR(64) DEFAULT NULL COMMENT '核销订单号，退回时清空',
  `receive_time` DATETIME    DEFAULT NULL,
  `use_time`     DATETIME    DEFAULT NULL,
  `expire_time`  DATETIME    DEFAULT NULL,
  `create_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`, `status`),
  KEY `idx_order_no` (`order_no`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='用户优惠券';

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
