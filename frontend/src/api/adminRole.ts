import http from './http'

export type RoleTagType = 'danger' | 'warning' | 'primary' | 'success' | 'info'

export interface RoleGroupRow {
  id: number
  name: string
  code: string
  description: string
  tagType?: RoleTagType | string
  users: string
  memberCount: number
}

export interface RoleGroupMember {
  id: number
  username: string
  realName?: string | null
  displayName: string
}

export interface RoleGroupDetail {
  id: number
  name: string
  code: string
  description: string
  tagType?: RoleTagType | string
  members: RoleGroupMember[]
}

export interface SaveRoleGroupPayload {
  name: string
  code: string
  tagType?: RoleTagType | string | null
  description?: string | null
}

export const adminRoleApi = {
  list: () => http.get('/admin/roles'),
  get: (id: number) => http.get(`/admin/roles/${id}`),
  create: (data: SaveRoleGroupPayload) => http.post('/admin/roles', data),
  update: (id: number, data: SaveRoleGroupPayload) => http.put(`/admin/roles/${id}`, data),
  delete: (id: number) => http.delete(`/admin/roles/${id}`)
}
