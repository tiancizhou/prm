import type { ThemeLocale } from './theme'

export const DASHBOARD_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    greeting: (name: string, period: string) => string
    greetingPeriod: { morning: string; afternoon: string; evening: string }
    assignedToMe: string
    bugCount: string
    taskCount: string
    requirementCount: string
    completedTitle: string
    completedRequirementCount: string
    resolvedBugCount: string
    summaryMessage: string
    dashboardTabs: { overview: string; todo: string }
    scheduleTitle: string
    scheduleAssignedToMe: string
    scheduleSearchPlaceholder: string
    scheduleEmpty: string
    todoTitle: string
    todoAssignedToMe: string
    todoUnfinished: string
    todoToday: string
    todoThisWeek: string
    todoThisMonth: string
    todoMore: string
    todoSearchPlaceholder: string
    todoAdd: string
    todoEmpty: string
  }
> = {
  'zh-CN': {
    pageTitle: '工作台',
    pageSubtitle: '您的开发任务与待办一览。',
    greeting: (name: string, period: string) => `${name}，${period}`,
    greetingPeriod: {
      morning: '早上好',
      afternoon: '下午好',
      evening: '晚上好'
    },
    assignedToMe: '指派给我',
    bugCount: 'BUG数',
    taskCount: '任务数',
    requirementCount: '需求数',
    completedTitle: '已完成',
    completedRequirementCount: '已完成需求',
    resolvedBugCount: '已解决 Bug',
    summaryMessage: '今日期待优秀的您来处理任务与 Bug。',
    dashboardTabs: {
      overview: '仪表盘',
      todo: '待处理'
    },
    scheduleTitle: '日程',
    scheduleAssignedToMe: '指派给我',
    scheduleSearchPlaceholder: '搜索',
    scheduleEmpty: '暂时没有需求。',
    todoTitle: '待处理',
    todoAssignedToMe: '指派给我',
    todoUnfinished: '未完成',
    todoToday: '今日',
    todoThisWeek: '本周',
    todoThisMonth: '本月',
    todoMore: '更多',
    todoSearchPlaceholder: '搜索',
    todoAdd: '添加待办',
    todoEmpty: '暂时没有待办。'
  },
  'en-US': {
    pageTitle: 'Dashboard',
    pageSubtitle: 'Your development tasks and assignments at a glance.',
    greeting: (name: string, period: string) => `${name}, ${period}`,
    greetingPeriod: {
      morning: 'Good morning',
      afternoon: 'Good afternoon',
      evening: 'Good evening'
    },
    assignedToMe: 'Assigned to Me',
    bugCount: 'Bugs',
    taskCount: 'Tasks',
    requirementCount: 'Requirements',
    completedTitle: 'Completed',
    completedRequirementCount: 'Requirements Done',
    resolvedBugCount: 'Bugs Resolved',
    summaryMessage: 'Ready for you to tackle today.',
    dashboardTabs: {
      overview: 'Overview',
      todo: 'To-Do'
    },
    scheduleTitle: 'Schedule',
    scheduleAssignedToMe: 'Assigned to me',
    scheduleSearchPlaceholder: 'Search',
    scheduleEmpty: 'No requirements yet.',
    todoTitle: 'To-Do',
    todoAssignedToMe: 'Assigned to me',
    todoUnfinished: 'Unfinished',
    todoToday: 'Today',
    todoThisWeek: 'This week',
    todoThisMonth: 'This month',
    todoMore: 'More',
    todoSearchPlaceholder: 'Search',
    todoAdd: 'Add To-Do',
    todoEmpty: 'No to-dos yet.'
  }
}
