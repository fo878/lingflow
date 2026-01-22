# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此代码仓库中工作时提供指导。

## 项目概述

LingFlow 是一个基于 bpmn.js + Vue3 + Spring Boot + Flowable 的流程管理系统。项目采用前后端分离架构，并在 Flowable 之上构建了扩展系统以提供增强的工作流功能。

**核心技术栈：**
- **前端**: Vue 3 (Composition API), TypeScript, Element Plus, bpmn.js, Pinia, Vite
- **后端**: Spring Boot 3.1.6, Flowable 7.0.1, PostgreSQL, MyBatis-Plus 3.5.7
- **Java**: 17 (使用 `-parameters` 编译标志保留参数名称)

## 开发命令

### 后端 (Spring Boot + Maven)
```bash
cd /data/lingflow/backend

# 标准构建命令（编译、打包到本地仓库）
mvn clean install

# 跳过测试的快速构建（开发推荐）
mvn clean install -DskipTests

# 运行测试
mvn test

# 运行应用（启动在 8080 端口）
mvn spring-boot:run

# 仅打包（不安装到本地仓库）
mvn clean package

# IDEA 内置 Maven 命令（完整路径）
/opt/idea/idea-IU-253.29346.138/plugins/maven/lib/maven3/bin/mvn \
  -Didea.version=2025.3.1 \
  -Dmaven.ext.class.path=/opt/idea/idea-IU-253.29346.138/plugins/maven/lib/maven-event-listener.jar \
  -Djansi.passthrough=true \
  -Dstyle.color=always \
  -DskipTests=true \
  -Dmaven.repo.local=/home/liuyohao/.m2/repository \
  install \
  -f pom.xml
```

**IDEA Maven 命令参数说明**：
- `-Didea.version`: IDEA 版本号
- `-Dmaven.ext.class.path`: Maven 扩展监听器路径
- `-Djansi.passthrough=true`: ANSI 颜色输出透传
- `-Dstyle.color=always`: 始终启用彩色输出
- `-DskipTests=true`: 跳过测试执行（加速构建）
- `-Dmaven.repo.local`: 本地 Maven 仓库路径
- `-f pom.xml`: 指定 POM 文件

### 后端代码变更工作流

**重要**：每次修改后端代码后，必须执行以下工作流直到构建成功：

1. **执行构建**
   ```bash
   cd /data/lingflow/backend
   mvn clean install -DskipTests
   ```

2. **分析编译错误**
   - 如果构建失败，仔细阅读 Maven 输出的错误信息
   - 识别错误类型：编译错误、依赖冲突、类型不匹配等

3. **修复编译错误**
   - 根据错误信息定位问题文件和行号
   - 修复代码中的错误
   - 常见问题：
     - 缺少依赖：在 `pom.xml` 中添加依赖
     - 类型错误：修正变量类型、方法签名
     - 导入错误：添加正确的 import 语句
     - 注解错误：检查注解使用是否正确

4. **重新构建验证**
   ```bash
   mvn clean install -DskipTests
   ```

5. **循环直到成功**
   - 如果仍然失败，重复步骤 2-4
   - 直到看到 `BUILD SUCCESS` 消息

6. **可选：运行测试**
   ```bash
   mvn test
   ```

**构建成功的标志**：
```
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 前端 (Vue 3 + Vite)
```bash
cd /data/lingflow/frontend

# 安装依赖
npm install

# 开发服务器（启动在 3000 端口，代理到 8080）
npm run dev

# 类型检查并构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码检查和格式化
npm run lint        # 自动修复 lint 问题
npm run lint:check  # 仅检查
npm run format      # 使用 Prettier 格式化
npm run format:check
```

**注意**: Husky 已配置为 pre-commit hooks，配合 lint-staged 自动对暂存文件进行 lint 和格式化。

## 数据库

使用 **PostgreSQL**，数据库名称为 `postgres`。连接配置位于 `backend/src/main/resources/application.yml`：
- URL: `jdbc:postgresql://localhost:5432/postgres`
- 默认凭据: `lingflow/lingflow`
- Flowable 启动时通过 `flowable.database-schema-update: true` 自动创建表
- 自定义迁移文件位于 `backend/src/main/resources/db/migration/`（使用 Flyway 风格的版本命名：`V1__`, `V2__` 等）

