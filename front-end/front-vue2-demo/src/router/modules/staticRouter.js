import AdminLayout from "@/layout/admin/index.vue";
import Layout from "@/layout/index.vue";

/**
 * LayoutRouter[布局路由]
 */
export const layoutRouter = [
  {
    path: "/",
    component: Layout,
    meta: { title: "Home" },
    children: [
      {
        path: "/",
        component: () => import("@/views/front/base/home.vue"),
        meta: { title: "首页" }
      },
      {
        path: "favorite",
        component: () => import("@/views/front/base/favorite.vue"),
        meta: { title: "我的收藏", requireAuth: true }
      },
      {
        path: "notification",
        component: () => import("@/views/front/base/notification.vue"),
        meta: { title: "消息中心", requireAuth: true }
      },
      {
        path: "profile",
        component: () => import("@/views/front/base/profile.vue"),
        meta: { title: "个人中心", requireAuth: true }
      }
    ]
  },
  {
    path: "/admin",
    component: AdminLayout,
    meta: { title: "后台管理", requireAuth: true },
    children: [
      {
        path: "/admin",
        component: () => import("@/views/admin/base/dashboard.vue"),
        meta: { title: "数据总览", requireAuth: true }
      },
      {
        path: "user",
        component: () => import("@/views/admin/base/user.vue"),
        meta: { title: "用户管理", requireAuth: true }
      },
      {
        path: "user/center",
        component: () => import("@/views/admin/base/user-center.vue"),
        meta: { title: "用户中心", requireAuth: true }
      },
      {
        path: "feedback",
        component: () => import("@/views/admin/base/feedback.vue"),
        meta: { title: "反馈管理", requireAuth: true }
      },
      {
        path: "notification",
        component: () => import("@/views/admin/base/notification.vue"),
        meta: { title: "消息通知", requireAuth: true }
      },
      {
        path: "comment",
        component: () => import("@/views/admin/base/comment.vue"),
        meta: { title: "评论管理", requireAuth: true }
      },
      {
        path: "banner",
        component: () => import("@/views/admin/base/banner.vue"),
        meta: { title: "轮播图管理", requireAuth: true }
      }
    ]
  }
];

/**
 * staticRouter[静态路由]
 */
export const staticRouter = [
  {
    path: "/login",
    name: "Login",
    component: () => import("@/views/login")
  },
  {
    path: "/register",
    name: "Register",
    component: () => import("@/views/register")
  }
];

/**
 * errorRouter (错误页面路由)
 */
export const errorRouter = [
  {
    path: "/403",
    name: "403",
    component: () => import("@/views/features/403.vue")
  },
  {
    path: "/404",
    name: "404",
    component: () => import("@/views/features/404.vue")
  },
  {
    path: "/500",
    name: "500",
    component: () => import("@/views/features/500.vue")
  },
  // 找不到path将跳转404页面
  {
    path: "/:pathMatch(.*)*",
    component: () => import("@/views/features/404.vue")
  }
];
