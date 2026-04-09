<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">意见反馈</h1>
        <p class="text-sm text-slate-500">提交您的建议，帮助我们做得更好</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-blue-500 transition-colors duration-300 shadow-lg shadow-slate-200">
        <iconify-icon icon="lucide:message-square-plus" width="18"></iconify-icon>
        提交反馈
      </button>
    </div>

    <!-- 反馈列表 - 卡片形式 -->
    <div v-if="tableData.length > 0" class="space-y-4">
      <div 
        v-for="item in tableData" 
        :key="item.id" 
        @click="viewDetail(item)"
        class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-lg transition-all duration-300 cursor-pointer group"
      >
        <div class="flex items-start justify-between mb-3">
          <h3 class="text-lg font-medium text-slate-900 group-hover:text-blue-500 transition-colors">{{ item.title }}</h3>
          <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="item.status === 0 ? 'bg-amber-50 text-amber-600' : 'bg-emerald-50 text-emerald-600'">
            <span class="w-1.5 h-1.5 rounded-full" :class="item.status === 0 ? 'bg-amber-500' : 'bg-emerald-500'"></span>
            {{ item.status === 0 ? '待回复' : '已回复' }}
          </span>
        </div>
        <p class="text-slate-500 text-sm line-clamp-2 mb-4">{{ item.content }}</p>
        <div class="flex items-center justify-between text-xs text-slate-400">
          <span>{{ item.createTime }}</span>
          <iconify-icon icon="lucide:chevron-right" width="16" class="group-hover:translate-x-1 transition-transform"></iconify-icon>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!loading" class="bg-white rounded-2xl border border-slate-100 p-12 text-center">
      <iconify-icon icon="lucide:message-square" width="48" class="text-slate-300 mb-4"></iconify-icon>
      <h3 class="text-lg font-medium text-slate-900 mb-2">暂无反馈记录</h3>
      <p class="text-sm text-slate-500 mb-6">有任何建议或问题，欢迎提交反馈</p>
      <button @click="handleAdd" class="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-blue-500 transition-colors">
        <iconify-icon icon="lucide:message-square-plus" width="18"></iconify-icon>
        提交反馈
      </button>
    </div>

    <!-- 分页 -->
    <div v-if="tableData.length > 0" class="flex justify-center">
      <el-pagination 
        v-model:current-page="pagination.current" 
        v-model:page-size="pagination.size" 
        :total="pagination.total" 
        layout="prev, pager, next" 
        @change="fetchData" 
        background
      />
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="flex justify-center py-12">
      <iconify-icon icon="lucide:loader-2" width="32" class="text-blue-500 animate-spin"></iconify-icon>
    </div>

    <!-- 提交反馈弹窗 -->
    <el-dialog v-model="dialogVisible" title="提交反馈" width="500px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="60px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="formData.title" placeholder="请简要描述您的问题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="5" placeholder="请详细描述您的意见或建议" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="!rounded-lg">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="!rounded-lg">提交</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="反馈详情" width="500px">
      <div class="space-y-6">
        <div>
          <p class="text-xs text-slate-400 uppercase tracking-wider mb-1">标题</p>
          <p class="text-slate-900 font-medium">{{ currentFeedback.title }}</p>
        </div>
        <div>
          <p class="text-xs text-slate-400 uppercase tracking-wider mb-1">内容</p>
          <p class="text-slate-700">{{ currentFeedback.content }}</p>
        </div>
        <div>
          <p class="text-xs text-slate-400 uppercase tracking-wider mb-1">提交时间</p>
          <p class="text-slate-600">{{ currentFeedback.createTime }}</p>
        </div>
        
        <div v-if="currentFeedback.status === 1" class="bg-emerald-50 rounded-xl p-5 border border-emerald-100">
          <div class="flex items-center gap-2 mb-3">
            <iconify-icon icon="lucide:message-circle" width="18" class="text-emerald-600"></iconify-icon>
            <span class="text-sm font-medium text-emerald-700">官方回复</span>
          </div>
          <p class="text-slate-700 mb-3">{{ currentFeedback.reply }}</p>
          <p class="text-xs text-slate-400">回复时间：{{ currentFeedback.replyTime }}</p>
        </div>
        
        <div v-else class="bg-amber-50 rounded-xl p-5 border border-amber-100 text-center">
          <iconify-icon icon="lucide:clock" width="24" class="text-amber-500 mb-2"></iconify-icon>
          <p class="text-amber-700 font-medium">等待回复中...</p>
          <p class="text-xs text-amber-500 mt-1">我们会尽快处理您的反馈</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyFeedbacks, createFeedback } from '@/api/feedback'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const currentFeedback = ref({})

const pagination = reactive({ current: 1, size: 10, total: 0 })
const formData = reactive({ title: '', content: '' })

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyFeedbacks({ current: pagination.current, size: pagination.size })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(formData, { title: '', content: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createFeedback(formData)
    ElMessage.success('反馈已提交')
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

const viewDetail = (row) => {
  currentFeedback.value = row
  detailDialogVisible.value = true
}

onMounted(() => fetchData())
</script>
