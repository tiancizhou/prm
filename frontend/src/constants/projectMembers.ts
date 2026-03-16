import type { ThemeLocale } from './theme'

export const PROJECT_MEMBERS_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    helperText: string
    buttons: {
      addMember: string
      remove: string
      cancel: string
      add: string
    }
    labels: {
      member: string
      systemRole: string
      joinedAt: string
      actions: string
      selectMember: string
      noneSymbol: string
    }
    aria: {
      searchAddableMembers: string
    }
    placeholders: {
      searchMembers: string
    }
    dialogs: {
      addMember: string
      confirmTitle: string
    }
    messages: {
      selectMemberFirst: string
      added: string
      removed: string
      noPermission: string
    }
    confirms: {
      removeMember: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: '项目成员',
    pageSubtitle: '维护当前项目的参与成员与数据范围。',
    helperText: '系统角色决定操作权限；成员加入当前项目后，才能查看当前项目数据。',
    buttons: {
      addMember: '添加成员',
      remove: '移除',
      cancel: '取消',
      add: '添加'
    },
    labels: {
      member: '成员',
      systemRole: '系统角色',
      joinedAt: '加入时间',
      actions: '操作',
      selectMember: '选择成员',
      noneSymbol: '—'
    },
    aria: {
      searchAddableMembers: '搜索可添加成员'
    },
    placeholders: {
      searchMembers: '输入姓名、用户名或工号搜索'
    },
    dialogs: {
      addMember: '添加到当前项目',
      confirmTitle: '确认'
    },
    messages: {
      selectMemberFirst: '请选择成员',
      added: '添加成功',
      removed: '已移除',
      noPermission: '当前系统角色无权维护项目成员'
    },
    confirms: {
      removeMember: '确定将成员「{name}」移出当前项目？'
    }
  },
  'en-US': {
    pageTitle: 'Project Members',
    pageSubtitle: 'Maintain current project participants and their data scope.',
    helperText: 'System roles define what a user can do. Users can view this project only after they join it.',
    buttons: {
      addMember: 'Add Member',
      remove: 'Remove',
      cancel: 'Cancel',
      add: 'Add'
    },
    labels: {
      member: 'Member',
      systemRole: 'System Role',
      joinedAt: 'Joined Time',
      actions: 'Actions',
      selectMember: 'Select Member',
      noneSymbol: '—'
    },
    aria: {
      searchAddableMembers: 'Search addable members'
    },
    placeholders: {
      searchMembers: 'Search by name, username, or employee number'
    },
    dialogs: {
      addMember: 'Add to Current Project',
      confirmTitle: 'Confirmation'
    },
    messages: {
      selectMemberFirst: 'Please select a member',
      added: 'Added successfully',
      removed: 'Removed',
      noPermission: 'Your current system role cannot maintain project members'
    },
    confirms: {
      removeMember: 'Remove member "{name}" from the current project?'
    }
  }
}
