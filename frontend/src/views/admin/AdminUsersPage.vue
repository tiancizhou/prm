<template>
  <div class="app-page admin-users-page">
    <div class="admin-users-toolbar">
      <el-input
        v-model="searchKeyword"
        class="admin-users-search"
        :placeholder="peopleText.usersPage.filters.search"
        clearable
        :prefix-icon="Search"
      />
      <el-button v-if="canCreateUserAction" type="primary" :icon="Plus" @click="openCreateDialog">
        {{ peopleText.usersPage.buttons.addUser }}
      </el-button>
    </div>

    <div class="workspace-layout--sidebar users-layout">
      <el-card class="surface-card workspace-side-panel users-department-card" shadow="never">
        <el-tree
          :data="departmentTree"
          node-key="nodeKey"
          default-expand-all
          highlight-current
          :current-node-key="selectedNodeKey ?? undefined"
          :props="treeProps"
          @node-click="handleDepartmentClick"
        />
      </el-card>

      <el-card class="surface-card workspace-stack users-table-card" shadow="never">
        <el-table :data="pagedUsers" class="workspace-data-table users-table">
          <el-table-column :label="peopleText.usersPage.labels.name" min-width="220">
            <template #default="{ row }">
              <div class="user-identity">
                <button type="button" class="workspace-link-text workspace-link-text--primary" @click="openScopeDrawer(row)">
                  {{ row.realName || row.username || row.employeeNo || '-' }}
                </button>
                <div class="user-identity__secondary">{{ resolveUserMeta(row) }}</div>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="department" :label="peopleText.usersPage.labels.department" min-width="140" />
          <el-table-column :label="peopleText.usersPage.labels.systemRole" width="160">
            <template #default="{ row }">
              <el-tag v-if="resolvePrimaryRoleCode(row)" :type="roleTagType(resolvePrimaryRoleCode(row))" size="small">
                {{ resolvePrimaryRoleName(row) }}
              </el-tag>
              <span v-else class="users-muted-text">-</span>
            </template>
          </el-table-column>
          <el-table-column :label="peopleText.usersPage.labels.joinedProjects" min-width="140">
            <template #default="{ row }">
              <span>{{ resolveJoinedProjectSummary(row) }}</span>
            </template>
          </el-table-column>
          <el-table-column :label="peopleText.usersPage.labels.status" width="110">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small" effect="light">
                {{ resolveStatusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column :label="peopleText.usersPage.labels.actions" width="220" fixed="right">
            <template #default="{ row }">
              <div class="workspace-action-icons">
                <el-tooltip :content="scopeCopy.view" placement="top">
                  <button type="button" class="workspace-icon-action" @click="openScopeDrawer(row)"><el-icon><View /></el-icon></button>
                </el-tooltip>
                <el-tooltip v-if="canUpdateUserAction" :content="editLabel" placement="top">
                  <button type="button" class="workspace-icon-action" @click="openEditDialog(row)"><el-icon><Edit /></el-icon></button>
                </el-tooltip>
                <el-tooltip v-if="canUpdateUserAction" :content="row.status === 'ACTIVE' ? disableLabel : enableLabel" placement="top">
                  <button type="button" class="workspace-icon-action" @click="toggleStatus(row)"><el-icon><Lock /></el-icon></button>
                </el-tooltip>
                <el-tooltip v-if="canDeleteUserAction" :content="deleteLabel" placement="top">
                  <button type="button" class="workspace-icon-action" @click="removeUser(row)"><el-icon><Delete /></el-icon></button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <div class="table-footer">
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="filteredUsers.length"
            layout="total, sizes, prev, pager, next"
          />
        </div>
      </el-card>
    </div>

    <el-dialog v-model="showDialog" :title="dialogTitle" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item :label="usernameLabel" required><el-input v-model="form.username" :disabled="dialogMode === 'edit'" /></el-form-item>
        <el-form-item v-if="dialogMode === 'create'" :label="passwordLabel" required><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item :label="nameLabel"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item :label="employeeNoLabel"><el-input v-model="form.employeeNo" /></el-form-item>
        <el-form-item :label="departmentLabel">
          <el-select v-model="form.departmentId" clearable>
            <el-option v-for="department in departmentOptions" :key="department.id" :label="department.name" :value="department.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="peopleText.usersPage.labels.systemRole">
          <el-select v-model="form.roleIds" multiple collapse-tags collapse-tags-tooltip class="form-full-width">
            <el-option v-for="role in allRoles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
        <el-form-item :label="phoneLabel"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item :label="emailLabel"><el-input v-model="form.email" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showDialog = false">{{ peopleText.usersPage.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="saving" @click="submitDialog">{{ dialogMode === 'create' ? peopleText.usersPage.buttons.create : peopleText.usersPage.buttons.save }}</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="showScopeDrawer" :title="scopeCopy.title" size="420px">
      <div v-loading="scopeLoading" class="user-scope-drawer">
        <template v-if="selectedUserScope">
          <section class="user-scope-section">
            <div class="user-scope-name">{{ selectedUserScope.displayName || selectedUserScope.username || '-' }}</div>
            <div class="user-scope-meta">{{ [selectedUserScope.username, selectedUserScope.employeeNo].filter(Boolean).join(' · ') || '-' }}</div>
          </section>

          <section class="user-scope-section">
            <div class="user-scope-section__title">{{ scopeCopy.systemRole }}</div>
            <div class="user-scope-role-list">
              <el-tag v-for="roleCode in selectedUserScope.roles || []" :key="roleCode" :type="roleTagType(roleCode)" size="small">
                {{ roleNameMap[roleCode] || roleCode }}
              </el-tag>
              <span v-if="!(selectedUserScope.roles || []).length" class="users-muted-text">-</span>
            </div>
          </section>

          <section class="user-scope-section">
            <div class="user-scope-section__title">{{ scopeCopy.projectScope }}</div>
            <div v-if="selectedUserScope.joinedProjects?.length" class="user-scope-project-list">
              <div v-for="project in selectedUserScope.joinedProjects" :key="project.id" class="user-scope-project-item">
                <RouterLink :to="`/projects/${project.id}/overview`" class="workspace-link-text workspace-link-text--primary">
                  {{ project.name }}
                </RouterLink>
                <div class="user-scope-project-item__code">{{ project.code }}</div>
              </div>
            </div>
            <div v-else class="users-muted-text">{{ scopeCopy.noJoinedProjects }}</div>
          </section>
        </template>
      </div>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { Delete, Edit, Lock, Plus, Search, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/api/http'
import { adminDepartmentApi, type DepartmentTreeNode } from '@/api/adminDepartment'
import { ADMIN_PEOPLE_I18N } from '@/constants/adminPeople'
import { getRoleGroupTagType, type RoleGroupTagType } from '@/constants/roleGroupCode'
import { resolveThemeLocale } from '@/constants/theme'
import { useAuthStore } from '@/stores/auth'
import { ACTION_PERMISSION_MAP } from '@/utils/permission'

interface Role { id: number; name: string; code: string; tagType?: RoleGroupTagType }
interface UserProjectScope {
  userId: number
  displayName: string
  username: string
  employeeNo: string
  roles: string[]
  joinedProjects: Array<{ id: number; name: string; code: string }>
}

const treeProps = { label: 'name', children: 'children' }
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const peopleText = ADMIN_PEOPLE_I18N[currentLocale]
const authStore = useAuthStore()

const usernameLabel = peopleText.usersPage.labels.username
const passwordLabel = peopleText.usersPage.labels.password
const nameLabel = peopleText.usersPage.labels.name
const employeeNoLabel = peopleText.usersPage.labels.employeeNo
const departmentLabel = peopleText.usersPage.labels.department
const phoneLabel = peopleText.usersPage.labels.phone
const emailLabel = peopleText.usersPage.labels.email
const editLabel = peopleText.usersPage.buttons.edit
const deleteLabel = peopleText.usersPage.buttons.delete
const enableLabel = peopleText.usersPage.buttons.enable
const disableLabel = peopleText.usersPage.buttons.disable

const departmentTree = ref<DepartmentTreeNode[]>([])
const selectedNodeKey = ref<string | null>(null)
const selectedDepartmentId = ref<number | null>(null)
const users = ref<any[]>([])
const allRoles = ref<Role[]>([])
const searchKeyword = ref('')
const page = ref(1)
const pageSize = ref(10)
const showDialog = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const saving = ref(false)
const showScopeDrawer = ref(false)
const scopeLoading = ref(false)
const selectedUserScope = ref<UserProjectScope | null>(null)
const isZh = currentLocale === 'zh-CN'

const form = reactive({
  id: null as number | null,
  username: '',
  password: '',
  realName: '',
  employeeNo: '',
  departmentId: null as number | null,
  phone: '',
  email: '',
  roleIds: [] as number[]
})

const departmentOptions = computed(() => flattenNodes(departmentTree.value).filter((item) => item.nodeType === 'DEPARTMENT'))
const roleNameMap = computed<Record<string, string>>(() => Object.fromEntries(allRoles.value.map((role) => [role.code, role.name])))
const roleTagTypeMapFromApi = computed<Record<string, RoleGroupTagType>>(() =>
  Object.fromEntries(
    allRoles.value
      .filter((r) => r.tagType)
      .map((r) => [r.code, r.tagType!])
  )
)
const dialogTitle = computed(() => dialogMode.value === 'create'
  ? peopleText.usersPage.dialogs.createUser
  : peopleText.usersPage.dialogs.editUser)
const canCreateUserAction = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.userCreate))
const canUpdateUserAction = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.userUpdate))
const canDeleteUserAction = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.userDelete))
const scopeCopy = computed(() => isZh
  ? {
      title: '项目范围',
      view: '查看项目范围',
      systemRole: '系统角色',
      projectScope: '已加入项目',
      noJoinedProjects: '当前未加入任何项目，因此暂时看不到项目业务数据'
    }
  : {
      title: 'Project Scope',
      view: 'View Project Scope',
      systemRole: 'System Role',
      projectScope: 'Joined Projects',
      noJoinedProjects: 'This user has not joined any project yet, so no project business data is available.'
    })
