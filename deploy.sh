#!/bin/bash

# 知之社区系统生产环境部署脚本

set -e  # 遇到错误时退出

echo "开始部署知之社区系统..."

# 检查必要的文件是否存在
if [ ! -f ".env.prod" ]; then
    echo "错误: 找不到 .env.prod 文件，请先创建该文件"
    exit 1
fi

if [ ! -f "docker-compose.prod.yml" ]; then
    echo "错误: 找不到 docker-compose.prod.yml 文件"
    exit 1
fi

# 停止现有服务
echo "停止现有服务..."
docker-compose -f docker-compose.prod.yml down

# 构建并启动服务
echo "构建并启动服务..."
docker-compose -f docker-compose.prod.yml --env-file .env.prod up --build -d

# 等待服务启动
echo "等待服务启动..."
sleep 30

# 检查服务状态
echo "检查服务状态..."
docker-compose -f docker-compose.prod.yml ps

echo "部署完成！"
echo "请确保已正确配置 .env.prod 文件中的敏感信息"
echo "访问地址:"
echo "- 后端API: http://localhost:8091"
echo "- API文档: http://localhost:8091/doc.html"
echo "- MinIO控制台: http://localhost:9001"