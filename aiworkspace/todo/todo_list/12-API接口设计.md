# API 接口设计

> 版本：v1.0
> 更新时间：2025-01-15

## 1. 接口规范

### 1.1 RESTful 设计原则

- 使用 HTTP 动词表示操作类型
- 使用名词表示资源
- 使用复数形式表示资源集合
- 层级结构不超过 3 层

### 1.2 HTTP 动词

| 动词 | 说明 | 示例 |
|------|------|------|
| GET | 查询资源 | GET /api/process-definitions |
| POST | 创建资源 | POST /api/process-instances |
| PUT | 更新资源（全量） | PUT /api/process-instances/{id} |
| PATCH | 更新资源（部分） | PATCH /api/tasks/{id} |
| DELETE | 删除资源 | DELETE /api/process-instances/{id} |

### 1.3 统一响应格式

**成功响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": { }
}
```

**分页响应**：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "size": 10
  }
}
```

**错误响应**：
```json
{
  "code": 500,
  "message": "Internal Server Error",
  "error": "详细错误信息"
}
```

---

## 2. 流程模板管理接口

### 2.1 发布流程

```
POST /api/process/deploy
Content-Type: application/json

Request:
{
  "name": "请假流程",
  "description": "员工请假审批流程",
  "xml": "<?xml version=\"1.0\"..."
}

Response 200:
{
  "code": 200,
  "message": "流程发布成功",
  "data": {
    "processDefinitionId": "leaveRequest:1:1001",
    "key": "leaveRequest",
    "name": "请假流程",
    "version": 1,
    "deploymentId": "1001"
  }
}
```

### 2.2 查询流程定义列表

```
GET /api/process/definitions?page=1&size=10&key=leave

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "leaveRequest:1:1001",
        "key": "leaveRequest",
        "name": "请假流程",
        "version": 1,
        "description": "员工请假审批流程",
        "deploymentId": "1001",
        "suspended": false
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 2.3 获取流程 XML

```
GET /api/process/definitions/{processDefinitionId}/xml

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "processDefinitionId": "leaveRequest:1:1001",
    "bpmnXml": "<?xml version=\"1.0\"...",
    "name": "请假流程",
    "key": "leaveRequest",
    "version": 1
  }
}
```

### 2.4 创建快照

```
POST /api/process/snapshots
Content-Type: application/json

Request:
{
  "processDefinitionKey": "leaveRequest",
  "snapshotName": "版本1.0",
  "description": "初始版本",
  "creator": "admin"
}

Response 200:
{
  "code": 200,
  "message": "快照创建成功",
  "data": {
    "id": "uuid",
    "snapshotName": "版本1.0",
    "snapshotVersion": "1.0",
    "createdTime": "2025-01-15T10:00:00"
  }
}
```

### 2.5 查询快照列表

```
GET /api/process/snapshots/{processDefinitionKey}

Response 200:
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": "uuid",
      "snapshotName": "版本1.0",
      "snapshotVersion": "1.0",
      "description": "初始版本",
      "creator": "admin",
      "createdTime": "2025-01-15T10:00:00"
    }
  ]
}
```

### 2.6 回滚到快照

```
POST /api/process/snapshots/{snapshotId}/rollback

Response 200:
{
  "code": 200,
  "message": "回滚成功",
  "data": {
    "processDefinitionId": "leaveRequest:2:1002",
    "version": 2
  }
}
```

---

## 3. 流程实例管理接口

### 3.1 启动流程

```
POST /api/process-instances
Content-Type: application/json

Request:
{
  "processDefinitionKey": "leaveRequest",
  "businessKey": "LEV20250115001",
  "variables": {
    "applicant": "user001",
    "leaveDays": 3,
    "reason": "事假"
  }
}

