import http from './http'

export interface ModulePermission {
  id: number
  name: string
  code: string
  children?: ModulePermission[]
  sort?: number
}

export const adminPermissionApi = {
  listModules: () => http.get('/admin/permissions/modules'),
  getRoleModulePermissions: (roleId: number) => http.get(`/admin/roles/${roleId}/module-permissions`),
  saveRoleModulePermissions: (roleId: number, permissionCodes: string[]) => http.put(`/admin/roles/${roleId}/module-permissions`, { permissionCodes })
}
