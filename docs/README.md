# 知知社区后端文档

## 📖 文档索引

### 入门
- [快速开始](./快速开始.md) - 环境配置与项目启动
- [系统架构](./系统架构.md) - 项目整体架构说明

### 功能模块
- [聚合搜索](./功能模块/聚合搜索功能.md) - 帖子/用户/标签并行搜索（Java8异步编排）
- [全文搜索](./功能模块/搜索功能使用指南.md) - ES全文搜索与筛选
- [私信系统](./功能模块/私信系统完整说明文档.md) - 用户私信功能
- [排行榜](./功能模块/排行榜功能方案.md) - 帖子和用户排行
- [用户设置](./功能模块/用户设置功能实现方案.md) - 用户偏好设置
- [个人主页](./功能模块/个人主页API设计.md) - 个人主页API
- [权限系统](./功能模块/后台管理权限系统.md) - 后台管理权限控制

### 开发规范
- [代码质量规范](./开发规范/代码质量规范.md)
- [命名规范](./开发规范/命名规范.md)
- [项目结构说明](./开发规范/项目结构说明.md)

### 配置与部署
- [部署说明](./部署/部署说明.md) - 环境部署指南
- [邮件配置](./配置/邮件配置.md) - SMTP邮件服务配置
- [Docker环境](./部署/environment/) - Docker Compose配置

### API文档
启动项目后访问: http://localhost:8091/doc.html

---

## 🏗️ 技术栈

| 技术 | 版本 | 说明 |
|-----|------|------|
| Spring Boot | 2.7.0 | 核心框架 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| Redis | 7.x | 缓存 |
| MySQL | 8.0+ | 数据库 |
| Elasticsearch | 7.x | 搜索引擎 |
| MinIO | - | 文件存储 |
| Sa-Token | 1.38.0 | 权限认证 |
| Knife4j | 4.3.0 | API文档 |

## 📁 项目结构

```
cn.xu/
├── controller/          # 接口层
│   ├── admin/          # 后台管理API
│   └── web/            # 前台API
├── service/            # 业务层
├── repository/         # 数据访问层
│   ├── mapper/         # MyBatis Mapper
│   ├── impl/           # 仓储实现
│   └── read/           # ES读模型
├── model/              # 数据模型
│   ├── entity/         # 实体类
│   ├── dto/            # 传输对象
│   └── vo/             # 视图对象
├── cache/              # 缓存服务
├── config/             # 配置类
├── event/              # 事件处理
├── task/               # 定时任务
├── support/            # 基础支撑
└── integration/        # 外部集成
```

## 🚀 快速启动

```bash
# 1. 初始化数据库
mysql -u root -p < ../src/main/resources/sql/01_schema.sql
mysql -u root -p zhizhi < ../src/main/resources/sql/02_data.sql

# 2. 启动依赖服务（MySQL、Redis）
cd 部署/environment
docker-compose up -d

# 3. 启动应用
mvn spring-boot:run

# 4. 访问API文档
http://localhost:8091/doc.html
```

## 📝 核心模块

| 模块 | 说明 |
|------|------|
| Post | 帖子发布、编辑、删除、热度计算 |
| Tag | 标签管理、热门标签 |
| User | 用户认证、个人资料、关注系统 |
| Comment | 评论、回复、点赞 |
| Notification | 互动通知（点赞、评论、关注等） |
| PrivateMessage | 私信系统 |
| Search | 全文搜索（ES） |
| Favorite | 收藏夹管理 |
