import type { ThemeLocale } from './theme'

export const DASHBOARD_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    requirementDelivery: string
    qualityStatus: string
    criticalSuffix: string
    requirementLabels: {
      totalRequirements: string
      doneRequirements: string
      completion: string
      inProgressTasks: string
    }
    qualityLabels: {
      totalBugs: string
      openBugs: string
      criticalOrBlocker: string
      overdueProjects: string
    }
    metricLabels: {
      activeProjects: string
      overdueProjects: string
      openBugs: string
      inProgressTasks: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: '工作台总览',
    pageSubtitle: '实时掌握项目推进、需求交付与质量状态。',
    requirementDelivery: '需求交付',
    qualityStatus: '质量状态',
    criticalSuffix: '严重',
    requirementLabels: {
      totalRequirements: '总需求数',
      doneRequirements: '已完成',
      completion: '完成率',
      inProgressTasks: '在制任务'
    },
    qualityLabels: {
      totalBugs: '总 Bug 数',
      openBugs: '未关闭',
      criticalOrBlocker: '严重/阻塞',
      overdueProjects: '延期项目'
    },
    metricLabels: {
      activeProjects: '进行中项目',
      overdueProjects: '延期项目',
      openBugs: '未关闭 Bug',
      inProgressTasks: '进行中任务'
    }
  },
  'en-US': {
    pageTitle: 'Dashboard Overview',
    pageSubtitle: 'Track project progress, requirement delivery, and quality status in real time.',
    requirementDelivery: 'Requirement Delivery',
    qualityStatus: 'Quality Status',
    criticalSuffix: 'Critical',
    requirementLabels: {
      totalRequirements: 'Total Requirements',
      doneRequirements: 'Completed',
      completion: 'Completion Rate',
      inProgressTasks: 'In Progress Tasks'
    },
    qualityLabels: {
      totalBugs: 'Total Bugs',
      openBugs: 'Open Bugs',
      criticalOrBlocker: 'Critical / Blocker',
      overdueProjects: 'Overdue Projects'
    },
    metricLabels: {
      activeProjects: 'Active Projects',
      overdueProjects: 'Overdue Projects',
      openBugs: 'Open Bugs',
      inProgressTasks: 'Tasks In Progress'
    }
  }
}