const filteredUsers = computed(() => {
  const kw = searchKeyword.value?.trim() || ''
  if (!kw) return users.value
  return users.value.filter((item) =>
    [item.realName, item.username, item.employeeNo].some((v) => String(v || '').includes(kw))
  )
})

const pagedUsers = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredUsers.value.slice(start, start + pageSize.value)
})

function resolvePrimaryRoleCode(row: any) {
  return Array.isArray(row.roles) && row.roles.length ? row.roles[0] : ''
}

function resolvePrimaryRoleName(row: any) {
  const roleCode = resolvePrimaryRoleCode(row)
  return roleCode ? (roleNameMap.value[roleCode] ?? roleCode) : '-'
}

function resolveUserMeta(row: any) {
  return [row.username, row.employeeNo].filter(Boolean).join(' / ') || '-'
}

function resolveJoinedProjectSummary(row: any) {
  const count = Number(row.joinedProjectCount || 0)
  if (count <= 0) {
    return isZh ? '未加入项目' : 'No joined projects'
  }
  return isZh ? `已加入 ${count} 个项目` : `${count} joined project${count > 1 ? 's' : ''}`
}

function resolveStatusLabel(status: string) {
  return status === 'ACTIVE'
    ? peopleText.usersPage.labels.active
    : peopleText.usersPage.labels.disabled
}

