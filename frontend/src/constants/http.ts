import type { ThemeLocale } from './theme'

export const HTTP_I18N: Record<
  ThemeLocale,
  {
    requestFailed: string
    networkError: string
  }
> = {
  'zh-CN': {
    requestFailed: '请求失败',
    networkError: '网络错误'
  },
  'en-US': {
    requestFailed: 'Request failed',
    networkError: 'Network error'
  }
}

