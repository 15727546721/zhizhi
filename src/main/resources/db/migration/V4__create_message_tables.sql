-- 私信会话表
CREATE TABLE `message_conversation` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `conversation_id` varchar(64) NOT NULL COMMENT '会话ID：较小用户ID_较大用户ID',
    `user_id_smaller` bigint(20) NOT NULL COMMENT '较小的用户ID',
    `user_id_larger` bigint(20) NOT NULL COMMENT '较大的用户ID',
    `last_message` varchar(500) DEFAULT NULL COMMENT '最后一条消息内容',
    `last_message_type` varchar(20) DEFAULT NULL COMMENT '最后一条消息类型：TEXT/IMAGE/FILE',
    `last_send_time` datetime DEFAULT NULL COMMENT '最后发送时间',
    `unread_smaller` int(11) NOT NULL DEFAULT '0' COMMENT '较小用户ID的未读数',
    `unread_larger` int(11) NOT NULL DEFAULT '0' COMMENT '较大用户ID的未读数',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1-正常，2-已删除',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_conversation_id` (`conversation_id`),
    KEY `idx_user_smaller` (`user_id_smaller`, `status`, `last_send_time`),
    KEY `idx_user_larger` (`user_id_larger`, `status`, `last_send_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信会话表';

-- 私信内容表（分表，按月分表）
CREATE TABLE `message_content_202401` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `conversation_id` varchar(64) NOT NULL COMMENT '会话ID',
    `message_id` varchar(64) NOT NULL COMMENT '消息ID：毫秒时间戳_发送者ID_4位随机数',
    `sender_id` bigint(20) NOT NULL COMMENT '发送者ID',
    `receiver_id` bigint(20) NOT NULL COMMENT '接收者ID',
    `content` text NOT NULL COMMENT '消息内容',
    `content_type` varchar(20) NOT NULL COMMENT '消息类型：TEXT/IMAGE/FILE',
    `extra` json DEFAULT NULL COMMENT '额外信息：图片大小、文件信息等',
    `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1-未读，2-已读，3-已删除，4-已撤回',
    `read_time` datetime DEFAULT NULL COMMENT '读取时间',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_message_id` (`message_id`),
    KEY `idx_conversation_time` (`conversation_id`, `create_time`),
    KEY `idx_sender_time` (`sender_id`, `create_time`),
    KEY `idx_receiver_status` (`receiver_id`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信内容表';

-- 私信关系表
CREATE TABLE `message_relation` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `friend_id` bigint(20) NOT NULL COMMENT '好友ID',
    `remark` varchar(50) DEFAULT NULL COMMENT '备注名',
    `is_blocked` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否拉黑：0-否，1-是',
    `is_sticky` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否置顶：0-否，1-是',
    `is_muted` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否免打扰：0-否，1-是',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
    KEY `idx_friend_id` (`friend_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信关系表';

-- 私信敏感词表
CREATE TABLE `message_sensitive_word` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT,
    `word` varchar(50) NOT NULL COMMENT '敏感词',
    `level` tinyint(4) NOT NULL DEFAULT '1' COMMENT '级别：1-替换，2-拦截，3-封禁',
    `replace_word` varchar(50) DEFAULT NULL COMMENT '替换词',
    `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='私信敏感词表'; 