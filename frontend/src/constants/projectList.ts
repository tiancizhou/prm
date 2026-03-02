import type { ThemeLocale } from './theme'

type ProjectStatus = 'ACTIVE' | 'ARCHIVED' | 'CLOSED'

export const PROJECT_LIST_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    buttons: {
      newProject: string
      reset: string
      enter: string
      archive: string
      cancel: string
      create: string
    }
    aria: {
      searchProject: string
      statusFilter: string
    }
    placeholders: {
      searchProject: string
      allStatus: string
      projectName: string
      projectCode: string
      projectDescription: string
      rangeSeparator: string
      startDate: string
      endDate: string
    }
    labels: {
      tableEmpty: string
      projectName: string
      code: string
      status: string
      owner: string
      startDate: string
      endDate: string
      actions: string
      description: string
      visibility: string
      dateRange: string
    }
    dialogs: {
      createProject: string
      confirmArchiveTitle: string
    }
    visibilityOptions: Array<{ value: 'PRIVATE' | 'PUBLIC'; label: string }>
    statusOptions: Array<{ value: ProjectStatus; label: string }>
    statusLabels: Record<ProjectStatus, string>
    rules: {
      projectNameRequired: string
      projectCodeRequired: string
    }
    messages: {
      projectCreated: string
      projectArchived: string
    }
    confirms: {
      archiveProject: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: '项目列表',
    pageSubtitle: '查看项目状态、负责人和时间计划，快速进入执行空间。',
    buttons: {
      newProject: '新建项目',
      reset: '重置',
      enter: '进入',
      archive: '归档',
      cancel: '取消',
      create: '创建'
    },
    aria: {
      searchProject: '搜索项目',
      statusFilter: '项目状态筛选'
    },
    placeholders: {
      searchProject: '搜索项目名称或代号…',
      allStatus: '全部状态',
      projectName: '例如：PRM 平台',
      projectCode: '例如：PRM',
      projectDescription: '可选，描述项目目标与范围',
      rangeSeparator: '至',
      startDate: '开始日期',
      endDate: '结束日期'
    },
    labels: {
      tableEmpty: '暂无项目数据',
      projectName: '项目名称',
      code: '代号',
      status: '状态',
      owner: '负责人',
      startDate: '开始日期',
      endDate: '结束日期',
      actions: '操作',
      description: '项目描述',
      visibility: '可见范围',
      dateRange: '日期范围'
    },
    dialogs: {
      createProject: '新建项目',
      confirmArchiveTitle: '确认操作'
    },
    visibilityOptions: [
      { value: 'PRIVATE', label: '私有' },
      { value: 'PUBLIC', label: '公开' }
    ],
    statusOptions: [
      { value: 'ACTIVE', label: '进行中' },
      { value: 'ARCHIVED', label: '已归档' },
      { value: 'CLOSED', label: '已关闭' }
    ],
    statusLabels: {
      ACTIVE: '进行中',
      ARCHIVED: '已归档',
      CLOSED: '已关闭'
    },
    rules: {
      projectNameRequired: '项目名称不能为空',
      projectCodeRequired: '项目代号不能为空'
    },
    messages: {
      projectCreated: '项目创建成功',
      projectArchived: '项目已归档'
    },
    confirms: {
      archiveProject: '确定归档项目“{name}”吗？'
    }
  },
  'en-US': {
    pageTitle: 'Project List',
    pageSubtitle: 'Review status, owner, and timeline, then quickly enter execution spaces.',
    buttons: {
      newProject: 'New Project',
      reset: 'Reset',
      enter: 'Enter',
      archive: 'Archive',
      cancel: 'Cancel',
      create: 'Create'
    },
    aria: {
      searchProject: 'Search projects',
      statusFilter: 'Project status filter'
    },
    placeholders: {
      searchProject: 'Search project name or code…',
      allStatus: 'All Statuses',
      projectName: 'e.g. PRM Platform',
      projectCode: 'e.g. PRM',
      projectDescription: 'Optional, describe goals and scope',
      rangeSeparator: 'to',
      startDate: 'Start Date',
      endDate: 'End Date'
    },
    labels: {
      tableEmpty: 'No projects yet',
      projectName: 'Project Name',
      code: 'Code',
      status: 'Status',
      owner: 'Owner',
      startDate: 'Start Date',
      endDate: 'End Date',
      actions: 'Actions',
      description: 'Description',
      visibility: 'Visibility',
      dateRange: 'Date Range'
    },
    dialogs: {
      createProject: 'Create Project',
      confirmArchiveTitle: 'Confirm Action'
    },
    visibilityOptions: [
      { value: 'PRIVATE', label: 'Private' },
      { value: 'PUBLIC', label: 'Public' }
    ],
    statusOptions: [
      { value: 'ACTIVE', label: 'Active' },
      { value: 'ARCHIVED', label: 'Archived' },
      { value: 'CLOSED', label: 'Closed' }
    ],
    statusLabels: {
      ACTIVE: 'Active',
      ARCHIVED: 'Archived',
      CLOSED: 'Closed'
    },
    rules: {
      projectNameRequired: 'Project name is required',
      projectCodeRequired: 'Project code is required'
    },
    messages: {
      projectCreated: 'Project created',
      projectArchived: 'Project archived'
    },
    confirms: {
      archiveProject: 'Archive project "{name}"?'
    }
  }
}

