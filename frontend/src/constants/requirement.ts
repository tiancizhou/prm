import type { ThemeLocale } from './theme'

export const REQUIREMENT_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    viewLabels: {
      list: string
      kanbanSoon: string
      timelineSoon: string
    }
    buttons: {
      newRequirement: string
      moreActions: string
      import: string
      export: string
      apply: string
      view: string
      edit: string
      decompose: string
      status: string
      cancel: string
      save: string
      create: string
      close: string
      download: string
      delete: string
      uploadNewAttachment: string
      createTask: string
      decomposeToTask: string
      viewAllTasks: string
      autoCalculate: string
    }
    tooltips: {
      advancedFilter: string
      customColumns: string
    }
    placeholders: {
      searchTitle: string
      assignee: string
      sprint: string
      status: string
      requirementTitle: string
      taskTitle: string
      priority: string
      chooseStartDate: string
      chooseDueDate: string
      optional: string
      doneStartAt: string
      doneEndAt: string
    }
    quickTabs: Record<'all' | 'assigned' | 'due_week' | 'unscheduled', string>
    columnPickerTitle: string
    expand: {
      relatedTasks: string
      noRelatedTasks: string
      taskTitle: string
      type: string
      status: string
      assignee: string
      hours: string
      estimatedPrefix: string
      spentPrefix: string
      due: string
    }
    tableHeaders: {
      title: string
      priority: string
      status: string
      assignee: string
      startDate: string
      dueDate: string
      estimate: string
      actual: string
      taskCount: string
      actions: string
    }
    formLabels: {
      requirementTitle: string
      priority: string
      estimatedHours: string
      actualStartAt: string
      actualEndAt: string
      sprint: string
      startDate: string
      dueDate: string
      description: string
      acceptanceCriteria: string
      attachments: string
      owner: string
      belongsRequirement: string
      taskTitle: string
      taskType: string
    }
    dialogs: {
      createRequirement: string
      editRequirement: string
      requirementDetail: string
      decomposeTaskPrefix: string
      doneHoursTitle: string
    }
    detail: {
      attachments: string
      noAttachments: string
      estimatedHours: string
      actualHours: string
      noneSymbol: string
    }
    upload: {
      maxFilesWarning: string
      dropText: string
      clickText: string
      tip: string
    }
    pagination: {
      showing: string
      to: string
      of: string
      items: string
    }
    statusOptions: Array<{ value: 'DRAFT' | 'IN_PROGRESS' | 'DONE'; label: string }>
    columnOptions: Array<{ key: string; label: string }>
    priorityLabels: Record<string, string>
    nextStatusLabels: {
      start: string
      markDone: string
      reopen: string
    }
    taskTypeOptions: Array<{ value: 'TASK' | 'SUBTASK' | 'TECH'; label: string }>
    taskPriorityOptions: Array<{ value: 'LOW' | 'MEDIUM' | 'HIGH'; label: string }>
    messages: {
      importSoon: string
      exportSoon: string
      noManagePermission: string
      noEditPermission: string
      loadDetailFailed: string
      startDownload: string
      downloadFailed: string
      loadRequirementFailed: string
      confirmDeleteAttachment: string
      confirmTitle: string
      deleted: string
      deleteFailed: string
      uploadSuccess: string
      uploadFailed: string
      titleRequired: string
      requirementUpdated: string
      requirementCreated: string
      taskTitleRequired: string
      taskCreated: string
      statusUpdated: string
      doneTimeRequired: string
      doneTimeRangeInvalid: string
      doneHoursPrompt: string
      doneHoursInvalid: string
      autoCalculatedHours: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: '需求管理',
    pageSubtitle: '聚焦需求优先级与交付状态，支撑项目计划稳定推进。',
    viewLabels: {
      list: '列表视图',
      kanbanSoon: '看板视图（即将推出）',
      timelineSoon: '时间线视图（即将推出）'
    },
    buttons: {
      newRequirement: '新建需求',
      moreActions: '更多操作',
      import: '导入',
      export: '导出',
      apply: '应用',
      view: '查看',
      edit: '编辑',
      decompose: '拆解',
      status: '状态',
      cancel: '取消',
      save: '保存',
      create: '创建',
      close: '关闭',
      download: '下载',
      delete: '删除',
      uploadNewAttachment: '上传新附件',
      createTask: '创建任务',
      decomposeToTask: '拆解为任务',
      viewAllTasks: '查看全部任务',
      autoCalculate: '自动计算'
    },
    tooltips: {
      advancedFilter: '高级筛选',
      customColumns: '自定义列'
    },
    placeholders: {
      searchTitle: '搜索标题…',
      assignee: '负责人',
      sprint: '迭代',
      status: '状态',
      requirementTitle: '请输入需求标题',
      taskTitle: '请输入任务标题',
      priority: '优先级',
      chooseStartDate: '选择开始日期',
      chooseDueDate: '选择截止日期',
      optional: '选填',
      doneStartAt: '请选择开始时间',
      doneEndAt: '请选择结束时间'
    },
    quickTabs: {
      all: '全部',
      assigned: '分配给我',
      due_week: '本周到期',
      unscheduled: '未排期'
    },
    columnPickerTitle: '显示列',
    expand: {
      relatedTasks: '关联任务',
      noRelatedTasks: '暂无关联任务',
      taskTitle: '任务标题',
      type: '类型',
      status: '状态',
      assignee: '负责人',
      hours: '工时',
      estimatedPrefix: '预估',
      spentPrefix: '已用',
      due: '截止'
    },
    tableHeaders: {
      title: '标题',
      priority: '优先级',
      status: '状态',
      assignee: '负责人',
      startDate: '开始日期',
      dueDate: '截止日期',
      estimate: '预估',
      actual: '实际',
      taskCount: '任务数',
      actions: '操作'
    },
    formLabels: {
      requirementTitle: '需求标题',
      priority: '优先级',
      estimatedHours: '预估工时',
      actualStartAt: '实际开始',
      actualEndAt: '实际结束',
      sprint: '迭代',
      startDate: '开始日期',
      dueDate: '截止日期',
      description: '描述',
      acceptanceCriteria: '验收标准',
      attachments: '附件',
      owner: '负责人',
      belongsRequirement: '所属需求',
      taskTitle: '任务标题',
      taskType: '任务类型'
    },
    dialogs: {
      createRequirement: '新建需求',
      editRequirement: '编辑需求',
      requirementDetail: '需求详情',
      decomposeTaskPrefix: '拆解任务',
      doneHoursTitle: '填写完成时间'
    },
    detail: {
      attachments: '附件',
      noAttachments: '暂无附件',
      estimatedHours: '预估工时',
      actualHours: '实际工时',
      noneSymbol: '—'
    },
    upload: {
      maxFilesWarning: '最多上传 10 个文件',
      dropText: '将文件拖到此处，或',
      clickText: '点击选择',
      tip: '支持常见文档（PDF、Word、Excel、图片等），单个不超过 50MB'
    },
    pagination: {
      showing: '显示',
      to: '-',
      of: '条，共',
      items: '条'
    },
    statusOptions: [
      { value: 'DRAFT', label: '待办' },
      { value: 'IN_PROGRESS', label: '进行中' },
      { value: 'DONE', label: '已完成' }
    ],
    columnOptions: [
      { key: 'id', label: 'ID' },
      { key: 'title', label: '标题' },
      { key: 'priority', label: '优先级' },
      { key: 'status', label: '状态' },
      { key: 'assignee', label: '负责人' },
      { key: 'startDate', label: '开始日期' },
      { key: 'dueDate', label: '截止日期' },
      { key: 'estimate', label: '预估' },
      { key: 'actual', label: '实际' },
      { key: 'taskCount', label: '任务数' }
    ],
    priorityLabels: {
      LOW: '低',
      MEDIUM: '中',
      HIGH: '高',
      CRITICAL: '紧急'
    },
    nextStatusLabels: {
      start: '开始',
      markDone: '标记完成',
      reopen: '重新打开'
    },
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
    messages: {
      importSoon: '导入功能即将上线',
      exportSoon: '导出功能即将上线',
      noManagePermission: '仅项目经理可执行该操作',
      noEditPermission: '仅项目经理或需求负责人可变更状态',
      loadDetailFailed: '加载需求详情失败',
      startDownload: '开始下载',
      downloadFailed: '下载失败',
      loadRequirementFailed: '加载需求失败',
      confirmDeleteAttachment: '确定删除该附件？',
      confirmTitle: '提示',
      deleted: '已删除',
      deleteFailed: '删除失败',
      uploadSuccess: '上传成功',
      uploadFailed: '上传失败',
      titleRequired: '请输入需求标题',
      requirementUpdated: '需求已更新',
      requirementCreated: '需求创建成功',
      taskTitleRequired: '请输入任务标题',
      taskCreated: '任务创建成功',
      statusUpdated: '状态已更新',
      doneTimeRequired: '请填写开始时间和结束时间',
      doneTimeRangeInvalid: '结束时间必须晚于开始时间',
      doneHoursPrompt: '请输入完成工时，或点击“自动计算”',
      doneHoursInvalid: '请填写大于 0 的工时',
      autoCalculatedHours: '已按规则自动计算工时'
    }
  },
  'en-US': {
    pageTitle: 'Requirements',
    pageSubtitle: 'Focus on requirement priority and delivery status to keep project planning on track.',
    viewLabels: {
      list: 'List view',
      kanbanSoon: 'Kanban view (coming soon)',
      timelineSoon: 'Timeline view (coming soon)'
    },
    buttons: {
      newRequirement: 'New Requirement',
      moreActions: 'More actions',
      import: 'Import',
      export: 'Export',
      apply: 'Apply',
      view: 'View',
      edit: 'Edit',
      decompose: 'Decompose',
      status: 'Status',
      cancel: 'Cancel',
      save: 'Save',
      create: 'Create',
      close: 'Close',
      download: 'Download',
      delete: 'Delete',
      uploadNewAttachment: 'Upload New Attachment',
      createTask: 'Create Task',
      decomposeToTask: 'Decompose to Task',
      viewAllTasks: 'View All Tasks',
      autoCalculate: 'Auto Calculate'
    },
    tooltips: {
      advancedFilter: 'Advanced filters',
      customColumns: 'Customize columns'
    },
    placeholders: {
      searchTitle: 'Search by title…',
      assignee: 'Assignee',
      sprint: 'Sprint',
      status: 'Status',
      requirementTitle: 'Please enter requirement title',
      taskTitle: 'Please enter task title',
      priority: 'Priority',
      chooseStartDate: 'Select start date',
      chooseDueDate: 'Select due date',
      optional: 'Optional',
      doneStartAt: 'Select start time',
      doneEndAt: 'Select end time'
    },
    quickTabs: {
      all: 'All',
      assigned: 'Assigned to Me',
      due_week: 'Due This Week',
      unscheduled: 'Unscheduled'
    },
    columnPickerTitle: 'Visible Columns',
    expand: {
      relatedTasks: 'Related Tasks',
      noRelatedTasks: 'No related tasks',
      taskTitle: 'Task Title',
      type: 'Type',
      status: 'Status',
      assignee: 'Assignee',
      hours: 'Hours',
      estimatedPrefix: 'Est.',
      spentPrefix: 'Spent',
      due: 'Due'
    },
    tableHeaders: {
      title: 'Title',
      priority: 'Priority',
      status: 'Status',
      assignee: 'Assignee',
      startDate: 'Start Date',
      dueDate: 'Due Date',
      estimate: 'Estimate',
      actual: 'Actual',
      taskCount: 'Tasks',
      actions: 'Actions'
    },
    formLabels: {
      requirementTitle: 'Requirement Title',
      priority: 'Priority',
      estimatedHours: 'Estimated Hours',
      actualStartAt: 'Actual Start',
      actualEndAt: 'Actual End',
      sprint: 'Sprint',
      startDate: 'Start Date',
      dueDate: 'Due Date',
      description: 'Description',
      acceptanceCriteria: 'Acceptance Criteria',
      attachments: 'Attachments',
      owner: 'Assignee',
      belongsRequirement: 'Requirement',
      taskTitle: 'Task Title',
      taskType: 'Task Type'
    },
    dialogs: {
      createRequirement: 'Create Requirement',
      editRequirement: 'Edit Requirement',
      requirementDetail: 'Requirement Details',
      decomposeTaskPrefix: 'Decompose Task',
      doneHoursTitle: 'Completion Time Range'
    },
    detail: {
      attachments: 'Attachments',
      noAttachments: 'No attachments',
      estimatedHours: 'Estimated Hours',
      actualHours: 'Actual Hours',
      noneSymbol: '—'
    },
    upload: {
      maxFilesWarning: 'Up to 10 files are allowed',
      dropText: 'Drop files here, or',
      clickText: 'click to choose',
      tip: 'Supports PDF, Word, Excel, images and more. Max 50MB per file.'
    },
    pagination: {
      showing: 'Showing',
      to: '-',
      of: 'of',
      items: 'items'
    },
    statusOptions: [
      { value: 'DRAFT', label: 'Todo' },
      { value: 'IN_PROGRESS', label: 'In Progress' },
      { value: 'DONE', label: 'Done' }
    ],
    columnOptions: [
      { key: 'id', label: 'ID' },
      { key: 'title', label: 'Title' },
      { key: 'priority', label: 'Priority' },
      { key: 'status', label: 'Status' },
      { key: 'assignee', label: 'Assignee' },
      { key: 'startDate', label: 'Start Date' },
      { key: 'dueDate', label: 'Due Date' },
      { key: 'estimate', label: 'Estimate' },
      { key: 'actual', label: 'Actual' },
      { key: 'taskCount', label: 'Task Count' }
    ],
    priorityLabels: {
      LOW: 'Low',
      MEDIUM: 'Medium',
      HIGH: 'High',
      CRITICAL: 'Critical'
    },
    nextStatusLabels: {
      start: 'Start',
      markDone: 'Mark Done',
      reopen: 'Reopen'
    },
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
    messages: {
      importSoon: 'Import feature is coming soon',
      exportSoon: 'Export feature is coming soon',
      noManagePermission: 'Only project managers can perform this action',
      noEditPermission: 'Only project managers or assignees can change status',
      loadDetailFailed: 'Failed to load requirement details',
      startDownload: 'Download started',
      downloadFailed: 'Download failed',
      loadRequirementFailed: 'Failed to load requirement',
      confirmDeleteAttachment: 'Are you sure you want to delete this attachment?',
      confirmTitle: 'Confirmation',
      deleted: 'Deleted',
      deleteFailed: 'Delete failed',
      uploadSuccess: 'Upload successful',
      uploadFailed: 'Upload failed',
      titleRequired: 'Please enter requirement title',
      requirementUpdated: 'Requirement updated',
      requirementCreated: 'Requirement created successfully',
      taskTitleRequired: 'Please enter task title',
      taskCreated: 'Task created successfully',
      statusUpdated: 'Status updated',
      doneTimeRequired: 'Please provide both start and end time',
      doneTimeRangeInvalid: 'End time must be later than start time',
      doneHoursPrompt: 'Enter completion hours, or click Auto Calculate',
      doneHoursInvalid: 'Please enter hours greater than 0',
      autoCalculatedHours: 'Hours were auto-calculated by rule'
    }
  }
}
