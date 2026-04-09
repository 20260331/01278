<template>
  <div class="space-y-6 w-full">
    <!-- 页面标题区 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-serif text-slate-900 mb-1">数据备份</h1>
        <p class="text-sm text-slate-500">备份和恢复系统数据</p>
      </div>
    </div>

    <!-- 数据统计 -->
    <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <h3 class="text-lg font-medium text-slate-900 mb-4">当前数据统计</h3>
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4" v-loading="statsLoading">
        <div class="p-4 bg-blue-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-blue-600">{{ stats.memberCount || 0 }}</p>
          <p class="text-sm text-blue-500 mt-1">会员</p>
        </div>
        <div class="p-4 bg-emerald-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-emerald-600">{{ stats.coachCount || 0 }}</p>
          <p class="text-sm text-emerald-500 mt-1">教练</p>
        </div>
        <div class="p-4 bg-purple-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-purple-600">{{ stats.courseCount || 0 }}</p>
          <p class="text-sm text-purple-500 mt-1">课程</p>
        </div>
        <div class="p-4 bg-amber-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-amber-600">{{ stats.equipmentCount || 0 }}</p>
          <p class="text-sm text-amber-500 mt-1">器材</p>
        </div>
        <div class="p-4 bg-rose-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-rose-600">{{ stats.feedbackCount || 0 }}</p>
          <p class="text-sm text-rose-500 mt-1">反馈</p>
        </div>
        <div class="p-4 bg-indigo-50 rounded-xl text-center">
          <p class="text-2xl font-bold text-indigo-600">{{ stats.consumeRecordCount || 0 }}</p>
          <p class="text-sm text-indigo-500 mt-1">消费记录</p>
        </div>
      </div>
    </div>

    <!-- 备份操作 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
      <!-- 数据导出 -->
      <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-12 h-12 bg-blue-100 rounded-xl flex items-center justify-center">
            <iconify-icon icon="lucide:download" width="24" class="text-blue-600"></iconify-icon>
          </div>
          <div>
            <h3 class="text-lg font-medium text-slate-900">数据导出</h3>
            <p class="text-sm text-slate-500">将当前数据导出为JSON文件</p>
          </div>
        </div>
        <p class="text-sm text-slate-600 mb-4">
          导出的备份文件包含会员、教练、课程、器材、反馈、消费记录等数据。
          建议定期备份以防数据丢失。
        </p>
        <el-button type="primary" @click="handleExport" :loading="exportLoading">
          <iconify-icon icon="lucide:download" width="18" class="mr-2"></iconify-icon>
          导出数据备份
        </el-button>
      </div>

      <!-- 数据导入 -->
      <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-12 h-12 bg-emerald-100 rounded-xl flex items-center justify-center">
            <iconify-icon icon="lucide:upload" width="24" class="text-emerald-600"></iconify-icon>
          </div>
          <div>
            <h3 class="text-lg font-medium text-slate-900">数据恢复</h3>
            <p class="text-sm text-slate-500">从备份文件恢复数据</p>
          </div>
        </div>
        <p class="text-sm text-slate-600 mb-4">
          上传备份文件进行验证和恢复。支持增量导入（仅导入新记录）和覆盖导入（更新已存在的记录）。
        </p>
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".json"
          :on-change="handleFileChange"
          :on-exceed="handleExceed"
        >
          <template #trigger>
            <el-button type="success">
              <iconify-icon icon="lucide:upload" width="18" class="mr-2"></iconify-icon>
              选择备份文件
            </el-button>
          </template>
        </el-upload>
        <div v-if="selectedFile" class="mt-3 space-y-3">
          <el-button type="primary" @click="handleValidate" :loading="validateLoading">
            验证备份文件
          </el-button>
        </div>
      </div>
    </div>

    <!-- 验证结果和恢复操作 -->
    <div v-if="validationResult" class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <h3 class="text-lg font-medium text-slate-900 mb-4">验证结果</h3>
      <div class="bg-emerald-50 rounded-xl p-4 mb-6">
        <div class="flex items-center gap-2 mb-3">
          <iconify-icon icon="lucide:check-circle" width="20" class="text-emerald-600"></iconify-icon>
          <span class="font-medium text-emerald-700">{{ validationResult.message }}</span>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4 text-sm">
          <div>
            <span class="text-slate-500">备份时间：</span>
            <span class="text-slate-700">{{ validationResult.backupTime }}</span>
          </div>
          <div>
            <span class="text-slate-500">版本：</span>
            <span class="text-slate-700">{{ validationResult.version }}</span>
          </div>
          <div>
            <span class="text-slate-500">会员数：</span>
            <span class="text-slate-700">{{ validationResult.membersInBackup || 0 }}</span>
          </div>
          <div>
            <span class="text-slate-500">教练数：</span>
            <span class="text-slate-700">{{ validationResult.coachesInBackup || 0 }}</span>
          </div>
          <div>
            <span class="text-slate-500">课程数：</span>
            <span class="text-slate-700">{{ validationResult.coursesInBackup || 0 }}</span>
          </div>
          <div>
            <span class="text-slate-500">总记录：</span>
            <span class="text-slate-700 font-semibold">{{ validationResult.totalRecords || 0 }}</span>
          </div>
        </div>
      </div>
      
      <!-- 恢复操作 -->
      <div class="border-t border-slate-200 pt-6">
        <h4 class="text-base font-medium text-slate-800 mb-4">执行数据恢复</h4>
        <div class="flex flex-wrap items-center gap-4">
          <el-radio-group v-model="restoreMode" class="mb-4">
            <el-radio value="increment">
              <span class="font-medium">增量导入</span>
              <span class="text-sm text-slate-500 ml-1">（仅导入ID不存在的记录，跳过已有数据）</span>
            </el-radio>
            <el-radio value="overwrite">
              <span class="font-medium">覆盖导入</span>
              <span class="text-sm text-slate-500 ml-1">（更新已存在的记录，导入新记录）</span>
            </el-radio>
          </el-radio-group>
        </div>
        <div class="flex gap-3">
          <el-button type="primary" @click="handleRestore" :loading="restoreLoading">
            <iconify-icon icon="lucide:database" width="18" class="mr-2"></iconify-icon>
            开始恢复
          </el-button>
          <el-button @click="resetRestore">取消</el-button>
        </div>
      </div>
    </div>

    <!-- 恢复结果 -->
    <div v-if="restoreResult" class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
      <h3 class="text-lg font-medium text-slate-900 mb-4">恢复结果</h3>
      <div :class="restoreResult.success ? 'bg-emerald-50' : 'bg-amber-50'" class="rounded-xl p-4">
        <div class="flex items-center gap-2 mb-3">
          <iconify-icon 
            :icon="restoreResult.success ? 'lucide:check-circle' : 'lucide:alert-circle'" 
            width="20" 
            :class="restoreResult.success ? 'text-emerald-600' : 'text-amber-600'"
          ></iconify-icon>
          <span :class="restoreResult.success ? 'text-emerald-700' : 'text-amber-700'" class="font-medium">
            {{ restoreResult.message }}
          </span>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4 text-sm mt-4">
          <div>
            <span class="text-slate-500">恢复模式：</span>
            <span class="text-slate-700">{{ restoreResult.mode }}</span>
          </div>
          <div>
            <span class="text-slate-500">导入记录：</span>
            <span class="text-emerald-600 font-semibold">{{ restoreResult.totalImported || 0 }}</span>
          </div>
          <div>
            <span class="text-slate-500">跳过记录：</span>
            <span class="text-slate-700">{{ restoreResult.totalSkipped || 0 }}</span>
          </div>
        </div>
        <!-- 详细导入统计 -->
        <div v-if="restoreResult.imported" class="mt-4 pt-4 border-t border-slate-200">
          <p class="text-sm text-slate-500 mb-2">详细统计：</p>
          <div class="grid grid-cols-3 md:grid-cols-6 gap-2 text-sm">
            <div v-for="(count, table) in restoreResult.imported" :key="table">
              <span class="text-slate-500">{{ getTableName(table) }}：</span>
              <span class="text-emerald-600">{{ count }}</span>
            </div>
          </div>
        </div>
        <!-- 错误信息 -->
        <div v-if="restoreResult.errors && Object.keys(restoreResult.errors).length > 0" class="mt-4 pt-4 border-t border-red-200">
          <p class="text-sm text-red-600 mb-2">错误信息：</p>
          <div v-for="(errors, table) in restoreResult.errors" :key="table" class="text-sm text-red-500">
            <strong>{{ getTableName(table) }}:</strong>
            <ul class="list-disc list-inside">
              <li v-for="(error, index) in errors.slice(0, 3)" :key="index">{{ error }}</li>
              <li v-if="errors.length > 3">...还有 {{ errors.length - 3 }} 个错误</li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <!-- 注意事项 -->
    <div class="bg-amber-50 rounded-2xl border border-amber-200 p-6">
      <div class="flex items-start gap-3">
        <iconify-icon icon="lucide:alert-triangle" width="24" class="text-amber-600 flex-shrink-0 mt-0.5"></iconify-icon>
        <div>
          <h4 class="font-medium text-amber-800 mb-2">注意事项</h4>
          <ul class="text-sm text-amber-700 space-y-1">
            <li>1. 建议每周至少进行一次数据备份</li>
            <li>2. 备份文件请妥善保管，避免泄露敏感信息</li>
            <li>3. 恢复前请确保已备份当前数据，以防数据丢失</li>
            <li>4. 增量导入仅导入新记录，不会修改已有数据</li>
            <li>5. 覆盖导入会更新已存在的记录，请谨慎操作</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBackupStats, exportBackup, validateBackup, importBackup } from '@/api/backup'

