<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div>
      <h1 class="text-2xl font-serif text-slate-900 mb-1">我的候补</h1>
      <p class="text-sm text-slate-500">查看和管理您的候补排队记录</p>
    </div>

    <!-- 候补列表 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="courseName" label="课程名称" min-width="150">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">{{ row.courseName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="courseStartTime" label="上课时间" min-width="170">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.courseStartTime }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="queueNo" label="排队序号" min-width="100">
          <template #default="{ row }">
            <span class="text-lg font-bold text-blue-600">#{{ row.queueNo }}</span>
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
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 1"
              type="primary"
              size="small"
              @click="handleConfirm(row)"
              class="!rounded-lg"
            >
              确认补位
            </el-button>
            <el-button
              v-if="row.status === 0 || row.status === 1"
              link
              type="danger"
              size="small"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
            <span v-if="row.status === 2" class="text-slate-400 text-sm">-</span>
            <span v-if="row.status === 3" class="text-slate-400 text-sm">已过期</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 空状态 -->
      <div v-if="tableData.length === 0 && !loading" class="py-12 text-center">
        <iconify-icon icon="lucide:clock" width="40" class="text-slate-300 mb-3"></iconify-icon>
        <p class="text-slate-400 mb-4">暂无候补记录</p>
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

    <!-- 补位确认对话框 -->
    <el-dialog v-model="confirmDialogVisible" title="确认补位" width="400px">
      <div class="py-4">
        <p class="text-lg mb-2">课程：<span class="font-medium">{{ currentWait.courseName }}</span></p>
        <p class="text-lg mb-4">上课时间：<span class="font-medium">{{ currentWait.courseStartTime }}</span></p>
        <el-alert type="success" :closable="false">
          确认后将成为正式预约，请按时参加课程
        </el-alert>
      </div>
      <template #footer>
        <el-button @click="confirmDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmWaitlist" :loading="confirmLoading">确认补位</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMyWaitlist, cancelWaitlist, confirmWaitlist as confirmWaitlistApi } from '@/api/waitlist'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ current: 1, size: 10, total: 0 })

const confirmDialogVisible = ref(false)
const confirmLoading = ref(false)
const currentWait = ref({})

const getStatusText = (s) => ({
  0: '排队中',
  1: '已补位',
  2: '已取消',
  3: '已过期'
}[s] || '-')

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
    const res = await getMyWaitlist({ current: pagination.current, size: pagination.size })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确定要取消该候补吗？', '取消候补', { type: 'warning' })
  await cancelWaitlist(row.id)
  ElMessage.success('取消成功')
  fetchData()
}

const handleConfirm = (row) => {
  currentWait.value = row
  confirmDialogVisible.value = true
}

const confirmWaitlist = async () => {
  confirmLoading.value = true
  try {
    await confirmWaitlistApi(currentWait.value.id)
    ElMessage.success('补位成功！已转为正式预约')
    confirmDialogVisible.value = false
    fetchData()
  } finally {
    confirmLoading.value = false
  }
}

onMounted(() => fetchData())
</script>
