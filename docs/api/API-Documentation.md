# 老人监护管理系统 API 文档

## 概述

老人监护管理系统提供完整的RESTful API，支持用户管理、设备管理、实时监控、预警系统、历史数据分析和消息通知等功能。

### 基础信息

- **Base URL**: `http://localhost:8080/api`
- **API版本**: v1.0.0
- **认证方式**: JWT Bearer Token
- **数据格式**: JSON
- **字符编码**: UTF-8

### 认证说明

所有需要认证的API都需要在请求头中包含JWT Token：

```
Authorization: Bearer <your-jwt-token>
```

### 响应格式

所有API响应都采用统一格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2024-01-01T12:00:00Z"
}
```

## 用户管理 API

### 用户注册

**POST** `/auth/register`

注册新用户账户。

**请求参数:**

```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "fullName": "测试用户",
  "phone": "13800138000",
  "role": "ELDERLY"
}
```

**响应示例:**

```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "测试用户",
    "role": "ELDERLY"
  }
}
```

### 用户登录

**POST** `/auth/login`

用户登录获取访问令牌。

**请求参数:**

```json
{
  "username": "testuser",
  "password": "password123"
}
```

**响应示例:**

```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh_token_here",
    "expiresIn": 3600,
    "user": {
      "id": 1,
      "username": "testuser",
      "fullName": "测试用户",
      "role": "ELDERLY"
    }
  }
}
```

### 获取用户信息

**GET** `/users/profile`

获取当前用户的详细信息。

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "fullName": "测试用户",
    "phone": "13800138000",
    "role": "ELDERLY",
    "status": "ACTIVE",
    "createdAt": "2024-01-01T12:00:00Z",
    "updatedAt": "2024-01-01T12:00:00Z"
  }
}
```

### 更新用户信息

**PUT** `/users/profile`

更新当前用户的信息。

**请求参数:**

```json
{
  "fullName": "新的用户名",
  "phone": "13900139000",
  "email": "new@example.com"
}
```

## 设备管理 API

### 获取设备列表

**GET** `/devices`

获取用户绑定的设备列表。

**查询参数:**

- `page`: 页码 (默认: 1)
- `size`: 每页大小 (默认: 10)
- `status`: 设备状态筛选
- `type`: 设备类型筛选

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "id": 1,
        "deviceId": "DEVICE001",
        "deviceName": "智能手环",
        "deviceType": "SMART_WATCH",
        "status": "ONLINE",
        "location": "客厅",
        "lastHeartbeat": "2024-01-01T12:00:00Z",
        "createdAt": "2024-01-01T10:00:00Z"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

### 添加设备

**POST** `/devices`

绑定新设备到用户账户。

**请求参数:**

```json
{
  "deviceId": "DEVICE002",
  "deviceName": "血压计",
  "deviceType": "BLOOD_PRESSURE_MONITOR",
  "location": "卧室"
}
```

### 获取设备详情

**GET** `/devices/{deviceId}`

获取指定设备的详细信息。

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "deviceId": "DEVICE001",
    "deviceName": "智能手环",
    "deviceType": "SMART_WATCH",
    "status": "ONLINE",
    "location": "客厅",
    "batteryLevel": 85,
    "lastHeartbeat": "2024-01-01T12:00:00Z",
    "firmwareVersion": "1.0.0",
    "createdAt": "2024-01-01T10:00:00Z"
  }
}
```

## 实时监控 API

### 获取实时健康数据

**GET** `/monitoring/realtime`

获取设备的实时健康监测数据。

**查询参数:**

- `deviceId`: 设备ID (可选)
- `dataTypes`: 数据类型，多个用逗号分隔 (可选)

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "deviceId": "DEVICE001",
      "dataType": "HEART_RATE",
      "value": 72,
      "unit": "bpm",
      "timestamp": "2024-01-01T12:00:00Z",
      "status": "NORMAL"
    },
    {
      "deviceId": "DEVICE001",
      "dataType": "BLOOD_PRESSURE_SYSTOLIC",
      "value": 120,
      "unit": "mmHg",
      "timestamp": "2024-01-01T11:55:00Z",
      "status": "NORMAL"
    }
  ]
}
```

### WebSocket 实时数据推送

**WebSocket** `/ws/monitoring`

建立WebSocket连接接收实时数据推送。

**连接参数:**

- `token`: JWT认证令牌
- `deviceId`: 设备ID (可选)

**消息格式:**

```json
{
  "type": "health_data",
  "data": {
    "deviceId": "DEVICE001",
    "dataType": "HEART_RATE",
    "value": 75,
    "unit": "bpm",
    "timestamp": "2024-01-01T12:00:00Z"
  }
}
```

## 预警系统 API

### 获取预警列表

**GET** `/alerts`

获取用户的预警记录。

**查询参数:**

- `page`: 页码
- `size`: 每页大小
- `status`: 预警状态 (ACTIVE, RESOLVED, CLOSED)
- `severity`: 严重程度 (LOW, MEDIUM, HIGH, CRITICAL)
- `startDate`: 开始日期
- `endDate`: 结束日期

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "id": 1,
        "alertType": "HEALTH_ALERT",
        "severity": "HIGH",
        "title": "心率异常",
        "description": "检测到心率超过正常范围",
        "status": "ACTIVE",
        "deviceId": "DEVICE001",
        "createdAt": "2024-01-01T12:00:00Z",
        "resolvedAt": null
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 创建预警规则

**POST** `/alerts/rules`

创建新的预警规则。

**请求参数:**

```json
{
  "ruleName": "心率异常预警",
  "dataType": "HEART_RATE",
  "condition": "heart_rate > 120",
  "severity": "HIGH",
  "isEnabled": true
}
```

