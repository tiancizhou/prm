<template>
  <div class="app-page project-members-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ membersText.pageTitle }}</h1>
        <p class="page-subtitle">{{ membersText.pageSubtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button v-if="canManageMembers" type="primary" :icon="Plus" @click="openAdd">{{ membersText.buttons.addMember }}</el-button>
      </div>
    </header>

    <div class="scope-rule-banner">
      {{ membersText.helperText }}
    </div>

    <el-card class="surface-card members-surface" shadow="never">
      <el-table :data="members" v-loading="loading" class="members-table">
        <el-table-column :label="membersText.labels.member" min-width="260">
          <template #default="{ row }">
            <div class="member-identity">
              <RouterLink
                :to="{ path: `/organization/team/${row.userId}`, query: { from: 'project-members', projectId: String(projectId) } }"
                class="workspace-link-text workspace-link-text--primary"
              >
                {{ row.nickname || row.username || row.employeeNo || membersText.labels.noneSymbol }}
              </RouterLink>
              <div class="member-identity__secondary">
                {{ [row.username, row.employeeNo].filter(Boolean).join(' / ') || membersText.labels.noneSymbol }}
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="membersText.labels.systemRole" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.roleName" :type="roleTagType(row.role)" size="small">{{ row.roleName }}</el-tag>
            <span v-else class="text-muted">{{ membersText.labels.noneSymbol }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="membersText.labels.joinedAt" min-width="170">
          <template #default="{ row }">
            <span>{{ formatJoinedAt(row.joinedAt) }}</span>
          </template>
        </el-table-column>
        <el-table-column v-if="canManageMembers" :label="membersText.labels.actions" width="120" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="removeMember(row)">{{ membersText.buttons.remove }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" :title="membersText.dialogs.addMember" width="480px">
      <el-form :model="addForm" label-width="96px">
        <el-form-item :label="membersText.labels.selectMember" required>
          <el-select
            v-model="addForm.userId"
            :aria-label="membersText.aria.searchAddableMembers"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            :placeholder="membersText.placeholders.searchMembers"
            class="form-full-width"
          >
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="`${user.realName || user.username}${user.employeeNo ? ' (' + user.employeeNo + ')' : ''}`"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">{{ membersText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="adding" @click="addMember">{{ membersText.buttons.add }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'
import { projectApi } from '@/api/project'
import { PROJECT_MEMBERS_I18N } from '@/constants/projectMembers'
import { getRoleGroupTagType } from '@/constants/roleGroupCode'
import { resolveThemeLocale } from '@/constants/theme'
import { useProjectStore } from '@/stores/project'

const route = useRoute()
const projectId = Number(route.params.id)
const projectStore = useProjectStore()
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const membersText = PROJECT_MEMBERS_I18N[currentLocale]

const loading = ref(false)
const members = ref<any[]>([])
const allRoles = ref<Array<{ code: string; tagType?: string }>>([])

const showAdd = ref(false)
const adding = ref(false)
const addForm = reactive({ userId: null as number | null })

const userOptions = ref<any[]>([])
const userSearchLoading = ref(false)

const canManageMembers = computed(() => {
  const currentProject = projectStore.currentProject
  return currentProject?.id === projectId && currentProject?.canEdit === true
})

const roleTagTypeMapFromApi = computed<Record<string, string>>(() =>
  Object.fromEntries(
    allRoles.value
      .filter((r) => r.tagType)
      .map((r) => [r.code, r.tagType!])
  )
)

function withName(template: string, name: string) {
  return template.replace('{name}', name)
}

function roleTagType(code: string) {
  return roleTagTypeMapFromApi.value[code] || getRoleGroupTagType(code)
}

function formatJoinedAt(value?: string | null) {
  if (!value) {
    return membersText.labels.noneSymbol
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return membersText.labels.noneSymbol
  }
  return date.toLocaleString(currentLocale)
}

async function loadMembers() {
  loading.value = true
  try {
    const response = await projectApi.getMembers(projectId)
    members.value = (response as any).data || []
  } finally {
    loading.value = false
  }
}

async function ensureProjectPermission() {
  const currentProject = projectStore.currentProject
  if (currentProject?.id === projectId && typeof currentProject.canEdit === 'boolean') {
    return
  }
  const response = await projectApi.get(projectId)
  projectStore.setCurrentProject((response as any).data)
}

async function searchUsers(query: string) {
  if (!query) {
    userOptions.value = []
    return
  }
  userSearchLoading.value = true
  try {
    const response: any = await http.get('/system/users', { params: { keyword: query, page: 1, size: 20 } })
    userOptions.value = response.data?.records || []
  } finally {
    userSearchLoading.value = false
  }
}

function openAdd() {
  if (!canManageMembers.value) {
    ElMessage.warning(membersText.messages.noPermission)
    return
  }
  addForm.userId = null
  userOptions.value = []
  showAdd.value = true
}

async function addMember() {
  if (!canManageMembers.value) {
    ElMessage.warning(membersText.messages.noPermission)
    return
  }
  if (!addForm.userId) {
    ElMessage.warning(membersText.messages.selectMemberFirst)
    return
  }
  adding.value = true
  try {
    await projectApi.addMember(projectId, addForm.userId)
    ElMessage.success(membersText.messages.added)
    showAdd.value = false
    await loadMembers()
  } finally {
    adding.value = false
  }
}

async function removeMember(member: any) {
  if (!canManageMembers.value) {
    ElMessage.warning(membersText.messages.noPermission)
    return
  }
  const displayName = member.nickname || member.username
  try {
    await ElMessageBox.confirm(withName(membersText.confirms.removeMember, displayName), membersText.dialogs.confirmTitle, { type: 'warning' })
  } catch {
    return
  }
  await projectApi.removeMember(projectId, member.userId)
  ElMessage.success(membersText.messages.removed)
  await loadMembers()
}

onMounted(() => {
  void (async () => {
    const rolesRes: any = await http.get('/system/users/roles')
    allRoles.value = rolesRes?.data ?? []
    await ensureProjectPermission()
    await loadMembers()
  })()
})
</script>

<style scoped>
.project-members-page {
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

.members-surface {
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.members-surface:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 28%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.scope-rule-banner {
  padding: 12px 14px;
  border: 1px solid color-mix(in srgb, var(--app-color-primary) 18%, var(--app-border-soft));
  border-radius: var(--app-radius-md);
  background: color-mix(in srgb, var(--app-color-primary-soft) 58%, white);
  color: var(--app-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.members-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.member-identity {
  display: grid;
  gap: 4px;
}

.member-identity__secondary {
  font-size: 12px;
  color: var(--app-text-muted);
}

.members-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.text-muted {
  color: var(--app-text-muted);
  font-size: 12px;
}

.form-full-width {
  width: 100%;
}
</style>
