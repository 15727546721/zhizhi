# Docker 部署

## 快速启动

```bash
# 1. 启动后端（含 MySQL/Redis/ES/MinIO）
cd zhizhi-backend
docker-compose up -d --build

# 2. 启动前端
cd ../zhizhi-front-vue3
docker-compose up -d --build

# 3. 启动管理后台
cd ../zhizhi-admin-vue
docker-compose up -d --build
```

## 访问地址

| 服务 | 地址 |
|------|------|
| 前端 | http://localhost:8080 |
| 管理后台 | http://localhost:8081 |
| 后端API | http://localhost:8091 |
| API文档 | http://localhost:8091/doc.html |
| MinIO控制台 | http://localhost:9001 |

默认账号：MySQL `root/zhizhi123`，MinIO `minioadmin/minioadmin`

## 生产环境部署

对于生产环境部署，请参考 [PRODUCTION_DEPLOY.md](PRODUCTION_DEPLOY.md) 文件，其中包含了详细的部署指南和安全建议。

## 常用命令

```bash
docker ps                          # 查看状态
docker logs -f zhizhi-backend      # 查看日志
docker restart zhizhi-backend      # 重启服务
docker-compose down                # 停止全部
docker-compose down -v             # 清理数据（慎用）
```