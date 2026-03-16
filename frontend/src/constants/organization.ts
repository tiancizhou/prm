import type { ThemeLocale } from './theme'

export type OrganizationSectionKey = 'team' | 'activity' | 'company'

interface OrganizationSummaryCard {
  title: string
  description: string
}

interface OrganizationSectionContent {
  title: string
  description: string
  summaryCards: OrganizationSummaryCard[]
  placeholderTitle: string
  placeholderDescription: string
  planningTitle: string
  planningPoints: string[]
}

export const ORGANIZATION_I18N: Record<
  ThemeLocale,
  {
    pageTitle: string
    pageSubtitle: string
    peoplePage: {
      title: string
      subtitle: string
      addUser: string
    }
    buttons: {
      backToTeam: string
      addUser: string
    }
    status: {
      active: string
      comingSoon: string
    }
    sections: Record<OrganizationSectionKey, OrganizationSectionContent>
  }
> = {
  'zh-CN': {
    pageTitle: '组织',
    pageSubtitle: '按团队视角查看组织成员与组织信息。',
    peoplePage: {
      title: '团队',
      subtitle: '当前仅展示一个团队，团队下直接显示人员列表。',
      addUser: '添加用户'
    },
    buttons: {
      backToTeam: '返回团队',
      addUser: '添加用户'
    },
    status: {
      active: '已启用',
      comingSoon: '即将开放'
    },
    sections: {
      team: {
        title: '团队',
        description: '当前团队直接展示人员列表，作为组织域的主页面。',
        summaryCards: [
          {
            title: '单团队视图',
            description: '第一版先保留一个团队入口，直接承载人员管理列表。'
          },
          {
            title: '组织成员',
            description: '统一查看成员基础信息、联系方式与所属部门。'
          }
        ],
        placeholderTitle: '团队',
        placeholderDescription: '当前团队页面直接展示成员列表。',
        planningTitle: '页面说明',
        planningPoints: [
          '以团队为主视角进入组织域。',
          '当前只保留一个团队，不做团队切换器。',
          '团队页面主体直接显示人员列表。'
        ]
      },
      activity: {
        title: '动态',
        description: '后续用于承载组织范围内的动态与消息。',
        summaryCards: [
          {
            title: '组织动态',
            description: '未来用于汇总组织层面的动态、提醒与变更信息。'
          },
          {
            title: '信息同步',
            description: '为组织成员提供统一的消息和协作提示入口。'
          }
        ],
        placeholderTitle: '动态',
        placeholderDescription: '组织动态模块即将开放。',
        planningTitle: '模块规划',
        planningPoints: [
          '后续在这里查看组织级动态、通知与提醒。',
          '支持聚合成员变更与组织消息。',
          '作为团队页面之外的信息入口。'
        ]
      },
      company: {
        title: '公司',
        description: '后续用于维护公司层面的基础资料和展示信息。',
        summaryCards: [
          {
            title: '公司资料',
            description: '未来用于展示和维护公司基础信息。'
          },
          {
            title: '品牌信息',
            description: '后续用于维护品牌、介绍与组织级展示字段。'
          }
        ],
        placeholderTitle: '公司',
        placeholderDescription: '公司模块即将开放。',
        planningTitle: '模块规划',
        planningPoints: [
          '后续在这里维护公司级信息与品牌资料。',
          '用于承载组织域之外的公司展示内容。',
          '为后台和组织页面提供统一的公司资料来源。'
        ]
      }
    }
  },
  'en-US': {
    pageTitle: 'Organization',
    pageSubtitle: 'View members and organization content from a team-first perspective.',
    peoplePage: {
      title: 'Team',
      subtitle: 'The first version keeps a single team and shows the personnel list directly.',
      addUser: 'Add User'
    },
    buttons: {
      backToTeam: 'Back to Team',
      addUser: 'Add User'
    },
    status: {
      active: 'Active',
      comingSoon: 'Coming Soon'
    },
    sections: {
      team: {
        title: 'Team',
        description: 'The team page directly displays the personnel list and acts as the primary organization view.',
        summaryCards: [
          {
            title: 'Single-Team View',
            description: 'The first version keeps one team entry and uses it as the people list container.'
          },
          {
            title: 'Organization Members',
            description: 'Review member basics, contact info, and department ownership in one list.'
          }
        ],
        placeholderTitle: 'Team',
        placeholderDescription: 'The team page directly shows the member list.',
        planningTitle: 'Page Notes',
        planningPoints: [
          'Use Team as the default organization landing page.',
          'Keep a single team for the first version.',
          'Render the people table directly in the team view.'
        ]
      },
      activity: {
        title: 'Activity',
        description: 'Future entry for organization-wide activity and notifications.',
        summaryCards: [
          {
            title: 'Organization Feed',
            description: 'Will aggregate organization-level updates and notifications later.'
          },
          {
            title: 'Message Sync',
            description: 'Will provide a unified place for organization-wide notices.'
          }
        ],
        placeholderTitle: 'Activity',
        placeholderDescription: 'The organization activity module is coming soon.',
        planningTitle: 'Module Plan',
        planningPoints: [
          'Show organization-level activity and reminders later.',
          'Support member change tracking and notifications.',
          'Complement the team page with a broader information view.'
        ]
      },
      company: {
        title: 'Company',
        description: 'Future entry for company-level profile and display information.',
        summaryCards: [
          {
            title: 'Company Profile',
            description: 'Will host shared company information and profile content later.'
          },
          {
            title: 'Brand Info',
            description: 'Will maintain branding and presentation fields for the organization.'
          }
        ],
        placeholderTitle: 'Company',
        placeholderDescription: 'The company module is coming soon.',
        planningTitle: 'Module Plan',
        planningPoints: [
          'Maintain company profile and branding information later.',
          'Provide shared company metadata for the organization domain.',
          'Act as the company-facing section inside Organization.'
        ]
      }
    }
  }
}
