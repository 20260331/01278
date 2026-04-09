import request from './request'

/**
 * 教练管理API
 * 
 * 教练状态：0-待审核 1-正常 2-已离职
 */

// 分页查询教练
export function getCoachPage(params) {
  return request.get('/coaches/page', { params })
}

// 获取所有教练
export function getCoachList() {
  return request.get('/coaches/list')
}

// 获取教练详情
export function getCoachById(id) {
  return request.get(`/coaches/${id}`)
}

// 获取当前教练信息
export function getCurrentCoach() {
  return request.get('/coaches/current')
}

// 新增教练
export function createCoach(data) {
  return request.post('/coaches', data)
}

// 修改教练
export function updateCoach(data) {
  return request.put('/coaches', data)
}

// 审核教练
export function auditCoach(id, status) {
  return request.put(`/coaches/${id}/audit`, null, { params: { status } })
}

// 获取教练排班
export function getCoachSchedule(id) {
  return request.get(`/coaches/${id}/schedule`)
}

// 删除教练
export function deleteCoach(id) {
  return request.delete(`/coaches/${id}`)
}
