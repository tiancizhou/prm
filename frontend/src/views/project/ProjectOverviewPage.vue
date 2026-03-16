<template>
  <div class="app-page overview-page">

    <header class="page-header">
      <div>
        <h1 class="page-title">{{ overviewText.pageTitle }}</h1>
        <p class="page-subtitle">{{ overviewText.pageSubtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button :icon="Edit" type="primary" plain @click="openEditDialog">
          {{ overviewText.editDescription }}
        </el-button>
      </div>
    </header>

    <!-- ═══════════════════ HEADER CARD ═══════════════════ -->
    <el-card class="header-card surface-card" shadow="never" v-loading="loading">
      <div class="header-inner">
        <div class="header-left">
          <div class="project-title-row">
            <span class="project-code-badge">{{ project?.code }}</span>
            <h2 class="project-name">{{ project?.name }}</h2>
            <el-tag :type="statusType(project?.status)" size="large" effect="light">
              {{ statusLabel(project?.status) }}
            </el-tag>
          </div>

          <p class="project-desc">
            {{ project?.description || overviewText.emptyDescription }}
          </p>

          <div class="meta-row">
            <div class="meta-item">
              <el-icon class="meta-icon"><User /></el-icon>
              <span class="meta-label">{{ overviewText.metaLabels.owner }}</span>
              <span class="meta-value">{{ project?.ownerName || '-' }}</span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <el-icon class="meta-icon"><Calendar /></el-icon>
              <span class="meta-label">{{ overviewText.metaLabels.start }}</span>
              <span class="meta-value">{{ formatDate(project?.startDate) }}</span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <el-icon class="meta-icon"><Aim /></el-icon>
              <span class="meta-label">{{ overviewText.metaLabels.end }}</span>
              <span class="meta-value" :class="{ 'text-danger': isOverdue }">
                {{ formatDate(project?.endDate) }}
                <el-tag v-if="isOverdue" type="danger" size="small" class="overdue-tag">{{ overviewText.overdueTag }}</el-tag>
              </span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <el-icon class="meta-icon"><View /></el-icon>
              <span class="meta-label">{{ overviewText.metaLabels.visibility }}</span>
              <span class="meta-value">{{ visibilityLabel(project?.visibility) }}</span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <el-icon class="meta-icon"><Clock /></el-icon>
              <span class="meta-label">{{ overviewText.metaLabels.created }}</span>
              <span class="meta-value">{{ formatDate(project?.createdAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- ═══════════════════ KPI CARDS ═══════════════════ -->
    <el-row :gutter="16" class="kpi-row" v-loading="statsLoading">

      <!-- 1. Overall Progress -->
      <el-col :span="6">
        <el-card class="kpi-card surface-card" shadow="never">
          <div class="kpi-header">
            <span class="kpi-title">{{ overviewText.kpiTitles.progress }}</span>
            <div class="kpi-icon kpi-icon--blue">
              <el-icon><TrendCharts /></el-icon>
            </div>
          </div>
          <div class="kpi-value kpi-value--blue">{{ progressPercent }}<span class="kpi-unit">%</span></div>
          <el-progress
            :percentage="progressPercent"
            :stroke-width="6"
            :show-text="false"
            color="var(--app-color-primary)"
            class="kpi-progress"
          />
          <div class="kpi-sub">
            {{ stats?.doneRequirements ?? 0 }} / {{ stats?.totalRequirements ?? 0 }} {{ overviewText.requirementsCompleted }}
          </div>
        </el-card>
      </el-col>

      <!-- 2. Open Tasks -->
      <el-col :span="6">
        <el-card class="kpi-card surface-card" shadow="never">
          <div class="kpi-header">
            <span class="kpi-title">{{ overviewText.kpiTitles.openTasks }}</span>
            <div class="kpi-icon kpi-icon--orange">
              <el-icon><List /></el-icon>
            </div>
          </div>
          <div class="kpi-value kpi-value--orange">{{ stats?.inProgressTasks ?? '-' }}</div>
          <div class="kpi-sub kpi-sub--spaced">
            <el-tag v-if="overdueTaskCount > 0" type="warning" size="small">
              {{ overdueTaskCount }} {{ overviewText.overdueItems }}
            </el-tag>
            <span v-else class="text-success">{{ overviewText.noOverdueTasks }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 3. Open Bugs -->
      <el-col :span="6">
        <el-card class="kpi-card surface-card" shadow="never">
          <div class="kpi-header">
            <span class="kpi-title">{{ overviewText.kpiTitles.openBugs }}</span>
            <div class="kpi-icon kpi-icon--red">
              <el-icon><Warning /></el-icon>
            </div>
          </div>
          <div class="kpi-value kpi-value--red">{{ stats?.openBugs ?? '-' }}</div>
          <div class="kpi-sub kpi-sub--spaced">
            <el-tag v-if="(stats?.criticalOpenBugs ?? 0) > 0" type="danger" size="small">
              {{ stats?.criticalOpenBugs }} {{ overviewText.criticalCountSuffix }}
            </el-tag>
            <span v-else class="text-success">{{ overviewText.noCriticalBugs }}</span>
          </div>
        </el-card>
      </el-col>

      <!-- 4. Days Left in Sprint -->
      <el-col :span="6">
        <el-card class="kpi-card surface-card" shadow="never">
          <div class="kpi-header">
            <span class="kpi-title">{{ overviewText.kpiTitles.sprintDaysLeft }}</span>
            <div class="kpi-icon kpi-icon--purple">
              <el-icon><Timer /></el-icon>
            </div>
          </div>
          <div class="kpi-value" :class="daysLeftClass">
            {{ daysLeft !== null ? daysLeft : '-' }}<span v-if="daysLeft !== null" class="kpi-unit">{{ overviewText.dayUnit }}</span>
          </div>
          <div class="kpi-sub kpi-sub--spaced">
            <span v-if="activeSprint">{{ activeSprint.name }}</span>
            <span v-else class="text-muted">{{ overviewText.noActiveSprint }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ═══════════════════ MAIN SPLIT LAYOUT ═══════════════════ -->
    <el-row :gutter="16" class="content-row">

      <!-- ──── LEFT COLUMN (7/10) ──── -->
      <el-col :span="17">

        <!-- Trend Chart -->
        <el-card class="chart-card surface-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ overviewText.trendTitle }}</span>
              <div class="card-header-right">
                <el-tag type="info" size="small" class="chart-range-tag">{{ overviewText.recent7Days }}</el-tag>
                <el-button link size="small" :icon="Refresh" @click="refreshTrend">{{ overviewText.refresh }}</el-button>
              </div>
            </div>
          </template>

          <div class="chart-wrapper">
            <svg viewBox="0 0 620 200" class="trend-svg" v-if="trendData.length > 0">
              <!-- Grid lines -->
              <line
                v-for="i in 4" :key="'grid-h-' + i"
                x1="50" :y1="i * 40" x2="600" :y2="i * 40"
                stroke="var(--app-chart-grid)" stroke-width="1"
              />
              <!-- Y-axis labels -->
              <text
                v-for="i in 5" :key="'y-' + i"
                x="44"
                :y="(5 - i) * 40 + 4"
                text-anchor="end"
                font-size="11"
                fill="var(--app-chart-axis)"
              >
                {{ Math.round(maxTrendVal * ((i - 1) / 4)) }}
              </text>
              <!-- X-axis labels -->
              <text
                v-for="(d, idx) in trendData"
                :key="'x-' + idx"
                :x="chartX(idx)"
                y="196"
                text-anchor="middle"
                font-size="11"
                fill="var(--app-chart-axis)"
              >
                {{ d.date }}
              </text>

              <!-- Task area fill -->
              <path :d="taskAreaPath" fill="var(--app-chart-task)" fill-opacity="0.08" />
              <!-- Bug area fill -->
              <path :d="bugAreaPath" fill="var(--app-chart-bug)" fill-opacity="0.08" />

              <!-- Task line -->
              <polyline
                :points="taskPoints"
                fill="none"
                stroke="var(--app-chart-task)"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <!-- Bug line -->
              <polyline
                :points="bugPoints"
                fill="none"
                stroke="var(--app-chart-bug)"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />

              <!-- Task dots -->
              <circle
                v-for="(d, idx) in trendData" :key="'td-' + idx"
                :cx="chartX(idx)" :cy="chartY(d.tasks)"
                r="4" fill="var(--app-bg-elevated)" stroke="var(--app-chart-task)" stroke-width="2"
              />
              <!-- Bug dots -->
              <circle
                v-for="(d, idx) in trendData" :key="'bd-' + idx"
                :cx="chartX(idx)" :cy="chartY(d.bugs)"
                r="4" fill="var(--app-bg-elevated)" stroke="var(--app-chart-bug)" stroke-width="2"
              />
            </svg>
            <el-empty v-else :description="overviewText.emptyTrend" :image-size="80" />

            <!-- Legend -->
            <div class="chart-legend">
              <span class="legend-item">
                <span class="legend-dot legend-dot--blue"></span>{{ overviewText.legendOpenTasks }}
              </span>
              <span class="legend-item">
                <span class="legend-dot legend-dot--red"></span>{{ overviewText.legendOpenBugs }}
              </span>
            </div>
          </div>
        </el-card>

        <!-- My To-Dos -->
        <el-card class="todos-card surface-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ overviewText.myTodos }}</span>
              <el-button link size="small" :icon="ArrowRight" @click="goToRequirements">{{ overviewText.viewAllRequirements }}</el-button>
            </div>
          </template>

          <div v-if="todosLoading" class="section-loading">
            <el-icon class="is-loading" size="24"><Loading /></el-icon>
          </div>
          <el-empty v-else-if="myTodos.length === 0" :description="overviewText.emptyTodos" :image-size="70" />
          <div v-else class="todo-list">
            <div
              v-for="task in myTodos"
              :key="task.id"
              class="todo-item"
              :class="{ 'todo-item--overdue': isTaskOverdue(task) }"
            >
              <div class="todo-left">
                <div class="todo-status-dot" :class="todoStatusDotClass(task.status)" />
                <div>
                  <div class="todo-title">{{ task.title }}</div>
                  <div class="todo-meta">
                    <el-tag :type="priorityType(task.priority)" size="small" effect="light">
                      {{ priorityLabel(task.priority) }}{{ overviewText.prioritySuffix }}
                    </el-tag>
                    <span v-if="task.dueDate || task.endDate" class="todo-date" :class="{ 'text-danger': isTaskOverdue(task) }">
                      <el-icon class="todo-date-icon"><Clock /></el-icon>
                      {{ overviewText.duePrefix }} {{ formatDate(task.dueDate || task.endDate) }}
                    </span>
                    <el-tag v-if="isTaskOverdue(task)" type="danger" size="small">{{ overviewText.overdueTag }}</el-tag>
                  </div>
                </div>
              </div>
              <el-tag :type="taskStatusType(task.status)" size="small" effect="plain">
                {{ taskStatusLabel(task.status) }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- ──── RIGHT COLUMN (3/10) ──── -->
      <el-col :span="7">
        <el-card class="activity-card surface-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">{{ overviewText.recentActivity }}</span>
              <el-button link size="small" :icon="Refresh" :loading="activityLoading" @click="loadActivity">
                {{ overviewText.refresh }}
              </el-button>
            </div>
          </template>

          <div v-if="activityLoading && activities.length === 0" class="section-loading">
            <el-icon class="is-loading" size="24"><Loading /></el-icon>
          </div>
          <el-empty v-else-if="activities.length === 0" :description="overviewText.emptyActivities" :image-size="70" />
          <el-timeline v-else class="activity-timeline">
            <el-timeline-item
              v-for="(act, idx) in activities"
              :key="idx"
              :type="act.type"
              :timestamp="act.time"
              placement="top"
              size="normal"
            >
              <div class="activity-item">
                <span class="activity-actor">{{ act.actor }}</span>
                <span class="activity-action">{{ act.action }}</span>
                <span class="activity-target">{{ overviewText.targetQuoteLeft }}{{ act.target }}{{ overviewText.targetQuoteRight }}</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- ═══════════════════ EDIT DESCRIPTION DIALOG ═══════════════════ -->
    <el-dialog
      v-model="editDialogVisible"
      :title="overviewText.dialogTitle"
      width="520px"
      :close-on-click-modal="false"
      draggable
    >
      <el-input
        v-model="editDesc"
        name="projectDescription"
        autocomplete="off"
        type="textarea"
        :rows="5"
        :placeholder="overviewText.dialogPlaceholder"
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="editDialogVisible = false">{{ overviewText.cancel }}</el-button>
        <el-button type="primary" @click="saveDesc" :loading="savingDesc">{{ overviewText.save }}</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Edit, User, Calendar, Aim, View, Clock, Timer,
  Warning, List, TrendCharts,
  ArrowRight, Refresh, Loading
} from '@element-plus/icons-vue'
import { projectApi } from '@/api/project'
import { dashboardApi } from '@/api/dashboard'
import { requirementApi } from '@/api/requirement'
import { useAuthStore } from '@/stores/auth'
import { sprintApi } from '@/api/sprint'
import { useProjectStore } from '@/stores/project'
import { PROJECT_OVERVIEW_I18N } from '@/constants/projectOverview'
import { resolveThemeLocale } from '@/constants/theme'

// ─── Route & Store ───────────────────────────────────────────────────────────
const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const authStore = useAuthStore()
const projectId = Number(route.params.id)
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const overviewText = PROJECT_OVERVIEW_I18N[currentLocale]

// ─── Reactive State ──────────────────────────────────────────────────────────
const loading = ref(false)
const statsLoading = ref(false)
const todosLoading = ref(false)
const activityLoading = ref(false)

const project = ref<any>(null)
const stats = ref<any>(null)
const myTodos = ref<any[]>([])
const activeSprint = ref<any>(null)
const activities = ref<any[]>([])
const trendData = ref<TrendPoint[]>([])

const editDialogVisible = ref(false)
const editDesc = ref('')
const savingDesc = ref(false)

interface TrendPoint { date: string; tasks: number; bugs: number }

// ─── Computed ────────────────────────────────────────────────────────────────
const isOverdue = computed(() => {
  if (!project.value?.endDate) return false
  return new Date(project.value.endDate) < new Date()
})

const progressPercent = computed(() => {
  const total = stats.value?.totalRequirements ?? 0
  const done = stats.value?.doneRequirements ?? 0
  if (!total) return 0
  return Math.round((done / total) * 100)
})

const overdueTaskCount = computed(() =>
  myTodos.value.filter(t => isTaskOverdue(t)).length
)

const daysLeft = computed<number | null>(() => {
  if (!activeSprint.value?.endDate) return null
  const diff = new Date(activeSprint.value.endDate).getTime() - Date.now()
  return Math.max(0, Math.ceil(diff / 86400000))
})

const daysLeftClass = computed(() => {
  const d = daysLeft.value
  if (d === null) return 'kpi-value--muted'
  if (d <= 3) return 'kpi-value--red'
  if (d <= 7) return 'kpi-value--orange'
  return 'kpi-value--purple'
})

const maxTrendVal = computed(() =>
  Math.max(...trendData.value.flatMap(d => [d.tasks, d.bugs]), 1)
)

// SVG chart helpers ───────────────────────────────────────────────────────────
const CHART = { x0: 50, x1: 600, y0: 10, y1: 160 }

function chartX(idx: number): number {
  const n = trendData.value.length
  return n <= 1 ? (CHART.x0 + CHART.x1) / 2 : CHART.x0 + idx * ((CHART.x1 - CHART.x0) / (n - 1))
}

function chartY(val: number): number {
  return CHART.y1 - (val / maxTrendVal.value) * (CHART.y1 - CHART.y0)
}

const taskPoints = computed(() =>
  trendData.value.map((d, i) => `${chartX(i)},${chartY(d.tasks)}`).join(' ')
)

const bugPoints = computed(() =>
  trendData.value.map((d, i) => `${chartX(i)},${chartY(d.bugs)}`).join(' ')
)

const taskAreaPath = computed(() => {
  if (!trendData.value.length) return ''
  const pts = trendData.value.map((d, i) => `${chartX(i)},${chartY(d.tasks)}`)
  const last = trendData.value.length - 1
  return `M ${pts[0]} L ${pts.join(' L ')} L ${chartX(last)},${CHART.y1} L ${chartX(0)},${CHART.y1} Z`
})

const bugAreaPath = computed(() => {
  if (!trendData.value.length) return ''
  const pts = trendData.value.map((d, i) => `${chartX(i)},${chartY(d.bugs)}`)
  const last = trendData.value.length - 1
  return `M ${pts[0]} L ${pts.join(' L ')} L ${chartX(last)},${CHART.y1} L ${chartX(0)},${CHART.y1} Z`
})

// ─── Formatting Helpers ───────────────────────────────────────────────────────
function formatDate(d?: string | null): string {
  if (!d) return '-'
  const date = new Date(d)
  if (isNaN(date.getTime())) return d
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function formatRelativeTime(d?: string | null): string {
  if (!d) return ''
  const date = new Date(d)
  if (isNaN(date.getTime())) return d
  const diffMs = Date.now() - date.getTime()
  const mins = Math.floor(diffMs / 60000)
  if (mins < 1) return overviewText.justNow
  if (mins < 60) return `${mins} ${overviewText.minutesAgo}`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs} ${overviewText.hoursAgo}`
  const days = Math.floor(hrs / 24)
  if (days < 7) return `${days} ${overviewText.daysAgo}`
  return formatDate(d)
}

// ─── Label / Type Maps ────────────────────────────────────────────────────────
function statusType(s?: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' {
  if (s === 'ACTIVE') return 'success'
  if (s === 'ARCHIVED') return 'info'
  return 'danger'
}

function statusLabel(s?: string): string {
  const m = overviewText.statusLabels
  return s ? (m[s] ?? s) : ''
}

function visibilityLabel(v?: string): string {
  return v === 'PUBLIC'
    ? overviewText.visibilityLabels.public
    : v === 'PRIVATE'
      ? overviewText.visibilityLabels.private
      : v ?? '-'
}

function priorityType(p?: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' {
  if (p === 'HIGH') return 'danger'
  if (p === 'MEDIUM') return 'warning'
  return 'info'
}

function priorityLabel(p?: string): string {
  const m = overviewText.priorityLabels
  return m[p ?? ''] ?? p ?? '-'
}

function taskStatusType(s?: string): 'primary' | 'success' | 'info' | 'warning' | 'danger' {
  if (s === 'DONE' || s === 'CLOSED') return 'success'
  if (s === 'IN_PROGRESS') return 'warning'
  return 'info'
}

function taskStatusLabel(s?: string): string {
  const m = overviewText.taskStatusLabels
  return m[s ?? ''] ?? s ?? '-'
}

function todoStatusDotClass(s?: string): string {
  if (s === 'DONE' || s === 'CLOSED') return 'dot--green'
  if (s === 'IN_PROGRESS' || s === 'ACTIVE') return 'dot--orange'
  if (s === 'PENDING_REVIEW') return 'dot--orange'
  return 'dot--grey'
}

function isTaskOverdue(item: any): boolean {
  const dateStr = item.dueDate || item.endDate
  if (!dateStr) return false
  return new Date(dateStr) < new Date() && item.status !== 'DONE' && item.status !== 'CLOSED'
}

// ─── Simulated Trend Data ─────────────────────────────────────────────────────
function buildTrendData(): TrendPoint[] {
  const openTasks = stats.value?.inProgressTasks ?? 8
  const openBugs = stats.value?.openBugs ?? 4
  const result: TrendPoint[] = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    const label = `${d.getMonth() + 1}/${d.getDate()}`
    const factor = i / 6
    result.push({
      date: label,
      tasks: Math.max(1, Math.round(openTasks * (0.6 + factor * 0.6) + (Math.random() * 3 - 1))),
      bugs: Math.max(0, Math.round(openBugs * (0.5 + factor * 0.7) + (Math.random() * 2 - 1)))
    })
  }
  return result
}

// ─── Data Loading ─────────────────────────────────────────────────────────────
async function loadProject(): Promise<void> {
  loading.value = true
  try {
    const res = await projectApi.get(projectId)
    project.value = (res as any).data
    projectStore.setCurrentProject(project.value)
    editDesc.value = project.value?.description ?? ''
  } finally {
    loading.value = false
  }
}

async function loadStats(): Promise<void> {
  statsLoading.value = true
  try {
    const res = await dashboardApi.overview(projectId)
    stats.value = (res as any).data
  } finally {
    statsLoading.value = false
  }
}

async function loadTodos(): Promise<void> {
  todosLoading.value = true
  try {
    const uid = authStore.user?.userId
    const res = await requirementApi.list({
      projectId,
      ...(uid ? { assigneeId: uid } : {}),
      page: 1,
      size: 5
    })
    const data = (res as any).data
    const list: any[] = data?.records ?? data?.list ?? (Array.isArray(data) ? data : [])
    myTodos.value = list.filter((r: any) => r.status !== 'DONE' && r.status !== 'CLOSED').slice(0, 5)
  } catch {
    myTodos.value = []
  } finally {
    todosLoading.value = false
  }
}

async function loadSprint(): Promise<void> {
  try {
    const res = await sprintApi.list({ projectId })
    const data = (res as any).data
    const list: any[] = data?.records ?? data?.list ?? (Array.isArray(data) ? data : [])
    activeSprint.value = list.find((s: any) => s.status === 'ACTIVE') ?? null
  } catch {
    activeSprint.value = null
  }
}

async function loadActivity(): Promise<void> {
  activityLoading.value = true
  try {
    const res = await requirementApi.list({ projectId, page: 1, size: 8 })
    const data = (res as any).data
    const list: any[] = data?.records ?? data?.list ?? (Array.isArray(data) ? data : [])
    const actionMap = overviewText.activityActionMap
    const typeMap: Record<string, string> = {
      DONE: 'success',
      CLOSED: 'success',
      IN_PROGRESS: 'warning',
      ACTIVE: 'primary',
      PENDING_REVIEW: 'warning'
    }
    activities.value = list.slice(0, 8).map((r: any) => ({
      actor: r.assigneeName ?? overviewText.teamMemberFallback,
      action: actionMap[r.status] ?? overviewText.updatedFallback,
      target: r.title ?? `${overviewText.requirementPrefix} #${r.id}`,
      time: formatRelativeTime(r.updatedAt ?? r.createdAt),
      type: typeMap[r.status] ?? 'primary'
    }))
  } catch {
    activities.value = []
  } finally {
    activityLoading.value = false
  }
}

function refreshTrend(): void {
  trendData.value = buildTrendData()
}

// ─── Edit Description ─────────────────────────────────────────────────────────
function openEditDialog(): void {
  editDesc.value = project.value?.description ?? ''
  editDialogVisible.value = true
}

async function saveDesc(): Promise<void> {
  savingDesc.value = true
  try {
    await projectApi.update(projectId, { description: editDesc.value })
    if (project.value) project.value.description = editDesc.value
    editDialogVisible.value = false
    ElMessage.success(overviewText.descUpdated)
  } catch {
    ElMessage.error(overviewText.updateFailed)
  } finally {
    savingDesc.value = false
  }
}

function goToRequirements(): void {
  router.push(`/projects/${projectId}/requirements`)
}

// ─── Lifecycle ────────────────────────────────────────────────────────────────
onMounted(async () => {
  await loadProject()
  await Promise.all([loadStats(), loadTodos(), loadSprint(), loadActivity()])
  trendData.value = buildTrendData()
})
</script>

<style scoped>
/* ─── Page Shell ────────────────────────────────────────────────────────────── */
.overview-page {
  padding: 0;
  min-height: 100%;
  min-width: 0;
}

/* ─── Header Card ───────────────────────────────────────────────────────────── */
.header-card {
  margin-bottom: var(--space-lg);
  border-radius: var(--app-radius-sm);
  border: 1px solid var(--app-border-soft);
}

.header-inner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-xl);
}

.header-left {
  flex: 1;
  min-width: 0;
}

.project-title-row {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  margin-bottom: var(--space-md);
  flex-wrap: wrap;
}

.project-code-badge {
  display: inline-block;
  background: var(--app-color-primary-soft);
  color: var(--app-color-primary);
  border: 1px solid var(--app-border-strong);
  border-radius: var(--el-border-radius-small);
  padding: var(--space-xs) var(--space-sm);
  font-size: 12px;
  font-weight: 700;
  font-family: 'SF Mono', 'Fira Code', monospace;
  letter-spacing: 0.5px;
}

.project-name {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: var(--app-text-primary);
  line-height: 1.3;
}

.project-desc {
  color: var(--app-text-secondary);
  font-size: 14px;
  margin: 0 0 var(--space-lg);
  line-height: 1.65;
  max-width: 680px;
}

.meta-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  padding: 0 var(--space-md);
  font-size: 13px;
  color: var(--app-text-secondary);
}

