import request from './request'

export function getNotificationPage(params) {
  return request.get('/notifications/page', { params })
}

export function getMyNotifications(params) {
  return request.get('/notifications/my', { params })
}

export function markAsRead(id) {
  return request.put(`/notifications/${id}/read`)
}

export function markAllAsRead() {
  return request.put('/notifications/mark-all-read')
}

export function getUnreadCount() {
  return request.get('/notifications/unread-count')
}
