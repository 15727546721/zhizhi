# -----------------------------
# 构建阶段
# -----------------------------
FROM node:18-alpine AS builder
WORKDIR /app

# 使用国内 npm 镜像加速
RUN npm config set registry https://registry.npmmirror.com

# 复制依赖文件
COPY package*.json ./

# 安装依赖
RUN npm ci

# 复制源码
COPY . .

# 构建前端项目（输出到 /app/dist）
RUN npm run build

# -----------------------------
# 运行阶段（Nginx）
# -----------------------------
FROM nginx:alpine
WORKDIR /app

# 替换默认 Nginx 配置，使用 mounted 目录中的配置文件
COPY mounted/nginx/nginx.conf /etc/nginx/conf.d/default.conf

# 复制构建好的静态文件
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]