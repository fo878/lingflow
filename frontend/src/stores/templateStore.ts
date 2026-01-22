import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { DraftTemplate, PublishedTemplate, TemplateView, TemplateStatus } from '@/types/process'

/**
 * 流程模板状态管理
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */
export const useTemplateStore = defineStore('template', () => {
  // 状态
  const currentTemplate = ref<DraftTemplate | PublishedTemplate | null>(null)
  const templateList = ref<TemplateView[]>([])
  const selectedCategoryId = ref<string>()
  const currentTenantId = ref<string>('default')

  // 计算属性
  const templateStatus = computed<TemplateStatus>(() => {
    if (!currentTemplate.value) {
      return 'DRAFT'
    }
    return currentTemplate.value.status
  })

  const isDraft = computed(() => templateStatus.value === 'DRAFT')
  const isActive = computed(() => templateStatus.value === 'ACTIVE')
  const isInactive = computed(() => templateStatus.value === 'INACTIVE')

  const canEdit = computed(() => isDraft.value)
  const canPublish = computed(() => isDraft.value)
  const canSuspend = computed(() => isActive.value)
  const canActivate = computed(() => isInactive.value)

  // 方法
  const setCurrentTemplate = (template: DraftTemplate | PublishedTemplate) => {
    currentTemplate.value = template
  }

  const clearCurrentTemplate = () => {
    currentTemplate.value = null
  }

  const updateTemplateList = (list: TemplateView[]) => {
    templateList.value = list
  }

  const setSelectedCategoryId = (categoryId?: string) => {
    selectedCategoryId.value = categoryId
  }

  const setCurrentTenantId = (tenantId: string) => {
    currentTenantId.value = tenantId
  }

  return {
    // 状态
    currentTemplate,
    templateList,
    selectedCategoryId,
    currentTenantId,

    // 计算属性
    templateStatus,
    isDraft,
    isActive,
    isInactive,
    canEdit,
    canPublish,
    canSuspend,
    canActivate,

    // 方法
    setCurrentTemplate,
    clearCurrentTemplate,
    updateTemplateList,
    setSelectedCategoryId,
    setCurrentTenantId
  }
})
