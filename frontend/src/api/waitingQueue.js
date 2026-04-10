import request from './request'

const waitingQueueApi = {
  join: (courseId) => {
    return request({
      url: `/waiting-queue/join/${courseId}`,
      method: 'post'
    })
  },

  cancel: (id) => {
    return request({
      url: `/waiting-queue/cancel/${id}`,
      method: 'post'
    })
  },

  getByCourseId: (courseId) => {
    return request({
      url: `/waiting-queue/course/${courseId}`,
      method: 'get'
    })
  },

  getMemberInfo: (courseId) => {
    return request({
      url: `/waiting-queue/member-info/${courseId}`,
      method: 'get'
    })
  },

  getHighRiskCourses: () => {
    return request({
      url: '/waiting-queue/high-risk-courses',
      method: 'get'
    })
  },

  getMyWaitingQueue: () => {
    return request({
      url: '/waiting-queue/my-waiting',
      method: 'get'
    })
  },

  getUnreadNotified: () => {
    return request({
      url: '/waiting-queue/unread-notified',
      method: 'get'
    })
  },

  markAsRead: (id) => {
    return request({
      url: `/waiting-queue/mark-as-read/${id}`,
      method: 'post'
    })
  }
}

export default waitingQueueApi
