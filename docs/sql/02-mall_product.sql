-- ============================================================
-- mall_product 商品库
-- ============================================================
USE `mall_product`;

DROP TABLE IF EXISTS `t_product_category`;
CREATE TABLE `t_product_category` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT,
  `parent_id`   BIGINT      NOT NULL DEFAULT 0 COMMENT '父级，0 为根',
  `name`        VARCHAR(64) NOT NULL,
  `level`       TINYINT     NOT NULL DEFAULT 1 COMMENT '层级 1~3',
  `icon`        VARCHAR(255) DEFAULT NULL,
  `sort`        INT         NOT NULL DEFAULT 0,
  `status`      TINYINT     NOT NULL DEFAULT 1,
  `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_parent` (`parent_id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品类目';

DROP TABLE IF EXISTS `t_product_spu`;
CREATE TABLE `t_product_spu` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT,
  `category_id` BIGINT         NOT NULL,
  `brand_id`    BIGINT         DEFAULT NULL,
  `name`        VARCHAR(128)   NOT NULL COMMENT '商品名称',
  `subtitle`    VARCHAR(255)   DEFAULT NULL COMMENT '副标题',
  `main_image`  VARCHAR(255)   DEFAULT NULL,
  `price`       DECIMAL(10, 2) NOT NULL DEFAULT 0.00 COMMENT '展示价（SKU 最低价）',
  `sales`       INT            NOT NULL DEFAULT 0 COMMENT '销量',
  `status`      TINYINT        NOT NULL DEFAULT 0 COMMENT '1-上架 0-下架',
  `detail`      TEXT           COMMENT '详情富文本',
  `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT        NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`, `status`),
  KEY `idx_status_sales` (`status`, `sales`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品 SPU';

DROP TABLE IF EXISTS `t_product_sku`;
CREATE TABLE `t_product_sku` (
  `id`          BIGINT         NOT NULL AUTO_INCREMENT,
  `spu_id`      BIGINT         NOT NULL,
  `sku_name`    VARCHAR(128)   NOT NULL,
  `sku_code`    VARCHAR(64)    DEFAULT NULL,
  `price`       DECIMAL(10, 2) NOT NULL,
  `image`       VARCHAR(255)   DEFAULT NULL,
  `attrs`       VARCHAR(512)   DEFAULT NULL COMMENT '规格 JSON',
  `status`      TINYINT        NOT NULL DEFAULT 1,
  `create_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT        NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_spu` (`spu_id`),
  UNIQUE KEY `uk_sku_code` (`sku_code`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='商品 SKU';
