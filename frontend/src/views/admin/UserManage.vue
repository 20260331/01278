<template>
  <div class="space-y-6 w-full">
    <div class="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center justify-between">
        <div class="flex flex-wrap gap-4 items-center">
          <el-input v-model="searchForm.username" placeholder="用户名" clearable style="width: 160px" />
          <el-select v-model="searchForm.role" placeholder="角色" clearable style="width: 120px">
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="教练" value="ROLE_COACH" />
            <el-option label="会员" value="ROLE_MEMBER" />
          </el-select>
          <el-button type="primary" @click="fetchData">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
        <el-button type="primary" @click="handleAdd"><Plus class="w-4 h-4 mr-1" /> 新增用户</el-button>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="150" />
        <el-table-column prop="role" label="角色" min-width="120">
          <template #default="{ row }">{{ getRoleText(row.role) }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <span :class="row.status === 1 ? 'text-emerald-600' : 'text-slate-400'">{{ row.status === 1 ? '正常' : '禁用' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleResetPassword(row)">重置密码</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="400px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username"><el-input v-model="formData.username" /></el-form-item>
        <el-form-item label="密码" prop="password"><el-input v-model="formData.password" type="password" :placeholder="isEdit ? '留空则不修改' : '请输入密码'" /></el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="formData.role">
            <el-option label="管理员" value="ROLE_ADMIN" />
            <el-option label="教练" value="ROLE_COACH" />
            <el-option label="会员" value="ROLE_MEMBER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { getUserPage, createUser, updateUser, deleteUser, resetPassword } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const searchForm = reactive({ username: '', role: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const formData = reactive({ id: null, username: '', password: '', role: '', status: 1 })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const getRoleText = (r) => ({ 'ROLE_ADMIN': '管理员', 'ROLE_COACH': '教练', 'ROLE_MEMBER': '会员' }[r] || '-')

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getUserPage({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const resetSearch = () => { Object.assign(searchForm, { username: '', role: '' }); pagination.current = 1; fetchData() }

const handleAdd = () => { isEdit.value = false; Object.assign(formData, { id: null, username: '', password: '', role: '', status: 1 }); dialogVisible.value = true }
const handleEdit = (row) => { isEdit.value = true; Object.assign(formData, { ...row, password: '' }); dialogVisible.value = true }

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    if (isEdit.value) { await updateUser(formData); ElMessage.success('修改成功') }
    else { await createUser(formData); ElMessage.success('新增成功') }
    dialogVisible.value = false; fetchData()
  } finally { submitLoading.value = false }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该用户吗？', '提示', { type: 'warning' })
  await deleteUser(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

const handleResetPassword = async (row) => {
  await ElMessageBox.confirm(`确定要重置用户 "${row.username}" 的密码吗？重置后密码为 123456`, '重置密码', { type: 'warning' })
  await resetPassword(row.id)
  ElMessage.success('密码已重置为 123456')
}

onMounted(() => fetchData())
</script>
