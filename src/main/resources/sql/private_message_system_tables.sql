-- ============================================================================
-- 私信系统完整表结构
-- 创建时间: 2025-01-XX
-- 适用于MySQL 8.0及以上版本
-- 说明：本文件包含私信系统所需的所有表结构和初始数据
-- 注意：新系统可直接执行，会删除旧表重新创建
-- ============================================================================

-- ============================================================================
-- 第一部分：核心业务表
-- ============================================================================

-- 1. 对话关系表（核心表）
-- 说明：记录两个用户之间的对话关系，双向共享一条记录
-- 只要此表存在记录，说明双方已建立信任关系，可以自由发送消息
DROP TABLE IF EXISTS `conversation`;
CREATE TABLE `conversation` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '对话关系ID',
  `user_id_1` BIGINT UNSIGNED NOT NULL COMMENT '用户1 ID（较小的ID）',
  `user_id_2` BIGINT UNSIGNED NOT NULL COMMENT '用户2 ID（较大的ID）',
  `created_by` BIGINT UNSIGNED NOT NULL COMMENT '创建者ID（首次发送消息的用户）',
  `last_message_time` DATETIME NOT NULL COMMENT '最后一条消息时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pair` (`user_id_1`, `user_id_2`),
  KEY `idx_user_id_1` (`user_id_1`),
  KEY `idx_user_id_2` (`user_id_2`),
  KEY `idx_last_message_time` (`last_message_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话关系表';

-- 2. 首次消息记录表（防骚扰机制核心表）
-- 说明：记录非互相关注用户的首次发送消息，用于防骚扰
-- 单向记录，A→B和B→A是两条不同的记录
DROP TABLE IF EXISTS `first_message`;
CREATE TABLE `first_message` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `message_id` BIGINT UNSIGNED NOT NULL COMMENT '首条消息ID',
  `has_replied` TINYINT NOT NULL DEFAULT 0 COMMENT '接收方是否已回复：0-未回复 1-已回复',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sender_receiver` (`sender_id`, `receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_has_replied` (`has_replied`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首次消息记录表（防骚扰机制）';

-- ============================================================================
-- 第二部分：用户功能表
-- ============================================================================

-- 3. 用户屏蔽表
-- 说明：记录用户之间的屏蔽关系
DROP TABLE IF EXISTS `user_block`;
CREATE TABLE `user_block` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '屏蔽关系ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID（屏蔽发起者）',
  `blocked_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被屏蔽用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_blocked_user_id` (`blocked_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户屏蔽表';

-- 4. 用户私信设置表
-- 说明：用户级别的隐私设置，控制谁可以给自己发私信
DROP TABLE IF EXISTS `user_message_settings`;
CREATE TABLE `user_message_settings` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `allow_stranger_message` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许陌生人私信：0-不允许 1-允许',
  `allow_non_mutual_follow_message` TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许非互相关注用户私信：0-不允许 1-允许',
  `message_notification_enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否开启私信通知：0-关闭 1-开启',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户私信设置表';

-- ============================================================================
-- 第三部分：系统配置表
-- ============================================================================

-- 5. 系统配置表
-- 说明：系统级别的全局配置，由管理员管理
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` VARCHAR(100) NOT NULL COMMENT '配置键',
  `config_value` VARCHAR(500) NOT NULL COMMENT '配置值',
  `config_desc` VARCHAR(200) DEFAULT NULL COMMENT '配置描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================================
-- 第四部分：初始化数据
-- ============================================================================

-- 插入私信系统默认配置
INSERT INTO `system_config` (`config_key`, `config_value`, `config_desc`) VALUES
('private_message.enabled', '1', '私信功能开关：0-关闭 1-开启'),
('private_message.allow_stranger', '1', '是否允许陌生人私信（系统默认）：0-不允许 1-允许'),
('private_message.max_message_length', '1000', '私信最大长度（字符数）'),
('private_message.rate_limit', '10', '私信发送频率限制（条/分钟）');

-- ============================================================================
-- 表结构说明
-- ============================================================================
-- 
-- 【核心设计思想】
-- 1. conversation表：双向对话关系，存在即表示双方可以自由聊天
-- 2. first_message表：单向首次消息记录，用于防骚扰机制
-- 3. message表需要有status字段（在message_tables.sql中定义）：区分消息可见性
--    - status=1: 已投递，接收方可见
--    - status=2: 待投递，仅发送方可见（防骚扰机制）
--    - status=3: 已屏蔽，仅发送方可见（被屏蔽）
--
-- 【防骚扰机制流程】
-- 1. A首次给B发消息：
--    - 创建first_message(A→B)记录
--    - 创建conversation(A-B)对话记录
--    - 消息status=1，B可见，B的对话列表能看到A
-- 2. A继续给B发消息（B未回复前）：
--    - 检查first_message(A→B).has_replied=false
--    - 消息status=2，B不可见（防骚扰）
-- 3. B回复A：
--    - 标记first_message(A→B).has_replied=true
--    - 更新conversation最后消息时间
--    - 消息status=1，A可见
-- 4. 之后A和B正常聊天：
--    - 所有消息status=1，双方可见
--
-- 【防骚扰说明】
-- 允许陌生人发第一条消息（让对方看到并能在对话列表找到），但防止连续骚扰（后续消息需要等待回复）
-- 
-- 【包含的表】
-- ├── conversation          对话关系表（核心）
-- ├── first_message         首次消息记录表（防骚扰）
-- ├── user_block            用户屏蔽表
-- ├── user_message_settings 用户私信设置表
-- └── system_config         系统配置表
-- 
-- 【前置条件】
-- 需要先执行 message_tables.sql，确保 message 表存在且包含 status 字段
-- 
-- ============================================================================
