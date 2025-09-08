-- 用户权限关联表
CREATE TABLE `user_permission` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `permission_id` bigint unsigned NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_permission` (`user_id`, `permission_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户权限关联表';