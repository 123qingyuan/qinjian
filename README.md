# 老人监护管理系统

## 项目概述

老人监护管理系统是一个专为老年人健康监护设计的综合性平台，通过智能设备实时监测用户的健康状况，提供及时的预警通知和健康数据分析。系统采用微服务架构，支持高并发、高可用和可扩展性。

## 技术架构

### 后端技术栈
- **框架**: Spring Boot 3.1.5
- **微服务**: Spring Cloud Gateway, Eureka
- **数据库**: PostgreSQL 15, InfluxDB 2.7
- **缓存**: Redis 7
- **消息队列**: RabbitMQ
- **监控**: Prometheus, Grafana
- **容器化**: Docker, Docker Compose

### 前端技术栈
- **框架**: React 18 + TypeScript
- **UI库**: Ant Design
- **状态管理**: Context API
- **图表**: Recharts
- **构建工具**: Vite
- **样式**: Less

### 系统架构图

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   前端应用      │    │   移动端应用    │    │   第三方集成    │
│   (React)       │    │   (React Native)│    │   (API/Webhook) │
└─────────┬───────┘    └─────────┬───────┘    └─────────┬───────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      API 网关              │
                    │   (Spring Cloud Gateway)   │
                    └─────────────┬─────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      服务注册中心          │
                    │       (Eureka)            │
                    └─────────────┬─────────────┘
                                 │
          ┌──────────────────────┼──────────────────────┐
          │                      │                      │
┌─────────┴───────┐    ┌─────────┴───────┐    ┌─────────┴───────┐
│   用户服务      │    │   设备服务      │    │   监控服务      │
│   (User)        │    │   (Device)      │    │   (Monitoring)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
          │                      │                      │
          └──────────────────────┼──────────────────────┘
                                 │
                    ┌─────────────┴─────────────┐
                    │      数据存储层           │
                    │ PostgreSQL │ InfluxDB │ Redis │
                    └─────────────────────────────┘
```

## 核心功能

### 1. 用户管理 (FR-001)
- 用户注册、登录、认证
- 角色权限管理
- 个人信息管理
- 家属关联管理

### 2. 设备管理 (FR-002)
- 设备注册和绑定
- 设备状态监控
- 设备远程控制
- 设备维护管理

### 3. 实时监控 (FR-003)
- 健康数据实时采集
- WebSocket实时推送
- 多维度健康指标监测
- 异常数据检测

### 4. 预警系统 (FR-004)
- 智能预警规则配置
- 多级预警机制
- 预警通知推送
- 预警处理流程

### 5. 历史数据 (FR-005)
- 健康数据存储
- 历史数据查询
- 趋势分析
- 健康报告生成

### 6. 消息通知 (FR-006)
- 多渠道通知发送
- 通知模板管理
- 通知偏好设置
- 通知状态跟踪

## 项目结构

```
elderly-assistant-web/
├── backend/                    # 后端服务
│   ├── eureka-service/        # 服务注册中心
│   ├── gateway-service/       # API网关
│   ├── user-service/          # 用户服务
│   ├── device-service/        # 设备服务
│   ├── monitoring-service/    # 监控服务
│   ├── alert-service/         # 预警服务
│   ├── history-service/       # 历史数据服务
│   ├── notification-service/  # 通知服务
│   ├── database/              # 数据库脚本
│   ├── monitoring/            # 监控配置
│   ├── performance-tests/     # 性能测试
│   └── scripts/               # 部署脚本
├── frontend/                  # 前端应用
│   ├── public/                # 静态资源
│   ├── src/                   # 源代码
│   │   ├── components/        # 通用组件
│   │   ├── pages/             # 页面组件
│   │   ├── contexts/          # 上下文
│   │   ├── services/          # 服务层
│   │   ├── utils/             # 工具函数
│   │   └── types/             # 类型定义
│   ├── package.json
│   └── vite.config.ts
├── docs/                      # 项目文档
│   ├── api/                   # API文档
│   ├── deployment/            # 部署文档
│   ├── user/                  # 用户手册
│   └── admin/                 # 管理员指南
└── README.md                  # 项目说明
```

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- Docker 20.10+
- Docker Compose 2.0+

### 本地开发

1. **克隆项目**
```bash
git clone https://github.com/your-org/elderly-monitoring-system.git
cd elderly-monitoring-system
```

2. **启动数据库服务**
```bash
cd backend/database
docker-compose up -d postgres redis influxdb
```

3. **启动后端服务**
```bash
cd backend
./scripts/start-all.sh
```

4. **启动前端应用**
```bash
cd frontend
npm install
npm run dev
```

5. **访问应用**
- 前端应用: http://localhost:3000
- API文档: http://localhost:8080/swagger-ui.html
- 监控面板: http://localhost:3000/grafana

### 生产部署

1. **环境配置**
```bash
cp backend/.env.example backend/.env.production
# 编辑生产环境配置
```

2. **构建应用**
```bash
# 构建后端服务
./scripts/build.sh

