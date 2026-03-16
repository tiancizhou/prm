<template>
  <div class="app-page dashboard-todo-page">
    <div class="todo-toolbar">
      <div class="todo-filters">
        <el-button
          :type="filterAssigned ? 'primary' : 'default'"
          size="default"
          @click="filterAssigned = true"
        >
          {{ text.todoAssignedToMe }}
        </el-button>
        <el-button
          :type="!filterAssigned ? 'primary' : 'default'"
          size="default"
          @click="filterAssigned = false"
        >
          {{ text.todoUnfinished }}
        </el-button>
        <el-button
          :type="dateRange === 'today' ? 'primary' : 'default'"
          size="default"
          @click="dateRange = 'today'"
        >
          {{ text.todoToday }}
        </el-button>
        <el-button
          :type="dateRange === 'week' ? 'primary' : 'default'"
          size="default"
          @click="dateRange = 'week'"
        >
          {{ text.todoThisWeek }}
        </el-button>
        <el-button
          :type="dateRange === 'month' ? 'primary' : 'default'"
          size="default"
          @click="dateRange = 'month'"
        >
          {{ text.todoThisMonth }}
        </el-button>
      </div>
      <div class="todo-actions">
        <el-input
          v-model="keyword"
          class="todo-search"
          :placeholder="text.todoSearchPlaceholder"
          clearable
          :prefix-icon="Search"
          @input="onSearch"
        />
        <el-button type="primary" :icon="Plus" @click="goAddTodo">{{ text.todoAdd }}</el-button>
      </div>
    </div>

    <div v-loading="loading" class="todo-content">
      <template v-if="items.length === 0">
        <div class="todo-empty">
          <span class="todo-empty-text">{{ text.todoEmpty }}</span>
          <el-button type="primary" :icon="Plus" @click="goAddTodo">{{ text.todoAdd }}</el-button>
        </div>
      </template>
      <template v-else>
        <ul class="todo-list">
          <li v-for="item in items" :key="item.id" class="todo-item">
            <RouterLink :to="item.link" class="todo-item-link">
              <el-tag size="small" class="todo-item-type">{{ item.typeLabel }}</el-tag>
              <span class="todo-item-title">{{ item.title }}</span>
              <span class="todo-item-meta">{{ item.projectName }} · {{ formatDate(item.dueDate || item.createdAt) }}</span>
            </RouterLink>
          </li>
        </ul>
        <div class="todo-footer">
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
import { useRouter } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { bugApi } from '@/api/bug'
import { requirementApi } from '@/api/requirement'
import { projectApi } from '@/api/project'
import { DASHBOARD_I18N } from '@/constants/dashboard'
import { resolveThemeLocale } from '@/constants/theme'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const items = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(15)
const keyword = ref('')
const filterAssigned = ref(true)
const dateRange = ref<'today' | 'week' | 'month' | null>(null)
const projectMap = ref<Record<number, string>>({})

const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const text = DASHBOARD_I18N[currentLocale]
const userId = computed(() => authStore.user?.userId ?? 0)
const isZh = currentLocale === 'zh-CN'

