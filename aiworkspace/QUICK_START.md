# LingFlow 流程管理系统 - 快速启动指南

## 项目概述

LingFlow 是一个基于 bpmn.js + Vue3 + Spring Boot + Flowable 的前后端分离流程管理系统，实现了基本的流程管理功能。

## 已实现功能

✅ **1. 流程图绘制** - 使用 bpmn.js 可视化绘制流程图
✅ **2. 流程发布** - 生成并发布流程模板到 Flowable 引擎
✅ **3. 流程启动** - 启动流程实例
✅ **4. 任务办理** - 查看和办理待办任务
✅ **5. 流程监控** - 查看运行中和已完结的流程

## 系统架构

```
┌─────────────────┐
│   前端 (Vue3)   │
│   Port: 3000    │
└────────┬────────┘
         │ HTTP
         ▼
┌─────────────────┐
│  后端 (Spring    │
│   Boot)         │
│   Port: 8080    │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│  PostgreSQL +   │
│  Flowable       │
└─────────────────┘
```

## 安装步骤

### 1. 数据库准备

#### 确保 PostgreSQL 服务已启动

确保 PostgreSQL 数据库服务已启动，并且数据库 `postgres` 已存在。

#### 配置数据库连接

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/postgres
    username: LINGFLOW
    password: LINGFLOW
    driver-class-name: org.postgresql.Driver
```

### 2. 启动后端服务

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

**首次启动时，Flowable 会自动创建所有需要的数据库表。**

后端成功启动后，会看到：
```
========================================
LingFlow 流程管理系统启动成功！
访问地址: http://localhost:8080
========================================
```

### 3. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

前端成功启动后，会看到：
```
VITE v5.x.x  ready in xxx ms

➜  Local:   http://localhost:3000/
➜  Network: http://xxx.xxx.xxx.xxx:3000/
```

## 使用指南

### 第一步：绘制流程图

1. 访问 http://localhost:3000
2. 点击左侧菜单的"流程设计器"
3. 在画布上绘制流程图：
   - 从左侧工具栏拖拽组件（开始事件、用户任务、结束事件等）
   - 使用连线工具连接各个节点
   - 双击节点可以编辑名称
4. 输入流程名称（如："请假审批流程"）
5. 点击"发布流程"按钮

**示例流程：**
- 开始事件
- 用户任务：提交申请
- 用户任务：经理审批
- 结束事件

### 第二步：管理流程

1. 点击左侧菜单的"流程管理"
2. 查看已发布的流程定义列表
3. 点击"启动流程"按钮创建流程实例
4. 可选输入业务Key（用于关联业务数据）
5. 点击"确定"启动流程

### 第三步：办理任务

1. 点击左侧菜单的"任务办理"
2. 查看待办任务列表
3. 点击"办理"按钮
4. 可选填写备注信息
5. 点击"确定"完成任务

### 第四步：监控流程

1. 点击左侧菜单的"流程监控"
2. 切换标签页查看：
   - **运行中流程**：显示当前正在运行的流程实例
   - **已完结流程**：显示已完成的流程实例
3. 点击"查看流程图"查看流程实例的执行状态（当前执行节点会高亮显示）

## API 接口说明

### 流程定义接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /process/deploy | 部署流程 |
| GET | /process/definitions | 获取流程定义列表 |
| DELETE | /process/definition/{deploymentId} | 删除流程定义 |

### 流程实例接口

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | /process/start/{processKey} | 启动流程实例 |
| GET | /process/running | 获取运行中的流程实例 |
| GET | /process/completed | 获取已完结的流程实例 |
| GET | /process/diagram/{processInstanceId} | 获取流程实例图 |

### 任务接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /task/list | 获取待办任务列表 |
| POST | /task/complete/{taskId} | 完成任务 |
| GET | /task/form/{taskId} | 获取任务表单 |

## 技术栈说明

### 前端
- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - JavaScript 的超集，提供静态类型检查
- **Element Plus** - 基于 Vue 3 的组件库
- **bpmn.js** - BPMN 2.0 建模工具包
- **Vue Router** - Vue.js 官方路由管理器
- **Pinia** - Vue 官方状态管理库
- **Axios** - HTTP 客户端
- **Vite** - 下一代前端构建工具

### 后端
- **Spring Boot 3.2.0** - Java 应用程序开发框架
- **Flowable 7.0.1** - 轻量级工作流和业务流程管理平台
- **PostgreSQL** - 关系型数据库
- **Lombok** - Java 库，通过注解简化 Java 代码

## 项目结构

```
lingflow/
├── frontend/                          # 前端项目
│   ├── src/
│   │   ├── api/                      # API 接口定义
│   │   │   ├── index.ts             # Axios 配置
│   │   │   └── process.ts          # 流程相关 API
│   │   ├── router/                   # 路由配置
│   │   │   └── index.ts            # 路由定义
│   │   ├── views/                    # 页面组件
│   │   │   ├── Designer.vue         # 流程设计器
│   │   │   ├── Process.vue          # 流程管理
│   │   │   ├── Task.vue             # 任务办理
│   │   │   └── Monitor.vue          # 流程监控
│   │   ├── App.vue                  # 根组件
│   │   ├── main.ts                  # 入口文件
│   │   └── main.css                 # 全局样式
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
│
└── backend/                           # 后端项目
    ├── src/main/java/com/lingflow/
    │   ├── controller/              # 控制器层
    │   │   ├── ProcessController.java    # 流程控制器
    │   │   └── TaskController.java       # 任务控制器
    │   ├── service/                 # 服务层
    │   │   └── ProcessService.java      # 流程服务
    │   ├── dto/                     # 数据传输对象
    │   │   ├── ProcessDefinitionVO.java
    │   │   ├── ProcessInstanceVO.java
    │   │   ├── TaskVO.java
    │   │   └── Result.java
    │   └── LingflowApplication.java # 主应用类
    ├── src/main/resources/
    │   └── application.yml          # 配置文件
    └── pom.xml                     # Maven 依赖
