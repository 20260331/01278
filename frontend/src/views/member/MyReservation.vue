<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div>
      <h1 class="text-2xl font-serif text-slate-900 mb-1">我的预约</h1>
      <p class="text-sm text-slate-500">查看和管理您的课程预约</p>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-select v-model="searchForm.status" placeholder="预约状态" clearable style="width: 140px">
          <el-option label="已预约" :value="0" />
          <el-option label="已签到" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchData" class="!rounded-lg">筛选</el-button>
        <el-button @click="resetSearch" class="!rounded-lg">重置</el-button>
      </div>
    </div>

    <!-- 预约列表 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">{{ row.courseName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="coachName" label="教练" min-width="100">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.coachName || '待定' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="courseStartTime" label="上课时间" min-width="170">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.courseStartTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getStatusClass(row.status)">
              <span class="w-1.5 h-1.5 rounded-full" :class="getStatusDotClass(row.status)"></span>
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="预约时间" min-width="170">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" link type="danger" size="small" @click="handleCancel(row)">取消</el-button>
            <span v-else class="text-slate-400 text-sm">-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="tableData.length === 0 && !loading" class="py-12 text-center">
        <iconify-icon icon="lucide:calendar-x" width="40" class="text-slate-300 mb-3"></iconify-icon>
        <p class="text-slate-400 mb-4">暂无预约记录</p>
        <router-link to="/course-reserve" class="text-blue-500 hover:text-blue-600 text-sm">去预约课程</router-link>
      </div>

      <!-- 分页 -->
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
import { getMyReservations, cancelReservation } from '@/api/reservation'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const getStatusText = (s) => ({ 0: '已预约', 1: '已签到', 2: '已取消', 3: '缺席' }[s] || '-')
const getStatusClass = (s) => ({
  0: 'bg-blue-50 text-blue-600',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-slate-100 text-slate-500',
  3: 'bg-rose-50 text-rose-600'
}[s] || '')
const getStatusDotClass = (s) => ({
  0: 'bg-blue-500',
  1: 'bg-emerald-500',
  2: 'bg-slate-400',
  3: 'bg-rose-500'
}[s] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyReservations({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.status = null
  pagination.current = 1
  fetchData()
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定要取消该预约吗？', '取消预约', { type: 'warning' })
  await cancelReservation(row.id)
  ElMessage.success('取消成功')
  fetchData()
}

onMounted(() => fetchData())
</script>
