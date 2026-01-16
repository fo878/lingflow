<template>
  <div class="notification-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>消息通知</span>
          <div class="header-actions">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="item">
              <el-button type="primary" @click="markAllAsRead" :disabled="unreadCount === 0">
                全部标记为已读
              </el-button>
            </el-badge>
            <el-button @click="refreshData">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 通知列表 -->
      <el-list :data="notifications" class="notification-list">
        <el-list-item v-for="notification in notifications" :key="notification.id">
          <div class="notification-item" :class="{ unread: !notification.read }">
            <div class="notification-icon">
              <el-icon v-if="notification.type === 'TASK_ASSIGNED'" class="is-primary">
                <Bell />
              </el-icon>
              <el-icon v-else-if="notification.type === 'TASK_COMPLETED'" class="is-success">
                <CircleCheck />
              </el-icon>
              <el-icon v-else-if="notification.type === 'PROCESS_TIMEOUT'" class="is-warning">
                <Warning />
              </el-icon>
              <el-icon v-else-if="notification.type === 'PROCESS_APPROVED'" class="is-success">
                <CircleCheck />
              </el-icon>
              <el-icon v-else-if="notification.type === 'PROCESS_REJECTED'" class="is-danger">
                <CircleClose />
              </el-icon>
              <el-icon v-else class="is-info">
                <Message />
              </el-icon>
            </div>

            <div class="notification-content">
              <div class="notification-title">
                {{ notification.title }}
                <el-tag v-if="!notification.read" size="small" type="danger">未读</el-tag>
              </div>
              <div class="notification-text">{{ notification.content }}</div>
              <div class="notification-meta">
                <span class="time">{{ notification.createTime }}</span>
                <span v-if="notification.processInstanceId" class="process-info">
                  流程ID: {{ notification.processInstanceId }}
                </span>
              </div>
            </div>

            <div class="notification-actions">
              <el-button
                v-if="!notification.read"
                size="small"
                type="primary"
                link
                @click="markAsRead(notification.id)"
              >
                标记已读
              </el-button>
              <el-button
                v-if="notification.processInstanceId"
                size="small"
                link
                @click="viewProcess(notification.processInstanceId)"
              >
                查看流程
              </el-button>
            </div>
          </div>
        </el-list-item>
      </el-list>

      <!-- 空状态 -->
      <el-empty v-if="notifications.length === 0" description="暂无通知" />

      <!-- 分页 -->
      <div class="pagination-container" v-if="total > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Bell,
  CircleCheck,
  Warning,
  CircleClose,
  Message
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import {
  getUserNotifications,
  getUnreadNotificationCount,
  markNotificationAsRead,
  markAllNotificationsAsRead
} from '@/api/extended'

const router = useRouter()

const notifications = ref<any[]>([])
const unreadCount = ref(0)
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 获取用户ID（实际应从用户状态管理中获取）
const userId = 'user001'

// 获取通知列表
const fetchNotifications = async () => {
  loading.value = true
  try {
    const response = await getUserNotifications(userId, pageSize.value)
    notifications.value = response.data || []
    total.value = notifications.value.length
  } catch (error) {
    ElMessage.error('获取通知列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 获取未读数量
const fetchUnreadCount = async () => {
  try {
    const response = await getUnreadNotificationCount(userId)
    unreadCount.value = response.data || 0
  } catch (error) {
    console.error('获取未读数量失败', error)
  }
}

// 刷新数据
const refreshData = () => {
  fetchNotifications()
  fetchUnreadCount()
}

// 标记单个通知为已读
const markAsRead = async (notificationId: string) => {
  try {
    await markNotificationAsRead(userId, notificationId)
    // 更新本地状态
    const notification = notifications.value.find(n => n.id === notificationId)
    if (notification) {
      notification.read = true
    }
    // 更新未读数量
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    ElMessage.success('已标记为已读')
  } catch (error) {
    ElMessage.error('标记已读失败')
    console.error(error)
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  try {
    const count = await markAllNotificationsAsRead(userId)
    // 更新本地状态
    notifications.value.forEach(n => {
      n.read = true
    })
    unreadCount.value = 0
    ElMessage.success(`已标记${count.data}条通知为已读`)
  } catch (error) {
    ElMessage.error('批量标记已读失败')
    console.error(error)
  }
}

// 查看流程
const viewProcess = (processInstanceId: string) => {
  router.push({
    path: '/monitor',
    query: { processInstanceId }
  })
}

// 分页大小改变
const handleSizeChange = (val: number) => {
  pageSize.value = val
  fetchNotifications()
}

// 当前页改变
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchNotifications()
}

onMounted(() => {
  fetchNotifications()
  fetchUnreadCount()
})
</script>

<style scoped>
.notification-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.notification-list {
  min-height: 400px;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  padding: 16px;
  border-bottom: 1px solid #ebeef5;
  transition: background-color 0.3s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item.unread {
  background-color: #ecf5ff;
}

.notification-icon {
  margin-right: 16px;
  font-size: 24px;
}

.notification-icon .is-primary {
  color: #409eff;
}

.notification-icon .is-success {
  color: #67c23a;
}

.notification-icon .is-warning {
  color: #e6a23c;
}

.notification-icon .is-danger {
  color: #f56c6c;
}

.notification-icon .is-info {
  color: #909399;
}

.notification-content {
  flex: 1;
}

.notification-title {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.notification-text {
  font-size: 14px;
  color: #606266;
  margin-bottom: 8px;
  line-height: 1.6;
}

.notification-meta {
  font-size: 12px;
  color: #909399;
  display: flex;
  gap: 16px;
}

.notification-actions {
  margin-left: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.box-card {
  margin-bottom: 20px;
}
</style>
