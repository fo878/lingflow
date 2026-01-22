<template>
  <div class="category-tree">
    <el-tree
      ref="treeRef"
      :data="data"
      :props="treeProps"
      node-key="id"
      :default-expand-all="false"
      :default-expanded-keys="expandedKeys"
      :expand-on-click-node="false"
      :highlight-current="true"
      :allow-drop="allowDrop"
      :allow-drag="allowDrag"
      draggable
      @node-drop="handleNodeDrop"
      @node-click="handleNodeClick"
    >
      <template #default="{ node, data }">
        <div class="tree-node">
          <!-- 分类图标 -->
          <el-icon v-if="data.icon" class="node-icon">
            <component :is="data.icon" />
          </el-icon>
          <el-icon v-else class="node-icon node-icon-default">
            <Folder />
          </el-icon>
          <span class="node-label">{{ node.label }}</span>
          <span v-if="data.processCount !== undefined" class="node-count">
            ({{ data.processCount }})
          </span>
          <!-- 操作按钮 -->
          <span class="node-actions">
            <el-tooltip content="添加子分类" placement="top">
              <el-button
                type="primary"
                link
                size="small"
                @click.stop="handleAdd(data)"
              >
                <el-icon><Plus /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="编辑" placement="top">
              <el-button
                type="primary"
                link
                size="small"
                @click.stop="handleEdit(data)"
              >
                <el-icon><Edit /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除" placement="top">
              <el-button
                type="danger"
                link
                size="small"
                @click.stop="handleDelete(data)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </span>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Folder
} from '@element-plus/icons-vue'
import type { NodeDropType } from 'element-plus'
import type { ProcessCategoryTree } from '@/api/processCategory'
import {
  moveCategory,
  batchUpdateSortOrder
} from '@/api/processCategory'

interface Props {
  data: ProcessCategoryTree[]
  tenantId: string
  appId?: string
  contextId?: string
}

const props = defineProps<Props>()
const emit = defineEmits(['node-click', 'add', 'edit', 'delete', 'refresh'])

const treeRef = ref()
const treeProps = {
  children: 'children',
  label: 'name'
}

/**
 * 递归收集 1-3 级节点的 ID
 * @param nodes 树节点数组
 * @param level 当前层级（从 1 开始）
 * @param maxLevel 最大展开层级
 * @returns 展开的节点 ID 数组
 */
const collectNodeKeysByLevel = (
  nodes: ProcessCategoryTree[],
  level: number = 1,
  maxLevel: number = 3
): string[] => {
  const keys: string[] = []

  for (const node of nodes) {
    // 如果当前层级在最大展开层级内，收集该节点的 ID
    if (level <= maxLevel && node.id) {
      keys.push(node.id)
    }

    // 递归处理子节点
    if (node.children && node.children.length > 0) {
      const childKeys = collectNodeKeysByLevel(node.children, level + 1, maxLevel)
      keys.push(...childKeys)
    }
  }

  return keys
}

/**
 * 计算需要默认展开的节点 keys（1-3 级）
 */
const expandedKeys = computed(() => {
  if (!props.data || props.data.length === 0) {
    return []
  }
  return collectNodeKeysByLevel(props.data, 1, 3)
})

// 拖拽相关
const allowDrop = (draggingNode: any, dropNode: any, type: NodeDropType) => {
  // 只允许同级排序或移动到父节点
  // 不允许跨级拖拽到子节点下
  if (type === 'inner') {
    // 可以移动到父节点
    return true
  }
  // prev 和 next 只允许同级
  return draggingNode.data.parentId === dropNode.data.parentId
}

const allowDrag = (draggingNode: any) => {
  // 根节点不允许拖拽
  return draggingNode.data.parentId !== null && draggingNode.data.parentId !== undefined
}

const handleNodeDrop = async (
  draggingNode: any,
  dropNode: any,
  dropType: NodeDropType
) => {
  try {
    if (dropType === 'inner') {
      // 移动到父节点下
      await moveCategory(draggingNode.data.id, dropNode.data.id)
      ElMessage.success('分类移动成功')
    } else {
      // 同级排序 - 需要重新计算所有兄弟节点的排序
      const siblings = dropNode.parent.childNodes
      const sortOrderList = siblings.map((node: any, index: number) => ({
        id: node.data.id,
        sortOrder: index
      }))
      await batchUpdateSortOrder(sortOrderList)
      ElMessage.success('排序更新成功')
    }
    emit('refresh')
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
    // 刷新树以恢复状态
    emit('refresh')
  }
}

const handleNodeClick = (data: any) => {
  emit('node-click', data)
}

const handleAdd = (data: any) => {
  emit('add', data)
}

const handleEdit = (data: any) => {
  emit('edit', data)
}

const handleDelete = async (data: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除分类"${data.name}"吗？`,
      '警告',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    emit('delete', data)
  } catch {
    // 用户取消
  }
}

// 暴露方法供父组件调用
defineExpose({
  setCurrentKey: (key: string) => {
    treeRef.value?.setCurrentKey(key)
  },
  getCurrentKey: () => {
    return treeRef.value?.getCurrentKey()
  },
  expandPath: (pathIds: string[]) => {
    if (!treeRef.value) return

    // 展开路径上的所有节点
    treeRef.value.store.nodesMap.forEach((node: any) => {
      if (pathIds.includes(node.key)) {
        node.expanded = true
      }
    })

    // 设置当前选中节点
    if (pathIds.length > 0) {
      const lastId = pathIds[pathIds.length - 1]
      treeRef.value?.setCurrentKey(lastId)
    }
  }
})

// 监听数据变化，刷新树
watch(() => props.data, () => {
  // 数据更新后，保持当前选中的节点
  const currentKey = treeRef.value?.getCurrentKey()
  if (currentKey) {
    setTimeout(() => {
      treeRef.value?.setCurrentKey(currentKey)
    }, 0)
  }
}, { deep: true })
</script>

<style scoped>
.category-tree {
  height: 100%;
  overflow-y: auto;
}

.tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
  width: 100%;
  min-width: 0;
}

.node-icon {
  margin-right: 8px;
  color: #409eff;
  font-size: 16px;
  flex-shrink: 0;
}

.node-icon-default {
  color: #909399;
}

.node-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.node-count {
  color: #909399;
  font-size: 12px;
  margin-left: 8px;
  flex-shrink: 0;
}

.node-actions {
  display: none;
  margin-left: 8px;
  flex-shrink: 0;
}

.tree-node:hover .node-actions {
  display: flex;
  gap: 4px;
}

/* 滚动条美化 */
.category-tree::-webkit-scrollbar {
  width: 6px;
}

.category-tree::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.category-tree::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.category-tree::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>
