-- 评论相关表结构
-- 创建时间: 2025-09-24
-- 适用于MySQL 8.0及以上版本

-- 1. 评论表 (comment)
CREATE TABLE `comment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `target_type` TINYINT NOT NULL COMMENT '评论类型，如1-文章；2-话题',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '评论来源的标识符',
  `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论的唯一标识符，顶级评论为NULL',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发表评论的用户ID',
  `reply_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '回复的用户ID，若为回复评论则存在',
  `content` TEXT NOT NULL COMMENT '评论的具体内容',
  `image_url` VARCHAR(1000) DEFAULT NULL COMMENT '图片地址url，分隔符号","',
  `like_count` BIGINT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` BIGINT NOT NULL DEFAULT 0 COMMENT '子评论数',
  `hot_score` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '热度分数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_reply_user_id` (`reply_user_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_hot_score` (`hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';