-- ============================================================
-- mall_comment 评价库
-- ============================================================
USE `mall_comment`;

DROP TABLE IF EXISTS `t_comment`;
CREATE TABLE `t_comment` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT      NOT NULL,
  `order_no`    VARCHAR(64) NOT NULL,
  `spu_id`      BIGINT      NOT NULL,
  `sku_id`      BIGINT      NOT NULL,
  `score`       TINYINT     NOT NULL COMMENT '1~5 星',
  `content`     VARCHAR(1000) NOT NULL,
  `images`      TEXT        COMMENT '图片 JSON 数组',
  `anonymous`   TINYINT     NOT NULL DEFAULT 0,
  `status`      TINYINT     NOT NULL DEFAULT 1 COMMENT '0待审核 1通过 2拒绝',
  `reply`       VARCHAR(500) DEFAULT NULL COMMENT '商家回复',
  `reply_time`  DATETIME    DEFAULT NULL,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_sku` (`order_no`, `sku_id`),
  KEY `idx_spu_status` (`spu_id`, `status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品评价';
