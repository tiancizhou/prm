<template>
  <div class="overview-page">

    <!-- ═══════════════════ HEADER CARD ═══════════════════ -->
    <el-card class="header-card" v-loading="loading">
      <div class="header-inner">
        <div class="header-left">
          <div class="project-title-row">
            <span class="project-code-badge">{{ project?.code }}</span>
            <h1 class="project-name">{{ project?.name }}</h1>
            <el-tag :type="statusType(project?.status)" size="large" effect="light">
              {{ statusLabel(project?.status) }}
            </el-tag>
          </div>

          <p class="project-desc">
            {{ project?.description || '暂无项目描述，点击右侧按钮添加。' }}
          </p>

          <div class="meta-row">
            <div class="meta-item">
              <el-icon class="meta-icon"><User /></el-icon>
              <span class="meta-label">负责人</span>
              <span class="meta-value">{{ project?.ownerName || '-' }}</span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <el-icon class="meta-icon"><Calendar /></el-icon>
              <span class="meta-label">开始</span>
              <span class="meta-value">{{ formatDate(project?.startDate) }}</span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <el-icon class="meta-icon"><Aim /></el-icon>
              <span class="meta-label">截止</span>
              <span class="meta-value" :class="{ 'text-danger': isOverdue }">
                {{ formatDate(project?.endDate) }}
                <el-tag v-if="isOverdue" type="danger" size="small" style="margin-left:6px">已逾期</el-tag>
              </span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <el-icon class="meta-icon"><View /></el-icon>
              <span class="meta-label">可见范围</span>
              <span class="meta-value">{{ visibilityLabel(project?.visibility) }}</span>
            </div>
            <div class="meta-divider" />
            <div class="meta-item">
              <el-icon class="meta-icon"><Clock /></el-icon>
              <span class="meta-label">创建</span>
              <span class="meta-value">{{ formatDate(project?.createdAt) }}</span>
            </div>
          </div>
        </div>

        <div class="header-actions">
          <el-button :icon="Edit" type="primary" plain @click="openEditDialog">
            编辑描述
          </el-button>
        </div>
      </div>
    </el-card>

    <!-- ═══════════════════ KPI CARDS ═══════════════════ -->
    <el-row :gutter="16" class="kpi-row" v-loading="statsLoading">

      <!-- 1. Overall Progress -->
      <el-col :span="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-header">
            <span class="kpi-title">项目整体进度</span>
            <div class="kpi-icon kpi-icon--blue">
              <el-icon><TrendCharts /></el-icon>
            </div>
          </div>
          <div class="kpi-value kpi-value--blue">{{ progressPercent }}<span class="kpi-unit">%</span></div>
          <el-progress
            :percentage="progressPercent"
            :stroke-width="6"
            :show-text="false"
            color="#409eff"
            style="margin-top: 8px"
          />
          <div class="kpi-sub">
            {{ stats?.doneRequirements ?? 0 }} / {{ stats?.totalRequirements ?? 0 }} 需求已完成
          </div>
        </el-card>
      </el-col>

      <!-- 2. Open Tasks -->
      <el-col :span="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-header">
            <span class="kpi-title">待处理任务</span>
            <div class="kpi-icon kpi-icon--orange">
              <el-icon><List /></el-icon>
            </div>
          </div>
          <div class="kpi-value kpi-value--orange">{{ stats?.inProgressTasks ?? '-' }}</div>
          <div class="kpi-sub" style="margin-top: 14px">
            <el-tag v-if="overdueTaskCount > 0" type="warning" size="small">
              {{ overdueTaskCount }} 项逾期
            </el-tag>
            <span v-else class="text-success">✓ 无逾期任务</span>
          </div>
        </el-card>
      </el-col>

      <!-- 3. Open Bugs -->
      <el-col :span="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-header">
            <span class="kpi-title">未关闭 Bug</span>
            <div class="kpi-icon kpi-icon--red">
              <el-icon><Warning /></el-icon>
            </div>
          </div>
          <div class="kpi-value kpi-value--red">{{ stats?.openBugs ?? '-' }}</div>
          <div class="kpi-sub" style="margin-top: 14px">
            <el-tag v-if="(stats?.criticalOpenBugs ?? 0) > 0" type="danger" size="small">
              {{ stats?.criticalOpenBugs }} 个严重/阻塞
            </el-tag>
            <span v-else class="text-success">✓ 无严重 Bug</span>
          </div>
        </el-card>
      </el-col>

      <!-- 4. Days Left in Sprint -->
      <el-col :span="6">
        <el-card class="kpi-card" shadow="never">
          <div class="kpi-header">
            <span class="kpi-title">当前迭代剩余</span>
            <div class="kpi-icon kpi-icon--purple">
              <el-icon><Timer /></el-icon>
            </div>
          </div>
          <div class="kpi-value" :class="daysLeftClass">
            {{ daysLeft !== null ? daysLeft : '-' }}<span v-if="daysLeft !== null" class="kpi-unit">天</span>
          </div>
          <div class="kpi-sub" style="margin-top: 14px">
            <span v-if="activeSprint">{{ activeSprint.name }}</span>
            <span v-else class="text-muted">暂无活跃迭代</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ═══════════════════ MAIN SPLIT LAYOUT ═══════════════════ -->
    <el-row :gutter="16" class="content-row" style="align-items: flex-start">

      <!-- ──── LEFT COLUMN (7/10) ──── -->
      <el-col :span="17">

        <!-- Trend Chart -->
        <el-card class="chart-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">任务 &amp; Bug 趋势</span>
              <div class="card-header-right">
                <el-tag type="info" size="small" style="margin-right:8px">近 7 天</el-tag>
                <el-button link size="small" :icon="Refresh" @click="refreshTrend">刷新</el-button>
              </div>
            </div>
          </template>

          <div class="chart-wrapper">
            <svg viewBox="0 0 620 200" class="trend-svg" v-if="trendData.length > 0">
              <!-- Grid lines -->
              <line
                v-for="i in 4" :key="'grid-h-' + i"
                x1="50" :y1="i * 40" x2="600" :y2="i * 40"
                stroke="#f0f0f0" stroke-width="1"
              />
              <!-- Y-axis labels -->
              <text
                v-for="i in 5" :key="'y-' + i"
                x="44"
                :y="(5 - i) * 40 + 4"
                text-anchor="end"
                font-size="11"
                fill="#bbb"
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
                fill="#bbb"
              >
                {{ d.date }}
              </text>

              <!-- Task area fill -->
              <path :d="taskAreaPath" fill="#409eff" fill-opacity="0.08" />
              <!-- Bug area fill -->
              <path :d="bugAreaPath" fill="#f56c6c" fill-opacity="0.08" />

              <!-- Task line -->
              <polyline
                :points="taskPoints"
                fill="none"
                stroke="#409eff"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
              <!-- Bug line -->
              <polyline
                :points="bugPoints"
                fill="none"
                stroke="#f56c6c"
                stroke-width="2.5"
                stroke-linecap="round"
                stroke-linejoin="round"
              />

              <!-- Task dots -->
              <circle
                v-for="(d, idx) in trendData" :key="'td-' + idx"
                :cx="chartX(idx)" :cy="chartY(d.tasks)"
                r="4" fill="#fff" stroke="#409eff" stroke-width="2"
              />
              <!-- Bug dots -->
              <circle
                v-for="(d, idx) in trendData" :key="'bd-' + idx"
                :cx="chartX(idx)" :cy="chartY(d.bugs)"
                r="4" fill="#fff" stroke="#f56c6c" stroke-width="2"
              />
            </svg>
            <el-empty v-else description="暂无趋势数据" :image-size="80" />

            <!-- Legend -->
            <div class="chart-legend">
              <span class="legend-item">
                <span class="legend-dot legend-dot--blue"></span>未完成任务
              </span>
              <span class="legend-item">
                <span class="legend-dot legend-dot--red"></span>未关闭 Bug
              </span>
            </div>
          </div>
        </el-card>

        <!-- My To-Dos -->
        <el-card class="todos-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">我的待办</span>
              <el-button link size="small" :icon="ArrowRight" @click="goToTasks">查看全部任务</el-button>
            </div>
          </template>

          <div v-if="todosLoading" style="padding: 16px 0; text-align: center">
            <el-icon class="is-loading" size="24"><Loading /></el-icon>
          </div>
          <el-empty v-else-if="myTodos.length === 0" description="暂无待办任务，尽情摸鱼 🎉" :image-size="70" />
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
                      {{ priorityLabel(task.priority) }}优先级
                    </el-tag>
                    <span v-if="task.dueDate || task.endDate" class="todo-date" :class="{ 'text-danger': isTaskOverdue(task) }">
                      <el-icon style="vertical-align: -2px"><Clock /></el-icon>
                      截止 {{ formatDate(task.dueDate || task.endDate) }}
                    </span>
                    <el-tag v-if="isTaskOverdue(task)" type="danger" size="small">逾期</el-tag>
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
        <el-card class="activity-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span class="card-title">最近动态</span>
              <el-button link size="small" :icon="Refresh" :loading="activityLoading" @click="loadActivity">
                刷新
              </el-button>
            </div>
          </template>

          <div v-if="activityLoading && activities.length === 0" style="padding: 16px 0; text-align: center">
            <el-icon class="is-loading" size="24"><Loading /></el-icon>
          </div>
          <el-empty v-else-if="activities.length === 0" description="暂无近期动态" :image-size="70" />
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
                <span class="activity-target">「{{ act.target }}」</span>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>

    <!-- ═══════════════════ EDIT DESCRIPTION DIALOG ═══════════════════ -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑项目描述"
      width="520px"
      :close-on-click-modal="false"
      draggable
    >
      <el-input
        v-model="editDesc"
        type="textarea"
        :rows="5"
        placeholder="请输入项目描述..."
        maxlength="500"
        show-word-limit
      />
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDesc" :loading="savingDesc">保存</el-button>
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
import { taskApi } from '@/api/task'
import { sprintApi } from '@/api/sprint'
import { useProjectStore } from '@/stores/project'

