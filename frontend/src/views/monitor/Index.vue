<template>
  <div class="monitor-container">
    <el-tabs v-model="activeTab">
      <el-tab-pane label="运行中流程" name="running">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>运行中流程实例</span>
              <el-button type="primary" size="small" @click="loadRunningInstances">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          <el-table :data="runningInstances" stripe style="width: 100%">
            <el-table-column prop="id" label="流程实例ID" width="250" />
            <el-table-column prop="processDefinitionId" label="流程定义ID" width="300" />
            <el-table-column prop="businessKey" label="业务Key" width="180" />
            <el-table-column prop="startTime" label="开始时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="viewDiagram(scope.row.id)">
                  <el-icon><View /></el-icon>
                  查看流程图
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <el-tab-pane label="已完结流程" name="completed">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>已完结流程实例</span>
              <el-button type="primary" size="small" @click="loadCompletedInstances">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
            </div>
          </template>
          <el-table :data="completedInstances" stripe style="width: 100%">
            <el-table-column prop="id" label="流程实例ID" width="250" />
            <el-table-column prop="processDefinitionId" label="流程定义ID" width="300" />
            <el-table-column prop="businessKey" label="业务Key" width="180" />
            <el-table-column prop="startTime" label="开始时间" width="180" />
            <el-table-column prop="endTime" label="结束时间" width="180" />
            <el-table-column label="操作" width="150">
              <template #default="scope">
                <el-button size="small" type="primary" @click="viewDiagram(scope.row.id)">
                  <el-icon><View /></el-icon>
                  查看流程图
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="diagramDialogVisible" title="流程图" width="90%" top="5vh" @close="closeDiagram">
      <div class="diagram-container" ref="canvasRef"></div>
    </el-dialog>

    <!-- 节点信息悬浮框 -->
    <div
      v-if="nodeTooltip.visible"
      class="node-tooltip"
      :style="{ left: nodeTooltip.x + 'px', top: nodeTooltip.y + 'px' }"
    >
      <div class="tooltip-header">节点信息</div>
      <div class="tooltip-content">
        <div v-if="nodeTooltip.data.taskName">
          <strong>任务名称:</strong> {{ nodeTooltip.data.taskName }}
        </div>
        <div v-if="nodeTooltip.data.assignee">
          <strong>办理人:</strong> {{ nodeTooltip.data.assignee }}
        </div>
        <div v-if="nodeTooltip.data.startTime">
          <strong>开始时间:</strong> {{ formatDate(nodeTooltip.data.startTime) }}
        </div>
        <div v-if="nodeTooltip.data.endTime">
          <strong>结束时间:</strong> {{ formatDate(nodeTooltip.data.endTime) }}
        </div>
        <div v-if="nodeTooltip.data.duration">
          <strong>处理时长:</strong> {{ formatDuration(nodeTooltip.data.duration) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { Refresh, View } from '@element-plus/icons-vue'
import {
  getRunningInstances,
  getCompletedInstances,
  getProcessBpmn
} from '@/api/process'
import NavigatedViewer from 'bpmn-js/lib/NavigatedViewer'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import 'bpmn-js/dist/assets/bpmn-js.css'

interface ProcessInstance {
  id: string
  processDefinitionId: string
  businessKey: string
  startTime: string
  endTime?: string
}

const route = useRoute()
const router = useRouter()
const activeTab = ref('running')
const runningInstances = ref<ProcessInstance[]>([])
const completedInstances = ref<ProcessInstance[]>([])
const diagramDialogVisible = ref(false)
const canvasRef = ref<HTMLElement>()

let viewer: any = null
let nodeInfoMap: Map<string, any> = new Map()
const nodeTooltip = ref({
  visible: false,
  x: 0,
  y: 0,
  data: {} as any
})

// 根据URL哈希值设置默认选项卡
onMounted(() => {
  const hash = window.location.hash
  if (hash.includes('completed')) {
    activeTab.value = 'completed'
  } else {
    activeTab.value = 'running'
  }
  
  loadRunningInstances()
  loadCompletedInstances()
})

// 监听选项卡变化，更新URL哈希
watch(activeTab, (newTab) => {
  if (newTab === 'completed') {
    router.replace({ hash: '#completed' })
  } else {
    router.replace({ hash: '#running' })
  }
})

onBeforeUnmount(() => {
  if (viewer) {
    viewer.destroy()
  }
})

const loadRunningInstances = async () => {
  try {
    const response = await getRunningInstances()
    runningInstances.value = response.data.data
  } catch (error) {
    ElMessage.error('加载运行中流程失败')
    console.error(error)
  }
}

const loadCompletedInstances = async () => {
  try {
    const response = await getCompletedInstances()
    completedInstances.value = response.data.data
  } catch (error) {
    ElMessage.error('加载已完结流程失败')
    console.error(error)
  }
}

const viewDiagram = async (processInstanceId: string) => {
  try {
    const response = await getProcessBpmn(processInstanceId)
    const { bpmnXml, nodeInfo, activeActivityIds } = response.data.data

    // 保存节点信息
    nodeInfoMap = new Map(Object.entries(nodeInfo))

    diagramDialogVisible.value = true

    // 等待对话框打开后初始化viewer
    setTimeout(async () => {
      if (canvasRef.value) {
        // 如果之前有viewer实例，先销毁
        if (viewer) {
          viewer.destroy()
        }

        viewer = new NavigatedViewer({
          container: canvasRef.value
        })

        try {
          await viewer.importXML(bpmnXml)

          // 高亮当前活动节点
          if (activeActivityIds && activeActivityIds.length > 0) {
            const canvas = viewer.get('canvas')
            const elementRegistry = viewer.get('elementRegistry')
            
            activeActivityIds.forEach((activityId: string) => {
              const element = elementRegistry.get(activityId)
              if (element) {
                canvas.addMarker(element, 'highlight')
              }
            })
          }

          // 自适应缩放
          const canvas = viewer.get('canvas')
          canvas.zoom('fit-viewport')

          // 添加节点悬浮事件监听
          const eventBus = viewer.get('eventBus')
          eventBus.on('element.hover', (e: any) => {
            if (e.element && e.element.id && nodeInfoMap.has(e.element.id)) {
              showNodeTooltip(e, nodeInfoMap.get(e.element.id))
            }
          })

          eventBus.on('element.out', () => {
            hideNodeTooltip()
          })
        } catch (error) {
          ElMessage.error('加载流程图失败')
          console.error(error)
        }
      }
    }, 100)
  } catch (error) {
    ElMessage.error('获取流程图失败')
    console.error(error)
  }
}

const closeDiagram = () => {
  diagramDialogVisible.value = false
  if (viewer) {
    viewer.destroy()
    viewer = null
  }
}

const showNodeTooltip = (event: any, data: any) => {
  nodeTooltip.value = {
    visible: true,
    x: event.originalEvent.clientX + 10,
    y: event.originalEvent.clientY + 10,
    data: data
  }
}

const hideNodeTooltip = () => {
  nodeTooltip.value.visible = false
}

const formatDate = (date: string) => {
  if (!date) return '-'
  const d = new Date(date)
  return d.toLocaleString('zh-CN')
}

const formatDuration = (duration: number) => {
  if (!duration) return '-'
  const minutes = Math.floor(duration / 60000)
  const seconds = Math.floor((duration % 60000) / 1000)
  if (minutes > 0) {
    return `${minutes}分${seconds}秒`
  }
  return `${seconds}秒`
}
</script>

<style scoped>
.monitor-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.diagram-container {
  width: 100%;
  height: 70vh;
  background: #f5f5f5;
  border-radius: 4px;
  overflow: hidden;
}

.node-tooltip {
  position: fixed;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 10px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 9999;
  min-width: 200px;
  max-width: 300px;
}

.tooltip-header {
  font-weight: bold;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;
  color: #303133;
}

.tooltip-content {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
}

.tooltip-content > div {
  margin-bottom: 4px;
}

.tooltip-content strong {
  color: #303133;
}

/* BPMN高亮样式 */
:deep(.highlight .djs-visual > :nth-child(1)) {
  fill: #e6f7ff !important;
  stroke: #1890ff !important;
  stroke-width: 2px !important;
}

:deep(.highlight.djs-shape .djs-visual > :nth-child(1)) {
  fill: #e6f7ff !important;
  stroke: #1890ff !important;
  stroke-width: 2px !important;
}
</style>