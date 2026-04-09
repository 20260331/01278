import request from './request'

/**
 * 健身记录API
 * 
 * 记录包含：体重、运动时长、运动类型、消耗卡路里、教练建议
 */

// 分页查询健身记录
export function getFitnessPage(params) {
  return request.get('/fitness-records/page', { params })
}

// 获取我的健身记录
export function getMyFitnessRecords(params) {
  return request.get('/fitness-records/my', { params })
}

// 录入健身数据
export function createFitnessRecord(data) {
  return request.post('/fitness-records', data)
}

// 获取健身周报
export function getWeeklyReport() {
  return request.get('/fitness-records/report')
}

// 教练发送健身建议
export function sendAdvice(id, advice) {
  return request.put(`/fitness-records/${id}/advice`, null, { params: { advice } })
}
