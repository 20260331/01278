# ================================
# 后端项目 Dockerfile
# 支持多架构: AMD64 (x86_64) / ARM64
# ================================

# 声明目标平台参数
ARG TARGETPLATFORM
ARG BUILDPLATFORM

# -------- 阶段1: 构建阶段 --------
FROM maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 配置 Maven 阿里云镜像加速
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?>\
    <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
              xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
              xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">\
        <mirrors>\
            <mirror>\
                <id>aliyun</id>\
                <mirrorOf>central</mirrorOf>\
                <name>Aliyun Maven Mirror</name>\
                <url>https://maven.aliyun.com/repository/public</url>\
            </mirror>\
        </mirrors>\
    </settings>' > /root/.m2/settings.xml

# 先复制 pom.xml 并下载依赖（利用 Docker 缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码
COPY src ./src

# 编译打包（跳过测试）
RUN mvn clean package -DskipTests -B

# -------- 阶段2: 运行阶段 --------
FROM eclipse-temurin:17-jre

# 设置时区
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 创建非 root 用户运行应用
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar

# 更改文件所有者
RUN chown -R appuser:appgroup /app

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM 优化参数
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
