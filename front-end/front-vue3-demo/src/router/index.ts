import { createRouter, createWebHashHistory, createWebHistory } from 'vue-router'
import { errorRouter, layoutRouter, staticRouter } from '@/router/modules/staticRouter.ts'
import nprogress from '@/utils/nprogress.ts'
import { appConfig } from '@/settings.ts'
import { useUserStore } from '@/store/modules/user.ts'
import { meMsgWarning } from '@/utils/modal.ts'
import { LOGIN_URL, ROUTER_WHITE_LIST } from '@/config'

// .env配置文件读取
// const mode = import.meta.env.VITE_ROUTER_MODE;
const mode = 'history'

// 路由访问两种模式：带#号的哈希模式，正常路径的web模式。
const routerMode = {
  hash: () => createWebHashHistory(),
  history: () => createWebHistory()
}

// 创建路由器对象
const router = createRouter({
  // 路由模式hash或者默认不带#
  history: routerMode[mode](),
  routes: [...layoutRouter, ...staticRouter, ...errorRouter],
  strict: false,
  // 滚动行为
  scrollBehavior() {
    return {
      left: 0,
      top: 0
    }
  }
})

/**
 * @description 前置路由
 * */
router.beforeEach(async (to, from, next) => {
  // 1、NProgress 开始
  nprogress.start()
  // 2、标题切换，没有防止后置路由，是因为页面路径不存在，title会变成undefined
  document.title = to.meta.title || appConfig.title
  // 3、判断是访问登陆页，有Token访问当前页面，token过期访问接口，axios封装则自动跳转登录页面，没有Token重置路由到登陆页。
  const useStore = useUserStore()
  if (to.path.toLocaleLowerCase() === LOGIN_URL) {
    // 有Token访问当前页面
    if (useStore.token) {
      return next(from.fullPath)
    }
    meMsgWarning({
      message: '账号身份已过期，请重新登录'
    })
    return next()
  }
  // 4、判断访问页面是否在路由白名单地址[静态路由]中，如果存在直接放行。
  if (ROUTER_WHITE_LIST.includes(to.path)) return next()
  // 5、判断进入的页面是否需要用户登录，如果需要，并且用户此时未登录
  if (to.meta && to.meta.requireAuth && !useStore.token) {
    return next({ path: LOGIN_URL, replace: true })
  }
  // 6、页面刷新后，如果用户信息不存在重新获取用户信息
  if (useStore.token && !useStore.userInfo?.id) {
    await useStore.getUserInfo()
  }
  // 7、正常访问页面。
  next()
})

/**
 * @description 重置路由
 */
export const resetRouter = () => {
}

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
  // console.log("后置守卫", to, from);
})

export default router
