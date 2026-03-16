<template>
  <div class="domain-shell">
    <header class="domain-topbar">
      <div class="domain-topbar-main">
        <div class="domain-topbar-title">
          <el-icon><component :is="domainIcon" /></el-icon>
          <span>{{ domainTitle }}</span>
        </div>

        <nav class="domain-topbar-tabs" :aria-label="domainTitle">
          <button
            v-for="tab in tabs"
            :key="tab.name"
            type="button"
            class="domain-tab"
            :class="{ 'is-active': activeTabKey === tab.key }"
            @click="openTab(tab.path)"
          >
            {{ tab.label }}
          </button>
        </nav>
      </div>
    </header>

    <section class="domain-shell-body">
      <router-view />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { OfficeBuilding, Setting } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { MAIN_LAYOUT_I18N } from '@/constants/layout'
import { ADMIN_PEOPLE_I18N } from '@/constants/adminPeople'
import { resolveThemeLocale } from '@/constants/theme'
import { GLOBAL_NAV_PERMISSION_MAP } from '@/utils/permission'

type DomainShellKey = 'organization' | 'admin'
interface DomainTab { key: string; name: string; label: string; path: string }

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const layoutText = MAIN_LAYOUT_I18N[currentLocale]
const adminPeopleText = ADMIN_PEOPLE_I18N[currentLocale]

const domainKey = computed<DomainShellKey>(() => (route.meta.domainShell === 'admin' ? 'admin' : 'organization'))
const activeTabKey = computed(() => String(route.meta.domainTab ?? route.name ?? ''))
const domainTitle = computed(() => {
  if (domainKey.value === 'admin') {
    return layoutText.globalNavLabels.admin
  }
  return layoutText.globalNavLabels.organization
})
const domainIcon = computed(() => (domainKey.value === 'admin' ? Setting : OfficeBuilding))

const tabs = computed<DomainTab[]>(() => {
  if (domainKey.value === 'admin') {
    const adminTabs = [
      { key: 'departments', name: 'AdminDepartments', label: adminPeopleText.tabs.departments, path: '/admin/departments' },
      { key: 'users', name: 'AdminUsers', label: adminPeopleText.tabs.users, path: '/admin/users' },
      { key: 'permissions', name: 'AdminPermissions', label: adminPeopleText.tabs.permissions, path: '/admin/permissions' }
    ]
    return authStore.canAccess(GLOBAL_NAV_PERMISSION_MAP.admin) ? adminTabs : []
  }
  const organizationTabs = [
    { key: 'team', name: 'OrganizationTeam', label: layoutText.routeLabels.OrganizationTeam ?? 'Team', path: '/organization/team' },
    { key: 'company', name: 'OrganizationCompany', label: layoutText.routeLabels.OrganizationCompany ?? 'Company', path: '/organization/company' }
  ]
  return authStore.canAccess(GLOBAL_NAV_PERMISSION_MAP.organization) ? organizationTabs : []
})

function openTab(path: string) { void router.push(path) }
</script>

<style scoped>
.domain-shell { min-height: 100%; display: flex; flex-direction: column; background: var(--app-page-bg); }
.domain-topbar {
  min-height: 56px;
  padding: 0 var(--space-xl);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
  background: var(--app-bg-surface);
  color: var(--app-text-primary);
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius-md);
  box-shadow: var(--app-shadow-soft);
}
.domain-topbar-main { display: flex; align-items: center; gap: var(--space-lg); min-width: 0; }
.domain-topbar-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 14px;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;
  color: var(--app-text-primary);
}
.domain-topbar-title .el-icon { color: var(--app-text-secondary); font-size: 16px; }
.domain-topbar-tabs { display: flex; align-items: stretch; min-width: 0; gap: var(--space-xs); }
.domain-tab {
  min-width: 80px;
  height: 56px;
  border: 0;
  padding: 0 var(--space-md);
  background: transparent;
  color: var(--app-text-secondary);
  cursor: pointer;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease;
}
.domain-tab:hover {
  color: var(--app-text-primary);
  background: var(--app-bg-muted);
}
.domain-tab.is-active {
  color: var(--app-color-primary);
  font-weight: 600;
  box-shadow: inset 0 -2px 0 var(--app-color-primary);
}
.domain-shell-body { flex: 1; padding: var(--space-xl) 0 0; }
@media (max-width: 1439px) {
  .domain-topbar {
    flex-wrap: wrap;
    padding-top: var(--space-sm);
    padding-bottom: var(--space-sm);
    padding-left: var(--space-lg);
    padding-right: var(--space-lg);
  }
  .domain-topbar-main { width: 100%; flex-wrap: wrap; gap: var(--space-md); }
  .domain-topbar-tabs { width: 100%; overflow-x: auto; }
  .domain-shell-body { padding-top: var(--space-lg); }
}
</style>
