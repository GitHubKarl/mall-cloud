-- ============================================================
-- mall_notice 通知库
-- ============================================================
USE `mall_notice`;

DROP TABLE IF EXISTS `t_notice_template`;
CREATE TABLE `t_notice_template` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `code`        VARCHAR(64) NOT NULL COMMENT '模板编码',
  `name`        VARCHAR(64) NOT NULL,
  `channel`     VARCHAR(16) NOT NULL COMMENT 'SITE/SMS/EMAIL',
  `title`       VARCHAR(128) DEFAULT NULL,
  `content`     VARCHAR(1024) NOT NULL COMMENT '用 {key} 占位',
  `status`      TINYINT     NOT NULL DEFAULT 1,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='通知模板';

DROP TABLE IF EXISTS `t_notice_log`;
CREATE TABLE `t_notice_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       DEFAULT NULL,
  `channel`     VARCHAR(16)  NOT NULL,
  `template`    VARCHAR(64)  NOT NULL,
  `title`       VARCHAR(128) DEFAULT NULL,
  `content`     VARCHAR(1024) DEFAULT NULL,
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0待发 1成功 2失败',
  `error_msg`   VARCHAR(500) DEFAULT NULL,
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user_channel` (`user_id`, `channel`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='通知发送流水';

INSERT INTO `t_notice_template` (`code`, `name`, `channel`, `title`, `content`, `status`)
VALUES ('ORDER_PAID', '支付成功通知', 'SITE', '订单支付成功',
        '您的订单 {orderNo} 已支付成功，金额 {amount} 元，我们将尽快为您发货。', 1),
       ('ORDER_CLOSED', '订单关闭通知', 'SITE', '订单已关闭',
        '您的订单 {orderNo} 已关闭，原因：{reason}。', 1),
       ('ORDER_SHIPPED', '发货通知', 'SITE', '订单已发货',
        '您的订单 {orderNo} 已发货，物流单号：{expressNo}。', 1);
