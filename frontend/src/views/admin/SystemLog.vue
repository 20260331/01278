<template>
  <div class="space-y-6 w-full">

    <div class="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-input v-model="searchForm.username" placeholder="操作用户" clearable style="width: 160px" />
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="username" label="操作用户" min-width="100" />
        <el-table-column prop="operation" label="操作" min-width="120" />
        <el-table-column prop="method" label="方法" min-width="280" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" min-width="130" />
        <el-table-column prop="duration" label="耗时(ms)" min-width="100" />
        <el-table-column prop="createTime" label="操作时间" min-width="180">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getSystemLogs } from '@/api/statistics'

const loading = ref(false)
const tableData = ref([])
const searchForm = reactive({ username: '' })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getSystemLogs({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.username = ''; pagination.current = 1; fetchData() }

const formatTime = (time) => {
  if (!time) return '-'
  return time.replace('T', ' ').substring(0, 19)
}

onMounted(() => fetchData())
</script>