function formatDate(v: string | null | undefined): string {
  if (v == null || v === '') return '-'
  const d = new Date(v)
  if (Number.isNaN(d.getTime())) return String(v)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function getDateRange(): { from: string; to: string } | null {
  if (!dateRange.value) return null
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  const d = String(now.getDate()).padStart(2, '0')
  const today = `${y}-${m}-${d}`
  if (dateRange.value === 'today') return { from: today, to: today }
  if (dateRange.value === 'week') {
    const day = now.getDay()
    const diff = day === 0 ? -6 : 1 - day
    const start = new Date(now)
    start.setDate(now.getDate() + diff)
    const from = start.toISOString().slice(0, 10)
    const end = new Date(start)
    end.setDate(start.getDate() + 6)
    const to = end.toISOString().slice(0, 10)
    return { from, to }
  }
  if (dateRange.value === 'month') {
    const from = `${y}-${m}-01`
    const lastDay = new Date(y, now.getMonth() + 1, 0).getDate()
    const to = `${y}-${m}-${String(lastDay).padStart(2, '0')}`
    return { from, to }
  }
  return null
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
    const range = getDateRange()
    const kw = keyword.value.trim() || undefined
    const combined: any[] = []

    const [reqRes, bugRes] = await Promise.all([
      requirementApi.list({ assigneeId: uid, page: 1, size: 100, keyword: kw }),
      bugApi.list({ assigneeId: uid, page: 1, size: 100, keyword: kw })
    ])
    const reqRecords = (reqRes as any).data?.records ?? []
    const bugRecords = (bugRes as any).data?.records ?? []

    reqRecords.forEach((r: any) => {
      if (r.status === 'DONE' || r.status === 'CLOSED') return
      if (range && r.dueDate) {
        const due = r.dueDate.slice(0, 10)
        if (due < range.from || due > range.to) return
      }
      combined.push({
        id: `req-${r.id}`,
        typeLabel: isZh ? '需求' : 'Requirement',
        title: r.title,
        projectId: r.projectId,
        projectName: projectMap.value[r.projectId] ?? '-',
        dueDate: r.dueDate,
        createdAt: r.createdAt,
        link: `/projects/${r.projectId}/requirements/${r.id}`
      })
    })
    bugRecords.forEach((r: any) => {
      if (r.status === 'RESOLVED' || r.status === 'CLOSED') return
      if (range && r.createdAt) {
        const created = r.createdAt.slice(0, 10)
        if (created < range.from || created > range.to) return
      }
      combined.push({
        id: `bug-${r.id}`,
        typeLabel: 'Bug',
        title: r.title,
        projectId: r.projectId,
        projectName: projectMap.value[r.projectId] ?? '-',
        dueDate: null,
        createdAt: r.createdAt,
        link: `/projects/${r.projectId}/bugs/${r.id}`
      })
    })
    combined.sort((a, b) => {
      const da = a.dueDate || a.createdAt || ''
      const db = b.dueDate || b.createdAt || ''
      return da.localeCompare(db)
    })
    const start = (page.value - 1) * pageSize.value
    items.value = combined.slice(start, start + pageSize.value)
    total.value = combined.length
  } finally {
    loading.value = false
  }
}

function onSearch() {
  page.value = 1
  void load()
}

function goAddTodo() {
  void router.push('/projects')
}

watch([userId, filterAssigned, dateRange], () => {
  page.value = 1
  void load()
})

onMounted(async () => {
  await loadProjects()
  void load()
})
</script>

<style scoped>
.dashboard-todo-page {
  min-width: 0;
}
.todo-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  margin-bottom: var(--space-lg);
  padding-bottom: var(--space-md);
  border-bottom: 1px solid var(--app-border-soft);
}
.todo-filters {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-sm);
}
.todo-actions {
  display: flex;
  align-items: center;
  gap: var(--space-md);
}
.todo-search {
  width: 200px;
}
.todo-content {
  min-height: 200px;
}
.todo-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-md);
  min-height: 280px;
  color: var(--app-text-secondary);
  font-size: 14px;
}
.todo-empty-text {
  margin-right: var(--space-sm);
}
.todo-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.todo-item {
  border-bottom: 1px solid var(--app-border-soft);
}
.todo-item:last-child {
  border-bottom: 0;
}
.todo-item-link {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  padding: var(--space-md) 0;
  color: var(--app-text-primary);
  text-decoration: none;
}
.todo-item-link:hover {
  color: var(--app-color-primary);
}
.todo-item-type {
  flex-shrink: 0;
}
.todo-item-title {
  flex: 1;
  font-weight: 500;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
}
.todo-item-meta {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--app-text-secondary);
}
.todo-footer {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: flex-end;
}
@media (max-width: 767px) {
  .todo-toolbar {
    flex-direction: column;
    align-items: stretch;
  }
  .todo-actions {
    flex-wrap: wrap;
  }
  .todo-search {
    width: 100%;
  }
}
</style>
