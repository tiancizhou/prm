import type { ThemeLocale } from './theme'

export const ADMIN_PEOPLE_I18N: Record<
  ThemeLocale,
  {
    title: string
    tabs: { departments: string; users: string; permissions: string }
    departmentPage: {
      title: string
      sectionTitle: string
      fallbackDepartment: string
      labels: {
        name: string
        parent: string
        sort: string
        status: string
        userCount: string
        children: string
      }
      buttons: {
        save: string
        back: string
        createChild: string
        edit: string
        delete: string
        cancel: string
      }
      statusOptions: { active: string; disabled: string }
      dialogs: { createChild: string; editDepartment: string }
      messages: {
        nameRequired: string
        created: string
        updated: string
        deleted: string
        noChildren: string
        deleteConfirm: string
        deleteTitle: string
      }
    }
    usersPage: {
      helperText: string
      filters: { all: string; internal: string; external: string; search: string }
      buttons: {
        maintainPosition: string
        addUser: string
        edit: string
        disable: string
        enable: string
        delete: string
        cancel: string
        create: string
        save: string
      }
      labels: {
        id: string
        name: string
        username: string
        systemRole: string
        joinedProjects: string
        status: string
        gender: string
        position: string
        phone: string
        qq: string
        email: string
        lastLogin: string
        visitCount: string
        actions: string
        department: string
        active: string
        disabled: string
        password: string
        employeeNo: string
      }
      dialogs: { createUser: string; editUser: string }
      messages: {
        requiredUsernamePassword: string
        created: string
        saved: string
        updated: string
        deleted: string
        deleteConfirm: string
        deleteTitle: string
      }
    }
    permissionsPage: {
      title: string
      helperText: string
      buttons: { assignByModule: string; addGroup: string }
      labels: {
        id: string
        groupName: string
        description: string
        users: string
        actions: string
      }
      tooltips: {
        view: string
        lock: string
        members: string
        edit: string
        copy: string
        delete: string
      }
    }
  }
