<template>
  <div class="space-y-6 w-full">
    <div class="flex items-center justify-end gap-2">
      <el-button @click="viewReport">查看周报</el-button>
      <el-button type="primary" @click="handleAdd">录入数据</el-button>
    </div>

    <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden w-full">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="recordDate" label="日期" min-width="120" />
        <el-table-column prop="weight" label="体重(kg)" min-width="100" />
        <el-table-column prop="duration" label="训练时长(分钟)" min-width="130" />
        <el-table-column prop="calories" label="消耗卡路里" min-width="120" />
        <el-table-column prop="content" label="训练内容" min-width="200" />
        <el-table-column prop="remark" label="备注" min-width="150" />
      </el-table>
      <div class="p-4 flex justify-end">
        <el-pagination v-model:current-page="pagination.current" v-model:page-size="pagination.size" :total="pagination.total" layout="total, sizes, prev, pager, next" @change="fetchData" />
      </div>
    </div>

    <!-- 录入弹窗 -->
    <el-dialog v-model="dialogVisible" title="录入健身数据" width="500px">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
        <el-form-item label="记录日期" prop="recordDate">
          <el-date-picker v-model="formData.recordDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="体重(kg)" prop="weight">
          <el-input-number v-model="formData.weight" :min="30" :max="200" :precision="1" />
        </el-form-item>
        <el-form-item label="训练时长" prop="duration">
          <el-input-number v-model="formData.duration" :min="0" :max="480" />
          <span class="ml-2 text-slate-400">分钟</span>
        </el-form-item>
        <el-form-item label="消耗卡路里" prop="calories">
          <el-input-number v-model="formData.calories" :min="0" :max="5000" />
        </el-form-item>
        <el-form-item label="训练内容" prop="content">
          <el-input v-model="formData.content" type="textarea" :rows="2" placeholder="如：跑步30分钟，哑铃训练等" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formData.remark" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 周报弹窗 -->
    <el-dialog v-model="reportDialogVisible" title="本周健身周报" width="500px">
      <div class="space-y-4">
        <div class="grid grid-cols-2 gap-4">
          <div class="bg-indigo-50 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold text-indigo-600">{{ report.trainDays || 0 }}</p>
            <p class="text-sm text-slate-500">训练天数</p>
          </div>
          <div class="bg-emerald-50 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold text-emerald-600">{{ report.totalDuration || 0 }}</p>
            <p class="text-sm text-slate-500">总时长(分钟)</p>
          </div>
          <div class="bg-amber-50 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold text-amber-600">{{ report.totalCalories || 0 }}</p>
            <p class="text-sm text-slate-500">总消耗卡路里</p>
          </div>
          <div class="bg-rose-50 rounded-xl p-4 text-center">
            <p class="text-2xl font-bold" :class="report.weightChange > 0 ? 'text-rose-600' : 'text-emerald-600'">
              {{ report.weightChange > 0 ? '+' : '' }}{{ report.weightChange || 0 }}kg
            </p>
            <p class="text-sm text-slate-500">体重变化</p>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getMyFitnessRecords, createFitnessRecord, getWeeklyReport } from '@/api/fitness'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const reportDialogVisible = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const report = ref({})

const pagination = reactive({ current: 1, size: 10, total: 0 })
const formData = reactive({ recordDate: dayjs().format('YYYY-MM-DD'), weight: null, duration: null, calories: null, content: '', remark: '' })

const rules = {
  recordDate: [{ required: true, message: '请选择日期', trigger: 'change' }]
}

const fetchData = async () => {
  loading.value = true
  try {
    const res = await getMyFitnessRecords({ current: pagination.current, size: pagination.size })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally { loading.value = false }
}

const handleAdd = () => {
  Object.assign(formData, { recordDate: dayjs().format('YYYY-MM-DD'), weight: null, duration: null, calories: null, content: '', remark: '' })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitLoading.value = true
  try {
    await createFitnessRecord(formData)
    ElMessage.success('录入成功')
    dialogVisible.value = false
    fetchData()
  } finally { submitLoading.value = false }
}

const viewReport = async () => {
  const res = await getWeeklyReport()
  report.value = res.data
  reportDialogVisible.value = true
}

onMounted(() => fetchData())
</script>
