import request from './index'

// ==================== 流程监控相关接口 ====================

/**
 * 获取流程实例监控信息
 */
export const getProcessInstanceMonitor = (processInstanceId: string) => {
  return request.get(`/api/monitor/process/${processInstanceId}`)
}

/**
 * 获取流程定义监控信息
 */
export const getProcessDefinitionMonitor = (processDefinitionId: string) => {
  return request.get(`/api/monitor/definition/${processDefinitionId}`)
}

/**
 * 检测超时的流程实例
 */
export const detectTimeoutProcesses = (timeoutHours: number) => {
  return request.get(`/api/monitor/timeout/${timeoutHours}`)
}

/**
 * 获取所有运行中的流程实例监控信息
 */
export const getAllRunningProcessMonitors = () => {
  return request.get('/api/monitor/process/running')
}

/**
 * 获取活动节点信息
 */
export const getActivityNodes = (processInstanceId: string) => {
  return request.get(`/api/monitor/process/${processInstanceId}/activities`)
}

/**
 * 获取流程进度信息
 */
export const getProcessProgress = (processInstanceId: string) => {
  return request.get(`/api/monitor/process/${processInstanceId}/progress`)
}

// ==================== 流程统计相关接口 ====================

/**
 * 获取流程实例统计
 */
export const getProcessInstanceStatistics = (processDefinitionKey?: string) => {
  return request.get('/api/statistics/process-instance', {
    params: { processDefinitionKey }
  })
}

/**
 * 获取任务统计
 */
export const getTaskStatistics = (processDefinitionKey?: string) => {
  return request.get('/api/statistics/task', {
    params: { processDefinitionKey }
  })
}

/**
 * 获取流程定义统计
 */
export const getProcessDefinitionStatistics = () => {
  return request.get('/api/statistics/process-definition')
}

/**
 * 获取每日流程统计
 */
export const getDailyStatistics = (startDate: string, endDate: string) => {
  return request.get('/api/statistics/daily', {
    params: { startDate, endDate }
  })
}

/**
 * 获取用户任务统计
 */
export const getUserTaskStatistics = (userId: string) => {
  return request.get(`/api/statistics/user-task/${userId}`)
}

/**
 * 获取流程平均完成时间
 */
export const getAverageCompletionTime = (processDefinitionKey?: string) => {
  return request.get('/api/statistics/average-completion-time', {
    params: { processDefinitionKey }
  })
}

/**
 * 获取流程趋势统计
 */
export const getProcessTrend = (days: number) => {
  return request.get(`/api/statistics/trend/${days}`)
}

/**
 * 获取超时流程统计
 */
export const getTimeoutStatistics = (timeoutHours: number) => {
  return request.get(`/api/statistics/timeout/${timeoutHours}`)
}

// ==================== 流程通知相关接口 ====================

/**
 * 发送任务分配通知
 */
export const sendTaskAssignedNotification = (data: {
  taskId: string
  assignee: string
  taskName: string
}) => {
  return request.post('/api/notifications/task-assigned', data)
}

/**
 * 发送任务完成通知
 */
export const sendTaskCompletedNotification = (data: {
  taskId: string
  assignee: string
  taskName: string
  nextAssignees?: string[]
}) => {
  return request.post('/api/notifications/task-completed', data)
}

/**
 * 发送流程超时通知
 */
export const sendProcessTimeoutNotification = (data: {
  processInstanceId: string
  assignee: string
  taskName: string
  timeoutHours: number
}) => {
  return request.post('/api/notifications/process-timeout', data)
}

/**
 * 发送流程拒绝通知
 */
export const sendProcessRejectedNotification = (data: {
  processInstanceId: string
  initiator: string
  processName: string
  reason?: string
}) => {
  return request.post('/api/notifications/process-rejected', data)
}

/**
 * 发送流程通过通知
 */
export const sendProcessApprovedNotification = (data: {
  processInstanceId: string
  initiator: string
  processName: string
}) => {
  return request.post('/api/notifications/process-approved', data)
}

/**
 * 发送流程撤回通知
 */
export const sendProcessWithdrawnNotification = (data: {
  processInstanceId: string
  initiator: string
  processName: string
  reason?: string
}) => {
  return request.post('/api/notifications/process-withdrawn', data)
}

/**
 * 发送任务转办通知
 */
export const sendTaskDelegatedNotification = (data: {
  taskId: string
  fromUser: string
  toUser: string
  taskName: string
}) => {
  return request.post('/api/notifications/task-delegated', data)
}

/**
 * 发送自定义通知
 */
export const sendCustomNotification = (data: {
  recipients: string[]
  title: string
  content: string
  processInstanceId?: string
}) => {
  return request.post('/api/notifications/custom', data)
}

/**
 * 获取用户的通知列表
 */
