<template>
  <div class="app-page admin-permissions-page">
    <div class="admin-permissions-toolbar">
      <div class="workspace-toolbar-right">
        <el-button v-if="canAssignPermission" plain @click="() => openAssignDialog()">{{ peopleText.permissionsPage.buttons.assignByModule }}</el-button>
        <el-button v-if="canCreateRoleGroup" type="primary" :icon="Plus" @click="openCreateDialog">{{ peopleText.permissionsPage.buttons.addGroup }}</el-button>
      </div>
    </div>

    <el-card class="surface-card permissions-surface" shadow="never">
      <el-table :data="roleRows" v-loading="loading" class="workspace-data-table permissions-table">
        <el-table-column prop="id" :label="peopleText.permissionsPage.labels.id" width="70" />
        <el-table-column prop="name" :label="peopleText.permissionsPage.labels.groupName" min-width="160" />
        <el-table-column prop="code" :label="codeLabel" width="140" show-overflow-tooltip />
        <el-table-column prop="description" :label="peopleText.permissionsPage.labels.description" min-width="200" />
        <el-table-column prop="users" :label="peopleText.permissionsPage.labels.users" min-width="340">
          <template #default="{ row }">
            <span>{{ row.users || emptyMembersText }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="peopleText.permissionsPage.labels.actions" width="220" fixed="right">
          <template #default="{ row }">
            <div class="workspace-action-icons permission-action-group">
              <el-tooltip :content="peopleText.permissionsPage.tooltips.view" placement="top">
                <button type="button" class="workspace-icon-action" @click="openViewDialog(row)"><el-icon><View /></el-icon></button>
              </el-tooltip>
              <el-tooltip v-if="canAssignPermission" :content="peopleText.permissionsPage.tooltips.lock" placement="top">
                <button type="button" class="workspace-icon-action" @click="openAssignDialog(row.id)"><el-icon><Lock /></el-icon></button>
              </el-tooltip>
              <el-tooltip :content="peopleText.permissionsPage.tooltips.members" placement="top">
                <button type="button" class="workspace-icon-action" @click="openMembersDialog(row)"><el-icon><User /></el-icon></button>
              </el-tooltip>
              <el-tooltip v-if="canUpdateRoleGroup" :content="peopleText.permissionsPage.tooltips.edit" placement="top">
                <button type="button" class="workspace-icon-action" @click="openEditDialog(row)"><el-icon><Edit /></el-icon></button>
              </el-tooltip>
              <el-tooltip :content="peopleText.permissionsPage.tooltips.copy" placement="top">
                <button type="button" class="workspace-icon-action" @click="notifyCopyReserved"><el-icon><DocumentCopy /></el-icon></button>
              </el-tooltip>
              <el-tooltip v-if="canDeleteRoleGroup" :content="peopleText.permissionsPage.tooltips.delete" placement="top">
                <button type="button" class="workspace-icon-action" :disabled="Boolean(row.memberCount)" @click="removeRole(row)"><el-icon><Delete /></el-icon></button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showViewDialog" :title="viewDialogTitle" width="680px">
      <div v-if="selectedRoleDetail">
        <div class="role-detail-grid">
          <div class="workspace-summary-card">
            <div class="workspace-summary-label">{{ peopleText.permissionsPage.labels.groupName }}</div>
            <div class="workspace-summary-value">{{ selectedRoleDetail.name }}</div>
          </div>
          <div class="workspace-summary-card">
            <div class="workspace-summary-label">{{ codeLabel }}</div>
            <div class="workspace-summary-value">{{ selectedRoleDetail.code }}</div>
          </div>
        </div>
        <div class="workspace-helper-text">{{ selectedRoleDetail.description }}</div>
        <div class="role-member-list">
          <div v-for="member in selectedRoleDetail.members" :key="member.id" class="workspace-member-row">
            <span class="workspace-member-primary">{{ member.displayName }}</span>
            <span class="workspace-member-secondary">{{ member.username }}</span>
          </div>
          <div v-if="!selectedRoleDetail.members.length" class="workspace-empty-text">{{ emptyMembersText }}</div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="showMembersDialog" :title="membersDialogTitle" width="560px">
      <div class="role-member-list">
        <div v-for="member in selectedRoleDetail?.members || []" :key="member.id" class="workspace-member-row">
          <span class="workspace-member-primary">{{ member.displayName }}</span>
          <span class="workspace-member-secondary">{{ member.username }}</span>
        </div>
        <div v-if="!(selectedRoleDetail?.members?.length)" class="workspace-empty-text">{{ emptyMembersText }}</div>
      </div>
    </el-dialog>

    <el-dialog v-model="showFormDialog" :title="formDialogTitle" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item :label="peopleText.permissionsPage.labels.groupName" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item :label="codeLabel" required>
          <el-input v-model="form.code" :disabled="formMode === 'edit'" />
        </el-form-item>
        <el-form-item :label="tagTypeLabel">
          <el-select v-model="form.tagType" :placeholder="tagTypeLabel" clearable class="form-tag-type-select">
            <el-option
              v-for="opt in tagTypeOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="peopleText.permissionsPage.labels.description">
          <el-input v-model="form.description" type="textarea" :rows="4" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showFormDialog = false">{{ cancelLabel }}</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">{{ saveLabel }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showAssignDialog" :title="assignDialogTitle" width="980px">
      <div class="permission-assignment-layout">
        <aside class="surface-card permission-role-list">
          <div class="workspace-panel-head permission-role-list-head">
            <h3 class="workspace-panel-title">{{ roleGroupListTitle }}</h3>
          </div>
          <button
            v-for="role in roleRows"
            :key="role.id"
            type="button"
            class="permission-role-item"
            :class="{ 'is-active': selectedRoleId === role.id }"
            @click="selectRoleForAssignment(role.id)"
          >
            <span class="permission-role-name">{{ role.name }}</span>
            <span class="permission-role-meta">{{ role.memberCount }} {{ memberCountSuffix }}</span>
          </button>
        </aside>

        <section class="surface-card permission-module-panel">
          <div class="permission-panel-head">
            <div>
              <h3 class="workspace-panel-title">{{ modulePanelTitle }}</h3>
              <div class="workspace-helper-text">{{ modulePanelHint }}</div>
            </div>
            <div v-if="selectedRoleForAssign" class="permission-panel-role-summary">
              <el-tag type="primary" effect="light">{{ selectedRoleForAssign.name }}</el-tag>
              <span class="permission-selected-count">{{ selectedPermissionCountLabel }}</span>
            </div>
          </div>

          <div v-if="selectedRoleForAssign && modulePermissions.length" class="permission-panel-actions">
            <button type="button" class="permission-panel-btn" @click="expandAllModules">{{ assignExpandAllLabel }}</button>
            <span class="permission-panel-sep">|</span>
            <button type="button" class="permission-panel-btn" @click="collapseAllModules">{{ assignCollapseAllLabel }}</button>
            <span class="permission-panel-sep">|</span>
            <button type="button" class="permission-panel-btn" @click="selectAllPermissions">{{ assignSelectAllLabel }}</button>
            <span class="permission-panel-sep">|</span>
            <button type="button" class="permission-panel-btn" @click="clearAllPermissions">{{ assignClearAllLabel }}</button>
          </div>

          <p v-if="selectedRoleForAssign && selectedPermissionCodes.length === 0" class="permission-empty-hint">
            {{ assignEmptyHint }}
          </p>

          <div class="permission-module-scroll">
            <div class="permission-module-grid">
              <section v-for="module in modulePermissions" :key="module.code" class="permission-module-card">
                <div
                  class="permission-module-head"
                  @click="toggleModuleExpanded(module.code)"
                >
                  <div class="permission-module-head-left">
                    <el-icon class="permission-module-chevron" :class="{ 'is-expanded': isModuleExpanded(module.code) }">
                      <ArrowRight />
                    </el-icon>
                    <el-checkbox
                      :model-value="hasSelectedPermission(module.code)"
                      @change="(checked) => toggleModule(module, checked)"
                      @click.stop
                    >
                      {{ module.name }}
                    </el-checkbox>
                    <span v-if="module.children?.length" class="permission-module-meta">
                      ({{ module.children.length }} {{ assignActionLabel }})
                    </span>
                  </div>
                  <span class="permission-module-code" :title="module.code">{{ module.code }}</span>
                </div>
                <div v-show="isModuleExpanded(module.code)" v-if="module.children?.length" class="permission-action-grid">
                  <label v-for="action in module.children" :key="action.code" class="permission-action-item">
                    <el-checkbox :model-value="hasSelectedPermission(action.code)" @change="(checked) => toggleAction(module, action, checked)">
                      {{ action.name }}
                    </el-checkbox>
                    <span class="permission-action-code" :title="action.code">{{ action.code }}</span>
                  </label>
                </div>
              </section>
            </div>
          </div>
        </section>
      </div>
      <template #footer>
        <el-button @click="showAssignDialog = false">{{ cancelLabel }}</el-button>
        <el-button type="primary" :loading="savingAssignments" :disabled="!selectedRoleId" @click="saveAssignments">{{ assignSaveLabel }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ArrowRight, Delete, DocumentCopy, Edit, Lock, Plus, User, View } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminPermissionApi, type ModulePermission } from '@/api/adminPermission'
import { adminRoleApi, type RoleGroupDetail, type RoleGroupRow, type RoleTagType } from '@/api/adminRole'
import { ADMIN_PEOPLE_I18N } from '@/constants/adminPeople'
import { resolveThemeLocale } from '@/constants/theme'
import { useAuthStore } from '@/stores/auth'
import { ACTION_PERMISSION_MAP } from '@/utils/permission'

const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const peopleText = ADMIN_PEOPLE_I18N[currentLocale]
const isZh = currentLocale === 'zh-CN'
const authStore = useAuthStore()

const loading = ref(false)
const saving = ref(false)
const roleRows = ref<RoleGroupRow[]>([])
const selectedRoleDetail = ref<RoleGroupDetail | null>(null)
const showViewDialog = ref(false)
const showMembersDialog = ref(false)
const showFormDialog = ref(false)
const showAssignDialog = ref(false)
const formMode = ref<'create' | 'edit'>('create')
const savingAssignments = ref(false)
const modulePermissions = ref<ModulePermission[]>([])
const selectedPermissionCodes = ref<string[]>([])
const selectedRoleId = ref<number | null>(null)
const expandedModules = ref<Set<string>>(new Set())

const TAG_TYPE_OPTIONS: { value: RoleTagType; labelZh: string; labelEn: string }[] = [
  { value: 'danger', labelZh: '危险(红)', labelEn: 'Danger (red)' },
  { value: 'warning', labelZh: '警告(橙)', labelEn: 'Warning (orange)' },
  { value: 'primary', labelZh: '主要(蓝)', labelEn: 'Primary (blue)' },
  { value: 'success', labelZh: '成功(绿)', labelEn: 'Success (green)' },
  { value: 'info', labelZh: '信息(灰)', labelEn: 'Info (gray)' }
]

const form = reactive({
  id: null as number | null,
  name: '',
  code: '',
  tagType: 'info' as RoleTagType | '',
  description: ''
})

const tagTypeOptions = computed(() =>
  TAG_TYPE_OPTIONS.map((opt) => ({ value: opt.value, label: isZh ? opt.labelZh : opt.labelEn }))
)
const tagTypeLabel = computed(() => (isZh ? '标签样式' : 'Tag Style'))

const copy = computed(() => isZh
  ? {
      codeLabel: '分组编码',
      viewDialogTitle: '分组详情',
      membersDialogTitle: '成员列表',
      createDialogTitle: '新增分组',
      editDialogTitle: '编辑分组',
      emptyMembersText: '暂无成员',
      deleteConfirm: '确认删除当前分组？若分组下仍有用户，将无法删除。',
      deleteTitle: '提示',
      moduleComingSoon: '按模块分配权限即将完善',
      copyComingSoon: '复制分组功能即将完善',
      requiredNameCode: '分组名称和编码不能为空',
      created: '分组已创建',
      updated: '分组已保存',
      deleted: '分组已删除',
      cancel: '取消',
      save: '保存'
    }
  : {
      codeLabel: 'Group Code',
      viewDialogTitle: 'Group Detail',
      membersDialogTitle: 'Members',
      createDialogTitle: 'Add Group',
      editDialogTitle: 'Edit Group',
      emptyMembersText: 'No members',
      deleteConfirm: 'Delete this group? It cannot be deleted when members are still bound.',
      deleteTitle: 'Notice',
      moduleComingSoon: 'Module-based permission assignment is coming soon',
      copyComingSoon: 'Group copy is coming soon',
      requiredNameCode: 'Group name and code are required',
      created: 'Group created',
      updated: 'Group saved',
      deleted: 'Group deleted',
      cancel: 'Cancel',
      save: 'Save'
    })

const codeLabel = computed(() => copy.value.codeLabel)
const viewDialogTitle = computed(() => copy.value.viewDialogTitle)
const membersDialogTitle = computed(() => copy.value.membersDialogTitle)
const formDialogTitle = computed(() => formMode.value === 'create' ? copy.value.createDialogTitle : copy.value.editDialogTitle)
const emptyMembersText = computed(() => copy.value.emptyMembersText)
const cancelLabel = computed(() => copy.value.cancel)
const saveLabel = computed(() => copy.value.save)
const assignDialogTitle = computed(() => isZh ? '按模块分配权限' : 'Assign Permissions by Module')
const roleGroupListTitle = computed(() => isZh ? '角色分组' : 'Role Groups')
const modulePanelTitle = computed(() => isZh ? '模块与动作权限' : 'Module and Action Permissions')
const modulePanelHint = computed(() => isZh ? '勾选动作会自动勾选所属模块；取消模块会同时取消其下动作。' : 'Selecting an action also selects its parent module. Clearing a module clears its child actions.')
const memberCountSuffix = computed(() => isZh ? '成员' : 'members')
const assignSaveLabel = computed(() => isZh ? '保存权限分配' : 'Save Permission Assignment')
const selectedRoleForAssign = computed(() => roleRows.value.find((role) => role.id === selectedRoleId.value) ?? null)
const selectedPermissionCountLabel = computed(() => {
  const n = selectedPermissionCodes.value.length
  return isZh ? `已选 ${n} 项` : `${n} selected`
})
const assignEmptyHint = computed(() =>
  isZh
    ? '当前该分组未分配任何权限，勾选下方模块/动作后点击保存即可分配。'
    : 'This group has no permissions assigned. Check the modules/actions below and click Save to assign.'
)
const assignExpandAllLabel = computed(() => (isZh ? '全部展开' : 'Expand all'))
const assignCollapseAllLabel = computed(() => (isZh ? '全部折叠' : 'Collapse all'))
const assignSelectAllLabel = computed(() => (isZh ? '全选' : 'Select all'))
const assignClearAllLabel = computed(() => (isZh ? '清空' : 'Clear all'))
const assignActionLabel = computed(() => (isZh ? '项' : 'items'))

function isModuleExpanded(code: string) {
  return expandedModules.value.has(code)
}

function toggleModuleExpanded(code: string) {
  const next = new Set(expandedModules.value)
  if (next.has(code)) next.delete(code)
  else next.add(code)
  expandedModules.value = next
}

function expandAllModules() {
  expandedModules.value = new Set(modulePermissions.value.map((m) => m.code))
}

function collapseAllModules() {
  expandedModules.value = new Set()
}

function selectAllPermissions() {
  const codes = new Set<string>()
  for (const m of modulePermissions.value) {
    codes.add(m.code)
    for (const c of m.children || []) codes.add(c.code)
  }
  selectedPermissionCodes.value = [...codes]
}

function clearAllPermissions() {
  selectedPermissionCodes.value = []
}
const canCreateRoleGroup = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.roleGroupCreate))
const canUpdateRoleGroup = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.roleGroupUpdate))
const canDeleteRoleGroup = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.roleGroupDelete))
const canAssignPermission = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.permissionAssign))

