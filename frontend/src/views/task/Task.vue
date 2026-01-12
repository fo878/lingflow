<template>
  <div class="task-container">
    <div class="page-header">
      <h2 class="page-title">任务办理</h2>
      <div class="header-actions">
        <el-button @click="loadTasks" class="refresh-btn">
          <el-icon><Refresh /></el-icon>
          刷新任务
        </el-button>
      </div>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="table-card">
          <template #header>
            <div class="table-header">
              <h3>待办任务列表</h3>
              <div class="table-tools">
                <el-input
                  v-model="searchQuery"
                  placeholder="搜索任务名称..."
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
            :data="filteredTasks" 
            stripe 
            style="width: 100%"
            class="custom-table"
          >
            <el-table-column prop="name" label="任务名称" width="250">
              <template #default="scope">
                <div class="task-name-cell">
                  <el-icon><Tickets /></el-icon>
                  <span>{{ scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="id" label="任务ID" width="200" />
            <el-table-column prop="processInstanceId" label="流程实例ID" width="300" />
            <el-table-column prop="createTime" label="创建时间" width="180">
              <template #default="scope">
                <div class="time-cell">
                  <el-icon><Clock /></el-icon>
                  <span>{{ formatTime(scope.row.createTime) }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <div class="action-buttons">
                  <el-button 
                    size="small" 
                    type="success" 
                    @click="handleComplete(scope.row)"
                    class="btn-complete"
                  >
                    <el-icon><Check /></el-icon>
                    办理
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
          
          <template #footer v-if="filteredTasks.length === 0">
            <div class="empty-state">
              <el-empty description="暂无待办任务" />
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="completeDialogVisible" title="办理任务" width="600px" class="custom-dialog">
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
            :rows="4"
            placeholder="请输入备注信息（可选）"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="completeDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmComplete">确认办理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getTasks, completeTask } from '@/api/process'
import { Refresh, Check, Search, Tickets, Clock } from '@element-plus/icons-vue'

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

// 搜索功能
const searchQuery = ref('')

// 计算过滤后的任务
const filteredTasks = computed(() => {
  if (!searchQuery.value) {
    return tasks.value
  }
  
  return tasks.value.filter(task => 
    task.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    task.id.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 时间格式化函数
const formatTime = (timeStr: string) => {
  if (!timeStr) return '--'
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN')
}

onMounted(() => {
  loadTasks()
})

const loadTasks = async () => {
  try {
    const response = await getTasks()
    tasks.value = response.data.data
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
  background: linear-gradient(45deg, #67c23a, #409eff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.refresh-btn {
  background: #f5f7fa;
  border: 1px solid #d8dee7;
}

.table-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border: none;
  overflow: hidden;
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
  overflow: hidden;
}

.task-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.task-name-cell .el-icon {
  color: #409eff;
}

.time-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}

.time-cell .el-icon {
  color: #909399;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.btn-complete {
  background-color: #f0f9eb;
  border-color: #c2e7b0;
  color: #67c23a;
}

.btn-complete:hover {
  background-color: #67c23a;
  color: white;
  transition: all 0.3s ease;
}

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px 0;
}

/* 按钮悬停效果 */
.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}
</style>
