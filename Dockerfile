# -----------------------------
# 构建阶段：使用 Maven 构建 jar
# -----------------------------
FROM maven:3.8.8-eclipse-temurin-8 AS builder

# 设置工作目录
WORKDIR /app

# 复制 pom.xml 并提前下载依赖，加快后续构建速度
COPY pom.xml ./

# 下载依赖（不使用宿主机 Maven 仓库）
RUN mvn -B dependency:go-offline

# 复制源代码
COPY src ./src

# 构建 jar，跳过测试
RUN mvn -B -DskipTests package

# -----------------------------
# 运行阶段：只包含 JRE 和打包好的 jar
# -----------------------------
FROM eclipse-temurin:8-jre

WORKDIR /app

# 复制构建好的 jar 文件
COPY --from=builder /app/target/zhizhi-1.0.jar app.jar

# 开放端口
EXPOSE 8091

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]