```

## 常见问题

### Q1: 后端启动失败，提示数据库连接错误

**A:** 检查以下内容：
1. PostgreSQL 服务是否启动
2. `application.yml` 中的数据库用户名和密码是否正确
3. 数据库 `postgres` 是否存在

### Q2: 前端页面无法访问后端接口

**A:** 检查以下内容：
1. 后端服务是否正常启动（访问 http://localhost:8080）
2. 查看 `vite.config.ts` 中的代理配置是否正确
3. 浏览器控制台是否有 CORS 错误

### Q3: Flowable 表未创建

**A:** 首次启动后端时，Flowable 会自动创建所有需要的表。如果没有创建：
1. 检查数据库连接是否正常
2. 查看后端日志中的错误信息
3. 确保 PostgreSQL 数据库用户有创建表的权限

### Q4: 流程图绘制器不显示

**A:** 检查以下内容：
1. 浏览器是否支持 bpmn.js（推荐使用 Chrome 或 Firefox）
2. 查看浏览器控制台是否有 JavaScript 错误
3. 确认 bpmn.js 相关依赖是否正确安装

## 扩展功能建议

当前系统实现了最基础的流程管理功能，可以根据需要进行扩展：

1. **用户认证** - 添加登录、注册、权限管理功能
2. **任务分配** - 实现任务指派、候选用户、候选组等功能
3. **流程变量** - 支持流程变量的传递和管理
4. **表单管理** - 集成动态表单，支持自定义表单字段
5. **流程历史** - 查看流程执行历史记录
6. **流程图导出** - 支持导出流程图为图片或 PDF
7. **多语言支持** - 支持中英文切换
8. **消息通知** - 集成邮件或即时消息通知

## 参考资源

- [Flowable 官方文档](https://www.flowable.com/open-source/docs)
- [bpmn.js 文档](https://bpmn.io/toolkit/bpmn-js/)
- [Vue 3 文档](https://cn.vuejs.org/)
- [Spring Boot 文档](https://spring.io/projects/spring-boot)

## 许可证

MIT License
