# 生产环境部署指南

本文档介绍了如何在生产服务器上部署知之社区系统。

## 部署前准备

### 1. 服务器要求
- 操作系统：Linux (推荐 Ubuntu 20.04 LTS 或 CentOS 8)
- 内存：至少 4GB RAM
- 存储：至少 20GB 可用空间
- Docker 和 Docker Compose 已安装

### 2. 创建数据目录
在部署之前，需要在服务器上创建用于数据持久化的目录：

```bash
# 创建项目目录
sudo mkdir -p /opt/zhizhi

# 创建数据存储目录
sudo mkdir -p /var/lib/zhizhi/mysql
sudo mkdir -p /var/lib/zhizhi/redis
sudo mkdir -p /var/lib/zhizhi/minio
sudo mkdir -p /var/lib/zhizhi/elasticsearch/data

# 设置正确的权限
sudo chown -R 1001:1001 /var/lib/zhizhi/mysql
sudo chown -R 1001:1001 /var/lib/zhizhi/redis
sudo chown -R 1001:1001 /var/lib/zhizhi/minio
# Elasticsearch数据目录需要特殊处理
sudo chown -R 1000:1000 /var/lib/zhizhi/elasticsearch
```

注意：MySQL 官方镜像使用 `1001:1001` 用户运行，MinIO 和 Redis 通常使用 `root` 用户运行，Elasticsearch 使用 `1000:1000` 用户运行。

### 3. 配置文件调整
确保 `mounted/redis/redis.conf` 文件适合生产环境使用。您可以参考以下配置：

```conf
# Redis 生产环境配置示例
bind 0.0.0.0
port 6379
timeout 0
tcp-keepalive 300
loglevel notice
logfile ""
databases 16
save 900 1
save 300 10
save 60 10000
stop-writes-on-bgsave-error yes
rdbcompression yes
rdbchecksum yes
dbfilename dump.rdb
dir /data
slave-serve-stale-data yes
slave-read-only yes
repl-diskless-sync no
repl-disable-tcp-nodelay no
slave-priority 100
maxmemory 256mb
maxmemory-policy allkeys-lru
appendonly no
appendfilename "appendonly.aof"
appendfsync everysec
no-appendfsync-on-rewrite no
auto-aof-rewrite-percentage 100
auto-aof-rewrite-min-size 64mb
aof-load-truncated yes
lua-time-limit 5000
slowlog-log-slower-than 10000
slowlog-max-len 128
latency-monitor-threshold 0
notify-keyspace-events ""
hash-max-ziplist-entries 512
hash-max-ziplist-value 64
list-max-ziplist-size -2
list-compress-depth 0
set-max-intset-entries 512
zset-max-ziplist-entries 128
zset-max-ziplist-value 64
hll-sparse-max-bytes 3000
activerehashing yes
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit slave 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60
hz 10
aof-rewrite-incremental-fsync yes
```

## 部署步骤

### 1. 克隆代码库
```bash
# 进入项目目录
cd /opt/zhizhi

# 克隆后端代码
git clone <repository-url> zhizhi-backend

# 如果需要，克隆前端代码
cd /opt/zhizhi
git clone <frontend-repository-url> zhizhi-front-vue3
git clone <admin-repository-url> zhizhi-admin-vue
```

### 2. 构建并启动后端服务
```bash
# 进入后端项目目录
cd /opt/zhizhi/zhizhi-backend

# 使用生产环境配置文件启动后端服务
docker-compose -f docker-compose.prod.yml up -d --build

# 等待服务启动完成（约1-2分钟）
sleep 120

# 检查容器状态
docker ps | grep zhizhi

# 查看后端服务日志确认启动成功
docker logs zhizhi-backend
```

### 3. 构建并启动前端服务
```bash
# 启动用户前端
cd /opt/zhizhi/zhizhi-front-vue3
docker-compose up -d --build

# 启动管理后台
cd /opt/zhizhi/zhizhi-admin-vue
docker-compose up -d --build

# 检查前端容器状态
docker ps | grep zhizhi-front
docker ps | grep zhizhi-admin
```

### 4. 验证部署
```bash
# 检查所有容器状态
docker ps

# 查看关键服务日志
docker logs zhizhi-backend
docker logs zhizhi-elasticsearch
docker logs zhizhi-mysql

# 测试API接口
curl -I http://localhost:8091/api/system/home/getDashboardTopStatistics

# 测试前端访问
curl -I http://localhost:8080
curl -I http://localhost:8081
```

## 安全建议

### 1. 修改默认密码
在生产环境中，强烈建议修改默认密码：
- MySQL root 密码
- MinIO 访问密钥和秘密密钥

