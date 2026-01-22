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
            :data="filteredTemplates"
            stripe
            style="width: 100%"
            class="custom-table"
            @row-dblclick="handleEdit"
            :row-class-name="getRowClassName"
          >
            <el-table-column prop="templateName" label="流程名称" width="200">
              <template #default="scope">
                <div class="process-name-cell">
                  <el-icon><Document /></el-icon>
                  <span>{{ scope.row.templateName }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="templateKey" label="流程Key" width="180" />
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)">
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="version" label="版本" width="80">
              <template #default="scope">
                <el-tag type="info" size="small">v{{ scope.row.version }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="runningInstanceCount" label="运行实例" width="100">
              <template #default="scope">
                <span v-if="scope.row.runningInstanceCount !== undefined">
                  {{ scope.row.runningInstanceCount }}
                </span>
                <span v-else>-</span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="250" fixed="right">
              <template #default="scope">
                <div class="action-buttons">
                  <!-- 设计态：编辑、发布、删除 -->
                  <template v-if="scope.row.status === 'DRAFT'">
                    <el-button size="small" type="primary" @click="handleEdit(scope.row)">
                      <el-icon><EditPen /></el-icon>
                      编辑
                    </el-button>
                    <el-button size="small" type="success" @click="handlePublish(scope.row)">
                      <el-icon><Upload /></el-icon>
                      发布
                    </el-button>
                    <el-button size="small" type="danger" @click="handleDelete(scope.row)">
                      <el-icon><Delete /></el-icon>
                      删除
                    </el-button>
                  </template>

                  <!-- 激活态：查看、停用 -->
                  <template v-else-if="scope.row.status === 'ACTIVE'">
                    <el-button size="small" type="primary" @click="handleView(scope.row)">
                      <el-icon><View /></el-icon>
                      查看
                    </el-button>
                    <el-button size="small" type="warning" @click="handleSuspend(scope.row)">
                      <el-icon><VideoPause /></el-icon>
                      停用
                    </el-button>
                    <el-button size="small" type="success" @click="handleStart(scope.row)">
                      <el-icon><VideoPlay /></el-icon>
                      启动
                    </el-button>
                  </template>

                  <!-- 停用态：查看、激活 -->
                  <template v-else-if="scope.row.status === 'INACTIVE'">
                    <el-button size="small" type="primary" @click="handleView(scope.row)">
                      <el-icon><View /></el-icon>
                      查看
                    </el-button>
                    <el-button size="small" type="success" @click="handleActivate(scope.row)">
                      <el-icon><VideoPlay /></el-icon>
                      激活
                    </el-button>
                  </template>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <template #footer v-if="filteredTemplates.length === 0">
            <div class="empty-state">
              <el-empty description="暂无流程模板" />
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
  listDraftTemplates,
  listPublishedTemplates,
  deleteDraftTemplate,
  publishTemplate,
  suspendTemplate,
  activateTemplate,
  startProcess
} from '@/api/process'
import {
  getCategoryTree,
  deleteCategory
} from '@/api/processCategory'
import type { ProcessCategoryTree } from '@/api/processCategory'
import type { TemplateView, TemplateStatus } from '@/types/process'
import {
  EditPen,
  Delete,
  VideoPlay,
  VideoPause,
  View,
  Upload,
  Plus,
  Refresh,
  Search,
  Document,
  Setting
} from '@element-plus/icons-vue'
import CategoryTree from '@/components/CategoryTree.vue'
import CategoryManagementDrawer from '@/components/CategoryManagementDrawer.vue'
import ProcessSearch from '@/components/ProcessSearch.vue'

const router = useRouter()

// 设计态和发布态模板列表
const draftTemplates = ref<TemplateView[]>([])
const publishedTemplates = ref<TemplateView[]>([])
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

// 高亮的模板ID（用于搜索定位）
const highlightedTemplateId = ref<string>()

// 合并后的模板列表
const allTemplates = computed(() => {
  return [...draftTemplates.value, ...publishedTemplates.value]
})

