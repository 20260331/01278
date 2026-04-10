<template>
  <div class="space-y-6 w-full">
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">消息通知</h1>
        <p class="text-sm text-slate-500">查看您的系统通知和消息</p>
      </div>
      <el-button v-if="unreadCount > 0" type="primary" link @click="handleMarkAllRead">全部标为已读</el-button>
    </div>

    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <div v-for="item in tableData" :key="item.id" 
           class="p-4 border-b border-slate-100 last:border-0 cursor-pointer hover:bg-slate-50 transition"
           @click="handleMarkRead(item)">
        <div class="flex items-start gap-3">
          <div class="w-2 h-2 rounded-full mt-1.5" :class="item.status === 0 ? 'bg-blue-500' : 'bg-slate-300'"></div>
          <div class="flex-1">
            <h4 class="font-medium text-slate-900 mb-1" :class="item.status === 0 ? '' : 'text-slate-500'">{{ item.title }}</h4>
            <p class="text-sm text-slate-600 mb-2">{{ item.content }}</p>
            <p class="text-xs text-slate-400">{{ item.createTime }}</p>
          </div>
        </div>
      </div>

      <div v-if="tableData.length === 0 && !loading" class="py-12 text-center">
        <iconify-icon icon="lucide:bell" width="40" class="text-slate-300 mb-3"></iconify-icon>
        <p class="text-slate-400">暂无通知消息</p>
      </div>

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
import { ElMessage } from 'element-plus'
import { getMyNotifications, markAsRead, markAllAsRead, getUnreadCount } from '@/api/notification'

const loading = ref(false)
const tableData = ref([])
const unreadCount = ref(0)
const pagination = reactive({ current: 1, size: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyNotifications({ current: pagination.current, size: pagination.size })
    tableData.value = res.data.records
    pagination.total = res.data.total
    
    const countRes = await getUnreadCount()
    unreadCount.value = countRes.data
  } finally { loading.value = false }
}

const handleMarkRead = async (item) => {
  if (item.status === 0) {
    await markAsRead(item.id)
    fetchData()
  }
}

const handleMarkAllRead = async () => {
  await markAllAsRead()
  ElMessage.success('已全部标为已读')
  fetchData()
}

onMounted(() => fetchData())
</script>
