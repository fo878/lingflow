/**
 * 流程模板相关类型定义
 *
 * @author LingFlow Team
 * @since 2026-01-22
 */

/**
 * 模板状态枚举
 */
export type TemplateStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE'

/**
 * 来源模板类型枚举
 */
export type SourceTemplateType = 'DRAFT' | 'PUBLISHED'

/**
 * 设计态模板
 */
export interface DraftTemplate {
  id: string
  templateKey: string
  templateName: string
  description?: string
  bpmnXml: string
  categoryId: string
  categoryName?: string
  categoryCode?: string
  tags?: string[]
  formConfig?: Record<string, unknown>
  appId?: string
  contextId?: string
  tenantId: string
  status: TemplateStatus
  version: number
  createdTime: string
  updatedTime: string
  createdBy?: string
  updatedBy?: string
}

/**
 * 发布态模板
 */
export interface PublishedTemplate {
  id: string
  templateKey: string
  templateName: string
  description?: string
  bpmnXml: string
  categoryId: string
  categoryName?: string
  categoryCode?: string
  tags?: string[]
  formConfig?: Record<string, unknown>
  appId?: string
  contextId?: string
  tenantId: string
  status: TemplateStatus
  version: number

  // Flowable 集成字段
  processDefinitionId: string
  deploymentId: string
  flowableVersion?: number

  // 统计字段
  instanceCount: number
  runningInstanceCount: number

  createdTime: string
  updatedTime: string
  publishedTime: string
  suspendedTime?: string
  createdBy?: string
}

/**
 * 模板快照
 */
export interface TemplateSnapshot {
  id: string
  templateKey: string
  snapshotName: string
  description?: string
  bpmnXml: string
  categoryId: string
  categoryName?: string
  categoryCode?: string
  tags?: string[]
  formConfig?: Record<string, unknown>
  snapshotVersion: number

  // 来源信息
  sourceTemplateId: string
  sourceTemplateType: SourceTemplateType
  sourceTemplateVersion: number

  appId?: string
  contextId?: string
  tenantId: string
  createdTime: string
  createdBy: string
}

/**
 * 统一的模板视图（用于列表展示）
 */
export interface TemplateView {
  id: string
  templateKey: string
  templateName: string
  description?: string
  categoryId: string
  categoryName?: string
  status: TemplateStatus
  version: number
  instanceCount?: number
  runningInstanceCount?: number
  createdTime: string
  updatedTime: string
}

/**
 * 创建设计态模板请求
 */
export interface CreateDraftTemplateRequest {
  templateKey: string
  templateName: string
  description?: string
  bpmnXml: string
  categoryId: string
  tags?: string[]
  formConfig?: Record<string, unknown>
  appId?: string
  contextId?: string
  tenantId: string
}

/**
 * 更新设计态模板请求
 */
export interface UpdateDraftTemplateRequest {
  templateName: string
  description?: string
  bpmnXml: string
  categoryId?: string
  tags?: string[]
  formConfig?: Record<string, unknown>
}

/**
 * 创建快照请求
 */
export interface CreateTemplateSnapshotRequest {
  sourceTemplateId: string
  sourceTemplateType: SourceTemplateType
  snapshotName: string
}

/**
 * 恢复快照请求
 */
export interface RestoreSnapshotRequest {
  snapshotId: string
  newTemplateName?: string
}