### 获取预警规则列表

**GET** `/alerts/rules`

获取用户的预警规则列表。

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "ruleName": "心率异常预警",
      "dataType": "HEART_RATE",
      "condition": "heart_rate > 120",
      "severity": "HIGH",
      "isEnabled": true,
      "createdAt": "2024-01-01T10:00:00Z"
    }
  ]
}
```

## 历史数据 API

### 获取健康数据历史

**GET** `/history/health`

获取健康数据的历史记录。

**查询参数:**

- `deviceId`: 设备ID
- `dataType`: 数据类型
- `startDate`: 开始日期 (格式: YYYY-MM-DD)
- `endDate`: 结束日期 (格式: YYYY-MM-DD)
- `aggregation`: 聚合方式 (NONE, HOURLY, DAILY, WEEKLY, MONTHLY)

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "timestamp": "2024-01-01T12:00:00Z",
      "dataType": "HEART_RATE",
      "value": 72,
      "unit": "bpm",
      "deviceId": "DEVICE001"
    }
  ]
}
```

### 获取健康趋势分析

**GET** `/history/trends`

获取健康数据的趋势分析。

**查询参数:**

- `dataType`: 数据类型
- `period`: 分析周期 (WEEK, MONTH, QUARTER, YEAR)

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "dataType": "HEART_RATE",
    "period": "WEEK",
    "trends": [
      {
        "period": "2024-W01",
        "average": 75.5,
        "minimum": 65,
        "maximum": 85,
        "count": 1008
      }
    ],
    "summary": {
      "overallAverage": 75.5,
      "trendDirection": "STABLE",
      "healthScore": 85
    }
  }
}
```

## 通知系统 API

### 获取通知列表

**GET** `/notifications`

获取用户的通知记录。

**查询参数:**

- `page`: 页码
- `size`: 每页大小
- `status`: 通知状态 (PENDING, SENT, FAILED, READ)
- `type`: 通知类型 (SYSTEM_ALERT, HEALTH_ALERT, DEVICE_ALERT)

**响应示例:**

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "content": [
      {
        "id": 1,
        "type": "HEALTH_ALERT",
        "title": "心率异常预警",
        "content": "检测到心率超过正常范围",
        "status": "SENT",
        "sendMethod": "PUSH",
        "createdAt": "2024-01-01T12:00:00Z",
        "readAt": null
      }
    ],
    "totalElements": 1,
    "totalPages": 1
  }
}
```

### 标记通知为已读

**PUT** `/notifications/{notificationId}/read`

将指定通知标记为已读。

**响应示例:**

```json
{
  "code": 200,
  "message": "标记成功",
  "data": {
    "id": 1,
    "status": "READ",
    "readAt": "2024-01-01T12:05:00Z"
  }
}
```

### 配置通知偏好

**PUT** `/notifications/preferences`

配置用户的通知偏好设置。

**请求参数:**

```json
{
  "emailEnabled": true,
  "smsEnabled": false,
  "pushEnabled": true,
  "voiceEnabled": true,
  "quietHours": {
    "enabled": true,
    "startTime": "22:00",
    "endTime": "07:00"
  }
}
```

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未授权访问 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 429 | 请求频率过高 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

## 限流规则

| 端点类型 | 限制 |
|----------|------|
| 登录接口 | 5次/分钟 |
| 注册接口 | 3次/分钟 |
| 一般接口 | 100次/分钟 |
| 数据上传接口 | 1000次/分钟 |

## SDK 和示例代码

### JavaScript/TypeScript 示例

```typescript
// API客户端示例
class ElderlyMonitoringAPI {
  private baseURL = 'http://localhost:8080/api';
  private token: string | null = null;

  async login(username: string, password: string) {
    const response = await fetch(`${this.baseURL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ username, password }),
    });
    
    const data = await response.json();
    if (data.code === 200) {
      this.token = data.data.token;
      localStorage.setItem('token', this.token);
    }
    return data;
  }

  async getDevices() {
    const response = await fetch(`${this.baseURL}/devices`, {
      headers: {
        'Authorization': `Bearer ${this.token}`,
      },
    });
    return response.json();
  }

  async getRealtimeData(deviceId?: string) {
    const url = deviceId 
      ? `${this.baseURL}/monitoring/realtime?deviceId=${deviceId}`
      : `${this.baseURL}/monitoring/realtime`;
    
    const response = await fetch(url, {
      headers: {
        'Authorization': `Bearer ${this.token}`,
      },
    });
    return response.json();
  }
}
```

### Python 示例

```python
import requests
from typing import Dict, Any, Optional

class ElderlyMonitoringAPI:
    def __init__(self, base_url: str = "http://localhost:8080/api"):
        self.base_url = base_url
        self.token: Optional[str] = None
    
    def login(self, username: str, password: str) -> Dict[str, Any]:
        response = requests.post(
            f"{self.base_url}/auth/login",
            json={"username": username, "password": password}
        )
        data = response.json()
        if data.get("code") == 200:
            self.token = data["data"]["token"]
        return data
    
    def get_devices(self) -> Dict[str, Any]:
        headers = {"Authorization": f"Bearer {self.token}"}
        response = requests.get(f"{self.base_url}/devices", headers=headers)
        return response.json()
    
    def get_realtime_data(self, device_id: Optional[str] = None) -> Dict[str, Any]:
        headers = {"Authorization": f"Bearer {self.token}"}
        params = {"deviceId": device_id} if device_id else {}
        response = requests.get(
            f"{self.base_url}/monitoring/realtime",
            headers=headers,
            params=params
        )
        return response.json()
```

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现用户管理、设备管理、实时监控、预警系统、历史数据和通知功能
- 支持JWT认证和WebSocket实时数据推送