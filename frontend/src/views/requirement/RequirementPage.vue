<template>
  <div>
    <div class="page-header">
      <h2>需求管理</h2>
      <el-button v-if="isManager" type="primary" icon="Plus" @click="openCreate(null)">新建需求</el-button>
    </div>

    <el-card>
      <el-row :gutter="12" style="margin-bottom: 16px">
        <el-col :span="6">
          <el-select v-model="query.status" placeholder="状态筛选" clearable @change="load">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="评审中" value="REVIEWING" />
            <el-option label="已立项" value="APPROVED" />
            <el-option label="开发中" value="IN_PROGRESS" />
            <el-option label="已完成" value="DONE" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-input v-model="query.keyword" placeholder="搜索标题" clearable @change="load" prefix-icon="Search" />
        </el-col>
      </el-row>

      <el-table
        :data="list"
        v-loading="loading"
        stripe
        row-key="id"
        @expand-change="onExpand"
      >
        <!-- 展开列 -->
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-wrap">
              <div class="expand-header">
                <span class="expand-title">关联任务</span>
                <el-button v-if="isManager" size="small" type="primary" icon="Plus" @click="openTaskCreate(row)">
                  拆解为任务
                </el-button>
              </div>

              <!-- 任务列表 -->
              <el-table
                :data="taskMap[row.id] || []"
                :loading="taskLoadingMap[row.id]"
                size="small"
                stripe
                style="margin-top: 8px"
              >
                <el-table-column prop="id" label="ID" width="60" />
                <el-table-column prop="title" label="任务标题" min-width="180" />
                <el-table-column prop="type" label="类型" width="80" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row: task }">
                    <el-tag size="small" :type="taskStatusType(task.status)">{{ task.statusLabel }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="assigneeName" label="负责人" width="90" />
                <el-table-column label="工时" width="130">
                  <template #default="{ row: task }">
                    <span class="hours">
                      预估 {{ task.estimatedHours }}h / 已用 {{ task.spentHours }}h
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="dueDate" label="截止" width="110" />
              </el-table>

              <el-empty
                v-if="!taskLoadingMap[row.id] && !(taskMap[row.id]?.length)"
                description="暂无关联任务，点击「拆解为任务」新建"
                :image-size="60"
                style="padding: 16px 0"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column label="优先级" width="90">
          <template #default="{ row }">
            <el-tag :type="priorityType(row.priority)" size="small">{{ priorityLabel(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag>{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="90" />
        <el-table-column label="任务数" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info" round>{{ taskCountMap[row.id] ?? '—' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button v-if="isManager" size="small" icon="Plus" @click.stop="openTaskCreate(row)">拆解任务</el-button>
            <el-dropdown v-if="isManager" @command="(cmd: string) => changeStatus(row, cmd)" size="small" style="margin-left: 8px">
              <el-button size="small">状态 <el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="REVIEWING">提交评审</el-dropdown-item>
                  <el-dropdown-item command="APPROVED">立项</el-dropdown-item>
                  <el-dropdown-item command="IN_PROGRESS">开始开发</el-dropdown-item>
                  <el-dropdown-item command="DONE">标记完成</el-dropdown-item>
                  <el-dropdown-item command="CLOSED">关闭</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <span v-if="!isManager" class="readonly-hint">只读</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="load"
      />
    </el-card>

    <!-- 新建需求对话框 -->
    <el-dialog v-model="showCreate" title="新建需求" width="560px">
      <el-form ref="formRef" :model="form" label-width="100px">
        <el-form-item label="需求标题" required>
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="form.priority">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="紧急" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="预估工时">
          <el-input-number v-model="form.estimatedHours" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="验收标准">
          <el-input v-model="form.acceptanceCriteria" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="create">创建</el-button>
      </template>
    </el-dialog>

    <!-- 拆解任务对话框 -->
    <el-dialog v-model="showTaskCreate" :title="`拆解任务 · ${currentReq?.title || ''}`" width="560px">
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="所属需求">
          <el-input :value="currentReq?.title" disabled />
        </el-form-item>
        <el-form-item label="任务标题" required>
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务类型">
          <el-select v-model="taskForm.type">
            <el-option label="任务" value="TASK" />
            <el-option label="子任务" value="SUBTASK" />
            <el-option label="技术优化" value="TECH" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="taskForm.priority">
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
          </el-select>
        </el-form-item>
        <el-form-item label="预估工时">
          <el-input-number v-model="taskForm.estimatedHours" :min="0" :precision="1" />
        </el-form-item>
        <el-form-item label="截止日期">
          <el-date-picker v-model="taskForm.dueDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskCreate = false">取消</el-button>
        <el-button type="primary" :loading="creatingTask" @click="createTask">创建任务</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { requirementApi } from '@/api/requirement'
import { taskApi } from '@/api/task'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
/** 是否是管理者（超级管理员 或 项目经理） */
const isManager = computed(() => {
  const roles: string[] = authStore.user?.roles || []
  return roles.includes('SUPER_ADMIN') || roles.includes('PROJECT_ADMIN')
})

const route = useRoute()
const projectId = Number(route.params.id)
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, projectId, status: '', keyword: '' })

// 每条需求下的任务缓存 { requirementId: Task[] }
const taskMap = reactive<Record<number, any[]>>({})
const taskLoadingMap = reactive<Record<number, boolean>>({})
const taskCountMap = reactive<Record<number, number>>({})

// 新建需求
const showCreate = ref(false)
const creating = ref(false)
const form = reactive({ title: '', description: '', priority: 'MEDIUM', estimatedHours: 0, acceptanceCriteria: '' })

// 拆解任务
const showTaskCreate = ref(false)
const creatingTask = ref(false)
const currentReq = ref<any>(null)
const taskForm = reactive({ title: '', type: 'TASK', priority: 'MEDIUM', estimatedHours: 0, dueDate: '' })

const priorityMap: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '紧急' }
const priorityTypeMap: Record<string, any> = { LOW: 'info', MEDIUM: '', HIGH: 'warning', CRITICAL: 'danger' }
const taskStatusTypeMap: Record<string, any> = { TODO: 'info', IN_PROGRESS: 'warning', PENDING_REVIEW: 'primary', DONE: 'success', CLOSED: '' }

function priorityLabel(p: string) { return priorityMap[p] || p }
function priorityType(p: string) { return priorityTypeMap[p] || '' }
function taskStatusType(s: string) { return taskStatusTypeMap[s] || '' }

async function load() {
  loading.value = true
  try {
    const res = await requirementApi.list({ ...query })
    const data = (res as any).data
    list.value = data.records
    total.value = data.total
    // 并行获取每条需求的任务数量
    data.records.forEach((req: any) => loadTaskCount(req.id))
  } finally {
    loading.value = false
  }
}

async function loadTaskCount(reqId: number) {
  try {
    const res = await taskApi.list({ projectId, requirementId: reqId, page: 1, size: 1 })
    taskCountMap[reqId] = (res as any).data?.total ?? 0
  } catch {}
}

async function loadTasks(reqId: number) {
  taskLoadingMap[reqId] = true
  try {
    const res = await taskApi.list({ projectId, requirementId: reqId, page: 1, size: 100 })
    taskMap[reqId] = (res as any).data?.records || []
  } finally {
    taskLoadingMap[reqId] = false
  }
}

function onExpand(row: any, expandedRows: any[]) {
  const isExpanded = expandedRows.some((r: any) => r.id === row.id)
  if (isExpanded) {
    loadTasks(row.id)
  }
}

function openCreate(req: any) {
  showCreate.value = true
}

function openTaskCreate(req: any) {
  currentReq.value = req
  taskForm.title = ''
  taskForm.type = 'TASK'
  taskForm.priority = 'MEDIUM'
  taskForm.estimatedHours = 0
  taskForm.dueDate = ''
  showTaskCreate.value = true
}

async function create() {
  if (!form.title.trim()) { ElMessage.warning('请输入需求标题'); return }
  creating.value = true
  try {
    await requirementApi.create({ ...form, projectId })
    ElMessage.success('需求创建成功')
    showCreate.value = false
    load()
  } finally {
    creating.value = false
  }
}

async function createTask() {
  if (!taskForm.title.trim()) { ElMessage.warning('请输入任务标题'); return }
  creatingTask.value = true
  try {
    await taskApi.create({
      ...taskForm,
      projectId,
      requirementId: currentReq.value?.id
    })
    ElMessage.success('任务创建成功')
    showTaskCreate.value = false
    // 刷新展开行任务列表和任务计数
    if (currentReq.value?.id) {
      loadTasks(currentReq.value.id)
      loadTaskCount(currentReq.value.id)
    }
  } finally {
    creatingTask.value = false
  }
}

async function changeStatus(row: any, status: string) {
  await requirementApi.updateStatus(row.id, status)
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 { margin: 0; font-size: 20px; }

.expand-wrap {
  padding: 12px 24px 16px;
  background: #fafafa;
  border-radius: 4px;
}
.expand-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.expand-title {
  font-size: 13px;
  font-weight: 600;
  color: #333;
}
.hours {
  font-size: 12px;
  color: #888;
}
.readonly-hint {
  font-size: 12px;
  color: #bbb;
}
</style>