async function loadRoles() {
  loading.value = true
  try {
    const response: any = await adminRoleApi.list()
    roleRows.value = response.data || []
  } finally {
    loading.value = false
  }
}

async function loadModulePermissions() {
  const response: any = await adminPermissionApi.listModules()
  modulePermissions.value = response.data || []
}

function hasSelectedPermission(code: string) {
  return selectedPermissionCodes.value.includes(code)
}

async function ensureRoleDetail(roleId: number) {
  const response: any = await adminRoleApi.get(roleId)
  selectedRoleDetail.value = response.data || null
}

async function openViewDialog(row: RoleGroupRow) {
  await ensureRoleDetail(row.id)
  showViewDialog.value = true
}

async function openMembersDialog(row: RoleGroupRow) {
  await ensureRoleDetail(row.id)
  showMembersDialog.value = true
}

function openCreateDialog() {
  if (!canCreateRoleGroup.value) return
  formMode.value = 'create'
  form.id = null
  form.name = ''
  form.code = ''
  form.tagType = 'info'
  form.description = ''
  showFormDialog.value = true
}

async function openEditDialog(row: RoleGroupRow) {
  if (!canUpdateRoleGroup.value) return
  await ensureRoleDetail(row.id)
  formMode.value = 'edit'
  form.id = row.id
  form.name = selectedRoleDetail.value?.name || row.name
  form.code = selectedRoleDetail.value?.code || row.code
  form.tagType = (selectedRoleDetail.value?.tagType || row.tagType || 'info') as RoleTagType | ''
  form.description = selectedRoleDetail.value?.description || row.description
  showFormDialog.value = true
}

