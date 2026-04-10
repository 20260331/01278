<template>
  <div class="space-y-8 w-full">
    <!-- 欢迎区 - Spectra 风格 -->
    <div class="bg-slate-900 rounded-[2rem] p-8 md:p-12 text-white relative overflow-hidden">
      <!-- 背景装饰 -->
      <div class="absolute top-0 right-0 -mt-20 -mr-20 w-96 h-96 bg-blue-500 rounded-full blur-[100px] opacity-20 pointer-events-none"></div>
      <div class="absolute bottom-0 left-0 -mb-20 -ml-20 w-96 h-96 bg-slate-500 rounded-full blur-[100px] opacity-20 pointer-events-none"></div>
      
      <div class="relative z-10">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-white/10 border border-white/20 mb-6">
          <span class="w-2 h-2 rounded-full bg-blue-400 animate-pulse"></span>
          <span class="text-xs font-medium text-white/80 tracking-wide uppercase">{{ getRoleText() }}</span>
        </div>
        <h2 class="text-3xl md:text-4xl font-serif font-medium mb-3 tracking-tight">
          {{ getGreeting() }}，{{ userStore.name || userStore.username }}
        </h2>
        <p class="text-slate-400 text-lg max-w-md">欢迎回来，今天也要保持活力！</p>
      </div>
    </div>

    <!-- 管理员统计 -->
    <template v-if="userStore.role === 'ROLE_ADMIN'">
      <!-- 统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatsCard icon="lucide:users" :value="stats.memberCount || 0" label="会员总数" color="blue" />
        <StatsCard icon="lucide:user-check" :value="stats.coachCount || 0" label="教练总数" color="emerald" />
        <StatsCard icon="lucide:calendar" :value="stats.todayCourseCount || 0" label="今日课程" color="amber" />
        <StatsCard icon="lucide:wallet" :value="formatMoney(stats.monthIncome)" label="本月收入" color="indigo" />
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 待办事项 -->
        <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-lg transition-all duration-300">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-serif text-slate-900">待办事项</h3>
            <span class="text-xs font-medium text-slate-400 uppercase tracking-wider">Today</span>
          </div>
          <div class="space-y-3">
            <div v-if="stats.pendingFeedback > 0" class="flex items-center justify-between p-4 bg-amber-50 rounded-xl group hover:bg-amber-100 transition-colors cursor-pointer">
              <div class="flex items-center gap-4">
                <div class="w-10 h-10 bg-white rounded-lg flex items-center justify-center shadow-sm">
                  <iconify-icon icon="lucide:message-square" width="20" class="text-amber-500"></iconify-icon>
                </div>
                <span class="text-sm font-medium text-slate-700">待回复反馈</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm font-bold text-amber-600">{{ stats.pendingFeedback }}</span>
                <iconify-icon icon="lucide:arrow-right" width="16" class="text-amber-400 group-hover:translate-x-1 transition-transform"></iconify-icon>
              </div>
            </div>
            <div v-if="stats.pendingLeave > 0" class="flex items-center justify-between p-4 bg-blue-50 rounded-xl group hover:bg-blue-100 transition-colors cursor-pointer">
              <div class="flex items-center gap-4">
                <div class="w-10 h-10 bg-white rounded-lg flex items-center justify-center shadow-sm">
                  <iconify-icon icon="lucide:clipboard-list" width="20" class="text-blue-500"></iconify-icon>
                </div>
                <span class="text-sm font-medium text-slate-700">待审核请假</span>
              </div>
              <div class="flex items-center gap-2">
                <span class="text-sm font-bold text-blue-600">{{ stats.pendingLeave }}</span>
                <iconify-icon icon="lucide:arrow-right" width="16" class="text-blue-400 group-hover:translate-x-1 transition-transform"></iconify-icon>
              </div>
            </div>
            <div v-if="!stats.pendingFeedback && !stats.pendingLeave" class="text-center py-8">
              <iconify-icon icon="lucide:check-circle" width="40" class="text-emerald-400 mb-3"></iconify-icon>
              <p class="text-slate-400 text-sm">暂无待办事项</p>
            </div>
          </div>
        </div>

        <!-- 高风险课程预警 -->
        <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-lg transition-all duration-300">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-serif text-slate-900">高风险课程预警</h3>
            <span class="text-xs font-medium text-slate-400 uppercase tracking-wider">24小时内</span>
          </div>
          <div v-if="highRiskCourses.length > 0" class="space-y-3">
            <div v-for="course in highRiskCourses.slice(0, 4)" :key="course.courseId"
                 class="flex items-center justify-between p-4 rounded-xl"
                 :class="getRiskBgClass(course.riskLevel)">
              <div class="flex items-center gap-4">
                <div class="w-10 h-10 rounded-lg flex items-center justify-center" :class="getRiskIconBgClass(course.riskLevel)">
                  <iconify-icon icon="lucide:alert-triangle" width="20" :class="getRiskIconClass(course.riskLevel)"></iconify-icon>
                </div>
                <div>
                  <p class="text-sm font-medium text-slate-700">{{ course.courseName }}</p>
                  <p class="text-xs text-slate-500">{{ course.startTime }} | {{ course.coachName }}</p>
                </div>
              </div>
              <div class="text-right">
                <span class="inline-flex items-center gap-1 px-2 py-1 rounded-full text-xs font-medium" :class="getRiskBadgeClass(course.riskLevel)">
                  {{ getRiskLevelText(course.riskLevel) }}
                </span>
                <p class="text-xs text-slate-500 mt-1">
                  候补{{ course.waitlistCount }}人 | 爽约率{{ course.noShowRate }}%
                </p>
              </div>
            </div>
          </div>
          <div v-else class="text-center py-8">
            <iconify-icon icon="lucide:shield-check" width="40" class="text-emerald-400 mb-3"></iconify-icon>
            <p class="text-slate-400 text-sm">暂无高风险课程</p>
          </div>
        </div>
      </div>

      <!-- 第二行 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
        <!-- 快捷入口 -->
        <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-lg transition-all duration-300">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-serif text-slate-900">快捷操作</h3>
            <a href="#" class="text-xs font-medium text-blue-500 hover:text-blue-600 transition-colors">查看全部</a>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <router-link to="/member" class="group p-5 bg-slate-50 rounded-xl hover:bg-slate-100 hover:shadow-md transition-all duration-300">
              <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center mb-4 shadow-sm group-hover:scale-110 transition-transform">
                <iconify-icon icon="lucide:user-circle" width="24" class="text-blue-500"></iconify-icon>
              </div>
              <h4 class="font-medium text-slate-900 mb-1">会员管理</h4>
              <p class="text-xs text-slate-500">管理会员信息</p>
            </router-link>
            <router-link to="/course" class="group p-5 bg-slate-50 rounded-xl hover:bg-slate-100 hover:shadow-md transition-all duration-300">
              <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center mb-4 shadow-sm group-hover:scale-110 transition-transform">
                <iconify-icon icon="lucide:calendar" width="24" class="text-emerald-500"></iconify-icon>
              </div>
              <h4 class="font-medium text-slate-900 mb-1">课程管理</h4>
              <p class="text-xs text-slate-500">安排课程计划</p>
            </router-link>
            <router-link to="/finance" class="group p-5 bg-slate-50 rounded-xl hover:bg-slate-100 hover:shadow-md transition-all duration-300">
              <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center mb-4 shadow-sm group-hover:scale-110 transition-transform">
                <iconify-icon icon="lucide:wallet" width="24" class="text-amber-500"></iconify-icon>
              </div>
              <h4 class="font-medium text-slate-900 mb-1">财务管理</h4>
              <p class="text-xs text-slate-500">收支明细查询</p>
            </router-link>
            <router-link to="/equipment" class="group p-5 bg-slate-50 rounded-xl hover:bg-slate-100 hover:shadow-md transition-all duration-300">
              <div class="w-12 h-12 bg-white rounded-xl flex items-center justify-center mb-4 shadow-sm group-hover:scale-110 transition-transform">
                <iconify-icon icon="lucide:dumbbell" width="24" class="text-rose-500"></iconify-icon>
              </div>
              <h4 class="font-medium text-slate-900 mb-1">器材管理</h4>
              <p class="text-xs text-slate-500">设备维护记录</p>
            </router-link>
          </div>
        </div>
      </div>
    </template>

    <!-- 教练首页 -->
    <template v-if="userStore.role === 'ROLE_COACH'">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <StatsCard icon="lucide:calendar" :value="coachStats.courseCount || 0" label="本月课程" color="blue" />
        <StatsCard icon="lucide:users" :value="coachStats.totalStudents || 0" label="学员人次" color="emerald" />
        <StatsCard icon="lucide:wallet" :value="formatMoney(coachStats.totalIncome)" label="课程收入" color="amber" />
      </div>
      
      <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm">
        <h3 class="text-xl font-serif text-slate-900 mb-6">快捷操作</h3>
        <div class="grid grid-cols-3 gap-4">
          <router-link to="/my-course" class="group flex flex-col items-center gap-3 p-6 bg-slate-50 rounded-xl hover:bg-blue-50 hover:shadow-md transition-all duration-300">
            <div class="w-14 h-14 bg-white rounded-xl flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform">
              <iconify-icon icon="lucide:calendar-check" width="28" class="text-blue-500"></iconify-icon>
            </div>
            <span class="text-sm font-medium text-slate-700">我的课程</span>
          </router-link>
          <router-link to="/my-members" class="group flex flex-col items-center gap-3 p-6 bg-slate-50 rounded-xl hover:bg-emerald-50 hover:shadow-md transition-all duration-300">
            <div class="w-14 h-14 bg-white rounded-xl flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform">
              <iconify-icon icon="lucide:users" width="28" class="text-emerald-500"></iconify-icon>
            </div>
            <span class="text-sm font-medium text-slate-700">会员跟进</span>
          </router-link>
          <router-link to="/performance" class="group flex flex-col items-center gap-3 p-6 bg-slate-50 rounded-xl hover:bg-amber-50 hover:shadow-md transition-all duration-300">
            <div class="w-14 h-14 bg-white rounded-xl flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform">
              <iconify-icon icon="lucide:star" width="28" class="text-amber-500"></iconify-icon>
            </div>
            <span class="text-sm font-medium text-slate-700">业绩查询</span>
          </router-link>
        </div>
      </div>
    </template>

    <!-- 会员首页 -->
    <template v-if="userStore.role === 'ROLE_MEMBER'">
      <!-- 到期提醒横幅 -->
      <div v-if="memberStats.isExpired" class="bg-rose-500 text-white rounded-2xl p-6 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center">
            <iconify-icon icon="lucide:alert-circle" width="28"></iconify-icon>
          </div>
          <div>
            <h4 class="font-medium text-lg">会员已过期</h4>
            <p class="text-rose-100 text-sm">您的会员已于 {{ memberStats.expireDate }} 过期，请尽快续费以继续享受会员权益</p>
          </div>
        </div>
        <router-link to="/consume-record" class="px-6 py-2.5 bg-white text-rose-500 rounded-lg font-medium hover:bg-rose-50 transition-colors">
          立即续费
        </router-link>
      </div>
      <div v-else-if="memberStats.isExpiringSoon" class="bg-amber-500 text-white rounded-2xl p-6 flex items-center justify-between">
        <div class="flex items-center gap-4">
          <div class="w-12 h-12 bg-white/20 rounded-xl flex items-center justify-center">
            <iconify-icon icon="lucide:clock" width="28"></iconify-icon>
          </div>
          <div>
            <h4 class="font-medium text-lg">会员即将到期</h4>
            <p class="text-amber-100 text-sm">您的会员将于 {{ memberStats.daysUntilExpire }} 天后到期（{{ memberStats.expireDate }}），请及时续费</p>
          </div>
        </div>
        <router-link to="/consume-record" class="px-6 py-2.5 bg-white text-amber-500 rounded-lg font-medium hover:bg-amber-50 transition-colors">
          立即续费
        </router-link>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
        <!-- 会员信息卡片 -->
        <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-lg transition-all duration-300">
          <div class="flex items-center justify-between mb-6">
            <h3 class="text-xl font-serif text-slate-900">会员信息</h3>
            <span class="inline-flex items-center gap-1 px-3 py-1 rounded-full text-xs font-medium" :class="getLevelClass(memberStats?.level)">
              <iconify-icon icon="lucide:crown" width="14"></iconify-icon>
              {{ getLevelText(memberStats?.level) }}
            </span>
          </div>
          <div class="space-y-4">
            <div class="flex justify-between items-center p-4 bg-slate-50 rounded-xl">
              <div class="flex items-center gap-3">
                <iconify-icon icon="lucide:wallet" width="20" class="text-emerald-500"></iconify-icon>
                <span class="text-slate-600">账户余额</span>
              </div>
              <span class="text-xl font-bold text-emerald-600">¥{{ memberStats?.balance || 0 }}</span>
            </div>
            <div class="flex justify-between items-center p-4 rounded-xl" :class="memberStats.isExpired ? 'bg-rose-50' : memberStats.isExpiringSoon ? 'bg-amber-50' : 'bg-slate-50'">
              <div class="flex items-center gap-3">
                <iconify-icon icon="lucide:calendar" width="20" :class="memberStats.isExpired ? 'text-rose-500' : memberStats.isExpiringSoon ? 'text-amber-500' : 'text-blue-500'"></iconify-icon>
                <span class="text-slate-600">到期时间</span>
              </div>
              <span class="font-medium" :class="memberStats.isExpired ? 'text-rose-600' : memberStats.isExpiringSoon ? 'text-amber-600' : 'text-slate-900'">
                {{ memberStats?.expireDate || '-' }}
                <span v-if="memberStats.isExpired" class="ml-2 text-xs text-rose-500">已过期</span>
                <span v-else-if="memberStats.isExpiringSoon" class="ml-2 text-xs text-amber-500">{{ memberStats.daysUntilExpire }}天后到期</span>
              </span>
            </div>
            <div class="flex justify-between items-center p-4 bg-slate-50 rounded-xl">
              <div class="flex items-center gap-3">
                <iconify-icon icon="lucide:calendar-check" width="20" class="text-indigo-500"></iconify-icon>
                <span class="text-slate-600">已完成课程</span>
              </div>
              <span class="font-medium text-slate-900">{{ memberStats?.completedCount || 0 }} 节</span>
            </div>
          </div>
        </div>

        <!-- 快捷操作 -->
        <div class="bg-white rounded-2xl border border-slate-100 p-6 shadow-sm hover:shadow-lg transition-all duration-300">
          <h3 class="text-xl font-serif text-slate-900 mb-6">快捷操作</h3>
          <div class="grid grid-cols-2 gap-4">
            <router-link to="/course-reserve" class="group flex flex-col items-center gap-3 p-6 bg-blue-50 rounded-xl hover:bg-blue-100 hover:shadow-md transition-all duration-300">
              <div class="w-14 h-14 bg-white rounded-xl flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform">
                <iconify-icon icon="lucide:calendar" width="28" class="text-blue-500"></iconify-icon>
              </div>
              <span class="text-sm font-medium text-slate-700">预约课程</span>
            </router-link>
            <router-link to="/fitness-record" class="group flex flex-col items-center gap-3 p-6 bg-emerald-50 rounded-xl hover:bg-emerald-100 hover:shadow-md transition-all duration-300">
              <div class="w-14 h-14 bg-white rounded-xl flex items-center justify-center shadow-sm group-hover:scale-110 transition-transform">
                <iconify-icon icon="lucide:activity" width="28" class="text-emerald-500"></iconify-icon>
              </div>
              <span class="text-sm font-medium text-slate-700">健身记录</span>
            </router-link>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/store/modules/user'
