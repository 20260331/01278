/**
 * 统一错误处理工具
 * 提供友好的错误提示和统一的错误处理机制
 */

import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import router from '@/router'

/**
 * 错误类型枚举
 */
export const ErrorType = {
  // 网络错误
  NETWORK: 'NETWORK',
  // 认证错误
  AUTH: 'AUTH',
  // 权限错误
  PERMISSION: 'PERMISSION',
  // 业务错误
  BUSINESS: 'BUSINESS',
  // 验证错误
  VALIDATION: 'VALIDATION',
  // 服务器错误
  SERVER: 'SERVER',
  // 未知错误
  UNKNOWN: 'UNKNOWN'
}

/**
 * 错误消息映射
 */
const errorMessages = {
  // 网络相关
  'Network Error': '网络连接失败，请检查网络设置',
  'timeout of': '请求超时，请稍后重试',
  'Request failed': '请求失败，请稍后重试',
  
  // 认证相关
  'token expired': '登录已过期，请重新登录',
  'invalid token': '登录信息无效，请重新登录',
  'unauthorized': '请先登录后再操作',
  
  // 权限相关
  'forbidden': '您没有权限执行此操作',
  'access denied': '访问被拒绝',
  
  // 业务相关
  'user not found': '用户不存在',
  'password incorrect': '密码错误',
  'duplicate entry': '数据已存在，请勿重复添加',
  'resource not found': '请求的资源不存在',
  'operation failed': '操作失败，请稍后重试'
}

/**
 * 根据错误信息获取友好的错误提示
 * @param {string} message - 原始错误消息
 * @returns {string} 友好的错误消息
 */
export function getFriendlyMessage(message) {
  if (!message) return '操作失败，请稍后重试'
  
  const lowerMessage = message.toLowerCase()
  
  for (const [key, value] of Object.entries(errorMessages)) {
    if (lowerMessage.includes(key.toLowerCase())) {
      return value
    }
  }
  
  return message
}

/**
 * 获取错误类型
 * @param {Error|Object} error - 错误对象
 * @returns {string} 错误类型
 */
export function getErrorType(error) {
  if (!error) return ErrorType.UNKNOWN
  
  // 网络错误
  if (!error.response || error.message === 'Network Error') {
    return ErrorType.NETWORK
  }
  
  const status = error.response?.status
  
  switch (status) {
    case 401:
      return ErrorType.AUTH
    case 403:
      return ErrorType.PERMISSION
    case 400:
    case 422:
      return ErrorType.VALIDATION
    case 404:
    case 409:
      return ErrorType.BUSINESS
    case 500:
    case 502:
    case 503:
      return ErrorType.SERVER
    default:
      return ErrorType.UNKNOWN
  }
}

/**
 * 显示错误消息
 * @param {string} message - 错误消息
 * @param {string} type - 消息类型 (error/warning/info)
 * @param {number} duration - 显示时长(毫秒)
 */
export function showError(message, type = 'error', duration = 3000) {
  ElMessage({
    message: getFriendlyMessage(message),
    type,
    duration,
    showClose: true
  })
}

/**
 * 显示错误通知
 * @param {string} title - 标题
 * @param {string} message - 消息内容
 */
export function showErrorNotification(title, message) {
  ElNotification({
    title,
    message: getFriendlyMessage(message),
    type: 'error',
    duration: 5000
  })
}

/**
 * 显示确认对话框
 * @param {string} message - 消息内容
 * @param {string} title - 标题
 * @returns {Promise}
 */
