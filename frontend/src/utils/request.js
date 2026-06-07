import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 创建 Axios 实例
const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

let isRefreshing = false
let refreshQueue = []

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token')
    if (token) config.headers.Authorization = `Bearer ${token}`
    if (config.data instanceof FormData) delete config.headers['Content-Type']
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) return res
    ElMessage.error(res.message || '请求失败')
    return Promise.reject(new Error(res.message))
  },
  async (error) => {
    const { response, config } = error
    if (response?.status === 401 && !config._retry) {
      // 尝试自动刷新Token
      const refreshToken = localStorage.getItem('refresh_token')
      if (refreshToken && !isRefreshing) {
        isRefreshing = true
        try {
          const res = await axios.post('/api/v1/auth/refresh-token', { refreshToken })
          const newAccess = res.data?.data?.accessToken || res.data?.accessToken
          if (newAccess) {
            localStorage.setItem('access_token', newAccess)
            config.headers.Authorization = `Bearer ${newAccess}`
            config._retry = true
            return request(config)
          }
        } catch (e) { /* refresh failed, fall through to login */ }
        finally { isRefreshing = false }
      }
      // 刷新失败，退回登录
      ElMessage.error('登录已过期，请重新登录')
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      router.push('/login')
      return Promise.reject(error)
    }

    if (response) {
      const { status, data } = response
      if (status === 403) ElMessage.error('没有访问权限')
      else if (status === 404) ElMessage.error('请求的资源不存在')
      else if (status === 500) ElMessage.error('服务器错误，请稍后重试')
      else ElMessage.error(data?.message || '请求失败')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络')
    } else {
      ElMessage.error('网络错误，请检查连接')
    }
    return Promise.reject(error)
  }
)

export default request
