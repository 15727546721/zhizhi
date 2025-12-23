# 知之社区后端服务

基于 Spring Boot 的后端服务，提供 RESTful API 接口。

## 项目结构

```
zhizhi-backend/
├── src/                    # 源代码目录
├── docs/                   # 文档目录
├── mounted/                # 配置文件目录（MySQL、Redis、Nginx等）
├── target/                 # 构建输出目录
├── Dockerfile              # Docker 构建文件
├── docker-compose.yml      # 开发环境 Docker 编排文件
├── docker-compose.prod.yml # 生产环境 Docker 编排文件
└── pom.xml                 # Maven 项目配置文件
```

## 快速开始

### 开发环境启动

```bash
# 启动所有服务（MySQL、Redis、MinIO、后端服务）
docker-compose up -d

# 查看日志
docker logs -f zhizhi-backend
```

### 生产环境部署

请参考 [PRODUCTION_DEPLOY.md](PRODUCTION_DEPLOY.md) 文件获取详细的生产环境部署指南。

## 配置文件说明

本项目使用 `mounted` 目录来统一管理所有服务的配置文件：

- `mounted/mysql/custom.cnf` - MySQL 配置文件
- `mounted/redis/redis.conf` - Redis 配置文件

## API 文档

启动服务后，可通过以下地址访问 API 文档：

- Knife4j UI: http://localhost:8091/doc.html

## 数据库初始化

首次启动时，系统会自动执行 `src/main/resources/sql` 目录下的 SQL 脚本初始化数据库。

## 技术栈

- Java 8
- Spring Boot 2.7.x
- MySQL 8.0
- Redis 7.2
- Elasticsearch 7.17
- MinIO
- Sa-Token 权限认证框架
- MyBatis Plus
- Maven

## 项目模块

- 用户管理
- 帖子管理
- 评论管理
- 标签管理
- 点赞收藏
- 消息通知
- 权限管理
- 文件存储
- 搜索功能

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目。