<template>
  <div class="process-management-container">
    <div class="page-header">
      <h2 class="page-title">流程管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="createNewProcess" class="action-btn">
          <el-icon><Plus /></el-icon>
          新建流程
        </el-button>
        <el-button @click="loadProcessDefinitions" class="refresh-btn">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>
    
    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="table-card">
          <template #header>
            <div class="table-header">
              <h3>流程定义列表</h3>
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
          >
            <el-table-column prop="name" label="流程名称" width="200">
              <template #default="scope">
                <div class="process-name-cell">
                  <el-icon><Document /></el-icon>
                  <span>{{ scope.row.name }}</span>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="key" label="流程Key" width="200" />
            <el-table-column prop="version" label="版本" width="100">
              <template #default="scope">
                <el-tag type="info" size="small">v{{ scope.row.version }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="deploymentId" label="部署ID" width="250" />
            <el-table-column label="操作" width="300" fixed="right">
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

    <el-dialog v-model="startDialogVisible" title="启动流程" width="500px" class="custom-dialog">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getProcessDefinitions, 
  deleteProcessDefinition, 
  startProcess 
} from '@/api/process'
import { EditPen, Delete, VideoPlay, Plus, Refresh, Search, Document } from '@element-plus/icons-vue'

interface ProcessDefinition {
  id: string
  key: string
  name: string
  version: number
  deploymentId: string
}

const router = useRouter()
const processDefinitions = ref<ProcessDefinition[]>([])
const startDialogVisible = ref(false)
const startForm = ref({
  processKey: '',
  businessKey: ''
})

// 搜索功能
const searchQuery = ref('')

// 计算过滤后的流程定义
const filteredProcessDefinitions = computed(() => {
  if (!searchQuery.value) {
    return processDefinitions.value
  }
  
  return processDefinitions.value.filter(def => 
    def.name.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    def.key.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
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

const createNewProcess = () => {
  router.push('/process/designer')
}

const handleEdit = (row: ProcessDefinition) => {
  // 传递processDefinitionId而不是deploymentId
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
}

.action-btn {
  background: linear-gradient(45deg, #667eea, #764ba2);
  border: none;
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

/* 按钮悬停效果 */
.action-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  transition: all 0.3s ease;
}

.refresh-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
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
