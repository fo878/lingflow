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
          <el-tag v-if="processStatus !== 'draft'"
                  :type="processStatus === 'active' ? 'success' : 'warning'"
                  size="large"
                  class="status-tag">
            {{ processStatus === 'active' ? '已发布' : '已停用' }}
          </el-tag>
          <el-tag v-else type="info" size="large" class="status-tag">草稿</el-tag>

          <!-- 保存按钮 -->
          <el-button @click="saveProcess" :loading="isSaving" class="save-btn">
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

          <!-- 发布/停用按钮组 -->
          <el-dropdown v-if="processStatus === 'draft' || processStatus === 'suspended'"
                      split-button
                      type="primary"
                      @click="publishProcess"
                      :loading="isPublishing"
                      class="publish-btn"
                      @command="handlePublishCommand">
            <el-icon><VideoPlay /></el-icon>
            {{ processStatus === 'draft' ? '发布流程' : '重新激活' }}
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-if="processStatus === 'draft'" command="publishAndActivate">
                  <el-icon><CircleCheck /></el-icon>
                  发布并激活
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-button v-else
                    type="warning"
                    @click="suspendProcess"
                    :loading="isSuspending"
                    class="suspend-btn">
            <el-icon><VideoPause /></el-icon>
            停用流程
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
          <el-tag v-if="!selectedElement && processStatus !== 'draft'"
                  :type="processStatus === 'active' ? 'success' : 'warning'"
                  size="small">
            {{ processStatus === 'active' ? '发布态' : '停用态' }}
          </el-tag>
          <el-tag v-else-if="!selectedElement" type="info" size="small">设计态</el-tag>
        </div>

        <!-- 保存按钮（仅在选中元素时显示） -->
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
            <el-input v-model="processName" placeholder="请输入流程名称"></el-input>
          </el-form-item>
          <el-form-item label="流程Key">
            <el-input v-model="processKey" placeholder="请输入流程Key"></el-input>
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
        <el-button type="primary" @click="showCreateSnapshotDialog">
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
        <el-table-column prop="creator" label="创建人" width="120"></el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180"></el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="rollbackToSnapshot(row.id)">
              回滚
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
        <el-form-item label="创建人">
          <el-input v-model="snapshotForm.creator" placeholder="请输入创建人姓名"></el-input>
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
  Upload,
  Refresh,
  Document,
  DocumentCopy,
  Plus,
  InfoFilled,
  ArrowLeft,
  VideoPlay,
  VideoPause,
  CircleCheck
} from '@element-plus/icons-vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import {
  saveProcessDefinition,
  updateProcessDefinition,
  activateProcessDefinition,
  suspendProcessDefinition,
  getProcessDefinitions,
  getProcessDefinitionXml,
  createProcessSnapshot,
  getProcessSnapshots,
  rollbackToSnapshot as apiRollbackToSnapshot,
  deleteSnapshot as apiDeleteSnapshot,
  saveElementExtension,
  getElementExtension,
  batchSaveElementExtensions,
  getAllElementExtensions
} from '@/api/process'
import {
  getCategoryTree,
  setProcessCategory
} from '@/api/processCategory'
import type { ProcessCategoryTree } from '@/api/processCategory'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-js.css'

const canvasRef = ref<HTMLElement>()
const processName = ref('')
const processKey = ref('')
const processVersion = ref('1.0')
const processDescription = ref('')
const processDefinitionId = ref<string>('')
const processSuspended = ref<boolean>(false)
let modeler: any = null

// 流程状态：'draft'（设计态） | 'active'（发布态） | 'suspended'（停用态）
const processStatus = ref<'draft' | 'active' | 'suspended'>('draft')

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

// 获取URL参数
const queryParams = route.query
if (queryParams.name) {
  processName.value = decodeURIComponent(queryParams.name as string)
  processKey.value = processName.value.replace(/\s+/g, '_').toLowerCase()
}

// 如果是编辑模式且提供了流程定义ID，可以加载对应的流程图
if (queryParams.id) {
  // 延迟加载以确保modeler已经初始化
  nextTick(() => {
    loadExistingProcess(queryParams.id as string)
  })
}

