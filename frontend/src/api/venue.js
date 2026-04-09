import request from './request'

/**
 * 场地管理API
 * 
 * 场地状态：0-停用 1-正常 2-维护中
 */

// 分页查询场地
export function getVenuePage(params) {
  return request.get('/venues/page', { params })
}

// 获取场地详情
export function getVenueById(id) {
  return request.get(`/venues/${id}`)
}

// 新增场地
export function createVenue(data) {
  return request.post('/venues', data)
}

// 修改场地
export function updateVenue(data) {
  return request.put('/venues', data)
}

// 删除场地
export function deleteVenue(id) {
  return request.delete(`/venues/${id}`)
}

// 修改场地状态
export function updateVenueStatus(id, status) {
  return request.put(`/venues/${id}/status`, null, { params: { status } })
}
