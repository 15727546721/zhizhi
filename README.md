# 知知社区 - 后端服务

一个现代化的开源社区系统后端，基于 Spring Boot 构建。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 2.7.0 | 核心框架 |
| MyBatis-Plus | 3.5.x | ORM框架 |
| MySQL | 8.0+ | 数据库 |
| Redis | 6.0+ | 缓存 |
| Elasticsearch | 7.x | 搜索引擎（可选） |
| MinIO | - | 文件存储（可选） |
| Sa-Token | 1.38.0 | 权限认证 |
| Knife4j | 4.3.0 | API文档 |

## 快速开始

### 1. 环境要求

- JDK 8+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 2. 初始化数据库

```bash
# 创建表结构
mysql -u root -p < src/main/resources/sql/01_schema.sql

# 导入初始数据
mysql -u root -p zhizhi < src/main/resources/sql/02_data.sql
```

### 3. 启动依赖服务

```bash
# 使用 Docker Compose 启动 MySQL、Redis
cd docs/部署/environment
docker-compose up -d
```

### 4. 修改配置

编辑 `src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/zhizhi
    username: root
    password: your_password
```

### 5. 启动应用

```bash
mvn spring-boot:run
```

### 6. 访问

- **API文档**: http://localhost:8091/doc.html
- **健康检查**: http://localhost:8091/actuator/health

## 项目结构

```
cn.xu/
├── controller/     # 接口层（admin/web）
├── service/        # 业务层
├── repository/     # 数据访问层
├── model/          # 数据模型（entity/dto/vo）
├── cache/          # 缓存服务
├── config/         # 配置类
├── event/          # 事件处理
├── task/           # 定时任务
├── support/        # 基础支撑
└── integration/    # 外部集成
```

## 核心功能

| 模块 | 说明 |
|------|------|
| 帖子 | 发布、编辑、删除、热度排行 |
| 标签 | 标签管理、热门标签 |
| 用户 | 注册登录、个人资料、关注系统 |
| 评论 | 多级回复、点赞 |
| 通知 | 互动通知（点赞、评论、关注等） |
| 私信 | 用户私信、会话管理 |
| 搜索 | 全文搜索（ES/MySQL双策略） |
| 收藏 | 收藏夹管理 |

## 文档

详细文档请查看 [docs/README.md](docs/README.md)

## 许可证

MIT License