.meta-item:first-child {
  padding-left: 0;
}

.meta-icon {
  color: var(--app-text-muted);
  font-size: 14px;
}

.meta-label {
  color: var(--app-text-muted);
  font-size: 12px;
}

.meta-value {
  font-weight: 500;
  color: var(--app-text-primary);
}

.overdue-tag {
  margin-left: var(--space-sm);
}

.meta-divider {
  width: 1px;
  height: 14px;
  background: var(--app-border-strong);
  flex-shrink: 0;
}

/* ─── KPI Row ───────────────────────────────────────────────────────────────── */
.kpi-row {
  margin-bottom: var(--space-lg);
}

.kpi-card {
  border-radius: var(--app-radius-sm);
  border: 1px solid var(--app-border-soft);
  transition: box-shadow 0.2s, transform 0.2s;
}

.kpi-card:hover {
  box-shadow: var(--app-shadow-hover);
  transform: translateY(-2px);
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-md);
}

.kpi-title {
  font-size: 13px;
  color: var(--app-text-secondary);
  font-weight: 500;
}

.kpi-icon {
  width: 34px;
  height: 34px;
  border-radius: var(--el-border-radius-small);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.kpi-icon--blue  { background: var(--app-color-primary-soft); color: var(--app-color-primary); }
.kpi-icon--orange { background: var(--app-color-warning-soft); color: var(--app-color-warning); }
.kpi-icon--red   { background: var(--app-color-danger-soft); color: var(--app-color-danger); }
.kpi-icon--purple { background: var(--app-color-accent-soft); color: var(--app-color-accent); }

.kpi-value {
  font-size: 38px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -1px;
}

.kpi-unit {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0;
  margin-left: var(--space-xs);
}

.kpi-value--blue   { color: var(--app-color-primary); }
.kpi-value--orange { color: var(--app-color-warning); }
.kpi-value--red    { color: var(--app-color-danger); }
.kpi-value--purple { color: var(--app-color-accent); }
.kpi-value--muted  { color: var(--app-dot-muted); }

.kpi-sub {
  font-size: 12px;
  color: var(--app-text-muted);
  min-height: 22px;
}

.kpi-progress {
  margin-top: var(--space-sm);
}

.kpi-sub--spaced {
  margin-top: var(--space-md);
}

/* ─── Split Layout ──────────────────────────────────────────────────────────── */
.content-row {
  margin-bottom: var(--space-lg);
  align-items: flex-start;
}

/* ─── Chart Card ────────────────────────────────────────────────────────────── */
.chart-card {
  margin-bottom: var(--space-lg);
  border-radius: var(--app-radius-sm);
  border: 1px solid var(--app-border-soft);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header-right {
  display: flex;
  align-items: center;
}

.chart-range-tag {
  margin-right: var(--space-sm);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--app-text-primary);
}

.chart-wrapper {
  padding-top: var(--space-xs);
}

.trend-svg {
  width: 100%;
  height: 210px;
  display: block;
  overflow: visible;
}

.trend-svg line {
  stroke: var(--app-chart-grid);
}

.trend-svg text {
  fill: var(--app-chart-axis);
}

.chart-legend {
  display: flex;
  gap: var(--space-xl);
  justify-content: center;
  margin-top: var(--space-md);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  font-size: 12px;
  color: var(--app-text-secondary);
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot--blue { background: var(--app-color-primary); }
.legend-dot--red  { background: var(--app-color-danger); }

/* ─── To-Dos Card ───────────────────────────────────────────────────────────── */
.todos-card {
  border-radius: var(--app-radius-sm);
  border: 1px solid var(--app-border-soft);
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
}

.section-loading {
  padding: var(--space-lg) 0;
  text-align: center;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--space-md);
  background: var(--app-bg-muted);
  border: 1px solid var(--app-border-soft);
  border-radius: var(--el-border-radius-small);
  transition: background 0.15s, border-color 0.15s;
}

.todo-item:hover {
  background: var(--app-color-primary-soft);
  border-color: var(--app-border-strong);
}

.todo-item--overdue {
  background: var(--app-todo-overdue-bg);
  border-color: var(--app-todo-overdue-border);
}

.todo-item--overdue:hover {
  background: var(--app-todo-overdue-hover-bg);
}

.todo-left {
  display: flex;
  align-items: flex-start;
  gap: var(--space-md);
  min-width: 0;
}

.todo-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: var(--space-sm);
}

