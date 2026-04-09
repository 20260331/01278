import { ref } from 'vue'
import { usePagination } from './usePagination'
import { useSearch } from './useSearch'

/**
 * 表格数据管理组合式函数
 * @param {Function} fetchApi - 获取数据的API函数
 * @param {Object} options - 配置选项
 * @param {Object} options.searchFields - 搜索字段初始值
 * @param {number} options.defaultSize - 默认每页条数
 * @returns {Object} 表格状态和方法
 */
export function useTable(fetchApi, options = {}) {
  const { searchFields = {}, defaultSize = 10 } = options

  const loading = ref(false)
  const tableData = ref([])

  const { pagination, resetPagination, setTotal, getPaginationParams } = usePagination({ defaultSize })
  const { searchForm, resetSearch: resetSearchForm, getSearchParams } = useSearch(searchFields)

  /**
   * 获取表格数据
   */
  const fetchData = async () => {
    loading.value = true
    try {
      const params = {
        ...getPaginationParams(),
        ...getSearchParams()
      }
      const res = await fetchApi(params)
      tableData.value = res.data.records || res.data || []
      setTotal(res.data.total || 0)
    } catch (error) {
      console.error('获取数据失败:', error)
      tableData.value = []
      setTotal(0)
    } finally {
      loading.value = false
    }
  }

  /**
   * 重置搜索并刷新数据
   */
  const resetSearch = () => {
    resetSearchForm()
    resetPagination()
    fetchData()
  }

  /**
   * 搜索
   */
  const handleSearch = () => {
    resetPagination()
    fetchData()
  }

  /**
   * 分页变化
   */
  const handlePageChange = () => {
    fetchData()
  }

  return {
    loading,
    tableData,
    pagination,
    searchForm,
    fetchData,
    resetSearch,
    handleSearch,
    handlePageChange
  }
}
