<template>
  <div class="space-y-6 w-full">

    <div class="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="待审核" :value="0" />
          <el-option label="已通过" :value="1" />
          <el-option label="已拒绝" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="coachName" label="教练" min-width="90" />
        <el-table-column prop="type" label="类型" min-width="70">
          <template #default="{ row }">{{ row.type === 1 ? '请假' : '调课' }}</template>
        </el-table-column>
        <el-table-column prop="courseName" label="关联课程" min-width="120" />
        <el-table-column prop="reason" label="原因" min-width="180" />
        <el-table-column prop="startTime" label="开始时间" min-width="160" />
        <el-table-column prop="status" label="状态" min-width="90">
          <template #default="{ row }">
            <span class="px-2 py-1 rounded-full text-xs font-medium" :class="getStatusClass(row.status)">{{ getStatusText(row.status) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 0">
              <el-button link type="success" size="small" @click="handleAudit(row, 1)">通过</el-button>
              <el-button link type="danger" size="small" @click="handleAudit(row, 2)">拒绝</el-button>
            </template>
            <span v-else class="text-slate-400 text-sm">已处理</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getLeavePage, auditLeave } from '@/api/course'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const getStatusText = (s) => ({ 0: '待审核', 1: '已通过', 2: '已拒绝' }[s] || '-')
const getStatusClass = (s) => ({ 0: 'bg-amber-50 text-amber-600', 1: 'bg-emerald-50 text-emerald-600', 2: 'bg-rose-50 text-rose-600' }[s] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLeavePage({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.status = null; pagination.current = 1; fetchData() }

const handleAudit = async (row, status) => {
  const action = status === 1 ? '通过' : '拒绝'
  const { value } = await ElMessageBox.prompt('请输入审核备注（可选）', `确认${action}`, { confirmButtonText: '确定', cancelButtonText: '取消' })
  await auditLeave(row.id, status, value || '')
  ElMessage.success('操作成功')
  fetchData()
}

onMounted(() => fetchData())
</script>
