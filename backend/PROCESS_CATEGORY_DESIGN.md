# 流程分类功能设计方案（增强版）

## 一、需求概述

实现流程模板的分类管理功能，允许用户：
1. 创建树形结构的分类（文件夹），支持多租户
2. 将流程模板归类到不同的分类下
3. 在流程列表中以树形结构展示分类
4. 在新建/编辑流程时选择分类
5. **支持模糊搜索流程模板，返回分类路径并定位到树节点**
6. **支持分类图标、搜索、拖拽排序等扩展功能**

## 二、数据库表设计

### 2.1 流程分类表 (process_category)

```sql
-- 流程分类表（增强版：支持多租户、路径记录）
CREATE TABLE IF NOT EXISTS process_category (
    id VARCHAR(36) PRIMARY KEY,              -- 分类ID (UUID)
    name VARCHAR(100) NOT NULL,              -- 分类名称
    code VARCHAR(50) NOT NULL,               -- 分类编码
    parent_id VARCHAR(36),                   -- 父分类ID (NULL表示根节点)
    path VARCHAR(1000),                      -- 分类路径（逗号分隔的ID，如：root,parent1,child1）
    level INT DEFAULT 0,                     -- 层级深度（根节点为0）
    description VARCHAR(500),                -- 分类描述
    sort_order INT DEFAULT 0,                -- 排序序号
    icon VARCHAR(100),                       -- 图标（支持 Element Plus 图标名或 URL）

    -- 多租户字段
    app_id VARCHAR(50),                      -- 应用ID
    context_id VARCHAR(50),                  -- 上下文ID
    tenant_id VARCHAR(50),                   -- 租户ID

    -- 审计字段
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),                  -- 创建人
    updated_by VARCHAR(50),                  -- 更新人
    is_deleted BOOLEAN DEFAULT FALSE,        -- 软删除标记
    version INT DEFAULT 1                    -- 乐观锁版本号
);

-- 创建唯一约束（同一租户下编码唯一）
CREATE UNIQUE INDEX uk_category_code ON process_category(code, tenant_id, app_id, context_id);

-- 创建索引（支持多租户查询）
CREATE INDEX idx_parent_id ON process_category(parent_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_tenant ON process_category(tenant_id, app_id, context_id) WHERE is_deleted = FALSE;
CREATE INDEX idx_path ON process_category(path) WHERE is_deleted = FALSE;
CREATE INDEX idx_sort_order ON process_category(sort_order);
CREATE INDEX idx_level ON process_category(level);

-- 添加外键约束（自关联）
ALTER TABLE process_category
ADD CONSTRAINT fk_category_parent
FOREIGN KEY (parent_id) REFERENCES process_category(id)
ON DELETE CASCADE;

-- 添加表注释
COMMENT ON TABLE process_category IS '流程分类表（支持多租户）';
COMMENT ON COLUMN process_category.id IS '分类ID (UUID)';
COMMENT ON COLUMN process_category.name IS '分类名称';
COMMENT ON COLUMN process_category.code IS '分类编码';
COMMENT ON COLUMN process_category.parent_id IS '父分类ID (NULL表示根节点)';
COMMENT ON COLUMN process_category.path IS '分类路径（逗号分隔的ID）';
COMMENT ON COLUMN process_category.level IS '层级深度（根节点为0）';
COMMENT ON COLUMN process_category.app_id IS '应用ID';
COMMENT ON COLUMN process_category.context_id IS '上下文ID';
COMMENT ON COLUMN process_category.tenant_id IS '租户ID';
COMMENT ON COLUMN process_category.icon IS '图标（支持 Element Plus 图标名或 URL）';
COMMENT ON COLUMN process_category.version IS '乐观锁版本号';
```

### 2.2 流程定义扩展表 (process_definition_extension)

```sql
-- 流程定义扩展表
CREATE TABLE IF NOT EXISTS process_definition_extension (
    id VARCHAR(36) PRIMARY KEY,
    process_definition_id VARCHAR(255) NOT NULL UNIQUE,  -- 关联Flowable的流程定义ID
    category_id VARCHAR(36),                             -- 分类ID
    tags VARCHAR(500),                                   -- 标签（JSON数组格式）

    -- 多租户字段（冗余，方便查询）
    app_id VARCHAR(50),
    context_id VARCHAR(50),
    tenant_id VARCHAR(50),

    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- 添加外键约束
    CONSTRAINT fk_extension_category
        FOREIGN KEY (category_id) REFERENCES process_category(id)
        ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_pde_process_def_id ON process_definition_extension(process_definition_id);
CREATE INDEX idx_pde_category_id ON process_definition_extension(category_id);
CREATE INDEX idx_pde_tenant ON process_definition_extension(tenant_id, app_id, context_id);

-- 添加全文搜索索引（支持 PostgreSQL）
CREATE INDEX idx_pde_tags_gin ON process_definition_extension USING gin(to_tsvector('simple', tags));

COMMENT ON TABLE process_definition_extension IS '流程定义扩展表';
COMMENT ON COLUMN process_definition_extension.category_id IS '所属分类ID';
COMMENT ON COLUMN process_definition_extension.tags IS '标签（JSON数组格式）';
```

