-- 文件相关表结构
-- 创建时间: 2025-09-24
-- 适用于MySQL 8.0及以上版本

-- 1. 文件表 (file)
CREATE TABLE `file` (
  `file_id` VARCHAR(64) NOT NULL COMMENT '文件唯一标识',
  `original_name` VARCHAR(255) NOT NULL COMMENT '原始文件名',
  `system_name` VARCHAR(255) NOT NULL COMMENT '系统生成的文件名',
  `file_url` VARCHAR(500) NOT NULL COMMENT '文件访问URL',
  `storage_path` VARCHAR(500) NOT NULL COMMENT '文件存储路径',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `mime_type` VARCHAR(100) NOT NULL COMMENT '文件类型/MIME类型',
  `file_extension` VARCHAR(20) NOT NULL COMMENT '文件扩展名',
  `status` ENUM('TEMPORARY', 'FORMAL', 'DELETED') NOT NULL DEFAULT 'TEMPORARY' COMMENT '文件状态',
  `upload_user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '上传用户ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`file_id`),
  KEY `idx_upload_user_id` (`upload_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件表';