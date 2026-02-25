# 构建
FROM node:18-alpine AS builder
WORKDIR /app
RUN npm install -g pnpm --registry=https://registry.npmmirror.com
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm build:prod

# 运行
FROM nginx:alpine
# 使用 mounted 目录中的 Nginx 配置文件
COPY mounted/nginx/nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]