const stats = ref({})
const statsLoading = ref(false)
const exportLoading = ref(false)
const validateLoading = ref(false)
const restoreLoading = ref(false)
const selectedFile = ref(null)
const validationResult = ref(null)
const restoreResult = ref(null)
const restoreMode = ref('increment')
const uploadRef = ref(null)

// 表名映射
const getTableName = (table) => {
  const map = {
    members: '会员',
    coaches: '教练',
    courses: '课程',
    equipments: '器材',
    feedbacks: '反馈',
    consumeRecords: '消费记录'
  }
  return map[table] || table
}

const fetchStats = async () => {
  statsLoading.value = true
  try {
    const res = await getBackupStats()
    stats.value = res.data
  } finally {
    statsLoading.value = false
  }
}

const handleExport = async () => {
  exportLoading.value = true
  try {
    const res = await exportBackup()
    
    const blob = new Blob([res], { type: 'application/json' })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `fitness_backup_${new Date().toISOString().slice(0, 10)}.json`
    link.click()
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('数据备份导出成功')
  } catch (e) {
    ElMessage.error('导出失败')
  } finally {
    exportLoading.value = false
  }
}

const handleFileChange = (file) => {
  selectedFile.value = file.raw
  validationResult.value = null
  restoreResult.value = null
}