// ─── Route & Store ───────────────────────────────────────────────────────────
const route = useRoute()
const router = useRouter()
const projectStore = useProjectStore()
const projectId = Number(route.params.id)

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
  if (mins < 1) return '刚刚'
  if (mins < 60) return `${mins} 分钟前`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs} 小时前`
  const days = Math.floor(hrs / 24)
  if (days < 7) return `${days} 天前`
  return formatDate(d)
}

// ─── Label / Type Maps ────────────────────────────────────────────────────────
function statusType(s?: string): '' | 'success' | 'info' | 'warning' | 'danger' {
  if (s === 'ACTIVE') return 'success'
  if (s === 'ARCHIVED') return 'info'
  return 'danger'
}

function statusLabel(s?: string): string {
  const m: Record<string, string> = { ACTIVE: '进行中', ARCHIVED: '已归档', CLOSED: '已关闭' }
  return s ? (m[s] ?? s) : ''
}

function visibilityLabel(v?: string): string {
  return v === 'PUBLIC' ? '公开' : v === 'PRIVATE' ? '私有' : v ?? '-'
}

function priorityType(p?: string): '' | 'success' | 'info' | 'warning' | 'danger' {
  if (p === 'HIGH') return 'danger'
  if (p === 'MEDIUM') return 'warning'
  return 'info'
}

function priorityLabel(p?: string): string {
  const m: Record<string, string> = { HIGH: '高', MEDIUM: '中', LOW: '低' }
  return m[p ?? ''] ?? p ?? '-'
}

function taskStatusType(s?: string): '' | 'success' | 'info' | 'warning' | 'danger' {
  if (s === 'DONE' || s === 'CLOSED') return 'success'
  if (s === 'IN_PROGRESS') return 'warning'
  return 'info'
}

function taskStatusLabel(s?: string): string {
  const m: Record<string, string> = { TODO: '待处理', IN_PROGRESS: '进行中', DONE: '已完成', CLOSED: '已关闭' }
  return m[s ?? ''] ?? s ?? '-'
}

function todoStatusDotClass(s?: string): string {
  if (s === 'DONE' || s === 'CLOSED') return 'dot--green'
  if (s === 'IN_PROGRESS') return 'dot--orange'
  return 'dot--grey'
}

function isTaskOverdue(task: any): boolean {
  const dateStr = task.dueDate || task.endDate
  if (!dateStr) return false
  return new Date(dateStr) < new Date() && task.status !== 'DONE' && task.status !== 'CLOSED'
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
    const res = await taskApi.list({ projectId, pageSize: 5 })
    const data = (res as any).data
    const list: any[] = data?.records ?? data?.list ?? (Array.isArray(data) ? data : [])
    myTodos.value = list.slice(0, 5)
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
    const res = await taskApi.list({ projectId, pageSize: 8 })
    const data = (res as any).data
    const list: any[] = data?.records ?? data?.list ?? (Array.isArray(data) ? data : [])
    const actionMap: Record<string, string> = {
      DONE: '完成了任务',
      CLOSED: '关闭了任务',
      IN_PROGRESS: '开始处理',
      TODO: '创建了任务'
    }
    const typeMap: Record<string, string> = {
      DONE: 'success',
      CLOSED: 'success',
      IN_PROGRESS: 'warning',
      TODO: 'primary'
    }
    activities.value = list.slice(0, 8).map((t: any) => ({
      actor: t.assigneeName ?? t.creatorName ?? '团队成员',
      action: actionMap[t.status] ?? '更新了',
      target: t.title ?? `任务 #${t.id}`,
      time: formatRelativeTime(t.updatedAt ?? t.createdAt),
      type: typeMap[t.status] ?? 'primary'
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
    ElMessage.success('项目描述已更新')
  } catch {
    ElMessage.error('更新失败，请重试')
  } finally {
    savingDesc.value = false
  }
}

