import request from './index'
import type {
  CreateDraftTemplateRequest,
  UpdateDraftTemplateRequest,
  CreateTemplateSnapshotRequest,
  RestoreSnapshotRequest,
  TemplateStatus
} from '@/types/process'

// ==================== 流程定义管理接口（已弃用，建议使用模板管理接口） ====================

/**
 * 保存流程定义（草稿状态）
 * 用于新建或更新流程定义，不影响已发布的版本
 */
export const saveProcessDefinition = (data: {
  id?: string  // 编辑时需要
  name: string
  key?: string
  description?: string
  xml: string
  categoryId?: string
  tenantId?: string
  appId?: string
  contextId?: string
}) => {
  return request.post('/api/process/definition', data)
}

/**
 * 更新流程定义属性
 * 用于更新已保存流程的基本信息
 */
export const updateProcessDefinition = (data: {
  id: string
  name?: string
  key?: string
  description?: string
  categoryId?: string
}) => {
  return request.put('/api/process/definition', data)
}

/**
 * 激活流程定义
 * 将流程定义从草稿或停用状态转为激活状态
 */
export const activateProcessDefinition = (processDefinitionId: string) => {
  return request.post(`/api/process/definition/${processDefinitionId}/activate`)
}

/**
 * 停用流程定义
 * 将流程定义从激活状态转为停用状态
 */
export const suspendProcessDefinition = (processDefinitionId: string) => {
  return request.post(`/api/process/definition/${processDefinitionId}/suspend`)
}

/**
 * 删除流程定义
 */
export const deleteProcessDefinition = (deploymentId: string) => {
  return request.delete(`/process/definition/${deploymentId}`)
}

/**
 * 获取流程定义列表
 */
export const getProcessDefinitions = () => {
  return request.get('/process/definitions')
}

// 流程实例相关接口
export const startProcess = (processKey: string, variables?: any) => {
  return request.post(`/process/start/${processKey}`, variables)
}

export const getRunningInstances = () => {
  return request.get('/process/running')
}

export const getCompletedInstances = () => {
  return request.get('/process/completed')
}

// 任务相关接口
export const getTasks = () => {
  return request.get('/task/list')
}

export const completeTask = (taskId: string, variables?: any) => {
  return request.post(`/task/complete/${taskId}`, variables)
}

export const getTaskForm = (taskId: string) => {
  return request.get(`/task/form/${taskId}`)
}

// 流程图相关接口
export const getProcessDiagram = (processInstanceId: string) => {
  return request.get(`/process/diagram/${processInstanceId}`, {
    responseType: 'arraybuffer'
  })
}

export const getProcessDefinitionDiagram = (processDefinitionId: string) => {
  return request.get(`/process/definition/diagram/${processDefinitionId}`, {
    responseType: 'arraybuffer'
  })
}

export const getProcessDefinitionXml = (processDefinitionId: string) => {
  return request.get(`/process/definition/xml/${processDefinitionId}`)
}

export const getProcessBpmn = (processInstanceId: string) => {
  return request.get(`/process/bpmn/${processInstanceId}`)
}

// ==================== 流程模板管理接口（新系统） ====================

// ============ 设计态模板管理 ============

/**
 * 创建设计态模板
 */
export const createDraftTemplate = (data: {
  templateKey: string
  templateName: string
  description?: string
  bpmnXml: string
  categoryId: string
  tags?: string[]
  formConfig?: any
  appId?: string
  contextId?: string
  tenantId: string
}) => {
  return request.post('/api/process/template/draft', data)
}

/**
 * 更新设计态模板
 */
export const updateDraftTemplate = (id: string, data: {
  templateName: string
  description?: string
  bpmnXml: string
  categoryId?: string
  tags?: string[]
  formConfig?: any
}) => {
  return request.put(`/api/process/template/draft/${id}`, data)
}

/**
 * 查询设计态模板
 */
export const getDraftTemplate = (id: string) => {
  return request.get(`/api/process/template/draft/${id}`)
}

/**
 * 删除设计态模板
 */
export const deleteDraftTemplate = (id: string) => {
  return request.delete(`/api/process/template/draft/${id}`)
}

/**
 * 查询设计态模板列表
 */
export const listDraftTemplates = (params: {
  tenantId: string
  appId?: string
  contextId?: string
  categoryId?: string
  keyword?: string
  offset?: number
  limit?: number
}) => {
  return request.get('/api/process/template/draft', { params })
}

// ============ 发布态模板管理 ============

/**
 * 发布模板（设计态 → 发布态）
 */
export const publishTemplate = (draftId: string) => {
  return request.post(`/api/process/template/draft/${draftId}/publish`)
}

/**
 * 停用模板（激活态 → 停用态）
 */
export const suspendTemplate = (publishedId: string) => {
  return request.post(`/api/process/template/published/${publishedId}/suspend`)
}

/**
 * 激活模板（停用态 → 激活态）
 */
export const activateTemplate = (publishedId: string) => {
  return request.post(`/api/process/template/published/${publishedId}/activate`)
}

/**
 * 查询发布态模板
 */
export const getPublishedTemplate = (id: string) => {
  return request.get(`/api/process/template/published/${id}`)
}

/**
 * 查询发布态模板列表
 */
export const listPublishedTemplates = (params: {
  tenantId: string
  appId?: string
  contextId?: string
  categoryId?: string
  status?: 'ACTIVE' | 'INACTIVE'
  keyword?: string
  offset?: number
  limit?: number
}) => {
  return request.get('/api/process/template/published', { params })
}

// ============ 快照管理（新系统） ============

/**
 * 创建模板快照
 */
export const createTemplateSnapshot = (data: {
  sourceTemplateId: string
  sourceTemplateType: 'DRAFT' | 'PUBLISHED'
  snapshotName: string
}) => {
  return request.post('/api/process/template/snapshot', data)
}

/**
 * 查询模板快照列表
 */
export const listTemplateSnapshots = (templateKey: string, params: {
  tenantId: string
  appId?: string
  contextId?: string
}) => {
  return request.get(`/api/process/template/snapshot/${templateKey}`, { params })
}

/**
 * 从快照恢复到设计态
 */
export const restoreFromSnapshot = (data: {
  snapshotId: string
  newTemplateName?: string
}) => {
  return request.post('/api/process/template/snapshot/restore', data)
}

/**
 * 删除模板快照
 */
export const deleteTemplateSnapshot = (snapshotId: string) => {
  return request.delete(`/api/process/template/snapshot/${snapshotId}`)
}

// BPMN元素扩展属性相关接口
export const saveElementExtension = (data: {
  processDefinitionId: string
  elementId: string
  elementType: string
  extensionAttributes: Record<string, any>
}) => {
  return request.post('/process/extension', data)
}

export const getElementExtension = (processDefinitionId: string, elementId: string) => {
  return request.get(`/process/extension/${processDefinitionId}/${elementId}`)
}

export const batchSaveElementExtensions = (data: {
  processDefinitionId: string
  extensions: Array<{
    elementId: string
    elementType: string
    extensionAttributes: Record<string, any>
  }>
}) => {
  return request.post('/process/extensions/batch', data)
}

export const getAllElementExtensions = (processDefinitionId: string) => {
  return request.get(`/process/extensions/${processDefinitionId}`)
}

export const deleteElementExtension = (processDefinitionId: string, elementId: string) => {
  return request.delete(`/process/extension/${processDefinitionId}/${elementId}`)
}
