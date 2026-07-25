<template>
  <div
    class="w-64 h-full flex flex-col bg-slate-50 dark:bg-slate-950/50 border-r border-slate-200 dark:border-white/5 p-4 hidden md:flex transition-colors duration-300"
  >
    <!-- Logo 区域 -->
    <div class="flex items-center gap-2 px-4 mb-8">
      <div
        class="w-8 h-8 rounded-lg bg-gradient-to-br from-primary to-secondary flex items-center justify-center shadow-lg shadow-primary/20"
      >
        <Music :size="18" class="text-white" />
      </div>
      <h1
        class="text-xl font-bold bg-clip-text text-transparent bg-gradient-to-r from-primary to-secondary"
      >
        MuseFlow
      </h1>
    </div>

    <!-- Discover -->
    <div class="space-y-1">
      <p
        class="px-4 text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2">
        Discover
      </p>
      <RouterLink to="/home" v-slot="{ isActive }: { isActive: boolean }">
        <div :class="navClass(isActive)">
          <Home :size="20" />
          <span>Home</span>
        </div>
      </RouterLink>
      <RouterLink to="/search" v-slot="{ isActive }: { isActive: boolean }">
        <div :class="navClass(isActive)">
          <Search :size="20" />
          <span>Search</span>
        </div>
      </RouterLink>
      <RouterLink to="/charts" v-slot="{ isActive }: { isActive: boolean }">
        <div :class="navClass(isActive)">
          <Disc :size="20" />
          <span>Leaderboard</span>
        </div>
      </RouterLink>
    </div>

    <!-- Library -->
    <div class="mt-8 space-y-1">
      <p
        class="px-4 text-xs font-semibold text-slate-400 dark:text-slate-500 uppercase tracking-wider mb-2">
        Library
      </p>
      <RouterLink to="/library" v-slot="{ isActive }: { isActive: boolean }">
        <div :class="navClass(isActive)">
          <Library :size="20" />
          <span>All Playlists</span>
        </div>
      </RouterLink>
      <RouterLink to="/favorites" v-slot="{ isActive }: { isActive: boolean }">
        <div :class="navClass(isActive)">
          <Heart :size="20" />
          <span>Favorites</span>
        </div>
      </RouterLink>
      <RouterLink to="/history" v-slot="{ isActive }: { isActive: boolean }">
        <div :class="navClass(isActive)">
          <Clock :size="20" />
          <span>History</span>
        </div>
      </RouterLink>
    </div>

    <!-- 底部区域 -->
    <div class="mt-auto space-y-4">
      <ThemeSettings />
      <div
        class="px-4 py-4 rounded-xl bg-gradient-to-br from-indigo-900/10 to-purple-900/10 dark:from-indigo-900/40 dark:to-purple-900/40 border border-indigo-200 dark:border-white/5"
      >
        <p class="text-sm font-medium text-indigo-800 dark:text-indigo-200">Go Premium</p>
        <p class="text-xs text-indigo-600/70 dark:text-indigo-300/60 mt-1">High quality audio &
          ad-free experience.</p>
        <button
          class="mt-3 w-full py-2 text-xs font-bold text-white bg-primary hover:bg-primary/90 rounded-md transition-colors shadow-lg shadow-primary/20"
        >
          Upgrade Plan
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import ThemeSettings from './ThemeSettings.vue'
import { Clock, Disc, Heart, Home, Library, Music, Search } from 'lucide-vue-next'

/* 激活样式 */
const navClass = (isActive: boolean) =>
  `flex items-center gap-3 px-4 py-3 rounded-lg transition-all duration-200 group ${
    isActive
      ? 'bg-primary/10 text-primary font-medium'
      : 'text-slate-500 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-200/50 dark:hover:bg-slate-800/50'
  }`
</script>
