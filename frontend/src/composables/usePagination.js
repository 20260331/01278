import { reactive } from 'vue'

/**
 * 分页状态管理组合式函数
 * @param {Object} options - 配置选项
 * @param {number} options.defaultSize - 默认每页条数
 * @returns {Object} 分页状态和方法
 */
export function usePagination(options = {}) {
  const { defaultSize = 10 } = options

  const pagination = reactive({
    current: 1,
    size: defaultSize,
    total: 0
  })

  /**
   * 重置分页到第一页
   */
  const resetPagination = () => {
    pagination.current = 1
  }

  /**
   * 更新总数
   * @param {number} total - 总记录数
   */
  const setTotal = (total) => {
    pagination.total = total
  }

  /**
   * 获取分页参数
   * @returns {Object} 分页参数对象
   */
  const getPaginationParams = () => ({
    current: pagination.current,
    size: pagination.size
  })

  return {
    pagination,
    resetPagination,
    setTotal,
    getPaginationParams
  }
}
