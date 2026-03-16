import http from './http'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  tokenName: string
  userId: number
  username: string
  nickname: string
  avatar: string
  roles: string[]
  permissions: string[]
}

export const authApi = {
  login: (data: LoginRequest) => http.post<any, { data: LoginResponse }>('/auth/login', data),
  logout: () => http.post('/auth/logout'),
  me: () => http.get<any, { data: LoginResponse }>('/auth/me')
}
