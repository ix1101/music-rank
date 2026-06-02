import axios from 'axios'

/**
 * 后端 API 请求实例 (localhost:8080)
 * 自动携带通用配置和错误处理
 */
const apiClient = axios.create({
  baseURL: 'http://localhost:8080/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
apiClient.interceptors.request.use(
  (config) => {
    // 可在此添加 token 等通用请求头
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理错误
apiClient.interceptors.response.use(
  (response) => {
    // 后端返回统一格式 { code, message, data }
    const body = response.data
    if (body && body.code && body.code !== 200) {
      // 业务错误
      const errMsg = body.message || '请求失败'
      return Promise.reject(new Error(errMsg))
    }
    return response
  },
  (error) => {
    // 网络错误
    if (!error.response) {
      return Promise.reject(new Error('网络连接失败，请检查后端是否启动'))
    }
    const { status } = error.response
    if (status >= 500) {
      return Promise.reject(new Error('服务器内部错误，请稍后重试'))
    }
    if (status === 409) {
      return Promise.reject(new Error('数据已存在，请勿重复添加'))
    }
    if (status === 400) {
      const msg = error.response.data?.message || '请求参数错误'
      return Promise.reject(new Error(msg))
    }
    return Promise.reject(error)
  }
)

export default apiClient
