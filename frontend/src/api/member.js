import request from './request'

/**
 * 会员管理API
 * 
 * 会员状态：0-冻结 1-正常
 * 会员等级：1-普通 2-银卡 3-金卡 4-钻石
 */

// 分页查询会员
export function getMemberPage(params) {
  return request.get('/members/page', { params })
}

// 获取会员详情
export function getMemberById(id) {
  return request.get(`/members/${id}`)
}

// 获取当前会员信息
export function getCurrentMember() {
  return request.get('/members/current')
}

// 新增会员
export function createMember(data) {
  return request.post('/members', data)
}

// 修改会员
export function updateMember(data) {
  return request.put('/members', data)
}

// 修改会员状态
export function updateMemberStatus(id, status) {
  return request.put(`/members/${id}/status`, null, { params: { status } })
}

// 删除会员
export function deleteMember(id) {
  return request.delete(`/members/${id}`)
}
