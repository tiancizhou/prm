<template>
  <div class="app-page task-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ taskText.pageTitle }}</h1>
        <p class="page-subtitle">{{ taskText.pageSubtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button v-if="isManager" type="primary" :icon="Plus" @click="openCreate(null)">{{ taskText.buttons.newTask }}</el-button>
      </div>
    </header>

    <el-card class="surface-card task-surface" shadow="never">
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <el-radio-group v-model="viewMode" size="small" :aria-label="taskText.aria.viewMode">
              <el-radio-button value="board">{{ taskText.viewLabels.board }}</el-radio-button>
              <el-radio-button value="list">{{ taskText.viewLabels.list }}</el-radio-button>
            </el-radio-group>
            <el-button
              v-if="isManager"
              :type="onlyMine ? 'primary' : 'default'"
              size="small"
              :icon="onlyMine ? UserFilled : User"
              @click="toggleOnlyMine"
              class="only-mine-toggle"
            >
              {{ onlyMine ? taskText.buttons.myTasks : taskText.buttons.allTasks }}
            </el-button>
            <el-tag v-else type="primary" size="small" class="only-mine-tag">{{ taskText.buttons.myTasks }}</el-tag>
          </div>
          <el-select
            v-model="filterReqId"
            :aria-label="taskText.aria.filterRequirement"
            :placeholder="taskText.placeholders.filterRequirement"
            clearable
            size="small"
            class="filter-req-select"
            @change="load"
          >
            <el-option v-for="req in requirementOptions" :key="req.id" :label="req.title" :value="req.id" />
          </el-select>
        </div>
      </template>

      <div v-if="viewMode === 'board'" class="kanban">
        <div v-for="col in columns" :key="col.status" class="kanban-col" :class="`status-${col.status.toLowerCase()}`">
          <div class="kanban-col-header">
            <span>{{ col.label }}</span>
            <el-badge :value="getTasksByStatus(col.status).length" :type="col.badgeType" />
          </div>
          <div class="kanban-col-body">
            <el-card v-for="task in getTasksByStatus(col.status)" :key="task.id" class="task-card" shadow="hover">
              <el-tag
                v-if="task.requirementTitle"
                size="small"
                type="success"
                class="req-tag"
                :title="task.requirementTitle"
              >
                {{ taskText.labels.requirementPrefix }}: {{ task.requirementTitle }}
              </el-tag>
              <div class="task-title">{{ task.title }}</div>
              <div class="task-meta">
                <el-tag size="small" :type="priorityType(task.priority)">{{ priorityLabel(task.priority) }}</el-tag>
                <el-select
                  v-if="isManager"
                  :model-value="task.assigneeId"
                  :placeholder="taskText.placeholders.assign"
                  size="small"
                  clearable
                  class="assign-select"
                  @change="(val: number | undefined) => assignTask(task, val ?? null)"
                  @click.stop
                >
                  <el-option v-for="m in memberOptions" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
                </el-select>
                <span v-else class="assignee">{{ task.assigneeName || taskText.placeholders.unassigned }}</span>
              </div>
              <div v-if="task.dueDate" class="task-due">
                <el-icon><Calendar /></el-icon>
                {{ task.dueDate }}
              </div>
              <div v-if="canOperateTask(task)" class="task-actions">
                <el-dropdown @click.stop @command="(cmd: string) => handleBoardCommand(task, cmd)" size="small">
                  <el-button size="small" link type="primary">{{ taskText.buttons.actions }} <el-icon><ArrowDown /></el-icon></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item v-for="opt in statusActionOptions" :key="opt.value" :command="`status:${opt.value}`">
                        {{ taskText.statusActionPrefix }} {{ opt.label }}
                      </el-dropdown-item>
                      <el-dropdown-item divided command="worklog">{{ taskText.buttons.logWork }}</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </el-card>
          </div>
        </div>
      </div>

      <el-table v-else :data="list" v-loading="loading" class="task-table">
        <el-table-column prop="title" :label="taskText.labels.title" min-width="180" />
        <el-table-column :label="taskText.labels.requirement" min-width="140">
          <template #default="{ row }">
            <el-tag v-if="row.requirementTitle" size="small" type="success">{{ row.requirementTitle }}</el-tag>
            <span v-else class="text-muted">{{ taskText.labels.noneSymbol }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="taskText.labels.type" width="120">
          <template #default="{ row }">
            <span>{{ taskTypeLabel(row.type) }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="taskText.labels.status" width="140">
          <template #default="{ row }">
            <el-tag size="small" :type="taskStatusType(row.status)">{{ taskStatusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="taskText.labels.assignee" width="180">
          <template #default="{ row }">
            <el-select
              v-if="isManager"
              :model-value="row.assigneeId"
              :placeholder="taskText.placeholders.assign"
              size="small"
              clearable
              class="assignee-select-cell"
              @change="(val: number | undefined) => assignTask(row, val ?? null)"
            >
              <el-option v-for="m in memberOptions" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
            </el-select>
            <span v-else class="assignee">{{ row.assigneeName || taskText.labels.noneSymbol }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="taskText.labels.hours" width="180">
          <template #default="{ row }">
            <span class="hours">{{ taskText.labels.estimatedPrefix }} {{ row.estimatedHours }}h / {{ taskText.labels.spentPrefix }} {{ row.spentHours }}h</span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" :label="taskText.labels.dueDate" width="140">
          <template #default="{ row }">
            <span>{{ row.dueDate || taskText.labels.noneSymbol }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="taskText.labels.actions" width="180" fixed="right">
          <template #default="{ row }">
            <template v-if="canOperateTask(row)">
              <el-dropdown @command="(cmd: string) => changeStatus(row, cmd)">
                <el-button size="small">{{ taskText.buttons.status }} <el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="opt in statusActionOptions" :key="opt.value" :command="opt.value">{{ opt.label }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button size="small" @click="openWorklog(row)">{{ taskText.buttons.logWork }}</el-button>
            </template>
            <span v-else class="text-muted">{{ taskText.labels.noneSymbol }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showWorklog" :title="taskText.dialogs.worklog" width="400px">
      <el-form :model="worklogForm" label-width="88px">
        <el-form-item :label="taskText.labels.task">
          <el-input :model-value="worklogTask?.title" name="worklogTaskTitle" autocomplete="off" disabled />
        </el-form-item>
        <el-form-item :label="taskText.labels.spentHours" required>
          <el-input-number v-model="worklogForm.spentHours" :min="0" :precision="1" />
          <span class="worklog-unit">{{ taskText.units.hour }}</span>
        </el-form-item>
        <el-form-item :label="taskText.labels.remark">
          <el-input v-model="worklogForm.remark" name="taskWorklogRemark" autocomplete="off" type="textarea" :rows="2" :placeholder="taskText.placeholders.optional" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWorklog = false">{{ taskText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="worklogSubmitting" @click="submitWorklog">{{ taskText.buttons.confirm }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCreate" :title="taskText.dialogs.createTask" width="560px">
      <el-form :model="form" label-width="92px">
        <el-form-item :label="taskText.labels.relatedRequirement">
          <el-select v-model="form.requirementId" :placeholder="taskText.placeholders.selectRequirement" clearable class="form-full-width">
            <el-option v-for="req in requirementOptions" :key="req.id" :label="req.title" :value="req.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="taskText.labels.taskTitle" required>
          <el-input v-model="form.title" name="taskTitle" autocomplete="off" :placeholder="taskText.placeholders.taskTitle" />
        </el-form-item>
        <el-form-item :label="taskText.labels.taskType">
          <el-select v-model="form.type">
            <el-option v-for="opt in taskTypeOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="taskText.labels.priority">
          <el-select v-model="form.priority">
            <el-option v-for="opt in taskPriorityOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="taskText.labels.estimatedHours">
          <el-input-number v-model="form.estimatedHours" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item :label="taskText.labels.dueDate">
          <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" class="form-full-width" />
        </el-form-item>
        <el-form-item :label="taskText.labels.assignee">
          <el-select v-model="form.assigneeId" :placeholder="taskText.placeholders.selectAssignee" clearable class="form-full-width">
            <el-option v-for="m in memberOptions" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">{{ taskText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="creating" @click="create">{{ taskText.buttons.create }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowDown, Calendar, Plus, User, UserFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { projectApi } from '@/api/project'
import { requirementApi } from '@/api/requirement'
import { taskApi } from '@/api/task'
import { TASK_PAGE_I18N } from '@/constants/task'
import { resolveThemeLocale } from '@/constants/theme'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const authStore = useAuthStore()
const projectId = Number(route.params.id)
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const taskText = TASK_PAGE_I18N[currentLocale]

const loading = ref(false)
const list = ref<any[]>([])
const viewMode = ref<'board' | 'list'>('board')
const filterReqId = ref<number | null>(null)
const requirementOptions = ref<any[]>([])
const memberOptions = ref<{ userId: number; nickname?: string; username?: string }[]>([])

const isManager = computed(() => {
  const roles: string[] = authStore.user?.roles || []
  return roles.includes('SUPER_ADMIN') || roles.includes('PROJECT_ADMIN')
})

const onlyMine = ref(!isManager.value)

const showWorklog = ref(false)
const worklogSubmitting = ref(false)
const worklogTask = ref<any>(null)
const worklogForm = reactive({ spentHours: 0.5, remark: '' })

const showCreate = ref(false)
const creating = ref(false)
const form = reactive({
  title: '',
  type: 'TASK' as 'TASK' | 'SUBTASK' | 'TECH',
  priority: 'MEDIUM' as 'LOW' | 'MEDIUM' | 'HIGH',
  estimatedHours: 0,
  dueDate: '',
  requirementId: null as number | null,
  assigneeId: null as number | null
})

const statusActionOptions = [...taskText.statusOptions]
const taskTypeOptions = [...taskText.taskTypeOptions]
const taskPriorityOptions = [...taskText.taskPriorityOptions]

const columns = statusActionOptions.map(opt => ({
  status: opt.value,
  label: opt.label,
  badgeType: ({ TODO: 'info', IN_PROGRESS: 'warning', PENDING_REVIEW: 'primary', DONE: 'success' } as const)[opt.value]
}))

type TagType = 'primary' | 'success' | 'warning' | 'info' | 'danger'
const priorityTypeMap: Record<string, TagType> = { LOW: 'info', MEDIUM: 'primary', HIGH: 'warning', CRITICAL: 'danger' }
const taskStatusTypeMap: Record<string, TagType> = { TODO: 'info', IN_PROGRESS: 'warning', PENDING_REVIEW: 'primary', DONE: 'success' }

function canOperateTask(task: { assigneeId?: number | null }) {
  if (isManager.value) return true
  const userId = authStore.user?.userId
  return userId != null && task.assigneeId != null && task.assigneeId === userId
}

function getTasksByStatus(status: string) {
  return list.value.filter(task => task.status === status)
}

function priorityType(priority: string): TagType {
  return priorityTypeMap[priority] || 'info'
}

function taskStatusType(status: string): TagType {
  return taskStatusTypeMap[status] || 'info'
}

function priorityLabel(priority: string) {
  return taskText.priorityLabels[priority] || priority || taskText.labels.noneSymbol
}

function taskStatusLabel(status: string) {
  return taskText.statusLabels[status] || status || taskText.labels.noneSymbol
}

function taskTypeLabel(type: string) {
  return taskText.taskTypeLabels[type] || type || taskText.labels.noneSymbol
}

function toggleOnlyMine() {
  if (!isManager.value) return
  onlyMine.value = !onlyMine.value
  load()
}

function handleBoardCommand(task: any, command: string) {
  if (command === 'worklog') {
    openWorklog(task)
    return
  }
  if (command.startsWith('status:')) {
    void changeStatus(task, command.replace('status:', ''))
  }
}

async function load() {
  loading.value = true
  try {
    const params: any = { projectId, page: 1, size: 200 }
    if (filterReqId.value) params.requirementId = filterReqId.value
    if (!isManager.value && authStore.user?.userId) {
      params.assigneeId = authStore.user.userId
    } else if (onlyMine.value && authStore.user?.userId) {
      params.assigneeId = authStore.user.userId
    }
    const response = await taskApi.list(params)
    list.value = (response as any).data?.records || []
  } finally {
    loading.value = false
  }
}

async function loadRequirements() {
  try {
    const response = await requirementApi.list({ projectId, page: 1, size: 200 })
    requirementOptions.value = (response as any).data?.records || []
  } catch {
    requirementOptions.value = []
  }
}

async function loadMembers() {
  try {
    const response = await projectApi.getMembers(projectId)
    memberOptions.value = (response as any).data || []
  } catch {
    memberOptions.value = []
  }
}

async function assignTask(row: any, assigneeId: number | null) {
  try {
    await taskApi.assign(row.id, assigneeId)
    row.assigneeId = assigneeId ?? undefined
    const matchedMember = memberOptions.value.find(member => member.userId === assigneeId)
    row.assigneeName = assigneeId == null ? null : (matchedMember?.nickname || matchedMember?.username)
    ElMessage.success(assigneeId != null ? taskText.messages.assigned : taskText.messages.unassigned)
  } catch {
    await load()
  }
}

function openCreate(requirementId: number | null) {
  form.title = ''
  form.type = 'TASK'
  form.priority = 'MEDIUM'
  form.estimatedHours = 0
  form.dueDate = ''
  form.requirementId = requirementId
  form.assigneeId = null
  showCreate.value = true
}

async function create() {
  if (!form.title.trim()) {
    ElMessage.warning(taskText.messages.titleRequired)
    return
  }
  creating.value = true
  try {
    await taskApi.create({ ...form, projectId })
    ElMessage.success(taskText.messages.createSuccess)
    showCreate.value = false
    await load()
  } finally {
    creating.value = false
  }
}

async function changeStatus(row: any, status: string) {
  try {
    await taskApi.updateStatus(row.id, status)
    ElMessage.success(taskText.messages.statusUpdated)
    await load()
  } catch {
    await load()
  }
}

function openWorklog(task: any) {
  worklogTask.value = task
  worklogForm.spentHours = 0.5
  worklogForm.remark = ''
  showWorklog.value = true
}

async function submitWorklog() {
  if (!worklogTask.value) return
  worklogSubmitting.value = true
  try {
    await taskApi.logWork(worklogTask.value.id, worklogForm.spentHours, worklogForm.remark || undefined)
    ElMessage.success(taskText.messages.worklogSuccess)
    showWorklog.value = false
    await load()
  } finally {
    worklogSubmitting.value = false
  }
}

onMounted(() => {
  void load()
  void loadRequirements()
  void loadMembers()
})
</script>

<style scoped>
.task-page {
  min-width: 0;
}

.title-block {
  display: flex;
  flex-direction: column;
  position: relative;
  padding-left: var(--space-md);
}

.title-block::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  border-radius: var(--app-radius-pill);
  background: linear-gradient(180deg, var(--app-color-primary), var(--app-color-accent));
}

.task-surface {
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.task-surface:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 28%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.card-header-left {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.only-mine-toggle,
.only-mine-tag {
  margin-left: 0;
}

.filter-req-select {
  width: 220px;
}

.kanban {
  display: flex;
  gap: var(--space-lg);
  overflow-x: auto;
  padding-bottom: var(--space-sm);
}

.kanban-col {
  flex: 1;
  min-width: 280px;
  background: var(--app-bg-muted);
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius-sm);
  padding: var(--space-md);
}

.kanban-col.status-todo {
  border-top: 3px solid color-mix(in srgb, var(--el-color-info) 70%, transparent);
}

.kanban-col.status-in_progress {
  border-top: 3px solid color-mix(in srgb, var(--el-color-warning) 70%, transparent);
}

.kanban-col.status-pending_review {
  border-top: 3px solid color-mix(in srgb, var(--app-color-primary) 70%, transparent);
}

.kanban-col.status-done {
  border-top: 3px solid color-mix(in srgb, var(--el-color-success) 70%, transparent);
}

.kanban-col-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-md);
  font-weight: 600;
  color: var(--app-text-primary);
}

.kanban-col-body {
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
  min-height: 80px;
}

.task-card {
  cursor: pointer;
  border: 1px solid var(--app-border-soft);
  transition: transform 0.18s ease, border-color 0.18s ease;
}

.task-card:hover {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--app-color-primary) 35%, var(--app-border-soft));
}

.req-tag {
  display: block;
  margin-bottom: var(--space-sm);
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-title {
  font-size: 13px;
  color: var(--app-text-primary);
  margin-bottom: var(--space-sm);
}

.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-sm);
}

.task-due {
  margin-top: var(--space-sm);
  font-size: 12px;
  color: var(--app-text-muted);
  display: flex;
  align-items: center;
  gap: var(--space-xs);
}

.assign-select {
  width: 110px;
  font-size: 12px;
}

.assignee-select-cell {
  width: 140px;
}

.assignee {
  font-size: 12px;
  color: var(--app-text-muted);
}

.hours {
  font-size: 12px;
  color: var(--app-text-secondary);
}

.text-muted {
  color: var(--app-text-muted);
}

.task-actions {
  margin-top: var(--space-sm);
  padding-top: var(--space-sm);
  border-top: 1px solid var(--app-border-soft);
}

.task-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.task-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.worklog-unit {
  margin-left: var(--space-sm);
}

.form-full-width {
  width: 100%;
}
</style>
