-- 通知相关表结构
-- 创建时间: 2025-09-24
-- 适用于MySQL 8.0及以上版本

-- 1. 通知表 (notification)
CREATE TABLE `notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `type` TINYINT NOT NULL COMMENT '通知类型：0-系统通知 1-点赞通知 2-收藏通知 3-评论通知 4-回复通知 5-关注通知',
  `sender_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '内容',
  `business_type` TINYINT NOT NULL COMMENT '业务类型：0-系统 1-文章 2-话题 3-用户',
  `business_id` BIGINT UNSIGNED NOT NULL COMMENT '业务ID',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-有效 0-已删除',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_type` (`type`),
  KEY `idx_business` (`business_type`, `business_id`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知表';