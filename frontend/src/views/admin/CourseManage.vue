<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">课程管理</h1>
        <p class="text-sm text-slate-500">安排和管理所有健身课程</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-blue-500 transition-colors duration-300 shadow-lg shadow-slate-200">
        <iconify-icon icon="lucide:plus" width="18"></iconify-icon>
        新增课程
      </button>
    </div>

    <!-- 搜索筛选栏 -->
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
        <el-select v-model="searchForm.coachId" placeholder="选择教练" clearable style="width: 130px">
          <el-option v-for="c in coachList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <el-button type="primary" @click="fetchData" class="!rounded-lg">搜索</el-button>
        <el-button @click="resetSearch" class="!rounded-lg">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="课程信息" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-12 h-12 rounded-xl flex items-center justify-center flex-shrink-0" :class="getTypeIconBg(row.type)">
                <iconify-icon :icon="getTypeIcon(row.type)" width="24" :class="getTypeIconColor(row.type)"></iconify-icon>
              </div>
              <div>
                <p class="font-medium text-slate-900">{{ row.name }}</p>
                <p class="text-xs text-slate-500">{{ row.type }} · {{ row.duration }}分钟</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="coachName" label="授课教练" min-width="100">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <div class="w-7 h-7 bg-emerald-100 rounded-full flex items-center justify-center">
                <iconify-icon icon="lucide:user" width="14" class="text-emerald-600"></iconify-icon>
              </div>
              <span class="text-slate-700">{{ row.coachName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="容量" min-width="100">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <div class="flex-1 h-2 bg-slate-100 rounded-full overflow-hidden">
                <div class="h-full rounded-full transition-all" :class="getCapacityColor(row)" :style="{ width: `${(row.currentCount / row.maxCapacity) * 100}%` }"></div>
              </div>
              <span class="text-xs text-slate-500 whitespace-nowrap">{{ row.currentCount }}/{{ row.maxCapacity }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="price" label="价格" min-width="90">
          <template #default="{ row }">
            <span class="font-medium text-slate-900">¥{{ row.price }}</span>
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
              {{ row.location }}
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
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <button @click="handleEdit(row)" class="p-2 text-slate-400 hover:text-blue-500 hover:bg-blue-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:edit-3" width="16"></iconify-icon>
              </button>
              <button @click="handleDelete(row)" class="p-2 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-lg transition-colors">
                <iconify-icon icon="lucide:trash-2" width="16"></iconify-icon>
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <div class="p-4 flex justify-between items-center border-t border-slate-100">
        <p class="text-sm text-slate-500">共 {{ pagination.total }} 条记录</p>
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '新增课程'" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="课程类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" class="w-full">
            <el-option label="瑜伽" value="瑜伽" />
            <el-option label="有氧" value="有氧" />
            <el-option label="器械" value="器械" />
            <el-option label="舞蹈" value="舞蹈" />
          </el-select>
        </el-form-item>
        <el-form-item label="授课教练" prop="coachId">
          <el-select v-model="formData.coachId" placeholder="请选择教练" class="w-full">
            <el-option v-for="c in coachList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="最大容量" prop="maxCapacity">
              <el-input-number v-model="formData.maxCapacity" :min="1" :max="100" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="课程价格" prop="price">
              <el-input-number v-model="formData.price" :min="0" :precision="2" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker 
                v-model="formData.startTime" 
                type="datetime" 
                placeholder="选择日期时间"
                format="YYYY-MM-DD HH:mm" 
                value-format="YYYY-MM-DD HH:mm:ss" 
                :shortcuts="dateTimeShortcuts"
                :default-time="new Date(2026, 0, 1, 9, 0, 0)"
                style="width: 100%" 
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="时长(分钟)" prop="duration">
              <el-input-number v-model="formData.duration" :min="30" :max="180" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="上课地点" prop="location">
          <el-input v-model="formData.location" placeholder="请输入上课地点" />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false" class="!rounded-lg">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading" class="!rounded-lg">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCoursePage, createCourse, updateCourse, deleteCourse, checkCoachLeave } from '@/api/course'
import { getCoachList } from '@/api/coach'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const coachList = ref([])

const searchForm = reactive({ name: '', type: '', coachId: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const formData = reactive({
  id: null, name: '', type: '', coachId: null, maxCapacity: 20, price: 0, duration: 60, startTime: '', location: '', description: ''
})

const rules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择课程类型', trigger: 'change' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }]
}

// 日期时间快捷选项
const dateTimeShortcuts = [
  {
    text: '今天上午9点',
    value: () => {
      const date = new Date()
      date.setHours(9, 0, 0, 0)
      return date
    }
  },
  {
    text: '今天下午2点',
    value: () => {
      const date = new Date()
      date.setHours(14, 0, 0, 0)
      return date
    }
  },
  {
    text: '今天晚上7点',
    value: () => {
      const date = new Date()
      date.setHours(19, 0, 0, 0)
      return date
    }
  },
  {
    text: '明天上午9点',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      date.setHours(9, 0, 0, 0)
      return date
    }
  },
  {
    text: '明天下午2点',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 1)
      date.setHours(14, 0, 0, 0)
      return date
    }
  },
  {
    text: '后天上午10点',
    value: () => {
      const date = new Date()
      date.setDate(date.getDate() + 2)
      date.setHours(10, 0, 0, 0)
      return date
    }
  }
]

