# ================================
# 数据库初始化 Dockerfile
# 支持多架构: AMD64 (x86_64) / ARM64
# ================================

FROM mysql:8.0

# 设置环境变量
ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=fitness_club
ENV MYSQL_CHARSET=utf8mb4
ENV MYSQL_COLLATION=utf8mb4_general_ci

# 设置时区
ENV TZ=Asia/Shanghai

# 复制初始化 SQL 脚本到 docker-entrypoint-initdb.d 目录
# MySQL 容器启动时会自动执行该目录下的 SQL 脚本
COPY schema.sql /docker-entrypoint-initdb.d/01-schema.sql

# 自定义 MySQL 配置
RUN echo '[mysqld]' > /etc/mysql/conf.d/custom.cnf && \
    echo 'character-set-server=utf8mb4' >> /etc/mysql/conf.d/custom.cnf && \
    echo 'collation-server=utf8mb4_general_ci' >> /etc/mysql/conf.d/custom.cnf && \
    echo 'default-time-zone=+08:00' >> /etc/mysql/conf.d/custom.cnf && \
    echo 'max_connections=500' >> /etc/mysql/conf.d/custom.cnf && \
    echo 'innodb_buffer_pool_size=256M' >> /etc/mysql/conf.d/custom.cnf && \
    echo '' >> /etc/mysql/conf.d/custom.cnf && \
    echo '[client]' >> /etc/mysql/conf.d/custom.cnf && \
    echo 'default-character-set=utf8mb4' >> /etc/mysql/conf.d/custom.cnf

# 暴露端口
EXPOSE 3306

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=5 \
    CMD mysqladmin ping -h localhost -u root -p${MYSQL_ROOT_PASSWORD} || exit 1
