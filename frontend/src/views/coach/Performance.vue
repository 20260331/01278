<template>
  <div class="space-y-6 w-full">
    <!-- 查询条件和统计卡片合并 -->
    <div class="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
      <div class="flex flex-wrap items-center gap-3 mb-6">
        <span class="text-sm text-slate-600">统计区间：</span>
        <el-date-picker 
          v-model="dateRange" 
          type="daterange" 
          range-separator="至" 
          start-placeholder="开始" 
          end-placeholder="结束" 
          value-format="YYYY-MM-DD"
          :shortcuts="performanceShortcuts"
          style="width: 280px"
        />
        <el-button type="primary" @click="fetchData">查询</el-button>
      </div>
      
      <!-- 统计数据 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div class="bg-gradient-to-br from-indigo-50 to-indigo-100 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-indigo-600 mb-1">课程数量</p>
              <p class="text-3xl font-bold text-indigo-700">{{ performance.courseCount || 0 }}</p>
            </div>
            <div class="w-12 h-12 bg-indigo-200 rounded-full flex items-center justify-center">
              <iconify-icon icon="lucide:calendar" width="24" class="text-indigo-600"></iconify-icon>
            </div>
          </div>
        </div>
        <div class="bg-gradient-to-br from-emerald-50 to-emerald-100 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-emerald-600 mb-1">学员人次</p>
              <p class="text-3xl font-bold text-emerald-700">{{ performance.totalStudents || 0 }}</p>
            </div>
            <div class="w-12 h-12 bg-emerald-200 rounded-full flex items-center justify-center">
              <iconify-icon icon="lucide:users" width="24" class="text-emerald-600"></iconify-icon>
            </div>
          </div>
        </div>
        <div class="bg-gradient-to-br from-amber-50 to-amber-100 rounded-xl p-5">
          <div class="flex items-center justify-between">
            <div>
              <p class="text-sm text-amber-600 mb-1">课程收入</p>
              <p class="text-3xl font-bold text-amber-700">¥{{ performance.totalIncome || 0 }}</p>
            </div>
            <div class="w-12 h-12 bg-amber-200 rounded-full flex items-center justify-center">
              <iconify-icon icon="lucide:wallet" width="24" class="text-amber-600"></iconify-icon>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 提成收入 -->
    <div class="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
      <h3 class="text-lg font-semibold text-slate-900 mb-4">收入明细</h3>
      <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div class="p-4 bg-slate-50 rounded-xl">
          <p class="text-sm text-slate-500 mb-1">基本薪资</p>
          <p class="text-2xl font-bold text-slate-700">¥{{ performance.baseSalary || 0 }}</p>
        </div>
        <div class="p-4 bg-rose-50 rounded-xl">
          <p class="text-sm text-rose-500 mb-1">提成比例</p>
          <p class="text-2xl font-bold text-rose-600">{{ performance.commissionRate || '10%' }}</p>
        </div>
        <div class="p-4 bg-purple-50 rounded-xl">
          <p class="text-sm text-purple-500 mb-1">业绩提成</p>
          <p class="text-2xl font-bold text-purple-600">¥{{ performance.commission || 0 }}</p>
        </div>
        <div class="p-4 bg-gradient-to-br from-emerald-100 to-emerald-200 rounded-xl">
          <p class="text-sm text-emerald-600 mb-1">预计总收入</p>
          <p class="text-2xl font-bold text-emerald-700">¥{{ performance.expectedIncome || 0 }}</p>
        </div>
      </div>
    </div>

    <!-- 业绩说明 -->
    <div class="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
      <h3 class="text-base font-semibold text-slate-900 mb-3">业绩说明</h3>
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm text-slate-600">
        <div class="flex items-start gap-2">
          <span class="text-indigo-500">•</span>
          <span><strong>课程数量</strong>：统计期间您负责的课程总数</span>
        </div>
        <div class="flex items-start gap-2">
          <span class="text-emerald-500">•</span>
          <span><strong>学员人次</strong>：所有课程的预约总人数</span>
        </div>
        <div class="flex items-start gap-2">
          <span class="text-amber-500">•</span>
          <span><strong>课程收入</strong>：课程价格 × 预约人数的总和</span>
        </div>
        <div class="flex items-start gap-2">
          <span class="text-purple-500">•</span>
          <span><strong>业绩提成</strong>：课程收入 × 提成比例（默认10%）</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCoachPerformance } from '@/api/statistics'
import dayjs from 'dayjs'

const dateRange = ref([
  dayjs().startOf('month').format('YYYY-MM-DD'),
  dayjs().endOf('month').format('YYYY-MM-DD')
])
const performance = ref({})

// 业绩查询快捷选项
const performanceShortcuts = [
  {
    text: '本周',
    value: () => {
      const end = new Date()
      const start = new Date()
      const day = start.getDay()
      start.setDate(start.getDate() - (day === 0 ? 6 : day - 1))
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
      end.setDate(0)
      const start = new Date()
      start.setMonth(start.getMonth() - 1)
      start.setDate(1)
      return [start, end]
    }
  },
  {
    text: '本季度',
    value: () => {
      const end = new Date()
      const start = new Date()
      const month = start.getMonth()
      start.setMonth(month - (month % 3))
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

const fetchData = async () => {
  if (!dateRange.value || dateRange.value.length < 2) return
  const res = await getCoachPerformance(dateRange.value[0], dateRange.value[1])
  performance.value = res.data
}

onMounted(() => fetchData())
</script>

