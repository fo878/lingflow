<template>
  <div class="designer-container">
    <!-- 头部导航栏 -->
    <div class="designer-header">
      <div class="header-left">
        <el-button @click="goBackToList" class="back-btn" title="返回流程列表">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <h2 class="logo">📄 流程设计器</h2>
        <el-input
          v-model="processName"
          placeholder="请输入流程名称"
          class="process-name-input"
          :disabled="!canEdit"
        />
      </div>
      <div class="header-right">
        <div class="zoom-controls">
          <el-button size="small" @click="zoomIn" title="放大">
            <el-icon><ZoomIn /></el-icon>
          </el-button>
          <el-button size="small" @click="zoomOut" title="缩小">
            <el-icon><ZoomOut /></el-icon>
          </el-button>
          <el-button size="small" @click="resetZoom" title="重置缩放">
            <el-icon><Refresh /></el-icon>
          </el-button>
          <span class="zoom-display">{{ Math.round(zoomLevel * 100) }}%</span>
        </div>
        <div class="operation-buttons">
          <!-- 流程状态标签 -->
          <el-tag v-if="processStatus === 'DRAFT'" type="info" size="large" class="status-tag">
            草稿
          </el-tag>
          <el-tag v-else-if="processStatus === 'ACTIVE'" type="success" size="large" class="status-tag">
            已发布
          </el-tag>
          <el-tag v-else-if="processStatus === 'INACTIVE'" type="warning" size="large" class="status-tag">
            已停用
          </el-tag>

          <!-- 保存按钮（仅设计态可用） -->
          <el-button v-if="canEdit" @click="saveProcess" :loading="isSaving" class="save-btn">
            <el-icon><Document /></el-icon>
            保存
          </el-button>

          <!-- 快照按钮组 -->
          <el-dropdown split-button type="default" @click="showSnapshotDialog" @command="handleSnapshotCommand">
            <el-icon><DocumentCopy /></el-icon>
            快照
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="listSnapshots" icon="Document">
                  查看快照
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button @click="exportXML" plain>
            <el-icon><Download /></el-icon>
            导出
          </el-button>

          <!-- 发布按钮（仅设计态可用） -->
          <el-button v-if="canPublish"
                    type="primary"
                    @click="publishProcess"
                    :loading="isPublishing"
                    class="publish-btn">
            <el-icon><VideoPlay /></el-icon>
            发布流程
          </el-button>

          <!-- 停用按钮（仅激活态可用） -->
          <el-button v-else-if="canSuspend"
                    type="warning"
                    @click="suspendProcess"
                    :loading="isSuspending"
                    class="suspend-btn">
            <el-icon><VideoPause /></el-icon>
            停用流程
          </el-button>

          <!-- 激活按钮（仅停用态可用） -->
          <el-button v-else-if="canActivate"
                    type="success"
                    @click="activateProcessFunc"
                    :loading="isActivating"
                    class="activate-btn">
            <el-icon><VideoPlay /></el-icon>
            激活流程
          </el-button>
        </div>
      </div>
    </div>

    <!-- 主体内容区域 -->
    <div class="main-content">
      <!-- 中间画布区域 -->
      <div class="canvas-wrapper">
        <div class="canvas-container" ref="canvasRef"></div>
      </div>

      <!-- 右侧属性面板 -->
      <div class="right-panel">
        <div class="panel-header">
          <h3>{{ selectedElement ? '元素属性' : '流程属性' }}</h3>
          <!-- 流程状态标签 -->
          <el-tag v-if="!selectedElement && processStatus === 'DRAFT'" type="info" size="small">
            设计态
          </el-tag>
          <el-tag v-else-if="!selectedElement && processStatus === 'ACTIVE'" type="success" size="small">
            发布态
          </el-tag>
          <el-tag v-else-if="!selectedElement && processStatus === 'INACTIVE'" type="warning" size="small">
            停用态
          </el-tag>
        </div>

        <!-- 保存按钮（仅在选中元素时可编辑时显示） -->
        <div v-if="selectedElement && canEditProperties" class="save-button-wrapper">
          <el-button type="primary" @click="saveElementProperties" size="small" :loading="isSaving">
            <el-icon><Document /></el-icon>
            保存属性
          </el-button>
        </div>

        <!-- 属性不可编辑提示 -->
        <div v-if="selectedElement && !canEditProperties" class="edit-warning">
          <el-icon><InfoFilled /></el-icon>
          <span>{{ getEditDisabledReason() }}</span>
        </div>

        <!-- 流程模板属性 -->
        <el-form v-if="!selectedElement" label-position="top">
          <el-form-item label="流程名称">
            <el-input v-model="processName" placeholder="请输入流程名称" :disabled="!canEdit"></el-input>
          </el-form-item>
          <el-form-item>
            <template #label>
              <div style="display: flex; align-items: center; gap: 4px;">
                <span>流程Key</span>
                <el-tooltip
                  content="流程Key 对应 BPMN XML 中的 process id，修改后自动同步到 BPMN"
                  placement="top"
                >
                  <el-icon style="cursor: help; color: #909399;"><InfoFilled /></el-icon>
                </el-tooltip>
              </div>
            </template>
            <el-input
              v-model="processKey"
              placeholder="请输入流程Key（对应 BPMN process id）"
              :disabled="!canEdit"
            >
              <template #suffix>
                <el-tag v-if="canEdit" type="success" size="small" effect="plain">
                  自动同步
                </el-tag>
              </template>
            </el-input>
            <div v-if="canEdit" class="form-tip">
              修改流程Key后，会自动更新 BPMN XML 中的 process id，确保两者一致
            </div>
          </el-form-item>
          <el-form-item label="所属分类">
            <el-cascader
              v-model="selectedCategoryId"
              :options="categoryTreeOptions"
              :props="{
                value: 'id',
                label: 'name',
                children: 'children',
                checkStrictly: true,
                emitPath: false
              }"
              placeholder="请选择分类（可选）"
              clearable
              filterable
              :disabled="!canEdit"
            />
          </el-form-item>
          <el-form-item label="流程版本">
            <el-input :value="processVersion" readonly></el-input>
          </el-form-item>
          <el-form-item label="流程描述">
            <el-input
              v-model="processDescription"
              type="textarea"
              :rows="4"
              placeholder="请输入流程描述"
              :disabled="!canEdit"
            ></el-input>
          </el-form-item>
          <el-divider></el-divider>
          <div class="process-info">
            <el-icon><InfoFilled /></el-icon>
            <span>在画布上选择元素可查看和编辑元素属性</span>
          </div>
        </el-form>

        <!-- 元素属性 -->
        <el-form v-else label-position="top">
          <el-form-item label="元素类型">
            <el-input v-model="selectedElement.type" readonly></el-input>
          </el-form-item>
          <el-form-item label="元素ID">
            <el-input v-model="selectedElement.id" readonly></el-input>
          </el-form-item>
          <el-form-item label="元素名称">
            <el-input
              v-model="selectedElement.name"
              @input="updateElementName"
              :disabled="!isPropertyEditable('name')"
            ></el-input>
          </el-form-item>

          <!-- 根据元素类型显示不同的属性配置 -->
          <!-- 用户任务属性 -->
          <template v-if="isUserTask(selectedElement.type)">
            <el-form-item label="执行人">
              <el-input
                v-model="selectedElement.extensionAttributes.assignee"
                placeholder="请输入执行人"
                :disabled="!isPropertyEditable('assignee')"
              ></el-input>
            </el-form-item>
            <el-form-item label="候选用户">
              <el-input
                v-model="selectedElement.extensionAttributes.candidateUsers"
                placeholder="请输入候选用户，多个用逗号分隔"
                :disabled="!isPropertyEditable('candidateUsers')"
              ></el-input>
            </el-form-item>
            <el-form-item label="候选组">
              <el-input
                v-model="selectedElement.extensionAttributes.candidateGroups"
                placeholder="请输入候选组，多个用逗号分隔"
                :disabled="!isPropertyEditable('candidateGroups')"
              ></el-input>
            </el-form-item>
            <el-form-item label="表单Key">
              <el-input
                v-model="selectedElement.extensionAttributes.formKey"
                placeholder="请输入表单Key"
                :disabled="!isPropertyEditable('formKey')"
              ></el-input>
            </el-form-item>
            <el-form-item label="截止日期">
              <el-input
                v-model="selectedElement.extensionAttributes.dueDate"
                placeholder="请输入截止日期表达式"
                :disabled="!isPropertyEditable('dueDate')"
              ></el-input>
            </el-form-item>
            <el-form-item label="优先级">
              <el-input
                v-model="selectedElement.extensionAttributes.priority"
                placeholder="请输入优先级"
                :disabled="!isPropertyEditable('priority')"
              ></el-input>
            </el-form-item>
          </template>

          <!-- 服务任务属性 -->
          <template v-else-if="isServiceTask(selectedElement.type)">
            <el-form-item label="实现类">
              <el-input
                v-model="selectedElement.extensionAttributes.implementation"
                placeholder="请输入实现类"
                :disabled="!isPropertyEditable('implementation')"
              ></el-input>
            </el-form-item>
            <el-form-item label="表达式">
              <el-input
                v-model="selectedElement.extensionAttributes.expression"
                placeholder="请输入表达式"
                :disabled="!isPropertyEditable('expression')"
              ></el-input>
            </el-form-item>
            <el-form-item label="代理表达式">
              <el-input
                v-model="selectedElement.extensionAttributes.delegateExpression"
                placeholder="请输入代理表达式"
                :disabled="!isPropertyEditable('delegateExpression')"
              ></el-input>
            </el-form-item>
            <el-form-item label="结果变量名">
              <el-input
                v-model="selectedElement.extensionAttributes.resultVariableName"
                placeholder="请输入结果变量名"
                :disabled="!isPropertyEditable('resultVariableName')"
              ></el-input>
            </el-form-item>
            <el-form-item label="任务类型">
              <el-select
                v-model="selectedElement.extensionAttributes.type"
                placeholder="请选择任务类型"
                :disabled="!isPropertyEditable('type')"
              >
                <el-option label="HTTP" value="http"></el-option>
                <el-option label="邮件" value="mail"></el-option>
                <el-option label="其他" value="other"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="异步执行">
              <el-switch
                v-model="selectedElement.extensionAttributes.async"
                :disabled="!isPropertyEditable('async')"
              />
            </el-form-item>
          </template>

          <!-- 网关属性 -->
          <template v-else-if="isGateway(selectedElement.type)">
            <el-form-item label="网关类型">
              <el-select
                v-model="selectedElement.extensionAttributes.gatewayType"
                placeholder="请选择网关类型"
                :disabled="!isPropertyEditable('gatewayType')"
              >
                <el-option label="排他网关" value="exclusive"></el-option>
                <el-option label="并行网关" value="parallel"></el-option>
                <el-option label="包容网关" value="inclusive"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="默认路径">
              <el-input
                v-model="selectedElement.extensionAttributes.default"
                placeholder="请输入默认路径ID"
                :disabled="!isPropertyEditable('default')"
              ></el-input>
            </el-form-item>
          </template>

          <!-- 事件属性 -->
          <template v-else-if="isEvent(selectedElement.type)">
            <el-form-item label="消息引用">
              <el-input
                v-model="selectedElement.extensionAttributes.messageRef"
                placeholder="请输入消息引用"
                :disabled="!isPropertyEditable('messageRef')"
              ></el-input>
            </el-form-item>
            <el-form-item label="定时器配置">
              <el-input
                v-model="selectedElement.extensionAttributes.timerEventDefinition"
                placeholder="请输入定时器配置"
                :disabled="!isPropertyEditable('timerEventDefinition')"
              ></el-input>
            </el-form-item>
            <el-form-item label="信号引用">
              <el-input
                v-model="selectedElement.extensionAttributes.signalRef"
                placeholder="请输入信号引用"
                :disabled="!isPropertyEditable('signalRef')"
              ></el-input>
            </el-form-item>
          </template>

          <!-- 通用属性 -->
          <el-form-item label="描述">
            <el-input
              v-model="selectedElement.description"
              type="textarea"
              :rows="4"
              placeholder="请输入描述信息"
            ></el-input>
          </el-form-item>
        </el-form>
      </div>
    </div>

    <!-- 快照对话框 -->
    <el-dialog v-model="snapshotDialogVisible" title="流程快照管理" width="80%" top="5vh">
      <div class="snapshot-toolbar">
        <el-button type="primary" @click="showCreateSnapshotDialog" :disabled="!canEdit">
          <el-icon><Plus /></el-icon>
          创建快照
        </el-button>
      </div>

      <el-table
        :data="snapshots"
        stripe
        style="width: 100%"
        row-key="id"
      >
        <el-table-column prop="snapshotName" label="快照名称" width="200"></el-table-column>
        <el-table-column prop="snapshotVersion" label="版本" width="100"></el-table-column>
        <el-table-column prop="description" label="描述"></el-table-column>
        <el-table-column prop="createdBy" label="创建人" width="120"></el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="restoreFromSnapshot(row.id)">
              恢复
            </el-button>
            <el-button size="small" type="danger" @click="deleteSnapshot(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 创建快照对话框 -->
    <el-dialog v-model="createSnapshotDialogVisible" title="创建快照" width="500px">
      <el-form :model="snapshotForm" label-width="100px">
        <el-form-item label="快照名称">
          <el-input v-model="snapshotForm.snapshotName" placeholder="请输入快照名称"></el-input>
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="snapshotForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入快照描述（可选）"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createSnapshotDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="createSnapshot">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, nextTick, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ZoomIn,
  ZoomOut,
  Download,
  Refresh,
  Document,
  DocumentCopy,
  Plus,
  InfoFilled,
  ArrowLeft,
  VideoPlay,
  VideoPause
} from '@element-plus/icons-vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import {
  createDraftTemplate,
  updateDraftTemplate,
  getDraftTemplate,
  publishTemplate,
  suspendTemplate,
  activateTemplate as apiActivateTemplate,
  getPublishedTemplate,
  createTemplateSnapshot,
  listTemplateSnapshots,
  restoreFromSnapshot as apiRestoreFromSnapshot,
  deleteTemplateSnapshot,
  saveElementExtension,
  getElementExtension,
  batchSaveElementExtensions,
  getAllElementExtensions
} from '@/api/process'
import {
  getCategoryTree
} from '@/api/processCategory'
import type { ProcessCategoryTree } from '@/api/processCategory'
import type { TemplateStatus } from '@/types/process'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-js.css'

