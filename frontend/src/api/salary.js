import request from './request'

/**
 * 薪资管理API
 * 
 * 薪资计算规则：总薪资 = 基本薪资 + 课程收入 × 10%
 */

// 获取教练薪资列表
export function getSalaryList(month) {
  return request.get('/salaries', { params: { month } })
}

// 获取薪资汇总
export function getSalarySummary(month) {
  return request.get('/salaries/summary', { params: { month } })
}