**自定义表**（除 Flowable 标准表外）：
- `bpmn_element_extension` - 以 JSONB 格式存储 BPMN 元素自定义属性
- `bpmn_element_extension_history` - 扩展属性变更的审计日志
- `process_snapshot` - 流程实例状态快照
- `process_comment` - 流程实例评论
- `notification_record` - 通知记录

## 架构设计

### 后端结构

```
com.lingflow/
├── config/           # Spring 配置（CORS、MyBatis-Plus、异常处理）
├── controller/       # REST API 端点
├── dto/             # 数据传输对象
├── entity/          # JPA/MyBatis 实体
├── enums/           # 枚举类
├── exception/       # 自定义异常
├── extension/       # 扩展系统（见下文）
├── mapper/          # MyBatis 映射器，XML 位于 resources/mapper/
├── repository/      # 数据访问层
├── service/         # 业务逻辑
└── util/            # 工具类
```

### 扩展系统架构

后端在 Flowable 之上实现了一个复杂的扩展框架，包含三个核心模式：

**1. 事件监听器模式** (`extension/event/`)
- `ProcessEventListener` 接口用于响应工作流生命周期事件
- `ProcessEventManager` 协调所有监听器
- 实现类：`ProcessStartListener`, `ProcessEndListener`, `TaskCreatedListener`, `TaskCompletedListener`, `TaskAssignedListener`
- 支持每种事件类型的 `onBefore()` 和 `onAfter()` 钩子
- 通过 `@Order` 注解实现有序执行

**2. 处理器链模式** (`extension/handler/`)
- `TaskCompletionHandler` 接口用于处理节点到节点的流转
- `TaskCompletionHandlerChain` 编排责任链
- 处理器支持特定节点类型的流转（用户任务 → 服务任务、网关、子流程等）
- 每个处理器实现：`preHandle()`, `handle()`, `postHandle()` 和可选的 `rollback()`
- 上下文对象 (`TaskCompletionContext`) 在链中传递状态
- 核心处理器：`ServiceTaskHandler`, `UserTaskToUserTaskHandler`, `GatewayHandler`, `MultiInstanceHandler`, `SubProcessHandler`, `ScriptTaskHandler`, `ReceiveTaskHandler`

**3. 包装器模式** (`extension/wrapper/`)
- `FlowableServiceWrapper` 接口用于横切关注点
- 使用 before/after/exception 钩子包装 Flowable API 调用
- 示例：`PerformanceMonitoringWrapper` 跟踪 API 调用耗时
- 无需 Spring AOP 即可实现类似 AOP 的功能

这个扩展系统允许在关键的工作流点注入自定义逻辑，而无需修改 Flowable 核心。

### 前端结构

```
src/
├── api/             # Axios API 客户端
│   ├── index.ts     # 基础 API 配置
│   ├── process.ts   # 流程相关 API
│   └── extended.ts  # 扩展 API（评论、通知、统计）
├── components/      # 可复用的 Vue 组件
├── router/          # Vue Router 配置
├── views/           # 按功能组织的页面组件
│   ├── process/     # 流程设计器和管理
│   ├── task/        # 任务处理
│   ├── monitor/     # 流程监控
│   ├── statistics/  # 流程统计
│   └── notification/ # 通知管理
├── App.vue          # 根组件
└── main.ts          # 应用入口
```

**API 代理**: 前端开发服务器将 `/api/*` 请求代理到 `http://localhost:8080`（见 `vite.config.ts:18`）。

## 核心 API 端点

### 流程定义
- `POST /process/deploy` - 部署流程定义
- `GET /process/definitions` - 获取流程定义列表
- `DELETE /process/definition/{deploymentId}` - 删除流程定义

### 流程实例
- `POST /process/start/{processKey}` - 启动流程实例
- `GET /process/running` - 获取运行中的流程实例
- `GET /process/completed` - 获取已完结的流程实例
- `GET /process/diagram/{processInstanceId}` - 获取流程实例图

### 任务
- `GET /task/list` - 获取用户任务列表
- `POST /task/complete/{taskId}` - 完成任务
- `GET /task/form/{taskId}` - 获取任务表单数据

