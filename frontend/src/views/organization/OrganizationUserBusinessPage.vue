<template>
  <div class="app-page user-business-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ userInfo.realName || userInfo.nickname || userInfo.username || '-' }}</h1>
        <p class="page-subtitle user-meta-line">
          <span>{{ detailText.labels.employeeNo }}: {{ userInfo.employeeNo || '-' }}</span>
          <span>{{ detailText.labels.department }}: {{ userInfo.department || '-' }}</span>
          <span>{{ detailText.labels.email }}: {{ userInfo.email || '-' }}</span>
          <span>{{ detailText.labels.phone }}: {{ userInfo.phone || '-' }}</span>
        </p>
      </div>
      <div class="page-actions">
        <el-button @click="goBack">{{ detailText.back }}</el-button>
      </div>
    </header>

    <el-card class="surface-card user-business-card" shadow="never">
      <el-tabs v-model="activeTab" class="user-business-tabs">
        <el-tab-pane :label="detailText.tabs.schedule" name="schedule">
          <div v-if="scheduleItems.length === 0 && !scheduleLoading" class="empty-hint">{{ detailText.empty.schedule }}</div>
          <div v-else>
            <div v-loading="scheduleLoading" class="schedule-list">
              <div v-for="item in scheduleItems" :key="item.id" class="schedule-item">
                <div class="schedule-item-main">
                  <div class="schedule-item-title">{{ item.title }}</div>
                  <div class="schedule-item-meta">
                    {{ detailText.labels.requirement }}: {{ item.requirementTitle || '-' }}
                    · {{ detailText.labels.status }}: {{ taskStatusLabel(item.status) }}
                  </div>
                </div>
                <div class="schedule-item-time">{{ item.displayTime }}</div>
              </div>
            </div>
            <div class="table-footer">
              <el-pagination
                v-model:current-page="schedulePage"
                v-model:page-size="schedulePageSize"
                :total="scheduleTotal"
                :page-sizes="[10, 15, 20]"
                layout="total, sizes, prev, pager, next"
                small
                @current-change="loadSchedule"
                @size-change="onScheduleSizeChange"
              />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane :label="detailText.tabs.requirements" name="requirements">
          <el-table :data="requirements" v-loading="requirementsLoading" class="detail-table" :empty-text="detailText.empty.requirements">
            <el-table-column prop="title" :label="detailText.labels.title" min-width="220" />
            <el-table-column :label="detailText.labels.status" min-width="120">
              <template #default="{ row }">{{ requirementStatusLabel(row.status) }}</template>
            </el-table-column>
            <el-table-column :label="detailText.labels.priority" min-width="120">
              <template #default="{ row }">{{ requirementPriorityLabel(row.priority) }}</template>
            </el-table-column>
            <el-table-column :label="detailText.labels.dueDate" min-width="140">
              <template #default="{ row }">{{ formatDate(row.dueDate) }}</template>
            </el-table-column>
          </el-table>
          <div class="table-footer">
            <el-pagination
              v-model:current-page="requirementsPage"
              v-model:page-size="requirementsPageSize"
              :total="requirementsTotal"
              :page-sizes="[10, 15, 20]"
              layout="total, sizes, prev, pager, next"
              small
              @current-change="loadRequirements"
              @size-change="onRequirementsSizeChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane :label="detailText.tabs.bugs" name="bugs">
          <el-table :data="bugs" v-loading="bugsLoading" class="detail-table" :empty-text="detailText.empty.bugs">
            <el-table-column prop="title" :label="detailText.labels.title" min-width="220" />
            <el-table-column :label="detailText.labels.severity" min-width="120">
              <template #default="{ row }">{{ bugSeverityLabel(row.severity) }}</template>
            </el-table-column>
            <el-table-column :label="detailText.labels.status" min-width="120">
              <template #default="{ row }">{{ bugStatusLabel(row.status) }}</template>
            </el-table-column>
            <el-table-column :label="detailText.labels.createdAt" min-width="160">
              <template #default="{ row }">{{ formatDate(row.createdAt) }}</template>
            </el-table-column>
          </el-table>
          <div class="table-footer">
            <el-pagination
              v-model:current-page="bugsPage"
              v-model:page-size="bugsPageSize"
              :total="bugsTotal"
              :page-sizes="[10, 15, 20]"
              layout="total, sizes, prev, pager, next"
              small
              @current-change="loadBugs"
              @size-change="onBugsSizeChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import http from '@/api/http'
import { bugApi } from '@/api/bug'
import { requirementApi } from '@/api/requirement'
import { taskApi } from '@/api/task'
import { REQUIREMENT_I18N } from '@/constants/requirement'
import { BUG_PAGE_I18N } from '@/constants/bug'
import { TASK_PAGE_I18N } from '@/constants/task'
import { resolveThemeLocale } from '@/constants/theme'
import { USER_BUSINESS_DETAIL_I18N } from '@/constants/userBusinessDetail'

const route = useRoute()
const router = useRouter()
const userId = Number(route.params.userId)
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const detailText = USER_BUSINESS_DETAIL_I18N[currentLocale]
const requirementText = REQUIREMENT_I18N[currentLocale]
const bugText = BUG_PAGE_I18N[currentLocale]
const taskText = TASK_PAGE_I18N[currentLocale]

function requirementStatusLabel(value: string | undefined | null): string {
  if (value == null || value === '') return '-'
  const opt = requirementText.statusOptions.find((o) => o.value === value)
  return opt?.label ?? value
}

