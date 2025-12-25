-- ============================================================================
-- 知知社区 - 数据库表结构脚本
-- ============================================================================
-- 文件：01_schema.sql
-- 版本：V1.0
-- 创建时间：2025-11-30
-- 数据库：MySQL 8.0+
-- 字符集：utf8mb4_unicode_ci
-- 
-- ⚠️ 警告：此脚本会删除并重建所有表，请先备份数据！
-- 
-- 表结构概览（共24个表）：
-- ┌─────────────────────────────────────────────────────────────┐
-- │ 用户模块（4个）                                              │
-- │   user, user_settings, user_interested_tag, user_block     │
-- ├─────────────────────────────────────────────────────────────┤
-- │ 内容模块（4个）                                              │
-- │   post, tag, post_tag, comment                             │
-- ├─────────────────────────────────────────────────────────────┤
-- │ 互动模块（3个）                                              │
-- │   `like`, favorite, follow                                 │
-- ├─────────────────────────────────────────────────────────────┤
-- │ 消息模块（5个）                                              │
-- │   notification, user_conversation, private_message,        │
-- │   greeting_record, feedback                                │
-- ├─────────────────────────────────────────────────────────────┤
-- │ 权限模块（4个）                                              │
-- │   role, menu, user_role, role_menu                         │
-- ├─────────────────────────────────────────────────────────────┤
-- │ 文件模块（1个）                                              │
-- │   file_record                                              │
-- ├─────────────────────────────────────────────────────────────┤
-- │ 举报模块（1个）                                              │
-- │   report                                                   │
-- └─────────────────────────────────────────────────────────────┘
-- ============================================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `zhizhi` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `zhizhi`;
SET NAMES utf8mb4;

-- ============================================================================
-- 删除所有表（按外键依赖顺序）
-- ============================================================================
DROP TABLE IF EXISTS `role_menu`;
DROP TABLE IF EXISTS `user_role`;
DROP TABLE IF EXISTS `menu`;
DROP TABLE IF EXISTS `role`;
DROP TABLE IF EXISTS `greeting_record`;
DROP TABLE IF EXISTS `user_conversation`;
DROP TABLE IF EXISTS `private_message`;
DROP TABLE IF EXISTS `notification`;
DROP TABLE IF EXISTS `feedback`;
DROP TABLE IF EXISTS `follow`;
DROP TABLE IF EXISTS `favorite`;
DROP TABLE IF EXISTS `like`;
DROP TABLE IF EXISTS `comment`;
DROP TABLE IF EXISTS `post_tag`;
DROP TABLE IF EXISTS `tag`;
DROP TABLE IF EXISTS `post`;
DROP TABLE IF EXISTS `user_block`;
DROP TABLE IF EXISTS `user_interested_tag`;
DROP TABLE IF EXISTS `user_settings`;
DROP TABLE IF EXISTS `user`;

-- ============================================================================
-- 第一部分：用户模块（4个表）
-- ============================================================================