import { getAdminStatistics, getCoachPerformance, getMemberStatistics, getHighRiskCourses } from '@/api/statistics'
import StatsCard from '@/components/common/StatsCard.vue'
import dayjs from 'dayjs'

const userStore = useUserStore()

const stats = ref({})
const coachStats = ref({})
const memberStats = ref({})
const highRiskCourses = ref([])

const getGreeting = () => {
  const hour = new Date().getHours()
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
}

const getRoleText = () => {
  const roles = {
    'ROLE_ADMIN': '系统管理员',
    'ROLE_COACH': '教练',
    'ROLE_MEMBER': '会员'
  }
  return roles[userStore.role] || ''
}

const getLevelText = (level) => {
  const levels = { 1: '普通会员', 2: '银卡会员', 3: '金卡会员', 4: '钻石会员' }
  return levels[level] || '普通会员'
}

const getLevelClass = (level) => {
  const classes = {
    1: 'bg-slate-100 text-slate-600',
    2: 'bg-slate-200 text-slate-700',
    3: 'bg-amber-100 text-amber-700',
    4: 'bg-blue-100 text-blue-700'
  }
  return classes[level] || classes[1]
}

const formatMoney = (value) => {
  return '¥' + (value || 0).toLocaleString()
}

const getRiskLevelText = (level) => {
  const texts = { 0: '无风险', 1: '低风险', 2: '中风险', 3: '高风险' }
  return texts[level] || '未知'
}