async function openAssignDialog(roleId?: number) {
  if (!canAssignPermission.value) return
  await Promise.all([loadRoles(), loadModulePermissions()])
  const targetRoleId = roleId ?? selectedRoleId.value ?? roleRows.value[0]?.id ?? null
  if (targetRoleId) {
    await selectRoleForAssignment(targetRoleId)
  }
  showAssignDialog.value = true
}

async function selectRoleForAssignment(roleId: number) {
  selectedRoleId.value = roleId
  const response: any = await adminPermissionApi.getRoleModulePermissions(roleId)
  selectedPermissionCodes.value = response.data || []
  expandAllModules()
}

function toggleModule(module: ModulePermission, checked: string | number | boolean) {
  const next = new Set(selectedPermissionCodes.value)
  if (checked) {
    next.add(module.code)
  } else {
    next.delete(module.code)
    for (const action of module.children || []) {
      next.delete(action.code)
    }
  }
  selectedPermissionCodes.value = [...next]
}

function toggleAction(module: ModulePermission, action: ModulePermission, checked: string | number | boolean) {
  const next = new Set(selectedPermissionCodes.value)
  if (checked) {
    next.add(module.code)
    next.add(action.code)
  } else {
    next.delete(action.code)
  }
  selectedPermissionCodes.value = [...next]
}

