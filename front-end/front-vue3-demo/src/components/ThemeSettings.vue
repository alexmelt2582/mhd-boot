<template>
  <div
    class="px-4 py-4 rounded-xl bg-slate-100 dark:bg-slate-900/50 border border-slate-200 dark:border-white/5 transition-colors duration-300"
  >
    <!-- 标题 + 深色切换 -->
    <div class="flex items-center justify-between mb-4">
      <span class="text-xs font-bold text-slate-500 uppercase tracking-wider">
        Appearance
      </span>

      <!-- 深色模式开关 -->
      <button
        @click="themeStore.toggleTheme()"
        class="relative w-12 h-6 rounded-full bg-slate-300 dark:bg-slate-700 transition-colors focus:outline-none"
      >
        <div
          :class="[
            'absolute top-1 left-1 w-4 h-4 rounded-full bg-white shadow-sm flex items-center justify-center transition-transform duration-300',
            themeStore.theme === 'dark' ? 'translate-x-6' : 'translate-x-0',
          ]"
        >
          <Moon v-if="themeStore.theme === 'dark'" :size="10" class="text-slate-900" />
          <Sun v-else :size="10" class="text-amber-500" />
        </div>
      </button>
    </div>

    <!-- 主题色选择 -->
    <div class="space-y-2">
      <span class="text-xs font-medium text-slate-500">Accent Color</span>
      <div class="flex flex-wrap gap-2">
        <button
          v-for="color in PRESET_COLORS"
          :key="color.hex"
          :title="color.name"
          @click="themeStore.setPrimaryColor(color.hex)"
          class="w-6 h-6 rounded-full flex items-center justify-center transition-transform hover:scale-110"
          :class="
            themeStore.primaryColor === color.hex
              ? 'ring-2 ring-offset-2 ring-offset-slate-100 dark:ring-offset-slate-900 ring-slate-400'
              : ''
          "
          :style="{ backgroundColor: color.hex }"
        >
          <Check
            v-if="themeStore.primaryColor === color.hex"
            :size="12"
            class="text-white drop-shadow-md"
          />
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { Check, Moon, Sun } from 'lucide-vue-next'
import { PRESET_COLORS } from '../utils/themeUtils'
import { useThemeStore } from '@/store/modules/theme.ts'

const themeStore = useThemeStore()
</script>
