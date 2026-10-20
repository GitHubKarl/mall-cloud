-- ============================================================
-- mall_admin 后台管理库
-- 默认账号：admin / admin123（BCrypt 密文，首次登录后请立即修改）
-- ============================================================
USE `mall_admin`;

DROP TABLE IF EXISTS `t_admin_user`;
CREATE TABLE `t_admin_user` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT,
  `username`        VARCHAR(32)  NOT NULL,
  `password`        VARCHAR(100) NOT NULL COMMENT 'BCrypt 密文',
  `nickname`        VARCHAR(32)  DEFAULT NULL,
  `avatar`          VARCHAR(255) DEFAULT NULL,
  `status`          TINYINT      NOT NULL DEFAULT 1,
  `role_code`       VARCHAR(64)  NOT NULL DEFAULT 'OPERATOR' COMMENT '角色编码',
  `perms`           VARCHAR(1024) DEFAULT NULL COMMENT '权限标识，逗号分隔',
  `last_login_time` DATETIME     DEFAULT NULL,
  `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT      NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT ='后台管理员';

-- 明文 admin123 的 BCrypt 密文
INSERT INTO `t_admin_user` (`username`, `password`, `nickname`, `status`, `role_code`, `perms`)
VALUES ('admin', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2',
        '超级管理员', 1, 'SUPER_ADMIN', '*');
