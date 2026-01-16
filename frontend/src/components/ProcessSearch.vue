<template>
  <div class="process-search">
    <el-input
      v-model="searchKeyword"
      placeholder="搜索流程模板..."
      clearable
      @input="handleSearch"
      @focus="showResults = true"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <!-- 搜索结果下拉框 -->
    <div
      v-if="showResults && (searchResults.length > 0 || (searchKeyword && searchResults.length === 0))"
      class="search-results"
    >
      <!-- 加载状态 -->
      <div v-if="searching" class="search-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>搜索中...</span>
      </div>

      <!-- 搜索结果 -->
      <div v-else-if="searchResults.length > 0">
        <div
          v-for="result in searchResults"
          :key="result.id"
          class="result-item"
          @click="handleSelectResult(result)"
        >
          <div class="result-header">
            <el-icon><Document /></el-icon>
            <span class="result-name">{{ result.name }}</span>
            <el-tag size="small" type="info">v{{ result.version }}</el-tag>
          </div>
          <div class="result-path">
            <el-icon><FolderOpened /></el-icon>
            <span>{{ result.categoryPathNames || '未分类' }}</span>
          </div>
        </div>
      </div>

      <!-- 无结果 -->
      <div v-else class="no-results">
        <el-empty description="未找到匹配的流程模板" :image-size="80" />
      </div>
    </div>

    <!-- 点击外部关闭结果 -->
    <div
      v-if="showResults"
      class="search-overlay"
      @click="showResults = false"
    ></div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Document, FolderOpened, Loading } from '@element-plus/icons-vue'
import { searchProcesses } from '@/api/processCategory'
import type { ProcessSearchResult } from '@/api/processCategory'

interface Props {
  tenantId: string
  appId?: string
  contextId?: string
}

interface Emits {
  (e: 'select', data: { process: ProcessSearchResult, pathIds: string[] }): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const searchKeyword = ref('')
const searchResults = ref<ProcessSearchResult[]>([])
const searching = ref(false)
const showResults = ref(false)
let searchTimer: any = null

const handleSearch = () => {
  clearTimeout(searchTimer)
  showResults.value = true

  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }

  searching.value = true
  searchTimer = setTimeout(async () => {
    try {
      const response = await searchProcesses({
        keyword: searchKeyword.value,
        tenantId: props.tenantId,
        appId: props.appId,
        contextId: props.contextId
      })
      searchResults.value = response.data.data || []
    } catch (error) {
      ElMessage.error('搜索失败')
      searchResults.value = []
    } finally {
      searching.value = false
    }
  }, 300) // 防抖300ms
}

const handleSelectResult = (result: ProcessSearchResult) => {
  emit('select', {
    process: result,
    pathIds: result.pathIds || [] // 返回路径ID列表用于定位
  })
  searchResults.value = []
  searchKeyword.value = ''
  showResults.value = false
}
</script>

<style scoped>
.process-search {
  position: relative;
  width: 400px;
}

.search-results {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  right: 0;
  max-height: 400px;
  overflow-y: auto;
  background: white;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.search-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #909399;
  gap: 8px;
}

.search-loading .el-icon {
  font-size: 18px;
}

.result-item {
  padding: 12px 16px;
  cursor: pointer;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.3s;
}

.result-item:last-child {
  border-bottom: none;
}

.result-item:hover {
  background-color: #f5f7fa;
}

.result-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.result-header .el-icon {
  color: #409eff;
  font-size: 16px;
}

.result-name {
  flex: 1;
  font-weight: 500;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.result-path {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #909399;
}

.result-path .el-icon {
  font-size: 14px;
}

.no-results {
  padding: 20px;
}

.search-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 999;
}

/* 滚动条美化 */
.search-results::-webkit-scrollbar {
  width: 6px;
}

.search-results::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.search-results::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.search-results::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