function statusTagType(status: string) {
  return status === 'ACTIVE' ? 'success' : 'info'
}

function roleTagType(roleCode: string): RoleGroupTagType {
  return roleTagTypeMapFromApi.value[roleCode] || getRoleGroupTagType(roleCode)
}

async function openScopeDrawer(row: any) {
  showScopeDrawer.value = true
  scopeLoading.value = true
  selectedUserScope.value = null
  try {
    const response: any = await http.get(`/system/users/${row.id}/project-scope`)
    selectedUserScope.value = response.data || null
  } finally {
    scopeLoading.value = false
  }
}

async function loadDepartmentTree(preferredNodeKey?: string | null) {
  const response: any = await adminDepartmentApi.tree()
  departmentTree.value = response.data || []

  const nextNode = findNodeByKey(preferredNodeKey ?? selectedNodeKey.value, departmentTree.value)
    ?? findFirstDepartment(departmentTree.value)
    ?? departmentTree.value[0]

  if (!nextNode) {
    selectedNodeKey.value = null
    selectedDepartmentId.value = null
    return
  }

  applySelection(nextNode)

  if (!form.departmentId) {
    form.departmentId = departmentOptions.value[0]?.id ?? null
  }
}

function applySelection(node: DepartmentTreeNode) {
  selectedNodeKey.value = node.nodeKey
  selectedDepartmentId.value = node.nodeType === 'DEPARTMENT' ? node.id : null
}

async function loadUsers() {
  const response: any = await http.get('/system/users', {
    params: {
      page: 1,
      size: 200,
      departmentId: selectedDepartmentId.value ?? undefined
    }
  })
  users.value = response.data?.records || []
}

async function loadRoles() {
  const response: any = await http.get('/system/users/roles')
  allRoles.value = response.data || []
}

function handleDepartmentClick(node: DepartmentTreeNode) {
  applySelection(node)
  page.value = 1
  void loadUsers()
}

function openCreateDialog() {
  if (!canCreateUserAction.value) return
  dialogMode.value = 'create'
  form.id = null
  form.username = ''
  form.password = ''
  form.realName = ''
  form.employeeNo = ''
  form.departmentId = selectedDepartmentId.value ?? departmentOptions.value[0]?.id ?? null
  form.phone = ''
  form.email = ''
  form.roleIds = []
  showDialog.value = true
}

function openEditDialog(row: any) {
  if (!canUpdateUserAction.value) return
  dialogMode.value = 'edit'
  form.id = row.id
  form.username = row.username || ''
  form.password = ''
  form.realName = row.realName || ''
  form.employeeNo = row.employeeNo || ''
  form.departmentId = row.departmentId ?? null
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.roleIds = row.roleIds || []
  showDialog.value = true
}

