import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { handleApiError, ErrorType, getErrorType } from '@/utils/errorHandler'

// 创建axios实例
// API版本号：v1，用于接口版本管理，后续升级可增加v2版本
const request = axios.create({
  baseURL: '/api/v1',
  timeout: 15000, // 增加超时时间到15秒
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求拦截器错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    // 如果是 blob 类型（文件下载），直接返回
    if (response.config.responseType === 'blob') {
      return response.data
    }
    
    const res = response.data
    if (res.code !== 200) {
      // 使用统一的错误提示
      ElMessage.error(res.message || '请求失败')
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  error => {
    // 使用统一的错误处理
    const errorType = getErrorType(error)
    
    if (error.response) {
      const { status, data } = error.response
      const errorMsg = data?.message || '请求失败'
      
      switch (status) {
        case 401:
          // 判断是否在登录页面
          const isLoginPage = window.location.pathname === '/login' || 
                              window.location.hash.includes('/login')
          if (isLoginPage) {
            ElMessage.error(errorMsg)
          } else {
            ElMessage.error('登录已过期，请重新登录')
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            router.push('/login')
          }
          break
          
        case 403:
          ElMessage.error('您没有权限执行此操作')
          break
          
        case 404:
          ElMessage.error('请求的资源不存在')
          break
          
        case 408:
          ElMessage.error('请求超时，请稍后重试')
          break
          
        case 422:
          // 验证错误，显示具体错误信息
          ElMessage.error(errorMsg)
          break
          
        case 429:
          ElMessage.error('请求过于频繁，请稍后重试')
          break
          
        case 500:
          ElMessage.error('服务器繁忙，请稍后重试')
          break
          
        case 502:
        case 503:
        case 504:
          ElMessage.error('服务暂时不可用，请稍后重试')
          break
          
        default:
          ElMessage.error(errorMsg)
      }
    } else if (error.code === 'ECONNABORTED') {
      // 请求超时
      ElMessage.error('请求超时，请检查网络连接后重试')
    } else if (error.message === 'Network Error') {
      // 网络错误
      ElMessage.error('网络连接失败，请检查网络设置')
    } else {
      // 其他错误
      ElMessage.error('请求失败，请稍后重试')
    }
    
    return Promise.reject(error)
  }
)

/**
 * 封装 GET 请求
 * @param {string} url - 请求地址
 * @param {Object} params - 查询参数
 * @param {Object} config - 额外配置
 */
export function get(url, params = {}, config = {}) {
  return request.get(url, { params, ...config })
}

/**
 * 封装 POST 请求
 * @param {string} url - 请求地址
 * @param {Object} data - 请求体数据
 * @param {Object} config - 额外配置
 */
export function post(url, data = {}, config = {}) {
  return request.post(url, data, config)
}

/**
 * 封装 PUT 请求
 * @param {string} url - 请求地址
 * @param {Object} data - 请求体数据
 * @param {Object} config - 额外配置
 */
export function put(url, data = {}, config = {}) {
  return request.put(url, data, config)
}

/**
 * 封装 DELETE 请求
 * @param {string} url - 请求地址
 * @param {Object} config - 额外配置
 */
export function del(url, config = {}) {
  return request.delete(url, config)
}

/**
 * 文件下载请求
 * @param {string} url - 请求地址
 * @param {Object} params - 查询参数
 * @param {string} filename - 文件名
 */
export function download(url, params = {}, filename = '') {
  return request.get(url, {
    params,
    responseType: 'blob'
  }).then(blob => {
    // 创建下载链接
    const downloadUrl = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = filename || 'download'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(downloadUrl)
  })
}

export default request
