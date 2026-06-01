import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  // 状态
  const token = ref(localStorage.getItem('access_token') || '')
  const refreshToken = ref(localStorage.getItem('refresh_token') || '')
  const userInfo = ref(null)
  const unreadCount = ref(0)

  // 计算属性
  const isLoggedIn = computed(() => !!token.value)
  const creditLevel = computed(() => {
    if (!userInfo.value) return { name: '新手', icon: 'Sunrise', color: '#909399' }
    const score = userInfo.value.creditScore || 0
    if (score >= 201) return { name: '钻石', icon: 'Trophy', color: '#9B59B6' }
    if (score >= 101) return { name: '金牌', icon: 'Medal', color: '#F5A623' }
    if (score >= 51)  return { name: '银牌', icon: 'Medal', color: '#909399' }
    if (score >= 21)  return { name: '铜牌', icon: 'Medal', color: '#E6A23C' }
    return { name: '新手', icon: 'Sunrise', color: '#909399' }
  })

  // 操作
  async function login(username, password) {
    const res = await authApi.login({ username, password })
    token.value = res.data.accessToken
    refreshToken.value = res.data.refreshToken
    localStorage.setItem('access_token', res.data.accessToken)
    localStorage.setItem('refresh_token', res.data.refreshToken)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    try {
      const res = await authApi.getUserInfo()
      userInfo.value = res.data
    } catch (e) {
      // 获取用户信息失败，清除过期 token
      userInfo.value = null
      if (!token.value) return
      // token 可能已过期，清除登录状态
      logout()
    }
  }

  function logout() {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    localStorage.removeItem('access_token')
    localStorage.removeItem('refresh_token')
    router.push('/login')
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLoggedIn,
    creditLevel,
    unreadCount,
    login,
    fetchUserInfo,
    logout,
  }
})
