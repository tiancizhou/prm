import type { ThemeLocale } from './theme'

type BugStatus = 'NEW' | 'CONFIRMED' | 'ASSIGNED' | 'RESOLVED' | 'VERIFIED' | 'CLOSED'
type BugSeverity = 'BLOCKER' | 'CRITICAL' | 'NORMAL' | 'MINOR'

export const BUG_PAGE_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    buttons: {
      submitBug: string
      assign: string
      resolve: string
      verify: string
      cancel: string
      submit: string
    }
    aria: {
      statusFilter: string
      severityFilter: string
      keywordSearch: string
    }
    placeholders: {
      status: string
      severity: string
      keyword: string
      bugTitle: string
      module: string
      steps: string
      expectedResult: string
      actualResult: string
      environment: string
    }
    labels: {
      id: string
      title: string
      severity: string
      status: string
      assignee: string
      reporter: string
      createdAt: string
      actions: string
      bugTitle: string
      module: string
      priority: string
      steps: string
      expectedResult: string
      actualResult: string
      environment: string
      noneSymbol: string
    }
    dialogs: {
      submitBug: string
      assignTitle: string
      assignMessage: string
    }
    statusOptions: Array<{ value: BugStatus; label: string }>
    severityOptions: Array<{ value: BugSeverity; label: string }>
    priorityOptions: Array<{ value: 'HIGH' | 'MEDIUM' | 'LOW'; label: string }>
    statusLabels: Record<string, string>
    severityLabels: Record<string, string>
    messages: {
      submitSuccess: string
      assignSuccess: string
      resolveSuccess: string
      verifySuccess: string
      assigneeIdInvalid: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: 'Bug 管理',
    pageSubtitle: '统一追踪缺陷状态并推动修复闭环。',
    buttons: {
      submitBug: '提交 Bug',
      assign: '指派',
      resolve: '解决',
      verify: '验证',
      cancel: '取消',
      submit: '提交'
    },
    aria: {
      statusFilter: '按状态筛选',
      severityFilter: '按严重程度筛选',
      keywordSearch: '搜索 Bug 标题'
    },
    placeholders: {
      status: '状态',
      severity: '严重程度',
      keyword: '搜索标题',
      bugTitle: '请输入 Bug 标题',
      module: '请输入所属模块',
      steps: '请输入重现步骤',
      expectedResult: '请输入期望结果',
      actualResult: '请输入实际结果',
      environment: '请输入环境信息'
    },
    labels: {
      id: 'ID',
      title: '标题',
      severity: '严重程度',
      status: '状态',
      assignee: '处理人',
      reporter: '提报人',
      createdAt: '提报时间',
      actions: '操作',
      bugTitle: 'Bug 标题',
      module: '所属模块',
      priority: '优先级',
      steps: '重现步骤',
      expectedResult: '期望结果',
      actualResult: '实际结果',
      environment: '环境信息',
      noneSymbol: '—'
    },
    dialogs: {
      submitBug: '提交 Bug',
      assignTitle: '指派',
      assignMessage: '请输入处理人用户 ID'
    },
    statusOptions: [
      { value: 'NEW', label: '新建' },
      { value: 'CONFIRMED', label: '已确认' },
      { value: 'ASSIGNED', label: '已指派' },
      { value: 'RESOLVED', label: '已解决' },
      { value: 'VERIFIED', label: '已验证' },
      { value: 'CLOSED', label: '已关闭' }
    ],
    severityOptions: [
      { value: 'BLOCKER', label: '阻塞' },
      { value: 'CRITICAL', label: '严重' },
      { value: 'NORMAL', label: '一般' },
      { value: 'MINOR', label: '轻微' }
    ],
    priorityOptions: [
      { value: 'HIGH', label: '高' },
      { value: 'MEDIUM', label: '中' },
      { value: 'LOW', label: '低' }
    ],
    statusLabels: {
      NEW: '新建',
      CONFIRMED: '已确认',
      ASSIGNED: '已指派',
      RESOLVED: '已解决',
      VERIFIED: '已验证',
      CLOSED: '已关闭'
    },
    severityLabels: {
      BLOCKER: '阻塞',
      CRITICAL: '严重',
      NORMAL: '一般',
      MINOR: '轻微'
    },
    messages: {
      submitSuccess: '提交成功',
      assignSuccess: '已指派',
      resolveSuccess: '已标记解决',
      verifySuccess: '已验证',
      assigneeIdInvalid: '请输入有效的数字用户 ID'
    }
  },
  'en-US': {
    pageTitle: 'Bug Management',
    pageSubtitle: 'Track defect status consistently and drive resolution closure.',
    buttons: {
      submitBug: 'Submit Bug',
      assign: 'Assign',
      resolve: 'Resolve',
      verify: 'Verify',
      cancel: 'Cancel',
      submit: 'Submit'
    },
    aria: {
      statusFilter: 'Filter by status',
      severityFilter: 'Filter by severity',
      keywordSearch: 'Search bug title'
    },
    placeholders: {
      status: 'Status',
      severity: 'Severity',
      keyword: 'Search title',
      bugTitle: 'Please enter bug title',
      module: 'Please enter module',
      steps: 'Please enter reproduce steps',
      expectedResult: 'Please enter expected result',
      actualResult: 'Please enter actual result',
      environment: 'Please enter environment info'
    },
    labels: {
      id: 'ID',
      title: 'Title',
      severity: 'Severity',
      status: 'Status',
      assignee: 'Assignee',
      reporter: 'Reporter',
      createdAt: 'Reported At',
      actions: 'Actions',
      bugTitle: 'Bug Title',
      module: 'Module',
      priority: 'Priority',
      steps: 'Reproduce Steps',
      expectedResult: 'Expected Result',
      actualResult: 'Actual Result',
      environment: 'Environment',
      noneSymbol: '—'
    },
    dialogs: {
      submitBug: 'Submit Bug',
      assignTitle: 'Assign',
      assignMessage: 'Please enter assignee user ID'
    },
    statusOptions: [
      { value: 'NEW', label: 'New' },
      { value: 'CONFIRMED', label: 'Confirmed' },
      { value: 'ASSIGNED', label: 'Assigned' },
      { value: 'RESOLVED', label: 'Resolved' },
      { value: 'VERIFIED', label: 'Verified' },
      { value: 'CLOSED', label: 'Closed' }
    ],
    severityOptions: [
      { value: 'BLOCKER', label: 'Blocker' },
      { value: 'CRITICAL', label: 'Critical' },
      { value: 'NORMAL', label: 'Normal' },
      { value: 'MINOR', label: 'Minor' }
    ],
    priorityOptions: [
      { value: 'HIGH', label: 'High' },
      { value: 'MEDIUM', label: 'Medium' },
      { value: 'LOW', label: 'Low' }
    ],
    statusLabels: {
      NEW: 'New',
      CONFIRMED: 'Confirmed',
      ASSIGNED: 'Assigned',
      RESOLVED: 'Resolved',
      VERIFIED: 'Verified',
      CLOSED: 'Closed'
    },
    severityLabels: {
      BLOCKER: 'Blocker',
      CRITICAL: 'Critical',
      NORMAL: 'Normal',
      MINOR: 'Minor'
    },
    messages: {
      submitSuccess: 'Submitted successfully',
      assignSuccess: 'Assigned',
      resolveSuccess: 'Marked as resolved',
      verifySuccess: 'Verified',
      assigneeIdInvalid: 'Please input a valid numeric user ID'
    }
  }
}

