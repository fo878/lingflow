<template>
  <div class="process-management-container">
    <div class="page-header">
      <h2 class="page-title">流程管理</h2>
      <div class="header-actions">
        <!-- 流程搜索组件 -->
        <ProcessSearch
          :tenant-id="currentTenantId"
          :app-id="currentAppId"
          :context-id="currentContextId"
          @select="handleSearchSelect"
        />
        <el-button type="primary" @click="createNewProcess" class="action-btn">
          <el-icon><Plus /></el-icon>
          新建流程
        </el-button>
        <el-button @click="loadData" class="refresh-btn">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 左侧分类树 -->
      <el-col :span="6">
        <el-card class="category-card" shadow="hover">
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
        <el-card class="table-card" shadow="hover">
          <template #header>
            <div class="table-header">
              <h3>{{ currentCategory ? `${currentCategory.name} (流程列表)` : '全部流程' }}</h3>
              <div class="table-tools">
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索流程名称..."
                  class="search-input"
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
            </div>
          </template>

          <el-table
            :data="filteredProcessDefinitions"
            stripe
            style="width: 100%"
            class="custom-table"
            @row-dblclick="handleEdit"
            :row-class-name="getRowClassName"
          >
            <el-table-column prop="name" label="流程名称" width="200">
              <template #default="scope">
                <div class="process-name-cell">
                  <el-icon><Document /></el-icon>
                  <span>{{ scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="key" label="流程Key" width="180" />
            <el-table-column prop="version" label="版本" width="80">
              <template #default="scope">
                <el-tag type="info" size="small">v{{ scope.row.version }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deploymentId" label="部署ID" width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button
                    size="small"
                    type="primary"
                    @click="handleEdit(scope.row)"
                    class="btn-edit"
                  >
                    <el-icon><EditPen /></el-icon>
                    编辑
                  </el-button>
                  <el-button
                    size="small"
                    type="success"
                    @click="handleStart(scope.row)"
                    class="btn-start"
                  >
                    <el-icon><VideoPlay /></el-icon>
                    启动
                  </el-button>
                  <el-button
                    size="small"
                    type="danger"
                    @click="handleDelete(scope.row)"
                    class="btn-delete"
                  >
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <template #footer v-if="filteredProcessDefinitions.length === 0">
            <div class="empty-state">
              <el-empty description="暂无流程定义" />
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="startDialogVisible" title="启动流程" width="500px">
      <el-form :model="startForm" label-width="100px">
        <el-form-item label="流程Key">
          <el-input v-model="startForm.processKey" disabled />
        </el-form-item>
        <el-form-item label="业务Key">
          <el-input
            v-model="startForm.businessKey"
            placeholder="请输入业务Key（可选）"
            clearable
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStart">启动流程</el-button>
      </template>
    </el-dialog>

    <!-- 分类管理抽屉 -->
    <CategoryManagementDrawer
      v-model="categoryDrawerVisible"
      :category-data="currentEditCategory"
      :category-tree="categoryTree"
      :tenant-id="currentTenantId"
      :app-id="currentAppId"
      :context-id="currentContextId"
      @saved="handleCategorySaved"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onActivated, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProcessDefinitions,
  deleteProcessDefinition,
  startProcess
} from '@/api/process'
import {
  getCategoryTree,
  deleteCategory
} from '@/api/processCategory'
import type { ProcessCategoryTree } from '@/api/processCategory'
import { EditPen, Delete, VideoPlay, Plus, Refresh, Search, Document, Setting } from '@element-plus/icons-vue'
import CategoryTree from '@/components/CategoryTree.vue'
import CategoryManagementDrawer from '@/components/CategoryManagementDrawer.vue'
import ProcessSearch from '@/components/ProcessSearch.vue'

interface ProcessDefinition {
  id: string
  key: string
  name: string
  version: number
  deploymentId: string
}

const router = useRouter()
const processDefinitions = ref<ProcessDefinition[]>([])
const categoryTree = ref<ProcessCategoryTree[]>([])
const currentCategory = ref<ProcessCategoryTree | null>(null)
const currentEditCategory = ref<ProcessCategoryTree | null>(null)
const selectedCategoryId = ref<string>()
const startDialogVisible = ref(false)
const categoryDrawerVisible = ref(false)
const startForm = ref({
  processKey: '',
  businessKey: ''
})

// TODO: 从上下文或配置获取当前租户信息
const currentTenantId = ref('default_tenant')
const currentAppId = ref('')
const currentContextId = ref('')

// 搜索功能
const searchQuery = ref('')

// 高亮的流程ID（用于搜索定位）
const highlightedProcessId = ref<string>()

// 计算过滤后的流程定义
const filteredProcessDefinitions = computed(() => {
  let result = processDefinitions.value

  // 按分类筛选
  if (selectedCategoryId.value) {
    // TODO: 实现按分类筛选流程的逻辑
    // 这里需要扩展 getProcessDefinitions 接口支持 categoryId 参数
  }

  // 按搜索关键词筛选
  if (searchQuery.value) {
    const keyword = searchQuery.value.toLowerCase()
    result = result.filter(def =>
      def.name.toLowerCase().includes(keyword) ||
      def.key.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 获取行的类名（用于高亮）
const getRowClassName = ({ row }: { row: ProcessDefinition }) => {
  return highlightedProcessId.value === row.id ? 'highlight-row' : ''
}

onMounted(() => {
  loadData()
})

onActivated(() => {
  loadData()
})

const loadData = () => {
  loadCategoryTree()
  loadProcessDefinitions()
}

const loadCategoryTree = async () => {
  try {
    const response = await getCategoryTree({
      tenantId: currentTenantId.value,
      appId: currentAppId.value,
      contextId: currentContextId.value
    })
    categoryTree.value = response.data.data || []
  } catch (error) {
    ElMessage.error('加载分类树失败')
    console.error(error)
  }
}

const loadProcessDefinitions = async () => {
  try {
    const response = await getProcessDefinitions()
    processDefinitions.value = response.data.data
  } catch (error) {
    ElMessage.error('加载流程定义失败')
    console.error(error)
  }
}

const createNewProcess = () => {
  router.push('/process/designer')
}

const handleEdit = (row: ProcessDefinition) => {
  router.push(`/process/designer?id=${row.id}&key=${row.key}&name=${encodeURIComponent(row.name)}`)
}

const handleStart = (row: ProcessDefinition) => {
  startForm.value.processKey = row.key
  startForm.value.businessKey = ''
  startDialogVisible.value = true
}

const confirmStart = async () => {
  try {
    await startProcess(startForm.value.processKey)
    ElMessage.success('流程启动成功')
    startDialogVisible.value = false
  } catch (error) {
    ElMessage.error('流程启动失败')
    console.error(error)
  }
}

const handleDelete = async (row: ProcessDefinition) => {
  try {
    await ElMessageBox.confirm('确定要删除此流程定义吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteProcessDefinition(row.deploymentId)
    ElMessage.success('删除成功')
    loadProcessDefinitions()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 分类相关操作
const handleCategoryClick = (data: ProcessCategoryTree) => {
  currentCategory.value = data
  selectedCategoryId.value = data.id
  // TODO: 加载该分类下的流程
}

const showCategoryManagement = () => {
  currentEditCategory.value = null
  categoryDrawerVisible.value = true
}

const handleAddCategory = (data: ProcessCategoryTree) => {
  currentEditCategory.value = null
  categoryDrawerVisible.value = true
}

const handleEditCategory = (data: ProcessCategoryTree) => {
  currentEditCategory.value = data
  categoryDrawerVisible.value = true
}

const handleDeleteCategory = async (data: ProcessCategoryTree) => {
  try {
    await deleteCategory(data.id!)
    ElMessage.success('分类删除成功')
    loadCategoryTree()
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '删除失败')
  }
}

const handleCategorySaved = () => {
  loadCategoryTree()
}

// 处理搜索选择（定位到分类树节点并高亮流程）
const handleSearchSelect = ({ process, pathIds }: { process: any, pathIds: string[] }) => {
  // 1. 使用 pathIds 定位到分类树节点
  const treeRef = categoryTreeRef.value
  if (treeRef && pathIds && pathIds.length > 0) {
    treeRef.expandPath(pathIds)
  }

  // 2. 设置当前分类
  selectedCategoryId.value = process.categoryId
  const treeData = categoryTree.value || []
  currentCategory.value = findCategoryInTree(treeData, process.categoryId)

  // 3. 高亮搜索到的流程
  highlightedProcessId.value = process.id

  // 4. 3秒后取消高亮
  setTimeout(() => {
    highlightedProcessId.value = undefined
  }, 3000)
}

// 在树中查找分类
const findCategoryInTree = (tree: ProcessCategoryTree[], categoryId: string): ProcessCategoryTree | null => {
  for (const node of tree) {
    if (node.id === categoryId) {
      return node
    }
    if (node.children) {
      const found = findCategoryInTree(node.children, categoryId)
      if (found) return found
    }
  }
  return null
}
</script>

<style scoped>
.process-management-container {
  padding: 20px;
  background: linear-gradient(to bottom, #f5f7fa 0%, #c3cfe2 100%);
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  background: linear-gradient(45deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.action-btn {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
}

.refresh-btn {
  background: #f5f7fa;
  border: 1px solid #d8dee7;
}

.category-card {
  border-radius: 12px;
  min-height: calc(100vh - 180px);
}

.category-card :deep(.el-card__body) {
  padding: 10px;
  max-height: calc(100vh - 240px);
  overflow-y: auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.card-header span {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: none;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header h3 {
  margin: 0;
  color: #303133;
  font-size: 18px;
  font-weight: 500;
}

.search-input {
  width: 280px;
}

.custom-table {
  border-radius: 8px;
}

.process-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.process-name-cell .el-icon {
  color: #409eff;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-edit {
  background-color: #ecf5ff;
  border-color: #b3d8ff;
  color: #409eff;
}

.btn-start {
  background-color: #f0f9eb;
  border-color: #c2e7b0;
  color: #67c23a;
}

.btn-delete {
  background-color: #fef0f0;
  border-color: #fbc4c4;
  color: #f56c6c;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

/* 高亮行样式 */
:deep(.el-table .highlight-row) {
  background-color: #fff7e6 !important;
}

:deep(.el-table .highlight-row:hover > td) {
  background-color: #ffe7ba !important;
}

/* 按钮悬停效果 */
.action-btn:hover,
.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.btn-edit:hover {
  background-color: #409eff;
  color: white;
  transition: all 0.3s ease;
}

.btn-start:hover {
  background-color: #67c23a;
  color: white;
  transition: all 0.3s ease;
}

.btn-delete:hover {
  background-color: #f56c6c;
  color: white;
  transition: all 0.3s ease;
}
</style>
