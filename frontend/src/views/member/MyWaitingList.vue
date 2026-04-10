<template>
  <div class="space-y-6 w-full">
    <div>
      <h1 class="text-2xl font-serif text-slate-900 mb-1">我的候补</h1>
      <p class="text-sm text-slate-500">查看和管理您的课程候补队列</p>
    </div>

    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">{{ row.courseName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="queueOrder" label="队列位置" width="100">
          <template #default="{ row }">
            <span class="text-amber-600 font-medium">第 {{ row.queueOrder }} 位</span>
          </template>
        </el-table-column>
        <el-table-column prop="courseStartTime" label="上课时间" min-width="170">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.courseStartTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="候补时间" min-width="170">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="danger" size="small" @click="handleCancel(row)">取消候补</el-button>
            <span v-else class="text-slate-400 text-sm">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="tableData.length === 0 && !loading" class="py-12 text-center">
        <iconify-icon icon="lucide:list" width="40" class="text-slate-300 mb-3"></iconify-icon>
        <p class="text-slate-400 mb-4">暂无候补记录</p>
      </div>

      <div v-if="tableData.length > 0" class="p-4 flex justify-between items-center border-t border-slate-100">
        <p class="text-sm text-slate-500">共 {{ pagination.total }} 条记录</p>
        <el-pagination 
          v-model:current-page="pagination.current" 
          v-model:page-size="pagination.size" 
          :total="pagination.total" 
          layout="sizes, prev, pager, next" 
          @change="fetchData" 
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyWaitingList, cancelWaitingList } from '@/api/waitingList'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })

const getStatusText = (s) => ({ 0: '排队中', 1: '候补成功', 2: '已取消', 3: '已过期' }[s] || '-')
const getStatusClass = (s) => ({
  0: 'bg-amber-50 text-amber-600',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-slate-100 text-slate-500',
  3: 'bg-rose-50 text-rose-600'
}[s] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyWaitingList({ current: pagination.current, size: pagination.size })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定要取消该候补吗？', '取消候补', { type: 'warning' })
  await cancelWaitingList(row.id)
  ElMessage.success('取消成功')
  fetchData()
}

onMounted(() => fetchData())
</script>
