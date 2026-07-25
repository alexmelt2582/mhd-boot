<template>
  <!-- 浅色时白底黑字，深色时黑底白字，颜色变化时有 300 ms 平滑过渡，且不允许出现滚动条 -->
  <div class="flex h-screen w-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 font-sans selection:bg-primary/30 transition-colors duration-300">
    <!-- 背景底部阴影 -->
    <div class="fixed top-[-20%] left-[-10%] w-[50%] h-[50%] bg-secondary/10 dark:bg-secondary/20 rounded-full blur-[120px] pointer-events-none z-0"></div>
    <!-- 背景底部阴影 -->
    <div class="fixed bottom-[-20%] right-[-10%] w-[50%] h-[50%] bg-primary/10 dark:bg-primary/20 rounded-full blur-[120px] pointer-events-none z-0"></div>
    <!-- 布局 -->
    <div class="z-10 flex w-full h-full relative">
      <Sidebar />
      <main class="flex-1 h-full relative overflow-hidden">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import Sidebar from '@/components/Sidebar.vue'
</script>