可以在 `docker-compose.prod.yml` 中修改环境变量：
```yaml
environment:
  - MYSQL_ROOT_PASSWORD=your_secure_mysql_password
  - MINIO_ROOT_USER=your_secure_minio_user
  - MINIO_ROOT_PASSWORD=your_secure_minio_password
```

### 2. 网络安全
- 不要在生产环境中暴露不必要的端口
- 考虑使用防火墙限制对数据库端口的访问
- 使用 HTTPS 保护前端和后端之间的通信

### 3. 定期备份
建立定期备份策略：
```bash
# 备份 MySQL 数据
docker exec zhizhi-mysql mysqldump -u root -p zhizhi > backup.sql

# 备份 Redis 数据
docker exec zhizhi-redis redis-cli SAVE
```

### 4. 安全配置（可选但推荐）
```bash
# 修改默认密码（在docker-compose.prod.yml中）
# 编辑配置文件
vim /opt/zhizhi/zhizhi-backend/docker-compose.prod.yml

# 修改以下环境变量：
# MYSQL_ROOT_PASSWORD: your_secure_mysql_password
# MINIO_ROOT_USER: your_secure_minio_user
# MINIO_ROOT_PASSWORD: your_secure_minio_password

# 重启服务以应用更改
cd /opt/zhizhi/zhizhi-backend
docker-compose -f docker-compose.prod.yml down
docker-compose -f docker-compose.prod.yml up -d
```

## 监控和维护

### 1. 日志管理
配置日志轮转以防止磁盘空间耗尽：
```bash
# 在服务器上配置 logrotate
sudo vim /etc/logrotate.d/zhizhi
```

添加以下内容：
```
/var/lib/docker/containers/*/*.log {
  rotate 7
  daily
  compress
  missingok
  delaycompress
  copytruncate
}
```

### 2. 系统监控
建议部署监控解决方案，如：
- Prometheus + Grafana 用于指标监控
- ELK Stack 用于日志分析

### 3. 定期更新
定期更新 Docker 镜像以获取安全补丁：
```bash
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d --build
```

### 4. 低内存服务器配置优化
如果在内存较小的服务器（如2GB RAM）上部署，建议调整 Elasticsearch 的内存配置：

1. 修改 `docker-compose.prod.yml` 文件中的 Elasticsearch 内存设置：
```yaml
environment:
  - discovery.type=single-node
  - xpack.security.enabled=false
  - ES_JAVA_OPTS=-Xms256m -Xmx256m  # 从512m调整为256m
```

2. 同样调整 `docker-compose.yml` 文件中的 Elasticsearch 内存设置：
```yaml
environment:
  - discovery.type=single-node
  - xpack.security.enabled=false
  - ES_JAVA_OPTS=-Xms256m -Xmx256m
```

### 4. 配置日志轮转
```bash
# 配置logrotate防止磁盘空间耗尽
sudo tee /etc/logrotate.d/zhizhi > /dev/null << 'EOF'
/var/lib/docker/containers/*/*.log {
  rotate 7
  daily
  compress
  missingok
  delaycompress
  copytruncate
}
EOF
```

### 5. 设置开机自启
```bash
# 创建systemd服务文件
sudo tee /etc/systemd/system/zhizhi.service > /dev/null << 'EOF'
[Unit]
Description=Zhizhi Community Platform
After=docker.service
Requires=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/zhizhi/zhizhi-backend
ExecStart=/usr/local/bin/docker-compose -f docker-compose.prod.yml up -d
ExecStop=/usr/local/bin/docker-compose -f docker-compose.prod.yml down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

# 启用开机自启
sudo systemctl enable zhizhi.service
```

### 6. 定期维护命令
```bash
# 定期更新Docker镜像
cd /opt/zhizhi/zhizhi-backend
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d --build

# 备份MySQL数据
docker exec zhizhi-mysql mysqldump -u root -p zhizhi > /opt/zhizhi/backups/backup-$(date +%Y%m%d).sql

# 备份Redis数据
docker exec zhizhi-redis redis-cli SAVE
```

## 故障排除

### 1. 容器无法启动
检查日志以获取详细信息：
```bash
docker logs <container_name>
```

### 2. 数据库连接问题
确保：
- 数据库服务正在运行
- 网络配置正确
- 连接参数正确

### 3. 存储空间不足
检查磁盘使用情况：
```bash
df -h
du -sh /var/lib/zhizhi/*
```

如有必要，清理不需要的数据或扩展存储空间。

### 4. 常见问题排查
```bash
# 查看所有容器日志
docker-compose -f docker-compose.prod.yml logs

# 查看特定容器日志
docker logs zhizhi-backend

# 检查容器资源使用情况
docker stats

# 重启特定服务
docker-compose -f docker-compose.prod.yml restart <service_name>

# 进入容器内部调试
docker exec -it zhizhi-backend /bin/bash
```