const canvasRef = ref<HTMLElement>()
const processName = ref('')
const processKey = ref('')
const processVersion = ref('v1')
const processDescription = ref('')
let modeler: any = null

/**
 * 从 BPMN XML 中提取流程定义 key
 * @param xml BPMN XML 字符串
 * @returns 流程定义 key 或 null
 */
const extractProcessKeyFromXML = (xml: string): string | null => {
  const match = xml.match(/<bpmn:process[^>]*id="([^"]+)"/)
  return match ? match[1] : null
}

/**
 * 更新 BPMN XML 中的流程定义 id
 * @param newKey 新的流程 key
 */
const updateBpmnProcessKey = async (newKey: string) => {
  if (!modeler || !newKey) return

  try {
    const modeling = modeler.get('modeling')
    const elementRegistry = modeler.get('elementRegistry')
    const processElements = elementRegistry.filter((element: any) => element.type === 'bpmn:Process')

    if (processElements && processElements.length > 0) {
      const processElement = processElements[0]
      modeling.updateProperties(processElement, {
        id: newKey
      })
      console.log('BPMN process key updated:', newKey)
    }
  } catch (error) {
    console.error('Failed to update BPMN process key:', error)
  }
}

/**
 * 同步 processKey 到 BPMN XML
 * 当用户修改界面上的 processKey 时，自动更新 BPMN XML 中的 process id
 */