Response 200:
{
  "code": 200,
  "message": "流程启动成功",
  "data": {
    "processInstanceId": "lev-10001",
    "processDefinitionId": "leaveRequest:1:1001",
    "businessKey": "LEV20250115001",
    "status": "running",
    "startTime": "2025-01-15T10:00:00"
  }
}
```

### 3.2 查询流程实例列表

```
GET /api/process-instances?processDefinitionKey=leaveRequest&status=running&page=1&size=10

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "processInstanceId": "lev-10001",
        "processDefinitionId": "leaveRequest:1:1001",
        "businessKey": "LEV20250115001",
        "processName": "请假流程",
        "startUser": "user001",
        "startTime": "2025-01-15T10:00:00",
        "status": "running",
        "currentNode": "部门审批"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 3.3 获取流程实例详情

```
GET /api/process-instances/{processInstanceId}

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "processInstanceId": "lev-10001",
    "processDefinitionId": "leaveRequest:1:1001",
    "businessKey": "LEV20250115001",
    "processName": "请假流程",
    "startUser": "user001",
    "startTime": "2025-01-15T10:00:00",
    "endTime": null,
    "status": "running",
    "variables": {
      "applicant": "user001",
      "leaveDays": 3
    },
    "currentActivities": [
      {
        "activityId": "deptApproval",
        "activityName": "部门审批",
        "assignee": "deptManager"
      }
    ]
  }
}
```

### 3.4 终止流程

```
DELETE /api/process-instances/{processInstanceId}
Content-Type: application/json

Request:
{
  "reason": "业务取消"
}

Response 200:
{
  "code": 200,
  "message": "流程终止成功"
}
```

### 3.5 挂起流程

```
PUT /api/process-instances/{processInstanceId}/suspend
Content-Type: application/json

Request:
{
  "reason": "等待补充材料"
}

Response 200:
{
  "code": 200,
  "message": "流程挂起成功",
  "data": {
    "processInstanceId": "lev-10001",
    "status": "suspended"
  }
}
```

### 3.6 激活流程

```
PUT /api/process-instances/{processInstanceId}/activate
Content-Type: application/json

Request:
{
  "reason": "材料已补充"
}

Response 200:
{
  "code": 200,
  "message": "流程激活成功",
  "data": {
    "processInstanceId": "lev-10001",
    "status": "running"
  }
}
```

---

## 4. 任务管理接口

### 4.1 查询我的待办

```
GET /api/tasks/todo?processDefinitionKey=leaveRequest&priority=80&page=1&size=10

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "task-10001",
        "taskName": "部门审批",
        "processInstanceId": "lev-10001",
        "processDefinitionId": "leaveRequest:1:1001",
        "processName": "请假流程",
        "businessKey": "LEV20250115001",
        "assignee": "deptManager",
        "priority": 80,
        "dueDate": "2025-01-16T18:00:00",
        "createTime": "2025-01-15T10:00:00",
        "startUser": "user001"
      }
    ],
    "total": 1,
    "page": 1,
    "size": 10
  }
}
```

### 4.2 待办统计

```
GET /api/tasks/todo/statistics

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 10,
    "highPriority": 3,
    "nearingDeadline": 2,
    "overdue": 1
  }
}
```

### 4.3 查询我的已办

```
GET /api/tasks/done?startTime=2025-01-01&endTime=2025-01-31&page=1&size=10

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "id": "task-10000",
        "taskName": "部门审批",
        "processInstanceId": "lev-10000",
        "processName": "请假流程",
        "assignee": "deptManager",
        "startTime": "2025-01-14T10:00:00",
        "endTime": "2025-01-14T14:00:00",
        "duration": 240,
        "processStatus": "completed"
      }
    ],
    "total": 50,
    "page": 1,
    "size": 10
  }
}
```

### 4.4 查询我发起的流程

```
GET /api/process-instances/my?status=running&page=1&size=10

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "processInstanceId": "lev-10001",
        "processName": "请假流程",
        "businessKey": "LEV20250115001",
        "startTime": "2025-01-15T10:00:00",
        "status": "running",
        "currentNode": "部门审批"
      }
    ],
    "total": 5,
    "page": 1,
    "size": 10
  }
}
```

---

## 5. 任务操作接口

