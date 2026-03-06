import http from './http'

export interface ModuleDTO {
  id: number
  projectId: number
  parentId: number | null
  name: string
  sortOrder: number
  children: ModuleDTO[]
}

export const moduleApi = {
  listTree: (projectId: number) =>
    http.get<ModuleDTO[]>(`/projects/${projectId}/modules`),
  create: (projectId: number, data: { name: string; parentId?: number | null; sortOrder?: number }) =>
    http.post<ModuleDTO>(`/projects/${projectId}/modules`, data),
  update: (projectId: number, id: number, data: { name: string; sortOrder?: number }) =>
    http.put<ModuleDTO>(`/projects/${projectId}/modules/${id}`, data),
  remove: (projectId: number, id: number) =>
    http.delete(`/projects/${projectId}/modules/${id}`)
}
