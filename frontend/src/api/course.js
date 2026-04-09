import request from './request'

/**
 * 课程管理API
 * 
 * 课程状态：0-已取消 1-正常 2-已满 3-已结束
 * 请假状态：0-待审核 1-已通过 2-已拒绝
 */

// 分页查询课程
export function getCoursePage(params) {
  return request.get('/courses/page', { params })
}

// 获取可预约课程
export function getAvailableCourses() {
  return request.get('/courses/available')
}

// 获取课程详情
export function getCourseById(id) {
  return request.get(`/courses/${id}`)
}

// 新增课程
export function createCourse(data) {
  return request.post('/courses', data)
}

// 修改课程
export function updateCourse(data) {
  return request.put('/courses', data)
}

// 删除课程
export function deleteCourse(id) {
  return request.delete(`/courses/${id}`)
}

// 提交请假申请
export function submitLeave(data) {
  return request.post('/courses/leave', data)
}

// 审核请假
export function auditLeave(id, status, remark) {
  return request.put(`/courses/leave/audit/${id}`, null, { params: { status, remark } })
}

// 获取请假列表
export function getLeavePage(params) {
  return request.get('/courses/leave/page', { params })
}

// 检查教练是否请假
export function checkCoachLeave(coachId, startTime, duration) {
  return request.get('/courses/leave/check', { params: { coachId, startTime, duration } })
}
