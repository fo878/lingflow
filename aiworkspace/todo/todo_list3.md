# LingFlow 项目优化任务清单 (TODO List 3)

## 任务概述

根据design_list1.md（流程模板历史快照功能）和design_list2.md（BPMN元素扩展属性管理系统）的设计方案，对lingflow项目进行全面优化和功能扩展。

---

## 第一阶段：数据库设计与实现

### 1.1 流程快照相关表设计
- [ ] 创建 `process_snapshot` 表
  - 字段：id, process_definition_key, snapshot_name, snapshot_version, bpmn_xml, description, creator, created_time
  - 创建索引：idx_process_key, idx_created_time

### 1.2 BPMN元素扩展属性相关表设计
- [ ] 创建 `bpmn_element_extension` 表
  - 字段：id, process_definition_id, element_id, element_type, extension_attributes (JSON), version, created_time, updated_time
  - 创建索引：idx_process_element, idx_element_type
- [ ] 创建 `bpmn_element_extension_history` 表
  - 字段：id, extension_id, process_definition_id, element_id, element_type, extension_attributes (JSON), version, operation_type, operation_time
  - 创建索引：idx_extension_id, idx_process_element

### 1.3 数据库迁移脚本
- [ ] 编写SQL创建脚本
- [ ] 编写数据库表结构验证脚本
- [ ] 测试数据库表创建

---

## 第二阶段：后端实现 - 流程快照功能

### 2.1 实体类创建
- [ ] 创建 `ProcessSnapshot.java` 实体类
  - 包含所有必要的Lombok注解
  - 字段类型使用LocalDateTime

### 2.2 数据访问层
- [ ] 创建 `ProcessSnapshotRepository.java` 接口
  - save() 方法
  - findByProcessDefinitionKey() 方法
  - findById() 方法
  - findLatestByProcessDefinitionKey() 方法
  - deleteById() 方法
- [ ] 创建 `ProcessSnapshotRepository.xml` MyBatis映射文件
  - 实现所有SQL映射

### 2.3 服务层实现
- [ ] 在 `ProcessService.java` 中添加快照管理方法
  - createProcessSnapshot() - 创建流程快照
  - getProcessSnapshots() - 获取流程快照列表
  - rollbackToSnapshot() - 回滚到指定快照
  - deleteSnapshot() - 删除快照

### 2.4 控制器层实现
- [ ] 创建 `SnapshotController.java` 控制器
  - POST /snapshot/create - 创建快照
  - GET /snapshot/list/{processDefinitionKey} - 获取快照列表
  - POST /snapshot/rollback/{snapshotId} - 回滚快照
  - DELETE /snapshot/{snapshotId} - 删除快照

---

## 第三阶段：后端实现 - BPMN元素扩展属性管理

### 3.1 实体类和枚举创建
- [ ] 创建 `BpmnElementExtension.java` 实体类
  - 使用JsonNode存储extensionAttributes
- [ ] 创建 `BpmnElementExtensionHistory.java` 实体类
- [ ] 创建 `BpmnElementType.java` 枚举类
  - 定义所有BPMN元素类型：USER_TASK, SERVICE_TASK, GATEWAY, EVENT等

### 3.2 DTO类创建
- [ ] 创建 `BpmnElementExtensionDTO.java`
- [ ] 创建 `ElementExtensionQueryResult.java`

### 3.3 数据访问层
- [ ] 创建 `BpmnElementExtensionRepository.java` 接口
  - save() 方法
  - update() 方法
  - findByProcessAndElement() 方法
  - findByProcessDefinitionId() 方法
  - findByElementType() 方法
  - deleteByProcessAndElement() 方法
  - saveHistory() 方法
  - findHistoryByExtensionId() 方法
- [ ] 创建 `BpmnElementExtensionRepository.xml` 映射文件
  - 配置JSON类型处理器
  - 实现所有SQL映射

### 3.4 服务层实现
- [ ] 在 `ProcessService.java` 中添加扩展属性管理方法
  - saveElementExtension() - 保存BPMN元素扩展属性
  - getElementExtension() - 获取BPMN元素扩展属性
  - batchSaveElementExtensions() - 批量保存扩展属性
  - getAllElementExtensions() - 获取流程定义的所有扩展属性
  - deleteElementExtension() - 删除BPMN元素扩展属性

### 3.5 控制器层实现
- [ ] 在 `ProcessController.java` 中添加扩展属性管理接口
  - POST /process/extension - 保存扩展属性
  - GET /process/extension/{processDefinitionId}/{elementId} - 获取扩展属性
  - POST /process/extensions/batch - 批量保存扩展属性
  - GET /process/extensions/{processDefinitionId} - 获取所有扩展属性
  - DELETE /process/extension/{processDefinitionId}/{elementId} - 删除扩展属性

---

## 第四阶段：前端实现 - 流程快照功能

### 4.1 API接口定义
- [ ] 在 `frontend/src/api/process.ts` 中添加快照相关API
  - createProcessSnapshot()
  - getProcessSnapshots()
  - rollbackToSnapshot()
  - deleteSnapshot()

### 4.2 流程设计器界面更新
- [ ] 修改 `frontend/src/views/process/Designer.vue`
  - 添加快照管理按钮组
  - 添加快照对话框（创建、查看快照列表）
  - 添加创建快照对话框（表单输入）
  - 实现快照列表展示（表格形式）
  - 实现快照回滚和删除功能
  - 添加快照相关交互逻辑和确认对话框

