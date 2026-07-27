import {defineStore} from 'pinia'
import {computed, ref} from 'vue'
import type {UserVO} from '@/api/auth/type'
import router from '@/router';
import {ADMIN_LOGIN_URL, LOGIN_URL} from '@/config'
import {getLoginUserInfo} from "@/api/auth/api.ts";

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
      const res = await getLoginUserInfo()
      userInfo.value = res.data
    } catch {
    }
  }

  async function HandleLogout() {
    const path = isAdmin ? ADMIN_LOGIN_URL : LOGIN_URL
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('library_token')
    await router.push(path)
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
