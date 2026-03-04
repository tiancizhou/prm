<template>
  <a class="skip-link" href="#main-content">{{ layoutText.skipToMain }}</a>

  <el-container class="layout-shell">
    <el-aside :width="collapsed ? '84px' : '248px'" class="layout-sidebar">
      <div class="brand">
        <span class="brand-mark">{{ BRAND_SHORT_NAME }}</span>
        <div v-if="!collapsed" class="brand-text">
          <strong>{{ BRAND_FULL_NAME }}</strong>
          <small>{{ BRAND_SUBTITLE }}</small>
        </div>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        class="sidebar-menu"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>{{ layoutText.routeLabels.Dashboard }}</template>
        </el-menu-item>

        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <template #title>{{ layoutText.routeLabels.Projects }}</template>
        </el-menu-item>

        <template v-if="currentProject">
          <div v-if="!collapsed" class="menu-section">{{ layoutText.currentProject }}</div>
          <el-menu-item :index="`/projects/${currentProject.id}/overview`">
            <el-icon><House /></el-icon>
            <template #title>{{ layoutText.routeLabels.ProjectOverview }}</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/requirements`">
            <el-icon><Document /></el-icon>
            <template #title>{{ layoutText.routeLabels.Requirements }}</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/tasks`">
            <el-icon><List /></el-icon>
            <template #title>{{ layoutText.routeLabels.Tasks }}</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/bugs`">
            <el-icon><Warning /></el-icon>
            <template #title>{{ layoutText.routeLabels.Bugs }}</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/sprints`">
            <el-icon><Calendar /></el-icon>
            <template #title>{{ layoutText.routeLabels.Sprints }}</template>
          </el-menu-item>
          <el-menu-item v-if="canManageCurrentProjectMembers" :index="`/projects/${currentProject.id}/members`">
            <el-icon><User /></el-icon>
            <template #title>{{ layoutText.routeLabels.ProjectMembers }}</template>
          </el-menu-item>
        </template>

        <template v-if="isSuperAdmin">
          <div v-if="!collapsed" class="menu-section">{{ layoutText.systemManagement }}</div>
          <el-menu-item index="/system/users">
            <el-icon><Setting /></el-icon>
            <template #title>{{ layoutText.routeLabels.Users }}</template>
          </el-menu-item>
        </template>
      </el-menu>

      <button
        class="collapse-btn"
        type="button"
        :aria-label="collapsed ? layoutText.sidebarExpandAria : layoutText.sidebarCollapseAria"
        @click="toggleSidebar"
      >
        <el-icon>
          <ArrowRight v-if="collapsed" />
          <ArrowLeft v-else />
        </el-icon>
      </button>
    </el-aside>

    <el-container class="layout-main">
      <el-header class="layout-header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">{{ layoutText.home }}</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteLabel">{{ currentRouteLabel }}</el-breadcrumb-item>
          </el-breadcrumb>

          <div v-if="currentProject" class="project-pill">
            <span class="project-pill-label">{{ layoutText.currentProject }}</span>
            <strong class="project-pill-name">{{ currentProject.name }}</strong>
          </div>
        </div>

        <div class="header-right">
          <el-dropdown @command="handleThemeCommand">
            <button class="theme-trigger" type="button" :aria-label="themeText.switchAria">
              <el-icon><component :is="themeIconName" /></el-icon>
              <span class="theme-label">{{ themeLabel }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item
                  v-for="option in themeOptions"
                  :key="option.mode"
                  :disabled="themeMode === option.mode"
                  :command="option.mode"
                >
                  {{ option.label }}
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-dropdown @command="handleCommand">
            <button class="user-trigger" type="button" :aria-label="layoutText.userMenuAria">
              <el-avatar :size="34" icon="UserFilled" />
              <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">{{ layoutText.logout }}</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main id="main-content" class="layout-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'
import {
  THEME_I18N,
  THEME_MODES,
  THEME_STORAGE_KEY,
  resolveThemeLocale,
  type ThemeMode
} from '@/constants/theme'
import { MAIN_LAYOUT_I18N } from '@/constants/layout'
import { BRAND_FULL_NAME, BRAND_SHORT_NAME, BRAND_SUBTITLE } from '@/constants/brand'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const projectStore = useProjectStore()

const themeMessageDuration = 1200
const systemThemeMedia = window.matchMedia('(prefers-color-scheme: dark)')
const compactLayoutMedia = window.matchMedia('(max-width: 1365px)')

const collapsed = ref(compactLayoutMedia.matches)
const activeMenu = computed(() => route.path)
const currentProject = computed(() => projectStore.currentProject)
const canManageCurrentProjectMembers = computed(() => currentProject.value?.canEdit === true)
const isSuperAdmin = computed(() => authStore.user?.roles?.includes('SUPER_ADMIN'))
const themeMode = ref<ThemeMode>(resolveThemeMode())
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const themeText = THEME_I18N[currentLocale]
const layoutText = MAIN_LAYOUT_I18N[currentLocale]
const themeOptions = computed(() => {
  return THEME_MODES.map((mode) => ({ mode, label: themeText.modeLabels[mode] }))
})

const themeLabel = computed(() => {
  return themeText.triggerLabels[themeMode.value]
})

const themeIconName = computed(() => {
  if (themeMode.value === 'light') {
    return 'Sunny'
  }
  if (themeMode.value === 'dark') {
    return 'Moon'
  }
  return 'Monitor'
})

const routeNameLabelMap: Record<string, string> = layoutText.routeLabels

const currentRouteLabel = computed(() => routeNameLabelMap[String(route.name ?? '')] ?? '')

function handleCompactLayoutChange(event: MediaQueryListEvent) {
  collapsed.value = event.matches
}

onMounted(() => {
  collapsed.value = compactLayoutMedia.matches
  compactLayoutMedia.addEventListener('change', handleCompactLayoutChange)
})

onBeforeUnmount(() => {
  compactLayoutMedia.removeEventListener('change', handleCompactLayoutChange)
})

function toggleSidebar() {
  collapsed.value = !collapsed.value
}

function resolveThemeMode(): ThemeMode {
  const saved = localStorage.getItem(THEME_STORAGE_KEY)
  if (saved === 'light' || saved === 'dark' || saved === 'system') {
    return saved
  }
  return 'system'
}

function applyThemeMode(mode: ThemeMode) {
  const theme = mode === 'system' ? (systemThemeMedia.matches ? 'dark' : 'light') : mode
  document.documentElement.setAttribute('data-theme', theme)
}

function handleThemeCommand(command: string) {
  if (command !== 'light' && command !== 'dark' && command !== 'system') {
    return
  }

  themeMode.value = command
  localStorage.setItem(THEME_STORAGE_KEY, command)
  applyThemeMode(command)

  ElMessage.info({
    message: `${themeText.switchedPrefix}${themeText.modeLabels[command]}`,
    duration: themeMessageDuration
  })
}

async function handleCommand(command: string) {
  if (command !== 'logout') {
    return
  }
  await authStore.logout()
  await router.push('/login')
}
</script>

<style scoped>
.layout-shell {
  height: 100vh;
  background: var(--app-bg-page);
}

.layout-sidebar {
  border-right: 1px solid var(--app-border-soft);
  background: var(--app-layout-sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width 0.24s ease;
  overflow: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  min-height: 84px;
  padding: 0 var(--space-lg);
  border-bottom: 1px solid var(--app-border-soft);
}

.brand-mark {
  width: 44px;
  height: 44px;
  border-radius: var(--app-radius-sm);
  display: grid;
  place-items: center;
  font-family: var(--app-font-heading);
  font-size: 17px;
  color: var(--app-text-on-primary);
  letter-spacing: 0.08em;
  background: var(--app-brand-mark-bg);
  box-shadow: var(--app-brand-mark-shadow);
}

.brand-text {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.brand-text strong {
  font-size: 13px;
  color: var(--app-text-primary);
  letter-spacing: 0.02em;
}

.brand-text small {
  font-size: 12px;
  color: var(--app-text-muted);
}

.menu-section {
  margin: var(--space-lg) var(--space-lg) var(--space-xs);
  font-size: 12px;
  color: var(--app-text-muted);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  background: transparent;
  padding: var(--space-sm);
}

.sidebar-menu :deep(.el-menu-item) {
  height: 42px;
  line-height: 42px;
  border-radius: var(--app-radius-sm);
  margin-bottom: var(--space-xs);
  color: var(--app-text-secondary);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background: var(--app-color-primary-soft);
  color: var(--app-color-primary);
  font-weight: 600;
}

.collapse-btn {
  height: 50px;
  border: 0;
  border-top: 1px solid var(--app-border-soft);
  background: transparent;
  cursor: pointer;
  color: var(--app-text-secondary);
}

.collapse-btn:hover {
  color: var(--app-color-primary);
  background: var(--app-primary-ghost-bg);
}

.layout-main {
  min-width: 0;
}

.layout-header {
  height: 72px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-md);
  padding: 0 var(--space-xl);
  border-bottom: 1px solid var(--app-border-soft);
  background: var(--app-layout-header-bg);
  backdrop-filter: blur(8px);
}

.header-left {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  min-width: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
  justify-content: flex-end;
}

.project-pill {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--app-radius-pill);
  border: 1px solid var(--app-border-soft);
  background: var(--app-bg-elevated);
}

.project-pill-label {
  font-size: 12px;
  color: var(--app-text-muted);
}

.project-pill-name {
  max-width: 260px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--app-text-primary);
  font-size: 13px;
}

