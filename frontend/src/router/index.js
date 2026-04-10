import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      // 管理员路由
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '首页', roles: ['ROLE_ADMIN', 'ROLE_COACH', 'ROLE_MEMBER'] }
      },
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'member',
        name: 'MemberManage',
        component: () => import('@/views/admin/MemberManage.vue'),
        meta: { title: '会员管理', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'coach',
        name: 'CoachManage',
        component: () => import('@/views/admin/CoachManage.vue'),
        meta: { title: '教练管理', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'course',
        name: 'CourseManage',
        component: () => import('@/views/admin/CourseManage.vue'),
        meta: { title: '课程管理', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'equipment',
        name: 'EquipmentManage',
        component: () => import('@/views/admin/EquipmentManage.vue'),
        meta: { title: '器材管理', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'venue',
        name: 'VenueManage',
        component: () => import('@/views/admin/VenueManage.vue'),
        meta: { title: '场地管理', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'finance',
        name: 'FinanceManage',
        component: () => import('@/views/admin/FinanceManage.vue'),
        meta: { title: '财务管理', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'salary',
        name: 'SalaryManage',
        component: () => import('@/views/admin/SalaryManage.vue'),
        meta: { title: '薪资结算', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'leave-audit',
        name: 'LeaveAudit',
        component: () => import('@/views/admin/LeaveAudit.vue'),
        meta: { title: '请假审核', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'feedback-manage',
        name: 'FeedbackManage',
        component: () => import('@/views/admin/FeedbackManage.vue'),
        meta: { title: '反馈管理', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'system-log',
        name: 'SystemLog',
        component: () => import('@/views/admin/SystemLog.vue'),
        meta: { title: '系统日志', roles: ['ROLE_ADMIN'] }
      },
      {
        path: 'data-backup',
        name: 'DataBackup',
        component: () => import('@/views/admin/DataBackup.vue'),
        meta: { title: '数据备份', roles: ['ROLE_ADMIN'] }
      },
      // 教练路由
      {
        path: 'my-course',
        name: 'MyCourse',
        component: () => import('@/views/coach/MyCourse.vue'),
        meta: { title: '我的课程', roles: ['ROLE_COACH'] }
      },
      {
        path: 'my-members',
        name: 'MyMembers',
        component: () => import('@/views/coach/MyMembers.vue'),
        meta: { title: '会员跟进', roles: ['ROLE_COACH'] }
      },
      {
        path: 'performance',
        name: 'Performance',
        component: () => import('@/views/coach/Performance.vue'),
        meta: { title: '业绩查询', roles: ['ROLE_COACH'] }
      },
      // 会员路由
      {
        path: 'course-reserve',
        name: 'CourseReserve',
        component: () => import('@/views/member/CourseReserve.vue'),
        meta: { title: '课程预约', roles: ['ROLE_MEMBER'] }
      },
      {
        path: 'my-reservation',
        name: 'MyReservation',
        component: () => import('@/views/member/MyReservation.vue'),
        meta: { title: '我的预约', roles: ['ROLE_MEMBER'] }
      },
      {
        path: 'consume-record',
        name: 'ConsumeRecord',
        component: () => import('@/views/member/ConsumeRecord.vue'),
        meta: { title: '消费记录', roles: ['ROLE_MEMBER'] }
      },
      {
        path: 'fitness-record',
        name: 'FitnessRecord',
        component: () => import('@/views/member/FitnessRecord.vue'),
        meta: { title: '健身记录', roles: ['ROLE_MEMBER'] }
      },
      {
        path: 'feedback',
        name: 'Feedback',
        component: () => import('@/views/member/Feedback.vue'),
        meta: { title: '意见反馈', roles: ['ROLE_MEMBER'] }
      },
      {
        path: 'my-waiting',
        name: 'MyWaiting',
        component: () => import('@/views/member/MyWaiting.vue'),
        meta: { title: '我的候补', category: '会员中心', roles: ['ROLE_MEMBER'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || '{}')
  
  if (to.path === '/login') {
    if (token) {
      next('/')
    } else {
      next()
    }
  } else {
    if (!token) {
      next('/login')
    } else {
      // 检查权限
      const roles = to.meta?.roles
      if (roles && !roles.includes(user.role)) {
        // 权限不足时显示提示消息
        import('element-plus').then(({ ElMessage }) => {
          ElMessage.warning('您没有权限访问该页面')
        })
        next('/dashboard')
      } else {
        next()
      }
    }
  }
})

export default router
