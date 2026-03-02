import type { ThemeLocale } from './theme'

export const PROJECT_OVERVIEW_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    editDescription: string
    emptyDescription: string
    metaLabels: {
      owner: string
      start: string
      end: string
      visibility: string
      created: string
    }
    overdueTag: string
    kpiTitles: {
      progress: string
      openTasks: string
      openBugs: string
      sprintDaysLeft: string
    }
    requirementsCompleted: string
    overdueItems: string
    noOverdueTasks: string
    criticalCountSuffix: string
    noCriticalBugs: string
    dayUnit: string
    noActiveSprint: string
    trendTitle: string
    recent7Days: string
    refresh: string
    emptyTrend: string
    legendOpenTasks: string
    legendOpenBugs: string
    myTodos: string
    viewAllTasks: string
    emptyTodos: string
    prioritySuffix: string
    duePrefix: string
    recentActivity: string
    emptyActivities: string
    targetQuoteLeft: string
    targetQuoteRight: string
    dialogTitle: string
    dialogPlaceholder: string
    cancel: string
    save: string
    justNow: string
    minutesAgo: string
    hoursAgo: string
    daysAgo: string
    statusLabels: Record<string, string>
    visibilityLabels: { public: string; private: string }
    priorityLabels: Record<string, string>
    taskStatusLabels: Record<string, string>
    activityActionMap: Record<string, string>
    teamMemberFallback: string
    updatedFallback: string
    taskPrefix: string
    descUpdated: string
    updateFailed: string
  }
> = {
  'zh-CN': {
    pageTitle: '项目概览',
    pageSubtitle: '跟踪项目健康度、风险与团队执行态势。',
    editDescription: '编辑描述',
    emptyDescription: '暂无项目描述，点击右侧按钮添加。',
    metaLabels: {
      owner: '负责人',
      start: '开始',
      end: '截止',
      visibility: '可见范围',
      created: '创建'
    },
    overdueTag: '逾期',
    kpiTitles: {
      progress: '项目整体进度',
      openTasks: '待处理任务',
      openBugs: '未关闭 Bug',
      sprintDaysLeft: '当前迭代剩余'
    },
    requirementsCompleted: '需求已完成',
    overdueItems: '项逾期',
    noOverdueTasks: '✓ 无逾期任务',
    criticalCountSuffix: '个严重/阻塞',
    noCriticalBugs: '✓ 无严重 Bug',
    dayUnit: '天',
    noActiveSprint: '暂无活跃迭代',
    trendTitle: '任务 & Bug 趋势',
    recent7Days: '近 7 天',
    refresh: '刷新',
    emptyTrend: '暂无趋势数据',
    legendOpenTasks: '未完成任务',
    legendOpenBugs: '未关闭 Bug',
    myTodos: '我的待办',
    viewAllTasks: '查看全部任务',
    emptyTodos: '暂无待办任务，尽情摸鱼 🎉',
    prioritySuffix: '优先级',
    duePrefix: '截止',
    recentActivity: '最近动态',
    emptyActivities: '暂无近期动态',
    targetQuoteLeft: '「',
    targetQuoteRight: '」',
    dialogTitle: '编辑项目描述',
    dialogPlaceholder: '请输入项目描述…',
    cancel: '取消',
    save: '保存',
    justNow: '刚刚',
    minutesAgo: '分钟前',
    hoursAgo: '小时前',
    daysAgo: '天前',
    statusLabels: { ACTIVE: '进行中', ARCHIVED: '已归档', CLOSED: '已关闭' },
    visibilityLabels: { public: '公开', private: '私有' },
    priorityLabels: { HIGH: '高', MEDIUM: '中', LOW: '低' },
    taskStatusLabels: { TODO: '待处理', IN_PROGRESS: '进行中', DONE: '已完成', CLOSED: '已关闭' },
    activityActionMap: {
      DONE: '完成了任务',
      CLOSED: '关闭了任务',
      IN_PROGRESS: '开始处理',
      TODO: '创建了任务'
    },
    teamMemberFallback: '团队成员',
    updatedFallback: '更新了',
    taskPrefix: '任务',
    descUpdated: '项目描述已更新',
    updateFailed: '更新失败，请重试'
  },
  'en-US': {
    pageTitle: 'Project Overview',
    pageSubtitle: 'Track project health, risks, and team execution trends.',
    editDescription: 'Edit Description',
    emptyDescription: 'No project description yet. Click the button on the right to add one.',
    metaLabels: {
      owner: 'Owner',
      start: 'Start',
      end: 'End',
      visibility: 'Visibility',
      created: 'Created'
    },
    overdueTag: 'Overdue',
    kpiTitles: {
      progress: 'Overall Progress',
      openTasks: 'Open Tasks',
      openBugs: 'Open Bugs',
      sprintDaysLeft: 'Sprint Days Left'
    },
    requirementsCompleted: 'requirements completed',
    overdueItems: 'overdue',
    noOverdueTasks: '✓ No overdue tasks',
    criticalCountSuffix: 'critical/blocker',
    noCriticalBugs: '✓ No critical bugs',
    dayUnit: 'd',
    noActiveSprint: 'No active sprint',
    trendTitle: 'Task & Bug Trend',
    recent7Days: 'Last 7 Days',
    refresh: 'Refresh',
    emptyTrend: 'No trend data',
    legendOpenTasks: 'Open Tasks',
    legendOpenBugs: 'Open Bugs',
    myTodos: 'My Todos',
    viewAllTasks: 'View All Tasks',
    emptyTodos: 'No pending tasks. Enjoy your free time 🎉',
    prioritySuffix: ' priority',
    duePrefix: 'Due',
    recentActivity: 'Recent Activity',
    emptyActivities: 'No recent activity',
    targetQuoteLeft: '"',
    targetQuoteRight: '"',
    dialogTitle: 'Edit Project Description',
    dialogPlaceholder: 'Please enter project description…',
    cancel: 'Cancel',
    save: 'Save',
    justNow: 'just now',
    minutesAgo: 'm ago',
    hoursAgo: 'h ago',
    daysAgo: 'd ago',
    statusLabels: { ACTIVE: 'Active', ARCHIVED: 'Archived', CLOSED: 'Closed' },
    visibilityLabels: { public: 'Public', private: 'Private' },
    priorityLabels: { HIGH: 'High', MEDIUM: 'Medium', LOW: 'Low' },
    taskStatusLabels: { TODO: 'Todo', IN_PROGRESS: 'In Progress', DONE: 'Done', CLOSED: 'Closed' },
    activityActionMap: {
      DONE: 'completed task',
      CLOSED: 'closed task',
      IN_PROGRESS: 'started working on',
      TODO: 'created task'
    },
    teamMemberFallback: 'Team member',
    updatedFallback: 'updated',
    taskPrefix: 'Task',
    descUpdated: 'Project description updated',
    updateFailed: 'Update failed, please try again'
  }
}
