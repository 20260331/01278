import request from './request'

export function getWaitingListPage(params) {
  return request.get('/waiting-list/page', { params })
}

export function getMyWaitingList(params) {
  return request.get('/waiting-list/my', { params })
}

export function joinWaitingList(courseId) {
  return request.post('/waiting-list', null, { params: { courseId } })
}

export function cancelWaitingList(id) {
  return request.put(`/waiting-list/${id}/cancel`)
}