## 三、后端接口设计

### 3.1 Entity 实体类

```java
// ProcessCategory.java
@Data
public class ProcessCategory {
    private String id;
    private String name;
    private String code;
    private String parentId;
    private String path;           // 分类路径：/root/id1/id2
    private Integer level;         // 层级深度
    private String description;
    private Integer sortOrder;
    private String icon;

    // 多租户字段
    private String appId;
    private String contextId;
    private String tenantId;

    // 审计字段
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private String createdBy;
    private String updatedBy;
    private Boolean isDeleted;
    private Integer version;

    // 关联子分类列表（用于树形结构）
    private List<ProcessCategory> children;

    // 非持久化字段：流程数量
    private Integer processCount;
}

// ProcessDefinitionExtension.java
@Data
public class ProcessDefinitionExtension {
    private String id;
    private String processDefinitionId;
    private String categoryId;
    private String tags;           // JSON数组格式

    // 多租户字段
    private String appId;
    private String contextId;
    private String tenantId;

    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;

    // 关联对象（非数据库字段）
    private ProcessCategory category;
    private String categoryPath;   // 分类路径信息
}
```

### 3.2 DTO 数据传输对象

```java
// ProcessCategoryDTO.java
@Data
public class ProcessCategoryDTO {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    @NotBlank(message = "分类编码不能为空")
    private String code;

    private String parentId;
    private String description;
    private Integer sortOrder;
    private String icon;

    // 多租户字段
    private String appId;
    private String contextId;
    private String tenantId;
}

// ProcessCategoryTreeDTO.java
@Data
public class ProcessCategoryTreeDTO {
    private String id;
    private String name;
    private String code;
    private String parentId;
    private String path;               // 完整路径：/root/id1/id2
    private String pathNames;          // 路径名称：根节点/子节点1/子节点2
    private Integer level;
    private String description;
    private Integer sortOrder;
    private String icon;
    private Integer processCount;      // 该分类下的流程数量
    private Boolean hasChildren;       // 是否有子分类
    private List<ProcessCategoryTreeDTO> children;
}

// ProcessSearchResultDTO.java
@Data
public class ProcessSearchResultDTO {
    private String id;                 // 流程定义ID
    private String key;                // 流程Key
    private String name;               // 流程名称
    private Integer version;           // 版本
    private String categoryId;         // 分类ID
    private String categoryName;       // 分类名称
    private String categoryPath;       // 分类路径ID：root/id1/id2
    private String categoryPathNames;  // 分类路径名称：根节点/子节点1/子节点2
    private List<String> pathIds;      // 路径ID列表，用于前端定位
}

// ProcessCategoryQueryDTO.java
@Data
public class ProcessCategoryQueryDTO {
    private String keyword;            // 搜索关键词（匹配分类名称或编码）
    private String tenantId;           // 租户ID（必需）
    private String appId;              // 应用ID
    private String contextId;          // 上下文ID
    private Boolean includeDeleted;    // 是否包含已删除
}
```

### 3.3 Repository 数据访问层

```java
// ProcessCategoryRepository.java
@Mapper
public interface ProcessCategoryRepository {
    // 基础 CRUD
    ProcessCategory findById(String id);
    ProcessCategory findByCode(String code, String tenantId, String appId, String contextId);
    List<ProcessCategory> findAll(String tenantId, String appId, String contextId);
    List<ProcessCategory> findByParentId(String parentId, String tenantId, String appId, String contextId);

    // 树形结构
    List<ProcessCategory> findTree(String tenantId, String appId, String contextId);

    // 搜索相关
    List<ProcessCategory> searchByName(String keyword, String tenantId, String appId, String contextId);

    // 路径相关
    List<ProcessCategory> findByPathLike(String pathPattern, String tenantId, String appId, String contextId);

    // 操作
    void save(ProcessCategory category);
    void update(ProcessCategory category);
    int updateVersion(String id);  // 乐观锁版本号+1
    void delete(String id);
    void softDelete(String id);

    // 统计
    int countByParentId(String parentId);  // 检查是否有子分类
    int countProcessesByCategoryId(String categoryId);  // 检查分类下是否有流程
}

// ProcessDefinitionExtensionRepository.java
@Mapper
public interface ProcessDefinitionExtensionRepository {
    ProcessDefinitionExtension findByProcessDefinitionId(String processDefinitionId);
    List<ProcessDefinitionExtension> findByCategoryId(String categoryId);

    // 搜索流程（支持模糊搜索）
    List<ProcessDefinitionExtension> searchProcesses(
        String keyword,
        String categoryId,
        String tenantId,
        String appId,
        String contextId,
        Integer page,
        Integer size
    );

    // 统计
    int countByCategoryId(String categoryId);
    int countByCategoryIds(List<String> categoryIds);

    void save(ProcessDefinitionExtension extension);
    void update(ProcessDefinitionExtension extension);
    void delete(String processDefinitionId);
}
```

