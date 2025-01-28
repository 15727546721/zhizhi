-- 用户表
CREATE TABLE `user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL COMMENT '用户名',
    `password` varchar(100) NOT NULL COMMENT '密码',
    `nickname` varchar(50) NOT NULL COMMENT '昵称',
    `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
    `bio` varchar(255) DEFAULT NULL COMMENT '个人简介',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1-正常，2-禁言，3-封禁',
    
    -- 文章相关统计
    `article_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '发布文章数',
    `article_view_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '文章被浏览数',
    `article_like_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '文章被点赞数',
    `article_collect_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '文章被收藏数',
    
    -- 互动相关统计
    `like_article_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '点赞文章数',
    `collect_article_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '收藏文章数',
    `comment_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '评论数',
    
    -- 关注相关统计
    `following_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '关注数',
    `follower_count` bigint(20) NOT NULL DEFAULT '0' COMMENT '粉丝数',
    
    -- 成就相关统计
    `total_points` bigint(20) NOT NULL DEFAULT '0' COMMENT '总积分',
    `level` int(11) NOT NULL DEFAULT '1' COMMENT '用户等级',
    `badge_count` int(11) NOT NULL DEFAULT '0' COMMENT '获得徽章数',
    
    -- 其他统计
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_active_time` datetime DEFAULT NULL COMMENT '最后活跃时间',
    `register_ip` varchar(50) DEFAULT NULL COMMENT '注册IP',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    UNIQUE KEY `uk_phone` (`phone`),
    KEY `idx_nickname` (`nickname`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 用户扩展信息表（用于存储不常用的用户信息）
CREATE TABLE `user_profile` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
    `gender` tinyint(4) DEFAULT NULL COMMENT '性别：1-男，2-女，0-未知',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `location` varchar(100) DEFAULT NULL COMMENT '所在地',
    `company` varchar(100) DEFAULT NULL COMMENT '公司',
    `position` varchar(100) DEFAULT NULL COMMENT '职位',
    `website` varchar(255) DEFAULT NULL COMMENT '个人网站',
    `github` varchar(255) DEFAULT NULL COMMENT 'GitHub主页',
    `weibo` varchar(255) DEFAULT NULL COMMENT '微博主页',
    `wechat` varchar(50) DEFAULT NULL COMMENT '微信号',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户扩展信息表';

-- 用户徽章表
CREATE TABLE `user_badge` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `badge_id` bigint(20) NOT NULL COMMENT '徽章ID',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获得时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_badge` (`user_id`,`badge_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户徽章表';

-- 徽章定义表
CREATE TABLE `badge` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `name` varchar(50) NOT NULL COMMENT '徽章名称',
    `description` varchar(200) NOT NULL COMMENT '徽章描述',
    `image` varchar(255) NOT NULL COMMENT '徽章图片',
    `type` varchar(50) NOT NULL COMMENT '徽章类型：ACHIEVEMENT/EVENT/IDENTITY',
    `condition` varchar(500) DEFAULT NULL COMMENT '获取条件',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='徽章定义表';

-- 用户每日签到表
CREATE TABLE `user_checkin` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `checkin_date` date NOT NULL COMMENT '签到日期',
    `checkin_time` datetime NOT NULL COMMENT '签到时间',
    `continuous_days` int(11) NOT NULL DEFAULT '1' COMMENT '连续签到天数',
    `points` int(11) NOT NULL COMMENT '获得积分',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_date` (`user_id`,`checkin_date`),
    KEY `idx_checkin_date` (`checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户每日签到表'; 