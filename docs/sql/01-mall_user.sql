-- ============================================================
-- mall_user 会员库
-- ============================================================
USE `mall_user`;

DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username`        VARCHAR(32)  NOT NULL COMMENT '登录名',
  `password`        VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
  `nickname`        VARCHAR(32)  DEFAULT NULL COMMENT '昵称',
  `phone`           VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  `email`           VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
  `avatar`          VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status`          TINYINT      NOT NULL DEFAULT 1 COMMENT '1-正常 0-禁用',
  `member_level`    TINYINT      NOT NULL DEFAULT 1 COMMENT '会员等级 1~5',
  `total_amount`    BIGINT       NOT NULL DEFAULT 0 COMMENT '累计消费（分）',
  `last_login_time` DATETIME     DEFAULT NULL COMMENT '最后登录时间',
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone` (`phone`),
  KEY `idx_status` (`status`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='会员';

DROP TABLE IF EXISTS `t_user_address`;
CREATE TABLE `t_user_address` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `user_id`     BIGINT       NOT NULL COMMENT '会员ID',
  `receiver`    VARCHAR(32)  NOT NULL COMMENT '收货人',
  `phone`       VARCHAR(20)  NOT NULL COMMENT '联系电话',
  `province`    VARCHAR(32)  NOT NULL,
  `city`        VARCHAR(32)  NOT NULL,
  `district`    VARCHAR(32)  NOT NULL,
  `detail`      VARCHAR(255) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(10)  DEFAULT NULL,
  `is_default`  TINYINT      NOT NULL DEFAULT 0 COMMENT '1-默认',
  `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_user` (`user_id`, `deleted`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='收货地址';
