import http from './http'

export type DepartmentNodeType = 'COMPANY' | 'DEPARTMENT'

export interface DepartmentTreeNode {
  nodeKey: string
  nodeType: DepartmentNodeType
  id: number
  companyId: number | null
  name: string
  parentId: number | null
  sortOrder?: number
  status?: string
  children?: DepartmentTreeNode[]
}

export interface DepartmentDetail {
  id: number
  companyId: number | null
  name: string
  parentId: number | null
  sortOrder?: number
  status?: string
  userCount?: number
  children: DepartmentTreeNode[]
}

export interface SaveDepartmentPayload {
  name: string
  companyId?: number | null
  parentId?: number | null
  sortOrder?: number
  status?: string
}

export const adminDepartmentApi = {
  tree: () => http.get('/admin/departments/tree'),
  get: (id: number) => http.get(`/admin/departments/${id}`),
  create: (data: SaveDepartmentPayload) => http.post('/admin/departments', data),
  update: (id: number, data: SaveDepartmentPayload) => http.put(`/admin/departments/${id}`, data),
  delete: (id: number) => http.delete(`/admin/departments/${id}`)
}