.dot--green  { background: var(--app-color-success); }
.dot--orange { background: var(--app-color-warning); }
.dot--grey   { background: var(--app-dot-muted); }

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--app-text-primary);
  margin-bottom: var(--space-xs);
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 380px;
}

.todo-date-icon {
  vertical-align: -2px;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.todo-date {
  font-size: 12px;
  color: var(--app-text-muted);
  display: inline-flex;
  align-items: center;
  gap: var(--space-xs);
}

/* ─── Activity Card ─────────────────────────────────────────────────────────── */
.activity-card {
  border-radius: var(--app-radius-sm);
  border: 1px solid var(--app-border-soft);
}

.activity-timeline {
  padding-top: var(--space-xs);
}

/* Override el-timeline default padding */
:deep(.el-timeline-item__wrapper) {
  padding-left: var(--space-lg);
}

.activity-item {
  font-size: 13px;
  line-height: 1.6;
  color: var(--app-text-secondary);
}

.activity-actor {
  font-weight: 600;
  color: var(--app-text-primary);
  margin-right: var(--space-xs);
}

.activity-action {
  color: var(--app-text-secondary);
  margin-right: var(--space-xs);
}

.activity-target {
  color: var(--app-color-primary);
  font-weight: 500;
}

@media (max-width: 1919px) {
  .project-name {
    font-size: 20px;
  }

  .kpi-value {
    font-size: 34px;
  }
}

@media (max-width: 1439px) {
  .header-inner {
    gap: var(--space-lg);
  }

  .meta-row {
    row-gap: var(--space-sm);
  }

  .meta-item {
    padding: 0 var(--space-sm);
  }

  .kpi-row :deep(.el-col) {
    flex: 0 0 50%;
    max-width: 50%;
  }

  .content-row :deep(.el-col) {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .todo-title {
    max-width: none;
  }
}

@media (max-width: 1365px) {
  .header-inner {
    flex-direction: column;
    align-items: stretch;
  }

  .meta-row {
    gap: var(--space-sm);
  }

  .meta-item {
    flex: 1 1 calc(50% - var(--space-sm));
    min-width: 220px;
    padding: 0;
  }

  .meta-divider {
    display: none;
  }

  .content-row {
    margin-bottom: var(--space-md);
  }

  .kpi-row :deep(.el-col) {
    flex: 0 0 100%;
    max-width: 100%;
  }

  .chart-legend {
    gap: var(--space-md);
  }
}

/* ─── Utilities ─────────────────────────────────────────────────────────────── */
.text-danger  { color: var(--app-color-danger) !important; }
.text-success { color: var(--app-color-success); }
.text-muted   { color: var(--app-text-muted); }
</style>
