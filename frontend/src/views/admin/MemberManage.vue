<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">会员管理</h1>
        <p class="text-sm text-slate-500">管理所有会员信息和状态</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-blue-500 transition-colors duration-300 shadow-lg shadow-slate-200">
        <iconify-icon icon="lucide:plus" width="18"></iconify-icon>
        新增会员
      </button>
    </div>

    <!-- 搜索筛选栏 - Spectra 风格 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-input v-model="searchForm.name" placeholder="搜索会员姓名" clearable style="width: 220px">
          <template #prefix>
            <iconify-icon icon="lucide:search" width="16" class="text-slate-400"></iconify-icon>
          </template>
        </el-input>
        <el-input v-model="searchForm.phone" placeholder="手机号" clearable style="width: 160px" />
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="正常" :value="1" />
          <el-option label="冻结" :value="0" />
        </el-select>
        <el-button type="primary" @click="fetchData" class="!rounded-lg">搜索</el-button>
        <el-button @click="resetSearch" class="!rounded-lg">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 - Spectra 风格 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="会员信息" min-width="180">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-slate-900 rounded-full flex items-center justify-center flex-shrink-0">
                <span class="text-white font-medium text-sm">{{ (row.name || 'U').charAt(0).toUpperCase() }}</span>
              </div>
              <div>
                <p class="font-medium text-slate-900">{{ row.name }}</p>
                <p class="text-xs text-slate-500">{{ row.username }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.phone || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" min-width="100">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium" :class="getLevelClass(row.level)">
              <iconify-icon v-if="row.level >= 3" icon="lucide:crown" width="12"></iconify-icon>
              {{ getLevelText(row.level) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balance" label="余额" min-width="100">
          <template #default="{ row }">
            <span class="font-medium text-emerald-600">¥{{ row.balance }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="expireDate" label="到期日" min-width="120">
          <template #default="{ row }">
            <span :class="isExpiringSoon(row.expireDate) ? 'text-rose-500' : 'text-slate-600'">
              {{ row.expireDate || '-' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="90">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 text-sm" :class="row.status === 1 ? 'text-emerald-600' : 'text-slate-400'">
              <span class="w-2 h-2 rounded-full" :class="row.status === 1 ? 'bg-emerald-500' : 'bg-slate-300'"></span>
              {{ row.status === 1 ? '正常' : '冻结' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <button @click="handleEdit(row)" class="p-2 text-slate-400 hover:text-blue-500 hover:bg-blue-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:edit-3" width="16"></iconify-icon>
              </button>
              <button @click="handleToggleStatus(row)" class="p-2 rounded-lg transition-colors" :class="row.status === 1 ? 'text-slate-400 hover:text-amber-500 hover:bg-amber-50' : 'text-slate-400 hover:text-emerald-500 hover:bg-emerald-50'">
                <iconify-icon :icon="row.status === 1 ? 'lucide:pause-circle' : 'lucide:play-circle'" width="16"></iconify-icon>
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
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="sizes, prev, pager, next"
          @change="fetchData"
        />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑会员' : '新增会员'" width="500px" destroy-on-close>
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
        <el-form-item label="会员等级" prop="level">
          <el-select v-model="formData.level" placeholder="请选择等级" class="w-full">
            <el-option label="普通会员" :value="1" />
            <el-option label="银卡会员" :value="2" />
            <el-option label="金卡会员" :value="3" />
            <el-option label="钻石会员" :value="4" />
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
import { getMemberPage, createMember, updateMember, updateMemberStatus, deleteMember } from '@/api/member'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const searchForm = reactive({ name: '', phone: '', status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const formData = reactive({ id: null, name: '', username: '', password: '', phone: '', gender: 1, level: 1 })

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const getLevelText = (level) => {
  const levels = { 1: '普通', 2: '银卡', 3: '金卡', 4: '钻石' }
  return levels[level] || '普通'
}

const getLevelClass = (level) => {
  const classes = {
    1: 'bg-slate-100 text-slate-600',
    2: 'bg-slate-200 text-slate-700',
    3: 'bg-amber-100 text-amber-700',
    4: 'bg-blue-100 text-blue-700'
  }
  return classes[level] || classes[1]
}

const isExpiringSoon = (date) => {
  if (!date) return false
  return dayjs(date).diff(dayjs(), 'day') <= 7
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMemberPage({
      current: pagination.current,
      size: pagination.size,
      ...searchForm
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.phone = ''
  searchForm.status = null
  pagination.current = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, { id: null, name: '', username: '', password: '', phone: '', gender: 1, level: 1 })
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
      await updateMember(formData)
      ElMessage.success('修改成功')
    } else {
      await createMember(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleToggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 0 ? '冻结' : '激活'
  await ElMessageBox.confirm(`确定要${action}该会员吗？`, '提示', { type: 'warning' })
  await updateMemberStatus(row.id, newStatus)
  ElMessage.success(`${action}成功`)
  fetchData()
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该会员吗？', '提示', { type: 'warning' })
  await deleteMember(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => {
  fetchData()
})
</script>
