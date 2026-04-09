<template>
  <div class="space-y-6 w-full">
    <div class="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
      <p class="text-slate-500">查看参加您课程的会员信息，并可发送健身建议</p>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden w-full">
      <el-table :data="memberList" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="150" />
        <el-table-column prop="level" label="会员等级" min-width="120">
          <template #default="{ row }">{{ getLevelText(row.level) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewFitness(row)">健身记录</el-button>
            <el-button link type="success" size="small" @click="openAdviceDialog(row)">发送建议</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <!-- 健身记录弹窗 -->
    <el-dialog v-model="fitnessDialogVisible" :title="`${currentMember?.name}的健身记录`" width="800px">
      <el-table :data="fitnessRecords" stripe max-height="400">
        <el-table-column prop="recordDate" label="日期" width="110" />
        <el-table-column prop="weight" label="体重(kg)" width="90" />
        <el-table-column prop="duration" label="时长(分钟)" width="90" />
        <el-table-column prop="calories" label="卡路里" width="80" />
        <el-table-column prop="content" label="训练内容" min-width="150" />
        <el-table-column label="建议" width="100">
          <template #default="{ row }">
            <el-button v-if="!row.coachAdvice" link type="primary" size="small" @click="openSingleAdvice(row)">发送</el-button>
            <el-tooltip v-else :content="row.coachAdvice" placement="top">
              <span class="text-emerald-500 text-sm cursor-pointer">已发送</span>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 发送建议弹窗 -->
    <el-dialog v-model="adviceDialogVisible" title="发送健身建议" width="500px">
      <div class="mb-4">
        <p class="text-slate-600">会员：<span class="font-medium text-slate-900">{{ currentMember?.name }}</span></p>
        <p v-if="currentRecord" class="text-slate-600 mt-2">
          记录日期：<span class="font-medium">{{ currentRecord.recordDate }}</span>
          <span class="ml-4">训练内容：{{ currentRecord.content || '-' }}</span>
        </p>
      </div>
      <el-form>
        <el-form-item label="健身建议">
          <el-input v-model="adviceContent" type="textarea" :rows="4" placeholder="请输入针对该会员的健身建议..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adviceDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdvice" :loading="adviceLoading">发送建议</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMemberPage } from '@/api/member'
import { getFitnessPage, sendAdvice } from '@/api/fitness'

const loading = ref(false)
const memberList = ref([])
const fitnessDialogVisible = ref(false)
const fitnessRecords = ref([])
const currentMember = ref(null)
const adviceDialogVisible = ref(false)
const adviceContent = ref('')
const adviceLoading = ref(false)
const currentRecord = ref(null)

const pagination = reactive({ current: 1, size: 10, total: 0 })

const getLevelText = (l) => ({ 1: '普通', 2: '银卡', 3: '金卡', 4: '钻石' }[l] || '普通')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMemberPage({ current: pagination.current, size: pagination.size, status: 1 })
    memberList.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const viewFitness = async (row) => {
  currentMember.value = row
  const res = await getFitnessPage({ current: 1, size: 50, memberId: row.id })
  fitnessRecords.value = res.data.records
  fitnessDialogVisible.value = true
}

const openAdviceDialog = async (row) => {
  currentMember.value = row
  currentRecord.value = null
  adviceContent.value = ''
  // 获取最新的健身记录
  const res = await getFitnessPage({ current: 1, size: 1, memberId: row.id })
  if (res.data.records.length > 0) {
    currentRecord.value = res.data.records[0]
  }
  adviceDialogVisible.value = true
}

const openSingleAdvice = (record) => {
  currentRecord.value = record
  adviceContent.value = ''
  adviceDialogVisible.value = true
}

const submitAdvice = async () => {
  if (!adviceContent.value.trim()) {
    ElMessage.warning('请输入建议内容')
    return
  }
  if (!currentRecord.value) {
    ElMessage.warning('该会员暂无健身记录')
    return
  }
  
  adviceLoading.value = true
  try {
    await sendAdvice(currentRecord.value.id, adviceContent.value)
    ElMessage.success('建议发送成功')
    adviceDialogVisible.value = false
    
    // 刷新健身记录
    if (fitnessDialogVisible.value && currentMember.value) {
      const res = await getFitnessPage({ current: 1, size: 50, memberId: currentMember.value.id })
      fitnessRecords.value = res.data.records
    }
  } finally {
    adviceLoading.value = false
  }
}

onMounted(() => fetchData())
</script>
