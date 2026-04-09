<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">薪资结算</h1>
        <p class="text-sm text-slate-500">教练薪资计算与统计</p>
      </div>
      <div class="flex items-center gap-3">
        <el-date-picker
          v-model="selectedMonth"
          type="month"
          placeholder="选择月份"
          value-format="YYYY-MM"
          :shortcuts="monthShortcuts"
          @change="fetchData"
        />
      </div>
    </div>

    <!-- 薪资汇总 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <h3 class="text-lg font-medium text-slate-900 mb-4">{{ selectedMonth }} 薪资汇总</h3>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4" v-loading="summaryLoading">
        <div class="p-4 bg-blue-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-blue-600">{{ summary.coachCount || 0 }}</p>
          <p class="text-sm text-blue-500 mt-1">教练人数</p>
        </div>
        <div class="p-4 bg-purple-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-purple-600">{{ summary.totalCourses || 0 }}</p>
          <p class="text-sm text-purple-500 mt-1">课程总数</p>
        </div>
        <div class="p-4 bg-emerald-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-emerald-600">{{ summary.totalStudents || 0 }}</p>
          <p class="text-sm text-emerald-500 mt-1">学员人次</p>
        </div>
        <div class="p-4 bg-slate-100 rounded-xl text-center">
          <p class="text-2xl font-bold text-slate-700">¥{{ formatMoney(summary.totalBaseSalary) }}</p>
          <p class="text-sm text-slate-500 mt-1">基本薪资</p>
        </div>
        <div class="p-4 bg-amber-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-amber-600">¥{{ formatMoney(summary.totalCommission) }}</p>
          <p class="text-sm text-amber-500 mt-1">提成合计</p>
        </div>
        <div class="p-4 bg-gradient-to-br from-rose-100 to-rose-200 rounded-xl text-center">
          <p class="text-2xl font-bold text-rose-700">¥{{ formatMoney(summary.totalSalary) }}</p>
          <p class="text-sm text-rose-600 mt-1">应发总额</p>
        </div>
      </div>
    </div>

    <!-- 教练薪资明细 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <el-table :data="salaryList" v-loading="listLoading" style="width: 100%">
        <el-table-column prop="coachName" label="教练" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 bg-emerald-100 rounded-full flex items-center justify-center">
                <span class="text-emerald-600 font-medium text-sm">{{ (row.coachName || 'C').charAt(0) }}</span>
              </div>
              <span class="font-medium text-slate-900">{{ row.coachName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="baseSalary" label="基本薪资" min-width="120">
          <template #default="{ row }">
            <span class="text-slate-700">¥{{ formatMoney(row.baseSalary) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="courseCount" label="课程数" min-width="80">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.courseCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="studentCount" label="学员人次" min-width="90">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.studentCount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="courseIncome" label="课程收入" min-width="120">
          <template #default="{ row }">
            <span class="text-slate-700">¥{{ formatMoney(row.courseIncome) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="commissionRate" label="提成比例" min-width="90">
          <template #default="{ row }">
            <span class="text-amber-600 font-medium">{{ row.commissionRate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="commission" label="提成金额" min-width="120">
          <template #default="{ row }">
            <span class="text-amber-600 font-medium">¥{{ formatMoney(row.commission) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="totalSalary" label="应发薪资" min-width="130">
          <template #default="{ row }">
            <span class="text-lg font-bold text-emerald-600">¥{{ formatMoney(row.totalSalary) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-between items-center border-t border-slate-100">
        <p class="text-sm text-slate-500">共 {{ salaryList.length }} 位教练</p>
      </div>
    </div>

    <!-- 说明 -->
    <div class="bg-blue-50 rounded-2xl border border-blue-200 p-6">
      <div class="flex items-start gap-3">
        <iconify-icon icon="lucide:info" width="24" class="text-blue-600 flex-shrink-0 mt-0.5"></iconify-icon>
        <div>
          <h4 class="font-medium text-blue-800 mb-2">薪资计算说明</h4>
          <ul class="text-sm text-blue-700 space-y-1">
            <li>1. <strong>基本薪资</strong>：教练档案中设定的月基本工资</li>
            <li>2. <strong>课程收入</strong>：当月所有课程的 (课程价格 × 实际报名人数) 总和</li>
            <li>3. <strong>提成金额</strong>：课程收入 × 提成比例（默认10%）</li>
            <li>4. <strong>应发薪资</strong>：基本薪资 + 提成金额</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getSalaryList, getSalarySummary } from '@/api/salary'
import dayjs from 'dayjs'

const selectedMonth = ref(dayjs().format('YYYY-MM'))
const salaryList = ref([])
const summary = ref({})
const listLoading = ref(false)
const summaryLoading = ref(false)

// 月份快捷选项
const monthShortcuts = [
  {
    text: '本月',
    value: new Date()
  },
  {
    text: '上月',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() - 1)
      return date
    }
  },
  {
    text: '上上月',
    value: () => {
      const date = new Date()
      date.setMonth(date.getMonth() - 2)
      return date
    }
  },
  {
    text: '去年同期',
    value: () => {
      const date = new Date()
      date.setFullYear(date.getFullYear() - 1)
      return date
    }
  }
]

const formatMoney = (value) => {
  if (!value) return '0.00'
  return Number(value).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const fetchData = async () => {
  listLoading.value = true
  summaryLoading.value = true
  
  try {
    const [listRes, summaryRes] = await Promise.all([
      getSalaryList(selectedMonth.value),
      getSalarySummary(selectedMonth.value)
    ])
    salaryList.value = listRes.data
    summary.value = summaryRes.data
  } finally {
    listLoading.value = false
    summaryLoading.value = false
  }
}

onMounted(() => fetchData())
</script>