async function submitForm() {
  if (formMode.value === 'create' && !canCreateRoleGroup.value) return
  if (formMode.value === 'edit' && !canUpdateRoleGroup.value) return
  if (!form.name.trim() || !form.code.trim()) {
    ElMessage.warning(copy.value.requiredNameCode)
    return
  }

  saving.value = true
  try {
    const payload = {
      name: form.name.trim(),
      code: form.code.trim(),
      tagType: (form.tagType || 'info') as RoleTagType,
      description: form.description.trim() || null
    }

    if (formMode.value === 'create') {
      await adminRoleApi.create(payload)
      ElMessage.success(copy.value.created)
    } else if (form.id != null) {
      await adminRoleApi.update(form.id, payload)
      ElMessage.success(copy.value.updated)
    }

    showFormDialog.value = false
    await loadRoles()
  } finally {
    saving.value = false
  }
}

async function removeRole(row: RoleGroupRow) {
  if (!canDeleteRoleGroup.value) return
  try {
    await ElMessageBox.confirm(copy.value.deleteConfirm, copy.value.deleteTitle, { type: 'warning' })
  } catch {
    return
  }

  await adminRoleApi.delete(row.id)
  ElMessage.success(copy.value.deleted)
  await loadRoles()
}

async function saveAssignments() {
  if (!canAssignPermission.value) return
  if (!selectedRoleId.value) {
    return
  }

  savingAssignments.value = true
  try {
    await adminPermissionApi.saveRoleModulePermissions(selectedRoleId.value, selectedPermissionCodes.value)
    ElMessage.success(isZh ? '权限分配已保存' : 'Permission assignment saved')
    showAssignDialog.value = false
  } finally {
    savingAssignments.value = false
  }
}

