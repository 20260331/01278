<template>
  <div class="min-h-screen bg-slate-100">
    <!-- 顶部导航栏 - Spectra 风格 -->
    <nav class="fixed w-full z-50 bg-slate-100/80 backdrop-blur-md border-b border-slate-200 transition-all duration-300">
      <div class="flex h-20 max-w-full mx-auto px-6 items-center justify-between">
        <!-- 移动端菜单按钮 -->
        <button @click="sidebarOpen = !sidebarOpen" class="lg:hidden text-slate-600 hover:text-blue-500 transition-colors">
          <iconify-icon icon="lucide:menu" width="24"></iconify-icon>
        </button>

        <!-- Logo -->
        <a href="#" class="text-2xl font-serif tracking-tighter text-slate-900 font-medium">
          FitClub
        </a>

        <!-- 右侧图标区 -->
        <div class="flex items-center gap-5">
          <!-- 候补补位通知图标（仅会员） -->
          <div v-if="userStore.role === 'ROLE_MEMBER'" class="relative cursor-pointer" @click="checkWaitingNotifications">
            <iconify-icon icon="lucide:bell" width="20" class="text-slate-600 hover:text-blue-500 transition-colors"></iconify-icon>
            <div 
              v-if="unreadNotifyCount > 0" 
              class="absolute -top-1 -right-1 w-4 h-4 bg-rose-500 rounded-full text-white text-xs flex items-center justify-center animate-pulse"
            >
              {{ unreadNotifyCount }}
            </div>
          </div>
          
          <!-- 用户下拉菜单 -->
          <el-dropdown @command="handleCommand" trigger="click">
            <div class="flex items-center gap-3 cursor-pointer group">
              <div class="w-9 h-9 bg-slate-900 rounded-full flex items-center justify-center group-hover:bg-blue-500 transition-colors">
                <span class="text-white font-medium text-sm">{{ (userStore.name || userStore.username || 'U').charAt(0).toUpperCase() }}</span>
              </div>
              <div class="hidden md:block">
                <p class="text-sm font-medium text-slate-900">{{ userStore.name || userStore.username }}</p>
                <p class="text-xs text-slate-500">{{ getRoleText(userStore.role) }}</p>
              </div>
              <iconify-icon icon="lucide:chevron-down" width="16" class="text-slate-400 hidden md:block"></iconify-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <div class="flex items-center gap-2">
                    <iconify-icon icon="lucide:user" width="16"></iconify-icon>
                    个人信息
                  </div>
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <div class="flex items-center gap-2 text-rose-500">
                    <iconify-icon icon="lucide:log-out" width="16"></iconify-icon>
                    退出登录
                  </div>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </nav>

    <!-- 侧边栏 -->
    <aside 
      class="fixed left-0 top-20 h-[calc(100vh-5rem)] w-64 bg-white border-r border-slate-200 z-40 transform transition-transform duration-300 lg:translate-x-0"
      :class="sidebarOpen ? 'translate-x-0' : '-translate-x-full'"
    >
      <!-- 当前页面标题 -->
      <div class="p-6 border-b border-slate-100">
        <span class="text-blue-500 text-xs font-bold tracking-wider uppercase">{{ $route.meta.category || '导航' }}</span>
        <h2 class="text-xl font-serif text-slate-900 mt-1">{{ $route.meta.title }}</h2>
      </div>
      
      <!-- 导航菜单 -->
      <nav class="p-4 space-y-1 overflow-y-auto no-scrollbar" style="height: calc(100% - 100px);">
        <template v-for="item in menuItems" :key="item.path">
          <router-link 
            v-if="item.roles.includes(userStore.role)"
            :to="item.path"
            class="group flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-medium transition-all duration-200"
            :class="$route.path === item.path 
              ? 'bg-slate-900 text-white shadow-lg shadow-slate-200' 
              : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'"
            @click="sidebarOpen = false"
          >
            <div 
              class="w-8 h-8 rounded-lg flex items-center justify-center transition-all"
              :class="$route.path === item.path 
                ? 'bg-white/20' 
                : 'bg-slate-50 group-hover:bg-blue-50'"
            >
              <iconify-icon 
                :icon="item.icon" 
                width="18"
                :class="$route.path === item.path ? 'text-white' : 'text-blue-500'"
              ></iconify-icon>
            </div>
            {{ item.name }}
          </router-link>
        </template>
      </nav>
    </aside>

    <!-- 遮罩层（移动端） -->
    <div 
      v-if="sidebarOpen" 
      class="fixed inset-0 bg-slate-900/20 backdrop-blur-sm z-30 lg:hidden"
      @click="sidebarOpen = false"
    ></div>
    
    <!-- 主内容区 -->
    <div class="lg:ml-64 pt-20">
      <!-- 页面内容 -->
      <main class="p-6">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 个人信息弹窗 -->
    <el-dialog v-model="profileDialogVisible" title="个人信息" width="500px" destroy-on-close align-center>
      <!-- 用户头像和基本信息 -->
      <div class="flex items-center gap-4 mb-6 pb-6 border-b border-slate-200">
        <div class="w-16 h-16 bg-slate-900 rounded-full flex items-center justify-center">
          <span class="text-2xl font-bold text-white">{{ (userStore.name || userStore.username || 'U').charAt(0).toUpperCase() }}</span>
        </div>
        <div>
          <h3 class="text-lg font-serif text-slate-900">{{ userStore.name || userStore.username }}</h3>
          <p class="text-sm text-slate-500">{{ getRoleText(userStore.role) }}</p>
        </div>
      </div>

      <!-- Tab 切换 -->
      <el-tabs v-model="profileActiveTab">
        <!-- 基本信息 Tab -->
        <el-tab-pane label="基本信息" name="profile">
          <!-- 会员基本信息编辑（仅会员角色显示） -->
          <div v-if="userStore.role === 'ROLE_MEMBER'">
            <div v-if="memberDetailLoading" class="text-center py-4">
              <el-icon class="is-loading"><iconify-icon icon="lucide:loader-2" /></el-icon>
              <span class="ml-2 text-slate-500">加载中...</span>
            </div>
            <el-form v-else ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="80px" class="pt-2.5">
              <el-form-item label="姓名" prop="name">
                <el-input v-model="profileForm.name" placeholder="请输入姓名" />
              </el-form-item>
              <el-form-item label="性别" prop="gender">
                <el-radio-group v-model="profileForm.gender">
                  <el-radio :value="1">男</el-radio>
                  <el-radio :value="0">女</el-radio>
                </el-radio-group>
              </el-form-item>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="profileForm.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
            </el-form>
          </div>
          <!-- 非会员角色显示提示 -->
          <div v-else class="text-center py-8 text-slate-500">
            <iconify-icon icon="lucide:user" class="text-4xl mb-2"></iconify-icon>
            <p>当前角色暂无可编辑的基本信息</p>
          </div>
        </el-tab-pane>

        <!-- 会员套餐 Tab（仅会员角色显示） -->
        <el-tab-pane v-if="userStore.role === 'ROLE_MEMBER'" label="会员套餐" name="membership">
          <div v-if="memberInfoLoading" class="text-center py-4">
            <el-icon class="is-loading"><iconify-icon icon="lucide:loader-2" /></el-icon>
            <span class="ml-2 text-slate-500">加载中...</span>
          </div>
          <div v-else-if="memberInfo" class="space-y-3">
            <!-- 到期提醒横幅 -->
            <el-alert
              v-if="memberInfo.isExpired"
              title="您的会员套餐已过期"
              type="error"
              :closable="false"
              show-icon
            >
              <template #default>
                <span>套餐已于 {{ memberInfo.expireDate }} 到期，请及时续费</span>
              </template>
            </el-alert>
            <el-alert
              v-else-if="memberInfo.isExpiringSoon"
              :title="`套餐即将到期（剩余 ${memberInfo.daysUntilExpire} 天）`"
              type="warning"
              :closable="false"
              show-icon
            >
              <template #default>
                <span>到期日期：{{ memberInfo.expireDate }}，请及时续费</span>
              </template>
            </el-alert>
            <!-- 套餐详情 -->
            <div class="grid grid-cols-2 gap-4 mt-4">
              <div class="bg-slate-50 rounded-lg p-3">
                <p class="text-xs text-slate-500 mb-1">会员等级</p>
                <p class="text-lg font-semibold text-slate-900">{{ getLevelText(memberInfo.level) }}</p>
              </div>
              <div class="bg-slate-50 rounded-lg p-3">
                <p class="text-xs text-slate-500 mb-1">账户余额</p>
                <p class="text-lg font-semibold text-blue-600">¥{{ memberInfo.balance?.toFixed(2) || '0.00' }}</p>
              </div>
              <div class="bg-slate-50 rounded-lg p-3">
                <p class="text-xs text-slate-500 mb-1">到期日期</p>
                <p class="text-lg font-semibold" :class="memberInfo.isExpired ? 'text-red-500' : memberInfo.isExpiringSoon ? 'text-orange-500' : 'text-slate-900'">
                  {{ memberInfo.expireDate || '未设置' }}
                </p>
              </div>
              <div class="bg-slate-50 rounded-lg p-3">
                <p class="text-xs text-slate-500 mb-1">已完成课程</p>
                <p class="text-lg font-semibold text-slate-900">{{ memberInfo.completedCount || 0 }} 节</p>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-4 text-slate-500">
            无法获取会员信息
          </div>
        </el-tab-pane>

        <!-- 修改密码 Tab -->
        <el-tab-pane label="修改密码" name="password">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="80px" class="pt-2">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <template #footer>
        <el-button @click="profileDialogVisible = false">取消</el-button>
        <el-button v-if="profileActiveTab === 'profile' && userStore.role === 'ROLE_MEMBER'" type="primary" @click="handleUpdateProfile" :loading="profileLoading">保存信息</el-button>
        <el-button v-if="profileActiveTab === 'password'" type="primary" @click="handleUpdatePassword" :loading="passwordLoading">修改密码</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import { useUserStore } from '@/store/modules/user'
