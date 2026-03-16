import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginPage.vue'),
      meta: { requiresAuth: false }
    },
    {
      path: '/',
      component: () => import('@/layouts/MainLayout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: 'dashboard',
          component: () => import('@/layouts/DashboardTabLayout.vue'),
          children: [
            { path: '', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardPage.vue') },
            { path: 'overview', redirect: () => ({ path: '/dashboard' }) },
            { path: 'schedule', redirect: () => ({ path: '/dashboard' }) },
            { path: 'todo', name: 'DashboardTodo', component: () => import('@/views/dashboard/DashboardTodoPage.vue') }
          ]
        },
        {
          path: 'projects',
          name: 'Projects',
          component: () => import('@/views/project/ProjectListPage.vue')
        },
        {
          path: 'projects/:id',
          component: () => import('@/layouts/ProjectTabLayout.vue'),
          meta: { projectShell: true },
          children: [
            { path: '', redirect: (to) => ({ path: `/projects/${to.params.id}/overview` }) },
            { path: 'overview', name: 'ProjectOverview', component: () => import('@/views/project/ProjectOverviewPage.vue') },
            { path: 'requirements', name: 'Requirements', component: () => import('@/views/requirement/RequirementPage.vue') },
            { path: 'requirements/create', name: 'RequirementCreate', component: () => import('@/views/requirement/RequirementCreatePage.vue') },
            { path: 'requirements/batch-edit', name: 'RequirementBatchEdit', component: () => import('@/views/requirement/RequirementBatchEditPage.vue') },
            { path: 'requirements/:reqId/edit', name: 'RequirementEdit', component: () => import('@/views/requirement/RequirementEditPage.vue') },
            { path: 'requirements/:reqId', name: 'RequirementDetail', component: () => import('@/views/requirement/RequirementDetailPage.vue') },
            { path: 'tasks', redirect: (to) => ({ path: `/projects/${to.params.id}/requirements` }) },
            { path: 'modules', name: 'Modules', component: () => import('@/views/module/ModulePage.vue') },
            { path: 'bugs', name: 'Bugs', component: () => import('@/views/bug/BugPage.vue') },
            { path: 'bugs/create', name: 'BugCreate', component: () => import('@/views/bug/BugCreatePage.vue') },
            { path: 'bugs/:bugId', name: 'BugDetail', component: () => import('@/views/bug/BugDetailPage.vue') },
            { path: 'sprints', name: 'Sprints', component: () => import('@/views/sprint/SprintPage.vue') },
            { path: 'members', name: 'ProjectMembers', component: () => import('@/views/project/ProjectMembersPage.vue') }
          ]
        },
        {
          path: 'system/users',
          redirect: '/admin/users'
        },
        {
          path: 'admin',
          component: () => import('@/layouts/DomainTopNavLayout.vue'),
          meta: { requiresAuth: true, domainShell: 'admin' },
          children: [
            { path: '', redirect: 'departments' },
            { path: 'departments', name: 'AdminDepartments', component: () => import('@/views/admin/AdminDepartmentPage.vue'), meta: { domainTab: 'departments' } },
            { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/AdminUsersPage.vue'), meta: { domainTab: 'users' } },
            { path: 'permissions', name: 'AdminPermissions', component: () => import('@/views/admin/AdminPermissionsPage.vue'), meta: { domainTab: 'permissions' } }
          ]
        },
        {
          path: 'organization',
          component: () => import('@/layouts/DomainTopNavLayout.vue'),
          meta: { requiresAuth: true, domainShell: 'organization' },
          children: [
            { path: '', redirect: 'team' },
            { path: 'team', name: 'OrganizationTeam', component: () => import('@/views/organization/OrganizationTeamPage.vue'), meta: { domainTab: 'team' } },
            { path: 'team/:userId', name: 'OrganizationUserBusiness', component: () => import('@/views/organization/OrganizationUserBusinessPage.vue'), meta: { domainTab: 'team' } },
            { path: 'activity', redirect: 'team' },
            { path: 'company', name: 'OrganizationCompany', component: () => import('@/views/organization/OrganizationCompanyPage.vue'), meta: { domainTab: 'company' } }
          ]
        }
      ]
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard'
    }
  ]
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth === false) return true
  if (!authStore.isLoggedIn) return '/login'
  if (!authStore.user) await authStore.fetchCurrentUser()
  return true
})

export default router