function notifyAssignByModule() {
  ElMessage.info(copy.value.moduleComingSoon)
}

function notifyCopyReserved() {
  ElMessage.info(copy.value.copyComingSoon)
}

onMounted(() => {
  void loadRoles()
})
</script>

<style scoped>
.admin-permissions-page { display: flex; flex-direction: column; gap: var(--space-lg); min-width: 0; }

.admin-permissions-page {
  min-width: 0;
}

.admin-permissions-toolbar {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: var(--space-md);
  flex-wrap: wrap;
}

.permissions-surface {
  min-width: 0;
}

.permissions-surface :deep(.el-card__body) {
  padding: var(--space-lg);
  min-width: 0;
}

.permissions-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.permissions-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.role-detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-md);
  margin-bottom: var(--space-md);
}
.role-member-list {
  display: grid;
  gap: var(--space-sm);
}
.permission-assignment-layout {
  display: grid;
  grid-template-columns: 240px minmax(0, 1fr);
  gap: var(--space-md);
  min-width: 0;
}
.permission-role-list,
.permission-module-panel {
  padding: var(--space-lg);
  min-width: 0;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.permission-panel-head {
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 1;
  background: var(--app-bg-surface);
  padding-bottom: var(--space-sm);
  margin-bottom: var(--space-sm);
  border-bottom: 1px solid var(--app-border-soft);
}

.permission-panel-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: var(--space-xs);
  margin-bottom: var(--space-md);
  font-size: 13px;
}

