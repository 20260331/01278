import { ref, reactive } from 'vue'

/**
 * 弹窗管理组合式函数
 * @param {Object} initialFormData - 表单初始数据
 * @returns {Object} 弹窗状态和方法
 */
export function useDialog(initialFormData = {}) {
  const dialogVisible = ref(false)
  const isEdit = ref(false)
  const submitLoading = ref(false)
  const formData = reactive({ ...initialFormData })

  /**
   * 打开新增弹窗
   */
  const openAddDialog = () => {
    isEdit.value = false
    resetFormData()
    dialogVisible.value = true
  }

  /**
   * 打开编辑弹窗
   * @param {Object} row - 行数据
   */
  const openEditDialog = (row) => {
    isEdit.value = true
    Object.assign(formData, row)
    dialogVisible.value = true
  }

  /**
   * 关闭弹窗
   */
  const closeDialog = () => {
    dialogVisible.value = false
  }

  /**
   * 重置表单数据
   */
  const resetFormData = () => {
    Object.keys(initialFormData).forEach(key => {
      formData[key] = initialFormData[key]
    })
  }

  /**
   * 设置提交状态
   * @param {boolean} status - 状态
   */
  const setSubmitLoading = (status) => {
    submitLoading.value = status
  }

  return {
    dialogVisible,
    isEdit,
    submitLoading,
    formData,
    openAddDialog,
    openEditDialog,
    closeDialog,
    resetFormData,
    setSubmitLoading
  }
}