// 计算过滤后的模板
const filteredTemplates = computed(() => {
  let result = allTemplates.value

  // 按分类筛选
  if (selectedCategoryId.value) {
    result = result.filter(t => t.categoryId === selectedCategoryId.value)
  }

  // 按搜索关键词筛选
  if (searchQuery.value) {
    const keyword = searchQuery.value.toLowerCase()
    result = result.filter(t =>
      t.templateName.toLowerCase().includes(keyword) ||
      t.templateKey.toLowerCase().includes(keyword)
    )
  }

  return result
})

// 获取行的类名（用于高亮）
const getRowClassName = ({ row }: { row: TemplateView }) => {
  return highlightedTemplateId.value === row.id ? 'highlight-row' : ''
}

// 获取状态类型
const getStatusType = (status: TemplateStatus) => {
  switch (status) {
    case 'DRAFT':
      return 'info'
    case 'ACTIVE':
      return 'success'
    case 'INACTIVE':
      return 'warning'
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status: TemplateStatus) => {
  switch (status) {
    case 'DRAFT':
      return '草稿'
    case 'ACTIVE':
      return '已发布'
    case 'INACTIVE':
      return '已停用'
    default:
      return ''
  }
}

onMounted(() => {
  loadData()
})

onActivated(() => {
  loadData()
})

const loadData = () => {
  loadCategoryTree()
  loadTemplates()
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

// 加载设计态和发布态模板
const loadTemplates = async () => {
  try {
    // 并行加载设计态和发布态模板
    const [draftsRes, publishedRes] = await Promise.all([
      listDraftTemplates({
        tenantId: currentTenantId.value,
        appId: currentAppId.value,
        contextId: currentContextId.value,
        categoryId: selectedCategoryId.value
      }),
      listPublishedTemplates({
        tenantId: currentTenantId.value,
        appId: currentAppId.value,
        contextId: currentContextId.value,
        categoryId: selectedCategoryId.value
      })
    ])

    // 转换为统一的 TemplateView 格式
    draftTemplates.value = (draftsRes.data.data || []).map((t: any) => ({
      id: t.id,
      templateKey: t.templateKey,
      templateName: t.templateName,
      description: t.description,
      categoryId: t.categoryId,
      categoryName: t.categoryName,
      status: 'DRAFT' as TemplateStatus,
      version: t.version,
      createdTime: t.createdTime,
      updatedTime: t.updatedTime
    }))

    publishedTemplates.value = (publishedRes.data.data || []).map((t: any) => ({
      id: t.id,
      templateKey: t.templateKey,
      templateName: t.templateName,
      description: t.description,
      categoryId: t.categoryId,
      categoryName: t.categoryName,
      status: t.status as TemplateStatus,
      version: t.version,
      instanceCount: t.instanceCount,
      runningInstanceCount: t.runningInstanceCount,
      createdTime: t.createdTime,
      updatedTime: t.updatedTime
    }))
  } catch (error) {
    ElMessage.error('加载流程模板失败')
    console.error(error)
  }
}

// 初始BPMN XML
const getInitialXML = () => {
  return `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  id="Definitions_1"
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" name="新建流程" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="开始">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:task id="Task_1" name="任务1">
      <bpmn:incoming>Flow_1</bpmn:incoming>
      <bpmn:outgoing>Flow_2</bpmn:outgoing>
    </bpmn:task>
    <bpmn:endEvent id="EndEvent_1" name="结束">
      <bpmn:incoming>Flow_2</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Task_1" />
    <bpmn:sequenceFlow id="Flow_2" sourceRef="Task_1" targetRef="EndEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="182" y="102" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="189" y="145" width="22" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="Task_1_di" bpmnElement="Task_1">
        <dc:Bounds x="280" y="80" width="100" height="80" />
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
        <dc:Bounds x="442" y="102" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="449" y="145" width="22" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
        <di:waypoint x="218" y="120" />
        <di:waypoint x="280" y="120" />
      </bpmndi:BPMNEdge>
      <bpmndi:BPMNEdge id="Flow_2_di" bpmnElement="Flow_2">
        <di:waypoint x="380" y="120" />
        <di:waypoint x="442" y="120" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`
}

// 创建新流程（先创建设计态模板）
const createNewProcess = async () => {
  try {
    const templateKey = `template_${Date.now()}`
    const initialXML = getInitialXML()

    // 先创建一个设计态模板
    const response = await listDraftTemplates({
      tenantId: currentTenantId.value
    })

    // 使用 createDraftTemplate API
    const { createDraftTemplate } = await import('@/api/process')
    const createResponse = await createDraftTemplate({
      templateKey,
      templateName: '新建流程模板',
      bpmnXml: initialXML,
      categoryId: selectedCategoryId.value || '',
      tenantId: currentTenantId.value,
      appId: currentAppId.value,
      contextId: currentContextId.value
    })

    // 跳转到设计器，带上设计态模板ID
    const draftId = createResponse.data.data.id
    router.push(`/process/designer?draftId=${draftId}`)
  } catch (error) {
    ElMessage.error('创建模板失败')
    console.error(error)
  }
}

// 编辑/查看模板
const handleEdit = (row: TemplateView) => {
  if (row.status === 'DRAFT') {
    router.push(`/process/designer?draftId=${row.id}`)
  } else {
    router.push(`/process/designer?publishedId=${row.id}&readonly=true`)
  }
}

// 查看模板（只读模式）
const handleView = (row: TemplateView) => {
  router.push(`/process/designer?publishedId=${row.id}&readonly=true`)
}

// 发布模板
const handlePublish = async (row: TemplateView) => {
  try {
    await ElMessageBox.confirm('确定要发布此模板吗？发布后将创建新的版本。', '确认发布', {
      type: 'warning'
    })

    await publishTemplate(row.id)
    ElMessage.success('发布成功')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('发布失败')
      console.error(error)
    }
  }
}

// 停用模板
const handleSuspend = async (row: TemplateView) => {
  try {
    await ElMessageBox.confirm('确定要停用此模板吗？', '确认停用', {
      type: 'warning'
    })

    await suspendTemplate(row.id)
    ElMessage.success('停用成功')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('停用失败')
      console.error(error)
    }
  }
}

