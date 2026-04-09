import request from './request'

/**
 * 器材管理API
 * 
 * 器材状态：0-报废 1-正常 2-维修中
 */

// 分页查询器材
export function getEquipmentPage(params) {
  return request.get('/equipments/page', { params })
}

// 获取器材详情
export function getEquipmentById(id) {
  return request.get(`/equipments/${id}`)
}

// 新增器材
export function createEquipment(data) {
  return request.post('/equipments', data)
}

// 修改器材
export function updateEquipment(data) {
  return request.put('/equipments', data)
}

// 删除器材
export function deleteEquipment(id) {
  return request.delete(`/equipments/${id}`)
}

// 记录维护
export function maintainEquipment(id, remark) {
  return request.put(`/equipments/${id}/maintenance`, null, { params: { remark } })
}
