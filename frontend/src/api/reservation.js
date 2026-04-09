import request from './request'

/**
 * 预约管理API
 * 
 * 预约状态：0-已预约 1-已签到 2-已取消 3-缺席
 * 签到时间窗口：课程开始前30分钟至课程结束
 */

// 分页查询预约
export function getReservationPage(params) {
  return request.get('/reservations/page', { params })
}

// 获取我的预约
export function getMyReservations(params) {
  return request.get('/reservations/my', { params })
}

// 创建预约
export function createReservation(courseId) {
  return request.post('/reservations', null, { params: { courseId } })
}

// 取消预约
export function cancelReservation(id) {
  return request.put(`/reservations/${id}/cancel`)
}

// 签到
export function checkinReservation(id) {
  return request.put(`/reservations/${id}/checkin`)
}
