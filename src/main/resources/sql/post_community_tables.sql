-- 知之社区系统帖子相关核心表结构（完整版）
-- 创建时间: 2025-09-23
-- 适用于MySQL 8.0及以上版本
-- 包含所有表结构定义和历史变更

-- 1. 帖子表 (post)
CREATE TABLE `post` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `title` VARCHAR(200) NOT NULL COMMENT '帖子标题',
  `content` LONGTEXT NOT NULL COMMENT '帖子内容',
  `type` VARCHAR(20) NOT NULL DEFAULT 'post' COMMENT '帖子类型',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '帖子状态：0-草稿，1-已发布，2-已删除，3-已归档',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `like_count` INT NOT NULL DEFAULT 0 COMMENT '点赞次数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `comment_count` INT NOT NULL DEFAULT 0 COMMENT '评论次数',
  `share_count` INT NOT NULL DEFAULT 0 COMMENT '分享次数',
  `hot_score` INT NOT NULL DEFAULT 0 COMMENT '热度分数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_hot_score` (`hot_score`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子表';

-- 2. 帖子标签关联表 (post_tag)
CREATE TABLE `post_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT NOT NULL COMMENT '帖子ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_tag_id` (`tag_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子标签关联表';

-- 3. 收藏表 (favorite)
CREATE TABLE `favorite` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `target_id` BIGINT NOT NULL COMMENT '被收藏内容ID',
  `folder_id` BIGINT DEFAULT NULL COMMENT '所属收藏夹ID，如果为空，表示是普通收藏',
  `target_type` VARCHAR(20) NOT NULL DEFAULT 'post' COMMENT '收藏内容类型',
  `status` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '收藏状态：1-收藏，0-未收藏',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target_folder` (`user_id`, `target_id`, `target_type`, `folder_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_folder_id` (`folder_id`),
  KEY `idx_target_type` (`target_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户内容收藏表';

-- 4. 收藏夹表 (favorite_folder)
CREATE TABLE `favorite_folder` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏夹ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `name` VARCHAR(100) NOT NULL COMMENT '收藏夹名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '收藏夹描述',
  `is_default` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为默认收藏夹（0-否，1-是）',
  `content_count` INT NOT NULL DEFAULT 0 COMMENT '收藏内容数量',
  `is_public` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否公开（0-私密，1-公开）',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='收藏夹表';

-- 5. 标签表 (tags)
CREATE TABLE `tags` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '标签名称',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='标签表';

-- 6. 话题表 (topic)
CREATE TABLE `topic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `name` VARCHAR(50) NOT NULL UNIQUE COMMENT '话题名称',
  `description` TEXT DEFAULT NULL COMMENT '话题描述',
  `is_recommended` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为推荐话题',
  `usage_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用次数',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='话题表';

-- 7. 帖子话题关联表 (post_topic)
CREATE TABLE `post_topic` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `topic_id` BIGINT NOT NULL COMMENT '话题ID',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_topic` (`post_id`, `topic_id`),
  KEY `idx_topic_id` (`topic_id`),
  KEY `idx_post_id` (`post_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='帖子话题关联表';