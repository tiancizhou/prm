import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, type LoginResponse } from '@/api/auth'
import { firstAccessiblePath, hasPermission, isSuperAdmin } from '@/utils/permission'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<LoginResponse | null>(null)
  const token = ref<string | null>(localStorage.getItem('accessToken'))

  const isLoggedIn = computed(() => !!token.value)
  const permissionCodes = computed(() => user.value?.permissions ?? [])
  const roleCodes = computed(() => user.value?.roles ?? [])
  const isSuperAdminUser = computed(() => isSuperAdmin(roleCodes.value))

  function canAccess(permissionCode?: string | null) {
    return hasPermission(roleCodes.value, permissionCodes.value, permissionCode)
  }

  function resolveFirstAccessiblePath() {
    return firstAccessiblePath(roleCodes.value, permissionCodes.value)
  }

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

  return { user, token, isLoggedIn, permissionCodes, roleCodes, isSuperAdminUser, canAccess, resolveFirstAccessiblePath, login, logout, fetchCurrentUser }
})
