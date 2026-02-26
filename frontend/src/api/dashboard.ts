import http from './http'

export const dashboardApi = {
  overview: (projectId?: number) =>
    http.get('/dashboard/overview', { params: projectId ? { projectId } : {} }),
  aggregate: (projectId?: number) =>
    http.post('/dashboard/aggregate', null, { params: projectId ? { projectId } : {} })
}
