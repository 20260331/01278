<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">器材管理</h1>
        <p class="text-sm text-slate-500">管理健身房所有器材设备</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-blue-500 transition-colors duration-300 shadow-lg shadow-slate-200">
        <iconify-icon icon="lucide:plus" width="18"></iconify-icon>
        新增器材
      </button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-input v-model="searchForm.name" placeholder="搜索器材名称" clearable style="width: 220px">
          <template #prefix>
            <iconify-icon icon="lucide:search" width="16" class="text-slate-400"></iconify-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 130px">
          <el-option label="正常" :value="1" />
          <el-option label="维修中" :value="0" />
          <el-option label="报废" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchData" class="!rounded-lg">搜索</el-button>
        <el-button @click="resetSearch" class="!rounded-lg">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="器材信息" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center flex-shrink-0">
                <iconify-icon icon="lucide:dumbbell" width="24" class="text-blue-500"></iconify-icon>
              </div>
              <div>
                <p class="font-medium text-slate-900">{{ row.name }}</p>
                <p class="text-xs text-slate-500">{{ row.type || '未分类' }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="brand" label="品牌" min-width="110">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.brand || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center gap-2 text-slate-600">
              <iconify-icon icon="lucide:map-pin" width="14" class="text-slate-400"></iconify-icon>
              {{ row.location || '-' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getStatusClass(row.status)">
              <iconify-icon :icon="getStatusIcon(row.status)" width="12"></iconify-icon>
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="lastMaintainDate" label="上次维护" min-width="120">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.lastMaintainDate || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <button @click="handleEdit(row)" class="p-2 text-slate-400 hover:text-blue-500 hover:bg-blue-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:edit-3" width="16"></iconify-icon>
              </button>
              <button @click="handleMaintain(row)" class="p-2 text-slate-400 hover:text-amber-500 hover:bg-amber-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:wrench" width="16"></iconify-icon>
              </button>
              <button @click="handleDelete(row)" class="p-2 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:trash-2" width="16"></iconify-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-between items-center border-t border-slate-100">
        <p class="text-sm text-slate-500">共 {{ pagination.total }} 条记录</p>
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑器材' : '新增器材'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入器材名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-input v-model="formData.type" placeholder="如：有氧器械、力量器械" />
        </el-form-item>
        <el-form-item label="品牌" prop="brand">
          <el-input v-model="formData.brand" placeholder="请输入品牌" />
        </el-form-item>
        <el-form-item label="位置" prop="location">
          <el-input v-model="formData.location" placeholder="请输入放置位置" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="formData.status" class="w-full">
            <el-option label="正常" :value="1" />
            <el-option label="维修中" :value="0" />
            <el-option label="报废" :value="2" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="!rounded-lg">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="!rounded-lg">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEquipmentPage, createEquipment, updateEquipment, deleteEquipment, maintainEquipment } from '@/api/equipment'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const searchForm = reactive({ name: '', status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const formData = reactive({ id: null, name: '', type: '', brand: '', location: '', status: 1 })

const rules = { name: [{ required: true, message: '请输入名称', trigger: 'blur' }] }

const getStatusText = (s) => ({ 0: '维修中', 1: '正常', 2: '报废' }[s] || '-')
const getStatusClass = (s) => ({
  0: 'bg-amber-50 text-amber-600',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-slate-100 text-slate-500'
}[s] || '')
const getStatusIcon = (s) => ({
  0: 'lucide:wrench',
  1: 'lucide:check-circle',
  2: 'lucide:x-circle'
}[s] || 'lucide:circle')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getEquipmentPage({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, { name: '', status: null })
  pagination.current = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, { id: null, name: '', type: '', brand: '', location: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateEquipment(formData)
      ElMessage.success('修改成功')
    } else {
      await createEquipment(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleMaintain = async (row) => {
  const { value } = await ElMessageBox.prompt('请输入维护备注', '记录维护', { confirmButtonText: '确定', cancelButtonText: '取消' })
  await maintainEquipment(row.id, value || '')
  ElMessage.success('维护记录已保存')
  fetchData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该器材吗？', '提示', { type: 'warning' })
  await deleteEquipment(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => fetchData())
</script>
