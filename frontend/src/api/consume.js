import request from './request'

/**
 * 消费记录API
 * 
 * 消费类型：1-充值 2-消费 3-退款
 */

// 分页查询消费记录
export function getConsumePage(params) {
  return request.get('/consume-records/page', { params })
}

// 获取我的消费记录
export function getMyConsumeRecords(params) {
  return request.get('/consume-records/my', { params })
}

// 会员充值
export function recharge(data) {
  return request.post('/consume-records/recharge', data)
}

// 收支统计
export function getStatistics(startDate, endDate) {
  return request.get('/consume-records/statistics', { params: { startDate, endDate } })
}

// 导出我的消费记录
export function exportMyRecords(params) {
  return request.get('/consume-records/export', { 
    params, 
    responseType: 'blob' 
  })
}

// 导出消费记录（管理员）
export function exportRecords(params) {
  return request.get('/consume-records/export/admin', { 
    params, 
    responseType: 'blob' 
  })
}