### 扩展功能
- 评论、通知、统计和监控端点（见 `ProcessCommentController`, `ProcessNotificationController`, `ProcessStatisticsController`）

## MyBatis 集成

**重要**: 项目明确使用 MyBatis 3.5.16（通过 dependencyManagement 管理）以确保与 MyBatis-Plus 3.5.7 的兼容性，避免与 Flowable 的 MyBatis 版本冲突。

`application.yml` 中的配置：
- Mapper XML 位置：`classpath:mapper/*.xml`
- 类型别名：`com.lingflow.entity`
- 启用下划线到驼峰映射
- 启用 SQL 日志

## 开发说明

### 添加新扩展
要添加新的扩展点：
1. **事件监听器**: 实现 `ProcessEventListener`，通过 `supports()` 支持特定事件类型
2. **任务处理器**: 实现 `TaskCompletionHandler`，支持特定节点类型流转
3. **包装器**: 实现 `FlowableServiceWrapper` 用于横切关注点
所有扩展组件通过 Spring 的 `@Component` 扫描自动发现，并通过 `@Order` 排序。

### 数据库迁移
将新的 SQL 文件添加到 `backend/src/main/resources/db/migration/`，使用递增的版本号（V6__, V7__ 等），遵循 Flyway 命名规范。

### 前端 API 集成
API 调用集中在 `src/api/` 中。将新端点添加到相应文件，并使用已配置的 axios 实例处理基础 URL。

### CORS 配置
开发环境 CORS 配置为仅允许来自 `http://localhost:3000` 的请求（见 `WebConfig.java:16`）。如需添加其他源，请更新此配置。

### 编码风格

#### 后端 (Java) - 遵循阿里巴巴 Java 编码规范

项目严格遵守《阿里巴巴 Java 开发手册》规范，以下是关键要点：

**命名规范**
- 类名使用 UpperCamelCase（大驼峰），如 `ProcessController`, `TaskCompletionHandler`
- 方法名和变量名使用 lowerCamelCase（小驼峰），如 `getProcessDefinitions()`, `processInstanceId`
- 常量名全部大写，单词间用下划线分隔，如 `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE`
- 包名全部小写，使用点分隔符，如 `com.lingflow.controller`
- DTO/Entity/Repository 等以类型结尾，如 `ProcessDefinitionDTO`, `ProcessInstanceEntity`

**代码风格**
- 使用 4 个空格缩进，禁止使用 Tab
- 大括号左不换行（K&R 风格）
```java
// ✅ 正确
public void processTask() {
    if (condition) {
        doSomething();
    }
}

// ❌ 错误
public void processTask()
{
    if (condition)
    {
        doSomething();
    }
}
```
- 代码行宽不超过 120 个字符
- 方法参数不超过 5 个，超过时使用对象封装
- 方法总行数不超过 80 行
- 类总行数不超过 500 行

**OOP 规范**
- 避免在循环中进行数据库查询或网络调用
- 集合初始化时指定大小，如 `new ArrayList<>(32)`
- 使用 `Collection.isEmpty()` 检查空集合，而不是 `size() == 0`
- 使用 `String.valueOf()` 而不是 `toString()` 避免空指针
- 使用 `Objects.equals()` 进行对象比较
- 所有 POJO 类属性必须添加 `@SerializedName` 或使用 Jackson 注解

**异常处理**
- 捕获异常后不要什么都不做，至少记录日志
- 不要使用 `catch (Exception e)` 捕获通用异常
- 使用 `try-with-resources` 关闭资源
- 抛出异常时包含清晰的错误信息

**注释规范**
- 类、接口、public 方法必须添加 Javadoc 注释
- 复杂业务逻辑添加行内注释说明
- 注释掉的代码必须说明原因，否则直接删除
- Javadoc 必须包含 `@param`, `@return`, `@throws` 标签

**日志规范**
- 使用 Slf4j + Logback
- 日志级别：ERROR > WARN > INFO > DEBUG > TRACE
- 生产环境禁止使用 DEBUG 日志
- 使用占位符而不是字符串拼接：
```java
// ✅ 正确
log.info("Processing task {} for process {}", taskId, processId);

// ❌ 错误
log.info("Processing task " + taskId + " for process " + processId);
```

