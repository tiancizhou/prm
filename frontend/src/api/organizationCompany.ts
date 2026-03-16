import http from './http'

export interface CompanyProfile {
  id?: number | null
  name: string
  shortName?: string | null
  contactName?: string | null
  phone?: string | null
  email?: string | null
  address?: string | null
  description?: string | null
  status?: string | null
}

export const organizationCompanyApi = {
  get: () => http.get('/organization/company'),
  update: (data: CompanyProfile) => http.put('/organization/company', data)
}