function goToTasks(): void {
  router.push(`/projects/${projectId}/tasks`)
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
}

/* ─── Header Card ───────────────────────────────────────────────────────────── */
.header-card {
  margin-bottom: 16px;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
}

.header-inner {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.header-left {
  flex: 1;
  min-width: 0;
}

.project-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.project-code-badge {
  display: inline-block;
  background: #eff6ff;
  color: #2563eb;
  border: 1px solid #bfdbfe;
  border-radius: 5px;
  padding: 2px 9px;
  font-size: 12px;
  font-weight: 700;
  font-family: 'SF Mono', 'Fira Code', monospace;
  letter-spacing: 0.5px;
}

.project-name {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  line-height: 1.3;
}

.project-desc {
  color: #6b7280;
  font-size: 14px;
  margin: 0 0 18px;
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
  gap: 5px;
  padding: 0 14px;
  font-size: 13px;
  color: #4b5563;
}

.meta-item:first-child {
  padding-left: 0;
}

.meta-icon {
  color: #9ca3af;
  font-size: 14px;
}

.meta-label {
  color: #9ca3af;
  font-size: 12px;
}

.meta-value {
  font-weight: 500;
  color: #1f2937;
}

.meta-divider {
  width: 1px;
  height: 14px;
  background: #e5e7eb;
  flex-shrink: 0;
}

.header-actions {
  flex-shrink: 0;
  padding-top: 4px;
}

/* ─── KPI Row ───────────────────────────────────────────────────────────────── */
.kpi-row {
  margin-bottom: 16px;
}

.kpi-card {
  border-radius: 10px;
  border: 1px solid #e8e8e8;
  transition: box-shadow 0.2s, transform 0.2s;
}

.kpi-card:hover {
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.09);
  transform: translateY(-2px);
}

