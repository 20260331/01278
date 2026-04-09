<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">场地管理</h1>
        <p class="text-sm text-slate-500">管理健身房场地信息</p>
      </div>
      <button @click="handleAdd" class="inline-flex items-center gap-2 px-5 py-2.5 bg-slate-900 text-white rounded-lg text-sm font-medium hover:bg-blue-500 transition-colors duration-300 shadow-lg shadow-slate-200">
        <iconify-icon icon="lucide:plus" width="18"></iconify-icon>
        新增场地
      </button>
    </div>

    <!-- 搜索筛选栏 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <div class="flex flex-wrap gap-4 items-center">
        <el-input v-model="searchForm.name" placeholder="搜索场地名称" clearable style="width: 200px">
          <template #prefix>
            <iconify-icon icon="lucide:search" width="16" class="text-slate-400"></iconify-icon>
          </template>
        </el-input>
        <el-select v-model="searchForm.type" placeholder="场地类型" clearable style="width: 130px">
          <el-option label="瑜伽室" value="瑜伽室" />
          <el-option label="有氧区" value="有氧区" />
          <el-option label="力量区" value="力量区" />
          <el-option label="舞蹈室" value="舞蹈室" />
          <el-option label="游泳池" value="游泳池" />
          <el-option label="其他" value="其他" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
          <el-option label="可用" :value="1" />
          <el-option label="维护中" :value="0" />
          <el-option label="已停用" :value="2" />
        </el-select>
        <el-button type="primary" @click="fetchData" class="!rounded-lg">搜索</el-button>
        <el-button @click="resetSearch" class="!rounded-lg">重置</el-button>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden">
      <el-table :data="tableData" v-loading="loading" style="width: 100%">
        <el-table-column prop="name" label="场地名称" min-width="150">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="getTypeIconBg(row.type)">
                <iconify-icon :icon="getTypeIcon(row.type)" width="20" :class="getTypeIconColor(row.type)"></iconify-icon>
              </div>
              <div>
                <p class="font-medium text-slate-900">{{ row.name }}</p>
                <p class="text-xs text-slate-500">{{ row.type }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="capacity" label="容纳人数" min-width="100">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.capacity || '-' }} 人</span>
          </template>
        </el-table-column>
        <el-table-column prop="area" label="面积" min-width="100">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.area || '-' }} m²</span>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="位置" min-width="150">
          <template #default="{ row }">
            <span class="text-slate-600">{{ row.location || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-medium" :class="getStatusClass(row.status)">
              <span class="w-1.5 h-1.5 rounded-full" :class="getStatusDotClass(row.status)"></span>
              {{ getStatusText(row.status) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑场地' : '新增场地'" width="550px" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="场地名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入场地名称" />
        </el-form-item>
        <el-form-item label="场地类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择类型" class="w-full">
            <el-option label="瑜伽室" value="瑜伽室" />
            <el-option label="有氧区" value="有氧区" />
            <el-option label="力量区" value="力量区" />
            <el-option label="舞蹈室" value="舞蹈室" />
            <el-option label="游泳池" value="游泳池" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="容纳人数" prop="capacity">
              <el-input-number v-model="formData.capacity" :min="1" :max="500" class="w-full" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="面积(m²)" prop="area">
              <el-input-number v-model="formData.area" :min="1" :precision="2" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="位置描述" prop="location">
          <el-input v-model="formData.location" placeholder="如：2楼东侧" />
        </el-form-item>
        <el-form-item label="设施设备" prop="facilities">
          <el-input v-model="formData.facilities" type="textarea" :rows="2" placeholder="请输入场地设施设备" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">可用</el-radio>
            <el-radio :label="0">维护中</el-radio>
            <el-radio :label="2">已停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { getVenuePage, createVenue, updateVenue, deleteVenue } from '@/api/venue'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const searchForm = reactive({ name: '', type: '', status: null })
const pagination = reactive({ current: 1, size: 10, total: 0 })
const formData = reactive({
  id: null, name: '', type: '', capacity: 20, area: null, location: '', facilities: '', status: 1, remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入场地名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择场地类型', trigger: 'change' }]
}

const getTypeIcon = (type) => {
  const icons = {
    '瑜伽室': 'lucide:flower-2',
    '有氧区': 'lucide:heart-pulse',
    '力量区': 'lucide:dumbbell',
    '舞蹈室': 'lucide:music',
    '游泳池': 'lucide:waves'
  }
  return icons[type] || 'lucide:map-pin'
}

const getTypeIconBg = (type) => {
  const bgs = {
    '瑜伽室': 'bg-purple-50',
    '有氧区': 'bg-rose-50',
    '力量区': 'bg-blue-50',
    '舞蹈室': 'bg-amber-50',
    '游泳池': 'bg-cyan-50'
  }
  return bgs[type] || 'bg-slate-50'
}

const getTypeIconColor = (type) => {
  const colors = {
    '瑜伽室': 'text-purple-500',
    '有氧区': 'text-rose-500',
    '力量区': 'text-blue-500',
    '舞蹈室': 'text-amber-500',
    '游泳池': 'text-cyan-500'
  }
  return colors[type] || 'text-slate-500'
}

const getStatusText = (s) => ({ 0: '维护中', 1: '可用', 2: '已停用' }[s] || '-')
const getStatusClass = (s) => ({
  0: 'bg-amber-50 text-amber-600',
  1: 'bg-emerald-50 text-emerald-600',
  2: 'bg-slate-100 text-slate-500'
}[s] || '')
const getStatusDotClass = (s) => ({
  0: 'bg-amber-500',
  1: 'bg-emerald-500',
  2: 'bg-slate-400'
}[s] || '')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getVenuePage({ current: pagination.current, size: pagination.size, ...searchForm })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  Object.assign(searchForm, { name: '', type: '', status: null })
  pagination.current = 1
  fetchData()
}

const handleAdd = () => {
  isEdit.value = false
  Object.assign(formData, { id: null, name: '', type: '', capacity: 20, area: null, location: '', facilities: '', status: 1, remark: '' })
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
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await updateVenue(formData)
      ElMessage.success('修改成功')
    } else {
      await createVenue(formData)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const handleDelete = async (row) => {
  await ElMessageBox.confirm('确定要删除该场地吗？', '提示', { type: 'warning' })
  await deleteVenue(row.id)
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => fetchData())
</script>
