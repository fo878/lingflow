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

    <el-dialog v-model="diagramDialogVisible" title="流程图" width="80%" top="5vh">
      <div class="diagram-container">
        <img :src="diagramUrl" alt="流程图" style="width: 100%; height: auto;" />
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getRunningInstances,
  getCompletedInstances,
  getProcessDiagram
} from '@/api/process'

interface ProcessInstance {
  id: string
  processDefinitionId: string
  businessKey: string
  startTime: string
  endTime?: string
}

const activeTab = ref('running')
const runningInstances = ref<ProcessInstance[]>([])
const completedInstances = ref<ProcessInstance[]>([])
const diagramDialogVisible = ref(false)
const diagramUrl = ref('')

onMounted(() => {
  loadRunningInstances()
  loadCompletedInstances()
})

const loadRunningInstances = async () => {
  try {
    const response = await getRunningInstances()
    runningInstances.value = response.data
  } catch (error) {
    ElMessage.error('加载运行中流程失败')
    console.error(error)
  }
}

const loadCompletedInstances = async () => {
  try {
    const response = await getCompletedInstances()
    completedInstances.value = response.data
  } catch (error) {
    ElMessage.error('加载已完结流程失败')
    console.error(error)
  }
}

const viewDiagram = async (processInstanceId: string) => {
  try {
    const response = await getProcessDiagram(processInstanceId)
    const blob = new Blob([response.data], { type: 'image/svg+xml' })
    diagramUrl.value = URL.createObjectURL(blob)
    diagramDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取流程图失败')
    console.error(error)
  }
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
  min-height: 500px;
  background: #f5f5f5;
  border-radius: 4px;
  padding: 20px;
}
</style>
