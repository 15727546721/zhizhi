-- 随笔相关表结构
-- 创建时间: 2025-09-24
-- 适用于MySQL 8.0及以上版本

-- 1. 随笔表 (essay)
CREATE TABLE `essay` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '随笔ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发布随笔的用户ID',
  `content` TEXT NOT NULL COMMENT '话题内容',
  `images` VARCHAR(1000) DEFAULT NULL COMMENT '图片URL数组，使用符号","分隔',
  `topics` VARCHAR(500) DEFAULT NULL COMMENT '绑定的话题，使用符号","分隔',
  `like_count` BIGINT NOT NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` BIGINT NOT NULL DEFAULT 0 COMMENT '评论数',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='随笔表';