// 激活模板
const handleActivate = async (row: TemplateView) => {
  try {
    await activateTemplate(row.id)
    ElMessage.success('激活成功')
    loadTemplates()
  } catch (error) {
    ElMessage.error('激活失败')
    console.error(error)
  }
}

// 删除模板
const handleDelete = async (row: TemplateView) => {
  try {
    await ElMessageBox.confirm('确定要删除此模板吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await deleteDraftTemplate(row.id)
    ElMessage.success('删除成功')
    loadTemplates()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
      console.error(error)
    }
  }
}

// 启动流程
const handleStart = (row: TemplateView) => {
  startForm.value.processKey = row.templateKey
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

// 分类相关操作
const handleCategoryClick = (data: ProcessCategoryTree) => {
  currentCategory.value = data
  selectedCategoryId.value = data.id
  // 重新加载该分类下的模板
  loadTemplates()
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

// 处理搜索选择（定位到分类树节点并高亮模板）
const handleSearchSelect = ({ template, pathIds }: { template: any, pathIds: string[] }) => {
  // 1. 使用 pathIds 定位到分类树节点
  const treeRef = categoryTreeRef.value
  if (treeRef && pathIds && pathIds.length > 0) {
    treeRef.expandPath(pathIds)
  }

  // 2. 设置当前分类
  selectedCategoryId.value = template.categoryId
  const treeData = categoryTree.value || []
  currentCategory.value = findCategoryInTree(treeData, template.categoryId)

  // 3. 高亮搜索到的模板
  highlightedTemplateId.value = template.id

  // 4. 重新加载该分类下的模板
  loadTemplates()

  // 5. 3秒后取消高亮
  setTimeout(() => {
    highlightedTemplateId.value = undefined
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
  flex-wrap: wrap;
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
</style>
