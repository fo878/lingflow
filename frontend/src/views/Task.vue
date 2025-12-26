<template>
  <div class="task-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>待办任务</span>
          <el-button type="primary" size="small" @click="loadTasks">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </template>
      <el-table :data="tasks" stripe style="width: 100%">
        <el-table-column prop="id" label="任务ID" width="180" />
        <el-table-column prop="name" label="任务名称" width="180" />
        <el-table-column prop="processInstanceId" label="流程实例ID" width="250" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button size="small" type="success" @click="handleComplete(scope.row)">
              <el-icon><Check /></el-icon>
              办理
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="completeDialogVisible" title="办理任务" width="500px">
      <el-form :model="completeForm" label-width="100px">
        <el-form-item label="任务ID">
          <el-input v-model="completeForm.taskId" disabled />
        </el-form-item>
        <el-form-item label="任务名称">
          <el-input v-model="completeForm.taskName" disabled />
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="completeForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息（可选）"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getTasks, completeTask } from '@/api/process'

interface Task {
  id: string
  name: string
  processInstanceId: string
  createTime: string
}

const tasks = ref<Task[]>([])
const completeDialogVisible = ref(false)
const completeForm = ref({
  taskId: '',
  taskName: '',
  remark: ''
})

onMounted(() => {
  loadTasks()
})

const loadTasks = async () => {
  try {
    const response = await getTasks()
    tasks.value = response.data
  } catch (error) {
    ElMessage.error('加载任务失败')
    console.error(error)
  }
}

const handleComplete = (row: Task) => {
  completeForm.value.taskId = row.id
  completeForm.value.taskName = row.name
  completeForm.value.remark = ''
  completeDialogVisible.value = true
}

const confirmComplete = async () => {
  try {
    const variables = completeForm.value.remark
      ? { remark: completeForm.value.remark }
      : undefined
    await completeTask(completeForm.value.taskId, variables)
    ElMessage.success('任务办理成功')
    completeDialogVisible.value = false
    loadTasks()
  } catch (error) {
    ElMessage.error('任务办理失败')
    console.error(error)
  }
}
</script>

<style scoped>
.task-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