async function submitDialog() {
  if (dialogMode.value === 'create' && !canCreateUserAction.value) return
  if (dialogMode.value === 'edit' && !canUpdateUserAction.value) return
  if (!form.username.trim() || (dialogMode.value === 'create' && !form.password.trim())) {
    ElMessage.warning(peopleText.usersPage.messages.requiredUsernamePassword)
    return
  }

  saving.value = true
  try {
    if (dialogMode.value === 'create') {
      await http.post('/system/users', {
        username: form.username.trim(),
        password: form.password,
        realName: form.realName,
        employeeNo: form.employeeNo,
        departmentId: form.departmentId,
        team: null,
        email: form.email,
        phone: form.phone,
        roleIds: form.roleIds
      })
      ElMessage.success(peopleText.usersPage.messages.created)
    } else if (form.id) {
      await http.put(`/system/users/${form.id}`, {
        realName: form.realName,
        employeeNo: form.employeeNo,
        departmentId: form.departmentId,
        team: null,
        email: form.email,
        phone: form.phone,
        roleIds: form.roleIds
      })
      ElMessage.success(peopleText.usersPage.messages.saved)
    }

    showDialog.value = false
    await loadUsers()
  } finally {
    saving.value = false
  }
}

async function toggleStatus(row: any) {
  if (!canUpdateUserAction.value) return
  const nextStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  await http.put(`/system/users/${row.id}/status`, null, { params: { status: nextStatus } })
  ElMessage.success(peopleText.usersPage.messages.updated)
  await loadUsers()
}

async function removeUser(row: any) {
  if (!canDeleteUserAction.value) return
  try {
    await ElMessageBox.confirm(peopleText.usersPage.messages.deleteConfirm, peopleText.usersPage.messages.deleteTitle, { type: 'warning' })
  } catch {
    return
  }

  await http.delete(`/system/users/${row.id}`)
  ElMessage.success(peopleText.usersPage.messages.deleted)
  await loadUsers()
}

function flattenNodes(nodes: DepartmentTreeNode[]): DepartmentTreeNode[] {
  return nodes.flatMap((node) => [node, ...flattenNodes(node.children || [])])
}

function findNodeByKey(nodeKey: string | null | undefined, nodes: DepartmentTreeNode[]): DepartmentTreeNode | null {
  if (!nodeKey) return null
  for (const node of nodes) {
    if (node.nodeKey === nodeKey) return node
    const child = findNodeByKey(nodeKey, node.children || [])
    if (child) return child
  }
  return null
}

function findFirstDepartment(nodes: DepartmentTreeNode[]): DepartmentTreeNode | null {
  for (const node of nodes) {
    if (node.nodeType === 'DEPARTMENT') return node
    const child = findFirstDepartment(node.children || [])
    if (child) return child
  }
  return null
}

watch([searchKeyword, pageSize], () => {
  page.value = 1
})

onMounted(async () => {
  await Promise.all([loadDepartmentTree(), loadRoles()])
  await loadUsers()
})
</script>

<style scoped>
.admin-users-page { min-width: 0; }

.admin-users-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
}

.admin-users-search {
  width: 260px;
}

.users-layout { min-width: 0; }

.users-department-card :deep(.el-card__body) {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  min-height: 200px;
}

.users-department-card :deep(.el-tree) {
  min-height: 180px;
  flex: 1;
}

.users-department-card :deep(.el-tree-node__content) {
  padding: 4px 0;
}

.users-department-card,
.users-table-card { min-width: 0; }

.users-table-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  min-width: 0;
}

.table-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--space-sm);
}

.users-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.users-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.user-identity {
  display: grid;
  gap: 2px;
}

.user-identity__secondary,
.users-muted-text {
  font-size: 12px;
  line-height: 1.4;
  color: var(--app-text-muted);
}

.form-full-width {
  width: 100%;
}
.user-scope-drawer {
  display: grid;
  gap: var(--space-lg);
  min-height: 160px;
}
.user-scope-section {
  display: grid;
  gap: var(--space-sm);
}
.user-scope-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--app-text-primary);
}
.user-scope-meta,
.user-scope-project-item__code {
  font-size: 12px;
  color: var(--app-text-muted);
}
.user-scope-section__title {
  font-size: 13px;
  font-weight: 600;
  color: var(--app-text-secondary);
}
.user-scope-role-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.user-scope-project-list {
  display: grid;
  gap: var(--space-sm);
}
.user-scope-project-item {
  padding: 12px;
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius-sm);
  background: var(--app-bg-surface);
}

@media (max-width: 1023px) {
  .users-layout {
    flex-direction: column;
  }

  .users-department-list {
    width: 100%;
  }
}

</style>
