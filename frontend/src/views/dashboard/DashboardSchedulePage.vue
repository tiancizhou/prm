<template>
  <div class="app-page dashboard-schedule-page">
    <div class="schedule-toolbar">
      <el-button type="primary" plain disabled class="filter-btn">{{ text.scheduleAssignedToMe }}</el-button>
      <el-input
        v-model="keyword"
        class="schedule-search"
        :placeholder="text.scheduleSearchPlaceholder"
        clearable
        :prefix-icon="Search"
        @input="onSearch"
      />
    </div>
    <div v-loading="loading" class="schedule-content">
      <template v-if="items.length === 0">
        <div class="schedule-empty">
          <span class="schedule-empty-text">{{ text.scheduleEmpty }}</span>
        </div>
      </template>
      <template v-else>
        <ul class="schedule-list">
          <li v-for="item in items" :key="item.id" class="schedule-item">
            <RouterLink :to="scheduleItemLink(item)" class="schedule-item-link">
              <span class="schedule-item-title">{{ item.title }}</span>
              <span class="schedule-item-meta">{{ item.projectName }} · {{ formatDate(item.dueDate) }}</span>
            </RouterLink>
          </li>
        </ul>
        <div class="schedule-footer">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            layout="total, prev, pager, next"
            small
            @current-change="load"
          />
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { requirementApi } from '@/api/requirement'
import { projectApi } from '@/api/project'
import { DASHBOARD_I18N } from '@/constants/dashboard'
import { resolveThemeLocale } from '@/constants/theme'

const authStore = useAuthStore()
const loading = ref(false)
const items = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(15)
const keyword = ref('')
const projectMap = ref<Record<number, string>>({})

const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const text = DASHBOARD_I18N[currentLocale]
const userId = computed(() => authStore.user?.userId ?? 0)

function formatDate(v: string | null | undefined): string {
  if (v == null || v === '') return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function scheduleItemLink(item: any): string {
  const pid = item.projectId
  const reqId = item.id
  return pid && reqId ? `/projects/${pid}/requirements/${reqId}` : '/projects'
}

async function loadProjects() {
  try {
    const res: any = await projectApi.list({ page: 1, size: 500 })
    const list = res?.data?.records ?? []
    projectMap.value = Object.fromEntries(list.map((p: any) => [p.id, p.name || p.code || '-']))
  } catch {
    projectMap.value = {}
  }
}

async function load() {
  const uid = userId.value
  if (!uid) return
  loading.value = true
  try {
    const res: any = await requirementApi.list({
      assigneeId: uid,
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value.trim() || undefined
    })
    const records = res?.data?.records ?? []
    total.value = res?.data?.total ?? 0
    items.value = records.map((r: any) => ({
      ...r,
      projectName: projectMap.value[r.projectId] ?? '-'
    }))
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  void load()
}

watch(userId, (uid) => {
  if (uid) void load()
})

onMounted(async () => {
  await loadProjects()
  void load()
})
</script>

<style scoped>
.dashboard-schedule-page {
  min-width: 0;
}
.schedule-toolbar {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
}
.filter-btn {
  flex-shrink: 0;
}
.schedule-search {
  max-width: 280px;
}
.schedule-content {
  min-height: 200px;
}
.schedule-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 240px;
  color: var(--app-text-secondary);
  font-size: 14px;
}
.schedule-empty-text {
  margin-right: var(--space-sm);
}
.schedule-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.schedule-item {
  border-bottom: 1px solid var(--app-border-soft);
}
.schedule-item:last-child {
  border-bottom: 0;
}
.schedule-item-link {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: var(--space-md) 0;
  color: var(--app-text-primary);
  text-decoration: none;
}
.schedule-item-link:hover {
  color: var(--app-color-primary);
}
.schedule-item-title {
  font-weight: 500;
}
.schedule-item-meta {
  font-size: 12px;
  color: var(--app-text-secondary);
}
.schedule-footer {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: flex-end;
}
</style>
