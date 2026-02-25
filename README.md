# 知之社区（后端 + 前台 + 管理后台）

基于 Spring Boot + Java17 的后端服务，配套 Vue3 前台与管理后台，提供完整的社区功能与 RESTful API 接口。

## 项目结构

```
zhizhi/
├── src/                         # 后端源代码目录
├── docs/                        # 项目文档（架构、功能设计、部署说明等）
├── mounted/                     # 中间件配置（MySQL、Redis、Nginx、Elasticsearch、MinIO 等）
├── zhizhi-front-vue3-master/    # 前台站点（Vue3 + Vite）
├── zhizhi-admin-vue-master/     # 管理后台（Vue3 + Vite）
├── Dockerfile                   # 后端 Docker 构建文件
├── docker-compose.yml           # 一键启动（后端 + 前台 + 管理后台 + 中间件）
├── docker-compose.middleware.yml# 仅中间件（本地开发用）
├── docker-compose.prod.yml      # 生产环境 Docker 编排文件
├── PRODUCTION_DEPLOY.md         # 生产部署说明
├── deploy.sh                    # 启动脚本（可选）
└── pom.xml                      # Maven 项目配置文件
```

## 一键启动（推荐方式）

在 `zhizhi` 目录下执行：

```bash
docker-compose up -d
```

会自动启动：

- 后端服务：`http://localhost:8091`
- 前台站点：`http://localhost:8080`
- 管理后台：`http://localhost:8081`
- MySQL / Redis / Elasticsearch / MinIO 等中间件

## 快速开始

### 仅启动中间件（后端在 IDE 中运行）

```bash
docker-compose -f docker-compose.middleware.yml up -d
```

然后在本机通过 IDE 运行 Spring Boot 项目即可。

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

- Java 17
- Spring Boot 3.2.0
- MySQL 8.0
- Redis 7.2
- Elasticsearch 7.17
- MinIO
- Sa-Token 权限认证框架
- MyBatis
- Maven

## 项目模块

- 用户管理
- RBAC权限
- 帖子管理
- 评论管理
- 标签管理
- 点赞收藏
- 专栏管理
- 消息通知
- 私信通知
- 权限管理
- 文件存储
- 搜索功能

## 运行截图
![输入图片说明](docs/%E5%8A%9F%E8%83%BD%E6%A8%A1%E5%9D%97/5080dd032c3ee72cecd0e2f3bd7d3d78.png)
![输入图片说明](docs/%E9%85%8D%E7%BD%AE/80f1cf013d76b017a7a9ca5c4c30dbb6.png)
![输入图片说明](docs/%E5%8A%9F%E8%83%BD%E6%A8%A1%E5%9D%97/2b8452484aad51434d04d30551311d90.png)
![输入图片说明](docs/%E5%8A%9F%E8%83%BD%E6%A8%A1%E5%9D%97/813ef2b9904e1e567b61fa66fa59926d.png)
![输入图片说明](docs/%E9%85%8D%E7%BD%AE/09cbe40b4465e03b18a31696dc77492b.png)
![输入图片说明](docs/%E5%8A%9F%E8%83%BD%E6%A8%A1%E5%9D%97/5f488af3a6df55cad37ee0021fd15541.png)

## 贡献指南

欢迎提交 Issue 和 Pull Request 来改进项目。