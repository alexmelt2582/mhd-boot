<template>
  <div
    class="flex min-h-screen flex-col bg-slate-50 text-slate-900 transition-colors duration-300 dark:bg-slate-950 dark:text-slate-100">
    <!-- Background blobs -->
    <div
      class="pointer-events-none fixed -left-[10%] -top-[20%] z-0 h-[50%] w-[50%] rounded-full bg-secondary/10 blur-[120px] dark:bg-secondary/20"></div>
    <div
      class="pointer-events-none fixed -bottom-[20%] -right-[10%] z-0 h-[50%] w-[50%] rounded-full bg-primary/10 blur-[120px] dark:bg-primary/20"></div>

    <!-- Navbar -->
    <header
      class="sticky top-0 z-50 border-b border-slate-200 bg-white/80 backdrop-blur-md dark:border-white/5 dark:bg-slate-950/80">
      <div class="mx-auto flex h-16 max-w-7xl items-center justify-between px-4 sm:px-6">
        <!-- Logo -->
        <RouterLink to="/home" class="flex items-center gap-2.5">
          <div
            class="flex h-9 w-9 items-center justify-center rounded-lg bg-gradient-to-br from-primary to-secondary shadow-lg shadow-primary/20">
            <svg class="h-5 w-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/>
            </svg>
          </div>
          <span
            class="bg-gradient-to-r from-primary to-secondary bg-clip-text text-xl font-bold text-transparent">智慧图书馆</span>
        </RouterLink>

        <!-- Desktop nav -->
        <nav class="hidden items-center gap-1 md:flex">
          <RouterLink v-for="link in navLinks" :key="link.path" :to="link.path"
                      v-slot="{ isActive }">
            <span :class="navClass(isActive)">{{ link.label }}</span>
          </RouterLink>
        </nav>

        <!-- Right actions -->
        <div class="flex items-center gap-3">
          <!-- Dark mode toggle -->
          <button
            @click="themeStore.toggleTheme()"
            class="rounded-lg p-2 text-slate-500 transition-colors hover:bg-slate-100 dark:text-slate-400 dark:hover:bg-slate-800"
          >
            <svg v-if="themeStore.theme === 'dark'" class="h-5 w-5" fill="none"
                 stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z"/>
            </svg>
            <svg v-else class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"/>
            </svg>
          </button>

          <!-- User dropdown -->
          <el-dropdown v-if="userStore.isLoggedIn" trigger="click">
            <span
              class="flex cursor-pointer items-center gap-2 rounded-lg p-1.5 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800">
              <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="hidden text-sm font-medium sm:inline">{{
                  userStore.userInfo?.realName
                }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>
                  <RouterLink to="/profile" class="block w-full">个人中心</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item>
                  <RouterLink to="/credit" class="block w-full">信用积分</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item v-if="userStore.isAdmin">
                  <RouterLink to="/admin/dashboard" class="block w-full">管理后台</RouterLink>
                </el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <!-- Mobile menu button -->
          <button @click="mobileMenuOpen = !mobileMenuOpen"
                  class="rounded-lg p-2 text-slate-500 md:hidden hover:bg-slate-100 dark:hover:bg-slate-800">
            <svg class="h-5 w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path v-if="!mobileMenuOpen" stroke-linecap="round" stroke-linejoin="round"
                    stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
              <path v-else stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                    d="M6 18L18 6M6 6l12 12"/>
            </svg>
          </button>
        </div>
      </div>

      <!-- Mobile menu -->
      <div v-if="mobileMenuOpen"
           class="border-t border-slate-200 px-4 pb-3 pt-2 md:hidden dark:border-white/5">
        <RouterLink v-for="link in navLinks" :key="link.path" :to="link.path"
                    @click="mobileMenuOpen = false" v-slot="{ isActive }">
          <span :class="[navClass(isActive), 'block w-full']">{{ link.label }}</span>
        </RouterLink>
      </div>
    </header>

    <!-- Main content -->
    <main class="relative z-10 flex-1">
      <router-view/>
    </main>

    <!-- Footer -->
    <footer class="border-t border-slate-200 bg-white dark:border-white/5 dark:bg-slate-950">
      <div
        class="mx-auto max-w-7xl px-4 py-8 text-center text-sm text-slate-400 dark:text-slate-500 sm:px-6">
        &copy; {{ new Date().getFullYear() }} 智慧图书馆空间预约系统.
      </div>
    </footer>
  </div>
</template>

<script setup lang="ts">
import {useThemeStore} from '@/store/modules/theme'
import {useUserStore} from '@/store/modules/user'
import {meMsgSuccess} from '@/utils/modal'

const themeStore = useThemeStore()
const userStore = useUserStore()
const mobileMenuOpen = ref(false)
const router = useRouter()

const navLinks = [
  {path: '/home', label: '首页'},
  {path: '/spaces', label: '空间浏览'},
  {path: '/my-reservations', label: '我的预约'},
  {path: '/messages', label: '消息中心'},
]

const navClass = (isActive: boolean) =>
  `px-3 py-2 rounded-lg text-sm font-medium transition-colors duration-200 ${
    isActive
      ? 'bg-primary/10 text-primary'
      : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-100'
  }`

async function handleLogout() {
  await userStore.HandleLogout()
  meMsgSuccess({message: '已退出登录'})
}
</script>
