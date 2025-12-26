<template>
  <div class="process-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>流程定义</span>
          <el-button type="primary" size="small" @click="loadProcessDefinitions">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="processDefinitions" stripe style="width: 100%">
        <el-table-column prop="key" label="流程Key" width="180" />
        <el-table-column prop="name" label="流程名称" width="180" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="deploymentId" label="部署ID" width="180" />
        <el-table-column label="操作" width="300">
          <template #default="scope">
            <el-button size="small" type="primary" @click="handleStart(scope.row)">
              <el-icon><VideoPlay /></el-icon>
              启动流程
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(scope.row)">
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="startDialogVisible" title="启动流程" width="500px">
      <el-form :model="startForm" label-width="80px">
        <el-form-item label="流程Key">
          <el-input v-model="startForm.processKey" disabled />
        </el-form-item>
        <el-form-item label="业务Key">
          <el-input v-model="startForm.businessKey" placeholder="请输入业务Key（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="startDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmStart">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getProcessDefinitions,
  deleteProcessDefinition,
  startProcess
} from '@/api/process'

interface ProcessDefinition {
  key: string
  name: string
  version: number
  deploymentId: string
}

const processDefinitions = ref<ProcessDefinition[]>([])
const startDialogVisible = ref(false)
const startForm = ref({
  processKey: '',
  businessKey: ''
})

onMounted(() => {
  loadProcessDefinitions()
})

const loadProcessDefinitions = async () => {
  try {
    const response = await getProcessDefinitions()
    processDefinitions.value = response.data.data
  } catch (error) {
    ElMessage.error('加载流程定义失败')
    console.error(error)
  }
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
</script>

<style scoped>
.process-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
