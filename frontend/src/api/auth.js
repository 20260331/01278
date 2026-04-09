import request from './request'

/**
 * 认证管理API
 * 
 * 登录成功后返回JWT令牌，有效期24小时
 */

// 登录
export function login(data) {
  return request.post('/auth/login', data)
}

// 获取当前用户信息
export function getUserInfo() {
  return request.get('/auth/info')
}

// 修改密码
export function updatePassword(data) {
  return request.put('/auth/password', data)
}

// 退出登录
export function logout() {
  return request.post('/auth/logout')
}