### 3.4 Service 业务逻辑层

```java
// ProcessCategoryService.java
@Service
public class ProcessCategoryService {

    /**
     * 获取分类树
     */
    public List<ProcessCategoryTreeDTO> getCategoryTree(
        String tenantId,
        String appId,
        String contextId
    );

    /**
     * 创建分类（自动计算路径和层级）
     */
    @Transactional
    public ProcessCategory createCategory(ProcessCategoryDTO dto);

    /**
     * 更新分类
     */
    @Transactional
    public void updateCategory(String id, ProcessCategoryDTO dto);

    /**
     * 删除分类（软删除）
     */
    @Transactional
    public void deleteCategory(String id);

    /**
     * 移动分类（修改父节点，自动更新路径和层级）
     */
    @Transactional
    public void moveCategory(String id, String newParentId);

    /**
     * 更新分类排序
     */
    @Transactional
    public void updateSortOrder(String id, Integer newSortOrder);

    /**
     * 批量更新排序（拖拽排序）
     */
    @Transactional
    public void batchUpdateSortOrder(List<SortOrderDTO> sortOrderList);

    /**
     * 搜索分类（支持名称和编码模糊搜索）
     */
    public List<ProcessCategoryTreeDTO> searchCategories(
        String keyword,
        String tenantId,
        String appId,
        String contextId
    );

    /**
     * 为流程定义设置分类
     */
    @Transactional
    public void setProcessCategory(
        String processDefinitionId,
        String categoryId,
        String tenantId,
        String appId,
        String contextId
    );

    /**
     * 搜索流程模板（支持模糊搜索，返回分类路径）
     */
    public List<ProcessSearchResultDTO> searchProcesses(
        String keyword,
        String categoryId,
        String tenantId,
        String appId,
        String contextId
    );

    /**
     * 更新分类路径及其所有子节点的路径
     */
    @Transactional
    public void updateCategoryPath(String categoryId);
}
```

### 3.5 Controller 控制器

```java
// ProcessCategoryController.java
@RestController
@RequestMapping("/api/process-category")
public class ProcessCategoryController {

    /**
     * 获取分类树
     * GET /api/process-category/tree?tenantId=xxx&appId=xxx&contextId=xxx
     */
    @GetMapping("/tree")
    public Result<List<ProcessCategoryTreeDTO>> getCategoryTree(
        @RequestParam String tenantId,
        @RequestParam(required = false) String appId,
        @RequestParam(required = false) String contextId
    );

    /**
     * 创建分类
     * POST /api/process-category
     */
    @PostMapping
    public Result<ProcessCategory> createCategory(@RequestBody ProcessCategoryDTO dto);

    /**
     * 更新分类
     * PUT /api/process-category/{id}
     */
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
        @PathVariable String id,
        @RequestBody ProcessCategoryDTO dto
    );

    /**
     * 删除分类
     * DELETE /api/process-category/{id}
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable String id);

    /**
     * 移动分类
     * PUT /api/process-category/{id}/move
     */
    @PutMapping("/{id}/move")
    public Result<Void> moveCategory(
        @PathVariable String id,
        @RequestParam String newParentId
    );

    /**
     * 更新排序
     * PUT /api/process-category/{id}/sort
     */
    @PutMapping("/{id}/sort")
    public Result<Void> updateSortOrder(
        @PathVariable String id,
        @RequestParam Integer sortOrder
    );

    /**
     * 批量更新排序（拖拽排序）
     * PUT /api/process-category/sort/batch
     */
    @PutMapping("/sort/batch")
    public Result<Void> batchUpdateSortOrder(
        @RequestBody List<SortOrderDTO> sortOrderList
    );

    /**
     * 搜索分类
     * GET /api/process-category/search?keyword=xxx&tenantId=xxx
     */
    @GetMapping("/search")
    public Result<List<ProcessCategoryTreeDTO>> searchCategories(
        @RequestParam String keyword,
        @RequestParam String tenantId,
        @RequestParam(required = false) String appId,
        @RequestParam(required = false) String contextId
    );

    /**
     * 设置流程分类
     * PUT /api/process-category/process/{processDefinitionId}
     */
    @PutMapping("/process/{processDefinitionId}")
    public Result<Void> setProcessCategory(
        @PathVariable String processDefinitionId,
        @RequestParam String categoryId,
        @RequestParam String tenantId,
        @RequestParam(required = false) String appId,
        @RequestParam(required = false) String contextId
    );

    /**
     * 搜索流程模板（支持模糊搜索，返回分类路径）
     * GET /api/process-category/processes/search?keyword=xxx&tenantId=xxx
     */
    @GetMapping("/processes/search")
    public Result<List<ProcessSearchResultDTO>> searchProcesses(
        @RequestParam String keyword,
        @RequestParam(required = false) String categoryId,
        @RequestParam String tenantId,
        @RequestParam(required = false) String appId,
        @RequestParam(required = false) String contextId
    );
}
```

