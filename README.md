# LingFlow 流程管理系统

基于 bpmn.js + Vue3 + Spring Boot + Flowable 的前后端分离流程管理系统

## 功能特性

1. **流程图绘制** - 使用 bpmn.js 可视化绘制流程图
2. **流程发布** - 生成并发布流程模板
3. **流程启动** - 启动流程实例
4. **任务办理** - 查看和办理待办任务
5. **流程监控** - 查看运行中和已完结的流程

## 技术栈

### 前端
- Vue 3
- TypeScript
- Element Plus
- bpmn.js
- Vue Router
- Pinia
- Axios
- Vite

### 后端
- Spring Boot 3.2.0
- Flowable 7.0.1
- PostgreSQL
- Lombok

## 项目结构

```
lingflow/
├── frontend/                 # 前端项目
│   ├── src/
│   │   ├── api/            # API 接口
│   │   ├── router/         # 路由配置
│   │   ├── views/          # 页面组件
│   │   ├── App.vue         # 根组件
│   │   └── main.ts         # 入口文件
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
│
└── backend/                  # 后端项目
    ├── src/main/java/com/lingflow/
    │   ├── controller/     # 控制器
    │   ├── service/        # 服务层
    │   ├── dto/            # 数据传输对象
    │   └── LingflowApplication.java
    ├── src/main/resources/
    │   └── application.yml # 配置文件
    └── pom.xml             # Maven 依赖
```

## 快速开始

### 前置要求

- Node.js 16+
- Java 17+
- Maven 3.6+
- PostgreSQL

### 数据库配置

1. 确保 PostgreSQL 数据库服务已启动，并且数据库 `postgres` 已创建。

2. 修改 `backend/src/main/resources/application.yml` 中的数据库配置：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: LINGFLOW
    password: LINGFLOW
    driver-class-name: org.postgresql.Driver
```

### 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动

### 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:3000` 启动

## 使用说明

### 1. 绘制流程图

1. 访问"流程设计器"页面
2. 使用 bpmn.js 工具栏绘制流程图
3. 输入流程名称
4. 点击"发布流程"按钮

### 2. 流程管理

1. 访问"流程管理"页面
2. 查看已发布的流程定义
3. 点击"启动流程"按钮创建流程实例
4. 点击"删除"按钮删除流程定义

### 3. 任务办理

1. 访问"任务办理"页面
2. 查看待办任务列表
3. 点击"办理"按钮完成任务
4. 可选填写备注信息

### 4. 流程监控

1. 访问"流程监控"页面
2. 查看"运行中流程"标签页：显示正在运行的流程实例
3. 查看"已完结流程"标签页：显示已完成的流程实例
4. 点击"查看流程图"查看流程实例的执行状态

## API 接口

### 流程定义相关

- `POST /process/deploy` - 部署流程
- `GET /process/definitions` - 获取流程定义列表
- `DELETE /process/definition/{deploymentId}` - 删除流程定义

### 流程实例相关

- `POST /process/start/{processKey}` - 启动流程实例
- `GET /process/running` - 获取运行中的流程实例
- `GET /process/completed` - 获取已完结的流程实例
- `GET /process/diagram/{processInstanceId}` - 获取流程实例图

### 任务相关

- `GET /task/list` - 获取待办任务列表
- `POST /task/complete/{taskId}` - 完成任务
- `GET /task/form/{taskId}` - 获取任务表单

## 示例流程

创建一个简单的请假审批流程：

1. 在流程设计器中绘制以下节点：
   - 开始事件
   - 用户任务：提交申请
   - 用户任务：经理审批
   - 结束事件

2. 使用连线连接各节点，形成一个完整的流程

3. 发布流程

4. 启动流程实例

5. 在任务办理页面完成各节点任务

## 常见问题

### 1. 数据库连接失败

检查 PostgreSQL 服务是否启动，以及配置文件中的用户名和密码是否正确。

### 2. Flowable 表未创建

首次启动时，Flowable 会自动创建所需的表，请确保数据库连接正常。

### 3. 前端无法连接后端

检查后端是否正常启动，以及前端的代理配置是否正确。

## 开发说明

### 前端开发

- 前端代码位于 `frontend` 目录
- 使用 Vue 3 Composition API
- 使用 TypeScript 进行类型检查
- API 接口定义在 `src/api` 目录

### 后端开发

- 后端代码位于 `backend` 目录
- 使用 Spring Boot 3.x
- Flowable 自动配置，无需手动配置
- RESTful API 风格

## 许可证

MIT License
