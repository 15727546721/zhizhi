-- 点赞相关表结构
-- 创建时间: 2025-09-24
-- 适用于MySQL 8.0及以上版本

-- 1. 点赞表 (like)
CREATE TABLE `like` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `type` TINYINT NOT NULL COMMENT '点赞类型：1-文章，2-话题，3-评论等',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '是否点赞，1-点赞，0-取消点赞',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';