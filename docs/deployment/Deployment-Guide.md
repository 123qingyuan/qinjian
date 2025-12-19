# 老人监护管理系统部署指南

## 概述

本文档详细说明了老人监护管理系统的部署流程，包括环境准备、系统配置、服务部署和运维监控等内容。

## 系统要求

### 硬件要求

#### 最低配置
- **CPU**: 4核心 2.0GHz
- **内存**: 8GB RAM
- **存储**: 100GB SSD
- **网络**: 100Mbps

#### 推荐配置
- **CPU**: 8核心 2.5GHz
- **内存**: 16GB RAM
- **存储**: 500GB SSD
- **网络**: 1Gbps

### 软件要求

#### 操作系统
- Ubuntu 20.04 LTS 或更高版本
- CentOS 8 或更高版本
- Windows Server 2019 或更高版本

#### 运行环境
- **Java**: OpenJDK 17 或更高版本
- **Docker**: 20.10 或更高版本
- **Docker Compose**: 2.0 或更高版本
- **Node.js**: 18.x 或更高版本 (前端构建)

## 环境准备

### 1. 安装 Java 17

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install -y openjdk-17-jdk
java -version
```

#### CentOS/RHEL
```bash
sudo yum install -y java-17-openjdk-devel
java -version
```

#### Windows
1. 下载 OpenJDK 17 安装包
2. 运行安装程序
3. 配置 JAVA_HOME 环境变量

### 2. 安装 Docker 和 Docker Compose

#### Ubuntu/Debian
```bash
# 安装 Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.12.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

#### CentOS/RHEL
```bash
# 安装 Docker
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker $USER

# 安装 Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.12.2/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 3. 安装 Node.js (前端构建)

```bash
# 使用 NodeSource 仓库
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt-get install -y nodejs

# 验证安装
node --version
npm --version
```

## 部署步骤

### 1. 获取源代码

```bash
# 克隆代码仓库
git clone https://github.com/your-org/elderly-monitoring-system.git
cd elderly-monitoring-system

# 切换到生产分支
git checkout production
```

### 2. 配置环境变量

创建生产环境配置文件：

```bash
# 后端环境配置
cp backend/.env.example backend/.env.production

# 前端环境配置
cp frontend/.env.example frontend/.env.production
```

编辑后端环境配置：

```bash
# backend/.env.production
# 数据库配置
DB_HOST=postgres
DB_PORT=5432
DB_NAME=elderly_monitoring
DB_USERNAME=postgres
DB_PASSWORD=your_secure_password

# Redis配置
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=your_redis_password

# JWT配置
JWT_SECRET=your_jwt_secret_key
JWT_EXPIRATION=3600

# InfluxDB配置
INFLUXDB_HOST=influxdb
INFLUXDB_PORT=8086
INFLUXDB_DATABASE=health_data
INFLUXDB_USERNAME=admin
INFLUXDB_PASSWORD=your_influxdb_password

# 服务端口配置
EUREKA_PORT=8761
GATEWAY_PORT=8080
USER_SERVICE_PORT=8081
DEVICE_SERVICE_PORT=8082
MONITORING_SERVICE_PORT=8083
ALERT_SERVICE_PORT=8084
HISTORY_SERVICE_PORT=8085
NOTIFICATION_SERVICE_PORT=8086

# 监控配置
PROMETHEUS_PORT=9090
GRAFANA_PORT=3000
ALERTMANAGER_PORT=9093

# 日志级别
LOG_LEVEL=INFO
```

编辑前端环境配置：

```bash
# frontend/.env.production
REACT_APP_API_BASE_URL=http://your-domain.com/api
REACT_APP_WS_URL=ws://your-domain.com/ws
REACT_APP_ENVIRONMENT=production
```

### 3. 构建应用

#### 构建后端服务

```bash
cd backend

# 编译所有服务
./scripts/build.sh

# 或使用 Maven
mvn clean package -DskipTests
```

#### 构建前端应用

```bash
cd frontend

# 安装依赖
npm install

# 构建生产版本
npm run build

# Docker 构建
docker build -t elderly-monitoring-frontend:latest .
```

### 4. 配置 Docker Compose

更新 `backend/database/docker-compose.yml` 中的生产配置：

```yaml
version: '3.8'