function requirementPriorityLabel(value: string | undefined | null): string {
  if (value == null || value === '') return '-'
  return (requirementText.priorityLabels as Record<string, string>)[value] ?? value
}

function bugStatusLabel(value: string | undefined | null): string {
  if (value == null || value === '') return '-'
  return bugText.statusLabels[value] ?? value
}

function bugSeverityLabel(value: string | undefined | null): string {
  if (value == null || value === '') return '-'
  return bugText.severityLabels[value] ?? value
}

function taskStatusLabel(value: string | undefined | null): string {
  if (value == null || value === '') return '-'
  return (taskText.statusLabels as Record<string, string>)[value] ?? value
}

const activeTab = ref<'schedule' | 'requirements' | 'bugs'>('schedule')
const userInfo = ref<any>({})

const schedulePage = ref(1)
const schedulePageSize = ref(10)
const scheduleTotal = ref(0)
const scheduleLoading = ref(false)
const scheduleItems = ref<any[]>([])

const requirementsPage = ref(1)
const requirementsPageSize = ref(10)
const requirementsTotal = ref(0)
const requirementsLoading = ref(false)
const requirements = ref<any[]>([])

const bugsPage = ref(1)
const bugsPageSize = ref(10)
const bugsTotal = ref(0)
const bugsLoading = ref(false)
const bugs = ref<any[]>([])

async function loadUserInfo() {
  const response: any = await http.get(`/system/users/${userId}`)
  userInfo.value = response.data || {}
}

function formatDate(value: string | null | undefined): string {
  if (value == null || value === '') return '-'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const h = d.getHours()
  const min = d.getMinutes()
  if (h === 0 && min === 0) return `${y}-${m}-${day}`
  return `${y}-${m}-${day} ${String(h).padStart(2, '0')}:${String(min).padStart(2, '0')}`
}

async function loadSchedule() {
  scheduleLoading.value = true
  try {
    const response: any = await taskApi.list({
      page: schedulePage.value,
      size: schedulePageSize.value,
      assigneeId: userId
    })
    const records = response.data?.records || []
    scheduleTotal.value = response.data?.total ?? 0
    scheduleItems.value = records.map((item: any) => ({
      ...item,
      displayTime: formatDate(item.dueDate || item.createdAt) || '-'
    }))
  } finally {
    scheduleLoading.value = false
  }
}

async function loadRequirements() {
  requirementsLoading.value = true
  try {
    const response: any = await requirementApi.list({
      page: requirementsPage.value,
      size: requirementsPageSize.value,
      assigneeId: userId
    })
    const data = response.data
    requirements.value = data?.records || []
    requirementsTotal.value = data?.total ?? 0
  } finally {
    requirementsLoading.value = false
  }
}

async function loadBugs() {
  bugsLoading.value = true
  try {
    const response: any = await bugApi.list({
      page: bugsPage.value,
      size: bugsPageSize.value,
      assigneeId: userId
    })
    const data = response.data
    bugs.value = data?.records || []
    bugsTotal.value = data?.total ?? 0
  } finally {
    bugsLoading.value = false
  }
}

function onScheduleSizeChange() {
  schedulePage.value = 1
  void loadSchedule()
}

function onRequirementsSizeChange() {
  requirementsPage.value = 1
  void loadRequirements()
}

function onBugsSizeChange() {
  bugsPage.value = 1
  void loadBugs()
}

function goBack() {
  const from = String(route.query.from || '')
  const projectId = Number(route.query.projectId)
  if (from === 'project-members' && Number.isFinite(projectId) && projectId > 0) {
    void router.push(`/projects/${projectId}/members`)
    return
  }
  void router.push('/organization/team')
}

onMounted(async () => {
  await loadUserInfo()
  await Promise.all([loadSchedule(), loadRequirements(), loadBugs()])
})
</script>

<style scoped>
.user-business-page { min-width: 0; }

.title-block { display: flex; flex-direction: column; gap: var(--space-xs); }

.user-meta-line {
  margin: 0;
  font-size: 14px;
  color: var(--app-text-secondary);
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-md);
}

.user-business-card { min-width: 0; }

.user-business-tabs :deep(.el-tabs__header) { margin-bottom: var(--space-lg); }
.user-business-tabs :deep(.el-tabs__item) { color: var(--app-text-secondary); }
.user-business-tabs :deep(.el-tabs__item.is-active) { color: var(--app-color-primary); }
.user-business-tabs :deep(.el-tabs__ink-bar) { background-color: var(--app-color-primary); }
.user-business-tabs :deep(.el-tabs__nav-wrap::after) { background-color: var(--app-border-soft); }

.empty-hint {
  padding: var(--space-xl);
  text-align: center;
  color: var(--app-text-muted);
  font-size: 14px;
}

.schedule-list { display: flex; flex-direction: column; gap: 0; }

.schedule-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: var(--space-lg);
  padding: var(--space-md) 0;
  border-bottom: 1px solid var(--app-border-soft);
}

.schedule-item:last-child { border-bottom: none; }

.schedule-item-main { min-width: 0; flex: 1; }

.schedule-item-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-primary);
  margin-bottom: var(--space-xs);
}

.schedule-item-meta {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.schedule-item-time {
  font-size: 13px;
  color: var(--app-text-muted);
  flex-shrink: 0;
}

.detail-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-table-header-bg);
}

.detail-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.table-footer {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: flex-end;
}
</style>
