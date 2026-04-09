<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">我的课程</h1>
        <p class="text-sm text-slate-500">管理您的授课安排</p>
      </div>
      <button @click="handleLeave" class="inline-flex items-center gap-2 px-5 py-2.5 bg-white border border-slate-200 text-slate-700 rounded-lg text-sm font-medium hover:border-blue-500 hover:text-blue-500 transition-colors">
        <iconify-icon icon="lucide:calendar-off" width="18"></iconify-icon>
        申请请假
      </button>
    </div>

    <!-- 课程列表 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="课程信息" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0" :class="getTypeBgClass(row.type)">
                <iconify-icon :icon="getTypeIcon(row.type)" width="24" :class="getTypeIconClass(row.type)"></iconify-icon>
              </div>
              <div>
                <p class="font-medium text-slate-900">{{ row.name }}</p>
                <p class="text-xs text-slate-500">{{ row.type }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="报名情况" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <div class="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden max-w-[80px]">
                <div class="h-full rounded-full transition-all" :class="getCapacityColor(row)" :style="{ width: `${(row.currentCount / row.maxCapacity) * 100}%` }"></div>
              </div>
              <span class="text-xs text-slate-500 whitespace-nowrap">{{ row.currentCount }}/{{ row.maxCapacity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="开始时间" min-width="160">
          <template #default="{ row }">
            <div class="flex items-center gap-2 text-slate-600">
              <iconify-icon icon="lucide:calendar" width="14" class="text-slate-400"></iconify-icon>
              {{ row.startTime }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="100">
          <template #default="{ row }">
            <div class="flex items-center gap-2 text-slate-600">
              <iconify-icon icon="lucide:map-pin" width="14" class="text-slate-400"></iconify-icon>
              {{ row.location || '-' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="90">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getStatusClass(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="viewReservations(row)">查看学员</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-between items-center border-t border-slate-100">
        <p class="text-sm text-slate-500">共 {{ pagination.total }} 条记录</p>
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <!-- 请假弹窗 -->
    <el-dialog v-model="leaveDialogVisible" title="申请请假/调课" width="500px" destroy-on-close>
      <el-form ref="leaveFormRef" :model="leaveForm" :rules="leaveRules" label-width="80px">
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="leaveForm.type">
            <el-radio :label="1">请假</el-radio>
            <el-radio :label="2">调课</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="leaveForm.type === 2" label="关联课程" prop="courseId">
          <el-select v-model="leaveForm.courseId" placeholder="选择课程" class="w-full">
            <el-option v-for="c in tableData" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime">
          <el-date-picker 
            v-model="leaveForm.startTime" 
            type="datetime" 
            placeholder="选择开始时间"
            format="YYYY-MM-DD HH:mm" 
            value-format="YYYY-MM-DD HH:mm:ss" 
            :shortcuts="leaveShortcuts"
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime">
          <el-date-picker 
            v-model="leaveForm.endTime" 
            type="datetime" 
            placeholder="选择结束时间"
            format="YYYY-MM-DD HH:mm" 
            value-format="YYYY-MM-DD HH:mm:ss" 
            :shortcuts="leaveEndShortcuts"
            style="width: 100%" 
          />
        </el-form-item>
        <el-form-item label="原因" prop="reason">
          <el-input v-model="leaveForm.reason" type="textarea" :rows="3" placeholder="请输入请假原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="leaveDialogVisible = false" class="!rounded-lg">取消</el-button>
        <el-button type="primary" @click="submitLeaveRequest" :loading="submitLoading" class="!rounded-lg">提交</el-button>
      </template>
    </el-dialog>

    <!-- 预约学员弹窗 -->
    <el-dialog v-model="reservationDialogVisible" title="预约学员" width="600px">
      <div v-if="reservationList.length > 0" class="space-y-3">
        <div 
          v-for="item in reservationList" 
          :key="item.id" 
          class="flex items-center justify-between p-4 bg-slate-50 rounded-xl"
        >
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-slate-200 rounded-full flex items-center justify-center">
              <span class="text-slate-600 font-medium text-sm">{{ (item.memberName || 'U').charAt(0) }}</span>
            </div>
            <div>
              <p class="font-medium text-slate-900">{{ item.memberName }}</p>
              <p class="text-xs text-slate-500">预约时间：{{ item.createTime }}</p>
            </div>
          </div>
          <div class="flex items-center gap-3">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getResStatusClass(item.status)">
              {{ getResStatusText(item.status) }}
            </span>
            <button 
              v-if="item.status === 0" 
              @click="handleCheckin(item)"
              class="px-3 py-1.5 bg-emerald-500 text-white text-sm rounded-lg hover:bg-emerald-600 transition-colors"
            >
              签到
            </button>
          </div>
        </div>
      </div>
      <div v-else class="text-center py-8">
        <iconify-icon icon="lucide:users" width="40" class="text-slate-300 mb-3"></iconify-icon>
        <p class="text-slate-400">暂无学员预约</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCoursePage, submitLeave } from '@/api/course'
import { getReservationPage, checkinReservation } from '@/api/reservation'
import { getCurrentCoach } from '@/api/coach'

const loading = ref(false)
const tableData = ref([])
const leaveDialogVisible = ref(false)
const reservationDialogVisible = ref(false)
const submitLoading = ref(false)
const leaveFormRef = ref(null)
const reservationList = ref([])
const currentCourse = ref(null)
const coachId = ref(null)

const pagination = reactive({ current: 1, size: 10, total: 0 })
const leaveForm = reactive({ type: 1, courseId: null, startTime: '', endTime: '', reason: '' })
const leaveRules = {
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
}

// 请假开始时间快捷选项
const leaveShortcuts = [
  {
    text: '今天',
    value: () => {
      const date = new Date()
      date.setHours(9, 0, 0, 0)
      return date
    }
  },
  {
    text: '明天',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      date.setHours(9, 0, 0, 0)
      return date
    }
  },
  {
    text: '后天',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 2)
      date.setHours(9, 0, 0, 0)
      return date
    }
  },
  {
    text: '下周一',
    value: () => {
      const date = new Date()
      const day = date.getDay()
      const diff = day === 0 ? 1 : 8 - day
      date.setDate(date.getDate() + diff)
      date.setHours(9, 0, 0, 0)
      return date
    }
  }
]

// 请假结束时间快捷选项
const leaveEndShortcuts = [
  {
    text: '今天下班',
    value: () => {
      const date = new Date()
      date.setHours(21, 0, 0, 0)
      return date
    }
  },
  {
    text: '明天下班',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      date.setHours(21, 0, 0, 0)
      return date
    }
  },
  {
    text: '本周末',
    value: () => {
      const date = new Date()
      const day = date.getDay()
      const diff = day === 0 ? 0 : 7 - day
      date.setDate(date.getDate() + diff)
      date.setHours(21, 0, 0, 0)
      return date
    }
  },
  {
    text: '下周末',
    value: () => {
      const date = new Date()
      const day = date.getDay()
      const diff = day === 0 ? 7 : 14 - day
      date.setDate(date.getDate() + diff)
      date.setHours(21, 0, 0, 0)
      return date
    }
  }
]

const getTypeIcon = (type) => ({
  '瑜伽': 'lucide:flower-2',
  '有氧': 'lucide:heart-pulse',
  '器械': 'lucide:dumbbell',
  '舞蹈': 'lucide:music'
}[type] || 'lucide:activity')

const getTypeBgClass = (type) => ({
  '瑜伽': 'bg-purple-50',
  '有氧': 'bg-rose-50',
  '器械': 'bg-blue-50',
  '舞蹈': 'bg-amber-50'
}[type] || 'bg-slate-50')

const getTypeIconClass = (type) => ({
  '瑜伽': 'text-purple-500',
  '有氧': 'text-rose-500',
  '器械': 'text-blue-500',
  '舞蹈': 'text-amber-500'
}[type] || 'text-slate-500')

const getCapacityColor = (row) => {
  const ratio = row.currentCount / row.maxCapacity
  if (ratio >= 1) return 'bg-rose-500'
  if (ratio >= 0.8) return 'bg-amber-500'
  return 'bg-emerald-500'
}

const getStatusText = (s) => ({ 0: '已取消', 1: '正常', 2: '已满', 3: '已结束' }[s] || '-')
const getStatusClass = (s) => ({
  0: 'bg-slate-100 text-slate-500',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-amber-50 text-amber-600',
  3: 'bg-slate-100 text-slate-500'
}[s] || '')

const getResStatusText = (s) => ({ 0: '已预约', 1: '已签到', 2: '已取消', 3: '缺席' }[s] || '-')
const getResStatusClass = (s) => ({
  0: 'bg-blue-50 text-blue-600',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-slate-100 text-slate-500',
  3: 'bg-rose-50 text-rose-600'
}[s] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCoursePage({ current: pagination.current, size: pagination.size, coachId: coachId.value })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const handleLeave = () => {
  Object.assign(leaveForm, { type: 1, courseId: null, startTime: '', endTime: '', reason: '' })
  leaveDialogVisible.value = true
}

const submitLeaveRequest = async () => {
  const valid = await leaveFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await submitLeave(leaveForm)
    ElMessage.success('申请已提交')
    leaveDialogVisible.value = false
  } finally { submitLoading.value = false }
}

const viewReservations = async (row) => {
  currentCourse.value = row
  const res = await getReservationPage({ current: 1, size: 100, courseId: row.id })
  reservationList.value = res.data.records
  reservationDialogVisible.value = true
}

const handleCheckin = async (row) => {
  await checkinReservation(row.id)
  ElMessage.success('签到成功')
  viewReservations(currentCourse.value)
}

onMounted(async () => {
  const res = await getCurrentCoach()
  coachId.value = res.data.id
  fetchData()
})
</script>
