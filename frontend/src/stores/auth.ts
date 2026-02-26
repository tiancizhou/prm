import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginResponse } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginResponse | null>(null)
  const token = ref<string | null>(localStorage.getItem('accessToken'))

  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const res = await authApi.login({ username, password })
    const data = res.data
    token.value = data.accessToken
    user.value = data
    localStorage.setItem('accessToken', data.accessToken)
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      token.value = null
      user.value = null
      localStorage.removeItem('accessToken')
    }
  }

  async function fetchCurrentUser() {
    if (!token.value) return
    try {
      const res = await authApi.me()
      user.value = res.data
    } catch {
      token.value = null
      localStorage.removeItem('accessToken')
    }
  }

  return { user, token, isLoggedIn, login, logout, fetchCurrentUser }
})
