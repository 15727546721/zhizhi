-- 用户画像相关表结构
-- 创建时间: 2025-11-06
-- 适用于MySQL 8.0及以上版本
-- 用于存储用户行为画像数据，支持推荐系统

-- 1. 用户画像表 (user_profile)
-- 存储用户的基本画像数据，包括偏好标签、话题、活跃度等
CREATE TABLE `user_profile` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `preferred_tags` JSON DEFAULT NULL COMMENT '偏好标签ID列表，格式：[1,2,3]',
  `preferred_topics` JSON DEFAULT NULL COMMENT '偏好话题ID列表，格式：[1,2,3]',
  `preferred_post_types` JSON DEFAULT NULL COMMENT '偏好的帖子类型，格式：["article","post","discussion"]',
  `interaction_count` INT NOT NULL DEFAULT 0 COMMENT '总交互次数（点赞+收藏+评论）',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论次数',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `active_days` INT NOT NULL DEFAULT 0 COMMENT '活跃天数',
  `last_active_time` DATETIME DEFAULT NULL COMMENT '最后活跃时间',
  `profile_version` INT NOT NULL DEFAULT 1 COMMENT '画像版本号，用于增量更新',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`),
  KEY `idx_interaction_count` (`interaction_count` DESC),
  KEY `idx_last_active_time` (`last_active_time` DESC),
  KEY `idx_profile_version` (`profile_version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户画像表';

-- 2. 用户标签偏好表 (user_tag_preference)
-- 存储用户对每个标签的偏好程度（基于交互次数计算）
CREATE TABLE `user_tag_preference` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  `preference_score` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '偏好分数（0-100）',
  `interaction_count` INT NOT NULL DEFAULT 0 COMMENT '与该标签相关的交互次数',
  `last_interaction_time` DATETIME DEFAULT NULL COMMENT '最后交互时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tag_id` (`tag_id`),
  KEY `idx_preference_score` (`preference_score` DESC),
  KEY `idx_interaction_count` (`interaction_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户标签偏好表';

-- 3. 用户话题偏好表 (user_topic_preference)
-- 存储用户对每个话题的偏好程度
CREATE TABLE `user_topic_preference` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `topic_id` BIGINT UNSIGNED NOT NULL COMMENT '话题ID',
  `preference_score` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '偏好分数（0-100）',
  `interaction_count` INT NOT NULL DEFAULT 0 COMMENT '与该话题相关的交互次数',
  `last_interaction_time` DATETIME DEFAULT NULL COMMENT '最后交互时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_topic` (`user_id`, `topic_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_preference_score` (`preference_score` DESC),
  KEY `idx_interaction_count` (`interaction_count` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户话题偏好表';

-- 4. 用户行为统计表 (user_behavior_stats)
-- 存储用户的行为统计数据（按时间段统计）
CREATE TABLE `user_behavior_stats` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `stat_date` DATE NOT NULL COMMENT '统计日期',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '当日点赞次数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '当日收藏次数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '当日评论次数',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '当日浏览次数',
  `post_count` INT NOT NULL DEFAULT 0 COMMENT '当日发帖次数',
  `active_duration` INT NOT NULL DEFAULT 0 COMMENT '活跃时长（分钟）',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_date` (`user_id`, `stat_date`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_stat_date` (`stat_date` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户行为统计表';

-- 5. 用户相似度表 (user_similarity)
-- 存储用户之间的相似度（用于协同过滤推荐）
CREATE TABLE `user_similarity` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id_1` BIGINT UNSIGNED NOT NULL COMMENT '用户1 ID',
  `user_id_2` BIGINT UNSIGNED NOT NULL COMMENT '用户2 ID',
  `similarity_score` DECIMAL(10,4) NOT NULL DEFAULT 0.0000 COMMENT '相似度分数（0-1）',
  `common_interactions` INT NOT NULL DEFAULT 0 COMMENT '共同交互的帖子数量',
  `calculate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_pair` (`user_id_1`, `user_id_2`),
  KEY `idx_user_id_1` (`user_id_1`),
  KEY `idx_user_id_2` (`user_id_2`),
  KEY `idx_similarity_score` (`similarity_score` DESC),
  KEY `idx_calculate_time` (`calculate_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户相似度表';

