<template>
  <div class="app-page bug-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ bugText.pageTitle }}</h1>
        <p class="page-subtitle">{{ bugText.pageSubtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Plus" @click="showCreate = true">{{ bugText.buttons.submitBug }}</el-button>
      </div>
    </header>

    <el-card class="surface-card bug-surface" shadow="never">
      <el-row :gutter="12" class="filter-row">
        <el-col :xs="24" :sm="8" :md="6">
          <el-select v-model="query.status" :aria-label="bugText.aria.statusFilter" :placeholder="bugText.placeholders.status" clearable @change="load">
            <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="8" :md="6">
          <el-select v-model="query.severity" :aria-label="bugText.aria.severityFilter" :placeholder="bugText.placeholders.severity" clearable @change="load">
            <el-option v-for="severity in severityOptions" :key="severity.value" :label="severity.label" :value="severity.value" />
          </el-select>
        </el-col>
        <el-col :xs="24" :sm="8" :md="12">
          <el-input
            v-model="query.keyword"
            name="bugKeyword"
            autocomplete="off"
            :aria-label="bugText.aria.keywordSearch"
            :placeholder="bugText.placeholders.keyword"
            clearable
            :prefix-icon="Search"
            @change="load"
          />
        </el-col>
      </el-row>

      <el-table :data="list" v-loading="loading" class="bug-table">
        <el-table-column prop="id" :label="bugText.labels.id" width="70" />
        <el-table-column prop="title" :label="bugText.labels.title" min-width="220" />
        <el-table-column :label="bugText.labels.severity" width="120">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)" size="small">{{ severityLabel(row.severity) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="bugText.labels.status" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" :label="bugText.labels.assignee" width="120">
          <template #default="{ row }">{{ row.assigneeName || bugText.labels.noneSymbol }}</template>
        </el-table-column>
        <el-table-column prop="reporterName" :label="bugText.labels.reporter" width="120" />
        <el-table-column prop="createdAt" :label="bugText.labels.createdAt" width="180" />
        <el-table-column :label="bugText.labels.actions" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'CONFIRMED'" size="small" @click="assign(row)">{{ bugText.buttons.assign }}</el-button>
            <el-button v-if="row.status === 'ASSIGNED'" size="small" type="success" @click="resolve(row)">{{ bugText.buttons.resolve }}</el-button>
            <el-button v-if="row.status === 'RESOLVED'" size="small" type="info" @click="verify(row)">{{ bugText.buttons.verify }}</el-button>
            <span v-if="row.status !== 'CONFIRMED' && row.status !== 'ASSIGNED' && row.status !== 'RESOLVED'" class="text-muted">{{ bugText.labels.noneSymbol }}</span>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        class="page-pagination"
        @change="load"
      />
    </el-card>

    <el-dialog v-model="showCreate" :title="bugText.dialogs.submitBug" width="640px">
      <el-form :model="form" label-width="104px">
        <el-form-item :label="bugText.labels.bugTitle" required>
          <el-input v-model="form.title" name="bugTitle" autocomplete="off" :placeholder="bugText.placeholders.bugTitle" />
        </el-form-item>
        <el-form-item :label="bugText.labels.module">
          <el-input v-model="form.module" name="bugModule" autocomplete="off" :placeholder="bugText.placeholders.module" />
        </el-form-item>
        <el-row :gutter="12">
          <el-col :span="12">
            <el-form-item :label="bugText.labels.severity">
              <el-select v-model="form.severity" class="full-width-control">
                <el-option v-for="severity in severityOptions" :key="severity.value" :label="severity.label" :value="severity.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="bugText.labels.priority">
              <el-select v-model="form.priority" class="full-width-control">
                <el-option v-for="priority in priorityOptions" :key="priority.value" :label="priority.label" :value="priority.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="bugText.labels.steps">
          <el-input v-model="form.steps" name="bugSteps" autocomplete="off" type="textarea" :rows="3" :placeholder="bugText.placeholders.steps" />
        </el-form-item>
        <el-form-item :label="bugText.labels.expectedResult">
          <el-input v-model="form.expectedResult" name="bugExpectedResult" autocomplete="off" :placeholder="bugText.placeholders.expectedResult" />
        </el-form-item>
        <el-form-item :label="bugText.labels.actualResult">
          <el-input v-model="form.actualResult" name="bugActualResult" autocomplete="off" :placeholder="bugText.placeholders.actualResult" />
        </el-form-item>
        <el-form-item :label="bugText.labels.environment">
          <el-input v-model="form.environment" name="bugEnvironment" autocomplete="off" :placeholder="bugText.placeholders.environment" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">{{ bugText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="creating" @click="create">{{ bugText.buttons.submit }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { bugApi } from '@/api/bug'
import { BUG_PAGE_I18N } from '@/constants/bug'
import { resolveThemeLocale } from '@/constants/theme'

const route = useRoute()
const projectId = Number(route.params.id)
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const bugText = BUG_PAGE_I18N[currentLocale]

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const statusOptions = [...bugText.statusOptions]
const severityOptions = [...bugText.severityOptions]
const priorityOptions = [...bugText.priorityOptions]

const query = reactive({
  page: 1,
  size: 20,
  projectId,
  status: '',
  severity: '',
  keyword: ''
})

const showCreate = ref(false)
const creating = ref(false)
const form = reactive({
  title: '',
  module: '',
  severity: 'NORMAL',
  priority: 'MEDIUM',
  steps: '',
  expectedResult: '',
  actualResult: '',
  environment: ''
})

function severityType(severity: string) {
  const map: Record<string, 'danger' | 'warning' | 'info' | undefined> = {
    BLOCKER: 'danger',
    CRITICAL: 'warning',
    NORMAL: undefined,
    MINOR: 'info'
  }
  return map[severity]
}

function statusLabel(status: string) {
  return bugText.statusLabels[status] || status || bugText.labels.noneSymbol
}

function severityLabel(severity: string) {
  return bugText.severityLabels[severity] || severity || bugText.labels.noneSymbol
}

async function load() {
  loading.value = true
  try {
    const response = await bugApi.list({ ...query })
    const data = (response as any).data
    list.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function create() {
  creating.value = true
  try {
    await bugApi.create({ ...form, projectId })
    ElMessage.success(bugText.messages.submitSuccess)
    showCreate.value = false
    await load()
  } finally {
    creating.value = false
  }
}

async function assign(row: any) {
  try {
    const result = await ElMessageBox.prompt(bugText.dialogs.assignMessage, bugText.dialogs.assignTitle, {
      inputType: 'number',
      inputPattern: /^\d+$/,
      inputErrorMessage: bugText.messages.assigneeIdInvalid
    })
    const assigneeId = Number((result as any).value)
    await bugApi.assign(row.id, assigneeId)
    ElMessage.success(bugText.messages.assignSuccess)
    await load()
  } catch {
    return
  }
}

async function resolve(row: any) {
  await bugApi.updateStatus(row.id, 'RESOLVED', 'FIXED')
  ElMessage.success(bugText.messages.resolveSuccess)
  await load()
}

async function verify(row: any) {
  await bugApi.updateStatus(row.id, 'VERIFIED')
  ElMessage.success(bugText.messages.verifySuccess)
  await load()
}

onMounted(() => {
  void load()
})
</script>

<style scoped>
.bug-page {
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

.bug-surface {
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.bug-surface:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 28%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.filter-row {
  margin-bottom: var(--space-lg);
}

.bug-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.bug-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.page-pagination {
  margin-top: var(--space-lg);
  justify-content: flex-end;
}

.full-width-control {
  width: 100%;
}

.text-muted {
  color: var(--app-text-muted);
}
</style>
