import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import './styles/tokens.css'
import './styles/base.css'
import router from './router'
import App from './App.vue'
import { THEME_STORAGE_KEY, type ThemeMode } from './constants/theme'

const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')

function resolveThemeMode(): ThemeMode {
  const saved = localStorage.getItem(THEME_STORAGE_KEY)
  if (saved === 'light' || saved === 'dark' || saved === 'system') {
    return saved
  }
  return 'system'
}

function resolveTheme(mode: ThemeMode): 'light' | 'dark' {
  if (mode === 'system') {
    return mediaQuery.matches ? 'dark' : 'light'
  }
  return mode
}

function applyTheme(theme: 'light' | 'dark') {
  document.documentElement.setAttribute('data-theme', theme)
}

function applyThemeMode(mode: ThemeMode) {
  applyTheme(resolveTheme(mode))
}

applyThemeMode(resolveThemeMode())
mediaQuery.addEventListener('change', () => {
  if (resolveThemeMode() !== 'system') {
    return
  }
  applyThemeMode('system')
})

const app = createApp(App)

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn })

app.mount('#app')
