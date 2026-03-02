export type ThemeMode = 'light' | 'dark' | 'system'
export type ThemeLocale = 'zh-CN' | 'en-US'

export const THEME_STORAGE_KEY = 'prm-theme'

export const THEME_MODES: ThemeMode[] = ['light', 'dark', 'system']

export const THEME_I18N: Record<
  ThemeLocale,
  {
    switchAria: string
    switchedPrefix: string
    triggerLabels: Record<ThemeMode, string>
    modeLabels: Record<ThemeMode, string>
  }
> = {
  'zh-CN': {
    switchAria: '切换主题',
    switchedPrefix: '已切换为',
    triggerLabels: {
      light: '浅色',
      dark: '深色',
      system: '跟随系统'
    },
    modeLabels: {
      light: '浅色模式',
      dark: '深色模式',
      system: '跟随系统'
    }
  },
  'en-US': {
    switchAria: 'Switch theme',
    switchedPrefix: 'Switched to ',
    triggerLabels: {
      light: 'Light',
      dark: 'Dark',
      system: 'System'
    },
    modeLabels: {
      light: 'Light mode',
      dark: 'Dark mode',
      system: 'Follow system'
    }
  }
}

export function resolveThemeLocale(locale?: string): ThemeLocale {
  if (!locale) {
    return 'en-US'
  }
  return locale.toLowerCase().startsWith('zh') ? 'zh-CN' : 'en-US'
}
