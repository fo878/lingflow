<template>
  <div class="icon-selector">
    <el-input
      v-model="searchKeyword"
      placeholder="搜索图标..."
      clearable
      class="search-input"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <div class="icon-grid">
      <div
        v-for="icon in filteredIcons"
        :key="icon.name"
        :class="['icon-item', { 'is-selected': modelValue === icon.name }]"
        @click="selectIcon(icon.name)"
      >
        <el-icon :size="24">
          <component :is="icon.name" />
        </el-icon>
        <span class="icon-name">{{ icon.label }}</span>
      </div>
    </div>

    <div v-if="modelValue" class="selected-info">
      <el-tag closable @close="clearIcon">
        <el-icon style="margin-right: 4px">
          <component :is="modelValue" />
        </el-icon>
        已选择
      </el-tag>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'

// 常用图标列表（Element Plus 图标）
const commonIcons = [
  { name: 'Folder', label: '文件夹' },
  { name: 'FolderOpened', label: '打开文件夹' },
  { name: 'FolderAdd', label: '添加文件夹' },
  { name: 'FolderDelete', label: '删除文件夹' },
  { name: 'Document', label: '文档' },
  { name: 'DocumentCopy', label: '复制文档' },
  { name: 'DocumentAdd', label: '添加文档' },
  { name: 'DocumentDelete', label: '删除文档' },
  { name: 'Files', label: '文件' },
  { name: 'Ticket', label: '票据' },
  { name: 'Menu', label: '菜单' },
  { name: 'Grid', label: '网格' },
  { name: 'List', label: '列表' },
  { name: 'Filled', label: '实心' },
  { name: 'Coin', label: '硬币' },
  { name: 'Wallet', label: '钱包' },
  { name: 'Goods', label: '商品' },
  { name: 'Box', label: '盒子' },
  { name: 'ShoppingBag', label: '购物袋' },
  { name: 'ShoppingCart', label: '购物车' },
  { name: 'Briefcase', label: '公文包' },
  { name: 'Suitcase', label: '行李箱' },
  { name: 'Calendar', label: '日历' },
  { name: 'Clock', label: '时钟' },
  { name: 'Timer', label: '计时器' },
  { name: 'Setting', label: '设置' },
  { name: 'Tools', label: '工具' },
  { name: 'Management', label: '管理' },
  { name: 'DataAnalysis', label: '数据分析' },
  { name: 'Monitor', label: '显示器' },
  { name: 'Mouse', label: '鼠标' },
  { name: 'Cpu', label: 'CPU' },
  { name: 'OfficeBuilding', label: '办公楼' },
  { name: 'School', label: '学校' },
  { name: 'Hospital', label: '医院' },
  { name: 'Shop', label: '商店' },
  { name: 'House', label: '房子' },
  { name: 'HomeFilled', label: '家' },
  { name: 'User', label: '用户' },
  { name: 'UserFilled', label: '用户填充' },
  { name: 'Avatar', label: '头像' },
  { name: 'Users', label: '用户组' },
  { name: 'Team', label: '团队' },
  { name: 'Stamp', label: '印章' },
  { name: 'Medal', label: '奖章' },
  { name: 'Trophy', label: '奖杯' },
  { name: 'Star', label: '星星' },
  { name: 'ChatDotRound', label: '聊天气泡' },
  { name: 'ChatLineRound', label: '聊天气泡线' },
  { name: 'Message', label: '消息' },
  { name: 'Bell', label: '铃铛' },
  { name: 'Phone', label: '电话' },
  { name: 'Cellphone', label: '手机' },
  { name: 'Location', label: '位置' },
  { name: 'Place', label: '地点' },
  { name: 'Position', label: '定位' },
  { name: 'Compass', label: '指南针' },
  { name: 'Warning', label: '警告' },
  { name: 'InfoFilled', label: '信息' },
  { name: 'SuccessFilled', label: '成功' },
  { name: 'CircleCheckFilled', label: '勾选' }
]

interface Props {
  modelValue?: string
}

interface Emits {
  (e: 'update:modelValue', value: string): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const searchKeyword = ref('')

// 过滤图标
const filteredIcons = computed(() => {
  if (!searchKeyword.value) {
    return commonIcons
  }
  const keyword = searchKeyword.value.toLowerCase()
  return commonIcons.filter(icon =>
    icon.label.toLowerCase().includes(keyword) ||
    icon.name.toLowerCase().includes(keyword)
  )
})

const selectIcon = (iconName: string) => {
  emit('update:modelValue', iconName)
}

const clearIcon = () => {
  emit('update:modelValue', '')
}
</script>

<style scoped>
.icon-selector {
  width: 100%;
}

.search-input {
  margin-bottom: 16px;
}

.icon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  max-height: 400px;
  overflow-y: auto;
  padding: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 12px 8px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
}

.icon-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.icon-item.is-selected {
  border-color: #409eff;
  background-color: #ecf5ff;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.icon-item > .el-icon {
  color: #606266;
  margin-bottom: 8px;
}

.icon-item:hover > .el-icon,
.icon-item.is-selected > .el-icon {
  color: #409eff;
}

.icon-name {
  font-size: 12px;
  color: #909399;
  text-align: center;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  width: 100%;
}

.selected-info {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #e4e7ed;
}

/* 滚动条美化 */
.icon-grid::-webkit-scrollbar {
  width: 6px;
}

.icon-grid::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.icon-grid::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.icon-grid::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
