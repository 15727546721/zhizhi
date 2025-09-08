-- 初始化权限数据脚本
-- 创建默认角色
INSERT INTO `role` (`id`, `code`, `name`, `remark`, `create_time`, `update_time`) VALUES
(1, 'admin', '超级管理员', '系统超级管理员，拥有所有权限', NOW(), NOW()),
(2, 'user', '普通用户', '系统普通用户', NOW(), NOW());

-- 创建默认菜单
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `create_time`, `update_time`, `redirect`, `name`, `hidden`, `perm`) VALUES
(1, 0, '/system', 'Layout', '系统管理', 1, 'system', 'CATALOG', NOW(), NOW(), '/system/user', 'System', 0, NULL),
(2, 1, 'user', 'system/user/index', '用户管理', 1, 'user', 'MENU', NOW(), NOW(), NULL, 'User', 0, 'system:user:list'),
(3, 1, 'role', 'system/role/index', '角色管理', 2, 'peoples', 'MENU', NOW(), NOW(), NULL, 'Role', 0, 'system:role:list'),
(4, 1, 'menu', 'system/menu/index', '菜单管理', 3, 'tree-table', 'MENU', NOW(), NOW(), NULL, 'Menu', 0, 'system:menu:list'),
(5, 1, 'dept', 'system/dept/index', '部门管理', 4, 'tree', 'MENU', NOW(), NOW(), NULL, 'Dept', 0, 'system:dept:list'),
(6, 1, 'post', 'system/post/index', '岗位管理', 5, 'post', 'MENU', NOW(), NOW(), NULL, 'Post', 0, 'system:post:list'),
(7, 1, 'dict', 'system/dict/index', '字典管理', 6, 'dict', 'MENU', NOW(), NOW(), NULL, 'Dict', 0, 'system:dict:list'),
(8, 1, 'config', 'system/config/index', '参数管理', 7, 'edit', 'MENU', NOW(), NOW(), NULL, 'Config', 0, 'system:config:list'),
(9, 1, 'notice', 'system/notice/index', '通知公告', 8, 'message', 'MENU', NOW(), NOW(), NULL, 'Notice', 0, 'system:notice:list'),
(10, 1, 'log', 'system/log/index', '日志管理', 9, 'log', 'MENU', NOW(), NOW(), NULL, 'Log', 0, 'system:log:list'),
(11, 2, '', '', '用户查询', 1, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:user:query'),
(12, 2, '', '', '用户新增', 2, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:user:add'),
(13, 2, '', '', '用户修改', 3, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:user:edit'),
(14, 2, '', '', '用户删除', 4, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:user:remove'),
(15, 2, '', '', '用户导出', 5, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:user:export'),
(16, 3, '', '', '角色查询', 1, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:role:query'),
(17, 3, '', '', '角色新增', 2, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:role:add'),
(18, 3, '', '', '角色修改', 3, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:role:edit'),
(19, 3, '', '', '角色删除', 4, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:role:remove'),
(20, 3, '', '', '角色导出', 5, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:role:export'),
(21, 4, '', '', '菜单查询', 1, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:menu:query'),
(22, 4, '', '', '菜单新增', 2, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:menu:add'),
(23, 4, '', '', '菜单修改', 3, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:menu:edit'),
(24, 4, '', '', '菜单删除', 4, '', 'BUTTON', NOW(), NOW(), NULL, '', 0, 'system:menu:remove');

-- 创建角色菜单关联关系（超级管理员拥有所有菜单权限）
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(1, 11), (1, 12), (1, 13), (1, 14), (1, 15), (1, 16), (1, 17), (1, 18), (1, 19), (1, 20),
(1, 21), (1, 22), (1, 23), (1, 24);

-- 创建默认用户与角色关联（假设用户ID为1的用户是超级管理员）
INSERT INTO `user_role` (`user_id`, `role_id`) VALUES (1, 1);