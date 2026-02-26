import http from './http'

export const requirementApi = {
  list: (params?: any) => http.get('/requirements', { params }),
  get: (id: number) => http.get(`/requirements/${id}`),
  create: (data: any) => http.post('/requirements', data),
  updateStatus: (id: number, status: string) =>
    http.put(`/requirements/${id}/status`, null, { params: { status } }),
  addReview: (id: number, conclusion: string, remark?: string) =>
    http.post(`/requirements/${id}/review`, null, { params: { conclusion, remark } })
}
