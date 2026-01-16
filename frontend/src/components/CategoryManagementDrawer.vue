<template>
  <el-drawer
    v-model="drawerVisible"
    :title="isEdit ? '编辑分类' : '创建分类'"
    direction="rtl"
    size="500px"
    @close="handleClose"
  >
    <el-form
      ref="formRef"
      :model="formData"
      :rules="formRules"
      label-width="100px"
      label-position="top"
    >
      <el-form-item label="分类名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入分类名称"
          clearable
        />
      </el-form-item>

      <el-form-item label="分类编码" prop="code">
        <el-input
          v-model="formData.code"
          placeholder="请输入分类编码（英文）"
          clearable
          :disabled="isEdit"
        >
          <template #append>
            <el-tooltip content="分类编码用于API调用和系统引用，创建后不可修改" placement="top">
              <el-icon><QuestionFilled /></el-icon>
            </el-tooltip>
          </template>
        </el-input>
      </el-form-item>

      <el-form-item label="父分类">
        <el-tree-select
          v-model="formData.parentId"
          :data="categoryTreeData"
          :props="{ label: 'name', value: 'id' }"
          placeholder="选择父分类（不选择则为根分类）"
          clearable
          check-strictly
          :render-after-expand="false"
        />
      </el-form-item>

      <el-form-item label="排序序号">
        <el-input-number
          v-model="formData.sortOrder"
          :min="0"
          :max="9999"
          placeholder="请输入排序序号"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item label="图标">
        <el-button @click="showIconSelector = true" style="width: 100%">
          <template v-if="formData.icon">
            <el-icon style="margin-right: 8px">
              <component :is="formData.icon" />
            </el-icon>
            已选择图标
          </template>
          <template v-else>
            <el-icon style="margin-right: 8px"><Picture /></el-icon>
            点击选择图标
          </template>
        </el-button>
      </el-form-item>

      <el-form-item label="描述">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入分类描述"
          clearable
        />
      </el-form-item>

      <!-- 图标选择对话框 -->
      <el-dialog
        v-model="showIconSelector"
        title="选择图标"
        width="600px"
        append-to-body
      >
        <CategoryIconSelector v-model="selectedIcon" />
        <template #footer>
          <el-button @click="showIconSelector = false">取消</el-button>
          <el-button type="primary" @click="confirmIcon">确定</el-button>
        </template>
      </el-dialog>
    </el-form>

    <template #footer>
      <div style="flex: 1">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { QuestionFilled, Picture } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import CategoryIconSelector from './CategoryIconSelector.vue'
import type { ProcessCategory, ProcessCategoryTree } from '@/api/processCategory'
import { createCategory, updateCategory } from '@/api/processCategory'

interface Props {
  modelValue: boolean
  categoryData?: ProcessCategoryTree | null
  categoryTree: ProcessCategoryTree[]
  tenantId: string
  appId?: string
  contextId?: string
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'saved'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const submitting = ref(false)
const showIconSelector = ref(false)
const selectedIcon = ref('')

const drawerVisible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const isEdit = computed(() => !!props.categoryData)

const formData = ref<ProcessCategory>({
  name: '',
  code: '',
  parentId: undefined,
  description: '',
  sortOrder: 0,
  icon: '',
  tenantId: props.tenantId,
  appId: props.appId,
  contextId: props.contextId
})

const categoryTreeData = computed(() => {
  // 过滤掉当前编辑的节点（避免将分类设置为自己的子节点）
  if (!isEdit.value || !props.categoryData) {
    return props.categoryTree
  }

  const filterTree = (nodes: ProcessCategoryTree[]): ProcessCategoryTree[] => {
    return nodes
      .filter(node => node.id !== props.categoryData?.id)
      .map(node => ({
        ...node,
        children: node.children ? filterTree(node.children) : undefined
      }))
  }

  return filterTree(props.categoryTree)
})

const formRules: FormRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { min: 2, max: 50, message: '分类名称长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入分类编码', trigger: 'blur' },
    { min: 2, max: 50, message: '分类编码长度在 2 到 50 个字符', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '分类编码只能包含字母、数字、下划线和连字符', trigger: 'blur' }
  ]
}

// 监听抽屉打开，初始化表单
watch(() => props.modelValue, (val) => {
  if (val) {
    if (props.categoryData) {
      // 编辑模式
      formData.value = {
        ...props.categoryData,
        tenantId: props.tenantId,
        appId: props.appId,
        contextId: props.contextId
      }
      selectedIcon.value = props.categoryData.icon || ''
    } else {
      // 创建模式
      formData.value = {
        name: '',
        code: '',
        parentId: undefined,
        description: '',
        sortOrder: 0,
        icon: '',
        tenantId: props.tenantId,
        appId: props.appId,
        contextId: props.contextId
      }
      selectedIcon.value = ''
    }
    formRef.value?.clearValidate()
  }
})

const confirmIcon = () => {
  formData.value.icon = selectedIcon.value
  showIconSelector.value = false
}

const handleClose = () => {
  drawerVisible.value = false
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()

    submitting.value = true

    if (isEdit.value) {
      await updateCategory(props.categoryData!.id!, formData.value)
      ElMessage.success('分类更新成功')
    } else {
      await createCategory(formData.value)
      ElMessage.success('分类创建成功')
    }

    emit('saved')
    handleClose()
  } catch (error: any) {
    if (error !== false) { // 表单验证失败时error为false
      ElMessage.error(error.response?.data?.message || '操作失败')
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
:deep(.el-drawer__body) {
  padding: 20px;
}
</style>