**并发规范**
- 线程池必须通过 `ThreadPoolExecutor` 创建，不允许使用 `Executors` 工具类
- 使用 `ThreadLocal` 必须在 finally 中清理
- 使用 `CountDownLatch`, `Semaphore` 等并发工具时注意资源释放
- 避免使用 `stop()`, `suspend()`, `resume()` 等已废弃方法

**数据库规范**
- 表名、字段名使用小写字母加下划线，如 `process_definition_id`
- 必须有主键 `id`，推荐使用 `VARCHAR(36)` 存储 UUID
- 必须有 `created_time`, `updated_time` 字段
- 字段允许为空时使用 `NULL`，禁止使用空字符串表示 NULL
- 使用 `BIGINT` 存储主键而非 `INT`
- 索引命名：`idx_表名_字段名`，如 `idx_process_definition_key`
- 唯一索引命名：`uk_表名_字段名`

**Spring Boot 特定规范**
- Controller 只处理请求响应，业务逻辑在 Service 层
- 使用 `@Transactional` 时指定 rollbackFor
- 依赖注入使用构造器注入，而非字段注入
- 配置属性使用 `@ConfigurationProperties`
- 避免在循环中调用 Bean 方法

#### 前端 (Vue 3 + TypeScript)

**命名规范**
- 组件文件名使用 PascalCase，如 `ProcessDesigner.vue`, `TaskList.vue`
- 组件 name 属性使用多单词，避免与 HTML 标签冲突
- TypeScript 接口使用 PascalCase，如 `ProcessDefinition`, `TaskInfo`
- 变量、函数使用 camelCase
- 常量使用 UPPER_SNAKE_CASE
- 私有变量/方法使用下划线前缀 `_internalState`

**Vue 3 Composition API 规范**
- 优先使用 `<script setup>` 语法
- 组合式函数（Composables）以 `use` 开头，如 `useProcessApi()`, `useTaskStore()`
- 响应式变量优先使用 `ref()`，对象使用 `reactive()`
- 解构 props 时使用 `toRefs()` 保持响应性
- 组件事件命名使用 kebab-case，如 `@task-complete`, `@process-deploy`

**TypeScript 规范**
- 避免使用 `any`，优先使用具体类型或 `unknown`
- 函数参数和返回值必须显式声明类型
- 使用接口定义数据结构，而非类型别名（除非是联合类型）
- 使用枚举或 `as const` 定义常量
- 严格开启 `strict: true` 模式

**代码风格**
- 使用单引号（ESLint 配置）
- 使用 2 个空格缩进
- 语句末尾添加分号
- 对象和数组最后一项保留逗号
- 模板字符串优先于字符串拼接
- 优先使用可选链 `?.` 和空值合并 `??` 运算符

**组件设计**
- 单个组件文件不超过 300 行
- Props 必须定义类型和默认值
- 使用 `defineEmits<T>()` 定义事件类型
- 避免在模板中编写复杂逻辑，提取到计算属性或方法
- 使用 `v-bind="$attrs"` 透传属性
- 使用 `v-for` 时必须提供 `:key`

**API 调用规范**
- 所有 API 调用集中在 `src/api/` 目录
- 使用 async/await 而非 Promise 链
- 统一错误处理，使用 try-catch 捕获异常
- 加载状态管理：使用 `loading` 变量和 ElMessage 提示

**样式规范**
- 优先使用 Element Plus 组件库
- 样式使用 scoped 作用域
- 使用 CSS 变量定义主题颜色
- 响应式布局使用 Element Plus 的栅格系统

#### 通用规范

**Git 提交规范**
- 提交信息格式：`<type>(<scope>): <subject>`
- type 类型：`feat`, `fix`, `docs`, `style`, `refactor`, `test`, `chore`
- 示例：`feat(process): add process deployment feature`
- 单次提交只做一件事

**代码审查要点**
- 代码可读性和可维护性
- 是否遵循项目架构设计
- 是否有潜在的性能问题
- 异常处理是否完善
- 是否存在安全漏洞（SQL 注入、XSS 等）
- 测试覆盖率是否足够

**性能优化原则**
- 避免不必要的循环嵌套
- 数据库查询避免 N+1 问题
- 前端使用虚拟滚动处理长列表
- 图片资源压缩和懒加载
- 使用缓存减少重复计算

