// 配置路由
import Vue from "vue";
import VueRouter from "vue-router";
import {
  errorRouter,
  layoutRouter,
  staticRouter
} from "@/router/modules/staticRouter";
import nprogress from "@/utils/nprogress";
import defaultSettings from "@/settings";
import store from "@/store";
import { meMsgWarning } from "@/utils/modal";
import { LOGIN_URL, ROUTER_WHITE_LIST } from "@/config"; // 使用 VueRouter
// 使用 VueRouter
Vue.use(VueRouter);

// 为了兼容旧版的使用方式，即当调用 push 或 replace 方法时，如果提供了 resolve 和 reject 回调，则按照旧的方式处理；如果没有提供这两个回调，则使用默认的空函数作为回调来避免报错
let originPush = VueRouter.prototype.push;
let originReplace = VueRouter.prototype.replace;
VueRouter.prototype.push = function (location, resolve, reject) {
  if (resolve || reject) {
    // 如果提供了 resolve 和 reject 回调
    originPush.call(this, location, resolve, reject);
  } else {
    // 如果没有提供回调，使用默认的空函数
    originPush.call(
      this,
      location,
      () => {},
      () => {}
    );
  }
};

VueRouter.prototype.replace = function (location, resolve, reject) {
  if (resolve || reject) {
    // 如果提供了 resolve 和 reject 回调
    originReplace.call(this, location, resolve, reject);
  } else {
    // 如果没有提供回调，使用默认的空函数
    originReplace.call(
      this,
      location,
      () => {},
      () => {}
    );
  }
};

// 创建路由器对象
const router = new VueRouter({
  // 设置路由模式为 history 模式。history 模式使用 HTML5 History API 来管理路由，这样可以避免 URL 中出现 # 符号，使 URL 更加美观。
  mode: "history",
  // 为所有路由路径设置一个基础路径，当你的应用部署在服务器子目录时（例如 https://example.com/my-app/），base 会为所有路由自动添加前缀（如 /my-app）
  base: process.env.BASE_URL,
  routes: [...layoutRouter, ...staticRouter, ...errorRouter],
  // strict: false,
  // 设置路由改变时的滚动行为。每次路由改变时，页面滚动到顶部位置（x: 0, y: 0）
  scrollBehavior: () => ({ x: 0, y: 0 })
});

/**
 * @description 前置路由
 * */
router.beforeEach(async (to, from, next) => {
  // 1、NProgress 开始
  nprogress.start();
  // 2、标题切换，没有防止后置路由，是因为页面路径不存在，title会变成undefined
  document.title = to.meta?.title || defaultSettings.title;
  // 3、判断是访问登陆页，有Token访问当前页面，token过期访问接口，axios封装则自动跳转登录页面，没有Token重置路由到登陆页。
  if (to.path.toLocaleLowerCase() === LOGIN_URL) {
    // 有Token访问当前页面
    if (store.state.user.token) {
      return next(from.fullPath);
    }
    meMsgWarning({
      message: "账号身份已过期，请重新登录"
    });
    return next();
  }
  // 4、判断访问页面是否在路由白名单地址[静态路由]中，如果存在直接放行。
  if (ROUTER_WHITE_LIST.includes(to.path)) return next();
  // 5、判断进入的页面是否需要用户登录，如果需要，并且用户此时未登录
  if (to.meta?.requireAuth && !store.state.user.token) {
    return next({ path: LOGIN_URL, replace: true });
  }
  // 6、页面刷新后，如果用户信息不存在重新获取用户信息
  if (store.state.user.token && !store.state.user.userInfo?.id) {
    await store.dispatch("user/getUserInfo");
  }
  // 7.  判断是否有进入对应页面的权限
  // 追加：特殊判断，如果跳转页面是 /，如果需要权限，则判断权限是否充足
  if ((to.path === "/" || to.redirectedFrom === "/") && to.meta?.requireAuth) {
    const role = store.state.user.userInfo?.role;
    return role === 2 ? next("/admin") : next();
  }
  if (
    to.meta?.requireAuth &&
    !isAccessAllowed(to, store.state.user.userInfo?.role)
  ) {
    return next({ path: "/403" });
  }
  // 8、正常访问页面。
  next();
});

// 判断该路径是否能被当前角色访问
function isAccessAllowed(to, role) {
  if (role === 1 && to.path.startsWith("/admin")) return false;
  return !(role === 2 && !to.path.startsWith("/admin"));
}

/**
 * @description 路由跳转错误
 */
router.onError(error => {
  // 结束全屏动画
  nprogress.done();
  console.warn("路由错误", error.message);
});

/**
 * @description 后置路由
 */
router.afterEach((to, from) => {
  // 结束全屏动画
  nprogress.done();
  // console.log("后置守卫", to, from);
});

export default router;
