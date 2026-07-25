import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserVO } from '@/api/auth/type'
import { mockGetUserInfo } from '@/api/auth/mock'
import { useRouter } from 'vue-router'
import { ADMIN_LOGIN_URL, LOGIN_URL } from '@/config'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('library_token') || '')
  const userInfo = ref<UserVO | null>(null)

  const role = computed(() => userInfo.value?.role || '')
  const isAdmin = computed(() => role.value === 'LIB_ADMIN' || role.value === 'SYS_ADMIN')
  const isLoggedIn = computed(() => !!token.value)

  function setToken(t: string) {
    token.value = t
    localStorage.setItem('library_token', t)
  }

  function setUserInfo(info: UserVO) {
    userInfo.value = info
  }

  async function getUserInfo() {
    try {
      const res = await mockGetUserInfo()
      userInfo.value = res.data
    } catch {
      // mock fallback
    }
  }

  async function HandleLogout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('library_token')
    const router = useRouter()
    await router.push(LOGIN_URL)
  }

  function hasPermission(roles: string[]): boolean {
    if (!roles || roles.length === 0) return true
    return roles.includes(role.value)
  }

  return {
    token,
    userInfo,
    role,
    isAdmin,
    isLoggedIn,
    setToken,
    setUserInfo,
    getUserInfo,
    HandleLogout,
    hasPermission,
  }
})
