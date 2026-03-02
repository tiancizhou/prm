import type { ThemeLocale } from './theme'

export const USER_PAGE_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    buttons: {
      newUser: string
      edit: string
      disable: string
      enable: string
      cancel: string
      create: string
      save: string
    }
    aria: {
      searchUsers: string
    }
    placeholders: {
      searchUsers: string
      syncFromMaster: string
      selectRoles: string
      username: string
      password: string
      realName: string
      employeeNo: string
      department: string
      team: string
      email: string
      phone: string
    }
    labels: {
      id: string
      employeeNo: string
      realName: string
      username: string
      department: string
      team: string
      email: string
      status: string
      roles: string
      actions: string
      active: string
      disabled: string
      password: string
      phone: string
    }
    dialogs: {
      createUser: string
      editUser: string
    }
    messages: {
      requiredUsernamePassword: string
      created: string
      saved: string
      updated: string
    }
  }
> = {
  'zh-CN': {
    pageTitle: '用户管理',
    pageSubtitle: '维护用户账号、角色与状态，保障组织权限配置清晰可控。',
    buttons: {
      newUser: '新建用户',
      edit: '编辑',
      disable: '禁用',
      enable: '启用',
      cancel: '取消',
      create: '创建',
      save: '保存'
    },
    aria: {
      searchUsers: '搜索用户'
    },
    placeholders: {
      searchUsers: '搜索 用户名/工号/姓名',
      syncFromMaster: '与主数据同步',
      selectRoles: '选择系统角色',
      username: '请输入用户名',
      password: '请输入初始密码',
      realName: '请输入姓名',
      employeeNo: '请输入工号',
      department: '请输入部门',
      team: '请输入小组',
      email: '请输入邮箱',
      phone: '请输入手机'
    },
    labels: {
      id: 'ID',
      employeeNo: '工号',
      realName: '姓名',
      username: '用户名',
      department: '部门',
      team: '小组',
      email: '邮箱',
      status: '状态',
      roles: '角色',
      actions: '操作',
      active: '正常',
      disabled: '禁用',
      password: '初始密码',
      phone: '手机'
    },
    dialogs: {
      createUser: '新建用户',
      editUser: '编辑用户'
    },
    messages: {
      requiredUsernamePassword: '用户名和密码为必填',
      created: '创建成功',
      saved: '保存成功',
      updated: '已更新'
    }
  },
  'en-US': {
    pageTitle: 'User Management',
    pageSubtitle: 'Maintain user accounts, roles, and status with clear and controllable permissions.',
    buttons: {
      newUser: 'New User',
      edit: 'Edit',
      disable: 'Disable',
      enable: 'Enable',
      cancel: 'Cancel',
      create: 'Create',
      save: 'Save'
    },
    aria: {
      searchUsers: 'Search users'
    },
    placeholders: {
      searchUsers: 'Search username/employee no/real name',
      syncFromMaster: 'Sync with master data',
      selectRoles: 'Select system roles',
      username: 'Please enter username',
      password: 'Please enter initial password',
      realName: 'Please enter real name',
      employeeNo: 'Please enter employee no',
      department: 'Please enter department',
      team: 'Please enter team',
      email: 'Please enter email',
      phone: 'Please enter phone'
    },
    labels: {
      id: 'ID',
      employeeNo: 'Employee No',
      realName: 'Real Name',
      username: 'Username',
      department: 'Department',
      team: 'Team',
      email: 'Email',
      status: 'Status',
      roles: 'Roles',
      actions: 'Actions',
      active: 'Active',
      disabled: 'Disabled',
      password: 'Initial Password',
      phone: 'Phone'
    },
    dialogs: {
      createUser: 'Create User',
      editUser: 'Edit User'
    },
    messages: {
      requiredUsernamePassword: 'Username and password are required',
      created: 'Created successfully',
      saved: 'Saved successfully',
      updated: 'Updated'
    }
  }
}

