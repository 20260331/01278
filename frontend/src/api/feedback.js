import request from './request'

/**
 * 意见反馈API
 * 
 * 反馈状态：0-待处理 1-已回复
 */

// 分页查询反馈
export function getFeedbackPage(params) {
  return request.get('/feedbacks/page', { params })
}

// 获取我的反馈
export function getMyFeedbacks(params) {
  return request.get('/feedbacks/my', { params })
}

// 提交反馈
export function createFeedback(data) {
  return request.post('/feedbacks', data)
}

// 回复反馈
export function replyFeedback(id, reply) {
  return request.put(`/feedbacks/${id}/reply`, null, { params: { reply } })
}
