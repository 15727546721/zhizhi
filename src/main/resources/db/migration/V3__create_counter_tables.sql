-- 文章计数缓存表（用于批量更新文章相关计数）
CREATE TABLE `article_counter_cache` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` bigint(20) NOT NULL COMMENT '文章ID',
    `view_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的浏览数',
    `like_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的点赞数',
    `collect_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的收藏数',
    `comment_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的评论数',
    `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号，用于乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_id` (`article_id`),
    KEY `idx_last_update_time` (`last_update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章计数缓存表';

-- 用户计数缓存表（用于批量更新用户相关计数）
CREATE TABLE `user_counter_cache` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `article_view_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的文章被浏览数',
    `article_like_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的文章被点赞数',
    `like_article_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的点赞文章数',
    `following_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的关注数',
    `follower_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '待更新的粉丝数',
    `last_update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `version` bigint(20) NOT NULL DEFAULT '0' COMMENT '版本号，用于乐观锁',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`),
    KEY `idx_last_update_time` (`last_update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户计数缓存表';

-- 热门文章缓存表（用于存储热门文章的实时计数）
CREATE TABLE `article_hot_cache` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` bigint(20) NOT NULL COMMENT '文章ID',
    `score` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '热度分数',
    `view_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '实时浏览数',
    `like_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '实时点赞数',
    `collect_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '实时收藏数',
    `comment_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '实时评论数',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_id` (`article_id`),
    KEY `idx_score` (`score`),
    KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='热门文章缓存表'; 