# Bug修复计划 (todo_list4)

## Bug分析

### Bug 1 & 4: 快照接口报错
**问题描述**: 
- URL: `http://localhost:8080/snapshot/list/null`
- 错误: `Invalid bound statement (not found): com.lingflow.repository.ProcessSnapshotRepository.findByProcessDefinitionKey`

**根本原因**: 
1. 前端在调用快照列表接口时，processName可能为null，导致URL路径为`/snapshot/list/null`
2. MyBatis mapper可能未正确扫描到ProcessSnapshotRepository.xml

### Bug 2: 流程编辑无法正常显示
**问题描述**:
- URL: `http://localhost:8080/process/definition/xml/007c4ca5-e237-11f0-9a7f-1e63a267e8f1`
- 错误: `流程定义不存在`
- 前端错误: `Cannot read properties of null (reading 'bpmnXml')`

**根本原因**:
1. 后端返回的data.data为null，导致前端访问bpmnXml时出错
2. 可能是流程定义ID不存在或已删除

### Bug 3: 新建流程不应有预置元素
**问题描述**: 新建流程设计器默认显示开始节点、任务节点和结束节点，应该显示空白画布

**根本原因**: Designer.vue中的initialXML包含预置元素

### Bug 5: 数据库权限问题
**问题描述**: 通过Flyway迁移脚本创建的表无法访问，提示"对表 process_snapshot 权限不够"

**根本原因**: 
1. 数据库用户lingflow可能没有足够的权限
2. Flyway创建的表所有权可能属于其他用户

---

## 修复计划

### Phase 1: 配置和权限修复
- [ ] 修复1.1: 检查并配置MyBatis mapper扫描路径
- [ ] 修复1.2: 解决数据库权限问题，给lingflow用户授予必要权限
- [ ] 修复1.3: 确保Flyway创建的表有正确的所有权

### Phase 2: 后端修复
- [ ] 修复2.1: 修改ProcessService.getProcessDefinitionXml()，确保返回值不为null
- [ ] 修复2.2: 添加更详细的错误日志，便于调试
- [ ] 修复2.3: 修复快照查询时的null值处理

### Phase 3: 前端修复
- [ ] 修复3.1: 修改Designer.vue的initialXML，改为空白流程
- [ ] 修复3.2: 在快照查询前检查processName是否为空
- [ ] 修复3.3: 添加错误处理，防止访问null对象的属性
- [ ] 修复3.4: 改进流程加载失败的用户提示

### Phase 4: 测试验证
- [ ] 测试4.1: 测试新建流程是否为空白画布
- [ ] 测试4.2: 测试编辑已有流程是否正常显示
- [ ] 测试4.3: 测试快照创建和列表查询
- [ ] 测试4.4: 测试数据库表访问是否正常

---

## 修复顺序说明

1. **优先修复数据库权限问题** (Bug 5)，因为这是基础问题，影响其他功能
2. **修复MyBatis配置** (Bug 1 & 4)，确保快照功能可用
3. **修复后端返回值处理** (Bug 2)，防止前端null引用错误
4. **修复前端初始流程** (Bug 3)，符合用户期望
5. **全面测试**所有修复的功能

---

## 预期结果

修复完成后：
1. 快照功能正常工作，可以创建、查看、回滚和删除快照
2. 流程编辑器可以正确加载已有流程定义
3. 新建流程显示空白画布，用户可以自由设计
4. 数据库表访问正常，无权限错误
5. 所有错误都有友好的用户提示
