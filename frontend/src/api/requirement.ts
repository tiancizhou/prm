import http from './http'

export const requirementApi = {
  list: (params?: any) => http.get('/requirements', { params }),
  get: (id: number) => http.get(`/requirements/${id}`),
  create: (data: any) => http.post('/requirements', data),
  update: (id: number, data: any) => http.put(`/requirements/${id}`, data),
  updateStatus: (id: number, status: string) =>
    http.put(`/requirements/${id}/status`, null, { params: { status } }),
  addReview: (id: number, conclusion: string, remark?: string) =>
    http.post(`/requirements/${id}/review`, null, { params: { conclusion, remark } }),
  uploadAttachment: (requirementId: number, file: File) => {
    const formData = new FormData()
    formData.append('file', file)
    return http.post(`/requirements/${requirementId}/attachments`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  },
  listAttachments: (requirementId: number) =>
    http.get(`/requirements/${requirementId}/attachments`),
  deleteAttachment: (requirementId: number, attachmentId: number) =>
    http.delete(`/requirements/${requirementId}/attachments/${attachmentId}`),
  /** Download attachment and trigger browser save dialog. */
  downloadAttachment: async (requirementId: number, attachmentId: number, filename: string) => {
    const res = await http.get(
      `/requirements/${requirementId}/attachments/${attachmentId}/download`,
      { responseType: 'blob' }
    ) as { data: Blob }
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = filename || 'download'
    a.click()
    URL.revokeObjectURL(url)
  }
}

