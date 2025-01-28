-- 文章表
CREATE TABLE `article` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `author_id` bigint(20) NOT NULL,
    `category_id` bigint(20) NOT NULL,
    `title` varchar(100) NOT NULL,
    `content` longtext NOT NULL,
    `summary` varchar(500) DEFAULT NULL,
    `cover_image` varchar(255) DEFAULT NULL,
    `status` varchar(20) NOT NULL DEFAULT 'DRAFT',
    `view_count` bigint(20) NOT NULL DEFAULT '0',
    `like_count` bigint(20) NOT NULL DEFAULT '0',
    `comment_count` bigint(20) NOT NULL DEFAULT '0',
    `collect_count` bigint(20) NOT NULL DEFAULT '0',
    `is_original` tinyint(1) NOT NULL DEFAULT '1',
    `source_url` varchar(255) DEFAULT NULL,
    `is_top` tinyint(1) NOT NULL DEFAULT '0',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 文章分类表
CREATE TABLE `category` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL,
    `description` varchar(200) DEFAULT NULL,
    `sort` int(11) NOT NULL DEFAULT '0',
    `parent_id` bigint(20) DEFAULT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分类表';

-- 标签表
CREATE TABLE `tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL,
    `article_count` bigint(20) NOT NULL DEFAULT '0',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- 文章标签关联表
CREATE TABLE `article_tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` bigint(20) NOT NULL,
    `tag_id` bigint(20) NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`,`tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签关联表';

-- 用户收藏夹表
CREATE TABLE `collection` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL,
    `name` varchar(50) NOT NULL,
    `description` varchar(200) DEFAULT NULL,
    `is_public` tinyint(1) NOT NULL DEFAULT '1',
    `article_count` int(11) NOT NULL DEFAULT '0',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏夹表';

-- 收藏记录表
CREATE TABLE `article_collection` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `collection_id` bigint(20) NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_collection` (`article_id`,`collection_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_collection_id` (`collection_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏记录表';

-- 点赞记录表
CREATE TABLE `article_like` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`,`user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞记录表';

-- 评论表
CREATE TABLE `comment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `article_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `content` text NOT NULL,
    `parent_id` bigint(20) DEFAULT NULL,
    `reply_user_id` bigint(20) DEFAULT NULL,
    `like_count` bigint(20) NOT NULL DEFAULT '0',
    `status` tinyint(4) NOT NULL DEFAULT '1',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 评论点赞表
CREATE TABLE `comment_like` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `comment_id` bigint(20) NOT NULL,
    `user_id` bigint(20) NOT NULL,
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论点赞表';

-- 用户关注表
CREATE TABLE `user_follow` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `follower_id` bigint(20) NOT NULL COMMENT '关注者ID',
    `following_id` bigint(20) NOT NULL COMMENT '被关注者ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_follower_following` (`follower_id`,`following_id`),
    KEY `idx_following_id` (`following_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户关注表';

-- 消息通知表
CREATE TABLE `notification` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL COMMENT '接收者ID',
    `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
    `type` varchar(50) NOT NULL COMMENT '通知类型：COMMENT/LIKE/FOLLOW/SYSTEM',
    `target_id` bigint(20) DEFAULT NULL COMMENT '目标ID（文章/评论ID等）',
    `content` varchar(500) NOT NULL COMMENT '通知内容',
    `is_read` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否已读',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

-- 用户积分记录表
CREATE TABLE `user_points` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL,
    `points` int(11) NOT NULL COMMENT '积分变动值',
    `type` varchar(50) NOT NULL COMMENT '积分类型：POST/COMMENT/LIKE/LOGIN',
    `description` varchar(200) NOT NULL COMMENT '积分说明',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户积分记录表'; 