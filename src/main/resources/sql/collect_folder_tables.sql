-- 创建收藏夹表
CREATE TABLE `collect_folder` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `name` varchar(100) NOT NULL COMMENT '收藏夹名称',
  `description` varchar(255) DEFAULT NULL COMMENT '收藏夹描述',
  `is_default` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否为默认收藏夹（0-否，1-是）',
  `article_count` int NOT NULL DEFAULT '0' COMMENT '收藏文章数量',
  `is_public` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否公开（0-私密，1-公开）',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹表';

-- 创建收藏夹文章关联表
CREATE TABLE `collect_folder_article` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `folder_id` bigint unsigned NOT NULL COMMENT '收藏夹ID',
  `article_id` bigint unsigned NOT NULL COMMENT '文章ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_folder_article` (`folder_id`, `article_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_article_id` (`article_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏夹文章关联表';