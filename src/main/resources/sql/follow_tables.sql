-- 关注相关表结构
-- 创建时间: 2025-09-24
-- 适用于MySQL 8.0及以上版本

-- 1. 关注表 (follow)
CREATE TABLE `follow` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关注关系ID',
  `follower_id` BIGINT UNSIGNED NOT NULL COMMENT '关注者ID',
  `followed_id` BIGINT UNSIGNED NOT NULL COMMENT '被关注者ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '关注状态（0-取消关注，1-已关注）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_followed` (`follower_id`, `followed_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_followed_id` (`followed_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';