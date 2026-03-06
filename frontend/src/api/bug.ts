import http from './http'

export const bugApi = {
  list: (params?: any) => http.get('/bugs', { params }),
  get: (id: number) => http.get(`/bugs/${id}`),
  create: (data: any) => http.post('/bugs', data),
  delete: (id: number) => http.delete(`/bugs/${id}`),
  updateStatus: (id: number, status: string, resolveType?: string) =>
    http.put(`/bugs/${id}/status`, null, { params: { status, resolveType } }),
  assign: (id: number, assigneeId: number) =>
    http.put(`/bugs/${id}/assign`, null, { params: { assigneeId } }),
  addComment: (id: number, content: string) =>
    http.post(`/bugs/${id}/comments`, null, { params: { content } }),
  listComments: (id: number) => http.get(`/bugs/${id}/comments`),
  convertToRequirement: (id: number) =>
    http.post(`/bugs/${id}/convert-to-requirement`)
}
