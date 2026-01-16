<template>
  <div class="statistics-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>流程统计</span>
          <el-button type="primary" @click="refreshData">刷新</el-button>
        </div>
      </template>

      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon :size="32"><DataLine /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ instanceStats.totalInstances }}</div>
                <div class="stat-label">总实例数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon running">
                <el-icon :size="32"><VideoPlay /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ instanceStats.runningInstances }}</div>
                <div class="stat-label">运行中</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon completed">
                <el-icon :size="32"><CircleCheck /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ instanceStats.completedInstances }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon rate">
                <el-icon :size="32"><TrendCharts /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ instanceStats.completionRate }}%</div>
                <div class="stat-label">完成率</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 任务统计 -->
      <el-divider content-position="left">任务统计</el-divider>
      <el-row :gutter="20" class="stats-cards">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon total">
                <el-icon :size="32"><List /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ taskStats.totalTasks }}</div>
                <div class="stat-label">总任务数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon pending">
                <el-icon :size="32"><Clock /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ taskStats.pendingTasks }}</div>
                <div class="stat-label">待处理</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon completed">
                <el-icon :size="32"><Select /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ taskStats.completedTasks }}</div>
                <div class="stat-label">已完成</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-content">
              <div class="stat-icon overdue">
                <el-icon :size="32"><Warning /></el-icon>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ taskStats.overdueTasks }}</div>
                <div class="stat-label">已逾期</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 流程定义列表 -->
      <el-divider content-position="left">流程定义统计</el-divider>
      <el-table :data="definitionStats" style="width: 100%" v-loading="loading">
        <el-table-column prop="processDefinitionKey" label="流程Key" width="200" />
        <el-table-column prop="processDefinitionName" label="流程名称" width="200" />
        <el-table-column prop="version" label="版本" width="80" />
        <el-table-column prop="runningInstanceCount" label="运行中" width="100" />
        <el-table-column prop="completedInstanceCount" label="已完成" width="100" />
        <el-table-column prop="totalInstanceCount" label="总实例数" width="120" />
        <el-table-column prop="averageCompletionTime" label="平均完成时间" width="150">
          <template #default="scope">
            {{ scope.row.formattedAverageTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="scope">
            <el-button size="small" @click="viewDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 详情对话框 -->
      <el-dialog v-model="detailVisible" title="统计详情" width="60%">
        <el-descriptions :column="2" border v-if="currentDefinition">
          <el-descriptions-item label="流程Key">
            {{ currentDefinition.processDefinitionKey }}
          </el-descriptions-item>
          <el-descriptions-item label="流程名称">
            {{ currentDefinition.processDefinitionName }}
          </el-descriptions-item>
          <el-descriptions-item label="版本">
            {{ currentDefinition.version }}
          </el-descriptions-item>
          <el-descriptions-item label="部署时间">
            {{ currentDefinition.deploymentTime }}
          </el-descriptions-item>
          <el-descriptions-item label="运行中实例">
            {{ currentDefinition.runningInstanceCount }}
          </el-descriptions-item>
          <el-descriptions-item label="已完成实例">
            {{ currentDefinition.completedInstanceCount }}
          </el-descriptions-item>
          <el-descriptions-item label="总实例数">
            {{ currentDefinition.totalInstanceCount }}
          </el-descriptions-item>
          <el-descriptions-item label="平均完成时间">
            {{ currentDefinition.formattedAverageTime || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <!-- 每日统计图表 -->
        <div v-if="dailyStats.length > 0">
          <h3>每日统计趋势</h3>
          <div ref="chartRef" style="width: 100%; height: 300px"></div>
        </div>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  DataLine,
  VideoPlay,
  CircleCheck,
  TrendCharts,
  List,
  Clock,
  Select,
  Warning
} from '@element-plus/icons-vue'
import {
  getProcessInstanceStatistics,
  getTaskStatistics,
  getProcessDefinitionStatistics,
  getDailyStatistics
} from '@/api/extended'

const loading = ref(false)
const instanceStats = ref({
  totalInstances: 0,
  runningInstances: 0,
  completedInstances: 0,
  suspendedInstances: 0,
  completionRate: 0
})

const taskStats = ref({
  totalTasks: 0,
  pendingTasks: 0,
  completedTasks: 0,
  overdueTasks: 0
})

const definitionStats = ref<any[]>([])
const detailVisible = ref(false)
const currentDefinition = ref<any>(null)
const dailyStats = ref<any[]>([])
const chartRef = ref<HTMLElement>()

// 获取流程实例统计
const fetchInstanceStats = async () => {
  try {
    const response = await getProcessInstanceStatistics()
    instanceStats.value = response.data || instanceStats.value
  } catch (error) {
    console.error('获取流程实例统计失败', error)
  }
}

// 获取任务统计
const fetchTaskStats = async () => {
  try {
    const response = await getTaskStatistics()
    taskStats.value = response.data || taskStats.value
  } catch (error) {
    console.error('获取任务统计失败', error)
  }
}

// 获取流程定义统计
const fetchDefinitionStats = async () => {
  loading.value = true
  try {
    const response = await getProcessDefinitionStatistics()
    definitionStats.value = response.data || []
  } catch (error) {
    ElMessage.error('获取流程定义统计失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取每日统计
const fetchDailyStats = async (processDefinitionKey?: string) => {
  try {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(endDate.getDate() - 30) // 最近30天

    const response = await getDailyStatistics(
      startDate.toISOString().split('T')[0],
      endDate.toISOString().split('T')[0]
    )
    dailyStats.value = response.data || []
  } catch (error) {
    console.error('获取每日统计失败', error)
  }
}

// 刷新数据
const refreshData = () => {
  fetchInstanceStats()
  fetchTaskStats()
  fetchDefinitionStats()
}

// 查看详情
const viewDetail = async (row: any) => {
  currentDefinition.value = row
  detailVisible.value = true
  await fetchDailyStats(row.processDefinitionKey)

  // TODO: 渲染图表
  // renderChart()
}

onMounted(() => {
  fetchInstanceStats()
  fetchTaskStats()
  fetchDefinitionStats()
})
</script>

<style scoped>
.statistics-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.stats-cards {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.stat-icon.total {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-icon.running {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-icon.completed {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-icon.pending {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-icon.rate {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
}

.stat-icon.overdue {
  background: linear-gradient(135deg, #ff0844 0%, #ffb199 100%);
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  line-height: 1.2;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.box-card {
  margin-bottom: 20px;
}
</style>
