<template>
  <div class="project-shell">
    <header class="project-topbar">
      <div class="project-topbar-main">
        <div class="project-topbar-title">
          <span class="project-name">{{ projectName }}</span>
        </div>
        <nav class="project-topbar-tabs" aria-label="项目导航">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            type="button"
            class="project-tab"
            :class="{ 'is-active': activeTabKey === tab.key }"
            @click="openTab(tab.path)"
          >
            {{ tab.label }}
          </button>
        </nav>
      </div>
    </header>
    <section class="project-shell-body">
      <router-view />
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useProjectStore } from '@/stores/project'
import { projectApi } from '@/api/project'
import { MAIN_LAYOUT_I18N } from '@/constants/layout'
import { resolveThemeLocale } from '@/constants/theme'

interface ProjectTab {
  key: string
  label: string
  path: string
}

const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const layoutText = MAIN_LAYOUT_I18N[currentLocale]
const tabLabels = layoutText.projectTabLabels

const projectId = computed(() => Number(route.params.id) || 0)
const projectName = ref<string>('')

const tabs = computed<ProjectTab[]>(() => {
  const id = projectId.value
  if (!id) return []
  const base = `/projects/${id}`
  const list: ProjectTab[] = [
    { key: 'overview', label: tabLabels.overview, path: `${base}/overview` },
    { key: 'requirements', label: tabLabels.requirements, path: `${base}/requirements` },
    { key: 'modules', label: tabLabels.modules, path: `${base}/modules` },
    { key: 'bugs', label: tabLabels.bugs, path: `${base}/bugs` },
    { key: 'sprints', label: tabLabels.sprints, path: `${base}/sprints` },
    { key: 'members', label: tabLabels.members, path: `${base}/members` }
  ]
  return list
})

function pathToTabKey(path: string): string {
  const id = projectId.value
  if (!id) return ''
  const base = `/projects/${id}`
  if (!path.startsWith(base + '/') && path !== base) return ''
  const rest = path.slice(base.length).replace(/^\//, '') || 'overview'
  const segment = rest.split('/')[0] ?? 'overview'
  const map: Record<string, string> = {
    overview: 'overview',
    requirements: 'requirements',
    modules: 'modules',
    bugs: 'bugs',
    sprints: 'sprints',
    members: 'members'
  }
  return map[segment] ?? 'overview'
}

const activeTabKey = computed(() => pathToTabKey(route.path))

function openTab(path: string) {
  void router.push(path)
}

async function ensureProject() {
  const id = projectId.value
  if (!id) return
  if (projectStore.currentProject?.id === id) {
    projectName.value = projectStore.currentProject.name
    return
  }
  try {
    const res: any = await projectApi.get(id)
    const project = res?.data
    if (project) {
      projectStore.setCurrentProject(project)
      projectName.value = project.name
    } else {
      projectName.value = String(id)
    }
  } catch {
    projectName.value = String(id)
  }
}

onMounted(() => {
  void ensureProject()
})

watch(projectId, () => {
  void ensureProject()
})
</script>

<style scoped>
.project-shell {
  min-height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--app-page-bg);
}
.project-topbar {
  min-height: 52px;
  padding: 0 var(--space-lg);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-lg);
  background: var(--app-layout-header-bg);
  color: var(--app-text-primary);
  border-bottom: 1px solid var(--app-border-soft);
  backdrop-filter: blur(8px);
}
.project-topbar-main {
  display: flex;
  align-items: center;
  gap: var(--space-xl);
  min-width: 0;
}
.project-topbar-title {
  display: inline-flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
  color: var(--app-text-primary);
}
.project-name {
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 200px;
}
.project-topbar-tabs {
  display: flex;
  align-items: stretch;
  min-width: 0;
}
.project-tab {
  min-width: 64px;
  height: 52px;
  border: 0;
  padding: 0 var(--space-md);
  background: transparent;
  color: var(--app-text-secondary);
  cursor: pointer;
  font-size: 14px;
  white-space: nowrap;
}
.project-tab:hover {
  color: var(--app-text-primary);
}
.project-tab.is-active {
  color: var(--app-color-primary);
  font-weight: 600;
  box-shadow: inset 0 -2px 0 var(--app-color-primary);
}
.project-shell-body {
  flex: 1;
  padding: var(--space-lg);
  min-width: 0;
}
@media (max-width: 1439px) {
  .project-topbar {
    flex-wrap: wrap;
    padding-top: var(--space-sm);
    padding-bottom: var(--space-sm);
  }
  .project-topbar-main {
    width: 100%;
    flex-wrap: wrap;
    gap: var(--space-md);
  }
  .project-topbar-tabs {
    width: 100%;
    overflow-x: auto;
  }
  .project-shell-body {
    padding: var(--space-md);
  }
}
</style>
