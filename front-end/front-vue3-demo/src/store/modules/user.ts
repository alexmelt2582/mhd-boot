import { defineStore } from 'pinia'
import { appConfig } from '@/settings'
import { getLoginInfo, login, logout } from '@/api/login.ts'
import type { SystemUserVO } from '@/api/type.ts'

// 统一的 key
const TOKEN_KEY = appConfig.tokenCookieKey

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY) || '', // 读缓存
    userInfo: ref<SystemUserVO>(<SystemUserVO>{})
  }),

  getters: {
    // 以后想加快捷 getter 可写这里
  },

  actions: {
    /* 获取用户信息 */
    async getUserInfo() {
      const { code, data } = await getLoginInfo()
      if (code === 0) {
        this.userInfo = data
      }
    },

    /* 登录 */
    async HandleLogin(user: any, rememberMe: boolean) {
      const { code, data } = await login(user)
      const { token, user: userInfo } = data
      if (code === 0 && token) {
        // 存 token
        if (rememberMe) {
          localStorage.setItem(TOKEN_KEY, token)
        } else {
          sessionStorage.setItem(TOKEN_KEY, token)
        }
        this.token = token
        this.userInfo = userInfo
      }
    },

    /* 登出 */
    async HandleLogout() {
      try {
        await logout()
      } finally {
        // 无论成功失败都清掉
        localStorage.removeItem(TOKEN_KEY)
        sessionStorage.removeItem(TOKEN_KEY)
        this.token = ''
        this.userInfo = <SystemUserVO>{}
      }
    }
  }
})