// 缩放级别
const zoomLevel = ref(1)

// 选中的元素
const selectedElement = ref<any>(null)

// 加载状态
const isSaving = ref(false)
const isPublishing = ref(false)
const isSuspending = ref(false)

// 计算是否可以编辑属性
// 设计态: 可以编辑所有属性
// 发布态: 只能编辑不影响流程运行的属性（描述、表单Key、截止日期、优先级等）
// 停用态: 不能编辑任何属性
const canEditProperties = computed(() => {
  if (processStatus.value === 'draft') {
    return true
  }
  if (processStatus.value === 'suspended') {
    return false
  }
  // 发布态：可以编辑部分属性
  return true
})

// 判断某个属性是否可编辑
const isPropertyEditable = (propertyName: string) => {
  if (processStatus.value === 'draft') {
    return true
  }
  if (processStatus.value === 'suspended') {
    return false
  }
  // 发布态不可编辑的属性（影响流程运行的属性）
  const readonlyProperties = [
    'id', 'type', 'name', 'assignee', 'candidateUsers', 'candidateGroups',
    'implementation', 'expression', 'delegateExpression', 'gatewayType', 'default',
    'messageRef', 'timerEventDefinition', 'signalRef', 'async', 'type'
  ]
  return !readonlyProperties.includes(propertyName)
}

// 获取不可编辑的原因
const getEditDisabledReason = () => {
  if (processStatus.value === 'suspended') {
    return '流程已停用，不能修改任何属性'
  }
  return '当前属性不可编辑'
}

// 加载现有流程定义
const loadExistingProcess = async (processDefId: string) => {
  try {
    console.log('开始加载流程定义:', processDefId)

    // 检查processDefId是否为空
    if (!processDefId || processDefId.trim() === '') {
      ElMessage.warning('流程定义ID为空，将创建新流程')
      if (modeler) {
        await modeler.importXML(initialXML)
        const canvas = modeler.get('canvas')
        canvas.zoom('fit-viewport')
        const currentViewbox = canvas.viewbox()
        zoomLevel.value = currentViewbox.scale
      }
      processStatus.value = 'draft'
      return
    }

    const response = await getProcessDefinitionXml(processDefId)

    // 检查响应数据是否存在
    if (!response || !response.data || !response.data.data) {
      console.error('响应数据为空:', response)
      throw new Error('获取流程定义失败：服务器返回数据为空')
    }

    // 检查是否有错误
    if (response.data.data.error) {
      console.error('流程定义错误:', response.data.data.error)
      throw new Error(response.data.data.error)
    }

    const xml = response.data.data.bpmnXml
    if (!xml) {
      throw new Error('流程XML为空')
    }

    if (modeler) {
      await modeler.importXML(xml)

      // 保存流程定义ID
      processDefinitionId.value = processDefId

      // 设置流程属性
      if (response.data.data.name) {
        processName.value = response.data.data.name
        processKey.value = response.data.data.key || processName.value.replace(/\s+/g, '_').toLowerCase()
      }
      if (response.data.data.version) {
        processVersion.value = response.data.data.version
      }
      if (response.data.data.description) {
        processDescription.value = response.data.data.description
      }

      // 设置流程状态
      processSuspended.value = response.data.data.suspended || false
      if (processSuspended.value) {
        processStatus.value = 'suspended'
      } else {
        processStatus.value = 'active'
      }

      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')

      // 更新缩放级别
      const currentViewbox = canvas.viewbox()
      zoomLevel.value = currentViewbox.scale

      // 加载扩展属性
      await loadElementExtensions(processDefId)

      ElMessage.success('流程加载成功')
    }
  } catch (error: any) {
    console.error('加载现有流程失败:', error)
    const errorMsg = error.response?.data?.message || error.message || '加载流程失败'

    // 显示详细的错误提示
    if (error.message && error.message.includes('流程定义不存在')) {
      ElMessage.error(`流程不存在或已被删除，将创建新流程`)
    } else if (error.response && error.response.status === 404) {
      ElMessage.error(`流程未找到 (ID: ${processDefId})，将创建新流程`)
    } else {
      ElMessage.error(`加载流程失败: ${errorMsg}`)
    }

    // 如果加载失败，仍使用初始XML并清空流程定义ID
    processDefinitionId.value = ''
    processStatus.value = 'draft'
    if (modeler) {
      await modeler.importXML(initialXML)
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')
      const currentViewbox = canvas.viewbox()
      zoomLevel.value = currentViewbox.scale
    }
  }
}

