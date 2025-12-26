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
