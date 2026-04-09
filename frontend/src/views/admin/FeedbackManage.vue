<template>
  <div class="space-y-6 w-full">

    <div class="bg-white rounded-xl border border-slate-200 p-4 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="待回复" :value="0" />
          <el-option label="已回复" :value="1" />
        </el-select>
        <el-button type="primary" @click="fetchData">搜索</el-button>
        <el-button @click="resetSearch">重置</el-button>
      </div>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="memberName" label="会员" min-width="100" />
        <el-table-column prop="title" label="标题" min-width="150" />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <span class="px-2 py-1 rounded-full text-xs font-medium" :class="row.status === 0 ? 'bg-amber-50 text-amber-600' : 'bg-emerald-50 text-emerald-600'">
              {{ row.status === 0 ? '待回复' : '已回复' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" min-width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleReply(row)">{{ row.status === 0 ? '回复' : '查看' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="反馈详情" width="500px">
      <div class="space-y-4">
        <div><span class="text-slate-500">会员：</span>{{ currentFeedback.memberName }}</div>
        <div><span class="text-slate-500">标题：</span>{{ currentFeedback.title }}</div>
        <div><span class="text-slate-500">内容：</span><p class="mt-1 text-slate-700">{{ currentFeedback.content }}</p></div>
        <div v-if="currentFeedback.status === 1">
          <span class="text-slate-500">回复：</span><p class="mt-1 text-slate-700">{{ currentFeedback.reply }}</p>
        </div>
        <div v-else>
          <span class="text-slate-500">回复：</span>
          <el-input v-model="replyContent" type="textarea" :rows="3" placeholder="请输入回复内容" class="mt-1" />
        </div>
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="currentFeedback.status === 0" type="primary" @click="handleSubmitReply" :loading="submitLoading">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getFeedbackPage, replyFeedback } from '@/api/feedback'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const submitLoading = ref(false)
const currentFeedback = ref({})
const replyContent = ref('')

const searchForm = reactive({ status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getFeedbackPage({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const resetSearch = () => { searchForm.status = null; pagination.current = 1; fetchData() }

const handleReply = (row) => {
  currentFeedback.value = row
  replyContent.value = ''
  dialogVisible.value = true
}

const handleSubmitReply = async () => {
  if (!replyContent.value) {
    ElMessage.warning('请输入回复内容')
    return
  }
  submitLoading.value = true
  try {
    await replyFeedback(currentFeedback.value.id, replyContent.value)
    ElMessage.success('回复成功')
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

onMounted(() => fetchData())
</script>
