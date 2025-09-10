-- 举报表
CREATE TABLE `report` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '举报ID',
  `target_type` tinyint NOT NULL COMMENT '举报类型 1-文章 2-评论 3-用户 4-话题',
  `target_id` bigint unsigned NOT NULL COMMENT '被举报的目标ID',
  `reporter_id` bigint unsigned NOT NULL COMMENT '举报人ID',
  `reason` varchar(50) NOT NULL COMMENT '举报原因',
  `detail` varchar(500) DEFAULT NULL COMMENT '举报详情',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '举报状态 0-待处理 1-已处理 2-已忽略',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `handler_id` bigint unsigned DEFAULT NULL COMMENT '处理人ID',
  `handle_result` varchar(500) DEFAULT NULL COMMENT '处理结果',
  PRIMARY KEY (`id`),
  KEY `idx_target` (`target_type`,`target_id`),
  KEY `idx_reporter` (`reporter_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报表';

-- 举报原因配置表（可选，用于管理举报原因选项）
CREATE TABLE `report_reason_config` (
  `id` int unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `reason_code` varchar(20) NOT NULL COMMENT '原因编码',
  `reason_name` varchar(50) NOT NULL COMMENT '原因名称',
  `target_type` tinyint NOT NULL COMMENT '适用的目标类型 1-文章 2-评论 3-用户 4-话题 0-通用',
  `sort_order` int NOT NULL DEFAULT '0' COMMENT '排序',
  `is_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '是否启用 1-启用 0-禁用',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_reason_code` (`reason_code`),
  KEY `idx_target_type` (`target_type`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='举报原因配置表';

-- 初始化举报原因数据
INSERT INTO `report_reason_config` (`reason_code`, `reason_name`, `target_type`, `sort_order`) VALUES
('spam', '垃圾广告', 0, 1),
('porn', '色情低俗', 0, 2),
('violence', '暴力恐怖', 0, 3),
('politics', '政治敏感', 0, 4),
('abuse', '辱骂攻击', 0, 5),
('fake', '不实信息', 0, 6),
('plagiarism', '抄袭盗用', 1, 7),
('inappropriate', '内容不当', 0, 8),
('other', '其他原因', 0, 9);