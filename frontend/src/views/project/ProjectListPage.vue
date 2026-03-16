<template>
  <div class="app-page project-list-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ projectListText.pageTitle }}</h1>
        <p class="page-subtitle">{{ projectListText.pageSubtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button v-if="canCreateProject" type="primary" :icon="Plus" @click="showCreate = true">{{ projectListText.buttons.newProject }}</el-button>
      </div>
    </header>

    <el-card class="surface-card project-surface" shadow="never">
      <div class="filter-bar">
        <el-input
          v-model="query.keyword"
          name="projectKeyword"
          autocomplete="off"
          :aria-label="projectListText.aria.searchProject"
          :placeholder="projectListText.placeholders.searchProject"
          clearable
          :prefix-icon="Search"
          @change="loadProjects"
        />
        <el-select
          v-model="query.status"
          :aria-label="projectListText.aria.statusFilter"
          :placeholder="projectListText.placeholders.allStatus"
          clearable
          @change="loadProjects"
        >
          <el-option v-for="status in statusOptions" :key="status.value" :label="status.label" :value="status.value" />
        </el-select>
        <div class="filter-actions">
          <el-button @click="resetFilters">{{ projectListText.buttons.reset }}</el-button>
        </div>
      </div>

      <el-table :data="projects" v-loading="loading" class="project-table" :empty-text="projectListText.labels.tableEmpty">
        <el-table-column prop="name" :label="projectListText.labels.projectName" min-width="220">
          <template #default="{ row }">
            <RouterLink :to="`/projects/${row.id}/overview`" class="workspace-link-text workspace-link-text--primary" @click="projectStore.setCurrentProject(row)">{{ row.name }}</RouterLink>
          </template>
        </el-table-column>
        <el-table-column prop="code" :label="projectListText.labels.code" width="140" />
        <el-table-column prop="status" :label="projectListText.labels.status" width="120">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)" effect="light">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" :label="projectListText.labels.owner" width="140" />
        <el-table-column prop="startDate" :label="projectListText.labels.startDate" width="140" />
        <el-table-column prop="endDate" :label="projectListText.labels.endDate" width="140" />
        <el-table-column :label="projectListText.labels.actions" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="enterProject(row)">{{ projectListText.buttons.enter }}</el-button>
            <el-button v-if="row.canEdit && row.status === 'ACTIVE'" size="small" type="warning" @click="archiveProject(row)">{{ projectListText.buttons.archive }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="query.page"
          v-model:page-size="query.size"
          :total="total"
          layout="total, prev, pager, next"
          @change="loadProjects"
        />
      </div>
    </el-card>

    <el-dialog v-model="showCreate" :title="projectListText.dialogs.createProject" width="560px" @closed="resetCreateForm">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item :label="projectListText.labels.projectName" prop="name">
          <el-input v-model="createForm.name" name="createProjectName" autocomplete="off" :placeholder="projectListText.placeholders.projectName" />
        </el-form-item>
        <el-form-item :label="projectListText.labels.code" prop="code">
          <el-input v-model="createForm.code" name="createProjectCode" autocomplete="off" :placeholder="projectListText.placeholders.projectCode" />
        </el-form-item>
        <el-form-item :label="projectListText.labels.description">
          <el-input v-model="createForm.description" name="createProjectDescription" autocomplete="off" type="textarea" :rows="3" :placeholder="projectListText.placeholders.projectDescription" />
        </el-form-item>
        <el-form-item :label="projectListText.labels.visibility">
          <el-radio-group v-model="createForm.visibility">
            <el-radio v-for="visibility in visibilityOptions" :key="visibility.value" :value="visibility.value">{{ visibility.label }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item :label="projectListText.labels.dateRange">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            :range-separator="projectListText.placeholders.rangeSeparator"
            :start-placeholder="projectListText.placeholders.startDate"
            :end-placeholder="projectListText.placeholders.endDate"
            value-format="YYYY-MM-DD"
            class="full-width-control"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">{{ projectListText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="creating" @click="createProject">{{ projectListText.buttons.create }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import type { FormInstance } from 'element-plus'
import { projectApi, type Project } from '@/api/project'
import { PROJECT_LIST_I18N } from '@/constants/projectList'
import { resolveThemeLocale } from '@/constants/theme'
import { ACTION_PERMISSION_MAP } from '@/utils/permission'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'

const router = useRouter()
const projectStore = useProjectStore()
const authStore = useAuthStore()
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const projectListText = PROJECT_LIST_I18N[currentLocale]

const canCreateProject = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.projectCreate))

const loading = ref(false)
const projects = ref<Project[]>([])
const total = ref(0)
const query = reactive({
  page: 1,
  size: 20,
  keyword: '',
  status: ''
})

const showCreate = ref(false)
const creating = ref(false)
const createFormRef = ref<FormInstance>()
const dateRange = ref<string[]>([])
const createForm = reactive({
  name: '',
  code: '',
  description: '',
  visibility: 'PRIVATE' as 'PRIVATE' | 'PUBLIC',
  startDate: '',
  endDate: ''
})

const statusOptions = [...projectListText.statusOptions]
const visibilityOptions = [...projectListText.visibilityOptions]

const createRules = {
  name: [{ required: true, message: projectListText.rules.projectNameRequired, trigger: 'blur' }],
  code: [{ required: true, message: projectListText.rules.projectCodeRequired, trigger: 'blur' }]
}

function withName(template: string, name: string) {
  return template.replace('{name}', name)
}

function statusType(status: string) {
  const map: Record<string, 'success' | 'info' | 'danger'> = {
    ACTIVE: 'success',
    ARCHIVED: 'info',
    CLOSED: 'danger'
  }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  return projectListText.statusLabels[status as keyof typeof projectListText.statusLabels] || status
}

function enterProject(project: Project) {
  projectStore.setCurrentProject(project)
  void router.push(`/projects/${project.id}/overview`)
}

function resetFilters() {
  query.page = 1
  query.keyword = ''
  query.status = ''
  void loadProjects()
}

function resetCreateForm() {
  createFormRef.value?.resetFields()
  dateRange.value = []
  createForm.startDate = ''
  createForm.endDate = ''
}

async function loadProjects() {
  loading.value = true
  try {
    const response = await projectApi.list({ ...query })
    const data = (response as any).data
    projects.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function createProject() {
  try {
    await createFormRef.value?.validate()
  } catch {
    return
  }

  creating.value = true
  try {
    if (dateRange.value.length === 2) {
      createForm.startDate = dateRange.value[0] ?? ''
      createForm.endDate = dateRange.value[1] ?? ''
    }

    await projectApi.create({ ...createForm })
    ElMessage.success(projectListText.messages.projectCreated)
    showCreate.value = false
    await loadProjects()
  } finally {
    creating.value = false
  }
}

async function archiveProject(project: Project) {
  try {
    await ElMessageBox.confirm(withName(projectListText.confirms.archiveProject, project.name), projectListText.dialogs.confirmArchiveTitle, { type: 'warning' })
  } catch {
    return
  }
  await projectApi.archive(project.id)
  ElMessage.success(projectListText.messages.projectArchived)
  await loadProjects()
}

onMounted(() => {
  void loadProjects()
})
</script>

<style scoped>
.project-list-page {
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

.project-surface {
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.project-surface:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 28%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
}

.project-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.project-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.table-footer {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: flex-end;
}

.full-width-control {
  width: 100%;
}
</style>