.permission-panel-btn {
  padding: 0 var(--space-xs);
  border: 0;
  background: none;
  color: var(--app-color-primary);
  cursor: pointer;
  font-size: 13px;
}

.permission-panel-btn:hover {
  text-decoration: underline;
}

.permission-panel-sep {
  color: var(--app-text-muted);
  user-select: none;
}

.permission-module-scroll {
  flex: 1;
  min-height: 0;
  max-height: 50vh;
  overflow-y: auto;
}

.permission-panel-role-summary {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}

.permission-selected-count {
  font-size: 13px;
  color: var(--app-text-secondary);
}

.permission-empty-hint {
  margin: 0 0 var(--space-md);
  padding: var(--space-sm) var(--space-md);
  background: var(--app-bg-muted);
  border-radius: var(--app-radius-sm);
  font-size: 13px;
  color: var(--app-text-secondary);
  line-height: 1.5;
}

.permission-role-list {
  background: var(--app-bg-surface);
}

.permission-role-list-head {
  margin-bottom: var(--space-md);
}
.permission-role-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-md);
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius-sm);
  background: var(--app-bg-surface);
  cursor: pointer;
  margin-bottom: var(--space-sm);
  color: var(--app-text-primary);
  transition: border-color 0.2s ease, background-color 0.2s ease, box-shadow 0.2s ease;
}

.permission-role-item:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 24%, var(--app-border-soft));
  background: var(--app-bg-muted);
}

.permission-role-item.is-active {
  border-color: color-mix(in srgb, var(--app-color-primary) 45%, var(--app-border-soft));
  background: color-mix(in srgb, var(--app-color-primary-soft) 72%, white);
}
.permission-role-name {
  font-weight: 600;
}
.permission-role-meta,
.permission-module-code {
  font-size: 12px;
  color: var(--app-text-muted);
}
.permission-module-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-md);
}
.permission-module-card {
  display: grid;
  gap: var(--space-sm);
  padding: var(--space-md);
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius-sm);
  background: var(--app-bg-surface);
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.permission-module-card:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 24%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.permission-module-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  cursor: pointer;
  padding: 2px 0;
}

.permission-module-head-left {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  min-width: 0;
}

.permission-module-chevron {
  flex-shrink: 0;
  font-size: 14px;
  color: var(--app-text-muted);
  transition: transform 0.2s ease;
}

.permission-module-chevron.is-expanded {
  transform: rotate(90deg);
}

.permission-module-meta {
  font-size: 12px;
  color: var(--app-text-muted);
  white-space: nowrap;
}

.permission-module-code {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--app-text-muted);
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
}

.permission-action-grid {
  display: grid;
  gap: var(--space-sm);
  padding-top: var(--space-sm);
  padding-left: 22px;
  border-top: 1px solid var(--app-border-soft);
}

.permission-action-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-md);
  padding: var(--space-xs) 0;
}

.permission-action-code {
  font-size: 11px;
  color: var(--app-text-muted);
  max-width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
}
@media (max-width: 1365px) {
  .role-detail-grid { grid-template-columns: 1fr; }
  .permission-assignment-layout,
  .permission-module-grid { grid-template-columns: 1fr; }
}
</style>
