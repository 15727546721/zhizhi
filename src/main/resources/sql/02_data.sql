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
-- │ 3. 菜单权限数据（42个菜单/按钮，含文件管理+消息管理+反馈管理）     │
-- │ 4. 用户角色关联                                              │
-- │ 5. 角色菜单关联                                              │
-- │ 6. 默认标签（83个，7大分类）                                   │
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

-- 文件管理
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(13, 1, 'file', '/system/file', '文件管理', 4, 'el-icon-FolderOpened', 'MENU', NULL, 'File', 0, NULL),
(131, 13, NULL, NULL, '文件列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:file:list'),
(132, 13, NULL, NULL, '文件上传', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:file:upload'),
(133, 13, NULL, NULL, '文件删除', 3, NULL, 'BUTTON', NULL, NULL, 0, 'system:file:delete');

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

-- 举报管理
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(23, 2, 'report', '/content/report', '举报管理', 4, 'el-icon-Warning', 'MENU', NULL, 'Report', 0, NULL),
(231, 23, NULL, NULL, '举报列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:report:list'),
(232, 23, NULL, NULL, '处理举报', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:report:handle');

-- === 消息管理目录 ===
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(3, 0, '/message', 'Layout', '消息管理', 3, 'el-icon-Message', 'CATALOG', '/message/message', 'Message', 0, NULL);

-- 系统消息
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(30, 3, 'message', '/message/message', '系统消息', 1, 'el-icon-Bell', 'MENU', NULL, 'SystemMessage', 0, NULL),
(301, 30, NULL, NULL, '消息列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:message:list'),
(302, 30, NULL, NULL, '发送消息', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:message:send'),
(303, 30, NULL, NULL, '删除消息', 3, NULL, 'BUTTON', NULL, NULL, 0, 'system:message:delete');

-- 用户反馈
INSERT INTO `menu` (`id`, `parent_id`, `path`, `component`, `title`, `sort`, `icon`, `type`, `redirect`, `name`, `hidden`, `perm`) VALUES
(31, 3, 'feedback', '/message/feedback', '用户反馈', 2, 'el-icon-ChatLineSquare', 'MENU', NULL, 'Feedback', 0, NULL),
(311, 31, NULL, NULL, '反馈列表', 1, NULL, 'BUTTON', NULL, NULL, 0, 'system:feedback:list'),
(312, 31, NULL, NULL, '回复反馈', 2, NULL, 'BUTTON', NULL, NULL, 0, 'system:feedback:update'),
(313, 31, NULL, NULL, '删除反馈', 3, NULL, 'BUTTON', NULL, NULL, 0, 'system:feedback:delete');

-- ============================================================================
-- 第五部分：用户角色关联（只处理初始管理员，不影响普通用户）
-- ============================================================================
-- 只删除初始管理员(id<=5)的角色关联，保留其他用户
DELETE FROM `user_role` WHERE `user_id` <= 5;

INSERT INTO `user_role` (`user_id`, `role_id`) VALUES
(1, 1),  -- admin -> 超级管理员
(2, 2),  -- zhizhi_official -> 内容管理员
(3, 2),  -- content_admin -> 内容管理员
(4, 3),  -- operation_admin -> 用户管理员
(5, 4)   -- test_user -> 只读用户
ON DUPLICATE KEY UPDATE `role_id` = VALUES(`role_id`);

-- ============================================================================
-- 第六部分：角色菜单关联（菜单已在第四部分删除重建，这里直接插入）
-- ============================================================================

-- 超级管理员(role_id=1)：拥有所有菜单权限
INSERT INTO `role_menu` (`role_id`, `menu_id`)
SELECT 1, `id` FROM `menu`;

-- 内容管理员(role_id=2)：内容管理 + 文件上传 + 消息管理 + 反馈管理
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(2, 2),   -- 内容管理目录
(2, 20),  -- 帖子管理
(2, 201), (2, 202), (2, 203), (2, 204), (2, 205), (2, 206),
(2, 21),  -- 标签管理
(2, 211), (2, 212), (2, 213), (2, 214),
(2, 22),  -- 评论管理
(2, 221), (2, 222),
(2, 23),  -- 举报管理
(2, 231), (2, 232),
(2, 13),  -- 文件管理
(2, 131), (2, 132),
(2, 3),   -- 消息管理目录
(2, 30),  -- 系统消息
(2, 301), (2, 302), (2, 303),
(2, 31),  -- 用户反馈
(2, 311), (2, 312), (2, 313);

-- 用户管理员(role_id=3)：用户管理模块权限
INSERT INTO `role_menu` (`role_id`, `menu_id`) VALUES
(3, 1),   -- 系统管理目录
(3, 10),  -- 用户管理
(3, 101), (3, 102), (3, 103), (3, 104), (3, 105);

-- 只读用户(role_id=4)：只有列表查看权限
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
-- 第七部分：默认标签（83个）
-- ============================================================================
-- 分类：前端(14) + 后端(15) + AI/ML(12) + 求职(13) + 优惠(10) + 工具(10) + 通用(9)

INSERT INTO `tag` (`id`, `name`, `description`, `is_recommended`, `sort`) VALUES
-- === 前端技术（1-14）===
(1, 'Vue.js', 'Vue.js 前端框架', 1, 1),
(2, 'React', 'React 前端框架', 1, 2),
(3, 'JavaScript', 'JavaScript 编程语言', 1, 3),
(4, 'TypeScript', 'TypeScript 编程语言', 1, 4),
(5, 'CSS', 'CSS 样式技术', 0, 5),
(6, 'HTML', 'HTML 标记语言', 0, 6),
(7, 'Webpack', 'Webpack 构建工具', 0, 7),
(8, 'Vite', 'Vite 构建工具', 0, 8),
(9, '前端工程化', '前端工程化实践', 1, 9),
(10, '性能优化', '前端性能优化', 1, 10),
(11, 'Node.js', 'Node.js 服务端 JavaScript', 1, 11),
(12, '微信小程序', '微信小程序开发', 0, 12),
(13, 'Uniapp', 'Uniapp 跨端框架', 0, 13),
(14, '组件库', '前端组件库', 0, 14),

-- === 后端技术（15-29）===
(15, 'Spring Boot', 'Spring Boot 后端框架', 1, 15),
(16, 'Java', 'Java 编程语言', 1, 16),
(17, 'Python', 'Python 编程语言', 1, 17),
(18, 'Go', 'Go 编程语言', 0, 18),
(19, '微服务', '微服务架构', 1, 19),
(20, '分布式', '分布式系统', 1, 20),
(21, 'MySQL', 'MySQL 数据库', 1, 21),
(22, 'Redis', 'Redis 缓存数据库', 1, 22),
(23, 'MongoDB', 'MongoDB NoSQL 数据库', 0, 23),
(24, '消息队列', '消息队列技术', 0, 24),
(25, 'Docker', 'Docker 容器技术', 1, 25),
(26, 'Kubernetes', 'K8s 容器编排', 0, 26),
(27, 'MyBatis', 'MyBatis ORM 框架', 0, 27),
(28, 'Spring Cloud', 'Spring Cloud 微服务', 0, 28),
(29, 'Nginx', 'Nginx Web 服务器', 0, 29),

-- === AI/机器学习（30-41）===
(30, '深度学习', '深度学习技术', 1, 30),
(31, '机器学习', '机器学习算法', 1, 31),
(32, 'PyTorch', 'PyTorch 深度学习框架', 1, 32),
(33, 'TensorFlow', 'TensorFlow 深度学习框架', 1, 33),
(34, 'NLP', '自然语言处理', 1, 34),
(35, '计算机视觉', '计算机视觉技术', 1, 35),
(36, '算法', '算法与数据结构', 1, 36),
(37, '大模型', 'LLM 大语言模型', 1, 37),
(38, '强化学习', '强化学习算法', 0, 38),
(39, '数据分析', '数据分析方法', 0, 39),
(40, '特征工程', '特征工程技术', 0, 40),
(41, '模型部署', 'AI 模型部署', 0, 41),

-- === 求职/面试（42-54）===
(42, '面试', '面试经验分享', 1, 42),
(43, '算法题', '算法面试题', 1, 43),
(44, '简历', '简历优化指导', 1, 44),
(45, '职业规划', '职业发展规划', 1, 45),
(46, '大厂面经', '大厂面试经验', 1, 46),
(47, '技术面试', '技术面试准备', 0, 47),
(48, 'HR面试', 'HR 面试技巧', 0, 48),
(49, '跳槽', '跳槽经验', 0, 49),
(50, '薪资谈判', '薪资谈判技巧', 0, 50),
(51, 'Offer选择', 'Offer 选择建议', 0, 51),
(52, '职场经验', '职场生存指南', 0, 52),
(53, '校招', '校园招聘', 0, 53),
(54, '社招', '社会招聘', 0, 54),

-- === 优惠/福利（55-64）===
(55, '京东', '京东优惠活动', 1, 55),
(56, '淘宝', '淘宝优惠活动', 1, 56),
(57, '拼多多', '拼多多优惠活动', 1, 57),
(58, '话费充值', '话费充值优惠', 1, 58),
(59, '外卖优惠', '外卖红包优惠', 1, 59),
(60, '视频会员', '视频会员优惠', 0, 60),
(61, '信用卡', '信用卡优惠活动', 0, 61),
(62, '购物返利', '购物返利平台', 0, 62),
(63, '理财', '理财产品推荐', 0, 63),
(64, '薅羊毛攻略', '薅羊毛全攻略', 1, 64),

-- === 工具/软件（65-74）===
(65, '效率工具', '提升效率的工具', 1, 65),
(66, '开源软件', '开源软件推荐', 1, 66),
(67, 'Chrome插件', 'Chrome 浏览器插件', 0, 67),
(68, 'Windows', 'Windows 系统工具', 0, 68),
(69, 'macOS', 'macOS 系统工具', 0, 69),
(70, 'VSCode', 'VSCode 编辑器', 0, 70),
(72, '下载工具', '下载工具推荐', 0, 72),
(73, '办公软件', '办公软件推荐', 0, 73),
(74, '设计工具', '设计工具推荐', 0, 74),

-- === 通用标签（75-83）===
(75, '入门教程', '入门级教程', 0, 75),
(76, '最佳实践', '最佳实践分享', 0, 76),
(77, '踩坑记录', '避坑指南', 0, 77),
(78, '源码解析', '源码分析', 0, 78),
(79, '开源项目', '开源项目推荐', 0, 79),
(80, '资源分享', '学习资源分享', 0, 80),
(81, '工具推荐', '工具推荐', 0, 81),
(82, '经验总结', '经验总结', 0, 82),
(83, '技术选型', '技术选型指南', 0, 83)
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

🏷️ 默认标签（83个，7大分类）：
   - 前端技术(14)：Vue.js、React、JavaScript、TypeScript...
   - 后端技术(15)：Spring Boot、Java、MySQL、Redis、Docker...
   - AI/机器学习(12)：深度学习、PyTorch、大模型、NLP...
   - 求职面试(13)：面试、简历、大厂面经、职业规划...
   - 优惠福利(10)：京东、淘宝、外卖优惠、薅羊毛攻略...
   - 工具软件(9)：效率工具、开源软件、VSCode...
   - 通用标签(9)：入门教程、最佳实践、源码解析...

🔐 权限体系：
   - 超级管理员：所有权限
   - 内容管理员：帖子、标签、评论、消息管理、用户反馈
   - 用户管理员：用户管理
   - 只读用户：仅查看权限

⚠️ 重要提醒：生产环境请立即修改所有管理员密码！

============================================================================
' AS message;
