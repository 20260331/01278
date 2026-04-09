import { defineStore } from 'pinia'
import { login, getUserInfo, logout } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    userInfo: JSON.parse(localStorage.getItem('user') || '{}')
  }),
  
  getters: {
    isLoggedIn: (state) => !!state.token,
    role: (state) => state.userInfo?.role || '',
    username: (state) => state.userInfo?.username || '',
    name: (state) => state.userInfo?.name || ''
  },
  
  actions: {
    async login(loginForm) {
      const res = await login(loginForm)
      this.token = res.data.token
      this.userInfo = res.data
      localStorage.setItem('token', res.data.token)
      localStorage.setItem('user', JSON.stringify(res.data))
      return res.data
    },
    
    async fetchUserInfo() {
      const res = await getUserInfo()
      this.userInfo = res.data
      localStorage.setItem('user', JSON.stringify(res.data))
      return res.data
    },
    
    async logout() {
      try {
        await logout()
      } catch (e) {
        // ignore
      }
      this.token = ''
      this.userInfo = {}
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      router.push('/login')
    }
  }
})
