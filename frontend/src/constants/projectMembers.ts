import type { ThemeLocale } from './theme'

export const PROJECT_MEMBERS_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    buttons: {
      addMember: string
      remove: string
      cancel: string
      add: string
    }
    labels: {
      employeeNo: string
      realName: string
      username: string
      systemRole: string
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
    }
    confirms: {
      removeMember: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: '项目成员',
    pageSubtitle: '维护项目参与成员，确保角色协作关系清晰透明。',
    buttons: {
      addMember: '添加成员',
      remove: '移除',
      cancel: '取消',
      add: '添加'
    },
    labels: {
      employeeNo: '工号',
      realName: '姓名',
      username: '用户名',
      systemRole: '系统角色',
      actions: '操作',
      selectMember: '选择成员',
      noneSymbol: '—'
    },
    aria: {
      searchAddableMembers: '搜索可添加成员'
    },
    placeholders: {
      searchMembers: '输入工号或姓名搜索'
    },
    dialogs: {
      addMember: '添加成员',
      confirmTitle: '确认'
    },
    messages: {
      selectMemberFirst: '请选择成员',
      added: '添加成功',
      removed: '已移除'
    },
    confirms: {
      removeMember: '确定移除成员「{name}」？'
    }
  },
  'en-US': {
    pageTitle: 'Project Members',
    pageSubtitle: 'Maintain project participants and keep role collaboration transparent.',
    buttons: {
      addMember: 'Add Member',
      remove: 'Remove',
      cancel: 'Cancel',
      add: 'Add'
    },
    labels: {
      employeeNo: 'Employee No',
      realName: 'Name',
      username: 'Username',
      systemRole: 'System Role',
      actions: 'Actions',
      selectMember: 'Select Member',
      noneSymbol: '—'
    },
    aria: {
      searchAddableMembers: 'Search addable members'
    },
    placeholders: {
      searchMembers: 'Search by employee no or name'
    },
    dialogs: {
      addMember: 'Add Member',
      confirmTitle: 'Confirmation'
    },
    messages: {
      selectMemberFirst: 'Please select a member',
      added: 'Added successfully',
      removed: 'Removed'
    },
    confirms: {
      removeMember: 'Remove member "{name}"?'
    }
  }
}

