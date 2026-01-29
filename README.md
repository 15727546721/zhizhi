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