let isLoadingTemplate = false  // 标记是否正在加载模板

watch(processKey, (newKey, oldKey) => {
  // 避免在加载模板时触发更新
  if (newKey && modeler && !isLoadingTemplate && newKey !== oldKey) {
    updateBpmnProcessKey(newKey)
  }
})

// 流程状态：'DRAFT'（设计态） | 'ACTIVE'（发布态） | 'INACTIVE'（停用态）
const processStatus = ref<TemplateStatus>('DRAFT')

// 设计态和发布态的ID
const draftId = ref<string>()
const publishedId = ref<string>()

const route = useRoute()
const router = useRouter()

// 分类相关
const categoryTree = ref<ProcessCategoryTree[]>([])
const selectedCategoryId = ref<string>()
const categoryTreeOptions = computed(() => categoryTree.value)

// TODO: 从上下文或配置获取当前租户信息
const currentTenantId = ref('default_tenant')
const currentAppId = ref('')
const currentContextId = ref('')

// 缩放级别
const zoomLevel = ref(1)

// 选中的元素
const selectedElement = ref<any>(null)

// 加载状态
const isSaving = ref(false)
const isPublishing = ref(false)
const isSuspending = ref(false)
const isActivating = ref(false)

// 计算是否可以编辑
const canEdit = computed(() => processStatus.value === 'DRAFT')
const canPublish = computed(() => processStatus.value === 'DRAFT')
const canSuspend = computed(() => processStatus.value === 'ACTIVE')
const canActivate = computed(() => processStatus.value === 'INACTIVE')

