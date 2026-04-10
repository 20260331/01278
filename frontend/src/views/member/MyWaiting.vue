<template>
  <div class="space-y-6 w-full">
    <div>
      <h1 class="text-2xl font-serif text-slate-900 mb-1">我的候补</h1>
      <p class="text-sm text-slate-500">查看和管理您的课程候补队列</p>
    </div>

    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-select v-model="searchForm.status" placeholder="候补状态" clearable style="width: 140px">
          <el-option label="排队中" :value="0" />
          <el-option label="已成功补位" :value="1" />
          <el-option label="已取消" :value="2" />
          <el-option label="补位失败" :value="3" />
        </el-select>
        <el-button type="primary" @click="fetchData" class="!rounded-lg">筛选</el-button>
        <el-button @click="resetSearch" class="!rounded-lg">重置</el-button>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <el-table :data="filteredData" v-loading="loading" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">{{ row.courseName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="候补顺序" min-width="100">
          <template #default="{ row }">
            <span v-if="row.status === 0" class="inline-flex items-center justify-center w-8 h-8 rounded-full bg-amber-100 text-amber-700 font-bold">
              {{ row.queueOrder }}
            </span>
            <span v-else class="text-slate-400">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="courseStartTime" label="课程时间" min-width="170">
          <template #default="{ row }">
            <span class="text-slate-600">{{ formatTime(row.courseStartTime) }}</span>
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
        <el-table-column prop="createTime" label="候补时间" min-width="170">
          <template #default="{ row }">
            <span class="text-slate-500">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="danger" size="small" @click="handleCancel(row)">取消候补</el-button>
            <router-link v-else-if="row.status === 1" to="/my-reservation" class="text-blue-500 hover:text-blue-600 text-xs">查看预约</router-link>
            <span v-else class="text-slate-400 text-sm">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="filteredData.length === 0 && !loading" class="py-12 text-center">
        <iconify-icon icon="lucide:list" width="40" class="text-slate-300 mb-3"></iconify-icon>
        <p class="text-slate-400 mb-4">暂无候补记录</p>
        <router-link to="/course-reserve" class="text-blue-500 hover:text-blue-600 text-sm">去浏览课程</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import waitingQueueApi from '@/api/waitingQueue'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ status: null })

const filteredData = computed(() => {
  if (searchForm.status === null) {
    return tableData.value
  }
  return tableData.value.filter(item => item.status === searchForm.status)
})

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'

const getStatusText = (s) => ({ 
  0: '排队中', 
  1: '已成功补位', 
  2: '已取消', 
  3: '补位失败' 
}[s] || '-')

const getStatusClass = (s) => ({
  0: 'bg-amber-50 text-amber-600',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-slate-100 text-slate-500',
  3: 'bg-rose-50 text-rose-600'
}[s] || '')

const getStatusDotClass = (s) => ({
  0: 'bg-amber-500',
  1: 'bg-emerald-500',
  2: 'bg-slate-400',
  3: 'bg-rose-500'
}[s] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await waitingQueueApi.getMyWaitingQueue()
    tableData.value = res.data || []
  } finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.status = null
  fetchData()
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定要取消该课程的候补吗？', '取消候补', { type: 'warning' })
  await waitingQueueApi.cancel(row.id)
  ElMessage.success('取消成功')
  fetchData()
}

onMounted(() => fetchData())
</script>