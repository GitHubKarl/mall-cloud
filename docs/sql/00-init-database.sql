-- ============================================================
-- mall-cloud 数据库初始化
-- 执行顺序：01 -> 09（也可直接整目录执行）
-- 约定：一服务一库，禁止跨库 JOIN；所有表统一 id/create_time/update_time/deleted
-- ============================================================

CREATE DATABASE IF NOT EXISTS `mall_user` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `mall_product` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `mall_ware` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `mall_order` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `mall_payment` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `mall_coupon` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `mall_comment` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `mall_notice` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS `mall_admin` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- Seata Server 自身的库（AT 模式 + db 存储时使用）
CREATE DATABASE IF NOT EXISTS `seata` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
