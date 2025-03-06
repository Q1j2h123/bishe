import { ref, watch } from 'vue'

const isDarkMode = ref(false)

// 监听系统主题变化
if (typeof window !== 'undefined') {
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  
  isDarkMode.value = mediaQuery.matches
  
  mediaQuery.addEventListener('change', (e) => {
    isDarkMode.value = e.matches
  })
}

export function useTheme() {
  return {
    isDarkMode
  }
} 