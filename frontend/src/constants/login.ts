import type { ThemeLocale } from './theme'
import { BRAND_CONSOLE_NAME } from './brand'

export const LOGIN_PAGE_I18N: Record<
  ThemeLocale,
  {
    heroKicker: string
    heroTitleLine1: string
    heroTitleLine2: string
    heroDescription: string
    heroPoints: string[]
    pageTitle: string
    pageSubtitle: string
    usernameLabel: string
    usernamePlaceholder: string
    passwordLabel: string
    passwordPlaceholder: string
    loginButton: string
    loginTip: string
    validationUsername: string
    validationPassword: string
    liveIncomplete: string
    liveValidating: string
    successMessage: string
    liveSuccess: string
    liveFailed: string
  }
> = {
  'zh-CN': {
    heroKicker: BRAND_CONSOLE_NAME,
    heroTitleLine1: '让项目节奏更清晰，',
    heroTitleLine2: '让协作决策更可靠。',
    heroDescription: '从需求、任务、缺陷到迭代发布，统一在一个专业工作台中完成闭环管理。',
    heroPoints: ['项目状态一目了然', '需求到交付全链路追踪', '团队协同流程标准化'],
    pageTitle: '欢迎登录',
    pageSubtitle: '请输入账号信息以继续访问系统。',
    usernameLabel: '用户名',
    usernamePlaceholder: '请输入用户名…',
    passwordLabel: '密　码',
    passwordPlaceholder: '请输入密码…',
    loginButton: '登录系统',
    loginTip: '默认账号：admin / Admin@123',
    validationUsername: '请输入用户名',
    validationPassword: '请输入密码',
    liveIncomplete: '请补全登录信息。',
    liveValidating: '正在验证登录信息。',
    successMessage: '登录成功',
    liveSuccess: '登录成功，正在跳转。',
    liveFailed: '登录失败，请检查账号或密码。'
  },
  'en-US': {
    heroKicker: BRAND_CONSOLE_NAME,
    heroTitleLine1: 'Make project rhythm clearer,',
    heroTitleLine2: 'make collaboration decisions reliable.',
    heroDescription:
      'From requirements and tasks to defects and sprint releases, manage the full lifecycle in one professional console.',
    heroPoints: ['Project status at a glance', 'End-to-end traceability from requirement to delivery', 'Standardized team collaboration workflow'],
    pageTitle: 'Welcome Back',
    pageSubtitle: 'Please enter your account information to continue.',
    usernameLabel: 'Username',
    usernamePlaceholder: 'Enter username…',
    passwordLabel: 'Password',
    passwordPlaceholder: 'Enter password…',
    loginButton: 'Sign In',
    loginTip: 'Default account: admin / Admin@123',
    validationUsername: 'Please enter your username',
    validationPassword: 'Please enter your password',
    liveIncomplete: 'Please complete the login information.',
    liveValidating: 'Validating login credentials.',
    successMessage: 'Sign-in successful',
    liveSuccess: 'Sign-in successful. Redirecting now.',
    liveFailed: 'Sign-in failed. Please check your username or password.'
  }
}
