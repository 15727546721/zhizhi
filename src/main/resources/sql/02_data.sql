-- ============================================================================
-- 知知社区 - 数据初始化脚本
-- ============================================================================
-- 文件：02_data.sql
-- 版本：V1.0
-- 创建时间：2025-11-30
-- 
-- 依赖：01_schema.sql（需先执行表结构创建）
-- 
-- 统一密码：AdminPassword123!
-- 
-- 初始化内容：
-- ┌─────────────────────────────────────────────────────────────┐
-- │ 1. 用户数据（5个管理员账号）                                   │
-- │ 2. 角色数据（4个角色）                                        │
-- │ 3. 菜单权限数据（27个菜单/按钮）                               │
-- │ 4. 用户角色关联                                              │
-- │ 5. 角色菜单关联                                              │
-- │ 6. 默认标签（10个）                                          │
-- └─────────────────────────────────────────────────────────────┘
-- ============================================================================

USE `zhizhi`;
SET NAMES utf8mb4;

-- ============================================================================
-- 第一部分：清空现有数据（可选，谨慎执行）
-- ============================================================================
-- DELETE FROM `role_menu`;
-- DELETE FROM `user_role`;
-- DELETE FROM `menu`;
-- DELETE FROM `role`;
-- DELETE FROM `tag`;
-- 不删除 user 表数据，避免外键问题

-- ============================================================================
-- 第二部分：用户数据
-- ============================================================================
-- 统一密码：AdminPassword123!
-- 加密方式：SHA2(password, 256) - 与Java SaSecureUtil.sha256()一致

-- 超级管理员
INSERT INTO `user` (`id`, `username`, `email`, `password`, `nickname`, `user_type`, `status`, `description`) VALUES
(1, 'admin', 'admin@zhizhi.com', SHA2('AdminPassword123!', 256), '超级管理员', 3, 1, '系统超级管理员，拥有所有权限')
ON DUPLICATE KEY UPDATE 
    `password` = SHA2('AdminPassword123!', 256),
    `nickname` = '超级管理员',
    `user_type` = 3,
    `status` = 1;

-- 官方运营账号
INSERT INTO `user` (`id`, `username`, `email`, `password`, `nickname`, `user_type`, `status`, `avatar`, `description`) VALUES
(2, 'zhizhi_official', 'official@zhizhi.com', SHA2('AdminPassword123!', 256), '知知小助手', 2, 1, 'https://cdn.zhizhi.com/avatars/official.png', '知知社区官方运营账号')
ON DUPLICATE KEY UPDATE 
    `password` = SHA2('AdminPassword123!', 256),
    `nickname` = '知知小助手',
    `user_type` = 2,
    `status` = 1;

-- 内容管理员
INSERT INTO `user` (`id`, `username`, `email`, `password`, `nickname`, `user_type`, `status`, `description`) VALUES
(3, 'content_admin', 'content@zhizhi.com', SHA2('AdminPassword123!', 256), '内容管理员', 3, 1, '负责社区内容审核和管理')
ON DUPLICATE KEY UPDATE 
    `password` = SHA2('AdminPassword123!', 256),
    `nickname` = '内容管理员',
    `user_type` = 3,
    `status` = 1;

-- 运营管理员
INSERT INTO `user` (`id`, `username`, `email`, `password`, `nickname`, `user_type`, `status`, `description`) VALUES
(4, 'operation_admin', 'operation@zhizhi.com', SHA2('AdminPassword123!', 256), '运营管理员', 3, 1, '负责社区运营和用户管理')
ON DUPLICATE KEY UPDATE 
    `password` = SHA2('AdminPassword123!', 256),
    `nickname` = '运营管理员',
    `user_type` = 3,
    `status` = 1;

-- 测试用户
INSERT INTO `user` (`id`, `username`, `email`, `password`, `nickname`, `user_type`, `status`, `gender`, `region`) VALUES
(5, 'test_user', 'test@example.com', SHA2('AdminPassword123!', 256), '测试用户', 1, 1, 1, '北京市')
ON DUPLICATE KEY UPDATE 
    `password` = SHA2('AdminPassword123!', 256),
    `nickname` = '测试用户',
    `user_type` = 1,
    `status` = 1;

-- ============================================================================
-- 第三部分：角色数据
-- ============================================================================
INSERT INTO `role` (`id`, `code`, `name`, `remark`) VALUES
(1, 'super_admin', '超级管理员', '拥有系统所有权限'),
(2, 'content_admin', '内容管理员', '管理帖子、标签、评论'),
(3, 'user_admin', '用户管理员', '管理用户账号'),
(4, 'viewer', '只读用户', '只有查看权限')
ON DUPLICATE KEY UPDATE 
    `name` = VALUES(`name`),
    `remark` = VALUES(`remark`);

