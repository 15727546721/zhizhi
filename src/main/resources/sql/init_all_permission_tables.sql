-- 创建所有权限相关的表结构和初始化数据

-- 创建角色表
CREATE TABLE `role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code` varchar(64) NOT NULL COMMENT '角色编码',
  `name` varchar(128) NOT NULL COMMENT '角色名称',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`code`),
  KEY `idx_role_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 创建菜单表
CREATE TABLE `menu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `parent_id` bigint unsigned NOT NULL DEFAULT '0' COMMENT '上级资源ID',
  `path` varchar(255) DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) DEFAULT NULL COMMENT '组件路径',
  `title` varchar(128) NOT NULL COMMENT '菜单名称',
  `sort` int NOT NULL DEFAULT '0' COMMENT '排序',
  `icon` varchar(128) DEFAULT NULL COMMENT '资源图标',
  `type` varchar(32) NOT NULL COMMENT '类型 menu、button',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `redirect` varchar(255) DEFAULT NULL COMMENT '重定向地址',
  `name` varchar(128) DEFAULT NULL COMMENT '跳转地址',
  `hidden` tinyint NOT NULL DEFAULT '0' COMMENT '是否隐藏',
  `perm` varchar(128) DEFAULT NULL COMMENT '权限标识',
  PRIMARY KEY (`id`),
  KEY `idx_menu_parent_id` (`parent_id`),
  KEY `idx_menu_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统管理-权限资源表';

-- 创建用户角色关联表
CREATE TABLE `user_role` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint unsigned NOT NULL COMMENT '用户ID',
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 创建角色菜单关联表
CREATE TABLE `role_menu` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint unsigned NOT NULL COMMENT '角色ID',
  `menu_id` bigint unsigned NOT NULL COMMENT '菜单ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_menu_id` (`menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

-- 创建用户权限关联表
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