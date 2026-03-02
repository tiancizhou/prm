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
    routeLabels: Record<string, string>
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
    routeLabels: {
      Dashboard: '\u5de5\u4f5c\u53f0',
      Projects: '\u9879\u76ee\u5217\u8868',
      ProjectOverview: '\u9879\u76ee\u6982\u89c8',
      Requirements: '\u9700\u6c42\u7ba1\u7406',
      Tasks: '\u4efb\u52a1\u7ba1\u7406',
      Bugs: '\u7f3a\u9677\u7ba1\u7406',
      Sprints: '\u8fed\u4ee3\u7ba1\u7406',
      ProjectMembers: '\u9879\u76ee\u6210\u5458',
      Users: '\u7528\u6237\u7ba1\u7406'
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
    routeLabels: {
      Dashboard: 'Dashboard',
      Projects: 'Projects',
      ProjectOverview: 'Project Overview',
      Requirements: 'Requirements',
      Tasks: 'Tasks',
      Bugs: 'Defects',
      Sprints: 'Sprints',
      ProjectMembers: 'Project Members',
      Users: 'Users'
    }
  }
}