## 四、前端实现方案

### 4.1 组件设计

```
frontend/src/
├── api/
│   └── processCategory.ts          # 分类相关API
├── views/
│   └── process/
│       ├── CategoryManagement.vue  # 分类管理组件（抽屉/对话框）
│       ├── Index.vue               # 流程列表（添加分类树侧边栏）
│       └── Designer.vue            # 流程设计器（添加分类选择）
└── components/
    ├── CategoryTree.vue            # 分类树组件（可复用，支持拖拽排序）
    ├── CategoryIconSelector.vue    # 分类图标选择器
    └── ProcessSearch.vue           # 流程搜索组件（支持路径定位）
```

### 4.2 API 定义

```typescript
// api/processCategory.ts
import request from '@/utils/request'

export interface ProcessCategory {
  id?: string
  name: string
  code: string
  parentId?: string
  path?: string
  level?: number
  description?: string
  sortOrder?: number
  icon?: string
  appId?: string
  contextId?: string
  tenantId: string
}

export interface ProcessCategoryTree extends ProcessCategory {
  processCount?: number
  hasChildren?: boolean
  children?: ProcessCategoryTree[]
}

export interface ProcessSearchResult {
  id: string
  key: string
  name: string
  version: number
  categoryId: string
  categoryName: string
  categoryPath: string         // 路径ID：root/id1/id2
  categoryPathNames: string    // 路径名称：根节点/子节点1/子节点2
  pathIds: string[]           // 用于前端定位
}

// 获取分类树
export function getCategoryTree(params: {
  tenantId: string
  appId?: string
  contextId?: string
}) {
  return request({
    url: '/api/process-category/tree',
    method: 'get',
    params
  })
}

// 创建分类
export function createCategory(data: ProcessCategory) {
  return request({
    url: '/api/process-category',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(id: string, data: ProcessCategory) {
  return request({
    url: `/api/process-category/${id}`,
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id: string) {
  return request({
    url: `/api/process-category/${id}`,
    method: 'delete'
  })
}

// 移动分类
export function moveCategory(id: string, newParentId: string) {
  return request({
    url: `/api/process-category/${id}/move`,
    method: 'put',
    params: { newParentId }
  })
}

// 更新排序
export function updateSortOrder(id: string, sortOrder: number) {
  return request({
    url: `/api/process-category/${id}/sort`,
    method: 'put',
    params: { sortOrder }
  })
}

// 批量更新排序（拖拽排序）
export function batchUpdateSortOrder(sortOrderList: Array<{id: string, sortOrder: number}>) {
  return request({
    url: '/api/process-category/sort/batch',
    method: 'put',
    data: sortOrderList
  })
}

// 搜索分类
export function searchCategories(params: {
  keyword: string
  tenantId: string
  appId?: string
  contextId?: string
}) {
  return request({
    url: '/api/process-category/search',
    method: 'get',
    params
  })
}

// 设置流程分类
export function setProcessCategory(
  processDefinitionId: string,
  categoryId: string,
  params: {
    tenantId: string
    appId?: string
    contextId?: string
  }
) {
  return request({
    url: `/api/process-category/process/${processDefinitionId}`,
    method: 'put',
    params: { categoryId, ...params }
  })
}

// 搜索流程模板（返回分类路径）
export function searchProcesses(params: {
  keyword: string
  categoryId?: string
  tenantId: string
  appId?: string
  contextId?: string
}) {
  return request({
    url: '/api/process-category/processes/search',
    method: 'get',
    params
  })
}
```

### 4.3 分类树组件（支持拖拽排序）

