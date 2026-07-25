import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

type ThemeMode = 'light' | 'dark'

export const useThemeStore = defineStore('theme', () => {
  const theme = ref<ThemeMode>(
    (localStorage.getItem('theme_preference') as ThemeMode) || 'dark'
  )
  const primaryColor = ref(localStorage.getItem('theme_color') || '#ec4899')

  function toggleTheme() {
    theme.value = theme.value === 'dark' ? 'light' : 'dark'
  }
  function setPrimaryColor(hex: string) {
    primaryColor.value = hex
  }

  /* 同步 DOM & localStorage */
  watch(theme, val => {
    document.documentElement.classList.toggle('dark', val === 'dark')
    localStorage.setItem('theme_preference', val)
  }, { immediate: true })

  watch(primaryColor, hex => {
    // hexToRgbChannels(hex)
    document.documentElement.style.setProperty('--color-primary', hex)
    document.documentElement.style.setProperty('--el-color-primary', hex)
    localStorage.setItem('theme_color', hex)
  }, { immediate: true })

  return { theme, toggleTheme, primaryColor, setPrimaryColor }
})