import { updatePassword } from '@/api/auth'
import { getMemberStatistics } from '@/api/statistics'
import { getCurrentMember, updateMember } from '@/api/member'
import waitingQueueApi from '@/api/waitingQueue'
import { onMounted, onUnmounted } from 'vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarOpen = ref(false)
const unreadNotifyCount = ref(0)
let notificationPollingTimer = null
const notifiedIds = ref(new Set())

// 个人信息弹窗
const profileDialogVisible = ref(false)
const profileActiveTab = ref('profile')
const passwordLoading = ref(false)
const passwordFormRef = ref(null)

// 会员套餐信息
const memberInfo = ref(null)
const memberInfoLoading = ref(false)

// 会员基本信息编辑
const memberDetail = ref(null)
const memberDetailLoading = ref(false)
const profileLoading = ref(false)
const profileFormRef = ref(null)
const profileForm = reactive({
  id: null,
  name: '',
  gender: 1,
  phone: ''
})

const profileRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 获取会员等级文本
const getLevelText = (level) => {
  const levelMap = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员', 4: '钻石会员' }
  return levelMap[level] || '普通会员'
}

// 获取会员详细信息（用于编辑）
const fetchMemberDetail = async () => {
  if (userStore.role !== 'ROLE_MEMBER') return
  
  memberDetailLoading.value = true
  try {
    const res = await getCurrentMember()
    memberDetail.value = res.data
    // 填充表单
    if (res.data) {
      profileForm.id = res.data.id
      profileForm.name = res.data.name || ''
      profileForm.gender = res.data.gender ?? 1
      profileForm.phone = res.data.phone || ''
    }
  } catch (error) {
    console.error('获取会员详情失败:', error)
  } finally {
    memberDetailLoading.value = false
  }
}

