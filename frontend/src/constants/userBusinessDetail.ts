import type { ThemeLocale } from './theme'

export const USER_BUSINESS_DETAIL_I18N: Record<ThemeLocale, {
  back: string
  tabs: { schedule: string; requirements: string; bugs: string }
  labels: {
    employeeNo: string
    department: string
    email: string
    phone: string
    title: string
    requirement: string
    time: string
    status: string
    priority: string
    dueDate: string
    severity: string
    createdAt: string
  }
  empty: { schedule: string; requirements: string; bugs: string }
}> = {
  'zh-CN': {
    back: '返回团队',
    tabs: { schedule: '日程', requirements: '需求', bugs: 'Bug' },
    labels: {
      employeeNo: '工号', department: '部门', email: '邮箱', phone: '电话', title: '标题',
      requirement: '所属需求', time: '时间', status: '状态', priority: '优先级',
      dueDate: '截止日期', severity: '严重程度', createdAt: '创建时间'
    },
    empty: { schedule: '暂无日程。', requirements: '暂无关联需求。', bugs: '暂无关联 Bug。' }
  },
  'en-US': {
    back: 'Back to Team',
    tabs: { schedule: 'Schedule', requirements: 'Requirements', bugs: 'Bugs' },
    labels: {
      employeeNo: 'Employee No', department: 'Department', email: 'Email', phone: 'Phone', title: 'Title',
      requirement: 'Requirement', time: 'Time', status: 'Status', priority: 'Priority',
      dueDate: 'Due Date', severity: 'Severity', createdAt: 'Created At'
    },
    empty: { schedule: 'No schedule items.', requirements: 'No related requirements.', bugs: 'No related bugs.' }
  }
}