-- 1.1 用户表
CREATE TABLE `user` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  
  -- 核心认证字段
  `username` VARCHAR(50) NOT NULL COMMENT '账号名(唯一、不可改、用于登录和@提及)',
  `password` VARCHAR(255) NOT NULL COMMENT '密码(SHA256加密)',
  `email` VARCHAR(100) NOT NULL COMMENT '邮箱(唯一、用于登录/找回密码)',
  
  -- 用户展示信息
  `nickname` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '显示名(可修改)',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '个人简介',
  
  -- 用户基本信息
  `gender` TINYINT DEFAULT NULL COMMENT '性别: 0-女 1-男 2-保密',
  `birthday` DATE DEFAULT NULL COMMENT '生日',
  `region` VARCHAR(100) DEFAULT NULL COMMENT '地区',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号(可选)',
  
  -- 用户状态和类型
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '账号状态: 0-禁用 1-正常 2-待审核',
  `user_type` TINYINT NOT NULL DEFAULT 1 COMMENT '用户类型: 1-普通 2-官方 3-管理员',
  
  -- 统计字段（冗余设计，提升查询性能）
  `follow_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关注数',
  `fans_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '粉丝数',
  `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '获赞数',
  `post_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '发帖数',
  `comment_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  
  -- 登录追踪
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  
  -- 时间戳
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_email` (`email`),
  KEY `idx_nickname` (`nickname`),
  KEY `idx_status` (`status`),
  KEY `idx_user_type` (`user_type`),
  KEY `idx_fans_count` (`fans_count` DESC),
  KEY `idx_like_count` (`like_count` DESC),
  KEY `idx_create_time` (`create_time` DESC),
  KEY `idx_status_type` (`status`, `user_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 1.2 用户设置表（合并了通用设置和私信设置）
CREATE TABLE `user_settings` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '设置ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `profile_visibility` TINYINT NOT NULL DEFAULT 1 COMMENT '资料可见性: 1-公开 2-仅关注者 3-私密',
  `show_online_status` TINYINT NOT NULL DEFAULT 1 COMMENT '显示在线状态: 0-否 1-是',
  `email_notification` TINYINT NOT NULL DEFAULT 1 COMMENT '邮件通知: 0-关 1-开',
  `browser_notification` TINYINT NOT NULL DEFAULT 1 COMMENT '浏览器通知: 0-关 1-开',
  `sound_notification` TINYINT NOT NULL DEFAULT 0 COMMENT '提示音: 0-关 1-开',
  `allow_stranger_message` TINYINT NOT NULL DEFAULT 1 COMMENT '允许陌生人私信: 0-否 1-是',
  `email_verified` TINYINT NOT NULL DEFAULT 0 COMMENT '邮箱已验证: 0-否 1-是',
  `email_verify_token` VARCHAR(255) DEFAULT NULL COMMENT '邮箱验证令牌',
  `email_verify_expire_time` DATETIME DEFAULT NULL COMMENT '邮箱验证过期时间',
  `password_reset_token` VARCHAR(255) DEFAULT NULL COMMENT '密码重置令牌',
  `password_reset_expire_time` DATETIME DEFAULT NULL COMMENT '密码重置过期时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户设置表';

-- 1.3 用户感兴趣标签表
CREATE TABLE `user_interested_tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_tag` (`user_id`, `tag_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户感兴趣标签表';

-- 1.4 用户屏蔽表
CREATE TABLE `user_block` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '屏蔽ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID(屏蔽发起者)',
  `blocked_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被屏蔽用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_blocked` (`user_id`, `blocked_user_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_blocked_user_id` (`blocked_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户屏蔽表';

-- ============================================================================
-- 第二部分：内容模块（4个表）
-- ============================================================================

-- 2.1 帖子表
CREATE TABLE `post` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '帖子ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '作者ID',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '摘要',
  `content` LONGTEXT NOT NULL COMMENT '内容(Markdown)',
  `cover_url` VARCHAR(500) DEFAULT NULL COMMENT '封面图URL',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-草稿 1-已发布 2-已删除 3-已归档',
  `is_top` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶: 0-否 1-是',
  `is_featured` TINYINT NOT NULL DEFAULT 0 COMMENT '是否精选: 0-否 1-是',
  
  -- 统计字段
  `view_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '浏览数',
  `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `favorite_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '收藏数',
  `comment_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '评论数',
  `share_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '分享数',
  `hot_score` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '热度分数',
  
  -- 时间戳
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_is_top` (`is_top`),
  KEY `idx_is_featured` (`is_featured`),
  KEY `idx_hot_score` (`hot_score` DESC),
  KEY `idx_create_time` (`create_time` DESC),
  KEY `idx_user_status` (`user_id`, `status`),
  FULLTEXT KEY `ft_title_content` (`title`, `content`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子表';

-- 2.2 标签表
CREATE TABLE `tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '描述',
  `icon` VARCHAR(200) DEFAULT NULL COMMENT '图标URL',
  `color` VARCHAR(20) DEFAULT NULL COMMENT '颜色值',
  `usage_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '使用次数',
  `is_recommended` TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐: 0-否 1-是',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`),
  KEY `idx_usage_count` (`usage_count` DESC),
  KEY `idx_is_recommended` (`is_recommended`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- 2.3 帖子标签关联表
CREATE TABLE `post_tag` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `post_id` BIGINT UNSIGNED NOT NULL COMMENT '帖子ID',
  `tag_id` BIGINT UNSIGNED NOT NULL COMMENT '标签ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_post_tag` (`post_id`, `tag_id`),
  KEY `idx_post_id` (`post_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='帖子标签关联表';

-- 2.4 评论表
CREATE TABLE `comment` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-帖子 2-其他',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `parent_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '父评论ID(顶级为NULL)',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '评论者ID',
  `reply_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '被回复用户ID',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `image_url` VARCHAR(1000) DEFAULT NULL COMMENT '图片URL(逗号分隔)',
  
  -- 统计字段
  `like_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '点赞数',
  `reply_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '回复数',
  `hot_score` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '热度分数',
  
  -- 时间戳
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time` DESC),
  KEY `idx_hot_score` (`hot_score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- ============================================================================
-- 第三部分：互动模块（3个表）
-- ============================================================================

-- 3.1 点赞表
CREATE TABLE `like` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `type` TINYINT NOT NULL COMMENT '类型: 1-帖子 2-评论',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-取消 1-点赞',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target_type` (`user_id`, `target_id`, `type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_type` (`target_id`, `type`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='点赞表';

-- 3.2 收藏表
CREATE TABLE `favorite` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `target_type` VARCHAR(20) NOT NULL DEFAULT 'post' COMMENT '目标类型',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-取消 1-收藏',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_target` (`user_id`, `target_id`, `target_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_target_id` (`target_id`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- 3.3 关注表
CREATE TABLE `follow` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `follower_id` BIGINT UNSIGNED NOT NULL COMMENT '关注者ID',
  `followed_id` BIGINT UNSIGNED NOT NULL COMMENT '被关注者ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-取消 1-关注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follower_followed` (`follower_id`, `followed_id`),
  KEY `idx_follower_id` (`follower_id`),
  KEY `idx_followed_id` (`followed_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='关注表';

-- ============================================================================
-- 第四部分：消息通知模块（4个表）
-- ============================================================================

-- 4.1 互动通知表
-- 用途：记录用户的互动通知（点赞/评论/关注等）和系统通知
-- 注意：私信系统独立管理，不在此表中
CREATE TABLE `notification` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `type` TINYINT NOT NULL COMMENT '类型: 0-系统 1-点赞 2-收藏 3-评论 4-回复 5-关注 6-@提及',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `sender_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发送者ID(系统通知为NULL)',
  `sender_type` TINYINT NOT NULL DEFAULT 1 COMMENT '发送者类型: 0-系统 1-用户',
  `title` VARCHAR(200) DEFAULT NULL COMMENT '标题(系统通知用)',
  `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
  `business_type` TINYINT NOT NULL DEFAULT 0 COMMENT '业务类型: 0-系统 1-帖子 2-评论 3-用户',
  `business_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务ID',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0-删除 1-有效',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_receiver_type` (`receiver_id`, `type`),
  KEY `idx_receiver_read` (`receiver_id`, `is_read`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='互动通知表';

-- 4.2 用户会话表（用户视角设计）
-- 设计理念：每个用户独立视角的会话记录，直接按owner_id查询
CREATE TABLE `user_conversation` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
    `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '会话所有者ID',
    `other_user_id` BIGINT UNSIGNED NOT NULL COMMENT '对方用户ID',
    `other_nickname` VARCHAR(50) DEFAULT NULL COMMENT '对方昵称（冗余，Java层维护）',
    `other_avatar` VARCHAR(500) DEFAULT NULL COMMENT '对方头像（冗余，Java层维护）',
    `relation_type` TINYINT NOT NULL DEFAULT 0 COMMENT '关系: 0-陌生人 1-我关注 2-互关',
    `conversation_status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待回复 1-已建立',
    `is_initiator` TINYINT NOT NULL DEFAULT 0 COMMENT '是否发起者',
    `is_blocked` TINYINT NOT NULL DEFAULT 0 COMMENT '我屏蔽了对方',
    `is_blocked_by` TINYINT NOT NULL DEFAULT 0 COMMENT '被对方屏蔽',
    `unread_count` INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '未读数',
    `last_message` VARCHAR(200) DEFAULT NULL COMMENT '最后消息预览',
    `last_message_time` DATETIME DEFAULT NULL COMMENT '最后消息时间',
    `last_message_is_mine` TINYINT DEFAULT 0 COMMENT '最后消息是否我发',
    `is_pinned` TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶',
    `is_muted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否免打扰',
    `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_owner_other` (`owner_id`, `other_user_id`),
    KEY `idx_owner_time` (`owner_id`, `is_deleted`, `last_message_time` DESC),
    KEY `idx_other_user` (`other_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- 4.3 私信消息表
CREATE TABLE `private_message` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` TINYINT NOT NULL DEFAULT 1 COMMENT '消息类型: 1-文本 2-图片 3-链接 4-系统消息',
  `media_url` VARCHAR(500) DEFAULT NULL COMMENT '媒体URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '消息状态: 1-已送达 2-待回复 3-被屏蔽 4-已撤回',
  `is_read` TINYINT NOT NULL DEFAULT 0 COMMENT '是否已读: 0-未读 1-已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '阅读时间',
  `sender_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '发送方已删除',
  `receiver_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '接收方已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`, `create_time` DESC),
  KEY `idx_receiver_sender` (`receiver_id`, `sender_id`, `create_time` DESC),
  KEY `idx_receiver_unread` (`receiver_id`, `is_read`, `status`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息表';

-- 4.4 打招呼消息记录表（防骚扰机制）
-- 用于记录单向关注场景下的"敲门消息"，A关注B后只能发1条打招呼消息
CREATE TABLE `greeting_record` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
    `user_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID（关注方）',
    `target_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID（被关注方）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_target` (`user_id`, `target_id`),
    KEY `idx_target_id` (`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='打招呼消息记录表';

-- 4.5 用户反馈表
CREATE TABLE `feedback` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `type` TINYINT NOT NULL DEFAULT 0 COMMENT '反馈类型: 0-Bug问题 1-功能建议 2-内容问题 3-其他',
  `title` VARCHAR(100) NOT NULL COMMENT '标题',
  `content` TEXT NOT NULL COMMENT '详细内容',
  `images` VARCHAR(500) DEFAULT NULL COMMENT '图片URL，多个用逗号分隔',
  `contact` VARCHAR(100) DEFAULT NULL COMMENT '联系方式',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-处理中 2-已解决 3-已关闭',
  `reply` TEXT DEFAULT NULL COMMENT '管理员回复',
  `reply_time` DATETIME DEFAULT NULL COMMENT '回复时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_type` (`type`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户反馈表';

-- ============================================================================
-- 第五部分：权限模块（4个表）
-- ============================================================================

-- 5.1 角色表
CREATE TABLE `role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code` VARCHAR(64) NOT NULL COMMENT '角色编码',
  `name` VARCHAR(128) NOT NULL COMMENT '角色名称',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 5.2 菜单权限表
CREATE TABLE `menu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级ID',
  `path` VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
  `component` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
  `title` VARCHAR(128) NOT NULL COMMENT '菜单名称',
  `name` VARCHAR(128) DEFAULT NULL COMMENT '路由名',
  `icon` VARCHAR(128) DEFAULT NULL COMMENT '图标',
  `type` VARCHAR(32) NOT NULL COMMENT '类型: CATALOG-目录 MENU-菜单 BUTTON-按钮',
  `perm` VARCHAR(128) DEFAULT NULL COMMENT '权限标识',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `hidden` TINYINT NOT NULL DEFAULT 0 COMMENT '是否隐藏: 0-显示 1-隐藏',
  `redirect` VARCHAR(255) DEFAULT NULL COMMENT '重定向',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

-- 5.3 用户角色关联表
CREATE TABLE `user_role` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 5.4 角色菜单关联表
CREATE TABLE `role_menu` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT UNSIGNED NOT NULL COMMENT '菜单ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 注：user_permission 表（直接授权）已移除，简化权限模型
-- 如需直接授权功能，可后续添加

-- ============================================================================
-- 第六部分：文件模块（1个表）
-- ============================================================================

-- 6.1 文件记录表
DROP TABLE IF EXISTS `file_record`;
CREATE TABLE `file_record` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `storage_name` VARCHAR(255) NOT NULL COMMENT '存储名称（如：uploads/202512/abc123.jpg）',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始名称（用户上传时的文件名）',
  `storage_path` VARCHAR(500) NOT NULL COMMENT '存储路径（如：zhizhi/uploads/202512/abc123.jpg）',
  `bucket_name` VARCHAR(100) NOT NULL DEFAULT 'zhizhi' COMMENT '存储桶名称',
  `size_bytes` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `mime_type` VARCHAR(100) DEFAULT NULL COMMENT 'MIME类型（如：image/jpeg）',
  `extension` VARCHAR(20) DEFAULT NULL COMMENT '扩展名（如：jpg）',
  `upload_user_id` BIGINT UNSIGNED NOT NULL COMMENT '上传用户ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-临时文件 1-正式文件 2-已删除',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_storage_name` (`storage_name`),
  KEY `idx_upload_user_id` (`upload_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- ============================================================================
-- 第七部分：举报模块（1个表）
-- ============================================================================

-- 7.1 举报表
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `reporter_id` BIGINT UNSIGNED NOT NULL COMMENT '举报人ID',
  
  -- 举报目标
  `target_type` TINYINT NOT NULL COMMENT '目标类型: 1-帖子 2-评论 3-用户',
  `target_id` BIGINT UNSIGNED NOT NULL COMMENT '目标ID',
  `target_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被举报用户ID',
  
  -- 举报内容
  `reason` TINYINT NOT NULL COMMENT '举报原因: 1-垃圾广告 2-违法违规 3-色情低俗 4-人身攻击 5-抄袭侵权 6-其他',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '详细说明',
  `evidence_urls` VARCHAR(2000) DEFAULT NULL COMMENT '截图证据URL(JSON数组)',
  
  -- 处理信息
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0-待处理 1-已通过 2-已驳回 3-已忽略',
  `handler_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '处理人ID',
  `handle_result` VARCHAR(500) DEFAULT NULL COMMENT '处理结果说明',
  `handle_action` TINYINT DEFAULT NULL COMMENT '处罚措施: 0-无 1-删除内容 2-警告 3-禁言7天 4-禁言30天 5-永久封号',
  `handle_time` DATETIME DEFAULT NULL COMMENT '处理时间',
  
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  
  PRIMARY KEY (`id`),
  KEY `idx_reporter_id` (`reporter_id`),
  KEY `idx_target` (`target_type`, `target_id`),
  KEY `idx_target_user_id` (`target_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time` DESC),
  KEY `idx_status_time` (`status`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- ============================================================================
-- 完成
-- ============================================================================
SELECT '
============================================
✅ 表结构创建完成！
============================================

📊 表结构统计（共23个表）：
   - 用户模块：4个表 (user, user_settings, user_interested_tag, user_block)
   - 内容模块：4个表 (post, tag, post_tag, comment)
   - 互动模块：3个表 (like, favorite, follow)
   - 消息模块：5个表 (notification, user_conversation, private_message, greeting_record, feedback)
   - 权限模块：4个表 (role, menu, user_role, role_menu)
   - 文件模块：1个表 (file_record)
   - 举报模块：1个表 (report)
   
⚠️ 请执行 02_data.sql 插入初始数据！

============================================
' AS message;