// 计算是否可以编辑属性
const canEditProperties = computed(() => {
  // 只有设计态可以编辑元素属性
  return processStatus.value === 'DRAFT'
})

// 判断某个属性是否可编辑
const isPropertyEditable = (propertyName: string) => {
  if (processStatus.value === 'DRAFT') {
    return true
  }
  return false
}

// 获取不可编辑的原因
const getEditDisabledReason = () => {
  if (processStatus.value === 'INACTIVE') {
    return '流程已停用，不能修改任何属性'
  }
  if (processStatus.value === 'ACTIVE') {
    return '流程已发布，不能修改属性'
  }
  return '当前属性不可编辑'
}

// 加载设计态模板
const loadDraftTemplate = async (id: string) => {
  isLoadingTemplate = true  // 标记开始加载
  try {
    const response = await getDraftTemplate(id)
    const template = response.data.data

    // 从 BPMN XML 中提取实际的流程 key
    const actualProcessKey = extractProcessKeyFromXML(template.bpmnXml)

    // 设置模板属性（使用 BPMN XML 中的实际流程 key）
    processName.value = template.templateName
    processKey.value = actualProcessKey || template.templateKey
    processDescription.value = template.description || ''
    processVersion.value = `v${template.version}`
    selectedCategoryId.value = template.categoryId
    draftId.value = template.id

    // 加载BPMN
    if (modeler && template.bpmnXml) {
      await modeler.importXML(template.bpmnXml)
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
      const currentViewbox = canvas.viewbox()
      zoomLevel.value = currentViewbox.scale
    }

    // 设置状态
    processStatus.value = 'DRAFT'

    ElMessage.success('模板加载成功')
  } catch (error: any) {
    ElMessage.error('加载模板失败: ' + (error.response?.data?.message || error.message))
    console.error(error)
  } finally {
    isLoadingTemplate = false  // 标记加载完成
  }
}

