import {createRouter, createWebHistory} from 'vue-router'
import {adminRouter, staticRouter, userRouter} from '@/router/modules/staticRouter'
import {errorRouter} from '@/router/modules/errorRouter'
import nprogress from '@/utils/nprogress'
import {appConfig} from '@/settings'
import {useUserStore} from '@/store/modules/user'
import {meMsgWarning} from '@/utils/modal'
import {ADMIN_LOGIN_URL, LOGIN_URL, ROUTER_WHITE_LIST} from '@/config'

const router = createRouter({
  history: createWebHistory(),
  routes: [...staticRouter, ...userRouter, ...adminRouter, ...errorRouter],
  strict: false,
  // 滚动行为
  scrollBehavior() {
    return {left: 0, top: 0}
  },
})

/**
 * @description 前置路由
 * */
router.beforeEach(async (to, from, next) => {
  // 1、NProgress 开始
  nprogress.start()

  // 2、设置标题
  document.title = (to.meta.title as string) || appConfig.title

  console.log("===> from: [\"", from.path, "\"] to: [\"", to.path, "\"]")
  // 3、判断访问的是否是登陆页
  // 如果 token 不存在，则访问登录页
  // 如果 token 存在，则什么也不做
  const userStore = useUserStore()
  if (to.path === LOGIN_URL || to.path === ADMIN_LOGIN_URL) {
    if (userStore.token) {
      return next(from.fullPath)
    }
    return next()
  }

  // 4、判断访问页面是否在路由白名单地址[静态路由]中，如果存在直接放行
  if (ROUTER_WHITE_LIST.includes(to.path)) return next()

  // 5、判断进入的页面是否需要用户登录，如果需要，并且用户此时未登录
  if (to.meta && to.meta.requiresAuth && !userStore.token) {
    const loginPath = to.path.startsWith('/admin') ? ADMIN_LOGIN_URL : LOGIN_URL
    meMsgWarning({message: '请先登录'})
    return next({path: loginPath, replace: true})
  }

  // 6、页面刷新后，如果用户信息不存在重新获取用户信息
  if (userStore.token && !userStore.userInfo?.id) {
    await userStore.getUserInfo()
  }

  // 7、判断访问的页面是否有权限。没权限则不允许访问
  if (to.meta.roles && to.meta.roles.length > 0) {
    if (!userStore.hasPermission(to.meta.roles)) {
      meMsgWarning({message: '无权限访问该页面'})
      return next({path: '/403', replace: true})
    }
  }

  // 8、正常访问页面
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
router.afterEach(() => {
  // 结束全屏动画
  nprogress.done()
})

export default router

