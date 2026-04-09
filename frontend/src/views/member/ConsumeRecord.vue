<template>
  <div class="space-y-6 w-full">
    <!-- 筛选和导出区域 -->
    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm p-6">
      <div class="flex flex-wrap gap-4 items-center justify-between">
        <div class="flex flex-wrap gap-4 items-center">
          <el-select v-model="searchForm.type" placeholder="消费类型" clearable style="width: 120px">
            <el-option label="充值" :value="1" />
            <el-option label="购课" :value="2" />
            <el-option label="购卡" :value="3" />
            <el-option label="其他" :value="4" />
          </el-select>
          <el-date-picker 
            v-model="dateRange" 
            type="daterange" 
            range-separator="至" 
            start-placeholder="开始日期" 
            end-placeholder="结束日期" 
            value-format="YYYY-MM-DD" 
            :shortcuts="dateRangeShortcuts"
            style="width: 280px" 
          />
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </div>
        <el-button type="success" @click="handleExport" :loading="exportLoading">
          <iconify-icon icon="lucide:download" width="16" class="mr-1"></iconify-icon>
          导出Excel
        </el-button>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="type" label="类型" min-width="100">
          <template #default="{ row }">{{ getTypeText(row.type) }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" min-width="120">
          <template #default="{ row }">
            <span :class="row.amount > 0 ? 'text-emerald-600' : 'text-rose-600'">
              {{ row.amount > 0 ? '+' : '' }}¥{{ row.amount }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" min-width="100">
          <template #default="{ row }">¥{{ row.balanceAfter }}</template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="createTime" label="时间" min-width="180" />
      </el-table>
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyConsumeRecords, exportMyRecords } from '@/api/consume'

const loading = ref(false)
const exportLoading = ref(false)
const tableData = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })
const searchForm = reactive({ type: null })
const dateRange = ref([])

// 日期范围快捷选项
const dateRangeShortcuts = [
  {
    text: '最近一周',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(start.getDate() - 7)
      return [start, end]
    }
  },
  {
    text: '最近一个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      return [start, end]
    }
  },
  {
    text: '最近三个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(start.getMonth() - 3)
      return [start, end]
    }
  },
  {
    text: '本月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setDate(1)
      return [start, end]
    }
  },
  {
    text: '上月',
    value: () => {
      const end = new Date()
      end.setDate(0) // 上月最后一天
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      start.setDate(1)
      return [start, end]
    }
  },
  {
    text: '今年',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setMonth(0)
      start.setDate(1)
      return [start, end]
    }
  }
]

const getTypeText = (t) => ({ 1: '充值', 2: '购课', 3: '购卡', 4: '其他' }[t] || '-')

const fetchData = async () => {
  loading.value = true
  try {
    const params = { 
      current: pagination.current, 
      size: pagination.size,
      type: searchForm.type
    }
    const res = await getMyConsumeRecords(params)
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const resetSearch = () => {
  searchForm.type = null
  dateRange.value = []
  pagination.current = 1
  fetchData()
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const params = {
      type: searchForm.type,
      startDate: dateRange.value?.[0] || '',
      endDate: dateRange.value?.[1] || ''
    }
    const res = await exportMyRecords(params)
    
    // 创建下载链接
    const blob = new Blob([res], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = '我的消费记录.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

onMounted(() => fetchData())
</script>