// 加载元素扩展属性
const loadElementExtensions = async (processDefinitionId: string) => {
  try {
    const response = await getAllElementExtensions(processDefinitionId)
    const extensions = response.data.data

    // 将扩展属性映射到元素
    extensions.forEach((ext: any) => {
      // 在模型中查找对应元素
      const element = modeler.get('elementRegistry').get(ext.elementId)
      if (element) {
        // 将扩展属性存储在元素的自定义属性中
        element.businessObject.extensionAttributes = ext.extensionAttributes
      }
    })
  } catch (error) {
    console.error('Failed to load element extensions:', error)
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

// 初始空白流程XML（不含任何元素）
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
const snapshots = ref([])
const snapshotForm = ref({
  snapshotName: '',
  description: '',
  creator: ''
})

onMounted(async () => {
  // 加载分类树
  await loadCategoryTree()

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

        // 如果有 processDefinitionId，从后端加载该元素的扩展属性
        let extensionAttributes = {}
        if (processDefinitionId.value) {
          try {
            const response = await getElementExtension(processDefinitionId.value, element.id)
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
      await modeler.importXML(initialXML)
      const canvas = modeler.get('canvas')
      canvas.zoom('fit-viewport')

      // 更新缩放级别
      const currentViewbox = canvas.viewbox()
      zoomLevel.value = currentViewbox.scale
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

// 监听扩展属性变化，保存到BPMN元素本地（不再自动保存到后端）
let isInitialSelection = true

watch(() => selectedElement.value?.extensionAttributes, async (newVal) => {
  if (newVal && selectedElement.value && modeler && !isInitialSelection) {
    const elementRegistry = modeler.get('elementRegistry')
    const element = elementRegistry.get(selectedElement.value.id)

    if (element) {
      // 只更新到元素本地，不保存到后端
      element.businessObject.extensionAttributes = newVal
    }
  }
  isInitialSelection = false
}, { deep: true })

// 手动保存元素属性到后端
const saveElementProperties = async () => {
  if (!selectedElement.value || !processDefinitionId.value) {
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
      processDefinitionId: processDefinitionId.value,
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
 * 保存流程定义（草稿）
 */
const saveProcess = async () => {
  if (!processName.value) {
    ElMessage.warning('请输入流程名称')
    return
  }

  isSaving.value = true
  try {
    const { xml } = await modeler.saveXML({ format: true })

    const response = await saveProcessDefinition({
      id: processDefinitionId.value, // 编辑时需要ID
      name: processName.value,
      key: processKey.value || processName.value.replace(/\s+/g, '_').toLowerCase(),
      description: processDescription.value,
      xml: xml as string,
      categoryId: selectedCategoryId.value,
      tenantId: currentTenantId.value,
      appId: currentAppId.value,
      contextId: currentContextId.value
    })

    // 保存返回的流程定义ID
    if (response.data.data && response.data.data.id) {
      processDefinitionId.value = response.data.data.id
    }

    ElMessage.success('流程保存成功')
  } catch (error) {
    ElMessage.error('流程保存失败')
    console.error(error)
  } finally {
    isSaving.value = false
  }
}

/**
 * 发布/激活流程
 */
const publishProcess = async () => {
  if (!processName.value) {
    ElMessage.warning('请输入流程名称')
    return
  }

  // 如果是草稿状态，先保存
  if (processStatus.value === 'draft') {
    await saveProcess()
  }

  if (!processDefinitionId.value) {
    ElMessage.error('流程定义ID不存在，请先保存流程')
    return
  }

  isPublishing.value = true
  try {
    await activateProcessDefinition(processDefinitionId.value)
    processStatus.value = 'active'
    processSuspended.value = false

    ElMessage.success('流程发布成功')
  } catch (error) {
    ElMessage.error('流程发布失败')
    console.error(error)
  } finally {
    isPublishing.value = false
  }
}

/**
 * 停用流程
 */
const suspendProcess = async () => {
  if (!processDefinitionId.value) {
    ElMessage.error('流程定义ID不存在')
    return
  }

  try {
    await ElMessageBox.confirm('确定要停用此流程吗？停用后将无法启动新的流程实例。', '确认停用', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    isSuspending.value = true
    try {
      await suspendProcessDefinition(processDefinitionId.value)
      processStatus.value = 'suspended'
      processSuspended.value = true

      ElMessage.success('流程已停用')
    } catch (error) {
      ElMessage.error('停用流程失败')
      console.error(error)
    } finally {
      isSuspending.value = false
    }
  } catch {
    // 用户取消
  }
}

/**
 * 处理发布命令
 */
const handlePublishCommand = async (command: string) => {
  if (command === 'publishAndActivate') {
    await publishProcess()
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

const saveXML = async () => {
  try {
    const { xml } = await modeler.saveXML({ format: true })
    console.log('Saved XML:', xml)
    ElMessage.success('XML已保存到控制台')
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  }
}

// 显示快照对话框
const showSnapshotDialog = async () => {
  if (!processName.value) {
    ElMessage.warning('请先设置流程名称');
    return;
  }

  try {
    const response = await getProcessSnapshots(processName.value);
    snapshots.value = response.data.data;
    snapshotDialogVisible.value = true;
  } catch (error) {
    ElMessage.error('获取快照列表失败');
    console.error(error);
  }
}

// 显示创建快照对话框
const showCreateSnapshotDialog = () => {
  snapshotForm.value = {
    snapshotName: '',
    description: '',
    creator: ''
  };
  createSnapshotDialogVisible.value = true;
}

// 创建快照
const createSnapshot = async () => {
  if (!snapshotForm.value.snapshotName) {
    ElMessage.warning('请输入快照名称');
    return;
  }

  if (!processKey.value) {
    ElMessage.warning('请先设置流程Key');
    return;
  }

  try {
    await createProcessSnapshot({
      processDefinitionKey: processKey.value,
      snapshotName: snapshotForm.value.snapshotName,
      description: snapshotForm.value.description,
      creator: snapshotForm.value.creator
    });

    ElMessage.success('快照创建成功');
    createSnapshotDialogVisible.value = false;

    // 刷新快照列表
    const response = await getProcessSnapshots(processKey.value);
    snapshots.value = response.data.data;
  } catch (error: any) {
    ElMessage.error(`创建快照失败: ${error.response?.data?.message || error.message}`);
    console.error(error);
  }
}

// 回滚到指定快照
const rollbackToSnapshot = async (snapshotId: string) => {
  try {
    await ElMessageBox.confirm(
      '确认要回滚到此快照吗？此操作不可逆！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await apiRollbackToSnapshot(snapshotId);
    ElMessage.success('回滚成功');
    snapshotDialogVisible.value = false;

    // 获取最新的流程定义并重新加载
    const latestDefinition = await getProcessDefinitions();
    const latestProcess = latestDefinition.data.data.find((p: any) => p.key === processName.value);
    if (latestProcess) {
      await loadExistingProcess(latestProcess.id);
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`回滚失败: ${error.response?.data?.message || error.message}`);
      console.error(error);
    }
  }
}

// 删除快照
const deleteSnapshot = async (snapshotId: string) => {
  try {
    await ElMessageBox.confirm(
      '确认要删除此快照吗？此操作不可逆！',
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    );

    await apiDeleteSnapshot(snapshotId);
    ElMessage.success('删除成功');

    // 刷新快照列表
    if (processName.value) {
      const response = await getProcessSnapshots(processName.value);
      snapshots.value = response.data.data;
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(`删除失败: ${error.response?.data?.message || error.message}`);
      console.error(error);
    }
  }
}

// 处理快照命令
const handleSnapshotCommand = async (command: string) => {
  if (command === 'listSnapshots') {
    await showSnapshotDialog();
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

.suspend-btn {
  background: #e6a23c;
  border-color: #e6a23c;
  color: white;
}

.suspend-btn:hover {
  background: #d99a2e;
  border-color: #d99a2e;
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