// 获取会员套餐信息
const fetchMemberInfo = async () => {
  if (userStore.role !== 'ROLE_MEMBER') return
  
  memberInfoLoading.value = true
  try {
    const res = await getMemberStatistics()
    memberInfo.value = res.data
  } catch (error) {
    console.error('获取会员信息失败:', error)
  } finally {
    memberInfoLoading.value = false
  }
}

// 更新会员个人信息
const handleUpdateProfile = async () => {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  profileLoading.value = true
  try {
    await updateMember({
      id: profileForm.id,
      name: profileForm.name,
      gender: profileForm.gender,
      phone: profileForm.phone
    })
    ElMessage.success('个人信息修改成功')
    // 更新 store 中的用户名
    userStore.userInfo.name = profileForm.name
    localStorage.setItem('user', JSON.stringify(userStore.userInfo))
  } catch (error) {
    ElMessage.error('修改失败：' + (error.message || '未知错误'))
  } finally {
    profileLoading.value = false
  }
}

// 监听弹窗打开，获取会员信息
watch(profileDialogVisible, (visible) => {
  if (visible && userStore.role === 'ROLE_MEMBER') {
    fetchMemberDetail()
    fetchMemberInfo()
  }
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const getRoleText = (role) => ({ 'ROLE_ADMIN': '系统管理员', 'ROLE_COACH': '教练', 'ROLE_MEMBER': '会员' }[role] || '-')

const resetPasswordForm = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

const handleUpdatePassword = async () => {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return
  
  passwordLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码修改成功，请重新登录')
    profileDialogVisible.value = false
    userStore.logout()
  } finally {
    passwordLoading.value = false
  }
}

// 侧边栏菜单
const menuItems = [
  { path: '/dashboard', name: '首页', icon: 'lucide:layout-dashboard', roles: ['ROLE_ADMIN', 'ROLE_COACH', 'ROLE_MEMBER'] },
  // 管理员菜单
  { path: '/user', name: '用户管理', icon: 'lucide:users', roles: ['ROLE_ADMIN'] },
  { path: '/member', name: '会员管理', icon: 'lucide:user-circle', roles: ['ROLE_ADMIN'] },
  { path: '/coach', name: '教练管理', icon: 'lucide:user-check', roles: ['ROLE_ADMIN'] },
  { path: '/course', name: '课程管理', icon: 'lucide:calendar', roles: ['ROLE_ADMIN'] },
  { path: '/equipment', name: '器材管理', icon: 'lucide:dumbbell', roles: ['ROLE_ADMIN'] },
  { path: '/venue', name: '场地管理', icon: 'lucide:map-pin', roles: ['ROLE_ADMIN'] },
  { path: '/finance', name: '财务管理', icon: 'lucide:wallet', roles: ['ROLE_ADMIN'] },
  { path: '/salary', name: '薪资结算', icon: 'lucide:banknote', roles: ['ROLE_ADMIN'] },
  { path: '/leave-audit', name: '请假审核', icon: 'lucide:clipboard-list', roles: ['ROLE_ADMIN'] },
  { path: '/feedback-manage', name: '反馈管理', icon: 'lucide:message-square', roles: ['ROLE_ADMIN'] },
  { path: '/system-log', name: '系统日志', icon: 'lucide:file-text', roles: ['ROLE_ADMIN'] },
  { path: '/data-backup', name: '数据备份', icon: 'lucide:database', roles: ['ROLE_ADMIN'] },
  // 教练菜单
  { path: '/my-course', name: '我的课程', icon: 'lucide:calendar-check', roles: ['ROLE_COACH'] },
  { path: '/my-members', name: '会员跟进', icon: 'lucide:users', roles: ['ROLE_COACH'] },
  { path: '/performance', name: '业绩查询', icon: 'lucide:star', roles: ['ROLE_COACH'] },
  // 会员菜单
  { path: '/course-reserve', name: '课程预约', icon: 'lucide:calendar', roles: ['ROLE_MEMBER'] },
  { path: '/my-reservation', name: '我的预约', icon: 'lucide:calendar-check', roles: ['ROLE_MEMBER'] },
  { path: '/consume-record', name: '消费记录', icon: 'lucide:wallet', roles: ['ROLE_MEMBER'] },
  { path: '/fitness-record', name: '健身记录', icon: 'lucide:activity', roles: ['ROLE_MEMBER'] },
  { path: '/feedback', name: '意见反馈', icon: 'lucide:message-square', roles: ['ROLE_MEMBER'] },
  { path: '/my-waiting', name: '我的候补', icon: 'lucide:list-orders', roles: ['ROLE_MEMBER'] },
]

const fetchUnreadNotifications = async () => {
  if (userStore.role !== 'ROLE_MEMBER') return
  
  try {
    const res = await waitingQueueApi.getUnreadNotified()
    const notifications = res.data || []
    
    unreadNotifyCount.value = notifications.length
    
    for (const notify of notifications) {
      if (!notifiedIds.value.has(notify.id)) {
        notifiedIds.value.add(notify.id)
        showWaitingNotification(notify)
      }
    }
  } catch (error) {
    console.error('获取候补通知失败:', error)
  }
}

const showWaitingNotification = (notify) => {
  ElNotification({
    title: '🎉 候补补位成功！',
    dangerouslyUseHTMLString: true,
    message: `
      <div class="py-2">
        <p class="text-slate-600 mb-2">恭喜您，课程 <strong class="text-blue-600">${notify.courseName || '未知课程'}</strong> 已候补成功！</p>
        <div class="flex gap-3 mt-3">
          <button 
            onclick="window.router.push('/my-reservation')" 
            class="px-3 py-1.5 bg-blue-500 text-white rounded-lg text-xs hover:bg-blue-600 transition-colors"
          >查看预约</button>
          <button 
            id="mark-read-${notify.id}"
            class="px-3 py-1.5 bg-slate-100 text-slate-600 rounded-lg text-xs hover:bg-slate-200 transition-colors"
          >我知道了</button>
        </div>
      </div>
    `,
    type: 'success',
    duration: 0,
    position: 'top-right',
    onClose: async () => {
      await waitingQueueApi.markAsRead(notify.id)
      unreadNotifyCount.value = Math.max(0, unreadNotifyCount.value - 1)
      notifiedIds.value.delete(notify.id)
    }
  })
  
  setTimeout(() => {
    const btn = document.getElementById(`mark-read-${notify.id}`)
    if (btn) {
      btn.onclick = async () => {
        await waitingQueueApi.markAsRead(notify.id)
        unreadNotifyCount.value = Math.max(0, unreadNotifyCount.value - 1)
        notifiedIds.value.delete(notify.id)
        document.querySelector('.el-notification').__vue__.close()
      }
    }
  }, 100)
}

const checkWaitingNotifications = async () => {
  if (unreadNotifyCount.value === 0) {
    ElMessage.info('暂无新的补位通知')
  }
}

const startNotificationPolling = () => {
  if (userStore.role !== 'ROLE_MEMBER') return
  
  fetchUnreadNotifications()
  notificationPollingTimer = setInterval(fetchUnreadNotifications, 10000)
}

const stopNotificationPolling = () => {
  if (notificationPollingTimer) {
    clearInterval(notificationPollingTimer)
    notificationPollingTimer = null
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    stopNotificationPolling()
    userStore.logout()
  } else if (command === 'profile') {
    resetPasswordForm()
    profileDialogVisible.value = true
  }
}

onMounted(() => {
  window.router = router
  if (userStore.role === 'ROLE_MEMBER') {
    startNotificationPolling()
  }
})

onUnmounted(() => {
  stopNotificationPolling()
})
</script>