> = {
  'zh-CN': {
    title: '\u4eba\u5458\u7ba1\u7406',
    tabs: {
      departments: '\u90e8\u95e8',
      users: '\u7528\u6237',
      permissions: '\u6743\u9650'
    },
    departmentPage: {
      title: '\u7ef4\u62a4\u90e8\u95e8',
      sectionTitle: '\u4e0b\u7ea7\u90e8\u95e8',
      fallbackDepartment: '\u7cfb\u7edf\u7814\u53d1\u90e8',
      labels: {
        name: '\u90e8\u95e8\u540d\u79f0',
        parent: '\u4e0a\u7ea7\u90e8\u95e8',
        sort: '\u6392\u5e8f',
        status: '\u72b6\u6001',
        userCount: '\u6210\u5458\u6570\u91cf',
        children: '\u4e0b\u7ea7\u90e8\u95e8'
      },
      buttons: {
        save: '\u4fdd\u5b58',
        back: '\u8fd4\u56de',
        createChild: '\u65b0\u589e\u5b50\u90e8\u95e8',
        edit: '\u7f16\u8f91',
        delete: '\u5220\u9664',
        cancel: '\u53d6\u6d88'
      },
      statusOptions: {
        active: '\u542f\u7528',
        disabled: '\u7981\u7528'
      },
      dialogs: {
        createChild: '\u65b0\u589e\u5b50\u90e8\u95e8',
        editDepartment: '\u7f16\u8f91\u90e8\u95e8'
      },
      messages: {
        nameRequired: '\u90e8\u95e8\u540d\u79f0\u4e0d\u80fd\u4e3a\u7a7a',
        created: '\u90e8\u95e8\u5df2\u521b\u5efa',
        updated: '\u90e8\u95e8\u5df2\u4fdd\u5b58',
        deleted: '\u90e8\u95e8\u5df2\u5220\u9664',
        noChildren: '\u6682\u65e0\u4e0b\u7ea7\u90e8\u95e8',
        deleteConfirm: '\u786e\u8ba4\u5220\u9664\u5f53\u524d\u90e8\u95e8\uff1f',
        deleteTitle: '\u63d0\u793a'
      }
    },
    usersPage: {
      helperText: '\u7cfb\u7edf\u89d2\u8272\u51b3\u5b9a\u529f\u80fd\u6743\u9650\uff1b\u52a0\u5165\u9879\u76ee\u540e\u624d\u53ef\u67e5\u770b\u5bf9\u5e94\u9879\u76ee\u4e1a\u52a1\u6570\u636e\u3002',
      filters: {
        all: '\u5168\u90e8',
        internal: '\u7ba1\u7406\u6743\u9650',
        external: '\u6267\u884c\u6743\u9650',
        search: '\u641c\u7d22'
      },
      buttons: {
        maintainPosition: '\u7ef4\u62a4\u804c\u4f4d',
        addUser: '\u6dfb\u52a0\u7528\u6237',
        edit: '\u7f16\u8f91',
        disable: '\u7981\u7528',
        enable: '\u542f\u7528',
        delete: '\u5220\u9664',
        cancel: '\u53d6\u6d88',
        create: '\u521b\u5efa',
        save: '\u4fdd\u5b58'
      },
      labels: {
        id: 'ID',
        name: '\u59d3\u540d',
        username: '\u7528\u6237\u540d',
        systemRole: '\u7cfb\u7edf\u89d2\u8272',
        joinedProjects: '\u5df2\u52a0\u5165\u9879\u76ee',
        status: '\u72b6\u6001',
        gender: '\u6027\u522b',
        position: '\u804c\u4f4d',
        phone: '\u7535\u8bdd',
        qq: 'QQ',
        email: '\u90ae\u7bb1',
        lastLogin: '\u6700\u540e\u767b\u5f55',
        visitCount: '\u8bbf\u95ee\u6b21\u6570',
        actions: '\u64cd\u4f5c',
        department: '\u90e8\u95e8',
        active: '\u542f\u7528',
        disabled: '\u7981\u7528',
        password: '\u5bc6\u7801',
        employeeNo: '\u5de5\u53f7'
      },
      dialogs: {
        createUser: '\u6dfb\u52a0\u7528\u6237',
        editUser: '\u7f16\u8f91\u7528\u6237'
      },
      messages: {
        requiredUsernamePassword: '\u7528\u6237\u540d\u548c\u5bc6\u7801\u5fc5\u586b',
        created: '\u521b\u5efa\u6210\u529f',
        saved: '\u4fdd\u5b58\u6210\u529f',
        updated: '\u72b6\u6001\u5df2\u66f4\u65b0',
        deleted: '\u7528\u6237\u5df2\u5220\u9664',
        deleteConfirm: '\u786e\u8ba4\u5220\u9664\u5f53\u524d\u7528\u6237\uff1f',
        deleteTitle: '\u63d0\u793a'
      }
    },
    permissionsPage: {
      title: '\u6743\u9650',
      helperText: '\u7cfb\u7edf\u89d2\u8272\u63a7\u5236\u529f\u80fd\u6743\u9650\uff1b\u662f\u5426\u52a0\u5165\u9879\u76ee\u51b3\u5b9a\u4e1a\u52a1\u6570\u636e\u53ef\u89c1\u8303\u56f4\u3002',
      buttons: {
        assignByModule: '\u6309\u6a21\u5757\u5206\u914d\u6743\u9650',
        addGroup: '\u65b0\u589e\u5206\u7ec4'
      },
      labels: {
        id: 'ID',
        groupName: '\u5206\u7ec4\u540d\u79f0',
        description: '\u5206\u7ec4\u63cf\u8ff0',
        users: '\u7528\u6237\u5217\u8868',
        actions: '\u64cd\u4f5c'
      },
      tooltips: {
        view: '\u67e5\u770b',
        lock: '\u9501\u5b9a',
        members: '\u6210\u5458',
        edit: '\u7f16\u8f91',
        copy: '\u590d\u5236',
        delete: '\u5220\u9664'
      }
    }
  },
  'en-US': {
    title: 'People Management',
    tabs: { departments: 'Departments', users: 'Users', permissions: 'Permissions' },
    departmentPage: {
      title: 'Maintain Departments',
      sectionTitle: 'Child Departments',
      fallbackDepartment: 'System R&D',
      labels: {
        name: 'Department Name',
        parent: 'Parent Department',
        sort: 'Sort',
        status: 'Status',
        userCount: 'Members',
        children: 'Child Departments'
      },
      buttons: {
        save: 'Save',
        back: 'Back',
        createChild: 'Add Child',
        edit: 'Edit',
        delete: 'Delete',
        cancel: 'Cancel'
      },
      statusOptions: { active: 'Active', disabled: 'Disabled' },
      dialogs: { createChild: 'Add Child Department', editDepartment: 'Edit Department' },
      messages: {
        nameRequired: 'Department name is required',
        created: 'Department created',
        updated: 'Department saved',
        deleted: 'Department deleted',
        noChildren: 'No child departments',
        deleteConfirm: 'Delete current department?',
        deleteTitle: 'Notice'
      }
    },
    usersPage: {
      helperText: 'System roles define permissions. Users can view project business data only after joining a project.',
      filters: { all: 'All', internal: 'Management Access', external: 'Execution Access', search: 'Search' },
      buttons: {
        maintainPosition: 'Maintain Positions',
        addUser: 'Add User',
        edit: 'Edit',
        disable: 'Disable',
        enable: 'Enable',
        delete: 'Delete',
        cancel: 'Cancel',
        create: 'Create',
        save: 'Save'
      },
      labels: {
        id: 'ID',
        name: 'Name',
        username: 'Username',
        systemRole: 'System Role',
        joinedProjects: 'Joined Projects',
        status: 'Status',
        gender: 'Gender',
        position: 'Position',
        phone: 'Phone',
        qq: 'QQ',
        email: 'Email',
        lastLogin: 'Last Login',
        visitCount: 'Visits',
        actions: 'Actions',
        department: 'Department',
        active: 'Active',
        disabled: 'Disabled',
        password: 'Password',
        employeeNo: 'Employee No'
      },
      dialogs: { createUser: 'Add User', editUser: 'Edit User' },
      messages: {
        requiredUsernamePassword: 'Username and password are required',
        created: 'Created successfully',
        saved: 'Saved successfully',
        updated: 'Status updated',
        deleted: 'User deleted',
        deleteConfirm: 'Delete this user?',
        deleteTitle: 'Notice'
      }
    },
    permissionsPage: {
      title: 'Permissions',
      helperText: 'System roles control feature permissions. Project membership determines which business data a user can see.',
      buttons: { assignByModule: 'Assign by Module', addGroup: 'Add Group' },
      labels: {
        id: 'ID',
        groupName: 'Group Name',
        description: 'Description',
        users: 'Users',
        actions: 'Actions'
      },
      tooltips: {
        view: 'View',
        lock: 'Lock',
        members: 'Members',
        edit: 'Edit',
        copy: 'Copy',
        delete: 'Delete'
      }
    }
  }
}
