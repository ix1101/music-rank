import axios from 'axios'

/**
 * 后端 API 请求实例
 * 开发模式 Vite 代理 /api → localhost:8080/api
 * 生产模式 Nginx 代理 /api → localhost:8080/api
 */
const apiClient = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

// 响应拦截器：统一处理错误
apiClient.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && body.code && body.code !== 200) {
      return Promise.reject(new Error(body.message || '请求失败'))
    }
    return response
  },
  (error) => {
    if (!error.response) {
      return Promise.reject(new Error('网络连接失败，请检查后端是否启动'))
    }
    const { status } = error.response
    if (status >= 500) return Promise.reject(new Error('服务器内部错误，请稍后重试'))
    if (status === 409) return Promise.reject(new Error('数据已存在，请勿重复添加'))
    if (status === 400) {
      return Promise.reject(new Error(error.response.data?.message || '请求参数错误'))
    }
    return Promise.reject(error)
  }
)

/**
 * 图片 URL 转 HTTPS（避免混合内容警告）
 */
export function httpsUrl(url) {
  if (!url) return 'https://picsum.photos/200/200'
  return url.replace(/^http:/, 'https:')
}

export default apiClient