services:
  # 数据库服务
  postgres:
    image: postgres:15
    container_name: elderly-postgres
    environment:
      POSTGRES_DB: ${DB_NAME}
      POSTGRES_USER: ${DB_USERNAME}
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
      - ./optimization.sql:/docker-entrypoint-initdb.d/optimization.sql
    ports:
      - "5432:5432"
    restart: unless-stopped
    networks:
      - elderly-network

  # Redis 缓存
  redis:
    image: redis:7-alpine
    container_name: elderly-redis
    command: redis-server --requirepass ${REDIS_PASSWORD}
    volumes:
      - redis_data:/data
    ports:
      - "6379:6379"
    restart: unless-stopped
    networks:
      - elderly-network

  # InfluxDB 时序数据库
  influxdb:
    image: influxdb:2.7
    container_name: elderly-influxdb
    environment:
      INFLUXDB_DB: ${INFLUXDB_DATABASE}
      INFLUXDB_ADMIN_USER: ${INFLUXDB_USERNAME}
      INFLUXDB_ADMIN_PASSWORD: ${INFLUXDB_PASSWORD}
    volumes:
      - influxdb_data:/var/lib/influxdb2
    ports:
      - "8086:8086"
    restart: unless-stopped
    networks:
      - elderly-network

  # Eureka 服务注册中心
  eureka-service:
    image: elderly-monitoring/eureka-service:latest
    container_name: elderly-eureka
    ports:
      - "${EUREKA_PORT}:8761"
    environment:
      SPRING_PROFILES_ACTIVE: production
    restart: unless-stopped
    networks:
      - elderly-network
    depends_on:
      - postgres
      - redis

  # API 网关
  gateway-service:
    image: elderly-monitoring/gateway-service:latest
    container_name: elderly-gateway
    ports:
      - "${GATEWAY_PORT}:8080"
    environment:
      SPRING_PROFILES_ACTIVE: production
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-service:8761/eureka
    restart: unless-stopped
    networks:
      - elderly-network
    depends_on:
      - eureka-service

  # 其他微服务...
  user-service:
    image: elderly-monitoring/user-service:latest
    container_name: elderly-user-service
    environment:
      SPRING_PROFILES_ACTIVE: production
      EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE: http://eureka-service:8761/eureka
    restart: unless-stopped
    networks:
      - elderly-network
    depends_on:
      - eureka-service
      - postgres

  # 监控服务
  prometheus:
    image: prom/prometheus:latest
    container_name: elderly-prometheus
    ports:
      - "${PROMETHEUS_PORT}:9090"
    volumes:
      - ../monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - ../monitoring/alert_rules.yml:/etc/prometheus/alert_rules.yml
      - prometheus_data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=200h'
      - '--web.enable-lifecycle'
    restart: unless-stopped
    networks:
      - elderly-network

  grafana:
    image: grafana/grafana:latest
    container_name: elderly-grafana
    ports:
      - "${GRAFANA_PORT}:3000"
    environment:
      GF_SECURITY_ADMIN_PASSWORD: admin123
    volumes:
      - grafana_data:/var/lib/grafana
      - ../monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards
      - ../monitoring/grafana/datasources:/etc/grafana/provisioning/datasources
    restart: unless-stopped
    networks:
      - elderly-network

  # 前端服务
  frontend:
    image: elderly-monitoring-frontend:latest
    container_name: elderly-frontend
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    restart: unless-stopped
    networks:
      - elderly-network
    depends_on:
      - gateway-service

volumes:
  postgres_data:
  redis_data:
  influxdb_data:
  prometheus_data:
  grafana_data:

networks:
  elderly-network:
    driver: bridge