-- ============================================================================
-- 第四部分：菜单权限数据
-- ============================================================================

-- 清空菜单表重新插入
DELETE FROM `role_menu`;
DELETE FROM `menu`;

-- === 系统管理目录 ===
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(1, 0, '/system', 'Layout', '系统管理', 1, 'el-icon-Setting', 'CATALOG', '/system/user', 'System', 0, NULL);

-- 用户管理
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(10, 1, 'user', '/system/user', '用户管理', 1, 'el-icon-User', 'MENU', NULL, 'User', 0, NULL),
(101, 10, NULL, NULL, '用户列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:user:list'),
(102, 10, NULL, NULL, '用户添加', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:user:add'),
(103, 10, NULL, NULL, '用户修改', 3, NULL, 'BUTTON', NULL, NULL, 0, 'system:user:update'),
(104, 10, NULL, NULL, '用户删除', 4, NULL, 'BUTTON', NULL, NULL, 0, 'system:user:delete'),
(105, 10, NULL, NULL, '重置密码', 5, NULL, 'BUTTON', NULL, NULL, 0, 'system:user:resetPwd');

-- 角色管理
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(11, 1, 'role', '/system/role', '角色管理', 2, 'el-icon-Avatar', 'MENU', NULL, 'Role', 0, NULL),
(111, 11, NULL, NULL, '角色列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:role:list'),
(112, 11, NULL, NULL, '角色添加', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:role:add'),
(113, 11, NULL, NULL, '角色修改', 3, NULL, 'BUTTON', NULL, NULL, 0, 'system:role:update'),
(114, 11, NULL, NULL, '角色删除', 4, NULL, 'BUTTON', NULL, NULL, 0, 'system:role:delete'),
(115, 11, NULL, NULL, '分配权限', 5, NULL, 'BUTTON', NULL, NULL, 0, 'system:role:assign');

-- 菜单管理
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(12, 1, 'menu', '/system/menu', '菜单管理', 3, 'el-icon-Menu', 'MENU', NULL, 'Menu', 0, NULL),
(121, 12, NULL, NULL, '菜单列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:menu:list'),
(122, 12, NULL, NULL, '菜单添加', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:menu:add'),
(123, 12, NULL, NULL, '菜单修改', 3, NULL, 'BUTTON', NULL, NULL, 0, 'system:menu:update'),
(124, 12, NULL, NULL, '菜单删除', 4, NULL, 'BUTTON', NULL, NULL, 0, 'system:menu:delete');

-- === 内容管理目录 ===
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(2, 0, '/content', 'Layout', '内容管理', 2, 'el-icon-Document', 'CATALOG', '/content/post', 'Content', 0, NULL);

-- 帖子管理
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(20, 2, 'post', '/posts/index', '帖子管理', 1, 'el-icon-Notebook', 'MENU', NULL, 'Post', 0, NULL),
(201, 20, NULL, NULL, '帖子列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:post:list'),
(202, 20, NULL, NULL, '帖子添加', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:post:add'),
(203, 20, NULL, NULL, '帖子修改', 3, NULL, 'BUTTON', NULL, NULL, 0, 'system:post:update'),
(204, 20, NULL, NULL, '帖子删除', 4, NULL, 'BUTTON', NULL, NULL, 0, 'system:post:delete'),
(205, 20, NULL, NULL, '帖子发布', 5, NULL, 'BUTTON', NULL, NULL, 0, 'system:post:publish'),
(206, 20, NULL, NULL, '帖子置顶', 6, NULL, 'BUTTON', NULL, NULL, 0, 'system:post:top');

-- 标签管理
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(21, 2, 'tag', '/content/tag', '标签管理', 2, 'el-icon-CollectionTag', 'MENU', NULL, 'Tag', 0, NULL),
(211, 21, NULL, NULL, '标签列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:tag:list'),
(212, 21, NULL, NULL, '标签添加', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:tag:add'),
(213, 21, NULL, NULL, '标签修改', 3, NULL, 'BUTTON', NULL, NULL, 0, 'system:tag:update'),
(214, 21, NULL, NULL, '标签删除', 4, NULL, 'BUTTON', NULL, NULL, 0, 'system:tag:delete');

-- 评论管理
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(22, 2, 'comment', '/content/comment', '评论管理', 3, 'el-icon-ChatDotRound', 'MENU', NULL, 'Comment', 0, NULL),
(221, 22, NULL, NULL, '评论列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:comment:list'),
(222, 22, NULL, NULL, '评论删除', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:comment:delete');

-- ============================================================================
-- 第五部分：用户角色关联
-- ============================================================================
DELETE FROM `user_role`;

INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> 超级管理员
(2, 2),  -- zhizhi_official -> 内容管理员
(3, 2),  -- content_admin -> 内容管理员
(4, 3),  -- operation_admin -> 用户管理员
(5, 4);  -- test_user -> 只读用户

-- ============================================================================
-- 第六部分：角色菜单关联
-- ============================================================================

-- 超级管理员：拥有所有权限
INSERT INTO `role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `menu`;

-- 内容管理员：内容管理模块全部权限
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(2, 2),   -- 内容管理目录
(2, 20),  -- 帖子管理
(2, 201), (2, 202), (2, 203), (2, 204), (2, 205), (2, 206),
(2, 21),  -- 标签管理
(2, 211), (2, 212), (2, 213), (2, 214),
(2, 22),  -- 评论管理
(2, 221), (2, 222);

-- 用户管理员：用户管理模块权限
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(3, 1),   -- 系统管理目录
(3, 10),  -- 用户管理
(3, 101), (3, 102), (3, 103), (3, 104), (3, 105);

-- 只读用户：只有列表查看权限
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(4, 1),   -- 系统管理目录
(4, 10),  -- 用户管理
(4, 101), -- 用户列表
(4, 2),   -- 内容管理目录
(4, 20),  -- 帖子管理
(4, 201), -- 帖子列表
(4, 21),  -- 标签管理
(4, 211), -- 标签列表
(4, 22),  -- 评论管理
(4, 221); -- 评论列表

-- ============================================================================
-- 第七部分：默认标签
-- ============================================================================
INSERT INTO `tag` (`id`, `name`, `description`, `is_recommended`, `sort`) VALUES
(1, '技术', '技术讨论与分享', 1, 1),
(2, '前端', '前端开发技术', 1, 2),
(3, '后端', '后端开发技术', 1, 3),
(4, 'Java', 'Java编程语言', 1, 4),
(5, 'Python', 'Python编程语言', 1, 5),
(6, '数据库', '数据库技术', 1, 6),
(7, '问答', '问题求助', 1, 7),
(8, '分享', '经验分享', 1, 8),
(9, '生活', '生活点滴', 0, 9),
(10, '闲聊', '闲聊灌水', 0, 10)
ON DUPLICATE KEY UPDATE 
    `description` = VALUES(`description`),
    `is_recommended` = VALUES(`is_recommended`),
    `sort` = VALUES(`sort`);

-- ============================================================================
-- 第八部分：验证数据
-- ============================================================================

SELECT '========== 用户数据 ==========' AS '';
SELECT id, username, nickname, 
       CASE user_type WHEN 1 THEN '普通用户' WHEN 2 THEN '官方账号' WHEN 3 THEN '管理员' END AS user_type,
       CASE status WHEN 0 THEN '禁用' WHEN 1 THEN '正常' END AS status
FROM `user` WHERE id <= 5 ORDER BY id;

SELECT '========== 角色数据 ==========' AS '';
SELECT id, code, name, remark FROM `role` ORDER BY id;

SELECT '========== 标签数据 ==========' AS '';
SELECT id, name, description, is_recommended FROM `tag` ORDER BY sort;

SELECT '========== 统计信息 ==========' AS '';
SELECT 
  (SELECT COUNT(*) FROM `user`) AS user_count,
  (SELECT COUNT(*) FROM `role`) AS role_count,
  (SELECT COUNT(*) FROM `menu`) AS menu_count,
  (SELECT COUNT(*) FROM `tag`) AS tag_count,
  (SELECT COUNT(*) FROM `user_role`) AS user_role_count,
  (SELECT COUNT(*) FROM `role_menu`) AS role_menu_count;

-- ============================================================================
-- 完成
-- ============================================================================
SELECT '
============================================================================
✅ 知知社区数据初始化完成！
============================================================================

👤 管理员账号：
   ┌─────────────────┬─────────────────────┬────────────────┐
   │ 用户名          │ 角色                │ 说明           │
   ├─────────────────┼─────────────────────┼────────────────┤
   │ admin           │ 超级管理员          │ 所有权限       │
   │ zhizhi_official │ 内容管理员          │ 官方运营账号   │
   │ content_admin   │ 内容管理员          │ 内容审核       │
   │ operation_admin │ 用户管理员          │ 用户管理       │
   │ test_user       │ 只读用户            │ 测试账号       │
   └─────────────────┴─────────────────────┴────────────────┘

🔐 密码加密：
   加密方式：SHA256（与Java SaSecureUtil.sha256一致）
   默认密码：AdminPassword123!

🏷️ 默认标签（10个）：
   技术、前端、后端、Java、Python、数据库、问答、分享、生活、闲聊

🔐 权限体系：
   - 超级管理员：所有权限
   - 内容管理员：帖子、标签、评论管理
   - 用户管理员：用户管理
   - 只读用户：仅查看权限

⚠️ 重要提醒：生产环境请立即修改所有管理员密码！

============================================================================
' AS message;
