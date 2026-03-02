import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { HTTP_I18N } from '@/constants/http'
import { resolveThemeLocale } from '@/constants/theme'

const http = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json; charset=UTF-8',
    Accept: 'application/json; charset=UTF-8'
  }
})

function getHttpText() {
  const locale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
  return HTTP_I18N[locale]
}

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    if (response.config.responseType === 'blob') return response
    const data = response.data
    if (data.code !== 200) {
      ElMessage.error(data.msg || getHttpText().requestFailed)
      return Promise.reject(new Error(data.msg))
    }
    return data
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      window.location.href = '/login'
    } else {
      ElMessage.error(error.response?.data?.msg || getHttpText().networkError)
    }
    return Promise.reject(error)
  }
)

export async function buildRequestConfig(url: string): Promise<AxiosRequestConfig> {
  const config: AxiosRequestConfig = { url, headers: {} as Record<string, string> }
  const token = localStorage.getItem('accessToken')
  if (token) {
    (config.headers as Record<string, string>).Authorization = `Bearer ${token}`
  }
  return config
}

export default http