```

### 5. 配置 Nginx

创建 `backend/nginx/nginx.conf`：

```nginx
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server gateway-service:8080;
    }

    server {
        listen 80;
        server_name your-domain.com;
        
        # 重定向到 HTTPS
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name your-domain.com;

        # SSL 配置
        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;

        # 前端静态文件
        location / {
            root /usr/share/nginx/html;
            index index.html index.htm;
            try_files $uri $uri/ /index.html;
        }

        # API 代理
        location /api/ {
            proxy_pass http://backend/api/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            
            # WebSocket 支持
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
        }

        # WebSocket 代理
        location /ws/ {
            proxy_pass http://backend/ws/;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### 6. 启动服务

```bash
# 启动所有服务
cd backend/database
docker-compose -f docker-compose.yml up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f
```

### 7. 数据库初始化

```bash
# 等待数据库启动
sleep 30

# 执行数据库初始化脚本
docker exec -i elderly-postgres psql -U postgres -d elderly_monitoring < init.sql

# 执行优化脚本
docker exec -i elderly-postgres psql -U postgres -d elderly_monitoring < optimization.sql
```

### 8. 验证部署

#### 检查服务健康状态

```bash
# 检查各个服务
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8080/actuator/health  # Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Device Service
curl http://localhost:8083/actuator/health  # Monitoring Service
curl http://localhost:8084/actuator/health  # Alert Service
curl http://localhost:8085/actuator/health  # History Service
curl http://localhost:8086/actuator/health  # Notification Service
```

#### 检查监控服务

```bash
# Prometheus
curl http://localhost:9090/-/healthy

# Grafana
curl http://localhost:3000/api/health
```

#### 访问应用

- **前端应用**: https://your-domain.com
- **API文档**: https://your-domain.com/api/doc.html
- **Prometheus**: http://your-domain.com:9090
- **Grafana**: http://your-domain.com:3000 (admin/admin123)

## 运维管理

### 1. 日志管理

#### 查看服务日志

```bash
# 查看所有服务日志
docker-compose logs

# 查看特定服务日志
docker-compose logs gateway-service
docker-compose logs monitoring-service

# 实时查看日志
docker-compose logs -f monitoring-service
```

#### 配置日志轮转

创建 `/etc/logrotate.d/elderly-monitoring`：

```
/var/log/elderly-monitoring/*.log {
    daily
    missingok
    rotate 30
    compress
    delaycompress
    notifempty
    create 644 root root
    postrotate
        docker-compose restart gateway-service
    endscript
}
```

### 2. 监控告警

#### Prometheus 告警规则

告警规则已配置在 `backend/monitoring/alert_rules.yml` 中，包括：

- 服务可用性告警
- 性能指标告警
- 业务指标告警
- 资源使用告警

#### Grafana 仪表板

预配置的仪表板包括：

- 系统概览仪表板
- 应用性能仪表板
- 业务指标仪表板
- 基础设施仪表板

### 3. 备份策略

#### 数据库备份

创建备份脚本 `scripts/backup-database.sh`：

```bash
#!/bin/bash

BACKUP_DIR="/backup/elderly-monitoring"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="elderly_monitoring_backup_${DATE}.sql"

# 创建备份目录
mkdir -p $BACKUP_DIR

# 备份数据库
docker exec elderly-postgres pg_dump -U postgres elderly_monitoring > $BACKUP_DIR/$BACKUP_FILE

# 压缩备份文件
gzip $BACKUP_DIR/$BACKUP_FILE

# 删除7天前的备份
find $BACKUP_DIR -name "*.sql.gz" -mtime +7 -delete

echo "数据库备份完成: $BACKUP_DIR/${BACKUP_FILE}.gz"
```

#### 定时备份

添加到 crontab：

```bash
# 每天凌晨2点备份数据库
0 2 * * * /path/to/scripts/backup-database.sh
```

### 4. 更新部署

#### 滚动更新

```bash
# 构建新版本镜像
docker build -t elderly-monitoring/gateway-service:v2.0.0 ./backend/gateway-service

# 更新服务
docker-compose up -d --no-deps gateway-service

# 验证更新
curl http://localhost:8080/actuator/health
```

#### 回滚操作

```bash
# 回滚到上一版本
docker-compose up -d --no-deps gateway-service:latest

# 或指定版本
docker-compose up -d --no-deps gateway-service:v1.0.0
```

### 5. 性能优化

#### 数据库优化

- 定期执行 `ANALYZE` 更新统计信息
- 监控慢查询并优化
- 定期清理历史数据
- 调整连接池配置

#### 应用优化

- 调整 JVM 参数
- 配置合适的线程池大小
- 启用缓存策略
- 优化数据库查询

## 故障排除

### 常见问题

#### 1. 服务启动失败

**问题**: 服务无法启动

**解决方案**:
```bash
# 查看服务日志
docker-compose logs service-name

# 检查端口占用
netstat -tlnp | grep port

# 检查磁盘空间
df -h

# 检查内存使用
free -h
```

#### 2. 数据库连接失败

**问题**: 应用无法连接数据库

**解决方案**:
```bash
# 检查数据库状态
docker exec elderly-postgres pg_isready

# 检查网络连接
docker network ls
docker network inspect elderly-network

# 验证连接参数
docker exec -it elderly-postgres psql -U postgres -d elderly_monitoring
```

#### 3. 内存不足

**问题**: 内存使用过高

**解决方案**:
```bash
# 检查内存使用
docker stats

# 调整 JVM 参数
JAVA_OPTS="-Xms512m -Xmx1024m"

# 重启服务
docker-compose restart service-name
```

### 紧急响应流程

1. **服务不可用**
   - 检查服务状态
   - 查看错误日志
   - 重启相关服务
   - 通知运维团队

2. **数据库异常**
   - 检查数据库连接
   - 验证数据完整性
   - 执行恢复操作
   - 通知DBA团队

3. **安全事件**
   - 立即隔离受影响系统
   - 收集证据和日志
   - 通知安全团队
   - 执行恢复计划

## 安全配置

### 1. 网络安全

- 配置防火墙规则
- 使用 HTTPS 加密传输
- 限制数据库访问
- 配置 VPN 访问

### 2. 应用安全

- 定期更新依赖包
- 配置安全头
- 启用 CSRF 保护
- 实施访问控制

### 3. 数据安全

- 数据库加密
- 备份加密
- 访问审计
- 数据脱敏

## 联系信息

- **技术支持**: tech-support@elderly-monitoring.com
- **运维团队**: ops@elderly-monitoring.com
- **安全团队**: security@elderly-monitoring.com

---

*本文档最后更新时间: 2024-01-01*