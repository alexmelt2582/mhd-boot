<template>
  <div
    class="flex h-screen bg-slate-50 text-slate-900 transition-colors duration-300 dark:bg-slate-950 dark:text-slate-100">
    <!-- Sidebar -->
    <aside
      :class="[
        'flex flex-col border-r border-slate-200 bg-white transition-all duration-300 dark:border-white/5 dark:bg-slate-950',
        isCollapsed ? 'w-16' : 'w-60',
      ]"
    >
      <!-- Logo -->
      <div
        class="flex h-16 items-center justify-center border-b border-slate-200 px-3 dark:border-white/5">
        <div class="flex items-center gap-2">
          <div
            class="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-secondary">
            <svg class="h-4 w-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
            </svg>
          </div>
          <span v-show="!isCollapsed"
                class="bg-gradient-to-r from-primary to-secondary bg-clip-text text-lg font-bold text-transparent">图书馆管理</span>
        </div>
      </div>

      <!-- Menu -->
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapsed"
        :collapse-transition="false"
        router
        class="flex-1 border-none !bg-transparent"
        background-color="transparent"
        text-color="var(--el-menu-text-color)"
        active-text-color="var(--color-primary)"
      >
        <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
          <el-icon>
            <component :is="item.icon"/>
          </el-icon>
          <template #title>{{ item.label }}</template>
        </el-menu-item>
      </el-menu>

      <!-- Footer -->
      <div class="border-t border-slate-200 p-3 dark:border-white/5">
        <button
          @click="isCollapsed = !isCollapsed"
          class="flex w-full items-center justify-center rounded-lg p-2 text-slate-400 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800"
        >
          <svg v-if="!isCollapsed" class="h-4 w-4" fill="none" stroke="currentColor"
               viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M11 19l-7-7 7-7m8 14l-7-7 7-7"/>
          </svg>
          <svg v-else class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M13 5l7 7-7 7M5 5l7 7-7 7"/>
          </svg>
        </button>
      </div>
    </aside>

    <!-- Main area -->
    <div class="flex flex-1 flex-col overflow-hidden">
      <!-- Header -->
      <header
        class="flex h-16 shrink-0 items-center justify-between border-b border-slate-200 bg-white px-6 dark:border-white/5 dark:bg-slate-950">
        <!-- Breadcrumb -->
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">管理后台</el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
        </el-breadcrumb>

        <!-- Right -->
        <div class="flex items-center gap-3">
          <!-- Go to user side -->
          <el-tooltip content="返回用户端" placement="bottom">
            <a href="/home" target="_blank"
               class="rounded-lg p-2 text-slate-500 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800">
              <svg class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14"/>
              </svg>
            </a>
          </el-tooltip>

          <!-- Dark toggle -->
          <button
            @click="themeStore.toggleTheme()"
            class="rounded-lg p-2 text-slate-500 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800"
          >
            <svg v-if="themeStore.theme === 'dark'" class="h-4 w-4" fill="none"
                 stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"/>
            </svg>
            <svg v-else class="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"/>
            </svg>
          </button>

          <!-- Admin dropdown -->
          <el-dropdown trigger="click">
            <span
              class="flex cursor-pointer items-center gap-2 rounded-lg p-1.5 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800">
              <el-avatar :size="30" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="text-sm font-medium">{{ userStore.userInfo?.realName }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- Page content -->
      <main class="flex-1 overflow-auto p-6">
        <router-view/>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import {computed, ref} from 'vue'
import {useThemeStore} from '@/store/modules/theme'
import {useUserStore} from '@/store/modules/user'
import {meMsgSuccess} from '@/utils/modal'

const themeStore = useThemeStore()
const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const isCollapsed = ref(false)

const activeMenu = computed(() => route.path)
const currentTitle = computed(() => route.meta.title as string | undefined)

const menuItems = [
  {path: '/admin/dashboard', label: '仪表盘', icon: 'Odometer'},
  {path: '/admin/spaces', label: '空间管理', icon: 'OfficeBuilding'},
  {path: '/admin/equipment', label: '设备管理', icon: 'Monitor'},
  {path: '/admin/reservations', label: '预约监管', icon: 'Calendar'},
  {path: '/admin/violations', label: '违规管理', icon: 'Warning'},
  {path: '/admin/credit-rules', label: '信用规则', icon: 'Tickets'},
  {path: '/admin/users', label: '用户管理', icon: 'User'},
  {path: '/admin/dict', label: '数据字典', icon: 'Notebook'},
  {path: '/admin/logs/operation', label: '操作日志', icon: 'Document'},
  {path: '/admin/logs/login', label: '登录日志', icon: 'Key'},
  {path: '/admin/jobs', label: '定时任务', icon: 'Clock'},
  {path: '/admin/files', label: '文件管理', icon: 'Folder'},
  {path: '/admin/ai-conversations', label: 'AI问答记录', icon: 'ChatDotRound'},
  {path: '/admin/settings', label: '系统设置', icon: 'Setting'},
]

async function handleLogout() {
  await userStore.HandleLogout()
  meMsgSuccess({message: '已退出登录'})
}
</script>
