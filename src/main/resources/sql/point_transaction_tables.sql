-- 积分系统优化表结构
-- 创建时间: 2025-09-23
-- 适用于MySQL 8.0及以上版本

-- 1. 积分流水表 (point_transaction)
-- 用来记录每一次积分变动，方便审计和回滚
CREATE TABLE `point_transaction` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `change_amount` BIGINT NOT NULL COMMENT '积分变动数量，正负数',
  `change_type` VARCHAR(50) NOT NULL COMMENT '变动类型：签到、下单、退款、手动调整等',
  `order_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联订单ID，可空',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '变动描述',
  `balance_after` BIGINT NOT NULL COMMENT '变动后余额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-成功，2-失败，3-补偿中',
  `related_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联ID（如评论ID、文章ID等）',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_change_type` (`change_type`),
  INDEX `idx_create_time` (`create_time`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分流水表';

-- 2. 会员表 (member)
-- 存储用户当前积分余额和等级
CREATE TABLE `member` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `current_points` BIGINT NOT NULL DEFAULT 0 COMMENT '当前积分余额',
  `total_earned_points` BIGINT NOT NULL DEFAULT 0 COMMENT '历史总获得积分',
  `level` INT NOT NULL DEFAULT 1 COMMENT '会员等级',
  `level_name` VARCHAR(50) NOT NULL DEFAULT '新手' COMMENT '等级名称',
  `current_exp` BIGINT NOT NULL DEFAULT 0 COMMENT '当前经验值',
  `next_level_exp` BIGINT NOT NULL DEFAULT 100 COMMENT '下一级所需经验值',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-正常，2-冻结',
  `level_updated_at` TIMESTAMP NULL DEFAULT NULL COMMENT '等级更新时间',
  `last_earned_at` TIMESTAMP NULL DEFAULT NULL COMMENT '最后获得积分时间',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  INDEX `idx_current_points` (`current_points` DESC),
  INDEX `idx_level` (`level` DESC),
  INDEX `idx_total_earned` (`total_earned_points` DESC),
  INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- 3. 积分变动类型配置表 (point_change_type_config)
-- 存储积分变动类型的配置信息
CREATE TABLE `point_change_type_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `change_type` VARCHAR(50) NOT NULL COMMENT '变动类型编码',
  `change_name` VARCHAR(100) NOT NULL COMMENT '变动类型名称',
  `point_value` INT NOT NULL COMMENT '默认积分值',
  `daily_limit` INT NOT NULL DEFAULT -1 COMMENT '每日限制次数，-1表示无限制',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '描述',
  `is_active` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用：1-启用，0-禁用',
  `create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_change_type` (`change_type`),
  INDEX `idx_is_active` (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分变动类型配置表';