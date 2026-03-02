import type { ThemeLocale } from './theme'

type SprintStatus = 'PLANNING' | 'ACTIVE' | 'CLOSED'

export const SPRINT_PAGE_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    buttons: {
      createSprint: string
      startSprint: string
      closeSprint: string
      cancel: string
      create: string
    }
    labels: {
      noGoal: string
      startDate: string
      endDate: string
      capacity: string
      name: string
      goal: string
      capacityHours: string
      dateRange: string
      hourSuffix: string
    }
    placeholders: {
      sprintName: string
      sprintGoal: string
      rangeSeparator: string
      startDate: string
      endDate: string
    }
    dialogs: {
      createSprint: string
      confirmTitle: string
    }
    confirms: {
      startSprint: string
      closeSprint: string
    }
    statusLabels: Record<SprintStatus, string>
    messages: {
      createSuccess: string
      started: string
      closed: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: '迭代管理',
    pageSubtitle: '规划冲刺目标与时间窗口，推动团队节奏稳定交付。',
    buttons: {
      createSprint: '创建迭代',
      startSprint: '开始迭代',
      closeSprint: '关闭迭代',
      cancel: '取消',
      create: '创建'
    },
    labels: {
      noGoal: '暂无目标',
      startDate: '开始',
      endDate: '结束',
      capacity: '容量',
      name: '迭代名称',
      goal: '迭代目标',
      capacityHours: '容量（小时）',
      dateRange: '时间范围',
      hourSuffix: 'h'
    },
    placeholders: {
      sprintName: '请输入迭代名称',
      sprintGoal: '请输入迭代目标',
      rangeSeparator: '至',
      startDate: '开始日期',
      endDate: '结束日期'
    },
    dialogs: {
      createSprint: '创建迭代',
      confirmTitle: '确认'
    },
    confirms: {
      startSprint: '确定开始迭代 “{name}” 吗？',
      closeSprint: '确定关闭迭代 “{name}” 吗？系统将检查未关闭的严重 Bug。'
    },
    statusLabels: {
      PLANNING: '规划中',
      ACTIVE: '进行中',
      CLOSED: '已关闭'
    },
    messages: {
      createSuccess: '创建成功',
      started: '迭代已开始',
      closed: '迭代已关闭'
    }
  },
  'en-US': {
    pageTitle: 'Sprint Management',
    pageSubtitle: 'Plan sprint goals and time windows to keep team delivery on cadence.',
    buttons: {
      createSprint: 'Create Sprint',
      startSprint: 'Start Sprint',
      closeSprint: 'Close Sprint',
      cancel: 'Cancel',
      create: 'Create'
    },
    labels: {
      noGoal: 'No goal yet',
      startDate: 'Start',
      endDate: 'End',
      capacity: 'Capacity',
      name: 'Sprint Name',
      goal: 'Sprint Goal',
      capacityHours: 'Capacity (Hours)',
      dateRange: 'Date Range',
      hourSuffix: 'h'
    },
    placeholders: {
      sprintName: 'Please enter sprint name',
      sprintGoal: 'Please enter sprint goal',
      rangeSeparator: 'to',
      startDate: 'Start Date',
      endDate: 'End Date'
    },
    dialogs: {
      createSprint: 'Create Sprint',
      confirmTitle: 'Confirmation'
    },
    confirms: {
      startSprint: 'Start sprint "{name}"?',
      closeSprint: 'Close sprint "{name}"? The system will check unresolved severe bugs.'
    },
    statusLabels: {
      PLANNING: 'Planning',
      ACTIVE: 'Active',
      CLOSED: 'Closed'
    },
    messages: {
      createSuccess: 'Created successfully',
      started: 'Sprint started',
      closed: 'Sprint closed'
    }
  }
}

