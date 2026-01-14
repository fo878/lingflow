import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 30000
})

request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 检查业务状态码
    if (res.code === 500) {
      ElMessage.error(res.message || '服务器错误')
      return Promise.reject(new Error(res.message || '服务器错误'))
    } else if (res.code !== 200) {
      ElMessage.warning(res.message || '未知错误')
      return Promise.reject(new Error(res.message || '未知错误'))
    }
    
    return response
  },
  error => {
    console.error('请求错误:', error)
    
    if (error.response) {
      const status = error.response.status
      const message = error.response.data?.message || error.message || '请求失败'
      
      if (status === 500) {
        ElMessage.error(`服务器错误: ${message}`)
      } else if (status === 404) {
        ElMessage.error('请求的资源不存在')
      } else if (status === 401) {
        ElMessage.error('未授权，请重新登录')
      } else if (status === 403) {
        ElMessage.error('拒绝访问')
      } else {
        ElMessage.error(message)
      }
    } else if (error.request) {
      ElMessage.error('网络错误，请检查网络连接')
    } else {
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
