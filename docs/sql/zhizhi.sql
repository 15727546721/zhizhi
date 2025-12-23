/*
 Navicat Premium Data Transfer

 Source Server         : mysql-docker
 Source Server Type    : MySQL
 Source Server Version : 80032
 Source Host           : localhost:13306
 Source Schema         : zhizhi

 Target Server Type    : MySQL
 Target Server Version : 80032
 File Encoding         : 65001

 Date: 30/12/2024 10:05:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for article
-- ----------------------------
DROP TABLE IF EXISTS `article`;
CREATE TABLE `article`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文章ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章标题',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章介绍',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文章内容',
  `cover_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文章封面图片的URL',
  `user_id` bigint NOT NULL COMMENT '作者ID',
  `view_count` bigint NULL DEFAULT 0 COMMENT '阅读次数',
  `collect_count` bigint NULL DEFAULT NULL COMMENT '收藏次数',
  `like_count` bigint NULL DEFAULT NULL COMMENT '点赞次数',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article
-- ----------------------------
INSERT INTO `article` VALUES (21, '快一点！再快一点！！接口性能优化多线程篇', '这篇文章是程序员木木熊关于接口性能优化多线程篇的分享。介绍了接口性能优化的重要性，包括异步处理（主次有序）、串行改并行、线程池优化。异步处理有线程池、Spring注解@Async、CompletableFuture三种方式。串行改并行有CountDownLatch和CompletableFuture两种实现方案。线程池优化要合理设置参数，不推荐使用Executors创建的线程池，且要注意默认线程池', '# 一、异步处理：主次有序\n异步处理最常见的处理方式有线程、MQ、事件通知等，本主要介绍线程相关的方式。\n一般的业务接口都会有一个主要业务逻辑和次要业务逻辑，有点像游戏里面的主线任务和支线任务。主要业务逻辑一般是重要的、核心的、影响执行结果和流程的，像订单创建，支付等，而次要业务逻辑，一般是主要逻辑的附属操作不影响整体业务的结果，像消息通知、数据埋点、日志记录等。\n以创建订单的例子来说明，先看看不使用异步处理，代码同步串行执行，createOrder方法总耗时为200ms。如果改用线程进行异步处理，在保存完订单，向线程池提交发消息和记录日志的任务后，就可以立即返回。提交任务时间基本可以忽略不计，故createOrder方法总耗时直接降为100ms，为串行执行的50%。\n![](http://localhost:5000/your-bucket-name/266e93e2-4b90-4f41-8c7d-e1e831654d4f_image.png)\n同步执行示例代码如下\n```java\n    //创建订单-串行执行\n    public void createOrder() {\n        //核心逻辑 - 保存order 100ms\n        saveOrder();\n        //发送消息 - 50ms\n        msgService.sendMsg();\n        //记录日志 - 50ms\n        oprLogService.saveOprLog();\n        //执行完成后返回，总耗时100 + 50 + 50 = 200ms\n    }\n\n```\n## 1.直接使用线程池\n## 2.使用Spring注解@Async\n\n# 二、串行改并行：多车道高速公路\n# 三、线程池优化：合理调配系统资源', 'https://picsum.photos/500/300?random=1735093510', 1001, NULL, NULL, NULL, '2024-12-25 10:27:42', '2024-12-25 10:29:42');

-- ----------------------------
-- Table structure for article_category
-- ----------------------------
DROP TABLE IF EXISTS `article_category`;
CREATE TABLE `article_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '分类描述',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储文章分类信息的表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article_category
-- ----------------------------
INSERT INTO `article_category` VALUES (12, '后端', '32132急急', '2024-09-12 14:48:05', '2024-12-25 10:28:16');
INSERT INTO `article_category` VALUES (14, '安卓', '312', '2024-09-12 14:59:08', '2024-10-09 11:21:39');
INSERT INTO `article_category` VALUES (15, '前端', '321', '2024-09-12 15:23:02', '2024-12-25 10:28:26');
INSERT INTO `article_category` VALUES (16, '机器人', '321', '2024-09-12 15:23:15', '2024-12-25 10:28:37');
INSERT INTO `article_category` VALUES (17, '人工智能', '312', '2024-09-12 15:23:20', '2024-12-25 10:28:45');

-- ----------------------------
-- Table structure for article_category_relation
-- ----------------------------
DROP TABLE IF EXISTS `article_category_relation`;
CREATE TABLE `article_category_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `article_id` bigint NULL DEFAULT NULL COMMENT '文章ID',
  `category_id` bigint NULL DEFAULT NULL COMMENT '分类ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章分类关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article_category_relation
-- ----------------------------
INSERT INTO `article_category_relation` VALUES (63, 21, 12);

-- ----------------------------
-- Table structure for article_collection
-- ----------------------------
DROP TABLE IF EXISTS `article_collection`;
CREATE TABLE `article_collection`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '收藏文章ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `article_id` int NOT NULL COMMENT '文章ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_favorite`(`user_id`, `article_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article_collection
-- ----------------------------

-- ----------------------------
-- Table structure for article_tag
-- ----------------------------
DROP TABLE IF EXISTS `article_tag`;
CREATE TABLE `article_tag`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `description` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签描述',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article_tag
-- ----------------------------
INSERT INTO `article_tag` VALUES (2, 'vue3', '一个前端框架，vue2的升级，但是不兼容vue2', '2024-09-29 17:23:59', '2024-09-29 17:23:59');
INSERT INTO `article_tag` VALUES (3, 'react', '', '2024-12-03 18:26:48', '2024-12-03 18:26:48');
INSERT INTO `article_tag` VALUES (6, 'Java', NULL, '2024-12-25 10:28:57', '2024-12-25 10:28:57');
INSERT INTO `article_tag` VALUES (7, 'Go', NULL, '2024-12-25 10:29:10', '2024-12-25 10:29:10');
INSERT INTO `article_tag` VALUES (8, 'vue', NULL, '2024-12-25 10:29:21', '2024-12-25 10:29:21');

-- ----------------------------
-- Table structure for article_tag_relation
-- ----------------------------
DROP TABLE IF EXISTS `article_tag_relation`;
CREATE TABLE `article_tag_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `article_id` bigint NOT NULL COMMENT '文章ID',
  `tag_id` bigint NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`) USING BTREE COMMENT '主键，唯一标识每条记录'
) ENGINE = InnoDB AUTO_INCREMENT = 74 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '存储文章与标签之间的多对多关系' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of article_tag_relation
-- ----------------------------
INSERT INTO `article_tag_relation` VALUES (73, 21, 6);

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论ID',
  `type` int NOT NULL COMMENT '评论类型，如1-文章；2-话题',
  `target_id` bigint NOT NULL COMMENT '评论来源的标识符',
  `parent_comment_id` bigint NULL DEFAULT NULL COMMENT '父评论的唯一标识符，顶级评论为NULL',
  `user_id` bigint NOT NULL COMMENT '发表评论的用户ID',
  `reply_to_user_id` bigint NULL DEFAULT NULL COMMENT '回复的用户ID，若为回复评论则存在',
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '评论的具体内容',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '评论表，用于存储用户评论及其相关信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------

-- ----------------------------
-- Table structure for like
-- ----------------------------
DROP TABLE IF EXISTS `like`;
CREATE TABLE `like`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '点赞ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `type` int NULL DEFAULT NULL COMMENT '点赞类型：1-文章，2-话题，3-评论等',
  `created_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '点赞时间',
  `updated_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新点赞时间',
  `value` int NULL DEFAULT 0 COMMENT '是否点赞，1-点赞，0-取消点赞',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `like_records_target_id_IDX`(`target_id`, `user_id`, `type`) USING BTREE,
  INDEX `like_records_user_id_IDX`(`user_id`, `target_id`, `type`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '点赞表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of like
-- ----------------------------

-- ----------------------------
-- Table structure for menu
-- ----------------------------
DROP TABLE IF EXISTS `menu`;
CREATE TABLE `menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `parent_id` bigint NOT NULL DEFAULT 0 COMMENT '上级资源ID',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '路由路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '组件路径',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '菜单名称',
  `sort` int NULL DEFAULT 0 COMMENT '排序',
  `icon` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '资源图标',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '类型 menu、button',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '重定向地址',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '跳转地址',
  `hidden` tinyint(1) NULL DEFAULT 0 COMMENT '是否隐藏',
  `perm` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限标识',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 323 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '系统管理-权限资源表 ' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of menu
-- ----------------------------
INSERT INTO `menu` VALUES (1, 0, '/system', 'Layout', '系统管理', 5, 'el-icon-setting', 'CATALOG', '2019-03-28 18:51:08', '2021-12-17 15:26:06', '/system/user', 'system', 1, NULL);
INSERT INTO `menu` VALUES (2, 1, 'role', '/system/role', '角色管理', 2, 'el-icon-Avatar', 'MENU', '2019-03-30 14:00:03', '2021-11-16 15:40:42', '', 'role', 1, NULL);
INSERT INTO `menu` VALUES (3, 2, NULL, NULL, '列表', 0, NULL, 'BUTTON', NULL, NULL, '', NULL, 0, 'system:role:list');
INSERT INTO `menu` VALUES (5, 2, NULL, NULL, '修改', 0, NULL, 'BUTTON', '2021-09-24 15:57:33', '2021-11-11 18:09:44', '', NULL, 0, 'system:role:update');
INSERT INTO `menu` VALUES (6, 2, NULL, NULL, '删除', 0, NULL, 'BUTTON', '2021-09-27 11:33:32', '2021-11-11 18:09:36', '', NULL, 0, 'system:role:delete');
INSERT INTO `menu` VALUES (7, 2, NULL, NULL, '添加', 1, NULL, 'BUTTON', '2021-11-13 21:14:07', '2024-04-03 11:08:31', NULL, NULL, 0, 'system:role:add');
INSERT INTO `menu` VALUES (8, 1, 'menu', '/system/menu', '菜单管理', 5, 'el-icon-menu', 'MENU', NULL, '2021-11-18 11:26:00', '', 'menu', 1, NULL);
INSERT INTO `menu` VALUES (9, 8, NULL, NULL, '列表', 0, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:menu:getMenuTree');
INSERT INTO `menu` VALUES (10, 8, NULL, NULL, '添加', 0, NULL, 'BUTTON', NULL, '2024-04-03 11:09:19', NULL, NULL, 0, 'system:menu:add');
INSERT INTO `menu` VALUES (11, 8, NULL, NULL, '修改', 2, NULL, 'BUTTON', '2021-11-11 16:56:34', '2021-11-11 18:10:09', NULL, '/system/menu/update', 0, 'system:menu:update');
INSERT INTO `menu` VALUES (12, 8, NULL, NULL, '获取所有的url', 6, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:menu:getMenuList');
INSERT INTO `menu` VALUES (13, 8, NULL, NULL, '删除', 0, NULL, 'BUTTON', '2021-09-27 11:45:33', '2021-11-11 18:10:03', NULL, NULL, 0, 'system:menu:delete');
INSERT INTO `menu` VALUES (14, 1, 'user', '/system/user', '用户管理', 1, 'el-icon-user', 'MENU', NULL, '2021-11-16 12:01:51', NULL, 'user', 1, NULL);
INSERT INTO `menu` VALUES (15, 14, NULL, NULL, '列表', 0, NULL, 'BUTTON', '2021-09-27 15:33:19', '2021-11-11 18:10:22', NULL, NULL, 0, 'system:user:list');
INSERT INTO `menu` VALUES (16, 14, NULL, NULL, '删除', 0, NULL, 'BUTTON', '2021-09-27 16:36:42', '2021-11-11 18:10:27', NULL, NULL, 0, 'system:user:delete');
INSERT INTO `menu` VALUES (17, 14, NULL, NULL, '添加', 0, NULL, 'BUTTON', '2021-09-27 16:36:54', '2021-11-11 18:10:30', NULL, NULL, 0, 'system:user:add');
INSERT INTO `menu` VALUES (18, 14, NULL, NULL, '修改', 0, NULL, 'BUTTON', '2021-09-27 16:59:38', '2021-11-11 18:10:34', NULL, NULL, 0, 'system:user:update');
INSERT INTO `menu` VALUES (19, 14, NULL, NULL, '详情', 0, NULL, 'BUTTON', '2021-09-27 16:59:50', '2021-11-11 18:10:37', NULL, NULL, 0, 'system:user:info');
INSERT INTO `menu` VALUES (20, 14, NULL, NULL, '获取用户权限', 0, NULL, 'BUTTON', NULL, '2021-11-11 18:10:40', NULL, NULL, 0, 'system:user:getUserMenu');
INSERT INTO `menu` VALUES (21, 14, NULL, NULL, '修改密码', 0, NULL, 'BUTTON', '2021-11-09 17:23:58', '2021-11-11 18:10:51', NULL, '/system/user/update_password', 0, 'system:user:updatePassword');
INSERT INTO `menu` VALUES (23, 14, NULL, NULL, '退出登录', 0, NULL, 'BUTTON', '2021-09-26 10:21:27', '2021-11-11 18:10:46', NULL, NULL, 0, 'system:user:logout');
INSERT INTO `menu` VALUES (25, 24, NULL, NULL, '列表', 1, '1', 'BUTTON', '2021-11-12 10:55:11', NULL, NULL, '', 0, 'system:menu:getMenuApi');
INSERT INTO `menu` VALUES (26, 0, '/articles', 'Layout', '文章管理', 1, 'dict', 'CATALOG', NULL, NULL, '/articles/index', '', 1, NULL);
INSERT INTO `menu` VALUES (27, 26, 'index', '/articles/index', '文章管理', 1, 'el-icon-Document', 'MENU', NULL, '2021-11-16 15:41:57', '', 'Articles', 1, NULL);
INSERT INTO `menu` VALUES (28, 27, NULL, NULL, '列表', 0, NULL, 'BUTTON', NULL, '2021-11-11 18:11:17', NULL, NULL, 0, 'system:article:list');
INSERT INTO `menu` VALUES (30, 27, NULL, NULL, '修改', 0, NULL, 'BUTTON', NULL, '2021-11-11 18:11:25', NULL, NULL, 0, 'system:article:update');
INSERT INTO `menu` VALUES (31, 27, NULL, NULL, '添加', 0, NULL, 'BUTTON', NULL, '2021-11-11 18:11:32', NULL, '2', 0, 'system:article:add');
INSERT INTO `menu` VALUES (32, 27, NULL, NULL, '详情', 0, NULL, 'BUTTON', NULL, '2021-11-11 18:11:35', NULL, NULL, 0, 'system:article:info');
INSERT INTO `menu` VALUES (33, 27, NULL, NULL, 'SEO', 0, NULL, 'BUTTON', '2021-10-15 10:38:19', '2021-11-11 18:11:41', NULL, NULL, 0, 'system:article:seo');
INSERT INTO `menu` VALUES (35, 26, 'tags', '/articles/tags', '标签管理', 2, 'el-icon-collection-tag', 'MENU', NULL, '2021-11-18 11:25:18', NULL, 'Tags', 1, NULL);
INSERT INTO `menu` VALUES (36, 35, NULL, NULL, '列表', 0, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:tags:list');
INSERT INTO `menu` VALUES (37, 35, NULL, NULL, '新增', 0, NULL, 'BUTTON', NULL, '2021-11-11 18:11:54', NULL, NULL, 0, 'system:tags:add');
INSERT INTO `menu` VALUES (38, 35, NULL, NULL, '详情', 0, NULL, 'BUTTON', NULL, '2021-11-11 18:11:58', NULL, NULL, 0, 'system:tags:info');
INSERT INTO `menu` VALUES (39, 35, NULL, NULL, '修改', 0, NULL, 'BUTTON', NULL, '2021-11-11 18:12:08', NULL, NULL, 0, 'system:tags:update');
INSERT INTO `menu` VALUES (40, 35, NULL, NULL, '删除', 0, NULL, 'BUTTON', '2021-11-10 17:34:38', '2021-11-11 18:12:01', NULL, '/sys/tags/remove', 0, 'system:tags:delete');
INSERT INTO `menu` VALUES (41, 0, '/site', 'Layout', '网站管理', 2, 'client', 'CATALOG', NULL, NULL, '/website/friendLink', '', 1, NULL);
INSERT INTO `menu` VALUES (47, 245, 'messages', '/message/message', '留言管理', 2, 'el-icon-message', 'MENU', NULL, '2021-11-16 15:43:46', '', '/message', 1, NULL);
INSERT INTO `menu` VALUES (48, 47, NULL, NULL, '列表', 0, NULL, 'BUTTON', '2021-09-26 11:50:33', '2021-11-11 18:12:56', NULL, NULL, 0, 'system:message:list');
INSERT INTO `menu` VALUES (51, 41, 'friendLink', '/website/friendLink', '友情链接', 3, 'el-icon-link', 'MENU', NULL, '2021-11-16 15:43:55', NULL, 'friendLink', 1, NULL);
INSERT INTO `menu` VALUES (52, 51, NULL, NULL, '列表', 0, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:friendLink:list');
INSERT INTO `menu` VALUES (53, 51, NULL, NULL, '添加', 1, NULL, 'BUTTON', '2021-11-12 16:52:26', '2024-04-03 11:02:21', NULL, NULL, 0, 'system:friendLink:add');
INSERT INTO `menu` VALUES (54, 51, NULL, NULL, '修改', 1, NULL, 'BUTTON', '2021-11-12 16:52:08', NULL, NULL, NULL, 0, 'system:friendLink:update');
INSERT INTO `menu` VALUES (55, 51, NULL, NULL, '删除', 1, NULL, 'BUTTON', '2021-11-14 12:18:00', NULL, NULL, NULL, 0, 'system:friendLink:delete');
INSERT INTO `menu` VALUES (56, 0, '/logs', 'Layout', '日志管理', 4, 'el-icon-document', 'CATALOG', NULL, '2021-12-31 14:46:11', '/logs/userLog', NULL, 1, NULL);
INSERT INTO `menu` VALUES (57, 56, 'userLog', '/logs/userLog', '用户日志', 1, 'el-icon-coordinate', 'MENU', NULL, '2021-11-17 10:02:31', NULL, 'userLogs', 1, NULL);
INSERT INTO `menu` VALUES (58, 57, NULL, NULL, '列表', 0, '', 'BUTTON', NULL, '2021-11-11 18:13:47', NULL, NULL, 0, 'system:userLog:list');
INSERT INTO `menu` VALUES (59, 56, 'adminLog', '/logs/adminLog', '操作日志', 2, 'el-icon-magic-stick', 'MENU', '2021-11-10 17:49:02', '2021-11-17 10:02:41', NULL, 'adminLog', 1, NULL);
INSERT INTO `menu` VALUES (60, 59, NULL, NULL, '列表', 0, NULL, 'BUTTON', '2021-11-10 17:49:27', '2021-11-11 18:13:54', NULL, '/zwblog/adminLog', 0, 'system:adminLog:list');
INSERT INTO `menu` VALUES (61, 56, 'exceptionLog', '/logs/exceptionLog', '异常日志', 3, 'el-icon-cpu', 'MENU', '2021-11-11 10:57:42', '2021-11-17 10:02:47', NULL, 'exceptionLog', 1, NULL);
INSERT INTO `menu` VALUES (62, 61, NULL, NULL, '列表', 0, NULL, 'BUTTON', '2021-11-11 11:05:47', '2021-11-11 18:13:59', NULL, '/sys/exceptionLog/query_log', 0, 'system:exceptionLog:list');
INSERT INTO `menu` VALUES (64, 0, '/image', '/image', '文件管理', 99, 'el-icon-picture-outline', 'MENU', '2021-11-12 09:31:24', '2021-11-16 15:47:05', NULL, '/image', 0, NULL);
INSERT INTO `menu` VALUES (65, 64, NULL, NULL, '删除', 0, NULL, 'BUTTON', '2021-09-27 11:53:16', '2021-11-11 18:10:55', NULL, NULL, 0, 'system:file:delBatchFile');
INSERT INTO `menu` VALUES (164, 0, '/monitor', 'Layout', '监控中心', 6, 'el-icon-monitor', 'CATALOG', '2021-11-16 11:48:04', '2024-05-10 11:34:33', '/monitor/job', 'listener', 1, NULL);
INSERT INTO `menu` VALUES (165, 164, 'server', '/monitor/server', '服务监控', 1, 'monitor', 'MENU', '2021-11-16 11:49:16', '2021-12-10 08:47:17', NULL, 'server', 1, NULL);
INSERT INTO `menu` VALUES (166, 165, NULL, NULL, '查看', 1, NULL, 'BUTTON', '2021-11-16 11:50:03', NULL, NULL, NULL, 0, 'system:homesystemInfo');
INSERT INTO `menu` VALUES (169, 1, 'dict', '/system/dict', '字典管理', 4, 'el-icon-DocumentChecked', 'MENU', '2021-11-25 17:37:43', '2021-12-10 15:28:52', NULL, 'dict', 1, NULL);
INSERT INTO `menu` VALUES (170, 169, NULL, NULL, '列表', 1, NULL, 'BUTTON', '2021-11-25 17:38:04', NULL, NULL, NULL, 0, 'system:dict:list');
INSERT INTO `menu` VALUES (173, 169, NULL, NULL, '添加', 1, NULL, 'BUTTON', '2021-11-26 08:57:12', NULL, NULL, NULL, 0, 'system:dict:add');
INSERT INTO `menu` VALUES (174, 169, NULL, NULL, '修改', 2, NULL, 'BUTTON', '2021-11-26 08:57:29', NULL, NULL, NULL, 0, 'system:dict:update');
INSERT INTO `menu` VALUES (176, 169, NULL, NULL, '删除', 3, NULL, 'BUTTON', '2021-11-26 11:22:21', NULL, NULL, NULL, 0, 'system:dict:delete');
INSERT INTO `menu` VALUES (182, 1, 'systemconfig', '/system/systemConfig', '系统配置', 3, 'el-icon-setting', 'MENU', '2021-11-26 15:06:11', '2021-11-27 12:53:08', NULL, 'systemconfig', 1, NULL);
INSERT INTO `menu` VALUES (183, 182, NULL, NULL, '查询', 1, NULL, 'BUTTON', '2021-11-26 15:06:39', '2021-11-26 15:45:36', NULL, NULL, 0, 'system:config:getConfig');
INSERT INTO `menu` VALUES (184, 182, NULL, NULL, '修改', 2, NULL, 'BUTTON', '2021-11-26 15:55:47', NULL, NULL, NULL, 0, 'system:config:update');
INSERT INTO `menu` VALUES (186, 41, 'webConfig', '/website/webConfig', '网站配置', 3, 'el-icon-setting', 'MENU', '2021-11-27 13:48:02', NULL, NULL, 'webConfig', 1, NULL);
INSERT INTO `menu` VALUES (187, 186, NULL, NULL, '查询', 1, NULL, 'BUTTON', '2021-11-27 13:48:33', NULL, NULL, NULL, 0, 'system:webConfig:list');
INSERT INTO `menu` VALUES (188, 186, NULL, NULL, '修改', 1, NULL, 'BUTTON', '2021-11-27 14:12:42', NULL, NULL, NULL, 0, 'system:webConfig:update');
INSERT INTO `menu` VALUES (191, 164, 'job', '/monitor/job', '定时任务', 2, 'el-icon-coordinate', 'MENU', '2021-12-10 08:46:08', '2024-05-10 11:34:08', NULL, 'quartz', 1, NULL);
INSERT INTO `menu` VALUES (192, 191, NULL, NULL, '列表', 1, NULL, 'BUTTON', '2021-12-10 08:47:52', NULL, NULL, NULL, 0, 'system:job:list');
INSERT INTO `menu` VALUES (193, 191, NULL, NULL, '添加', 2, NULL, 'BUTTON', '2021-12-10 08:48:13', NULL, NULL, NULL, 0, 'system:job:add');
INSERT INTO `menu` VALUES (194, 191, NULL, NULL, '修改', 3, NULL, 'BUTTON', '2021-12-10 08:48:27', NULL, NULL, NULL, 0, 'system:job:update');
INSERT INTO `menu` VALUES (195, 191, NULL, NULL, '删除', 4, NULL, 'BUTTON', '2021-12-10 08:48:45', NULL, NULL, NULL, 0, 'system:job:delete');
INSERT INTO `menu` VALUES (196, 191, NULL, NULL, '立即执行', 5, NULL, 'BUTTON', '2021-12-10 08:52:15', NULL, NULL, NULL, 0, 'system:job:run');
INSERT INTO `menu` VALUES (197, 191, NULL, NULL, '修改状态', 6, NULL, 'BUTTON', '2021-12-10 08:52:42', NULL, NULL, NULL, 0, 'system:job:change');
INSERT INTO `menu` VALUES (198, 191, NULL, NULL, '详情', 7, NULL, 'BUTTON', '2021-12-10 10:09:27', NULL, NULL, NULL, 0, 'system:job:info');
INSERT INTO `menu` VALUES (199, 164, 'jobLog', '/monitor/jobLog', '任务日志', 3, 'el-icon-help', 'MENU', '2021-12-10 11:45:00', '2024-05-10 11:34:12', NULL, 'jobLog', 0, NULL);
INSERT INTO `menu` VALUES (200, 199, NULL, NULL, '列表', 1, NULL, 'BUTTON', '2021-12-10 11:45:23', NULL, NULL, NULL, 0, 'system:jobLog:list');
INSERT INTO `menu` VALUES (201, 199, NULL, NULL, '批量删除', 2, NULL, 'BUTTON', '2021-12-10 13:50:17', '2024-04-03 11:13:50', NULL, NULL, 0, 'system:jobLog:delete');
INSERT INTO `menu` VALUES (202, 199, NULL, NULL, '清空', 3, NULL, 'BUTTON', '2021-12-10 13:50:28', NULL, NULL, NULL, 0, 'system:jobLog:clean');
INSERT INTO `menu` VALUES (215, 27, NULL, NULL, '爬虫', 6, NULL, 'BUTTON', '2021-12-24 09:00:15', NULL, NULL, NULL, 0, 'system:article:reptile');
INSERT INTO `menu` VALUES (216, 35, NULL, NULL, '标签置顶', 5, NULL, 'BUTTON', '2021-12-24 09:00:36', NULL, NULL, NULL, 0, 'system:tags:top');
INSERT INTO `menu` VALUES (223, 26, 'category', '/articles/category', '分类管理', 3, 'el-icon-files', 'MENU', '2021-12-29 10:05:12', '2021-12-29 10:08:05', NULL, '/category', 1, NULL);
INSERT INTO `menu` VALUES (224, 223, NULL, NULL, '列表', 1, NULL, 'BUTTON', '2021-12-29 10:05:38', NULL, NULL, NULL, 0, 'system:category:list');
INSERT INTO `menu` VALUES (225, 223, NULL, NULL, '详情', 2, NULL, 'BUTTON', '2021-12-29 10:05:58', NULL, NULL, NULL, 0, 'system:category:info');
INSERT INTO `menu` VALUES (226, 223, NULL, NULL, '新增', 3, NULL, 'BUTTON', '2021-12-29 10:06:18', NULL, NULL, NULL, 0, 'system:category:add');
INSERT INTO `menu` VALUES (227, 223, NULL, NULL, '修改', 4, NULL, 'BUTTON', '2021-12-29 10:06:33', NULL, NULL, NULL, 0, 'system:category:update');
INSERT INTO `menu` VALUES (229, 223, NULL, NULL, '置顶', 6, NULL, 'BUTTON', '2021-12-29 10:07:06', NULL, NULL, NULL, 0, 'system:category:top');
INSERT INTO `menu` VALUES (230, 223, NULL, NULL, '删除', 7, NULL, 'BUTTON', '2021-12-29 10:27:55', NULL, NULL, NULL, 0, 'system:category:delete');
INSERT INTO `menu` VALUES (245, 0, '/news', 'Layout', '消息管理', 3, 'el-icon-bell', 'CATALOG', '2021-12-31 14:21:26', '2024-05-10 11:32:24', '/message/message', '/new', 1, NULL);
INSERT INTO `menu` VALUES (247, 47, NULL, NULL, '批量删除', 4, NULL, 'BUTTON', '2021-12-31 14:35:47', '2024-04-03 11:05:12', NULL, NULL, 0, 'system:message:delete');
INSERT INTO `menu` VALUES (250, 57, NULL, NULL, '删除', 2, NULL, 'BUTTON', '2022-01-06 15:41:01', NULL, NULL, NULL, 0, 'system:userLog:delete');
INSERT INTO `menu` VALUES (251, 59, NULL, NULL, '删除', 2, NULL, 'BUTTON', '2022-01-06 15:41:27', NULL, NULL, NULL, 0, 'system:adminLog:delete');
INSERT INTO `menu` VALUES (252, 61, NULL, NULL, '删除', 2, NULL, 'BUTTON', '2022-01-06 15:41:49', NULL, NULL, NULL, 0, 'system:exceptionLog:delete');
INSERT INTO `menu` VALUES (253, 27, NULL, NULL, '批量删除', 6, NULL, 'BUTTON', '2022-01-06 18:00:24', '2024-04-03 09:35:49', NULL, NULL, 0, 'system:article:delete');
INSERT INTO `menu` VALUES (254, 51, NULL, NULL, '置顶', 4, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:friendLink:top');
INSERT INTO `menu` VALUES (256, 245, 'feedbacks', '/message/feedback', '反馈管理', 2, 'el-icon-Soccer', 'MENU', NULL, '2024-03-29 13:48:47', NULL, '/feedback', 1, NULL);
INSERT INTO `menu` VALUES (257, 256, NULL, NULL, '列表', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:feedback:list');
INSERT INTO `menu` VALUES (258, 256, NULL, NULL, '批量删除', 2, NULL, 'BUTTON', NULL, '2024-04-03 11:04:19', NULL, NULL, 0, 'system:feedback:delete');
INSERT INTO `menu` VALUES (260, 64, NULL, NULL, '上传图片', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:file:upload');
INSERT INTO `menu` VALUES (262, 27, NULL, NULL, '发布或下架文章', 4, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:article:pubOrShelf');
INSERT INTO `menu` VALUES (263, 164, 'onlineUser', '/monitor/onlineUser', '在线用户', 3, 'el-icon-user', 'MENU', NULL, NULL, NULL, 'online', 1, NULL);
INSERT INTO `menu` VALUES (264, 263, NULL, NULL, '踢人下线', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:user:kick');
INSERT INTO `menu` VALUES (265, 164, 'druids', '/monitor/druid', 'druid监控', 4, 'el-icon-help', 'MENU', NULL, NULL, NULL, 'druid', 1, NULL);
INSERT INTO `menu` VALUES (266, 245, 'comment', '/message/comment', '评论管理', 1, 'el-icon-chat-dot-round', 'MENU', NULL, NULL, NULL, 'comments', 1, NULL);
INSERT INTO `menu` VALUES (267, 266, NULL, NULL, '评论列表', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:comment:list');
INSERT INTO `menu` VALUES (268, 266, NULL, NULL, '批量删除评论', 2, NULL, 'BUTTON', NULL, '2024-04-03 11:04:11', NULL, NULL, 0, 'system:comment:delete');
INSERT INTO `menu` VALUES (269, 164, 'cache', '/monitor/cache', '缓存监控', 5, 'el-icon-hot-water', 'MENU', NULL, '2024-04-03 08:55:11', NULL, 'caches', 1, NULL);
INSERT INTO `menu` VALUES (270, 269, NULL, NULL, '缓存数据列表', 1, NULL, 'BUTTON', NULL, '2024-04-10 17:12:43', NULL, NULL, 0, 'system:cache:list');
INSERT INTO `menu` VALUES (271, 27, NULL, NULL, '置顶文章', 5, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:article:top');
INSERT INTO `menu` VALUES (286, 26, 'topic', '/articles/topic', '话题管理', 5, 'el-icon-Management', 'MENU', NULL, NULL, NULL, 'say', 1, NULL);
INSERT INTO `menu` VALUES (287, 286, NULL, NULL, '删除', 1, NULL, 'BUTTON', NULL, '2023-09-22 15:57:58', NULL, NULL, 0, 'system:say:delete');
INSERT INTO `menu` VALUES (288, 286, NULL, NULL, '添加', 2, NULL, 'BUTTON', NULL, '2024-04-03 11:00:55', NULL, NULL, 0, 'system:say:add');
INSERT INTO `menu` VALUES (289, 256, NULL, NULL, '修改', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:feedback:update');
INSERT INTO `menu` VALUES (290, 286, NULL, NULL, '修改', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:say:update');
INSERT INTO `menu` VALUES (291, 286, NULL, NULL, '列表', 2, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:say:list');
INSERT INTO `menu` VALUES (298, 297, NULL, NULL, '修改', 1, NULL, 'BUTTON', NULL, '2023-10-25 09:13:59', NULL, NULL, 0, 'system:sponsor:update');
INSERT INTO `menu` VALUES (300, 1, 'generate', '/system/generate', '代码生成', 6, 'project', 'MENU', NULL, '2024-04-02 15:23:53', NULL, 'generate', 1, NULL);
INSERT INTO `menu` VALUES (301, 300, NULL, NULL, '下载', 2, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:generate:download');
INSERT INTO `menu` VALUES (302, 265, NULL, NULL, '列表', 2, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:druid:list');
INSERT INTO `menu` VALUES (313, 2, '', NULL, '分配权限', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:role:assign');
INSERT INTO `menu` VALUES (314, 300, '', NULL, '预览', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:generate:preview');
INSERT INTO `menu` VALUES (315, 269, '', NULL, '缓存详情', 1, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:cache:getCacheInfo');
INSERT INTO `menu` VALUES (316, 269, '', NULL, '根据键获取值', 2, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:cache:getValue');
INSERT INTO `menu` VALUES (317, 269, '', NULL, '删除缓存', 3, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:cache:delete');
INSERT INTO `menu` VALUES (319, 41, 'resource', '/website/resource', '资源管理', 3, 'api', 'MENU', NULL, '2024-04-11 10:40:36', NULL, NULL, 0, NULL);
INSERT INTO `menu` VALUES (320, 319, '', NULL, '列表', 1, NULL, 'BUTTON', NULL, '2024-04-11 10:22:49', NULL, NULL, 0, 'system:resource:list');
INSERT INTO `menu` VALUES (321, 319, '', NULL, '删除', 2, NULL, 'BUTTON', NULL, NULL, NULL, NULL, 0, 'system:resource:delete');

-- ----------------------------
-- Table structure for report
-- ----------------------------
DROP TABLE IF EXISTS `report`;
CREATE TABLE `report`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '举报记录的唯一标识符',
  `entity_type` enum('article','comment') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '被举报的实体类型（文章或评论）',
  `entity_id` bigint NOT NULL COMMENT '被举报的实体ID',
  `user_id` int NOT NULL COMMENT '举报用户的ID',
  `reason` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '举报原因的详细描述',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '举报记录的创建时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '举报表，用于存储用户对文章或评论的举报记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of report
-- ----------------------------

-- ----------------------------
-- Table structure for role
-- ----------------------------
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码',
  `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色介绍',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1023 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role
-- ----------------------------
INSERT INTO `role` VALUES (1001, 'root', 'admin', '管理员用户');
INSERT INTO `role` VALUES (1002, 'admin', 'admin', '普通管理员');
INSERT INTO `role` VALUES (1014, '1', '1', '1');
INSERT INTO `role` VALUES (1015, '2', '2', '2');
INSERT INTO `role` VALUES (1016, '3', '3', '3');
INSERT INTO `role` VALUES (1017, '4', '4', '4');
INSERT INTO `role` VALUES (1018, '5', '5', '5');
INSERT INTO `role` VALUES (1019, '6', '6', '6');
INSERT INTO `role` VALUES (1020, '7', '7', '7');
INSERT INTO `role` VALUES (1021, '8', '8', '8');
INSERT INTO `role` VALUES (1022, '9', '9', '9');

-- ----------------------------
-- Table structure for role_menu
-- ----------------------------
DROP TABLE IF EXISTS `role_menu`;
CREATE TABLE `role_menu`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色菜单关系ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `menu_id` bigint NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `role_id`(`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色与菜单关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of role_menu
-- ----------------------------

-- ----------------------------
-- Table structure for topic
-- ----------------------------
DROP TABLE IF EXISTS `topic`;
CREATE TABLE `topic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '话题ID',
  `user_id` bigint NOT NULL COMMENT '发布话题的用户ID',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '话题标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '话题内容',
  `images` json NULL COMMENT '话题图片URL数组',
  `category_id` bigint NULL DEFAULT NULL COMMENT '话题分类ID',
  `like_count` int UNSIGNED NULL DEFAULT 0 COMMENT '点赞数',
  `comment_count` int UNSIGNED NULL DEFAULT 0 COMMENT '评论数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of topic
-- ----------------------------

-- ----------------------------
-- Table structure for topic_category
-- ----------------------------
DROP TABLE IF EXISTS `topic_category`;
CREATE TABLE `topic_category`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类描述',
  `sort` int NULL DEFAULT 0 COMMENT '排序序号，值越小越靠前',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '话题分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of topic_category
-- ----------------------------

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `password` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '邮箱',
  `nickname` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `status` int NULL DEFAULT 0 COMMENT '用户状态（0：正常，1：封禁）',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个人介绍',
  `create_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `email`(`email`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10015 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1001, 'admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '123456@qq.com', '肥田', 'http://localhost:5000/your-bucket-name/2ba3dd13-5edc-4fa2-aa45-c57f65bfd7a4_1111112313211.png', 0, NULL, '2024-08-28 09:14:22', '2024-12-19 16:58:03');
INSERT INTO `user` VALUES (1002, 'user', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '12345@qq.com', '普通用户小明', '313@qq.com', 0, NULL, '2024-09-02 10:12:33', '2024-09-12 09:33:39');
INSERT INTO `user` VALUES (10014, NULL, '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '123@qq.com', '32131321323', NULL, 0, NULL, '2024-12-25 18:49:54', '2024-12-25 19:57:48');

-- ----------------------------
-- Table structure for user_role
-- ----------------------------
DROP TABLE IF EXISTS `user_role`;
CREATE TABLE `user_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户角色关系ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1003 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户角色关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_role
-- ----------------------------
INSERT INTO `user_role` VALUES (1001, 1001, 1001);
INSERT INTO `user_role` VALUES (1002, 1002, 1002);

SET FOREIGN_KEY_CHECKS = 1;