// 加载发布态模板
const loadPublishedTemplate = async (id: string) => {
  isLoadingTemplate = true  // 标记开始加载
  try {
    const response = await getPublishedTemplate(id)
    const template = response.data.data

    // 从 BPMN XML 中提取实际的流程 key
    const actualProcessKey = extractProcessKeyFromXML(template.bpmnXml)

    // 设置模板属性（使用 BPMN XML 中的实际流程 key）
    processName.value = template.templateName
    processKey.value = actualProcessKey || template.templateKey
    processDescription.value = template.description || ''
    processVersion.value = `v${template.version}`
    selectedCategoryId.value = template.categoryId
    publishedId.value = template.id

    // 加载BPMN
    if (modeler && template.bpmnXml) {
      await modeler.importXML(template.bpmnXml)
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
      const currentViewbox = canvas.viewbox()
      zoomLevel.value = currentViewbox.scale
    }

    // 设置状态
    processStatus.value = template.status

    ElMessage.success('模板加载成功')
  } catch (error: any) {
    ElMessage.error('加载模板失败: ' + (error.response?.data?.message || error.message))
    console.error(error)
  } finally {
    isLoadingTemplate = false  // 标记加载完成
  }
}

// 加载分类树
const loadCategoryTree = async () => {
  try {
    const response = await getCategoryTree({
      tenantId: currentTenantId.value,
      appId: currentAppId.value,
      contextId: currentContextId.value
    })
    categoryTree.value = response.data.data || []
  } catch (error) {
    console.error('加载分类树失败:', error)
  }
}