export const getUserNotifications = (userId: string, limit: number = 20) => {
  return request.get(`/api/notifications/user/${userId}`, {
    params: { limit }
  })
}

/**
 * 获取未读通知数量
 */
export const getUnreadNotificationCount = (userId: string) => {
  return request.get(`/api/notifications/user/${userId}/unread-count`)
}

/**
 * 标记通知为已读
 */
export const markNotificationAsRead = (userId: string, notificationId: string) => {
  return request.post(`/api/notifications/user/${userId}/read/${notificationId}`)
}

/**
 * 批量标记通知为已读
 */
export const markAllNotificationsAsRead = (userId: string) => {
  return request.post(`/api/notifications/user/${userId}/read-all`)
}

// ==================== 流程评论相关接口 ====================

/**
 * 添加流程实例评论
 */
export const addProcessComment = (data: {
  processInstanceId: string
  userId: string
  content: string
}) => {
  return request.post('/api/comments/process', data)
}

/**
 * 添加任务评论
 */
export const addTaskComment = (data: {
  taskId: string
  userId: string
  content: string
}) => {
  return request.post('/api/comments/task', data)
}

/**
 * 添加系统评论
 */
export const addSystemComment = (data: {
  processInstanceId: string
  content: string
}) => {
  return request.post('/api/comments/system', data)
}

/**
 * 获取流程实例的所有评论
 */
export const getProcessComments = (processInstanceId: string) => {
  return request.get(`/api/comments/process/${processInstanceId}`)
}

/**
 * 获取任务的所有评论
 */
export const getTaskComments = (taskId: string) => {
  return request.get(`/api/comments/task/${taskId}`)
}

/**
 * 获取流程实例的所有评论（包括任务评论）
 */
export const getAllCommentsForProcess = (processInstanceId: string) => {
  return request.get(`/api/comments/process/${processInstanceId}/all`)
}

/**
 * 删除评论
 */
export const deleteComment = (commentId: string, userId: string) => {
  return request.delete(`/api/comments/${commentId}`, {
    params: { userId }
  })
}

/**
 * 更新评论
 */
export const updateComment = (data: {
  commentId: string
  userId: string
  newContent: string
}) => {
  return request.put('/api/comments/', data)
}

/**
 * 获取评论统计
 */
export const getCommentStatistics = (processInstanceId: string) => {
  return request.get(`/api/comments/process/${processInstanceId}/statistics`)
}

// ==================== 流程变量相关接口 ====================

/**
 * 获取流程实例的所有变量
 */
export const getProcessVariables = (processInstanceId: string) => {
  return request.get(`/api/variables/process/${processInstanceId}`)
}

/**
 * 获取流程实例的单个变量
 */
export const getProcessVariable = (processInstanceId: string, variableName: string) => {
  return request.get(`/api/variables/process/${processInstanceId}/variable/${variableName}`)
}

/**
 * 设置流程实例变量
 */
export const setProcessVariables = (data: {
  processInstanceId: string
  variables: Record<string, any>
}) => {
  return request.post('/api/variables/process', data)
}

/**
 * 更新流程实例变量
 */
export const updateProcessVariable = (data: {
  processInstanceId: string
  variableName: string
  value: any
}) => {
  return request.put('/api/variables/process', data)
}

/**
 * 删除流程实例变量
 */
export const removeProcessVariable = (processInstanceId: string, variableName: string) => {
  return request.delete(`/api/variables/process/${processInstanceId}/variable/${variableName}`)
}

/**
 * 获取任务的所有本地变量
 */
export const getTaskVariables = (taskId: string) => {
  return request.get(`/api/variables/task/${taskId}`)
}

/**
 * 获取任务的单个本地变量
 */
export const getTaskVariable = (taskId: string, variableName: string) => {
  return request.get(`/api/variables/task/${taskId}/variable/${variableName}`)
}

/**
 * 设置任务本地变量
 */
export const setTaskVariables = (data: {
  taskId: string
  variables: Record<string, any>
}) => {
  return request.post('/api/variables/task', data)
}

/**
 * 更新任务本地变量
 */
export const updateTaskVariable = (data: {
  taskId: string
  variableName: string
  value: any
}) => {
  return request.put('/api/variables/task', data)
}

/**
 * 获取历史变量
 */
export const getHistoricVariables = (processInstanceId: string) => {
  return request.get(`/api/variables/history/${processInstanceId}`)
}

/**
 * 获取变量的历史变更记录
 */
export const getVariableHistory = (processInstanceId: string, variableName: string) => {
  return request.get(`/api/variables/history/${processInstanceId}/variable/${variableName}`)
}

/**
 * 复制变量
 */
export const copyVariables = (data: {
  sourceProcessInstanceId: string
  targetProcessInstanceId: string
  variableNames?: string[]
}) => {
  return request.post('/api/variables/copy', data)
}
