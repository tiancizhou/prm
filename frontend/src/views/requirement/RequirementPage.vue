<template>
  <div class="req-page">

    <!-- ═══════════════════ HEADER: Title + View Toggles + Actions ═══════════════════ -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">需求管理</h1>
        <div class="view-toggles">
          <el-tooltip content="列表视图" placement="bottom">
            <el-button :type="viewMode === 'list' ? 'primary' : 'default'" size="small" circle @click="viewMode = 'list'">
              <el-icon><List /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="看板视图（即将推出）" placement="bottom">
            <el-button type="default" size="small" circle disabled>
              <el-icon><Grid /></el-icon>
            </el-button>
          </el-tooltip>
          <el-tooltip content="时间线视图（即将推出）" placement="bottom">
            <el-button type="default" size="small" circle disabled>
              <el-icon><Calendar /></el-icon>
            </el-button>
          </el-tooltip>
        </div>
      </div>
      <div class="header-right">
        <el-button v-if="isManager" type="primary" :icon="Plus" @click="openCreate">新建需求</el-button>
        <el-dropdown trigger="click" @command="handleImportExport">
          <el-button type="default" :icon="MoreFilled" circle />
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="import">导入</el-dropdown-item>
              <el-dropdown-item command="export">导出</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>

    <!-- ═══════════════════ QUICK VIEW TABS ═══════════════════ -->
    <div class="quick-tabs">
      <button
        v-for="tab in quickTabs"
        :key="tab.value"
        class="quick-tab"
        :class="{ active: quickView === tab.value }"
        @click="setQuickView(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- ═══════════════════ ADVANCED FILTER BAR ═══════════════════ -->
    <div class="filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="搜索标题..."
        clearable
        class="filter-search"
        :prefix-icon="Search"
        @keyup.enter="load"
      />
      <el-select
        v-model="filterAssigneeIds"
        multiple
        collapse-tags
        collapse-tags-tooltip
        placeholder="负责人"
        clearable
        class="filter-select"
        @change="load"
      >
        <el-option
          v-for="m in members"
          :key="m.userId"
          :label="m.nickname || m.username"
          :value="m.userId"
        />
      </el-select>
      <el-select
        v-model="filterSprintId"
        placeholder="迭代"
        clearable
        class="filter-select"
        @change="load"
      >
        <el-option
          v-for="s in sprints"
          :key="s.id"
          :label="s.name"
          :value="s.id"
        />
      </el-select>
      <el-select
        v-model="filterStatuses"
        multiple
        collapse-tags
        collapse-tags-tooltip
        placeholder="状态"
        clearable
        class="filter-select"
        @change="load"
      >
        <el-option
          v-for="opt in statusOptions"
          :key="opt.value"
          :label="opt.label"
          :value="opt.value"
        />
      </el-select>
      <el-button type="primary" plain @click="load">应用</el-button>
      <div class="filter-bar-right">
        <el-tooltip content="高级筛选" placement="bottom">
          <el-button link :icon="Filter" class="icon-btn" />
        </el-tooltip>
        <el-popover placement="bottom-end" :width="220" trigger="click">
          <template #reference>
            <el-tooltip content="自定义列" placement="bottom">
              <el-button link :icon="Setting" class="icon-btn" />
            </el-tooltip>
          </template>
          <div class="column-picker">
            <div class="column-picker-title">显示列</div>
            <el-checkbox-group v-model="visibleColumns">
              <div v-for="col in columnOptions" :key="col.key" class="column-picker-item">
                <el-checkbox :label="col.key">{{ col.label }}</el-checkbox>
              </div>
            </el-checkbox-group>
          </div>
        </el-popover>
      </div>
    </div>

    <!-- ═══════════════════ DATA TABLE ═══════════════════ -->
    <el-card class="table-card" shadow="never">
      <el-table
        :data="displayList"
        v-loading="loading"
        row-key="id"
        class="req-table"
        @expand-change="onExpand"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="40" align="center" />
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-wrap">
              <div class="expand-header">
                <span class="expand-title">关联任务</span>
                <el-button v-if="isManager" size="small" type="primary" :icon="Plus" @click="openTaskCreate(row)">
                  拆解为任务
                </el-button>
              </div>
              <el-table
                :data="taskMap[row.id] || []"
                :loading="taskLoadingMap[row.id]"
                size="small"
                class="expand-table"
              >
                <el-table-column prop="id" label="ID" width="64" />
                <el-table-column prop="title" label="任务标题" min-width="180" />
                <el-table-column prop="type" label="类型" width="88" />
                <el-table-column label="状态" width="100">
                  <template #default="{ row: task }">
                    <el-tag size="small" round :type="taskStatusType(task.status)">{{ task.statusLabel }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="assigneeName" label="负责人" width="96" />
                <el-table-column label="工时" width="120">
                  <template #default="{ row: task }">
                    <span class="hours">预估 {{ task.estimatedHours }}h / 已用 {{ task.spentHours }}h</span>
                  </template>
                </el-table-column>
                <el-table-column prop="dueDate" label="截止" width="110">
                  <template #default="{ row: task }">{{ formatDate(task.dueDate) || '—' }}</template>
                </el-table-column>
              </el-table>
              <el-empty
                v-if="!taskLoadingMap[row.id] && !(taskMap[row.id]?.length)"
                description="暂无关联任务"
                :image-size="56"
                class="expand-empty"
              />
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="ID" width="72" align="center" v-if="colVisible('id')">
          <template #default="{ row }">{{ row.id }}</template>
        </el-table-column>

        <el-table-column label="标题" min-width="240" v-if="colVisible('title')">
          <template #default="{ row }">
            <div class="title-cell">
              <span class="title-text">{{ row.title || '—' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="优先级" width="96" v-if="colVisible('priority')">
          <template #default="{ row }">
            <el-tag size="small" round :type="priorityType(row.priority)" effect="plain" class="pill">
              {{ priorityLabel(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="100" v-if="colVisible('status')">
          <template #default="{ row }">
            <el-tag size="small" round :type="statusTagType(row.status)" effect="plain" class="pill">
              {{ row.statusLabel || '—' }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="负责人" width="110" v-if="colVisible('assignee')">
          <template #default="{ row }">
            <div class="assignee-cell">
              <el-avatar v-if="row.assigneeName" :size="24" class="avatar">{{ (row.assigneeName || '').charAt(0) }}</el-avatar>
              <span class="assignee-name">{{ row.assigneeName || '—' }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="开始日期" width="108" v-if="colVisible('startDate')">
          <template #default="{ row }">{{ formatDate(row.startDate) || '—' }}</template>
        </el-table-column>

        <el-table-column label="截止日期" width="108" v-if="colVisible('dueDate')">
          <template #default="{ row }">{{ formatDate(row.dueDate) || '—' }}</template>
        </el-table-column>

        <el-table-column label="预估" width="80" align="center" v-if="colVisible('estimate')">
          <template #default="{ row }">
            {{ row.estimatedHours != null && row.estimatedHours !== '' ? row.estimatedHours + 'h' : '—' }}
          </template>
        </el-table-column>

        <el-table-column label="任务数" width="80" align="center" v-if="colVisible('taskCount')">
          <template #default="{ row }">
            <el-tag size="small" type="info" round>{{ taskCountMap[row.id] ?? '—' }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="220" fixed="right" align="right">
          <template #default="{ row }">
            <el-button v-if="canViewRequirement(row)" size="small" link type="primary" @click.stop="openDetail(row)">
              查看
            </el-button>
            <el-button v-if="canViewRequirement(row)" size="small" link type="primary" @click.stop="openEdit(row)">
              编辑
            </el-button>
            <el-button v-if="isManager" size="small" link type="primary" :icon="Plus" @click.stop="openTaskCreate(row)">
              拆解
            </el-button>
            <el-dropdown v-if="canViewRequirement(row) && nextStatusOptions(row).length" @command="(cmd: string) => changeStatus(row, cmd)" trigger="click">
              <el-button size="small" link>状态 <el-icon><ArrowDown /></el-icon></el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-for="opt in nextStatusOptions(row)" :key="opt.command" :command="opt.command">
                    {{ opt.label }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
            <span v-if="!canViewRequirement(row)" class="readonly-hint">—</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="pagination-wrap">
        <span class="pagination-summary">
          显示 {{ rangeStart }}-{{ rangeEnd }} 条，共 {{ total }} 条
        </span>
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="sizes, prev, pager, next"
          small
          @current-change="load"
          @size-change="load"
        />
      </div>
    </el-card>

    <!-- New Requirement Dialog -->
    <el-dialog v-model="showCreate" title="新建需求" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="88px">
        <el-form-item label="需求标题" prop="title" required>
          <el-input v-model="form.title" placeholder="请输入需求标题" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="form.priority" placeholder="优先级" style="width:100%">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预估工时">
              <el-input-number v-model="form.estimatedHours" :min="0" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止日期">
              <el-date-picker v-model="form.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择截止日期" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
        <el-form-item label="验收标准">
          <el-input v-model="form.acceptanceCriteria" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload
            v-model:file-list="uploadFileList"
            :auto-upload="false"
            :limit="10"
            :on-exceed="() => ElMessage.warning('最多上传 10 个文件')"
            multiple
            drag
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">将文件拖到此处，或<em>点击选择</em></div>
            <template #tip>
              <div class="el-upload__tip">支持常见文档（PDF、Word、Excel、图片等），单个不超过 50MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="create">创建</el-button>
      </template>
    </el-dialog>

    <!-- Edit Requirement Dialog -->
    <el-dialog v-model="showEdit" title="编辑需求" width="600px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="formRules" label-width="88px">
        <el-form-item label="需求标题" prop="title" required>
          <el-input v-model="editForm.title" placeholder="请输入需求标题" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="editForm.priority" placeholder="优先级" style="width:100%">
                <el-option label="低" value="LOW" />
                <el-option label="中" value="MEDIUM" />
                <el-option label="高" value="HIGH" />
                <el-option label="紧急" value="CRITICAL" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="预估工时">
              <el-input-number v-model="editForm.estimatedHours" :min="0" :precision="1" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-select v-model="editForm.assigneeId" placeholder="负责人" clearable style="width:100%">
                <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="迭代">
              <el-select v-model="editForm.sprintId" placeholder="迭代" clearable style="width:100%">
                <el-option v-for="s in sprints" :key="s.id" :label="s.name" :value="s.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="editForm.startDate" type="date" value-format="YYYY-MM-DD" placeholder="选择开始日期" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="截止日期">
              <el-date-picker v-model="editForm.dueDate" type="date" value-format="YYYY-MM-DD" placeholder="选择截止日期" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="描述">
          <el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="选填" />
        </el-form-item>
        <el-form-item label="验收标准">
          <el-input v-model="editForm.acceptanceCriteria" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-form-item label="附件">
          <div class="edit-attachments">
            <div v-if="editAttachments.length" class="attachment-list">
              <div v-for="att in editAttachments" :key="att.id" class="attachment-item">
                <el-icon class="att-icon"><Document /></el-icon>
                <span class="att-name" :title="att.filename">{{ att.filename }}</span>
                <span class="att-size">{{ formatFileSize(att.fileSize) }}</span>
                <el-button type="danger" link size="small" @click="removeEditAttachment(att)">删除</el-button>
              </div>
            </div>
            <el-upload
              :show-file-list="false"
              :before-upload="(file) => { uploadEditAttachment(file); return false }"
              :limit="10"
              accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.png,.jpg,.jpeg,.gif,.zip"
              multiple
            >
              <el-button type="primary" plain size="small" :loading="editUploading">
                上传新附件
              </el-button>
            </el-upload>
            <div class="el-upload__tip" style="margin-top:6px">支持 PDF、Word、Excel、图片等，单个不超过 50MB</div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>

    <!-- Requirement Detail Dialog (查看) -->
    <el-dialog v-model="showDetail" title="需求详情" width="620px" destroy-on-close>
      <div v-if="detailReq" class="detail-body">
        <div class="detail-section">
          <h3 class="detail-title">{{ detailReq.title }}</h3>
          <div class="detail-meta">
            <el-tag size="small" round :type="statusTagType(detailReq.status)">{{ detailReq.statusLabel }}</el-tag>
            <el-tag size="small" round :type="priorityType(detailReq.priority)" effect="plain">{{ priorityLabel(detailReq.priority) }}</el-tag>
          </div>
        </div>
        <el-descriptions :column="2" border size="small" class="detail-desc">
          <el-descriptions-item label="负责人">{{ detailReq.assigneeName || '—' }}</el-descriptions-item>
          <el-descriptions-item label="预估工时">{{ detailReq.estimatedHours != null ? detailReq.estimatedHours + 'h' : '—' }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ formatDate(detailReq.startDate) || '—' }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">{{ formatDate(detailReq.dueDate) || '—' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ detailReq.description || '—' }}</el-descriptions-item>
          <el-descriptions-item label="验收标准" :span="2">{{ detailReq.acceptanceCriteria || '—' }}</el-descriptions-item>
        </el-descriptions>
        <div class="detail-section">
          <div class="detail-section-title">附件</div>
          <div v-if="detailAttachments.length" class="attachment-list">
            <div v-for="att in detailAttachments" :key="att.id" class="attachment-item">
              <el-icon class="att-icon"><Document /></el-icon>
              <span class="att-name" :title="att.filename">{{ att.filename }}</span>
              <span class="att-size">{{ formatFileSize(att.fileSize) }}</span>
              <el-button type="primary" link size="small" @click="downloadAttachment(att)">下载</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无附件" :image-size="50" />
        </div>
      </div>
      <template #footer>
        <el-button @click="showDetail = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Decompose Task Dialog -->
    <el-dialog v-model="showTaskCreate" :title="`拆解任务 · ${currentReq?.title || ''}`" width="560px" destroy-on-close>
      <el-form :model="taskForm" label-width="100px">
        <el-form-item label="所属需求">
          <el-input :model-value="currentReq?.title" disabled />
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
          <el-date-picker v-model="taskForm.dueDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
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
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, Search, List, Grid, Calendar, MoreFilled, Filter, Setting, ArrowDown, UploadFilled, Document } from '@element-plus/icons-vue'
import { requirementApi } from '@/api/requirement'
import { taskApi } from '@/api/task'
import { projectApi } from '@/api/project'
import { sprintApi } from '@/api/sprint'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const route = useRoute()
const projectId = Number(route.params.id)
const currentUserId = computed(() => authStore.user?.userId ?? null)

const isManager = computed(() => {
  const roles: string[] = authStore.user?.roles || []
  return roles.includes('SUPER_ADMIN') || roles.includes('PROJECT_ADMIN')
})

// View mode & quick view
const viewMode = ref<'list' | 'kanban' | 'timeline'>('list')
const quickView = ref<'all' | 'assigned' | 'due_week' | 'unscheduled'>('all')

const quickTabs = [
  { value: 'all', label: '全部' },
  { value: 'assigned', label: '分配给我' },
  { value: 'due_week', label: '本周到期' },
  { value: 'unscheduled', label: '未排期' }
]

// Filter state (multi-select values)
const filterAssigneeIds = ref<number[]>([])
const filterSprintId = ref<number | null>(null)
const filterStatuses = ref<string[]>([])
const query = reactive({
  page: 1,
  size: 20,
  projectId,
  status: '' as string,
  keyword: '',
  assigneeId: null as number | null,
  sprintId: null as number | null,
  unscheduled: false
})

// Build API params from filters
function buildParams() {
  const status = filterStatuses.value.length === 1 ? filterStatuses.value[0] : filterStatuses.value.length > 1 ? undefined : query.status

  // 本周到期：周一到周日
  let dueDateFrom: string | undefined
  let dueDateTo: string | undefined
  if (quickView.value === 'due_week') {
    const now = new Date()
    const dayOfWeek = now.getDay() === 0 ? 7 : now.getDay()
    const monday = new Date(now)
    monday.setDate(now.getDate() - dayOfWeek + 1)
    monday.setHours(0, 0, 0, 0)
    const sunday = new Date(monday)
    sunday.setDate(monday.getDate() + 6)
    dueDateFrom = monday.toISOString().slice(0, 10)
    dueDateTo   = sunday.toISOString().slice(0, 10)
  }

  return {
    page: query.page,
    size: query.size,
    projectId,
    keyword: query.keyword || undefined,
    status: status || undefined,
    assigneeId: quickView.value === 'assigned' ? currentUserId.value : filterAssigneeIds.value[0] ?? undefined,
    sprintId: quickView.value === 'unscheduled' ? undefined : (filterSprintId.value ?? undefined),
    unscheduled: quickView.value === 'unscheduled' ? true : undefined,
    dueDateFrom,
    dueDateTo
  }
}

function setQuickView(v: typeof quickView.value) {
  quickView.value = v
  query.page = 1
  load()
}

const statusOptions = [
  { value: 'DRAFT', label: '待办' },
  { value: 'IN_PROGRESS', label: '进行中' },
  { value: 'DONE', label: '已完成' }
]

// Column visibility (customize columns)
const columnOptions = [
  { key: 'id', label: 'ID' },
  { key: 'title', label: '标题' },
  { key: 'priority', label: '优先级' },
  { key: 'status', label: '状态' },
  { key: 'assignee', label: '负责人' },
  { key: 'startDate', label: '开始日期' },
  { key: 'dueDate', label: '截止日期' },
  { key: 'estimate', label: '预估' },
  { key: 'taskCount', label: '任务数' }
]

const visibleColumns = ref<string[]>(['id', 'title', 'priority', 'status', 'assignee', 'dueDate', 'estimate', 'taskCount'])

function colVisible(key: string) {
  return visibleColumns.value.includes(key)
}

// Data
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const members = ref<any[]>([])
const sprints = ref<any[]>([])

const taskMap = reactive<Record<number, any[]>>({})
const taskLoadingMap = reactive<Record<number, boolean>>({})
const taskCountMap = reactive<Record<number, number>>({})

// When multiple statuses selected, filter client-side (API only supports single status)
const displayList = computed(() => {
  let rows = list.value
  if (filterStatuses.value.length > 1) {
    const set = new Set(filterStatuses.value)
    rows = rows.filter(r => set.has(r.status))
  }
  if (filterAssigneeIds.value.length > 1) {
    const set = new Set(filterAssigneeIds.value)
    rows = rows.filter(r => r.assigneeId != null && set.has(r.assigneeId))
  }
  return rows
})

const rangeStart = computed(() => total.value === 0 ? 0 : (query.page - 1) * query.size + 1)
const rangeEnd = computed(() => Math.min(query.page * query.size, total.value))

// Selection for bulk actions
const selectedRows = ref<any[]>([])

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows
}

function handleImportExport(cmd: string) {
  if (cmd === 'import') ElMessage.info('导入功能即将上线')
  else if (cmd === 'export') ElMessage.info('导出功能即将上线')
}

// Labels & types
const priorityMap: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '紧急' }
const priorityTypeMap: Record<string, string> = { LOW: 'info', MEDIUM: '', HIGH: 'warning', CRITICAL: 'danger' }
const statusTagMap: Record<string, string> = {
  DRAFT: 'info', IN_PROGRESS: 'warning', DONE: 'success'
}
const taskStatusTypeMap: Record<string, string> = { TODO: 'info', IN_PROGRESS: 'warning', PENDING_REVIEW: 'primary', DONE: 'success', CLOSED: '' }

function priorityLabel(p: string) { return priorityMap[p] || p || '—' }
function priorityType(p: string) { return priorityTypeMap[p] || '' }
function statusTagType(s: string) { return statusTagMap[s] || 'info' }
function taskStatusType(s: string) { return taskStatusTypeMap[s] || '' }

/** 三态流转：待办→开始，进行中→完成，已完成→重新打开 */
function nextStatusOptions(row: any): { command: string; label: string }[] {
  const s = row?.status
  if (s === 'DRAFT' || s === 'REVIEWING' || s === 'APPROVED') return [{ command: 'IN_PROGRESS', label: '开始' }]
  if (s === 'IN_PROGRESS') return [{ command: 'DONE', label: '标记完成' }]
  if (s === 'DONE' || s === 'CLOSED') return [{ command: 'IN_PROGRESS', label: '重新打开' }]
  return []
}

/** 是否可查看该需求（管理员或负责人） */
function canViewRequirement(row: any): boolean {
  if (isManager.value) return true
  return row?.assigneeId != null && row.assigneeId === currentUserId.value
}

async function openDetail(row: any) {
  try {
    const res = await requirementApi.get(row.id)
    detailReq.value = (res as any).data
    const attRes = await requirementApi.listAttachments(row.id)
    detailAttachments.value = (attRes as any).data ?? []
    showDetail.value = true
  } catch {
    ElMessage.error('加载需求详情失败')
  }
}

async function downloadAttachment(att: any) {
  if (!detailReq.value?.id) return
  try {
    await requirementApi.downloadAttachment(detailReq.value.id, att.id, att.filename)
    ElMessage.success('开始下载')
  } catch {
    ElMessage.error('下载失败')
  }
}

function formatDate(d: string | null | undefined): string {
  if (!d) return ''
  const date = new Date(d)
  if (isNaN(date.getTime())) return d
  return date.toISOString().slice(0, 10)
}

function formatFileSize(bytes: number | null | undefined): string {
  if (bytes == null || bytes === 0) return '—'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

// New requirement form
const showCreate = ref(false)
const creating = ref(false)
const formRef = ref()
const uploadFileList = ref<any[]>([])

const showEdit = ref(false)
const updating = ref(false)
const editFormRef = ref()
const editingReq = ref<any>(null)
const editAttachments = ref<any[]>([])
const editUploading = ref(false)

const showDetail = ref(false)
const detailReq = ref<any>(null)
const detailAttachments = ref<any[]>([])

const editForm = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  assigneeId: null as number | null,
  sprintId: null as number | null,
  estimatedHours: 0,
  acceptanceCriteria: '',
  startDate: '',
  dueDate: ''
})

const form = reactive({
  title: '',
  description: '',
  priority: 'MEDIUM',
  estimatedHours: 0,
  acceptanceCriteria: '',
  startDate: '',
  dueDate: ''
})
const formRules = { title: [{ required: true, message: '请输入需求标题', trigger: 'blur' }] }

// Decompose task
const showTaskCreate = ref(false)
const creatingTask = ref(false)
const currentReq = ref<any>(null)
const taskForm = reactive({ title: '', type: 'TASK', priority: 'MEDIUM', estimatedHours: 0, dueDate: '' })

async function load() {
  loading.value = true
  try {
    const params = buildParams() as any
    const res = await requirementApi.list(params)
    const data = (res as any).data
    list.value = data.records || []
    total.value = data.total ?? 0
    list.value.forEach((req: any) => loadTaskCount(req.id))
  } finally {
    loading.value = false
  }
}

async function loadMembers() {
  try {
    const res = await projectApi.getMembers(projectId)
    members.value = (res as any).data || []
  } catch {
    members.value = []
  }
}

async function loadSprints() {
  try {
    const res = await sprintApi.list({ projectId })
    const data = (res as any).data
    sprints.value = data?.records ?? data?.list ?? (Array.isArray(data) ? data : [])
  } catch {
    sprints.value = []
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
    const data = (res as any).data
    taskMap[reqId] = data?.records || []
  } finally {
    taskLoadingMap[reqId] = false
  }
}

function onExpand(row: any, expandedRows: any[]) {
  if (expandedRows.some((r: any) => r.id === row.id)) loadTasks(row.id)
}

function openCreate() {
  showCreate.value = true
}

async function openEdit(row: any) {
  editingReq.value = row
  try {
    const res = await requirementApi.get(row.id)
    const d = (res as any).data
    editForm.title = d.title ?? ''
    editForm.description = d.description ?? ''
    editForm.priority = d.priority ?? 'MEDIUM'
    editForm.assigneeId = d.assigneeId ?? null
    editForm.sprintId = d.sprintId ?? null
    editForm.estimatedHours = d.estimatedHours ?? 0
    editForm.acceptanceCriteria = d.acceptanceCriteria ?? ''
    editForm.startDate = d.startDate ? String(d.startDate).slice(0, 10) : ''
    editForm.dueDate = d.dueDate ? String(d.dueDate).slice(0, 10) : ''
    showEdit.value = true
    await loadEditAttachments(row.id)
  } catch {
    ElMessage.error('加载需求失败')
  }
}

async function loadEditAttachments(requirementId: number) {
  try {
    const res = await requirementApi.listAttachments(requirementId)
    editAttachments.value = (res as any).data ?? []
  } catch {
    editAttachments.value = []
  }
}

async function removeEditAttachment(att: any) {
  try {
    await ElMessageBox.confirm('确定删除该附件？', '提示', { type: 'warning' })
  } catch {
    return
  }
  if (!editingReq.value?.id) return
  try {
    await requirementApi.deleteAttachment(editingReq.value.id, att.id)
    ElMessage.success('已删除')
    editAttachments.value = editAttachments.value.filter(a => a.id !== att.id)
  } catch {
    ElMessage.error('删除失败')
  }
}

async function uploadEditAttachment(file: File) {
  if (!editingReq.value?.id) return
  editUploading.value = true
  try {
    const res = await requirementApi.uploadAttachment(editingReq.value.id, file)
    const newAtt = (res as any).data
    if (newAtt) editAttachments.value = [newAtt, ...editAttachments.value]
    ElMessage.success('上传成功')
  } catch {
    ElMessage.error('上传失败')
  } finally {
    editUploading.value = false
  }
}

async function saveEdit() {
  await (editFormRef.value as any)?.validate().catch(() => {})
  if (!editForm.title.trim()) { ElMessage.warning('请输入需求标题'); return }
  if (!editingReq.value?.id) return
  updating.value = true
  try {
    const payload = {
      title: editForm.title,
      description: editForm.description,
      priority: editForm.priority,
      assigneeId: editForm.assigneeId ?? undefined,
      sprintId: editForm.sprintId ?? undefined,
      estimatedHours: editForm.estimatedHours,
      acceptanceCriteria: editForm.acceptanceCriteria,
      startDate: editForm.startDate || undefined,
      dueDate: editForm.dueDate || undefined
    }
    await requirementApi.update(editingReq.value.id, payload)
    ElMessage.success('需求已更新')
    showEdit.value = false
    load()
  } finally {
    updating.value = false
  }
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
  await (formRef.value as any)?.validate().catch(() => {})
  if (!form.title.trim()) { ElMessage.warning('请输入需求标题'); return }
  creating.value = true
  try {
    const res = await requirementApi.create({ ...form, projectId })
    const newId = (res as any).data?.id
    if (newId && uploadFileList.value?.length) {
      for (const item of uploadFileList.value) {
        if (item.raw) await requirementApi.uploadAttachment(newId, item.raw)
      }
      uploadFileList.value = []
    }
    ElMessage.success('需求创建成功')
    showCreate.value = false
    Object.assign(form, { title: '', description: '', priority: 'MEDIUM', estimatedHours: 0, acceptanceCriteria: '', startDate: '', dueDate: '' })
    load()
  } finally {
    creating.value = false
  }
}

async function createTask() {
  if (!taskForm.title.trim()) { ElMessage.warning('请输入任务标题'); return }
  creatingTask.value = true
  try {
    await taskApi.create({ ...taskForm, projectId, requirementId: currentReq.value?.id })
    ElMessage.success('任务创建成功')
    showTaskCreate.value = false
    if (currentReq.value?.id) {
      loadTasks(currentReq.value.id)
      loadTaskCount(currentReq.value.id)
    }
    load()
  } finally {
    creatingTask.value = false
  }
}

async function changeStatus(row: any, status: string) {
  await requirementApi.updateStatus(row.id, status)
  ElMessage.success('状态已更新')
  load()
}

onMounted(() => {
  load()
  loadMembers()
  loadSprints()
})
</script>

<style scoped>
.req-page {
  padding: 0;
  min-height: 100%;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  flex-wrap: wrap;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
  color: #111827;
  letter-spacing: -0.02em;
}

.view-toggles {
  display: flex;
  gap: 4px;
}

.view-toggles .el-button.is-circle {
  width: 32px;
  height: 32px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* Quick tabs */
.quick-tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
  padding-bottom: 0;
}

.quick-tab {
  padding: 10px 16px;
  font-size: 13px;
  color: #6b7280;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -1px;
  cursor: pointer;
  border-radius: 6px 6px 0 0;
  transition: color 0.15s, background 0.15s;
}

.quick-tab:hover {
  color: #111827;
  background: #f3f4f6;
}

.quick-tab.active {
  color: #2563eb;
  font-weight: 600;
  border-bottom-color: #2563eb;
}

/* Filter bar */
.filter-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
}

.filter-search {
  width: 220px;
}

.filter-select {
  width: 140px;
}

.filter-bar-right {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 4px;
}

.icon-btn {
  font-size: 16px;
  color: #6b7280;
}

.icon-btn:hover {
  color: #111827;
}

.column-picker {
  padding: 4px 0;
}

.column-picker-title {
  font-size: 13px;
  font-weight: 600;
  color: #111827;
  margin-bottom: 12px;
}

.column-picker-item {
  margin-bottom: 8px;
}

.column-picker-item:last-child {
  margin-bottom: 0;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 6px;
  line-height: 1.4;
}

.edit-attachments {
  width: 100%;
}

.attachment-list {
  margin-bottom: 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 10px;
  background: #f5f7fa;
  border-radius: 6px;
  margin-bottom: 6px;
  font-size: 13px;
}

.attachment-item .att-icon {
  color: #909399;
  font-size: 16px;
}

.attachment-item .att-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #303133;
}

.attachment-item .att-size {
  color: #909399;
  font-size: 12px;
  flex-shrink: 0;
}

.detail-body {
  padding: 0 4px;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-section:last-of-type {
  margin-bottom: 0;
}

.detail-title {
  margin: 0 0 10px;
  font-size: 16px;
  font-weight: 600;
  color: #111827;
  line-height: 1.4;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 14px;
}

.detail-type {
  font-size: 12px;
  color: #6b7280;
}

.detail-desc {
  margin-bottom: 16px;
}

.detail-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 10px;
}

/* Table card */
.table-card {
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.req-table {
  --el-table-border-color: #f3f4f6;
  --el-table-header-bg-color: #fafafa;
}

.req-table :deep(.el-table__row:hover) {
  background-color: #f8fafc !important;
}

.req-table :deep(.el-table th.el-table__cell) {
  font-weight: 600;
  font-size: 12px;
  color: #6b7280;
  text-transform: none;
}

.title-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}


.title-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #111827;
  font-size: 14px;
}

.pill {
  font-size: 12px;
}

.assignee-cell {
  display: flex;
  align-items: center;
  gap: 6px;
}

.assignee-cell .avatar {
  background: #e5e7eb;
  color: #374151;
  font-size: 12px;
}

.assignee-name {
  font-size: 13px;
  color: #374151;
}

.expand-wrap {
  padding: 16px 24px 20px;
  background: #fafafa;
  border-radius: 8px;
  margin: 8px 16px;
}

.expand-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.expand-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
}

.expand-table {
  margin-top: 8px;
}

.expand-empty {
  padding: 16px 0;
}

.hours {
  font-size: 12px;
  color: #6b7280;
}

.readonly-hint {
  font-size: 12px;
  color: #9ca3af;
}

.pagination-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f3f4f6;
  background: #fafafa;
}

.pagination-summary {
  font-size: 13px;
  color: #6b7280;
}

.pagination-wrap .el-pagination {
  justify-content: flex-end;
}
</style>
