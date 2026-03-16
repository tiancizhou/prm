<template>
  <div class="dashboard-tab-shell">
    <header class="dashboard-tab-bar">
      <nav class="dashboard-tabs" aria-label="首页导航">
        <button
          v-for="tab in tabs"
          :key="tab.key"
          type="button"
          class="dashboard-tab"
          :class="{ 'is-active': activeTabKey === tab.key }"
          @click="openTab(tab.path)"
        >
          {{ tab.label }}
        </button>
      </nav>
    </header>
    <section class="dashboard-tab-body">
      <router-view />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { DASHBOARD_I18N } from '@/constants/dashboard'
import { resolveThemeLocale } from '@/constants/theme'

interface TabItem { key: string; label: string; path: string }

const route = useRoute()
const router = useRouter()
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const t = DASHBOARD_I18N[currentLocale]?.dashboardTabs ?? DASHBOARD_I18N['zh-CN'].dashboardTabs

const tabs: TabItem[] = [
  { key: 'overview', label: t.overview, path: '/dashboard' },
  { key: 'todo', label: t.todo, path: '/dashboard/todo' }
]

const activeTabKey = computed(() => {
  const path = route.path
  if (path.includes('/dashboard/todo')) return 'todo'
  return 'overview'
})

function openTab(path: string) {
  void router.push(path)
}
</script>

<style scoped>
.dashboard-tab-shell {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--app-page-bg);
}
.dashboard-tab-bar {
  flex-shrink: 0;
  padding: 0 var(--space-lg);
  background: var(--app-layout-header-bg);
  border-bottom: 1px solid var(--app-border-soft);
}
.dashboard-tabs {
  display: flex;
  align-items: stretch;
  gap: 0;
}
.dashboard-tab {
  min-width: 80px;
  height: 48px;
  border: 0;
  padding: 0 var(--space-lg);
  background: transparent;
  color: var(--app-text-secondary);
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}
.dashboard-tab:hover {
  color: var(--app-text-primary);
}
.dashboard-tab.is-active {
  color: var(--app-color-primary);
  font-weight: 600;
  box-shadow: inset 0 -2px 0 var(--app-color-primary);
}
.dashboard-tab-body {
  flex: 1;
  padding: var(--space-lg);
  min-width: 0;
  overflow: auto;
}
</style>
