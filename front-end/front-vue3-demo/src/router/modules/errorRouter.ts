import type { RouteRecordRaw } from 'vue-router'

/**
 * errorRouter — 错误页面路由
 */
export const errorRouter: RouteRecordRaw[] = [
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/_core/error/403.vue'),
    meta: { title: '无权限' },
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/_core/error/404.vue'),
    meta: { title: '页面不存在' },
  },
  {
    path: '/500',
    name: '500',
    component: () => import('@/views/_core/error/500.vue'),
    meta: { title: '服务器错误' },
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/_core/error/404.vue'),
    meta: { title: '页面不存在' },
  },
]
