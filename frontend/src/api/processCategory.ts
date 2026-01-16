import request from './index'

// 流程分类接口
export interface ProcessCategory {
  id?: string
  name: string
  code: string
  parentId?: string
  path?: string
  level?: number
  description?: string
  sortOrder?: number
  icon?: string
  appId?: string
  contextId?: string
  tenantId: string
}

// 流程分类树接口
export interface ProcessCategoryTree extends ProcessCategory {
  processCount?: number
  hasChildren?: boolean
  children?: ProcessCategoryTree[]
}

// 流程搜索结果接口
export interface ProcessSearchResult {
  id: string
  key: string
  name: string
  version: number
  categoryId: string
  categoryName: string
  categoryPath: string         // 路径ID：root/id1/id2
  categoryPathNames: string    // 路径名称：根节点/子节点1/子节点2
  pathIds: string[]           // 用于前端定位
}

// 排序数据接口
export interface SortOrderDTO {
  id: string
  sortOrder: number
}

/**
 * 获取分类树
 */
export function getCategoryTree(params: {
  tenantId: string
  appId?: string
  contextId?: string
}) {
  return request({
    url: '/api/process-category/tree',
    method: 'get',
    params
  })
}

/**
 * 创建分类
 */
export function createCategory(data: ProcessCategory) {
  return request({
    url: '/api/process-category',
    method: 'post',
    data
  })
}

/**
 * 更新分类
 */
export function updateCategory(id: string, data: ProcessCategory) {
  return request({
    url: `/api/process-category/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除分类
 */
export function deleteCategory(id: string) {
  return request({
    url: `/api/process-category/${id}`,
    method: 'delete'
  })
}

/**
 * 移动分类
 */
export function moveCategory(id: string, newParentId: string) {
  return request({
    url: `/api/process-category/${id}/move`,
    method: 'put',
    params: { newParentId }
  })
}

/**
 * 更新排序
 */
export function updateSortOrder(id: string, sortOrder: number) {
  return request({
    url: `/api/process-category/${id}/sort`,
    method: 'put',
    params: { sortOrder }
  })
}

/**
 * 批量更新排序（拖拽排序）
 */
export function batchUpdateSortOrder(sortOrderList: SortOrderDTO[]) {
  return request({
    url: '/api/process-category/sort/batch',
    method: 'put',
    data: sortOrderList
  })
}

/**
 * 搜索分类
 */
export function searchCategories(params: {
  keyword: string
  tenantId: string
  appId?: string
  contextId?: string
}) {
  return request({
    url: '/api/process-category/search',
    method: 'get',
    params
  })
}

/**
 * 设置流程分类
 */
export function setProcessCategory(
  processDefinitionId: string,
  categoryId: string,
  params: {
    tenantId: string
    appId?: string
    contextId?: string
  }
) {
  return request({
    url: `/api/process-category/process/${processDefinitionId}`,
    method: 'put',
    params: { categoryId, ...params }
  })
}

/**
 * 搜索流程模板（返回分类路径）
 */
export function searchProcesses(params: {
  keyword: string
  categoryId?: string
  tenantId: string
  appId?: string
  contextId?: string
}) {
  return request({
    url: '/api/process-category/processes/search',
    method: 'get',
    params
  })
}
