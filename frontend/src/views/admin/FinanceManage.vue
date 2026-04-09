<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div>
      <h1 class="text-2xl font-serif text-slate-900 mb-1">财务管理</h1>
      <p class="text-sm text-slate-500">查看收支明细和会员充值</p>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
      <div class="group bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-xl hover:shadow-slate-200/50 hover:-translate-y-1 transition-all duration-300">
        <div class="flex items-center justify-between mb-4">
          <div class="w-12 h-12 bg-emerald-50 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
            <iconify-icon icon="lucide:trending-up" width="24" class="text-emerald-500"></iconify-icon>
          </div>
          <span class="text-xs font-medium text-slate-400 uppercase tracking-wider">本月</span>
        </div>
        <p class="text-sm text-slate-500 mb-1">收入</p>
        <p class="text-3xl font-serif font-medium text-emerald-600">¥{{ statistics.income || 0 }}</p>
      </div>
      <div class="group bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-xl hover:shadow-slate-200/50 hover:-translate-y-1 transition-all duration-300">
        <div class="flex items-center justify-between mb-4">
          <div class="w-12 h-12 bg-rose-50 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
            <iconify-icon icon="lucide:trending-down" width="24" class="text-rose-500"></iconify-icon>
          </div>
          <span class="text-xs font-medium text-slate-400 uppercase tracking-wider">本月</span>
        </div>
        <p class="text-sm text-slate-500 mb-1">支出</p>
        <p class="text-3xl font-serif font-medium text-rose-600">¥{{ statistics.expense || 0 }}</p>
      </div>
      <div class="group bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-xl hover:shadow-slate-200/50 hover:-translate-y-1 transition-all duration-300">
        <div class="flex items-center justify-between mb-4">
          <div class="w-12 h-12 bg-blue-50 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform">
            <iconify-icon icon="lucide:wallet" width="24" class="text-blue-500"></iconify-icon>
          </div>
          <span class="text-xs font-medium text-slate-400 uppercase tracking-wider">本月</span>
        </div>
        <p class="text-sm text-slate-500 mb-1">利润</p>
        <p class="text-3xl font-serif font-medium text-blue-600">¥{{ statistics.profit || 0 }}</p>
      </div>
    </div>

    <!-- 会员充值 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <h3 class="text-xl font-serif text-slate-900 mb-6">会员充值</h3>
      <el-form :inline="true" :model="rechargeForm" @submit.prevent="handleRecharge" class="flex flex-wrap gap-4 items-end">
        <el-form-item label="会员" class="!mb-0">
          <el-select v-model="rechargeForm.memberId" placeholder="选择会员" filterable style="width: 160px">
            <el-option v-for="m in memberList" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" class="!mb-0">
          <el-input-number v-model="rechargeForm.amount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="备注" class="!mb-0">
          <el-input v-model="rechargeForm.description" placeholder="充值备注" style="width: 160px" />
        </el-form-item>
        <el-form-item class="!mb-0">
          <el-button type="primary" @click="handleRecharge" class="!rounded-lg">确认充值</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 消费记录 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden w-full">
      <div class="p-6 border-b border-slate-100">
        <h3 class="text-xl font-serif text-slate-900">消费记录</h3>
      </div>
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="memberName" label="会员" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 bg-slate-100 rounded-full flex items-center justify-center">
                <span class="text-slate-600 font-medium text-xs">{{ (row.memberName || 'U').charAt(0) }}</span>
              </div>
              <span class="text-slate-700">{{ row.memberName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" min-width="100">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getTypeClass(row.type)">
              <iconify-icon :icon="getTypeIcon(row.type)" width="12"></iconify-icon>
              {{ getTypeText(row.type) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="金额" min-width="120">
          <template #default="{ row }">
            <span class="font-medium" :class="row.amount > 0 ? 'text-emerald-600' : 'text-rose-600'">
              {{ row.amount > 0 ? '+' : '' }}¥{{ Math.abs(row.amount) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="balanceAfter" label="余额" min-width="100">
          <template #default="{ row }">
            <span class="text-slate-600">¥{{ row.balanceAfter }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="200">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.description || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" min-width="170">
          <template #default="{ row }">
            <span class="text-slate-500">{{ row.createTime }}</span>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-between items-center border-t border-slate-100">
        <p class="text-sm text-slate-500">共 {{ pagination.total }} 条记录</p>
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getConsumePage, recharge, getStatistics } from '@/api/consume'
import { getMemberPage } from '@/api/member'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const memberList = ref([])
const statistics = ref({})

const pagination = reactive({ current: 1, size: 10, total: 0 })
const rechargeForm = reactive({ memberId: null, amount: 0, description: '' })

const getTypeText = (t) => ({ 1: '充值', 2: '购课', 3: '购卡', 4: '其他' }[t] || '-')
const getTypeClass = (t) => ({
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-blue-50 text-blue-600',
  3: 'bg-amber-50 text-amber-600',
  4: 'bg-slate-100 text-slate-600'
}[t] || 'bg-slate-100 text-slate-600')
const getTypeIcon = (t) => ({
  1: 'lucide:plus-circle',
  2: 'lucide:calendar',
  3: 'lucide:credit-card',
  4: 'lucide:more-horizontal'
}[t] || 'lucide:circle')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getConsumePage({ current: pagination.current, size: pagination.size })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const fetchStatistics = async () => {
  const startDate = dayjs().startOf('month').format('YYYY-MM-DD')
  const endDate = dayjs().endOf('month').format('YYYY-MM-DD')
  const res = await getStatistics(startDate, endDate)
  statistics.value = res.data
}

const fetchMembers = async () => {
  const res = await getMemberPage({ current: 1, size: 1000 })
  memberList.value = res.data.records
}

const handleRecharge = async () => {
  if (!rechargeForm.memberId || !rechargeForm.amount) {
    ElMessage.warning('请选择会员并输入金额')
    return
  }
  await recharge(rechargeForm)
  ElMessage.success('充值成功')
  rechargeForm.memberId = null
  rechargeForm.amount = 0
  rechargeForm.description = ''
  fetchData()
  fetchStatistics()
}

onMounted(() => {
  fetchData()
  fetchStatistics()
  fetchMembers()
})
</script>
