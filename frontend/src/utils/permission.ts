export const SUPER_ADMIN_ROLE = 'SUPER_ADMIN'

export const GLOBAL_NAV_PERMISSION_MAP = {
  dashboard: 'dashboard:view',
  projects: 'projects:view',
  organization: 'organization:view',
  admin: 'admin:view'
} as const

export const PROJECT_SECTION_PERMISSION_MAP = {
  overview: 'project.overview:view',
  requirements: 'requirement:view',
  bugs: 'bug:view',
  sprints: 'sprint:view',
  members: 'project.member:view'
} as const

export const ACTION_PERMISSION_MAP = {
  projectCreate: 'project:create',
  departmentCreate: 'department:create',
  departmentUpdate: 'department:update',
  departmentDelete: 'department:delete',
  userCreate: 'user:create',
  userUpdate: 'user:update',
  userDelete: 'user:delete',
  roleGroupCreate: 'role-group:create',
  roleGroupUpdate: 'role-group:update',
  roleGroupDelete: 'role-group:delete',
  permissionAssign: 'permission:assign',
  requirementCreate: 'requirement:create',
  requirementUpdate: 'requirement:update',
  requirementDelete: 'requirement:delete',
  requirementAssign: 'requirement:assign',
  bugCreate: 'bug:create',
  bugUpdate: 'bug:update',
  bugDelete: 'bug:delete',
  bugAssign: 'bug:assign'
} as const

export type GlobalNavPermissionKey = keyof typeof GLOBAL_NAV_PERMISSION_MAP
export type ProjectSectionPermissionKey = keyof typeof PROJECT_SECTION_PERMISSION_MAP

export function isSuperAdmin(roles?: string[] | null) {
  return Array.isArray(roles) && roles.includes(SUPER_ADMIN_ROLE)
}

export function hasPermission(roles?: string[] | null, permissions?: string[] | null, permissionCode?: string | null) {
  if (isSuperAdmin(roles)) {
    return true
  }
  if (!permissionCode) {
    return true
  }
  return Array.isArray(permissions) && permissions.includes(permissionCode)
}

export function firstAccessiblePath(roles?: string[] | null, permissions?: string[] | null) {
  if (hasPermission(roles, permissions, GLOBAL_NAV_PERMISSION_MAP.dashboard)) return '/dashboard'
  if (hasPermission(roles, permissions, GLOBAL_NAV_PERMISSION_MAP.projects)) return '/projects'
  if (hasPermission(roles, permissions, GLOBAL_NAV_PERMISSION_MAP.organization)) return '/organization/team'
  if (hasPermission(roles, permissions, GLOBAL_NAV_PERMISSION_MAP.admin)) return '/admin/departments'
  return '/dashboard'
}
