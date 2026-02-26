import http from './http'

export const taskApi = {
  list: (params?: any) => http.get('/tasks', { params }),
  get: (id: number) => http.get(`/tasks/${id}`),
  create: (data: any) => http.post('/tasks', data),
  updateStatus: (id: number, status: string) =>
    http.put(`/tasks/${id}/status`, null, { params: { status } }),
  assign: (id: number, assigneeId: number | null) =>
    http.put(`/tasks/${id}/assign`, null, { params: assigneeId != null ? { assigneeId } : {} }),
  logWork: (id: number, spentHours: number, remark?: string) =>
    http.post(`/tasks/${id}/worklog`, null, { params: { spentHours, remark } })
}
