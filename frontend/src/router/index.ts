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
          name: 'Dashboard',
          component: () => import('@/views/dashboard/DashboardPage.vue')
        },
        {
          path: 'projects',
          name: 'Projects',
          component: () => import('@/views/project/ProjectListPage.vue')
        },
        {
          path: 'projects/:id/overview',
          name: 'ProjectOverview',
          component: () => import('@/views/project/ProjectOverviewPage.vue')
        },
        {
          path: 'projects/:id/requirements',
          name: 'Requirements',
          component: () => import('@/views/requirement/RequirementPage.vue')
        },
        {
          path: 'projects/:id/tasks',
          name: 'Tasks',
          component: () => import('@/views/task/TaskPage.vue')
        },
        {
          path: 'projects/:id/bugs',
          name: 'Bugs',
          component: () => import('@/views/bug/BugPage.vue')
        },
        {
          path: 'projects/:id/sprints',
          name: 'Sprints',
          component: () => import('@/views/sprint/SprintPage.vue')
        },
        {
          path: 'projects/:id/members',
          name: 'ProjectMembers',
          component: () => import('@/views/project/ProjectMembersPage.vue')
        },
        {
          path: 'system/users',
          name: 'Users',
          component: () => import('@/views/system/UserPage.vue')
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
