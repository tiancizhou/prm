<template>
  <div>
    <div class="page-header">
      <h2>任务管理</h2>
      <el-button v-if="isManager" type="primary" icon="Plus" @click="openCreate(null)">新建任务</el-button>
    </div>

    <el-card>
      <template #header>
        <div class="card-header">
          <div class="card-header-left">
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button value="board">看板</el-radio-button>
              <el-radio-button value="list">列表</el-radio-button>
            </el-radio-group>
            <!-- 管理者可切换；非管理者固定为"我的任务" -->
            <el-button
              v-if="isManager"
              :type="onlyMine ? 'primary' : 'default'"
              size="small"
              :icon="onlyMine ? 'UserFilled' : 'User'"
              @click="toggleOnlyMine"
              style="margin-left: 8px"
            >
              {{ onlyMine ? '我的任务' : '全部任务' }}
            </el-button>
            <el-tag v-else type="primary" size="small" style="margin-left: 8px">我的任务</el-tag>
          </div>
          <!-- 需求筛选 -->
          <el-select
            v-model="filterReqId"
            placeholder="按需求筛选"
            clearable
            size="small"
            style="width: 200px"
            @change="load"
          >
            <el-option
              v-for="req in requirementOptions"
              :key="req.id"
              :label="req.title"
              :value="req.id"
            />
          </el-select>
        </div>
      </template>

      <!-- 看板 -->
      <div v-if="viewMode === 'board'" class="kanban">
        <div v-for="col in columns" :key="col.status" class="kanban-col">
          <div class="kanban-col-header">
            <span>{{ col.label }}</span>
            <el-badge :value="getTasksByStatus(col.status).length" :type="col.badgeType" />
          </div>
          <div class="kanban-col-body">
            <el-card
              v-for="task in getTasksByStatus(col.status)"
              :key="task.id"
              class="task-card"
              shadow="hover"
            >
              <!-- 所属需求标签 -->
              <el-tag
                v-if="task.requirementTitle"
                size="small"
                type="success"
                class="req-tag"
                :title="task.requirementTitle"
              >
                需求: {{ task.requirementTitle }}
              </el-tag>
              <div class="task-title">{{ task.title }}</div>
              <div class="task-meta">
                <el-tag size="small" :type="priorityType(task.priority)">{{ task.priority }}</el-tag>
                <el-select
                  v-if="isManager"
                  :model-value="task.assigneeId"
                  placeholder="指派"
                  size="small"
                  clearable
                  class="assign-select"
                  @change="(val: number | null) => assignTask(task, val)"
                  @click.stop
                >
                  <el-option label="未指派" :value="null" />
                  <el-option
                    v-for="m in memberOptions"
                    :key="m.userId"
                    :label="m.nickname || m.username"
                    :value="m.userId"
                  />
                </el-select>
                <span v-else class="assignee">{{ task.assigneeName || '未指派' }}</span>
              </div>
              <div v-if="task.dueDate" class="task-due">
                <el-icon><Calendar /></el-icon> {{ task.dueDate }}
              </div>
              <!-- 开发/负责人可改状态、登记工时 -->
              <div v-if="canOperateTask(task)" class="task-actions">
                <el-dropdown @click.stop @command="(cmd: string) => cmd.startsWith('status:') ? changeStatus(task, cmd.replace('status:', '')) : openWorklog(task)" size="small">
                  <el-button size="small" link type="primary">操作 <el-icon><ArrowDown /></el-icon></el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="status:TODO">改为 待处理</el-dropdown-item>
                      <el-dropdown-item command="status:IN_PROGRESS">改为 进行中</el-dropdown-item>
                      <el-dropdown-item command="status:PENDING_REVIEW">改为 待验收</el-dropdown-item>
                      <el-dropdown-item command="status:DONE">改为 已完成</el-dropdown-item>
                      <el-dropdown-item divided command="worklog">登记工时</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </el-card>
          </div>
        </div>
      </div>

      <!-- 列表 -->
      <el-table v-else :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column label="所属需求" min-width="140">
          <template #default="{ row }">
            <el-tag v-if="row.requirementTitle" size="small" type="success">
              {{ row.requirementTitle }}
            </el-tag>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="80" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="taskStatusType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="负责人" width="180">
          <template #default="{ row }">
            <el-select
              v-if="isManager"
              :model-value="row.assigneeId"
              placeholder="指派"
              size="small"
              clearable
              style="width: 140px"
              @change="(val: number | null) => assignTask(row, val)"
            >
              <el-option label="未指派" :value="null" />
              <el-option
                v-for="m in memberOptions"
                :key="m.userId"
                :label="m.nickname || m.username"
                :value="m.userId"
              />
            </el-select>
            <span v-else class="assignee">{{ row.assigneeName || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="工时" width="140">
          <template #default="{ row }">
            <span class="hours">预估 {{ row.estimatedHours }}h / 已用 {{ row.spentHours }}h</span>
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="120" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <template v-if="canOperateTask(row)">
              <el-dropdown @command="(cmd: string) => changeStatus(row, cmd)" size="small" style="margin-right: 8px">
                <el-button size="small">状态 <el-icon><ArrowDown /></el-icon></el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="TODO">待处理</el-dropdown-item>
                    <el-dropdown-item command="IN_PROGRESS">进行中</el-dropdown-item>
                    <el-dropdown-item command="PENDING_REVIEW">待验收</el-dropdown-item>
                    <el-dropdown-item command="DONE">已完成</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button size="small" @click="openWorklog(row)">登记工时</el-button>
            </template>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 登记工时 -->
    <el-dialog v-model="showWorklog" title="登记工时" width="400px">
      <el-form :model="worklogForm" label-width="80px">
        <el-form-item label="任务">
          <span>{{ worklogTask?.title }}</span>
        </el-form-item>
        <el-form-item label="已用工时" required>
          <el-input-number v-model="worklogForm.spentHours" :min="0.5" :step="0.5" :precision="1" />
          <span style="margin-left: 8px">小时</span>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="worklogForm.remark" type="textarea" rows="2" placeholder="可选" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWorklog = false">取消</el-button>
        <el-button type="primary" :loading="worklogSubmitting" @click="submitWorklog">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showCreate" title="新建任务" width="560px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="关联需求">
          <el-select v-model="form.requirementId" placeholder="可选，选择所属需求" clearable style="width: 100%">
            <el-option
              v-for="req in requirementOptions"
              :key="req.id"
              :label="req.title"
              :value="req.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="form.type">
            <el-option label="任务" value="TASK" />
            <el-option label="子任务" value="SUBTASK" />
            <el-option label="技术优化" value="TECH" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="预估工时">
          <el-input-number v-model="form.estimatedHours" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-select v-model="form.assigneeId" placeholder="可选" clearable style="width: 100%">
            <el-option
              v-for="m in memberOptions"
              :key="m.userId"
              :label="m.nickname || m.username"
              :value="m.userId"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="create">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { taskApi } from '@/api/task'
import { requirementApi } from '@/api/requirement'
import { projectApi } from '@/api/project'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const authStore = useAuthStore()
const projectId = Number(route.params.id)
const loading = ref(false)
const list = ref<any[]>([])
const viewMode = ref<'board' | 'list'>('board')
const filterReqId = ref<number | null>(null)
const requirementOptions = ref<any[]>([])
const memberOptions = ref<{ userId: number; nickname?: string; username?: string }[]>([])

/** 是否是管理者（超级管理员 或 项目经理） */
const isManager = computed(() => {
  const roles: string[] = authStore.user?.roles || []
  return roles.includes('SUPER_ADMIN') || roles.includes('PROJECT_ADMIN')
})

/** 当前用户是否可操作该任务（改状态、登记工时）：管理者 或 任务负责人 */
function canOperateTask(task: { assigneeId?: number | null }) {
  if (isManager.value) return true
  const uid = authStore.user?.userId
  return uid != null && task.assigneeId != null && task.assigneeId === uid
}

// 非管理者默认只看自己的任务，且不可切换
const onlyMine = ref(!isManager.value)

// 登记工时
const showWorklog = ref(false)
const worklogSubmitting = ref(false)
const worklogTask = ref<any>(null)
const worklogForm = reactive({ spentHours: 0.5, remark: '' })

const showCreate = ref(false)
const creating = ref(false)
const form = reactive({
  title: '',
  type: 'TASK',
  priority: 'MEDIUM',
  estimatedHours: 0,
  dueDate: '',
  requirementId: null as number | null,
  assigneeId: null as number | null
})

const columns = [
  { status: 'TODO', label: '待处理', badgeType: 'info' as const },
  { status: 'IN_PROGRESS', label: '进行中', badgeType: 'warning' as const },
  { status: 'PENDING_REVIEW', label: '待验收', badgeType: 'primary' as const },
  { status: 'DONE', label: '已完成', badgeType: 'success' as const }
]

const priorityTypeMap: Record<string, any> = { LOW: 'info', MEDIUM: '', HIGH: 'warning', CRITICAL: 'danger' }
const taskStatusTypeMap: Record<string, any> = { TODO: 'info', IN_PROGRESS: 'warning', PENDING_REVIEW: 'primary', DONE: 'success' }

function getTasksByStatus(status: string) { return list.value.filter(t => t.status === status) }
function priorityType(p: string) { return priorityTypeMap[p] || '' }
function taskStatusType(s: string) { return taskStatusTypeMap[s] || '' }

function toggleOnlyMine() {
  if (!isManager.value) return  // 非管理者不允许切换
  onlyMine.value = !onlyMine.value
  load()
}

async function load() {
  loading.value = true
  try {
    const params: any = { projectId, page: 1, size: 200 }
    if (filterReqId.value) params.requirementId = filterReqId.value
    // 非管理者后端已强制过滤，前端仍传 assigneeId 以防万一
    if (!isManager.value && authStore.user?.userId) {
      params.assigneeId = authStore.user.userId
    } else if (onlyMine.value && authStore.user?.userId) {
      params.assigneeId = authStore.user.userId
    }
    const res = await taskApi.list(params)
    list.value = (res as any).data?.records || []
  } finally {
    loading.value = false
  }
}

async function loadRequirements() {
  try {
    const res = await requirementApi.list({ projectId, page: 1, size: 200 })
    requirementOptions.value = (res as any).data?.records || []
  } catch {}
}

async function loadMembers() {
  try {
    const res = await projectApi.getMembers(projectId)
    memberOptions.value = (res as any).data || []
  } catch {}
}

async function assignTask(row: any, assigneeId: number | null) {
  try {
    await taskApi.assign(row.id, assigneeId)
    row.assigneeId = assigneeId ?? undefined
    row.assigneeName = assigneeId == null ? null : (memberOptions.value.find(m => m.userId === assigneeId)?.nickname || memberOptions.value.find(m => m.userId === assigneeId)?.username)
    ElMessage.success(assigneeId != null ? '已指派' : '已取消指派')
  } catch {
    load()
  }
}

function openCreate(reqId: number | null) {
  form.title = ''
  form.type = 'TASK'
  form.priority = 'MEDIUM'
  form.estimatedHours = 0
  form.dueDate = ''
  form.requirementId = reqId
  form.assigneeId = null
  showCreate.value = true
}

async function create() {
  if (!form.title.trim()) { ElMessage.warning('请输入任务标题'); return }
  creating.value = true
  try {
    await taskApi.create({ ...form, projectId })
    ElMessage.success('创建成功')
    showCreate.value = false
    load()
  } finally {
    creating.value = false
  }
}

async function changeStatus(row: any, status: string) {
  try {
    await taskApi.updateStatus(row.id, status)
    ElMessage.success('状态已更新')
    load()
  } catch {
    load()
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
    ElMessage.success('工时已登记')
    showWorklog.value = false
    load()
  } finally {
    worklogSubmitting.value = false
  }
}

onMounted(() => {
  load()
  loadRequirements()
  loadMembers()
})
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header-left { display: flex; align-items: center; }
.kanban { display: flex; gap: 16px; overflow-x: auto; padding-bottom: 8px; }
.kanban-col { flex: 1; min-width: 240px; background: #f5f5f5; border-radius: 8px; padding: 12px; }
.kanban-col-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; color: #333; }
.kanban-col-body { display: flex; flex-direction: column; gap: 8px; min-height: 80px; }
.task-card { cursor: pointer; }
.req-tag { display: block; margin-bottom: 6px; max-width: 100%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.task-title { font-size: 13px; color: #333; margin-bottom: 8px; }
.task-meta { display: flex; justify-content: space-between; align-items: center; }
.task-due { margin-top: 6px; font-size: 12px; color: #aaa; display: flex; align-items: center; gap: 4px; }
.assign-select { width: 100px; font-size: 12px; }
.assignee { font-size: 12px; color: #999; }
.hours { font-size: 12px; color: #888; }
.text-muted { color: #ccc; }
.task-actions { margin-top: 8px; padding-top: 6px; border-top: 1px solid #eee; }
</style>