.user-trigger {
  border: 1px solid var(--app-border-soft);
  background: var(--app-bg-elevated);
  border-radius: var(--app-radius-pill);
  height: 44px;
  padding: 0 var(--space-md) 0 var(--space-sm);
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  color: var(--app-text-primary);
}

.theme-trigger {
  border: 1px solid var(--app-border-soft);
  background: var(--app-bg-elevated);
  border-radius: var(--app-radius-pill);
  height: 44px;
  padding: 0 var(--space-md);
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  color: var(--app-text-primary);
}

.theme-trigger:hover {
  border-color: var(--app-control-hover-border);
  box-shadow: var(--app-control-hover-shadow);
}

.theme-label {
  font-size: 13px;
}

.user-trigger:hover {
  border-color: var(--app-control-hover-border);
  box-shadow: var(--app-control-hover-shadow);
}

.username {
  font-size: 13px;
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.layout-content {
  padding: var(--space-xl);
  overflow-y: auto;
}

@media (max-width: 1919px) {
  .layout-header {
    padding: 0 var(--space-lg);
  }

  .layout-content {
    padding: var(--space-lg);
  }

  .project-pill-name {
    max-width: 220px;
  }
}

@media (max-width: 1439px) {
  .layout-header {
    height: auto;
    min-height: 68px;
    padding: 0 var(--space-md);
    padding-top: var(--space-sm);
    padding-bottom: var(--space-sm);
    align-items: flex-start;
    flex-wrap: wrap;
    row-gap: var(--space-sm);
  }

  .header-left {
    gap: var(--space-md);
    width: 100%;
  }

  .header-right {
    width: 100%;
    justify-content: flex-end;
  }

  .project-pill {
    padding: var(--space-xs) var(--space-sm);
  }

  .layout-content {
    padding: var(--space-md);
  }

  .username {
    max-width: 110px;
  }
}

@media (max-width: 1365px) {
  .header-left,
  .header-right {
    width: 100%;
  }

  .header-right {
    justify-content: flex-end;
  }

  .project-pill {
    display: none;
  }

  .theme-label {
    display: none;
  }

  .theme-trigger {
    padding: 0 var(--space-sm);
  }

  .user-trigger {
    padding: 0 var(--space-sm);
  }

  .username {
    display: none;
  }
}
</style>
