import type { ThemeLocale } from './theme'

export type AdminSectionKey = 'system' | 'activity' | 'company'

interface AdminSummaryCard {
  title: string
  description: string
}

interface AdminSectionContent {
  title: string
  description: string
  summaryCards: AdminSummaryCard[]
  planningTitle: string
  planningPoints: string[]
}

export const ADMIN_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    buttons: {
      backToSystem: string
    }
    status: {
      available: string
      comingSoon: string
    }
    sections: Record<AdminSectionKey, AdminSectionContent>
  }
> = {
  'zh-CN': {
    pageTitle: '后台',
    pageSubtitle: '集中承载系统级配置、动态信息与公司基础资料。',
    buttons: {
      backToSystem: '返回系统'
    },
    status: {
      available: '已启用',
      comingSoon: '即将开放'
    },
    sections: {
      system: {
        title: '系统',
        description: '汇总系统级设置入口与基础配置。',
        summaryCards: [
          {
            title: '系统入口',
            description: '承接全局配置、管理能力与系统级维护入口。'
          },
          {
            title: '配置边界',
            description: '未来将在这里归档不属于项目域和组织域的后台配置。'
          }
        ],
        planningTitle: '模块规划',
        planningPoints: [
          '后续用于承载系统参数、全局选项与后台配置入口。',
          '与项目域、组织域区分，专注系统级维护能力。',
          '为动态中心、公司资料等后台模块提供统一入口。'
        ]
      },
      activity: {
        title: '动态',
        description: '查看系统范围内的运营动态、提醒与全局消息。',
        summaryCards: [
          {
            title: '消息汇总',
            description: '后续用于聚合系统级消息、提醒与重要动态。'
          },
          {
            title: '操作追踪',
            description: '支持查看关键后台操作与全局变更记录。'
          }
        ],
        planningTitle: '模块规划',
        planningPoints: [
          '后续用于展示系统级动态、提醒与全局消息。',
          '支持全局操作记录与关键配置变更追踪。',
          '为后台管理提供统一的信息感知入口。'
        ]
      },
      company: {
        title: '公司',
        description: '维护公司层面的品牌资料、基础信息与对外展示字段。',
        summaryCards: [
          {
            title: '品牌资料',
            description: '后续用于维护名称、标识、品牌描述等基础信息。'
          },
          {
            title: '公司信息',
            description: '支持管理组织级公共资料与对外展示字段。'
          }
        ],
        planningTitle: '模块规划',
        planningPoints: [
          '后续用于维护品牌信息、公司资料与公共展示字段。',
          '与组织域区分，专注公司级静态配置和基础信息。',
          '为后台系统提供统一的公司资料入口。'
        ]
      }
    }
  },
  'en-US': {
    pageTitle: 'Admin',
    pageSubtitle: 'Collect system configuration, activity information, and company-level metadata in one domain.',
    buttons: {
      backToSystem: 'Back to System'
    },
    status: {
      available: 'Available',
      comingSoon: 'Coming Soon'
    },
    sections: {
      system: {
        title: 'System',
        description: 'Collect system-level settings and foundational configuration entry points.',
        summaryCards: [
          {
            title: 'System Entry',
            description: 'Acts as the home for global configuration and administrative capabilities.'
          },
          {
            title: 'Configuration Scope',
            description: 'Will later contain settings that do not belong to project or organization domains.'
          }
        ],
        planningTitle: 'Module Plan',
        planningPoints: [
          'Host future system parameters, global options, and backend settings.',
          'Stay separate from project and organization concerns.',
          'Provide a stable starting point for other admin modules.'
        ]
      },
      activity: {
        title: 'Activity',
        description: 'Review platform-wide activity, reminders, and operational signals.',
        summaryCards: [
          {
            title: 'Message Hub',
            description: 'Will aggregate system-wide reminders, notices, and important activity.'
          },
          {
            title: 'Operational Trace',
            description: 'Will highlight meaningful admin actions and global changes.'
          }
        ],
        planningTitle: 'Module Plan',
        planningPoints: [
          'Show system activity, reminders, and platform-wide notices later.',
          'Support global operation history and key change tracking.',
          'Give admins a single place to sense system movement.'
        ]
      },
      company: {
        title: 'Company',
        description: 'Maintain company-level brand, profile, and public-facing metadata.',
        summaryCards: [
          {
            title: 'Brand Assets',
            description: 'Will store names, marks, and brand descriptions in one place.'
          },
          {
            title: 'Company Metadata',
            description: 'Will manage shared profile fields and organization-wide identity data.'
          }
        ],
        planningTitle: 'Module Plan',
        planningPoints: [
          'Maintain brand information and shared company metadata later.',
          'Stay focused on static company-level configuration.',
          'Provide a dedicated home for company profile management.'
        ]
      }
    }
  }
}