// 初始空白流程XML
const initialXML = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
  id="Definitions_1"
  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="true" />
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1" />
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`

// 快照相关
const snapshotDialogVisible = ref(false)
const createSnapshotDialogVisible = ref(false)
const snapshots = ref<any[]>([])
const snapshotForm = ref({
  snapshotName: '',
  description: ''
})

onMounted(async () => {
  // 加载分类树
  await loadCategoryTree()

  const queryParams = route.query

  if (canvasRef.value) {
    modeler = new BpmnModeler({
      container: canvasRef.value,
      keyboard: {
        bindTo: window
      }
    })

    // 监听元素选择事件
    const eventBus = modeler.get('eventBus')
    const selection = modeler.get('selection')

    eventBus.on('selection.changed', async (e: any) => {
      const newSelection = e.newSelection
      if (newSelection && newSelection.length > 0) {
        const element = newSelection[0]

        // 如果有发布态ID，从后端加载该元素的扩展属性
        let extensionAttributes = {}
        if (publishedId.value) {
          try {
            const response = await getElementExtension(publishedId.value, element.id)
            if (response.data.data.exists) {
              extensionAttributes = response.data.data.extensionAttributes || {}
            }
          } catch (error) {
            console.error('加载扩展属性失败:', error)
          }
        } else {
          // 使用元素本地存储的扩展属性
          extensionAttributes = element.businessObject.extensionAttributes || {}
        }

        selectedElement.value = {
          id: element.id,
          type: element.type,
          name: element.businessObject.name || '',
          extensionAttributes: extensionAttributes,
          description: element.businessObject.documentation ? element.businessObject.documentation[0]?.text : ''
        }
      } else {
        selectedElement.value = null
      }
    })

    try {
      // 根据URL参数加载模板
      if (queryParams.draftId) {
        draftId.value = queryParams.draftId as string
        await loadDraftTemplate(queryParams.draftId as string)
      } else if (queryParams.publishedId) {
        publishedId.value = queryParams.publishedId as string
        await loadPublishedTemplate(queryParams.publishedId as string)
      } else {
        // 新建模板
        processStatus.value = 'DRAFT'
        await modeler.importXML(initialXML)
        const canvas = modeler.get('canvas')
        canvas.zoom('fit-viewport')
        const currentViewbox = canvas.viewbox()
        zoomLevel.value = currentViewbox.scale
      }
    } catch (error) {
      ElMessage.error('初始化流程图失败')
      console.error(error)
    }
  }
})

onBeforeUnmount(() => {
  if (modeler) {
    modeler.destroy()
  }
})

// 缩放功能
const zoomIn = () => {
  const canvas = modeler.get('canvas')
  const currentViewbox = canvas.viewbox()
  const newScale = currentViewbox.scale * 1.1
  canvas.zoom(newScale)
  zoomLevel.value = newScale
}

const zoomOut = () => {
  const canvas = modeler.get('canvas')
  const currentViewbox = canvas.viewbox()
  const newScale = currentViewbox.scale * 0.9
  canvas.zoom(newScale)
  zoomLevel.value = newScale
}

const resetZoom = () => {
  const canvas = modeler.get('canvas')
  canvas.zoom('fit-viewport')
  const currentViewbox = canvas.viewbox()
  zoomLevel.value = currentViewbox.scale
}

// 更新元素名称
const updateElementName = () => {
  if (selectedElement.value && modeler) {
    const modeling = modeler.get('modeling')
    const elementRegistry = modeler.get('elementRegistry')
    const element = elementRegistry.get(selectedElement.value.id)

    if (element) {
      modeling.updateProperties(element, {
        name: selectedElement.value.name
      })
    }
  }
}

// 判断是否为用户任务
const isUserTask = (elementType: string) => {
  return elementType === 'bpmn:UserTask'
}

// 判断是否为服务任务
const isServiceTask = (elementType: string) => {
  return elementType === 'bpmn:ServiceTask'
}

// 判断是否为网关
const isGateway = (elementType: string) => {
  return elementType.includes('Gateway')
}

// 判断是否为事件
const isEvent = (elementType: string) => {
  return elementType.includes('Event')
}

// 监听扩展属性变化
let isInitialSelection = true

watch(() => selectedElement.value?.extensionAttributes, async (newVal) => {
  if (newVal && selectedElement.value && modeler && !isInitialSelection) {
    const elementRegistry = modeler.get('elementRegistry')
    const element = elementRegistry.get(selectedElement.value.id)

    if (element) {
      element.businessObject.extensionAttributes = newVal
    }
  }
  isInitialSelection = false
}, { deep: true })

// 手动保存元素属性到后端
const saveElementProperties = async () => {
  if (!selectedElement.value || !publishedId.value) {
    ElMessage.warning('请先发布流程后再保存属性')
    return
  }

  if (!canEditProperties.value) {
    ElMessage.warning(getEditDisabledReason())
    return
  }

  isSaving.value = true
  try {
    await saveElementExtension({
      processDefinitionId: publishedId.value,
      elementId: selectedElement.value.id,
      elementType: selectedElement.value.type,
      extensionAttributes: selectedElement.value.extensionAttributes
    })
    ElMessage.success('属性保存成功')
  } catch (error) {
    ElMessage.error('属性保存失败')
    console.error('保存扩展属性失败:', error)
  } finally {
    isSaving.value = false
  }
}

// ==================== 流程操作函数 ====================

/**
 * 保存流程模板（设计态）
 */
const saveProcess = async () => {
  if (!processName.value) {
    ElMessage.warning('请输入流程名称')
    return
  }

  if (!canEdit.value) {
    ElMessage.warning('只有设计态模板可以保存')
    return
  }

  isSaving.value = true
  try {
    // 获取最新的 BPMN XML（确保包含同步后的 process key）
    const { xml } = await modeler.saveXML({ format: true })

    // 从 BPMN XML 中提取实际的流程 key
    const actualProcessKey = extractProcessKeyFromXML(xml as string)

    if (!actualProcessKey) {
      ElMessage.error('无法从 BPMN XML 中提取流程 Key')
      return
    }

    // 使用实际的流程 key 更新界面显示
    processKey.value = actualProcessKey

    if (draftId.value) {
      // 更新设计态
      await updateDraftTemplate(draftId.value, {
        templateKey: actualProcessKey,  // 使用 BPMN XML 中的实际 key
        templateName: processName.value,
        description: processDescription.value,
        bpmnXml: xml as string,
        categoryId: selectedCategoryId.value
      })
    } else {
      // 创建设计态
      const response = await createDraftTemplate({
        templateKey: actualProcessKey,  // 使用 BPMN XML 中的实际 key
        templateName: processName.value,
        description: processDescription.value,
        bpmnXml: xml as string,
        categoryId: selectedCategoryId.value || '',
        tenantId: currentTenantId.value,
        appId: currentAppId.value,
        contextId: currentContextId.value
      })
      draftId.value = response.data.data.id
    }

    ElMessage.success('保存成功')
  } catch (error: any) {
    ElMessage.error('保存失败: ' + (error.response?.data?.message || error.message))
    console.error(error)
  } finally {
    isSaving.value = false
  }
}

/**
 * 发布流程模板（设计态 → 发布态）
 */
const publishProcess = async () => {
  if (!draftId.value) {
    ElMessage.error('模板ID不存在，请先保存')
    return
  }

  // 先保存
  await saveProcess()

  isPublishing.value = true
  try {
    const response = await publishTemplate(draftId.value)
    publishedId.value = response.data.data.id
    processStatus.value = 'ACTIVE'
    draftId.value = undefined

    ElMessage.success('发布成功')
  } catch (error: any) {
    ElMessage.error('发布失败: ' + (error.response?.data?.message || error.message))
    console.error(error)
  } finally {
    isPublishing.value = false
  }
}

/**
 * 停用流程模板（激活态 → 停用态）
 */
const suspendProcess = async () => {
  if (!publishedId.value) {
    ElMessage.error('发布态ID不存在')
    return
  }

  try {
    await ElMessageBox.confirm('确定要停用此模板吗？', '确认停用', {
      type: 'warning'
    })

    isSuspending.value = true
    try {
      await suspendTemplate(publishedId.value)
      processStatus.value = 'INACTIVE'

      ElMessage.success('已停用')
    } finally {
      isSuspending.value = false
    }
  } catch {
    // 用户取消
  }
}

/**
 * 激活流程模板（停用态 → 激活态）
 */
const activateProcessFunc = async () => {
  if (!publishedId.value) {
    ElMessage.error('发布态ID不存在')
    return
  }

  isActivating.value = true
  try {
    await apiActivateTemplate(publishedId.value)
    processStatus.value = 'ACTIVE'

    ElMessage.success('已激活')
  } catch (error: any) {
    ElMessage.error('激活失败: ' + (error.response?.data?.message || error.message))
    console.error(error)
  } finally {
    isActivating.value = false
  }
}

// 返回流程列表
const goBackToList = () => {
  router.push('/process')
}

const exportXML = async () => {
  try {
    const { xml } = await modeler.saveXML({ format: true })
    const blob = new Blob([xml], { type: 'application/xml' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${processName.value || 'process'}.bpmn`
    link.click()
    URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('导出失败')
    console.error(error)
  }
}