const handleExceed = () => {
  ElMessage.warning('只能选择一个文件')
}

const handleValidate = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择备份文件')
    return
  }
  
  validateLoading.value = true
  try {
    const res = await validateBackup(selectedFile.value)
    validationResult.value = res.data
    restoreResult.value = null
    ElMessage.success('备份文件验证成功')
  } catch (e) {
    ElMessage.error('验证失败：' + (e.message || '文件格式错误'))
  } finally {
    validateLoading.value = false
  }
}

const handleRestore = async () => {
  if (!selectedFile.value) {
    ElMessage.warning('请先选择备份文件')
    return
  }
  
  // 确认操作
  const modeText = restoreMode.value === 'increment' ? '增量导入' : '覆盖导入'
  const warningText = restoreMode.value === 'overwrite' 
    ? '覆盖导入会更新已存在的记录，此操作不可撤销！' 
    : '增量导入仅导入新记录，不会修改已有数据。'
  
  try {
    await ElMessageBox.confirm(
      `确定要执行${modeText}吗？${warningText}`,
      '确认恢复',
      {
        confirmButtonText: '确定恢复',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }
  
  restoreLoading.value = true
  try {
    const res = await importBackup(selectedFile.value, restoreMode.value)
    restoreResult.value = res.data
    
    if (res.data.success) {
      ElMessage.success(`数据恢复完成，共导入 ${res.data.totalImported} 条记录`)
      // 刷新统计
      fetchStats()
    } else {
      ElMessage.warning('数据恢复完成，但存在部分错误')
    }
  } catch (e) {
    ElMessage.error('恢复失败：' + (e.message || '未知错误'))
  } finally {
    restoreLoading.value = false
  }
}

const resetRestore = () => {
  validationResult.value = null
  restoreResult.value = null
  selectedFile.value = null
  restoreMode.value = 'increment'
  if (uploadRef.value) {
    uploadRef.value.clearFiles()
  }
}

onMounted(() => fetchStats())
</script>
