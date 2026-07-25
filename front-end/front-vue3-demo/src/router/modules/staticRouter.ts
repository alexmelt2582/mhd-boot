import type { RouteRecordRaw } from 'vue-router'

/**
 * LayoutRouter[布局路由]
 */
export const layoutRouter: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        component: () => import('@/views/home.vue')
      },
      {
        path: '/search',
        component: () => import('@/views/search.vue')
      },
      {
        path: '/charts',
        component: () => import('@/views/charts.vue')
      }
    ]
  }
]

/**
 * staticRouter[静态路由]
 */
export const staticRouter: RouteRecordRaw[] = []

/**
 * errorRouter (错误页面路由)
 */
export const errorRouter: RouteRecordRaw[] = [
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
]
