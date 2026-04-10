import request from './request'

/**
 * 统计分析API
 *
 * 提供系统各类统计数据
 */

// 获取管理员统计
export function getAdminStatistics() {
  return request.get('/statistics/admin/dashboard')
}

// 获取教练业绩
export function getCoachPerformance(startDate, endDate) {
  return request.get('/statistics/coach/performance', { params: { startDate, endDate } })
}

// 获取会员统计
export function getMemberStatistics() {
  return request.get('/statistics/member/dashboard')
}

// 获取系统日志
export function getSystemLogs(params) {
  return request.get('/statistics/logs', { params })
}

// 获取高风险课程
export function getHighRiskCourses() {
  return request.get('/statistics/high-risk-courses')
}
