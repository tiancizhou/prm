import type { ThemeLocale } from './theme'

export const MAIN_LAYOUT_I18N: Record<
  ThemeLocale,
  {
    skipToMain: string
    home: string
    currentProject: string
    systemManagement: string
    userMenuAria: string
    logout: string
    sidebarExpandAria: string
    sidebarCollapseAria: string
    comingSoon: string
    globalNavLabels: { admin: string; organization: string }
    routeLabels: Record<string, string>
    projectTabLabels: { overview: string; requirements: string; modules: string; bugs: string; sprints: string; members: string }
  }
> = {
  'zh-CN': {
    skipToMain: '\u8df3\u8f6c\u5230\u4e3b\u5185\u5bb9',
    home: '\u9996\u9875',
    currentProject: '\u5f53\u524d\u9879\u76ee',
    systemManagement: '\u7cfb\u7edf\u7ba1\u7406',
    userMenuAria: '\u7528\u6237\u83dc\u5355',
    logout: '\u9000\u51fa\u767b\u5f55',
    sidebarExpandAria: '\u5c55\u5f00\u4fa7\u8fb9\u5bfc\u822a',
    sidebarCollapseAria: '\u6536\u8d77\u4fa7\u8fb9\u5bfc\u822a',
    comingSoon: '\u529f\u80fd\u5c06\u5728\u540e\u7eed\u7248\u672c\u4e2d\u63d0\u4f9b',
    globalNavLabels: { admin: '\u540e\u53f0', organization: '\u7ec4\u7ec7' },
    routeLabels: {
      Dashboard: '\u5de5\u4f5c\u53f0',
      Projects: '\u9879\u76ee\u5217\u8868',
      ProjectOverview: '\u9879\u76ee\u6982\u89c8',
      Requirements: '\u9700\u6c42\u7ba1\u7406',
      Modules: '\u6a21\u5757\u7ef4\u62a4',
      Tasks: '\u4efb\u52a1\u7ba1\u7406',
      Bugs: 'Bug管理',
      Sprints: '\u8fed\u4ee3\u7ba1\u7406',
      ProjectMembers: '\u9879\u76ee\u6210\u5458',
      Users: '\u7528\u6237\u7ba1\u7406',
      Admin: '\u540e\u53f0',
      AdminDepartments: '\u90e8\u95e8',
      AdminUsers: '\u7528\u6237',
      AdminPermissions: '\u6743\u9650',
      Organization: '\u7ec4\u7ec7',
      OrganizationTeam: '\u56e2\u961f',
      OrganizationActivity: '\u6d3b\u52a8',
      OrganizationCompany: '\u516c\u53f8'
    },
    projectTabLabels: {
      overview: '\u6982\u89c8',
      requirements: '\u9700\u6c42',
      modules: '\u6a21\u5757',
      bugs: 'Bug',
      sprints: '\u8fed\u4ee3',
      members: '\u6210\u5458'
    }
  },
  'en-US': {
    skipToMain: 'Skip to main content',
    home: 'Home',
    currentProject: 'Current Project',
    systemManagement: 'System Management',
    userMenuAria: 'User menu',
    logout: 'Sign out',
    sidebarExpandAria: 'Expand sidebar navigation',
    sidebarCollapseAria: 'Collapse sidebar navigation',
    comingSoon: 'Coming in a future release',
    globalNavLabels: { admin: 'Admin', organization: 'Organization' },
    routeLabels: {
      Dashboard: 'Dashboard',
      Projects: 'Projects',
      ProjectOverview: 'Project Overview',
      Requirements: 'Requirements',
      Modules: 'Module Management',
      Tasks: 'Tasks',
      Bugs: 'Defects',
      Sprints: 'Sprints',
      ProjectMembers: 'Project Members',
      Users: 'Users',
      Admin: 'Admin',
      AdminDepartments: 'Departments',
      AdminUsers: 'Users',
      AdminPermissions: 'Permissions',
      Organization: 'Organization',
      OrganizationTeam: 'Team',
      OrganizationActivity: 'Activity',
      OrganizationCompany: 'Company'
    },
    projectTabLabels: {
      overview: 'Overview',
      requirements: 'Requirements',
      modules: 'Modules',
      bugs: 'Bugs',
      sprints: 'Sprints',
      members: 'Members'
    }
  }
}
