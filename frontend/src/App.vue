<template>
  <div class="app-container">
    <el-container>
      <el-header>
        <div class="header">
          <h1 class="logo-text">LingFlow 流程管理系统</h1>
          <div class="header-actions">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="item">
              <el-button circle @click="goToNotifications">
                <el-icon><Bell /></el-icon>
              </el-button>
            </el-badge>
          </div>
        </div>
      </el-header>
      <el-container>
        <el-aside width="220px">
          <el-menu
            :default-active="activeMenu"
            :default-openeds="['process', 'monitor']"
            router
            background-color="#f5f7fa"
            text-color="#303133"
            active-text-color="#409eff"
            class="side-menu"
          >
            <el-sub-menu index="process">
              <template #title>
                <el-icon><Document /></el-icon>
                <span>流程管理</span>
              </template>
              <el-menu-item index="/process">
                <el-icon><List /></el-icon>
                <span>流程列表</span>
              </el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/task">
              <el-icon><Checked /></el-icon>
              <span>任务办理</span>
            </el-menu-item>

            <el-sub-menu index="monitor">
              <template #title>
                <el-icon><Monitor /></el-icon>
                <span>流程监控</span>
              </template>
              <el-menu-item index="/monitor#running">
                <el-icon><VideoPlay /></el-icon>
                <span>运行中的流程</span>
              </el-menu-item>
              <el-menu-item index="/monitor#completed">
                <el-icon><Finished /></el-icon>
                <span>历史流程</span>
              </el-menu-item>
            </el-sub-menu>

            <el-menu-item index="/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <span>流程统计</span>
            </el-menu-item>

            <el-menu-item index="/notification">
              <el-icon><Bell /></el-icon>
              <span>消息通知</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Document,
  Monitor,
  List,
  VideoPlay,
  Finished,
  Bell,
  Checked,
  DataAnalysis
} from '@element-plus/icons-vue'
import { getUnreadNotificationCount } from '@/api/extended'

const route = useRoute()
const router = useRouter()
const activeMenu = ref(route.path)
const unreadCount = ref(0)

// 监听路由变化并更新激活的菜单项
watch(() => route.path, (newPath) => {
  activeMenu.value = newPath
})

// 获取未读通知数量
const fetchUnreadCount = async () => {
  try {
    const userId = 'user001' // 实际应从用户状态中获取
    const response = await getUnreadNotificationCount(userId)
    unreadCount.value = response.data || 0
  } catch (error) {
    console.error('获取未读通知数量失败', error)
  }
}

// 跳转到通知页面
const goToNotifications = () => {
  router.push('/notification')
}

// 定时刷新未读数量
onMounted(() => {
  fetchUnreadCount()
  // 每30秒刷新一次未读数量
  setInterval(fetchUnreadCount, 30000)
})
</script>

<style scoped>
.app-container {
  height: 100vh;
  overflow: hidden;
}

.header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  height: 100%;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.logo-text {
  margin: 0;
  font-size: 26px;
  font-weight: 600;
  background: linear-gradient(45deg, #ffffff, #e6e6fa);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  letter-spacing: 1px;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "微软雅黑", Arial, sans-serif;
}

.header-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}

.el-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #333;
  line-height: 60px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.side-menu {
  border-right: none;
  height: 100%;
}

.el-aside {
  background-color: #f5f7fa;
  box-shadow: 2px 0 8px rgba(232, 237, 250, 0.6);
  border-right: 1px solid #e6ebf5;
  overflow-x: hidden;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
  background: linear-gradient(to bottom, #f5f7fa 0%, #c3cfe2 100%);
  min-height: calc(100vh - 60px);
}

/* 菜单动画效果 */
.el-menu-item {
  transition: all 0.3s ease;
  border-radius: 8px;
  margin: 4px 8px;
}

.el-menu-item:hover {
  background-color: #ecf5ff !important;
  transform: translateX(4px);
  transition: all 0.3s ease;
}

.el-sub-menu__title:hover {
  background-color: #ecf5ff !important;
}
</style>
