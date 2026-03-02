import type { ThemeLocale } from './theme'

type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'PENDING_REVIEW' | 'DONE'

export const TASK_PAGE_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    aria: {
      viewMode: string
      filterRequirement: string
    }
    buttons: {
      newTask: string
      myTasks: string
      allTasks: string
      actions: string
      status: string
      logWork: string
      cancel: string
      confirm: string
      create: string
    }
    statusActionPrefix: string
    placeholders: {
      filterRequirement: string
      assign: string
      unassigned: string
      optional: string
      taskTitle: string
      selectRequirement: string
      selectAssignee: string
    }
    viewLabels: {
      board: string
      list: string
    }
    labels: {
      requirementPrefix: string
      noneSymbol: string
      title: string
      requirement: string
      type: string
      status: string
      assignee: string
      hours: string
      estimatedPrefix: string
      spentPrefix: string
      dueDate: string
      actions: string
      task: string
      spentHours: string
      remark: string
      relatedRequirement: string
      taskTitle: string
      taskType: string
      priority: string
      estimatedHours: string
    }
    dialogs: {
      worklog: string
      createTask: string
    }
    units: {
      hour: string
    }
    statusOptions: Array<{ value: TaskStatus; label: string }>
    taskTypeOptions: Array<{ value: 'TASK' | 'SUBTASK' | 'TECH'; label: string }>
    taskPriorityOptions: Array<{ value: 'LOW' | 'MEDIUM' | 'HIGH'; label: string }>
    statusLabels: Record<string, string>
    priorityLabels: Record<string, string>
    taskTypeLabels: Record<string, string>
    messages: {
      assigned: string
      unassigned: string
      titleRequired: string
      createSuccess: string
      statusUpdated: string
      worklogSuccess: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: '任务管理',
    pageSubtitle: '按需求、负责人与状态跟踪执行进展，支持看板与列表双视图。',
    aria: {
      viewMode: '任务视图模式',
      filterRequirement: '按需求筛选任务'
    },
    buttons: {
      newTask: '新建任务',
      myTasks: '我的任务',
      allTasks: '全部任务',
      actions: '操作',
      status: '状态',
      logWork: '登记工时',
      cancel: '取消',
      confirm: '确定',
      create: '创建'
    },
    statusActionPrefix: '改为',
    placeholders: {
      filterRequirement: '按需求筛选',
      assign: '指派',
      unassigned: '未指派',
      optional: '可选',
      taskTitle: '请输入任务标题',
      selectRequirement: '可选，选择所属需求',
      selectAssignee: '可选'
    },
    viewLabels: {
      board: '看板',
      list: '列表'
    },
    labels: {
      requirementPrefix: '需求',
      noneSymbol: '—',
      title: '标题',
      requirement: '所属需求',
      type: '类型',
      status: '状态',
      assignee: '负责人',
      hours: '工时',
      estimatedPrefix: '预估',
      spentPrefix: '已用',
      dueDate: '截止日期',
      actions: '操作',
      task: '任务',
      spentHours: '已用工时',
      remark: '备注',
      relatedRequirement: '关联需求',
      taskTitle: '任务标题',
      taskType: '任务类型',
      priority: '优先级',
      estimatedHours: '预估工时'
    },
    dialogs: {
      worklog: '登记工时',
      createTask: '新建任务'
    },
    units: {
      hour: '小时'
    },
    statusOptions: [
      { value: 'TODO', label: '待处理' },
      { value: 'IN_PROGRESS', label: '进行中' },
      { value: 'PENDING_REVIEW', label: '待验收' },
      { value: 'DONE', label: '已完成' }
    ],
    taskTypeOptions: [
      { value: 'TASK', label: '任务' },
      { value: 'SUBTASK', label: '子任务' },
      { value: 'TECH', label: '技术优化' }
    ],
    taskPriorityOptions: [
      { value: 'LOW', label: '低' },
      { value: 'MEDIUM', label: '中' },
      { value: 'HIGH', label: '高' }
    ],
    statusLabels: {
      TODO: '待处理',
      IN_PROGRESS: '进行中',
      PENDING_REVIEW: '待验收',
      DONE: '已完成'
    },
    priorityLabels: {
      LOW: '低',
      MEDIUM: '中',
      HIGH: '高',
      CRITICAL: '紧急'
    },
    taskTypeLabels: {
      TASK: '任务',
      SUBTASK: '子任务',
      TECH: '技术优化'
    },
    messages: {
      assigned: '已指派',
      unassigned: '已取消指派',
      titleRequired: '请输入任务标题',
      createSuccess: '创建成功',
      statusUpdated: '状态已更新',
      worklogSuccess: '工时已登记'
    }
  },
  'en-US': {
    pageTitle: 'Task Management',
    pageSubtitle: 'Track delivery by requirement, assignee, and status with board and list views.',
    aria: {
      viewMode: 'Task view mode',
      filterRequirement: 'Filter tasks by requirement'
    },
    buttons: {
      newTask: 'New Task',
      myTasks: 'My Tasks',
      allTasks: 'All Tasks',
      actions: 'Actions',
      status: 'Status',
      logWork: 'Log Work',
      cancel: 'Cancel',
      confirm: 'Confirm',
      create: 'Create'
    },
    statusActionPrefix: 'Change to',
    placeholders: {
      filterRequirement: 'Filter by requirement',
      assign: 'Assign',
      unassigned: 'Unassigned',
      optional: 'Optional',
      taskTitle: 'Please enter task title',
      selectRequirement: 'Optional, choose related requirement',
      selectAssignee: 'Optional'
    },
    viewLabels: {
      board: 'Board',
      list: 'List'
    },
    labels: {
      requirementPrefix: 'Req',
      noneSymbol: '—',
      title: 'Title',
      requirement: 'Requirement',
      type: 'Type',
      status: 'Status',
      assignee: 'Assignee',
      hours: 'Hours',
      estimatedPrefix: 'Est.',
      spentPrefix: 'Spent',
      dueDate: 'Due Date',
      actions: 'Actions',
      task: 'Task',
      spentHours: 'Spent Hours',
      remark: 'Remark',
      relatedRequirement: 'Related Requirement',
      taskTitle: 'Task Title',
      taskType: 'Task Type',
      priority: 'Priority',
      estimatedHours: 'Estimated Hours'
    },
    dialogs: {
      worklog: 'Log Work',
      createTask: 'Create Task'
    },
    units: {
      hour: 'hours'
    },
    statusOptions: [
      { value: 'TODO', label: 'Todo' },
      { value: 'IN_PROGRESS', label: 'In Progress' },
      { value: 'PENDING_REVIEW', label: 'Pending Review' },
      { value: 'DONE', label: 'Done' }
    ],
    taskTypeOptions: [
      { value: 'TASK', label: 'Task' },
      { value: 'SUBTASK', label: 'Subtask' },
      { value: 'TECH', label: 'Tech Improvement' }
    ],
    taskPriorityOptions: [
      { value: 'LOW', label: 'Low' },
      { value: 'MEDIUM', label: 'Medium' },
      { value: 'HIGH', label: 'High' }
    ],
    statusLabels: {
      TODO: 'Todo',
      IN_PROGRESS: 'In Progress',
      PENDING_REVIEW: 'Pending Review',
      DONE: 'Done'
    },
    priorityLabels: {
      LOW: 'Low',
      MEDIUM: 'Medium',
      HIGH: 'High',
      CRITICAL: 'Critical'
    },
    taskTypeLabels: {
      TASK: 'Task',
      SUBTASK: 'Subtask',
      TECH: 'Tech Improvement'
    },
    messages: {
      assigned: 'Assigned',
      unassigned: 'Unassigned',
      titleRequired: 'Please enter task title',
      createSuccess: 'Created successfully',
      statusUpdated: 'Status updated',
      worklogSuccess: 'Work logged'
    }
  }
}

