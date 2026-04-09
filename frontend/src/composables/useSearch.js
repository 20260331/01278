import { reactive } from 'vue'

/**
 * 搜索表单管理组合式函数
 * @param {Object} initialValues - 初始值
 * @returns {Object} 搜索表单状态和方法
 */
export function useSearch(initialValues = {}) {
  const searchForm = reactive({ ...initialValues })

  /**
   * 重置搜索表单
   */
  const resetSearch = () => {
    Object.keys(initialValues).forEach(key => {
      searchForm[key] = initialValues[key]
    })
  }

  /**
   * 获取非空搜索参数
   * @returns {Object} 过滤后的搜索参数
   */
  const getSearchParams = () => {
    const params = {}
    Object.keys(searchForm).forEach(key => {
      const value = searchForm[key]
      if (value !== null && value !== undefined && value !== '') {
        params[key] = value
      }
    })
    return params
  }

  return {
    searchForm,
    resetSearch,
    getSearchParams
  }
}