```vue
<!-- components/CategoryTree.vue -->
<template>
  <div class="category-tree">
    <el-tree
      ref="treeRef"
      :data="treeData"
      :props="treeProps"
      node-key="id"
      :default-expand-all="false"
      :expand-on-click-node="false"
      :highlight-current="true"
      :allow-drop="allowDrop"
      :allow-drag="allowDrag"
      draggable
      @node-drop="handleNodeDrop"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="tree-node">
          <!-- 分类图标 -->
          <el-icon v-if="data.icon" class="node-icon">
            <component :is="data.icon" />
          </el-icon>
          <span class="node-label">{{ node.label }}</span>
          <span v-if="data.processCount !== undefined" class="node-count">
            ({{ data.processCount }})
          </span>
          <!-- 操作按钮 -->
          <span class="node-actions">
            <el-button
              type="primary"
              link
              size="small"
              @click.stop="handleAdd(data)"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button
              type="primary"
              link
              size="small"
              @click.stop="handleEdit(data)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click.stop="handleDelete(data)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </span>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import type { NodeDropType } from 'element-plus'

interface Props {
  data: ProcessCategoryTree[]
  tenantId: string
  appId?: string
  contextId?: string
}

const props = defineProps<Props>()
const emit = defineEmits(['node-click', 'add', 'edit', 'delete'])

const treeRef = ref()
const treeProps = {
  children: 'children',
  label: 'name'
}

// 拖拽相关
const allowDrop = (draggingNode: any, dropNode: any, type: NodeDropType) => {
  // 只允许同级排序或移动到父节点
  return type === 'prev' || type === 'next' || type === 'inner'
}

const allowDrag = (draggingNode: any) => {
  // 根节点不允许拖拽
  return draggingNode.data.parentId !== null
}

const handleNodeDrop = async (
  draggingNode: any,
  dropNode: any,
  dropType: NodeDropType
) => {
  try {
    if (dropType === 'inner') {
      // 移动到父节点下
      await moveCategory(draggingNode.data.id, dropNode.data.id)
    } else {
      // 同级排序
      const siblings = dropNode.parent.childNodes
      const sortOrderList = siblings.map((node: any, index: number) => ({
        id: node.data.id,
        sortOrder: index
      }))
      await batchUpdateSortOrder(sortOrderList)
    }
    ElMessage.success('排序更新成功')
    emit('refresh')
  } catch (error) {
    ElMessage.error('排序更新失败')
  }
}

const handleNodeClick = (data: any) => {
  emit('node-click', data)
}

const handleAdd = (data: any) => {
  emit('add', data)
}

const handleEdit = (data: any) => {
  emit('edit', data)
}

const handleDelete = async (data: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${data.name}"吗？`,
      '警告',
      { type: 'warning' }
    )
    emit('delete', data)
  } catch {
    // 用户取消
  }
}

// 暴露方法供父组件调用
defineExpose({
  setCurrentKey: (key: string) => {
    treeRef.value?.setCurrentKey(key)
  },
  expandPath: (pathIds: string[]) => {
    // 展开路径上的所有节点
    treeRef.value?.store.nodesMap.forEach((node: any) => {
      if (pathIds.includes(node.key)) {
        node.expanded = true
      }
    })
    // 设置当前选中节点
    if (pathIds.length > 0) {
      treeRef.value?.setCurrentKey(pathIds[pathIds.length - 1])
    }
  }
})
</script>

<style scoped>
.category-tree {
  height: 100%;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.node-icon {
  margin-right: 8px;
  color: #409eff;
}

.node-label {
  flex: 1;
}

.node-count {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
}

.node-actions {
  display: none;
  margin-left: 8px;
}

.tree-node:hover .node-actions {
  display: flex;
  gap: 4px;
}
</style>
```

### 4.4 流程搜索组件（支持路径定位）

```vue
<!-- components/ProcessSearch.vue -->
<template>
  <div class="process-search">
    <el-input
      v-model="searchKeyword"
      placeholder="搜索流程模板..."
      clearable
      @input="handleSearch"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <div v-if="searchResults.length > 0" class="search-results">
      <div
        v-for="result in searchResults"
        :key="result.id"
        class="result-item"
        @click="handleSelectResult(result)"
      >
        <div class="result-header">
          <el-icon><Document /></el-icon>
          <span class="result-name">{{ result.name }}</span>
          <el-tag size="small" type="info">v{{ result.version }}</el-tag>
        </div>
        <div class="result-path">
          <el-icon><FolderOpened /></el-icon>
          <span>{{ result.categoryPathNames }}</span>
        </div>
      </div>
    </div>

    <div v-else-if="searchKeyword && searchResults.length === 0" class="no-results">
      <el-empty description="未找到匹配的流程模板" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Document, FolderOpened } from '@element-plus/icons-vue'
