# LingFlow Backend 单元测试

## 测试概览

已为 LingFlow 后端项目创建了全面的单元测试，覆盖了所有 Controller 和 Service 层。

### 测试统计

- **测试类总数**: 18 个
- **Controller 测试**: 8 个
- **Service 测试**: 10 个
- **测试代码行数**: 约 4,092 行

## 测试文件列表

### Controller 层测试 (8个)

1. **TaskControllerTest** - 任务控制器测试
2. **ProcessControllerTest** - 流程控制器测试
3. **ProcessVariableControllerTest** - 流程变量控制器测试
4. **ProcessMonitorControllerTest** - 流程监控控制器测试
5. **SnapshotControllerTest** - 流程快照控制器测试
6. **ProcessCommentControllerTest** - 流程评论控制器测试
7. **ProcessNotificationControllerTest** - 流程通知控制器测试
8. **ProcessStatisticsControllerTest** - 流程统计控制器测试

### Service 层测试 (10个)

1. **ProcessServiceTest** - 流程核心服务测试
2. **ExtendedTaskServiceTest** - 扩展任务服务测试
3. **ExtendedRuntimeServiceTest** - 扩展运行时服务测试
4. **ExtendedRepositoryServiceTest** - 扩展仓储服务测试
5. **ExtendedHistoryServiceTest** - 扩展历史服务测试
6. **ProcessVariableServiceTest** - 流程变量服务测试
7. **ProcessCommentServiceTest** - 流程评论服务测试
8. **ProcessNotificationServiceTest** - 流程通知服务测试
9. **ProcessMonitorServiceTest** - 流程监控服务测试
10. **ProcessStatisticsServiceTest** - 流程统计服务测试

## 如何运行测试

### 前提条件

确保已安装以下工具：
- Java JDK 17 或更高版本
- Maven 3.6+ 或 Gradle（如果使用 Gradle 构建）

### 运行所有测试

```bash
# 使用 Maven
mvn clean test

# 或使用 Gradle（如果适用）
gradle test
```

### 运行特定测试类

```bash
# 运行单个Controller测试
mvn test -Dtest=TaskControllerTest

# 运行单个Service测试
mvn test -Dtest=ProcessServiceTest

# 运行特定包的所有测试
mvn test -Dtest=com.lingflow.controller.*
mvn test -Dtest=com.lingflow.service.*
```

### 生成测试报告

```bash
# Maven 会自动生成测试报告
mvn clean test

# 报告位置:
# - HTML: target/surefire-reports/index.html
# - XML: target/surefire-reports/
```

## 测试技术栈

- **JUnit 5** - 测试框架
- **Mockito** - Mock 框架
- **Spring Boot Test** - Spring Boot 集成测试支持
- **MockMvc** - Controller 层测试
- **AssertJ** - 断言库（通过 JUnit 5）

## 测试配置

测试配置文件位于: `src/test/resources/application-test.yml`

使用 H2 内存数据库进行测试，配置包括:
- 数据源配置
- Flowable 引擎配置
- 日志配置

## 测试覆盖范围

### Controller 层测试覆盖

- ✅ HTTP 请求/响应测试
- ✅ 请求参数验证
- ✅ 异常处理测试
- ✅ REST API 端点测试
- ✅ JSON 序列化/反序列化

### Service 层测试覆盖

- ✅ 业务逻辑测试
- ✅ 数据访问层 Mock
- ✅ 事务处理测试
- ✅ 异常场景测试
- ✅ 边界条件测试

## 测试最佳实践

1. **隔离性**: 每个测试方法独立运行，不依赖其他测试
2. **Mock**: 使用 Mockito 隔离外部依赖
3. **可读性**: 测试方法命名清晰，遵循 `test{MethodName}_{Scenario}` 格式
4. **覆盖率**: 测试涵盖正常流程、异常流程和边界条件
5. **维护性**: 使用 `@BeforeEach` 初始化测试数据

## 持续集成

建议在 CI/CD 流程中运行测试:

```yaml
# 示例 GitHub Actions 配置
- name: Run Tests
  run: mvn clean test

- name: Generate Test Report
  if: always()
  uses: actions/upload-artifact@v2
  with:
    name: test-report
    path: target/surefire-reports/
```

## 常见问题

### Q: 测试运行失败？
A: 检查以下几点:
1. Java 版本是否为 17+
2. 依赖是否正确下载
3. 测试配置文件是否正确

### Q: 如何跳过测试？
```bash
mvn clean install -DskipTests
```

### Q: 如何查看详细测试日志？
```bash
mvn clean test -X
```

## 后续改进建议

1. 添加集成测试（使用@SpringBootTest）
2. 添加端到端测试
3. 增加测试覆盖率报告（JaCoCo）
4. 添加性能测试
5. 添加API文档测试（Swagger）

## 联系方式

如有问题或建议，请联系开发团队。

---
生成时间: 2026-01-16
测试框架: JUnit 5 + Mockito + Spring Boot Test
