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
        <el-table-column label="操作" width="200" fixed="right">
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
            <template v-else>
              <el-popover
                placement="top"
                :width="300"
                trigger="click"
                v-model:visible="row.showWaitingInfo"
                @show="loadWaitingInfo(row)"
              >
                <template #reference>
                  <el-button 
                    size="small" 
                    class="!rounded-lg !bg-amber-500 hover:!bg-amber-600"
                  >
                    候补
                  </el-button>
                </template>
                <div class="waiting-info-popover p-2">
                  <h5 class="font-medium text-slate-900 mb-3">「{{ row.name }}」候补队列</h5>
                  <div v-if="row.waitingInfo">
                    <p class="text-sm text-slate-600 mb-2">
                      当前候补人数：<span class="font-semibold text-amber-600">{{ row.waitingInfo.waitingCount }}</span> 人
                    </p>
                    <p v-if="row.waitingInfo.isInQueue" class="text-sm text-emerald-600 mb-3">
                      您的候补顺序：第 {{ row.waitingInfo.myQueueOrder }} 位
                    </p>
                    <p v-else class="text-sm text-slate-500 mb-3">您还未加入该课程的候补队列</p>
                    <div class="flex justify-end gap-2 mt-3 pt-3 border-t border-slate-100">
                      <el-button 
                        v-if="!row.waitingInfo.isInQueue"
                        type="primary" 
                        size="small"
                        class="!bg-amber-500 hover:!bg-amber-600 !rounded-lg"
                        @click="handleJoinWaiting(row)"
                      >
                        加入候补
                      </el-button>
                      <el-button 
                        v-else
                        type="danger" 
                        size="small"
                        class="!rounded-lg"
                        @click="handleCancelWaiting(row)"
                      >
                        取消候补
                      </el-button>
                    </div>
                  </div>
                  <div v-else class="text-center py-2">
                    <el-spinner size="small" />
                  </div>
                </div>
              </el-popover>
            </template>
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
import waitingQueueApi from '@/api/waitingQueue'
import dayjs from 'dayjs'

const loading = ref(false)
const courseList = ref([])
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
  } finally { 
    loading.value = false 
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
    // 用户取消确认框时不显示错误
    if (error === 'cancel' || error?.toString?.().includes('cancel')) {
      return
    }
    // 业务错误已由响应拦截器处理，这里不再重复提示
  }
}

const loadWaitingInfo = async (course) => {
  try {
    const res = await waitingQueueApi.getMemberInfo(course.id)
    course.waitingInfo = res.data
  } catch (error) {
    course.waitingInfo = { waitingCount: 0, isInQueue: false }
  }
}

const handleJoinWaiting = async (course) => {
  try {
    await waitingQueueApi.join(course.id)
    ElMessage.success('成功加入候补队列')
    await loadWaitingInfo(course)
  } catch (error) {
    // 错误已拦截处理
  }
}

const handleCancelWaiting = async (course) => {
  try {
    // 先获取当前会员在该课程的候补记录ID
    const waitingList = await waitingQueueApi.getMyWaitingQueue()
    const myWaiting = waitingList.data?.find(w => w.courseId === course.id)
    if (myWaiting) {
      await waitingQueueApi.cancel(myWaiting.id)
      ElMessage.success('已取消候补')
      await loadWaitingInfo(course)
    }
  } catch (error) {
    // 错误已拦截处理
  }
}

onMounted(() => fetchData())
</script>
