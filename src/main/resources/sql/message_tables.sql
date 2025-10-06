-- 消息相关表结构
-- 创建时间: 2025-09-24
-- 适用于MySQL 8.0及以上版本

-- 1. 消息表 (message)
CREATE TABLE `message` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `type` TINYINT NOT NULL COMMENT '消息类型：1-系统消息/公告 2-私信消息 3-点赞消息 4-收藏消息 5-评论消息 6-关注消息',
  `sender_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发送者ID（系统消息时为null）',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '消息标题（系统消息/公告必填）',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `target_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '目标ID（如：文章ID、评论ID、用户ID等）',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_type` (`type`),
  KEY `idx_is_read` (`is_read`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';