### 4.3 流程管理界面更新
- [ ] 在 `frontend/src/views/process/Index.vue` 中添加快照管理入口
  - 在操作列添加"快照管理"按钮
  - 点击后打开快照管理对话框

---

## 第五阶段：前端实现 - BPMN元素扩展属性管理

### 5.1 API接口定义
- [ ] 创建 `frontend/src/api/extension.ts`
  - saveElementExtension()
  - getElementExtension()
  - batchSaveElementExtensions()
  - getAllElementExtensions()
  - deleteElementExtension()

### 5.2 流程设计器属性面板更新
- [ ] 修改 `frontend/src/views/process/Designer.vue` 的属性面板
  - 根据元素类型动态显示不同的属性配置
  - 实现用户任务属性配置（assignee, candidateUsers, candidateGroups, formKey等）
  - 实现服务任务属性配置（implementation, expression, delegateExpression等）
  - 实现网关属性配置（gatewayType, default等）
  - 实现事件属性配置（messageRef, timerEventDefinition等）
  - 添加元素类型判断方法（isUserTask, isServiceTask, isGateway, isEvent）
  - 使用watch监听扩展属性变化并实时保存

### 5.3 加载和保存扩展属性
- [ ] 实现扩展属性的加载逻辑
  - 在加载现有流程时同步加载扩展属性
  - 将扩展属性映射到BPMN元素
- [ ] 实现扩展属性的实时保存
  - 使用watch深度监听selectedElement变化
  - 自动保存扩展属性到后端

---

## 第六阶段：集成与测试(跳过)

### 6.1 后端集成测试
- [ ] 测试流程快照功能
  - 测试创建快照
  - 测试获取快照列表
  - 测试回滚快照
  - 测试删除快照
- [ ] 测试BPMN元素扩展属性功能
  - 测试保存扩展属性
  - 测试获取扩展属性
  - 测试批量保存扩展属性
  - 测试删除扩展属性
  - 测试JSON数据正确处理

### 6.2 前端集成测试
- [ ] 测试流程设计器快照功能
  - 测试快照管理对话框
  - 测试创建快照
  - 测试快照列表展示
  - 测试快照回滚
  - 测试快照删除
- [ ] 测试流程设计器扩展属性功能
  - 测试不同元素类型的属性面板
  - 测试属性实时保存
  - 测试扩展属性加载
  - 测试扩展属性编辑

### 6.3 端到端测试
- [ ] 测试完整流程
  - 创建流程 -> 配置扩展属性 -> 创建快照 -> 回滚快照 -> 验证数据
  - 编辑现有流程 -> 修改扩展属性 -> 保存 -> 验证数据
  - 流程发布 -> 启动实例 -> 验证扩展属性生效

### 6.4 性能测试
- [ ] 测试大量快照情况下的查询性能
- [ ] 测试大量扩展属性情况下的保存和查询性能
- [ ] 验证数据库索引效果

---

## 第七阶段：文档与部署

### 7.1 代码文档
- [ ] 为所有新增类添加JavaDoc注释
- [ ] 为所有新增方法添加注释说明
- [ ] 为复杂业务逻辑添加代码注释

### 7.2 用户文档
- [ ] 更新QUICK_START.md，添加新功能使用说明
- [ ] 创建流程快照功能使用指南
- [ ] 创建BPMN元素扩展属性配置指南

### 7.3 部署准备
- [ ] 验证数据库迁移脚本
- [ ] 验证前端构建配置
- [ ] 验证后端打包配置
- [ ] 准备部署说明文档

---

## 第八阶段：代码质量保证

### 8.1 代码规范检查
- [ ] 后端代码符合Java编码规范
- [ ] 前端代码符合TypeScript/Vue编码规范
- [ ] 检查代码风格一致性

### 8.2 错误处理
- [ ] 完善异常处理逻辑
- [ ] 添加用户友好的错误提示
- [ ] 记录关键操作日志

### 8.3 安全性检查
- [ ] 验证SQL注入防护
- [ ] 验证XSS防护
- [ ] 验证权限控制

---

## 优先级说明

### 高优先级（P0）
- 数据库表设计和创建
- 后端核心功能实现
- 前端流程设计器界面更新

### 中优先级（P1）
- API接口实现
- 扩展属性管理功能
- 集成测试

### 低优先级（P2）
- 性能优化
- 文档完善
- 代码注释

---

## 预期成果

完成后，lingflow项目将具备以下新功能：

1. **流程快照管理**
   - 可以为任意流程定义创建快照
   - 可以查看历史快照列表
   - 可以快速回滚到任意历史快照
   - 支持快照版本管理

2. **BPMN元素扩展属性管理**
   - 为不同类型的BPMN元素提供差异化属性配置
   - 支持用户任务、服务任务、网关、事件等多种元素类型
   - 使用JSON格式灵活存储扩展属性
   - 支持扩展属性的历史追踪和版本管理

3. **系统整体优化**
   - 提升流程管理系统的灵活性和可维护性
   - 改善用户体验
   - 增强系统的可扩展性

---

## 注意事项

1. **数据库兼容性**：确保SQL脚本支持PostgreSQL
2. **事务管理**：涉及多表操作的方法需要添加@Transactional注解
3. **JSON处理**：确保前后端JSON数据格式一致
4. **版本控制**：所有数据库操作都需要考虑版本号管理
5. **并发控制**：快照和扩展属性操作需要考虑并发场景
6. **错误提示**：提供清晰的中英文错误提示
7. **向后兼容**：确保新功能不影响现有功能