export function showConfirm(message, title = '提示') {
  return ElMessageBox.confirm(message, title, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
}

/**
 * 处理API错误
 * @param {Error|Object} error - 错误对象
 * @param {Object} options - 配置选项
 * @param {boolean} options.showMessage - 是否显示错误消息
 * @param {boolean} options.redirect - 是否重定向（用于401错误）
 * @param {Function} options.onError - 自定义错误处理回调
 */
export function handleApiError(error, options = {}) {
  const {
    showMessage = true,
    redirect = true,
    onError = null
  } = options
  
  const errorType = getErrorType(error)
  const message = error.response?.data?.message || error.message || '操作失败'
  
  // 调用自定义错误处理
  if (onError && typeof onError === 'function') {
    onError(error, errorType)
    return
  }
  
  switch (errorType) {
    case ErrorType.NETWORK:
      if (showMessage) {
        showError('网络连接失败，请检查网络设置')
      }
      break
      
    case ErrorType.AUTH:
      if (showMessage) {
        // 判断是否在登录页面
        const isLoginPage = router.currentRoute.value.path === '/login'
        if (isLoginPage) {
          showError(message)
        } else {
          showError('登录已过期，请重新登录')
          if (redirect) {
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            router.push('/login')
          }
        }
      }
      break
      
    case ErrorType.PERMISSION:
      if (showMessage) {
        showError('您没有权限执行此操作')
      }
      break
      
    case ErrorType.VALIDATION:
      if (showMessage) {
        showError(message)
      }
      break
      
    case ErrorType.BUSINESS:
      if (showMessage) {
        showError(message)
      }
      break
      
    case ErrorType.SERVER:
      if (showMessage) {
        showError('服务器繁忙，请稍后重试')
      }
      break
      
    default:
      if (showMessage) {
        showError(message)
      }
  }
}

/**
 * 处理表单验证错误
 * @param {Object} errors - 验证错误对象
 */
export function handleValidationError(errors) {
  if (!errors || typeof errors !== 'object') return
  
  const firstError = Object.values(errors)[0]
  if (Array.isArray(firstError) && firstError.length > 0) {
    showError(firstError[0], 'warning')
  } else if (typeof firstError === 'string') {
    showError(firstError, 'warning')
  }
}

/**
 * 全局错误处理器（用于 Vue 全局错误捕获）
 * @param {Error} error - 错误对象
 * @param {Object} instance - Vue 组件实例
 * @param {string} info - 错误信息
 */
export function globalErrorHandler(error, instance, info) {
  console.error('全局错误:', error)
  console.error('错误信息:', info)
  
  // 忽略已被响应拦截器处理的 API 错误（避免重复提示）
  if (error?.isAxiosError || error?.response || error?.config) {
    return
  }
  
  // 忽略 ElMessageBox 取消操作
  if (error === 'cancel' || error?.toString?.().includes('cancel')) {
    return
  }
  
  // 生产环境下可以上报错误日志
  if (process.env.NODE_ENV === 'production') {
    // TODO: 上报错误到日志服务
    // reportError(error, info)
  }
  
  // 显示友好的错误提示
  showError('页面出现异常，请刷新后重试')
}

/**
 * Promise 未捕获异常处理器
 * @param {PromiseRejectionEvent} event - 异常事件
 */
export function unhandledRejectionHandler(event) {
  console.error('未捕获的Promise异常:', event.reason)
  
  // 阻止默认处理
  event.preventDefault()
  
  // 可以选择性地显示错误提示
  // showError('操作失败，请稍后重试')
}

/**
 * 初始化全局错误处理
 * @param {Object} app - Vue 应用实例
 */
export function initErrorHandler(app) {
  // Vue 全局错误处理
  app.config.errorHandler = globalErrorHandler
  
  // 未捕获的 Promise 异常处理
  window.addEventListener('unhandledrejection', unhandledRejectionHandler)
  
  // 全局 JS 错误处理
  window.onerror = (message, source, lineno, colno, error) => {
    console.error('全局JS错误:', { message, source, lineno, colno, error })
    return true // 阻止默认处理
  }
}

export default {
  ErrorType,
  getFriendlyMessage,
  getErrorType,
  showError,
  showErrorNotification,
  showConfirm,
  handleApiError,
  handleValidationError,
  globalErrorHandler,
  unhandledRejectionHandler,
  initErrorHandler
}