const getTypeIcon = (type) => {
  const icons = {
    '瑜伽': 'lucide:flower-2',
    '有氧': 'lucide:heart-pulse',
    '器械': 'lucide:dumbbell',
    '舞蹈': 'lucide:music'
  }
  return icons[type] || 'lucide:activity'
}

const getTypeIconBg = (type) => {
  const bgs = {
    '瑜伽': 'bg-purple-50',
    '有氧': 'bg-rose-50',
    '器械': 'bg-blue-50',
    '舞蹈': 'bg-amber-50'
  }
  return bgs[type] || 'bg-slate-50'
}

const getTypeIconColor = (type) => {
  const colors = {
    '瑜伽': 'text-purple-500',
    '有氧': 'text-rose-500',
    '器械': 'text-blue-500',
    '舞蹈': 'text-amber-500'
  }
  return colors[type] || 'text-slate-500'
}

const getCapacityColor = (row) => {
  const ratio = row.currentCount / row.maxCapacity
  if (ratio >= 1) return 'bg-rose-500'
  if (ratio >= 0.8) return 'bg-amber-500'
  return 'bg-emerald-500'
}

const getStatusText = (s) => ({ 0: '已取消', 1: '正常', 2: '已满', 3: '已结束' }[s] || '-')
const getStatusClass = (s) => ({
  0: 'bg-slate-100 text-slate-600',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-amber-50 text-amber-600',
  3: 'bg-slate-100 text-slate-500'
}[s] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCoursePage({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const fetchCoachList = async () => {
  const res = await getCoachList()
  coachList.value = res.data.filter(c => c.status === 1)
}

const resetSearch = () => {
  Object.assign(searchForm, { name: '', type: '', coachId: null })
  pagination.current = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, { id: null, name: '', type: '', coachId: null, maxCapacity: 20, price: 0, duration: 60, startTime: '', location: '', description: '' })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  // 检查教练是否请假
  if (formData.coachId && formData.startTime) {
    try {
      const leaveRes = await checkCoachLeave(formData.coachId, formData.startTime, formData.duration || 60)
      if (leaveRes.data) {
        const coach = coachList.value.find(c => c.id === formData.coachId)
        ElMessage.error(`${coach?.name || '该教练'}在此时间段已请假，请选择其他时间或教练`)
        return
      }
    } catch (e) {
      console.error('检查请假状态失败', e)
    }
  }
  
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateCourse(formData)
      ElMessage.success('修改成功')
    } else {
      await createCourse(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该课程吗？', '提示', { type: 'warning' })
  await deleteCourse(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => {
  fetchData()
  fetchCoachList()
})
</script>