### 5.1 提交任务

```
POST /api/tasks/{taskId}/complete
Content-Type: application/json

Request:
{
  "comment": "同意",
  "variables": {
    "approved": true,
    "approvalComment": "材料齐全，同意请假"
  },
  "attachments": [
    {
      "attachmentId": "uuid",
      "fileName": "证明材料.pdf"
    }
  ]
}

Response 200:
{
  "code": 200,
  "message": "任务提交成功"
}
```

### 5.2 转办任务

```
POST /api/tasks/{taskId}/transfer
Content-Type: application/json

Request:
{
  "targetUser": "user002",
  "reason": "原处理人出差"
}

Response 200:
{
  "code": 200,
  "message": "任务转办成功"
}
```

### 5.3 委托任务

```
POST /api/tasks/{taskId}/delegate
Content-Type: application/json

Request:
{
  "targetUser": "user002",
  "dueDate": "2025-01-20T18:00:00",
  "reason": "临时委托"
}

Response 200:
{
  "code": 200,
  "message": "任务委托成功"
}
```

### 5.4 驳回任务

```
POST /api/tasks/{taskId}/reject
Content-Type: application/json

Request:
{
  "comment": "材料不齐，请补充",
  "rejectTo": "start"  // 驳回到发起节点
}

Response 200:
{
  "code": 200,
  "message": "任务驳回成功"
}
```

---

## 6. 统计分析接口

### 6.1 流程实例统计

```
GET /api/statistics/process-instances?dimension=organization&startTime=2025-01-01&endTime=2025-01-31

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "dimension": "organization",
    "statistics": [
      {
        "name": "技术部",
        "totalCount": 100,
        "completedCount": 80,
        "runningCount": 15,
        "terminatedCount": 5,
        "completionRate": 80.0,
        "avgDuration": 1440
      }
    ]
  }
}
```

### 6.2 任务处理量统计

```
GET /api/statistics/task-volume?dimension=user&startTime=2025-01-01&endTime=2025-01-31

Response 200:
{
  "code": 200,
  "message": "success",
  "data": {
    "dimension": "user",
    "statistics": [
      {
        "userId": "user001",
        "userName": "张三",
        "totalTasks": 50,
        "completedTasks": 45,
        "avgDailyTasks": 2.5,
        "avgDuration": 120
      }
    ]
  }
}
```

---

## 7. 附件管理接口

### 7.1 上传附件

```
POST /api/attachments
Content-Type: multipart/form-data

Request:
[文件]

Response 200:
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "attachmentId": "uuid",
    "fileName": "证明材料.pdf",
    "fileSize": 1024000,
    "fileType": "pdf",
    "uploadTime": "2025-01-15T10:00:00"
  }
}
```

### 7.2 下载附件

```
GET /api/attachments/{attachmentId}/download

Response: File
Content-Type: application/pdf
Content-Disposition: attachment; filename="证明材料.pdf"
```

### 7.3 删除附件

```
DELETE /api/attachments/{attachmentId}

Response 200:
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 8. 错误码定义

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 500 | 服务器内部错误 |
| 1001 | 流程定义不存在 |
| 1002 | 流程实例不存在 |
| 1003 | 任务不存在 |
| 1004 | 流程已挂起 |
| 1005 | 流程已终止 |

---

## 9. 接口安全

### 9.1 认证

使用 JWT Token 认证：

```
Authorization: Bearer <token>
```

### 9.2 权限控制

基于 RBAC 的权限控制，在 Header 中传递租户信息（多租户场景）：

```
X-Tenant-Id: <tenantId>
```

---

## 10. 接口版本控制

通过 URL 路径进行版本控制：

```
/api/v1/process/definitions
/api/v2/process/definitions
```

---

## 11. 接口限流

使用令牌桶算法进行限流：

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 99
X-RateLimit-Reset: 1642234567
```

超过限制返回 429 状态码：

```json
{
  "code": 429,
  "message": "请求过于频繁，请稍后再试"
}
```
