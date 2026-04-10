<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div>
      <h1 class="text-2xl font-serif text-slate-900 mb-1">课程预约</h1>
      <p class="text-sm text-slate-500">浏览并预约您感兴趣的健身课程</p>
    </div>

    <!-- 筛选栏 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-input v-model="searchForm.name" placeholder="搜索课程名称" clearable style="width: 220px">
          <template #prefix>
            <iconify-icon icon="lucide:search" width="16" class="text-slate-400"></iconify-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.type" placeholder="课程类型" clearable style="width: 130px">
          <el-option label="瑜伽" value="瑜伽" />
          <el-option label="有氧" value="有氧" />
          <el-option label="器械" value="器械" />
          <el-option label="舞蹈" value="舞蹈" />
        </el-select>
        <el-button type="primary" @click="handleSearch" class="!rounded-lg">搜索</el-button>
        <el-button @click="resetSearch" class="!rounded-lg">重置</el-button>
      </div>
    </div>

    <!-- 课程列表 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <el-table :data="filteredList" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="课程名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">{{ row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" min-width="100">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getTypeClass(row.type)">
              {{ row.type }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="coachName" label="教练" min-width="100">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.coachName || '待定' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" label="上课时间" min-width="160">
          <template #default="{ row }">
            <span class="text-slate-600">{{ formatTime(row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="100">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.location || '待定' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="剩余名额" min-width="120">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span :class="getCapacityClass(row)">
                {{ row.maxCapacity - row.currentCount }}
              </span>
              <span class="text-slate-400">/ {{ row.maxCapacity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" min-width="100">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.currentCount < row.maxCapacity"
              type="primary"
              size="small"
              @click="handleReserve(row)"
              class="!rounded-lg"
            >
              预约
            </el-button>
            <div v-else class="flex items-center gap-2">
              <el-button
                type="warning"
                size="small"
                @click="handleJoinWaitlist(row)"
                class="!rounded-lg"
              >
                候补
              </el-button>
              <span v-if="waitlistCounts[row.id]" class="text-xs text-amber-600">
                {{ waitlistCounts[row.id] }}人排队
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="filteredList.length === 0 && !loading" class="py-12 text-center">
        <iconify-icon icon="lucide:calendar-x" width="40" class="text-slate-300 mb-3"></iconify-icon>
        <p class="text-slate-400">暂无可预约课程</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAvailableCourses } from '@/api/course'
import { createReservation } from '@/api/reservation'
import { joinWaitlist, getWaitlistCount } from '@/api/waitlist'
import dayjs from 'dayjs'

const loading = ref(false)
const courseList = ref([])
const waitlistCounts = ref({})
const searchForm = reactive({
  name: '',
  type: ''
})

const filteredList = computed(() => {
  return courseList.value.filter(course => {
    const nameMatch = !searchForm.name || course.name.includes(searchForm.name)
    const typeMatch = !searchForm.type || course.type === searchForm.type
    return nameMatch && typeMatch
  })
})

const formatTime = (time) => time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '-'

const getTypeClass = (type) => ({
  '瑜伽': 'bg-purple-50 text-purple-600',
  '有氧': 'bg-rose-50 text-rose-600',
  '器械': 'bg-blue-50 text-blue-600',
  '舞蹈': 'bg-amber-50 text-amber-600'
}[type] || 'bg-slate-100 text-slate-600')

const getCapacityClass = (row) => {
  const remaining = row.maxCapacity - row.currentCount
  if (remaining === 0) return 'text-slate-400'
  if (remaining <= 3) return 'text-amber-600 font-medium'
  return 'text-emerald-600 font-medium'
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getAvailableCourses()
    courseList.value = res.data
    await fetchWaitlistCounts()
  } finally {
    loading.value = false
  }
}

const fetchWaitlistCounts = async () => {
  const fullCourses = courseList.value.filter(c => c.currentCount >= c.maxCapacity)
  for (const course of fullCourses) {
    try {
      const res = await getWaitlistCount(course.id)
      waitlistCounts.value[course.id] = res.data
    } catch (e) {
      waitlistCounts.value[course.id] = 0
    }
  }
}

const handleSearch = () => {
  // 筛选由 computed 自动处理
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.type = ''
}

const handleReserve = async (course) => {
  try {
    await ElMessageBox.confirm(
      `确定预约「${course.name}」课程吗？`,
      '确认预约',
      {
        type: 'info',
        confirmButtonText: '确认预约',
        cancelButtonText: '取消'
      }
    )
    await createReservation(course.id)
    ElMessage.success('预约成功')
    fetchData()
  } catch (error) {
    if (error === 'cancel' || error?.toString?.().includes('cancel')) {
      return
    }
  }
}

const handleJoinWaitlist = async (course) => {
  try {
    await ElMessageBox.confirm(
      `课程「${course.name}」已满，是否加入候补队列？\n候补成功后，当有名额释放时将自动为您补位。`,
      '加入候补',
      {
        type: 'warning',
        confirmButtonText: '确认候补',
        cancelButtonText: '取消'
      }
    )
    await joinWaitlist(course.id)
    ElMessage.success('候补成功，您将在有名额时收到通知')
    fetchData()
  } catch (error) {
    if (error === 'cancel' || error?.toString?.().includes('cancel')) {
      return
    }
  }
}

onMounted(() => fetchData())
</script>