const getRiskBgClass = (level) => {
  const classes = {
    0: 'bg-emerald-50',
    1: 'bg-amber-50',
    2: 'bg-orange-50',
    3: 'bg-rose-50'
  }
  return classes[level] || 'bg-slate-50'
}

const getRiskIconBgClass = (level) => {
  const classes = {
    0: 'bg-emerald-100',
    1: 'bg-amber-100',
    2: 'bg-orange-100',
    3: 'bg-rose-100'
  }
  return classes[level] || 'bg-slate-100'
}

const getRiskIconClass = (level) => {
  const classes = {
    0: 'text-emerald-500',
    1: 'text-amber-500',
    2: 'text-orange-500',
    3: 'text-rose-500'
  }
  return classes[level] || 'text-slate-500'
}

const getRiskBadgeClass = (level) => {
  const classes = {
    0: 'bg-emerald-100 text-emerald-700',
    1: 'bg-amber-100 text-amber-700',
    2: 'bg-orange-100 text-orange-700',
    3: 'bg-rose-100 text-rose-700'
  }
  return classes[level] || 'bg-slate-100 text-slate-700'
}

onMounted(async () => {
  if (userStore.role === 'ROLE_ADMIN') {
    const res = await getAdminStatistics()
    stats.value = res.data
    const riskRes = await getHighRiskCourses()
    highRiskCourses.value = riskRes.data
  } else if (userStore.role === 'ROLE_COACH') {
    const startDate = dayjs().startOf('month').format('YYYY-MM-DD')
    const endDate = dayjs().endOf('month').format('YYYY-MM-DD')
    const res = await getCoachPerformance(startDate, endDate)
    coachStats.value = res.data
  } else if (userStore.role === 'ROLE_MEMBER') {
    const res = await getMemberStatistics()
    memberStats.value = res.data
  }
})
</script>
