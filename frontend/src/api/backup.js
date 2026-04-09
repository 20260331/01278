import request from './request'

/**
 * 数据备份API
 * 
 * 备份内容：会员、教练、课程、器材、反馈、消费记录
 */

// 获取备份统计信息
export function getBackupStats() {
  return request.get('/backups/stats')
}

// 导出数据备份
export function exportBackup() {
  return request.get('/backups/export', { responseType: 'blob' })
}

// 验证备份文件
export function validateBackup(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/backups/validate', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

// 导入数据恢复
// mode: 'increment'（增量，默认）或 'overwrite'（覆盖）
export function importBackup(file, mode = 'increment') {
  const formData = new FormData()
  formData.append('file', file)
  return request.post(`/backups/import?mode=${mode}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