# 构建前端应用
cd frontend
npm run build
docker build -t elderly-monitoring-frontend:latest .
```

3. **启动生产服务**
```bash
cd backend/database
docker-compose -f docker-compose.yml up -d
```

详细的部署指南请参考 [部署文档](docs/deployment/Deployment-Guide.md)

## API文档

完整的API文档请参考：
- [API文档](docs/api/API-Documentation.md)
- 在线API文档: http://localhost:8080/swagger-ui.html

## 测试

### 单元测试
```bash
cd backend
mvn test
```

### 集成测试
```bash
cd backend
mvn test -Dtest=**/*IntegrationTest
```

### 性能测试
```bash
cd backend/performance-tests
./run-performance-test.bat
```

### 测试覆盖率
```bash
cd backend
mvn jacoco:report
```

## 监控

### 系统监控
- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin123)

### 健康检查
- **Eureka**: http://localhost:8761/actuator/health
- **Gateway**: http://localhost:8080/actuator/health
- **User Service**: http://localhost:8081/actuator/health

## 配置说明

### 环境变量
主要的环境变量配置：

```bash
# 数据库配置
DB_HOST=localhost
DB_PORT=5432
DB_NAME=elderly_monitoring
DB_USERNAME=postgres
DB_PASSWORD=password

# Redis配置
REDIS_HOST=localhost
REDIS_PORT=6379

# JWT配置
JWT_SECRET=your-secret-key
JWT_EXPIRATION=3600
```

### 服务端口
- Eureka: 8761
- Gateway: 8080
- User Service: 8081
- Device Service: 8082
- Monitoring Service: 8083
- Alert Service: 8084
- History Service: 8085
- Notification Service: 8086

## 贡献指南

### 开发规范
- 遵循 [代码规范](.roo/rules-code/coding-standards.md)
- 使用统一的代码格式化配置
- 编写单元测试和集成测试
- 提交前进行代码审查

### 提交规范
```bash
# 功能开发
git commit -m "feat: 添加用户管理功能"

# 问题修复
git commit -m "fix: 修复设备连接问题"

# 文档更新
git commit -m "docs: 更新API文档"
```

### 分支策略
- `main`: 主分支，用于生产环境
- `develop`: 开发分支，用于集成测试
- `feature/*`: 功能分支
- `hotfix/*`: 热修复分支

## 文档

- [用户手册](docs/user/User-Manual.md)
- [管理员指南](docs/admin/Admin-Guide.md)
- [部署指南](docs/deployment/Deployment-Guide.md)
- [API文档](docs/api/API-Documentation.md)

## 许可证

本项目采用 [MIT License](LICENSE) 许可证。

## 联系我们

- **项目维护者**: 老人监护管理系统团队
- **技术支持**: https://github.com/123qingyuan
- **问题反馈**: https://github.com/123qingyuan/qinjian
