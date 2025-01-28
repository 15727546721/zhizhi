-- 文章主表
CREATE TABLE `t_article` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `author_id` bigint(20) NOT NULL COMMENT '作者ID',
    `category_id` bigint(20) NOT NULL COMMENT '分类ID',
    `title` varchar(100) NOT NULL COMMENT '文章标题',
    `content` longtext NOT NULL COMMENT '文章内容',
    `summary` varchar(500) DEFAULT NULL COMMENT '文章摘要',
    `cover_image` varchar(255) DEFAULT NULL COMMENT '封面图片URL',
    `status` varchar(20) NOT NULL DEFAULT 'DRAFT' COMMENT '文章状态：DRAFT-草稿，PUBLISHED-已发布，ARCHIVED-已归档',
    `view_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '浏览量',
    `like_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '点赞数',
    `comment_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '评论数',
    `collect_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '收藏数',
    `is_original` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否原创：1-原创，0-转载',
    `source_url` varchar(255) DEFAULT NULL COMMENT '转载来源URL',
    `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶：1-置顶，0-不置顶',
    `is_recommend` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否推荐：1-推荐，0-不推荐',
    `read_time` int(11) NOT NULL DEFAULT '0' COMMENT '预计阅读时间(分钟)',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_author_id` (`author_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 文章分类表
CREATE TABLE `t_article_category` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` varchar(50) NOT NULL COMMENT '分类名称',
    `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
    `icon` varchar(255) DEFAULT NULL COMMENT '分类图标',
    `sort` int(11) NOT NULL DEFAULT '0' COMMENT '排序号',
    `parent_id` bigint(20) DEFAULT NULL COMMENT '父分类ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类表';

-- 文章标签表
CREATE TABLE `t_article_tag` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `name` varchar(50) NOT NULL COMMENT '标签名称',
    `description` varchar(200) DEFAULT NULL COMMENT '标签描述',
    `icon` varchar(255) DEFAULT NULL COMMENT '标签图标',
    `follow_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '关注数',
    `article_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '文章数',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签表';

-- 文章-标签关联表
CREATE TABLE `t_article_tag_relation` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '关联ID',
    `article_id` bigint(20) NOT NULL COMMENT '文章ID',
    `tag_id` bigint(20) NOT NULL COMMENT '标签ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_tag` (`article_id`,`tag_id`),
    KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章-标签关联表';

-- 文章点赞记录表
CREATE TABLE `t_article_like` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `article_id` bigint(20) NOT NULL COMMENT '文章ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`,`user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章点赞记录表';

-- 文章收藏记录表
CREATE TABLE `t_article_collect` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `article_id` bigint(20) NOT NULL COMMENT '文章ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_user` (`article_id`,`user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章收藏记录表';

-- 文章阅读记录表
CREATE TABLE `t_article_view` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '阅读ID',
    `article_id` bigint(20) NOT NULL COMMENT '文章ID',
    `user_id` bigint(20) DEFAULT NULL COMMENT '用户ID（未登录为空）',
    `ip` varchar(50) NOT NULL COMMENT '访问IP',
    `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章阅读记录表';

-- 文章评论表
CREATE TABLE `t_article_comment` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `article_id` bigint(20) NOT NULL COMMENT '文章ID',
    `user_id` bigint(20) NOT NULL COMMENT '评论用户ID',
    `content` text NOT NULL COMMENT '评论内容',
    `parent_id` bigint(20) DEFAULT NULL COMMENT '父评论ID',
    `reply_user_id` bigint(20) DEFAULT NULL COMMENT '回复用户ID',
    `like_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '点赞数',
    `is_top` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1-正常，2-待审核，3-已删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_article_id` (`article_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';

-- 文章评论点赞表
CREATE TABLE `t_article_comment_like` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
    `comment_id` bigint(20) NOT NULL COMMENT '评论ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_comment_user` (`comment_id`,`user_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论点赞表'; 