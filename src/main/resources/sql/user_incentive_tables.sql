-- 用户激励系统表结构
-- 创建时间: 2025-09-10
-- 适用于MySQL 8.0及以上版本

-- 1. 用户积分表 (user_point)
CREATE TABLE `user_point` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `total_points` BIGINT NOT NULL DEFAULT 0 COMMENT '总积分',
  `available_points` BIGINT NOT NULL DEFAULT 0 COMMENT '可用积分',
  `consumed_points` BIGINT NOT NULL DEFAULT 0 COMMENT '已消费积分',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  INDEX `idx_total_points` (`total_points` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分表';

-- 2. 用户等级表 (user_level)
CREATE TABLE `user_level` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `level` INT NOT NULL DEFAULT 1 COMMENT '等级',
  `level_name` VARCHAR(50) NOT NULL DEFAULT '新手' COMMENT '等级名称',
  `current_exp` BIGINT NOT NULL DEFAULT 0 COMMENT '当前经验值',
  `next_level_exp` BIGINT NOT NULL DEFAULT 100 COMMENT '下一级所需经验值',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  INDEX `idx_level` (`level` DESC),
  INDEX `idx_exp` (`current_exp` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户等级表';

-- 3. 用户勋章表 (user_badge)
CREATE TABLE `user_badge` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `badge_id` BIGINT UNSIGNED NOT NULL COMMENT '勋章ID',
  `badge_name` VARCHAR(100) NOT NULL COMMENT '勋章名称',
  `badge_description` VARCHAR(500) DEFAULT NULL COMMENT '勋章描述',
  `badge_icon` VARCHAR(255) DEFAULT NULL COMMENT '勋章图标',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-未获得，1-已获得，2-已失效',
  `obtain_time` TIMESTAMP NULL DEFAULT NULL COMMENT '获得时间',
  `expire_time` TIMESTAMP NULL DEFAULT NULL COMMENT '过期时间',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_badge` (`user_id`, `badge_id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_badge_id` (`badge_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户勋章表';