<template>
  <div class="min-h-screen bg-slate-100 flex items-center justify-center p-4">
    <!-- 登录卡片 -->
    <div class="relative w-full max-w-md">
      <div class="bg-white/90 backdrop-blur-xl rounded-2xl p-8 border border-slate-200 shadow-2xl shadow-slate-200/50">
        <!-- Logo 和标题 -->
        <div class="text-center mb-8">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-slate-50 border border-slate-200 mb-6">
            <span class="w-2 h-2 rounded-full bg-blue-500 animate-pulse"></span>
            <span class="text-xs font-medium text-slate-600 tracking-wide uppercase">健身管理平台</span>
          </div>
          <h1 class="text-4xl font-serif font-medium text-slate-900 tracking-tight mb-2">
            FitClub
          </h1>
          <p class="text-slate-500">健身俱乐部综合管理系统</p>
        </div>
        
        <!-- 登录表单 -->
        <el-form ref="formRef" :model="loginForm" :rules="rules" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input 
              v-model="loginForm.username" 
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              class="custom-input"
            />
          </el-form-item>
          
          <el-form-item prop="password">
            <el-input 
              v-model="loginForm.password" 
              type="password" 
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
              class="custom-input"
              @keyup.enter="handleLogin"
            />
          </el-form-item>
          
          <el-form-item class="mt-8">
            <el-button 
              type="primary" 
              size="large" 
              class="w-full !h-12 !rounded-lg !bg-slate-900 !border-0 hover:!bg-blue-500 !shadow-lg !shadow-slate-200 transition-all duration-300"
              :loading="loading"
              @click="handleLogin"
            >
              <span class="text-sm font-medium">{{ loading ? '登录中...' : '登 录' }}</span>
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 底部装饰 -->
        <div class="mt-8 flex items-center justify-center gap-4">
          <div class="h-px flex-1 bg-gradient-to-r from-transparent to-slate-200"></div>
          <span class="text-xs text-slate-400">安全登录</span>
          <div class="h-px flex-1 bg-gradient-to-l from-transparent to-slate-200"></div>
        </div>

        <!-- 信任指标 -->
        <div class="mt-6 flex items-center justify-center gap-3 text-slate-400">
          <iconify-icon icon="lucide:shield-check" width="16"></iconify-icon>
          <span class="text-xs">数据安全加密保护</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  
  loading.value = true
  try {
    await userStore.login(loginForm)
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 自定义输入框样式 */
.custom-input :deep(.el-input__wrapper) {
  background-color: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 0.5rem;
  box-shadow: none;
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper:hover) {
  border-color: #cbd5e1;
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
  background-color: #fff;
}

.custom-input :deep(.el-input__inner) {
  color: #1e293b;
  font-size: 15px;
}

.custom-input :deep(.el-input__inner::placeholder) {
  color: #94a3b8;
}

.custom-input :deep(.el-input__prefix) {
  color: #64748b;
}

.custom-input :deep(.el-input__suffix) {
  color: #64748b;
}

/* 表单项错误状态 */
:deep(.el-form-item.is-error .el-input__wrapper) {
  border-color: #ef4444 !important;
}

:deep(.el-form-item__error) {
  color: #ef4444;
}
</style>
