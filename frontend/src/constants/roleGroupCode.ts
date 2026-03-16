/**
 * 分组编码映射表
 * 系统内已知的角色分组编码，用于展示名称、标签样式、校验等。
 * 新增或变更分组编码时在此维护，保证前后端一致。
 */
export type RoleGroupTagType = 'danger' | 'warning' | 'primary' | 'success' | 'info'

export interface RoleGroupCodeItem {
  labelZh: string
  labelEn: string
  tagType: RoleGroupTagType
}

export const ROLE_GROUP_CODE_MAP: Record<string, RoleGroupCodeItem> = {
  SUPER_ADMIN: {
    labelZh: '超级管理员',
    labelEn: 'Super Administrator',
    tagType: 'danger'
  },
  PROJECT_ADMIN: {
    labelZh: '项目管理员',
    labelEn: 'Project Administrator',
    tagType: 'warning'
  },
  DEV: {
    labelZh: '开发',
    labelEn: 'Developer',
    tagType: 'primary'
  }
}

/** 已知分组编码列表，可用于校验或下拉建议 */
export const KNOWN_ROLE_GROUP_CODES = Object.keys(ROLE_GROUP_CODE_MAP) as string[]

export function getRoleGroupLabel(code: string, locale: 'zh-CN' | 'en-US'): string {
  const item = ROLE_GROUP_CODE_MAP[code]
  if (!item) return code
  return locale === 'zh-CN' ? item.labelZh : item.labelEn
}

export function getRoleGroupTagType(code: string): RoleGroupTagType {
  return ROLE_GROUP_CODE_MAP[code]?.tagType ?? 'info'
}
