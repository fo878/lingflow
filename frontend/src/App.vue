<template>
  <div class="app-container">
    <el-container>
      <el-header>
        <div class="header">
          <h1>LingFlow 流程管理系统</h1>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
          >
            <el-menu-item index="/designer">
              <el-icon><Edit /></el-icon>
              <span>流程设计器</span>
            </el-menu-item>
            <el-menu-item index="/process">
              <el-icon><Document /></el-icon>
              <span>流程管理</span>
            </el-menu-item>
            <el-menu-item index="/task">
              <el-icon><List /></el-icon>
              <span>任务办理</span>
            </el-menu-item>
            <el-menu-item index="/monitor">
              <el-icon><Monitor /></el-icon>
              <span>流程监控</span>
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
import { ref } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = ref(route.path)
</script>

<style scoped>
.header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.header h1 {
  font-size: 24px;
  margin: 0;
}

.el-header {
  background-color: #409eff;
  color: #333;
  line-height: 60px;
}

.el-aside {
  background-color: #545c64;
}

.el-main {
  background-color: #f0f2f5;
  padding: 20px;
}
</style>
