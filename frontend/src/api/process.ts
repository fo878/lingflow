import request from './index'

// 流程定义相关接口
export const deployProcess = (data: { name: string; xml: string }) => {
  return request.post('/process/deploy', data)
}

export const getProcessDefinitions = () => {
  return request.get('/process/definitions')
}

export const deleteProcessDefinition = (deploymentId: string) => {
  return request.delete(`/process/definition/${deploymentId}`)
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

// 快照相关接口
export const createProcessSnapshot = (data: { 
  processDefinitionKey: string; 
  snapshotName: string; 
  description?: string; 
  creator?: string 
}) => {
  return request.post('/snapshot/create', data)
}

export const getProcessSnapshots = (processDefinitionKey: string) => {
  return request.get(`/snapshot/list/${processDefinitionKey}`)
}

export const rollbackToSnapshot = (snapshotId: string) => {
  return request.post(`/snapshot/rollback/${snapshotId}`)
}

export const deleteSnapshot = (snapshotId: string) => {
  return request.delete(`/snapshot/${snapshotId}`)
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
