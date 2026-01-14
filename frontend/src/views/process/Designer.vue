<template>
  <div class="designer-container">
    <!-- 头部导航栏 -->
    <div class="designer-header">
      <div class="header-left">
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
          <!-- 快照按钮组 -->
          <el-dropdown split-button type="default" @click="showSnapshotDialog" @command="handleSnapshotCommand">
            <el-icon><Document /></el-icon>
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
          <el-button type="primary" @click="deploy" class="publish-btn">
            <el-icon><Upload /></el-icon>
            发布流程
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
        <h3>{{ selectedElement ? '元素属性' : '流程属性' }}</h3>
        
        <!-- 流程模板属性 -->
        <el-form v-if="!selectedElement" label-position="top">
          <el-form-item label="流程名称">
            <el-input v-model="processName" placeholder="请输入流程名称"></el-input>
          </el-form-item>
          <el-form-item label="流程Key">
            <el-input :value="processKey" readonly></el-input>
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
            <el-input v-model="selectedElement.name" @input="updateElementName"></el-input>
          </el-form-item>
          
          <!-- 根据元素类型显示不同的属性配置 -->
          <!-- 用户任务属性 -->
          <template v-if="isUserTask(selectedElement.type)">
            <el-form-item label="执行人">
              <el-input v-model="selectedElement.extensionAttributes.assignee" placeholder="请输入执行人"></el-input>
            </el-form-item>
            <el-form-item label="候选用户">
              <el-input v-model="selectedElement.extensionAttributes.candidateUsers" placeholder="请输入候选用户，多个用逗号分隔"></el-input>
            </el-form-item>
            <el-form-item label="候选组">
              <el-input v-model="selectedElement.extensionAttributes.candidateGroups" placeholder="请输入候选组，多个用逗号分隔"></el-input>
            </el-form-item>
            <el-form-item label="表单Key">
              <el-input v-model="selectedElement.extensionAttributes.formKey" placeholder="请输入表单Key"></el-input>
            </el-form-item>
            <el-form-item label="截止日期">
              <el-input v-model="selectedElement.extensionAttributes.dueDate" placeholder="请输入截止日期表达式"></el-input>
            </el-form-item>
            <el-form-item label="优先级">
              <el-input v-model="selectedElement.extensionAttributes.priority" placeholder="请输入优先级"></el-input>
            </el-form-item>
          </template>
          
          <!-- 服务任务属性 -->
          <template v-else-if="isServiceTask(selectedElement.type)">
            <el-form-item label="实现类">
              <el-input v-model="selectedElement.extensionAttributes.implementation" placeholder="请输入实现类"></el-input>
            </el-form-item>
            <el-form-item label="表达式">
              <el-input v-model="selectedElement.extensionAttributes.expression" placeholder="请输入表达式"></el-input>
            </el-form-item>
            <el-form-item label="代理表达式">
              <el-input v-model="selectedElement.extensionAttributes.delegateExpression" placeholder="请输入代理表达式"></el-input>
            </el-form-item>
            <el-form-item label="结果变量名">
              <el-input v-model="selectedElement.extensionAttributes.resultVariableName" placeholder="请输入结果变量名"></el-input>
            </el-form-item>
            <el-form-item label="任务类型">
              <el-select v-model="selectedElement.extensionAttributes.type" placeholder="请选择任务类型">
                <el-option label="HTTP" value="http"></el-option>
                <el-option label="邮件" value="mail"></el-option>
                <el-option label="其他" value="other"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="异步执行">
              <el-switch v-model="selectedElement.extensionAttributes.async" />
            </el-form-item>
          </template>
          
          <!-- 网关属性 -->
          <template v-else-if="isGateway(selectedElement.type)">
            <el-form-item label="网关类型">
              <el-select v-model="selectedElement.extensionAttributes.gatewayType" placeholder="请选择网关类型">
                <el-option label="排他网关" value="exclusive"></el-option>
                <el-option label="并行网关" value="parallel"></el-option>
                <el-option label="包容网关" value="inclusive"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="默认路径">
              <el-input v-model="selectedElement.extensionAttributes.default" placeholder="请输入默认路径ID"></el-input>
            </el-form-item>
          </template>
          
          <!-- 事件属性 -->
          <template v-else-if="isEvent(selectedElement.type)">
            <el-form-item label="消息引用">
              <el-input v-model="selectedElement.extensionAttributes.messageRef" placeholder="请输入消息引用"></el-input>
            </el-form-item>
            <el-form-item label="定时器配置">
              <el-input v-model="selectedElement.extensionAttributes.timerEventDefinition" placeholder="请输入定时器配置"></el-input>
            </el-form-item>
            <el-form-item label="信号引用">
              <el-input v-model="selectedElement.extensionAttributes.signalRef" placeholder="请输入信号引用"></el-input>
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
import { ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRoute } from 'vue-router'
import { ZoomIn, ZoomOut, Download, Upload, Refresh, Document, Plus, InfoFilled } from '@element-plus/icons-vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import { 
  deployProcess, 
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
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-js.css'

const canvasRef = ref<HTMLElement>()
const processName = ref('')
const processKey = ref('')
const processVersion = ref('1.0')
const processDescription = ref('')
const processDefinitionId = ref<string>('')
let modeler: any = null

const route = useRoute()

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

// 监听扩展属性变化，保存到BPMN元素
// 只在属性值变化时才保存，而不是在选择元素时保存
let isInitialSelection = true

watch(() => selectedElement.value?.extensionAttributes, async (newVal, oldVal) => {
  if (newVal && selectedElement.value && modeler && !isInitialSelection && processDefinitionId.value) {
    const elementRegistry = modeler.get('elementRegistry')
    const element = elementRegistry.get(selectedElement.value.id)
    
    if (element) {
      // 更新扩展属性到元素
      element.businessObject.extensionAttributes = newVal
      
      // 保存扩展属性到后端
      try {
        await saveElementExtension({
          processDefinitionId: processDefinitionId.value,
          elementId: selectedElement.value.id,
          elementType: selectedElement.value.type,
          extensionAttributes: newVal
        })
      } catch (error) {
        console.error('保存扩展属性失败:', error)
      }
    }
  }
  isInitialSelection = false
}, { deep: true })

const deploy = async () => {
  if (!processName.value) {
    ElMessage.warning('请输入流程名称')
    return
  }

  try {
    const { xml } = await modeler.saveXML({ format: true })
    await deployProcess({
      name: processName.value,
      xml: xml as string
    })
    ElMessage.success('流程发布成功')
  } catch (error) {
    ElMessage.error('流程发布失败')
    console.error(error)
  }
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
  
  if (!processName.value) {
    ElMessage.warning('请先设置流程名称');
    return;
  }

  try {
    await createProcessSnapshot({
      processDefinitionKey: processName.value,
      snapshotName: snapshotForm.value.snapshotName,
      description: snapshotForm.value.description,
      creator: snapshotForm.value.creator
    });
    
    ElMessage.success('快照创建成功');
    createSnapshotDialogVisible.value = false;
    
    // 刷新快照列表
    const response = await getProcessSnapshots(processName.value);
    snapshots.value = response.data.data;
  } catch (error: any) {
    ElMessage.error(`创建快照失败: ${error.response?.data?.message || error.message}`);
    console.error(error);
  }
}

// 回滚到指定快照
const rollbackToSnapshot = async (snapshotId: number) => {
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
const deleteSnapshot = async (snapshotId: number) => {
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
}

.publish-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
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
