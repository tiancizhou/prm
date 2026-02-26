import http from './http'

export const sprintApi = {
  list: (params?: any) => http.get('/sprints', { params }),
  get: (id: number) => http.get(`/sprints/${id}`),
  create: (data: any) => http.post('/sprints', data),
  start: (id: number) => http.post(`/sprints/${id}/start`),
  close: (id: number) => http.post(`/sprints/${id}/close`)
}
