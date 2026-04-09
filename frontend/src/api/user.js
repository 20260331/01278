import request from './request'

/**
 * 用户管理API
 * 
 * 用户角色：ADMIN-管理员 COACH-教练 MEMBER-会员
 */

// 分页查询用户
export function getUserPage(params) {
  return request.get('/users/page', { params })
}

// 获取用户详情
export function getUserById(id) {
  return request.get(`/users/${id}`)
}

// 新增用户
export function createUser(data) {
  return request.post('/users', data)
}

// 修改用户
export function updateUser(data) {
  return request.put('/users', data)
}

// 删除用户
export function deleteUser(id) {
  return request.delete(`/users/${id}`)
}

// 重置用户密码
export function resetPassword(id, newPassword) {
  return request.put(`/users/${id}/password`, null, { 
    params: { newPassword } 
  })
}
