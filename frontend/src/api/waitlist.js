import request from './request'

export function getWaitlistPage(params) {
  return request.get('/waitlist/page', { params })
}

export function getMyWaitlist(params) {
  return request.get('/waitlist/my', { params })
}

export function joinWaitlist(courseId) {
  return request.post('/waitlist', null, { params: { courseId } })
}

export function cancelWaitlist(id) {
  return request.put(`/waitlist/${id}/cancel`)
}

export function confirmWaitlist(id) {
  return request.put(`/waitlist/${id}/confirm`)
}

export function getWaitlistCount(courseId) {
  return request.get(`/waitlist/count/${courseId}`)
}

export function getWaitlistByCourseId(courseId) {
  return request.get(`/waitlist/course/${courseId}`)
}
