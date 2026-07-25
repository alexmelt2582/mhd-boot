import { createRouter, createWebHistory } from 'vue-router'
import { adminRouter, errorRouter, staticRouter, userRouter } from '@/router/modules/staticRouter'
import nprogress from '@/utils/nprogress'
import { appConfig } from '@/settings'
import { useUserStore } from '@/store/modules/user'
import { meMsgWarning } from '@/utils/modal'
import { ADMIN_LOGIN_URL, LOGIN_URL, ROUTER_WHITE_LIST } from '@/config'

const router = createRouter({
  history: createWebHistory(),
  routes: [...staticRouter, ...userRouter, ...adminRouter, ...errorRouter],
  strict: false,
  // 滚动行为
  scrollBehavior() {
    return { left: 0, top: 0 }
  },
})

/**
 * @description 前置路由
 * */
router.beforeEach(async (to, from, next) => {
  // 1、NProgress 开始
  nprogress.start()
  // 2、标题切换，没有防止后置路由，是因为页面路径不存在，title会变成undefined
  document.title = (to.meta.title as string) || appConfig.title
  // 3、判断是访问登陆页，有Token访问当前页面，token过期访问接口，axios封装则自动跳转登录页面，没有Token重置路由到登陆页。
  const userStore = useUserStore()

  if (to.path === LOGIN_URL || to.path === ADMIN_LOGIN_URL) {
    if (userStore.token) {
      return next(from.fullPath)
    }
    return next()
  }

  // 4、判断访问页面是否在路由白名单地址[静态路由]中，如果存在直接放行。
  if (ROUTER_WHITE_LIST.includes(to.path)) return next()

  // 5、判断进入的页面是否需要用户登录，如果需要，并且用户此时未登录
  if (to.meta && to.meta.requiresAuth && !userStore.token) {
    const loginPath = to.path.startsWith('/admin') ? ADMIN_LOGIN_URL : LOGIN_URL
    meMsgWarning({ message: '请先登录' })
    return next({ path: loginPath, replace: true })
  }

  // Check role-based access for admin routes
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!userStore.hasPermission(to.meta.roles)) {
      meMsgWarning({ message: '无权限访问该页面' })
      return next({ path: '/403', replace: true })
    }
  }

  // 6、页面刷新后，如果用户信息不存在重新获取用户信息
  if (userStore.token && !userStore.userInfo?.id) {
    await userStore.getUserInfo()
  }
  // 7、正常访问页面。
  next()
})



/**
 * @description 路由跳转错误
 */
router.onError((error) => {
  // 结束全屏动画
  nprogress.done()
  console.warn('路由错误', error.message)
})

/**
 * @description 后置路由
 */
router.afterEach((to, from) => {
  // 结束全屏动画
  nprogress.done()
  console.warn('路由错误', error.message)
})

export default router