// 显示快照对话框
const showSnapshotDialog = async () => {
  if (!processKey.value) {
    ElMessage.warning('请先设置模板Key')
    return
  }

  try {
    const response = await listTemplateSnapshots(processKey.value, {
      tenantId: currentTenantId.value,
      appId: currentAppId.value,
      contextId: currentContextId.value
    })
    snapshots.value = response.data.data || []
    snapshotDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取快照列表失败')
    console.error(error)
  }
}

// 显示创建快照对话框
const showCreateSnapshotDialog = () => {
  snapshotForm.value = {
    snapshotName: '',
    description: ''
  }
  createSnapshotDialogVisible.value = true
}

// 创建快照
const createSnapshot = async () => {
  if (!snapshotForm.value.snapshotName) {
    ElMessage.warning('请输入快照名称')
    return
  }

  const sourceTemplateId = processStatus.value === 'DRAFT' ? draftId.value : publishedId.value
  const sourceTemplateType = processStatus.value === 'DRAFT' ? 'DRAFT' : 'PUBLISHED'

  if (!sourceTemplateId) {
    ElMessage.warning('模板ID不存在')
    return
  }

  try {
    await createTemplateSnapshot({
      sourceTemplateId,
      sourceTemplateType,
      snapshotName: snapshotForm.value.snapshotName
    })

    ElMessage.success('快照创建成功')
    createSnapshotDialogVisible.value = false

    // 刷新快照列表
    if (processKey.value) {
      const response = await listTemplateSnapshots(processKey.value, {
        tenantId: currentTenantId.value,
        appId: currentAppId.value,
        contextId: currentContextId.value
      })
      snapshots.value = response.data.data || []
    }
  } catch (error: any) {
    ElMessage.error('创建快照失败: ' + (error.response?.data?.message || error.message))
    console.error(error)
  }
}

// 从快照恢复
const restoreFromSnapshot = async (snapshotId: string) => {
  try {
    await ElMessageBox.confirm(
      '恢复后将创建新的设计态模板，是否继续？',
      '确认恢复',
      { type: 'warning' }
    )

    const response = await apiRestoreFromSnapshot({
      snapshotId,
      newTemplateName: `${processName.value}_restored_${Date.now()}`
    })

    ElMessage.success('恢复成功，跳转到新模板')
    router.push(`/process/designer?draftId=${response.data.data.id}`)
  } catch (error) {
    // 用户取消或出错
  }
}