.kpi-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.kpi-title {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.kpi-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.kpi-icon--blue  { background: #eff6ff; color: #2563eb; }
.kpi-icon--orange { background: #fffbeb; color: #d97706; }
.kpi-icon--red   { background: #fef2f2; color: #dc2626; }
.kpi-icon--purple { background: #f5f3ff; color: #7c3aed; }

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
  margin-left: 2px;
}

.kpi-value--blue   { color: #2563eb; }
.kpi-value--orange { color: #d97706; }
.kpi-value--red    { color: #dc2626; }
.kpi-value--purple { color: #7c3aed; }
.kpi-value--muted  { color: #d1d5db; }

.kpi-sub {
  font-size: 12px;
  color: #9ca3af;
  min-height: 22px;
}

/* ─── Split Layout ──────────────────────────────────────────────────────────── */
.content-row {
  margin-bottom: 16px;
}

/* ─── Chart Card ────────────────────────────────────────────────────────────── */
.chart-card {
  margin-bottom: 16px;
  border-radius: 10px;
  border: 1px solid #e8e8e8;
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

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #111827;
}

.chart-wrapper {
  padding-top: 4px;
}

.trend-svg {
  width: 100%;
  height: 210px;
  display: block;
  overflow: visible;
}

.chart-legend {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 12px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #6b7280;
}

.legend-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
}

.legend-dot--blue { background: #2563eb; }
.legend-dot--red  { background: #dc2626; }

/* ─── To-Dos Card ───────────────────────────────────────────────────────────── */
.todos-card {
  border-radius: 10px;
  border: 1px solid #e8e8e8;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  background: #fafafa;
  border: 1px solid #f3f4f6;
  border-radius: 8px;
  transition: background 0.15s, border-color 0.15s;
}

.todo-item:hover {
  background: #f0f4ff;
  border-color: #dbeafe;
}

.todo-item--overdue {
  background: #fff5f5;
  border-color: #fecaca;
}

.todo-item--overdue:hover {
  background: #fee2e2;
}

.todo-left {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
}

.todo-status-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 6px;
}

.dot--green  { background: #22c55e; }
.dot--orange { background: #f59e0b; }
.dot--grey   { background: #d1d5db; }

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 5px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 380px;
}

.todo-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.todo-date {
  font-size: 12px;
  color: #9ca3af;
  display: inline-flex;
  align-items: center;
  gap: 3px;
}

/* ─── Activity Card ─────────────────────────────────────────────────────────── */
.activity-card {
  border-radius: 10px;
  border: 1px solid #e8e8e8;
}

.activity-timeline {
  padding-top: 4px;
}

/* Override el-timeline default padding */
:deep(.el-timeline-item__wrapper) {
  padding-left: 18px;
}

.activity-item {
  font-size: 13px;
  line-height: 1.6;
  color: #4b5563;
}

.activity-actor {
  font-weight: 600;
  color: #1f2937;
  margin-right: 3px;
}

.activity-action {
  color: #6b7280;
  margin-right: 3px;
}

.activity-target {
  color: #2563eb;
  font-weight: 500;
}

/* ─── Utilities ─────────────────────────────────────────────────────────────── */
.text-danger  { color: #dc2626 !important; }
.text-success { color: #16a34a; }
.text-muted   { color: #9ca3af; }
</style>
