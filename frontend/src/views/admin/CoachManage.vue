<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">教练管理</h1>
        <p class="text-sm text-slate-500">管理所有教练信息和审核状态</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-blue-500 transition-colors duration-300 shadow-lg shadow-slate-200">
        <iconify-icon icon="lucide:plus" width="18"></iconify-icon>
        新增教练
      </button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-input v-model="searchForm.name" placeholder="搜索教练姓名" clearable style="width: 220px">
          <template #prefix>
            <iconify-icon icon="lucide:search" width="16" class="text-slate-400"></iconify-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 130px">
          <el-option label="待审核" :value="0" />
          <el-option label="正常" :value="1" />
          <el-option label="离职" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchData" class="!rounded-lg">搜索</el-button>
        <el-button @click="resetSearch" class="!rounded-lg">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="教练信息" min-width="220">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-emerald-500 rounded-full flex items-center justify-center flex-shrink-0">
                <span class="text-white font-medium text-sm">{{ (row.name || 'C').charAt(0).toUpperCase() }}</span>
              </div>
              <div>
                <p class="font-medium text-slate-900">{{ row.name }}</p>
                <p class="text-xs text-slate-500">{{ row.username }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="150">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="specialty" label="专长" min-width="250">
          <template #default="{ row }">
            <div class="flex flex-wrap gap-1">
              <template v-if="row.specialty">
                <span v-for="(s, i) in row.specialty.split(',')" :key="i" class="px-2 py-0.5 bg-blue-50 text-blue-600 rounded text-xs">
                  {{ s.trim() }}
                </span>
              </template>
              <span v-else class="text-slate-400">-</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="120">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getStatusClass(row.status)">
              <span class="w-1.5 h-1.5 rounded-full" :class="getStatusDotClass(row.status)"></span>
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <button @click="handleEdit(row)" class="p-2 text-slate-400 hover:text-blue-500 hover:bg-blue-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:edit-3" width="16"></iconify-icon>
              </button>
              <button v-if="row.status === 0" @click="handleAudit(row, 1)" class="p-2 text-slate-400 hover:text-emerald-500 hover:bg-emerald-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:check-circle" width="16"></iconify-icon>
              </button>
              <button v-if="row.status === 0" @click="handleAudit(row, 2)" class="p-2 text-slate-400 hover:text-amber-500 hover:bg-amber-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:x-circle" width="16"></iconify-icon>
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教练' : '新增教练'" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="formData.name" placeholder="请输入姓名" />
        </el-form-item>
        <template v-if="!isEdit">
          <el-form-item label="账号" prop="username">
            <el-input v-model="formData.username" placeholder="请输入登录账号" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="formData.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
        </template>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="formData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-radio-group v-model="formData.gender">
            <el-radio :label="1">男</el-radio>
            <el-radio :label="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="专长" prop="specialty">
          <el-input v-model="formData.specialty" placeholder="如：健身塑形,力量训练" />
        </el-form-item>
        <el-form-item label="基本薪资" prop="salary">
          <el-input-number v-model="formData.salary" :min="0" :precision="2" :step="500" placeholder="请输入基本薪资" style="width: 100%" />
          <span class="text-xs text-slate-400 mt-1">元/月</span>
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
import { getCoachPage, createCoach, updateCoach, auditCoach, deleteCoach } from '@/api/coach'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const searchForm = reactive({ name: '', status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const formData = reactive({ id: null, name: '', username: '', password: '', phone: '', gender: 1, specialty: '', salary: 5000 })

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const getStatusText = (status) => ({ 0: '待审核', 1: '正常', 2: '离职' }[status] || '-')
const getStatusClass = (status) => ({
  0: 'bg-amber-50 text-amber-600',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-slate-100 text-slate-500'
}[status] || '')
const getStatusDotClass = (status) => ({
  0: 'bg-amber-500',
  1: 'bg-emerald-500',
  2: 'bg-slate-400'
}[status] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCoachPage({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.status = null
  pagination.current = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, { id: null, name: '', username: '', password: '', phone: '', gender: 1, specialty: '', salary: 5000 })
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
      await updateCoach(formData)
      ElMessage.success('修改成功')
    } else {
      await createCoach(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleAudit = async (row, status) => {
  const action = status === 1 ? '通过' : '拒绝'
  await ElMessageBox.confirm(`确定${action}该教练的申请吗？`, '提示', { type: 'warning' })
  await auditCoach(row.id, status)
  ElMessage.success('操作成功')
  fetchData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该教练吗？', '提示', { type: 'warning' })
  await deleteCoach(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => fetchData())
</script>