// 删除快照
const deleteSnapshot = async (snapshotId: string) => {
  try {
    await ElMessageBox.confirm('确定要删除此快照吗？', '确认删除', {
      type: 'warning'
    })

    await deleteTemplateSnapshot(snapshotId)
    ElMessage.success('删除成功')

    // 刷新快照列表
    if (processKey.value) {
      const response = await listTemplateSnapshots(processKey.value, {
        tenantId: currentTenantId.value,
        appId: currentAppId.value,
        contextId: currentContextId.value
      })
      snapshots.value = response.data.data || []
    }
  } catch (error) {
    // 用户取消
  }
}

// 处理快照命令
const handleSnapshotCommand = async (command: string) => {
  if (command === 'listSnapshots') {
    await showSnapshotDialog()
  }
}
</script>

<style scoped>
.designer-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.designer-header {
  background: white;
  padding: 15px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.back-btn {
  background: #f5f7fa;
  border: 1px solid #d8dee7;
  color: #606266;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: #e6e8eb;
  border-color: #c8cdd3;
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.logo {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  animation: pulse 2s infinite;
}

.process-name-input {
  width: 300px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.zoom-controls {
  display: flex;
  align-items: center;
  gap: 5px;
  margin-right: 15px;
}

.zoom-display {
  margin-left: 5px;
  font-size: 14px;
  color: #606266;
}

.operation-buttons {
  display: flex;
  gap: 10px;
  align-items: center;
}

.status-tag {
  font-weight: 500;
  padding: 8px 16px;
}

.save-btn {
  background: #67c23a;
  border-color: #67c23a;
  color: white;
}

.save-btn:hover {
  background: #5daf34;
  border-color: #5daf34;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
  transition: all 0.3s ease;
}

.publish-btn {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: all 0.3s ease;
}

.suspend-btn, .activate-btn {
  color: white;
}

.suspend-btn:hover, .activate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.4);
  transition: all 0.3s ease;
}

.main-content {
  display: flex;
  flex: 1;
  overflow: hidden;
  gap: 0;
}

.canvas-wrapper {
  flex: 2;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
  position: relative;
  overflow: hidden;
}

.canvas-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
    radial-gradient(circle, #d1d5db 1px, transparent 1px);
  background-size: 24px 24px;
  opacity: 0.5;
  pointer-events: none;
}

.canvas-container {
  flex: 1;
  background: white;
  margin: 16px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow:
    0 2px 8px rgba(0, 0, 0, 0.08),
    0 1px 4px rgba(0, 0, 0, 0.06),
    inset 0 0 0 1px rgba(0, 0, 0, 0.04);
  position: relative;
  z-index: 1;
}

.canvas-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.9) 0%, rgba(248, 250, 252, 0.9) 100%);
  pointer-events: none;
  z-index: -1;
}

.right-panel {
  width: 380px;
  background: white;
  padding: 24px;
  border-left: 2px solid #e5e7eb;
  overflow-y: auto;
  animation: slideInRight 0.3s ease;
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.05);
  z-index: 2;
}

.right-panel h3 {
  margin-top: 0;
  margin-bottom: 24px;
  color: #1f2937;
  font-size: 18px;
  font-weight: 600;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e7eb;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding-bottom: 12px;
  border-bottom: 2px solid #e5e7eb;
}

.panel-header h3 {
  margin: 0;
  padding: 0;
  border: none;
}

.save-button-wrapper {
  margin-bottom: 16px;
  padding: 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border: 1px solid #bae6fd;
}

.edit-warning {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
  border-radius: 8px;
  border: 1px solid #fbbf24;
  color: #92400e;
  font-size: 13px;
}

.edit-warning .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.process-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 8px;
  border: 1px solid #bae6fd;
  color: #0369a1;
  font-size: 13px;
  margin-top: 8px;
}

.process-info .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.form-tip {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
  line-height: 1.5;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-radius: 6px;
  border-left: 3px solid #409eff;
}

/* 滚动条美化 */
.right-panel::-webkit-scrollbar {
  width: 6px;
}

.right-panel::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.right-panel::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.right-panel::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

/* 快照对话框样式 */
.snapshot-toolbar {
  margin-bottom: 20px;
}

/* 动画效果 */
@keyframes pulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.05); }
  100% { transform: scale(1); }
}

@keyframes slideInLeft {
  from { opacity: 0; transform: translateX(-20px); }
  to { opacity: 1; transform: translateX(0); }
}

@keyframes slideInRight {
  from { opacity: 0; transform: translateX(20px); }
  to { opacity: 1; transform: translateX(0); }
}
</style>
