-- ============================================================
-- mall_payment 支付库
-- ============================================================
USE `mall_payment`;

DROP TABLE IF EXISTS `t_payment`;
CREATE TABLE `t_payment` (
  `id`            BIGINT         NOT NULL AUTO_INCREMENT,
  `payment_no`    VARCHAR(64)    NOT NULL COMMENT '支付单号',
  `order_no`      VARCHAR(64)    NOT NULL COMMENT '业务订单号',
  `user_id`       BIGINT         NOT NULL,
  `amount`        DECIMAL(10, 2) NOT NULL,
  `channel`       VARCHAR(32)    NOT NULL COMMENT 'MOCK/WECHAT_NATIVE/ALIPAY',
  `status`        TINYINT        NOT NULL DEFAULT 0 COMMENT '0待支付 1成功 2失败 3已退款',
  `trade_no`      VARCHAR(64)    DEFAULT NULL COMMENT '渠道交易号',
  `pay_time`      DATETIME       DEFAULT NULL,
  `callback_body` TEXT           COMMENT '渠道原始回调报文',
  `create_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`   DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT        NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_order_no` (`order_no`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='支付单';
