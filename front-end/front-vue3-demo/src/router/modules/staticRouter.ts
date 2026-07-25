import type { RouteRecordRaw } from 'vue-router'

/**
 * staticRouter — 静态路由（无布局，如登录/注册）
 */
export const staticRouter: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/login.vue'),
    meta: { title: '登录' },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/register.vue'),
    meta: { title: '注册' },
  },
]

/**
 * userRouter — 用户端路由（UserLayout）
 */
export const userRouter: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layout/UserLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: '/home',
        name: 'Home',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', requiresAuth: true },
      },
      {
        path: '/spaces',
        name: 'Spaces',
        component: () => import('@/views/spaces/index.vue'),
        meta: { title: '空间浏览', requiresAuth: true },
      },
      {
        path: '/spaces/:id',
        name: 'SpaceDetail',
        component: () => import('@/views/spaces/detail.vue'),
        meta: { title: '空间详情', requiresAuth: true },
      },
      {
        path: '/my-reservations',
        name: 'MyReservations',
        component: () => import('@/views/reservations/index.vue'),
        meta: { title: '我的预约', requiresAuth: true },
      },
      {
        path: '/my-reservations/:id',
        name: 'ReservationDetail',
        component: () => import('@/views/reservations/detail.vue'),
        meta: { title: '预约详情', requiresAuth: true },
      },
      {
        path: '/messages',
        name: 'Messages',
        component: () => import('@/views/messages/index.vue'),
        meta: { title: '消息中心', requiresAuth: true },
      },
      {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true },
      },
      {
        path: '/credit',
        name: 'Credit',
        component: () => import('@/views/credit/index.vue'),
        meta: { title: '信用积分', requiresAuth: true },
      },
    ],
  },
]

/**
 * adminRouter — 管理端路由（AdminLayout）
 */
export const adminRouter: RouteRecordRaw[] = [
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/auth/login.vue'),
    meta: { title: '管理员登录' },
  },
  {
    path: '/admin',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/admin/dashboard',
    children: [
      {
        path: '/admin/dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/dashboard/index.vue'),
        meta: { title: '仪表盘', requiresAuth: true, roles: ['LIB_ADMIN', 'SYS_ADMIN'], icon: 'Odometer' },
      },
      {
        path: '/admin/spaces',
        name: 'AdminSpaces',
        component: () => import('@/views/admin/spaces/index.vue'),
        meta: { title: '空间管理', requiresAuth: true, roles: ['LIB_ADMIN', 'SYS_ADMIN'], icon: 'OfficeBuilding' },
      },
      {
        path: '/admin/equipment',
        name: 'AdminEquipment',
        component: () => import('@/views/admin/equipment/index.vue'),
        meta: { title: '设备管理', requiresAuth: true, roles: ['LIB_ADMIN', 'SYS_ADMIN'], icon: 'Monitor' },
      },
      {
        path: '/admin/reservations',
        name: 'AdminReservations',
        component: () => import('@/views/admin/reservations/index.vue'),
        meta: { title: '预约监管', requiresAuth: true, roles: ['LIB_ADMIN', 'SYS_ADMIN'], icon: 'Calendar' },
      },
      {
        path: '/admin/violations',
        name: 'AdminViolations',
        component: () => import('@/views/admin/violations/index.vue'),
        meta: { title: '违规管理', requiresAuth: true, roles: ['LIB_ADMIN', 'SYS_ADMIN'], icon: 'Warning' },
      },
      {
        path: '/admin/credit-rules',
        name: 'AdminCreditRules',
        component: () => import('@/views/admin/credit-rules/index.vue'),
        meta: { title: '信用规则', requiresAuth: true, roles: ['LIB_ADMIN', 'SYS_ADMIN'], icon: 'Tickets' },
      },
      {
        path: '/admin/users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/users/index.vue'),
        meta: { title: '用户管理', requiresAuth: true, roles: ['SYS_ADMIN'], icon: 'User' },
      },
      {
        path: '/admin/dict',
        name: 'AdminDict',
        component: () => import('@/views/admin/dict/index.vue'),
        meta: { title: '数据字典', requiresAuth: true, roles: ['SYS_ADMIN'], icon: 'Notebook' },
      },
      {
        path: '/admin/logs/operation',
        name: 'AdminOperationLogs',
        component: () => import('@/views/admin/logs/operation.vue'),
        meta: { title: '操作日志', requiresAuth: true, roles: ['SYS_ADMIN'], icon: 'Document' },
      },
      {
        path: '/admin/logs/login',
        name: 'AdminLoginLogs',
        component: () => import('@/views/admin/logs/login.vue'),
        meta: { title: '登录日志', requiresAuth: true, roles: ['SYS_ADMIN'], icon: 'Key' },
      },
      {
        path: '/admin/jobs',
        name: 'AdminJobs',
        component: () => import('@/views/admin/jobs/index.vue'),
        meta: { title: '定时任务', requiresAuth: true, roles: ['SYS_ADMIN'], icon: 'Clock' },
      },
      {
        path: '/admin/files',
        name: 'AdminFiles',
        component: () => import('@/views/admin/files/index.vue'),
        meta: { title: '文件管理', requiresAuth: true, roles: ['LIB_ADMIN', 'SYS_ADMIN'], icon: 'Folder' },
      },
      {
        path: '/admin/ai-conversations',
        name: 'AdminAIConversations',
        component: () => import('@/views/admin/ai-conversations/index.vue'),
        meta: { title: 'AI问答记录', requiresAuth: true, roles: ['LIB_ADMIN', 'SYS_ADMIN'], icon: 'ChatDotRound' },
      },
      {
        path: '/admin/settings',
        name: 'AdminSettings',
        component: () => import('@/views/admin/settings/index.vue'),
        meta: { title: '系统设置', requiresAuth: true, roles: ['SYS_ADMIN'], icon: 'Setting' },
      },
    ],
  },
]

/**
 * errorRouter — 错误页面路由
 */
export const errorRouter: RouteRecordRaw[] = [
  {
    path: '/403',
    name: '403',
    component: () => import('@/views/features/403.vue'),
    meta: { title: '无权限' },
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/features/404.vue'),
    meta: { title: '页面不存在' },
  },
  {
    path: '/500',
    name: '500',
    component: () => import('@/views/features/500.vue'),
    meta: { title: '服务器错误' },
  },
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/features/404.vue'),
    meta: { title: '页面不存在' },
  },
]