import { searchProcesses } from '@/api/processCategory'
import type { ProcessSearchResult } from '@/api/processCategory'

interface Props {
  tenantId: string
  appId?: string
  contextId?: string
}

const props = defineProps<Props>()
const emit = defineEmits(['select'])

const searchKeyword = ref('')
const searchResults = ref<ProcessSearchResult[]>([])
let searchTimer: any = null

const handleSearch = () => {
  clearTimeout(searchTimer)
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }

  searchTimer = setTimeout(async () => {
    try {
      const response = await searchProcesses({
        keyword: searchKeyword.value,
        tenantId: props.tenantId,
        appId: props.appId,
        contextId: props.contextId
      })
      searchResults.value = response.data.data
    } catch (error) {
      ElMessage.error('搜索失败')
    }
  }, 300) // 防抖300ms
}

const handleSelectResult = (result: ProcessSearchResult) => {
  emit('select', {
    process: result,
    pathIds: result.pathIds  // 返回路径ID列表用于定位
  })
  searchResults.value = []
  searchKeyword.value = ''
}
</script>

<style scoped>
.process-search {
  position: relative;
}

.search-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  max-height: 400px;
  overflow-y: auto;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-top: 4px;
  z-index: 1000;
}

.result-item {
  padding: 12px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.result-item:hover {
  background-color: #f5f7fa;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.result-name {
  flex: 1;
  font-weight: 500;
}

.result-path {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.no-results {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  margin-top: 4px;
  padding: 20px;
  z-index: 1000;
}
</style>
```

### 4.5 流程列表页面改造（集成搜索和定位）

```vue
<!-- views/process/Index.vue（关键部分） -->
<template>
  <div class="process-management">
    <div class="page-header">
      <h2>流程管理</h2>
      <div class="header-actions">
        <!-- 流程搜索组件 -->
        <ProcessSearch
          :tenant-id="currentTenantId"
          :app-id="currentAppId"
          :context-id="currentContextId"
          @select="handleSearchSelect"
        />
        <el-button type="primary" @click="createNewProcess">
          新建流程
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧分类树 -->
      <el-col :span="6">
        <el-card class="category-card">
          <template #header>
            <div class="card-header">
              <span>流程分类</span>
              <el-button type="primary" link @click="showCategoryManagement">
                <el-icon><Setting /></el-icon>
                管理
              </el-button>
            </div>
          </template>
          <CategoryTree
            ref="categoryTreeRef"
            :data="categoryTree"
            :tenant-id="currentTenantId"
            :app-id="currentAppId"
            :context-id="currentContextId"
            @node-click="handleCategoryClick"
            @add="handleAddCategory"
            @edit="handleEditCategory"
            @delete="handleDeleteCategory"
            @refresh="loadCategoryTree"
          />
        </el-card>
      </el-col>

      <!-- 右侧流程列表 -->
      <el-col :span="18">
        <el-card>
          <el-table :data="filteredProcessDefinitions">
            <!-- ... 表格列 ... -->
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <!-- 分类管理抽屉 -->
    <CategoryManagementDrawer
      v-model="categoryDrawerVisible"
      :tenant-id="currentTenantId"
      :app-id="currentAppId"
      :context-id="currentContextId"
      @saved="loadCategoryTree"
    />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import CategoryTree from '@/components/CategoryTree.vue'
import ProcessSearch from '@/components/ProcessSearch.vue'
import CategoryManagementDrawer from './CategoryManagementDrawer.vue'
import { getCategoryTree } from '@/api/processCategory'

const categoryTreeRef = ref()
const categoryTree = ref([])
const selectedCategoryId = ref<string>()

// 加载分类树
const loadCategoryTree = async () => {
  const response = await getCategoryTree({
    tenantId: currentTenantId.value,
    appId: currentAppId.value,
    contextId: currentContextId.value
  })
  categoryTree.value = response.data.data
}

// 处理分类点击
const handleCategoryClick = (data: any) => {
  selectedCategoryId.value = data.id
  // 加载该分类下的流程
  loadProcesses(data.id)
}

// 处理搜索选择（定位到分类树节点）
const handleSearchSelect = ({ process, pathIds }: any) => {
  // 使用 pathIds 定位到分类树节点
  categoryTreeRef.value?.expandPath(pathIds)

  // 加载该分类下的流程
  selectedCategoryId.value = process.categoryId
  loadProcesses(process.categoryId)

  // 高亮搜索到的流程
  highlightProcess(process.id)
}
</script>
```

## 五、数据库迁移脚本

```sql
-- V6__create_process_category_tables.sql
-- 创建流程分类表（支持多租户和路径记录）

-- 流程分类表
CREATE TABLE IF NOT EXISTS process_category (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL,
    parent_id VARCHAR(36),
    path VARCHAR(1000),
    level INT DEFAULT 0,
    description VARCHAR(500),
    sort_order INT DEFAULT 0,
    icon VARCHAR(100),
    app_id VARCHAR(50),
    context_id VARCHAR(50),
    tenant_id VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    is_deleted BOOLEAN DEFAULT FALSE,
    version INT DEFAULT 1
);

CREATE UNIQUE INDEX uk_category_code
    ON process_category(code, tenant_id, app_id, context_id)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_category_parent
    ON process_category(parent_id)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_category_tenant
    ON process_category(tenant_id, app_id, context_id)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_category_path
    ON process_category(path)
    WHERE is_deleted = FALSE;

CREATE INDEX idx_category_sort
    ON process_category(sort_order);

CREATE INDEX idx_category_level
    ON process_category(level);

ALTER TABLE process_category
ADD CONSTRAINT fk_category_parent
FOREIGN KEY (parent_id) REFERENCES process_category(id)
ON DELETE CASCADE;

-- 流程定义扩展表
CREATE TABLE IF NOT EXISTS process_definition_extension (
    id VARCHAR(36) PRIMARY KEY,
    process_definition_id VARCHAR(255) NOT NULL UNIQUE,
    category_id VARCHAR(36),
    tags VARCHAR(500),
    app_id VARCHAR(50),
    context_id VARCHAR(50),
    tenant_id VARCHAR(50),
    created_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pde_category
        FOREIGN KEY (category_id)
        REFERENCES process_category(id)
        ON DELETE SET NULL
);

CREATE INDEX idx_pde_process_def
    ON process_definition_extension(process_definition_id);

CREATE INDEX idx_pde_category
    ON process_definition_extension(category_id);

CREATE INDEX idx_pde_tenant
    ON process_definition_extension(tenant_id, app_id, context_id);

CREATE INDEX idx_pde_tags_gin
    ON process_definition_extension
    USING gin(to_tsvector('simple', tags));

COMMENT ON TABLE process_category IS '流程分类表（支持多租户）';
COMMENT ON TABLE process_definition_extension IS '流程定义扩展表';
```

## 六、实施步骤

### Phase 1: 数据库和后端基础
1. ✅ 创建数据库迁移脚本
2. 实现后端 Entity、Repository、Service、Controller
3. 实现分类路径自动计算逻辑
4. 实现多租户数据隔离
5. 编写单元测试

### Phase 2: 前端基础组件
1. 实现分类树组件（支持拖拽排序）
2. 实现分类图标选择器
3. 实现分类管理抽屉组件
4. 实现分类的 CRUD 操作

### Phase 3: 搜索和定位功能
1. 实现流程搜索组件
2. 实现后端模糊搜索接口
3. 实现前端路径定位功能
4. 集成搜索到流程列表页

### Phase 4: 扩展功能
1. 实现分类图标设置
2. 实现分类名称/编码搜索
3. 实现拖拽排序
4. 实现分类统计（流程数量）

### Phase 5: 流程集成
1. 流程列表页集成分类树
2. 流程设计器集成分类选择
3. 修改部署接口，支持设置分类
4. 完善分类数据流

## 七、核心功能实现要点

### 7.1 分类路径自动计算

```java
// 创建分类时自动计算路径
public ProcessCategory createCategory(ProcessCategoryDTO dto) {
    ProcessCategory category = new ProcessCategory();
    category.setId(UUID.randomUUID().toString());
    category.setName(dto.getName());
    category.setCode(dto.getCode());
    category.setParentId(dto.getParentId());
    category.setTenantId(dto.getTenantId());
    category.setAppId(dto.getAppId());
    category.setContextId(dto.getContextId());

    // 计算路径和层级
    if (dto.getParentId() != null) {
        ProcessCategory parent = findById(dto.getParentId());
        category.setPath(parent.getPath() + "/" + category.getId());
        category.setLevel(parent.getLevel() + 1);
    } else {
        category.setPath("/" + category.getId());
        category.setLevel(0);
    }

    save(category);
    return category;
}

// 移动分类时更新路径
@Transactional
public void moveCategory(String id, String newParentId) {
    ProcessCategory category = findById(id);
    String oldPath = category.getPath();

    // 更新当前节点的路径
    ProcessCategory newParent = findById(newParentId);
    category.setParentId(newParentId);
    category.setPath(newParent.getPath() + "/" + id);
    category.setLevel(newParent.getLevel() + 1);
    update(category);

    // 递归更新所有子节点的路径
    updateChildrenPath(id, oldPath, category.getPath());
}

private void updateChildrenPath(String parentId, String oldPath, String newPath) {
    List<ProcessCategory> children = findByParentId(parentId);
    for (ProcessCategory child : children) {
        String childOldPath = child.getPath();
        String childNewPath = newPath + "/" + child.getId();
        child.setPath(childNewPath);
        child.setLevel(calculateLevel(childNewPath));
        update(child);
        updateChildrenPath(child.getId(), childOldPath, childNewPath);
    }
}
```

### 7.2 模糊搜索实现

```java
// 搜索流程模板（返回分类路径）
public List<ProcessSearchResultDTO> searchProcesses(
    String keyword,
    String categoryId,
    String tenantId,
    String appId,
    String contextId
) {
    // 1. 搜索流程定义
    List<ProcessDefinition> processes = processDefinitionRepository.search(
        keyword, categoryId, tenantId, appId, contextId
    );

    // 2. 构建结果并包含分类路径
    List<ProcessSearchResultDTO> results = new ArrayList<>();
    for (ProcessDefinition process : processes) {
        ProcessDefinitionExtension ext = findByProcessDefinitionId(process.getId());
        if (ext != null && ext.getCategoryId() != null) {
            ProcessCategory category = findById(ext.getCategoryId());

            ProcessSearchResultDTO result = new ProcessSearchResultDTO();
            result.setId(process.getId());
            result.setKey(process.getKey());
            result.setName(process.getName());
            result.setVersion(process.getVersion());
            result.setCategoryId(category.getId());
            result.setCategoryName(category.getName());
            result.setCategoryPath(category.getPath());

            // 构建路径名称（从路径ID获取名称）
            result.setCategoryPathNames(buildPathNames(category.getPath()));
            result.setPathIds(Arrays.asList(category.getPath().split("/")).stream()
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList()));

            results.add(result);
        }
    }
    return results;
}

// 从路径ID构建路径名称
private String buildPathNames(String path) {
    String[] ids = path.split("/");
    List<String> names = new ArrayList<>();
    for (String id : ids) {
        if (!id.isEmpty()) {
            ProcessCategory category = findById(id);
            if (category != null) {
                names.add(category.getName());
            }
        }
    }
    return String.join("/", names);
}
```

### 7.3 前端路径定位实现

```typescript
// 在流程搜索组件中
const handleSelectResult = (result: ProcessSearchResult) => {
  // 1. 触发父组件的定位方法
  emit('locate', {
    processId: result.id,
    pathIds: result.pathIds  // ['rootId', 'parentId', 'categoryId']
  })
}

// 在流程列表页中
const handleLocate = ({ processId, pathIds }: any) => {
  // 1. 使用分类树组件的 expandPath 方法展开路径
  categoryTreeRef.value?.expandPath(pathIds)

  // 2. 加载该分类下的流程
  const categoryId = pathIds[pathIds.length - 1]
  selectedCategoryId.value = categoryId
  loadProcesses(categoryId)

  // 3. 高亮搜索到的流程
  highlightProcessId.value = processId
}
```

## 八、注意事项

1. **多租户数据隔离**：
   - 所有查询必须带上 tenantId、appId、contextId
   - 使用数据库索引确保查询性能
   - 考虑使用 Row Level Security (RLS)

2. **路径维护**：
   - 创建、移动分类时自动更新路径
   - 使用事务确保路径更新的一致性
   - 定期检查路径完整性

3. **搜索性能**：
   - 使用 PostgreSQL 全文搜索优化
   - 考虑使用 Elasticsearch 处理大规模数据
   - 实现搜索结果缓存

4. **拖拽排序**：
   - 使用乐观锁防止并发冲突
   - 批量更新排序时使用事务
   - 前端实时反馈，后台异步更新

5. **图标管理**：
   - 支持 Element Plus 图标库
   - 支持自定义图标 URL
   - 考虑图标缓存

## 九、扩展功能实现清单

- ✅ **1. 分类图标**：已设计 icon 字段和图标选择器组件
- ✅ **4. 分类搜索**：已实现名称和编码模糊搜索接口
- ✅ **5. 拖拽排序**：已实现拖拽排序功能和批量更新接口

## 十、后续优化建议

1. **性能优化**：
   - 分类树懒加载（大数据量时）
   - 搜索结果分页
   - 路径计算缓存

2. **功能增强**：
   - 分类权限控制
   - 分类模板（预设属性）
   - 批量操作（移动、删除）
   - 分类导入/导出

3. **用户体验**：
   - 快捷键支持
   - 撤销/重做
   - 操作历史记录
   - 更丰富的图标库
