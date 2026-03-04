import http from './http'

export interface Project {
  id: number
  name: string
  code: string
  description: string
  status: string
  visibility: string
  ownerId: number
  ownerName: string
  startDate: string
  endDate: string
  createdAt: string
  canEdit?: boolean
}

export interface PageResult<T> {
  total: number
  page: number
  size: number
  records: T[]
}

export const projectApi = {
  list: (params?: any) => http.get<any, { data: PageResult<Project> }>('/projects', { params }),
  get: (id: number) => http.get<any, { data: Project }>(`/projects/${id}`),
  create: (data: any) => http.post<any, { data: Project }>('/projects', data),
  update: (id: number, data: any) => http.put<any, { data: Project }>(`/projects/${id}`, data),
  archive: (id: number) => http.post(`/projects/${id}/archive`),
  close: (id: number) => http.post(`/projects/${id}/close`),
  getMembers: (id: number) => http.get(`/projects/${id}/members`),
  addMember: (id: number, userId: number) =>
    http.post(`/projects/${id}/members`, null, { params: { userId } }),
  removeMember: (id: number, userId: number) => http.delete(`/projects/${id}/members/${userId}`)
}
