<template>
  <div class="app-page admin-department-page">
    <div class="workspace-layout--sidebar department-layout">
      <el-card class="surface-card workspace-side-panel department-sidebar" shadow="never">
        <div class="workspace-panel-head department-sidebar-head">
          <h2 class="workspace-panel-title">{{ peopleText.departmentPage.title }}</h2>
          <div class="workspace-action-row department-sidebar-actions">
            <el-button v-if="canCreateDepartment" size="small" :icon="Plus" @click="appendEmptyChildRow">{{ addRowLabel }}</el-button>
            <el-button v-if="selectedNode?.nodeType === 'DEPARTMENT'" size="small" type="primary" :icon="Check" :loading="savingCurrent" :disabled="!canSaveCurrent" @click="saveCurrentNode">{{ saveCurrentLabel }}</el-button>
            <el-button v-if="selectedNode?.nodeType === 'DEPARTMENT'" size="small" plain :icon="Delete" :disabled="!canDeleteCurrent" @click="removeCurrent">{{ deleteCurrentLabel }}</el-button>
          </div>
        </div>

        <el-tree
          :data="treeData"
          node-key="nodeKey"
          highlight-current
          default-expand-all
          :current-node-key="selectedNodeKey ?? undefined"
          :props="treeProps"
          @node-click="handleNodeClick"
        />
      </el-card>

      <div class="workspace-stack department-main-column">
        <el-card class="surface-card workspace-section-panel workspace-node-card" shadow="never">
          <div class="workspace-panel-head">
            <div>
              <div class="workspace-path">{{ breadcrumbText }}</div>
              <h2 class="workspace-panel-title">{{ currentCardTitle }}</h2>
            </div>
            <div class="workspace-action-row">
              <el-tag v-if="hasPendingChanges" type="warning" effect="light">{{ unsavedTag }}</el-tag>
              <el-button v-if="selectedNode?.nodeType === 'COMPANY'" @click="openCompanyPage">{{ companyActionLabel }}</el-button>
              <el-button v-else type="primary" :loading="savingCurrent" :disabled="!canSaveCurrent" @click="saveCurrentNode">{{ currentSaveLabel }}</el-button>
            </div>
          </div>

          <template v-if="selectedNode?.nodeType === 'COMPANY'">
            <div class="workspace-helper-text company-summary-text">{{ companySummaryText }}</div>
            <div class="department-summary-grid">
              <div class="workspace-summary-card">
                <div class="workspace-summary-label">{{ companyRootLabel }}</div>
                <div class="workspace-summary-value">{{ selectedNode?.name }}</div>
              </div>
              <div class="workspace-summary-card">
                <div class="workspace-summary-label">{{ directDepartmentCountLabel }}</div>
                <div class="workspace-summary-value">{{ childDepartments.length }}</div>
              </div>
            </div>
          </template>

          <template v-else>
            <div class="workspace-helper-text">{{ currentCardHint }}</div>
            <el-form label-width="96px" class="workspace-inline-form department-current-form">
              <el-form-item :label="peopleText.departmentPage.labels.name" required>
                <el-input v-model="currentForm.name" />
              </el-form-item>
              <el-form-item :label="peopleText.departmentPage.labels.parent">
                <el-select v-model="currentForm.parentId" clearable>
                  <el-option v-for="department in parentOptions" :key="department.id" :label="department.name" :value="department.id" />
                </el-select>
              </el-form-item>
              <el-form-item :label="peopleText.departmentPage.labels.sort">
                <el-input-number v-model="currentForm.sortOrder" :min="0" />
              </el-form-item>
              <el-form-item :label="peopleText.departmentPage.labels.status">
                <el-select v-model="currentForm.status">
                  <el-option :label="peopleText.departmentPage.statusOptions.active" value="ACTIVE" />
                  <el-option :label="peopleText.departmentPage.statusOptions.disabled" value="DISABLED" />
                </el-select>
              </el-form-item>
            </el-form>
          </template>
        </el-card>

        <el-card class="surface-card workspace-section-panel workspace-node-card" shadow="never">
          <div class="workspace-panel-head">
            <div>
              <h2 class="workspace-panel-title">{{ childrenCardTitle }}</h2>
              <div class="workspace-helper-text">{{ childrenCardHint }}</div>
            </div>
            <div class="workspace-action-row">
              <el-tag v-if="isChildrenDirty" type="warning" effect="light">{{ unsavedTag }}</el-tag>
              <button v-if="canCreateDepartment" type="button" class="workspace-icon-button" @click="appendEmptyChildRow"><el-icon><Plus /></el-icon></button>
            </div>
          </div>

          <div class="child-editor-list">
            <el-input
              v-for="(row, index) in childEditorRows"
              :key="row.key"
              v-model="row.name"
              class="child-editor-input"
              :placeholder="childPlaceholder(index)"
              clearable
            />
          </div>

          <div class="workspace-footer department-actions">
            <el-button type="primary" :loading="savingChildren" :disabled="!canManageChildren" @click="saveChildDepartments">{{ peopleText.departmentPage.buttons.save }}</el-button>
            <el-button @click="handleResetChildEditor">{{ resetLabel }}</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { Check, Delete, Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onBeforeRouteLeave, useRouter } from 'vue-router'
import { adminDepartmentApi, type DepartmentDetail, type DepartmentTreeNode } from '@/api/adminDepartment'
import { ADMIN_PEOPLE_I18N } from '@/constants/adminPeople'
import { resolveThemeLocale } from '@/constants/theme'
import { useAuthStore } from '@/stores/auth'
import { ACTION_PERMISSION_MAP } from '@/utils/permission'

interface ChildEditorRow {
  key: string
  id: number | null
  name: string
}

const MIN_EDITOR_ROWS = 10
const treeProps = { label: 'name', children: 'children' }
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const peopleText = ADMIN_PEOPLE_I18N[currentLocale]
const isZh = currentLocale === 'zh-CN'
const router = useRouter()
const authStore = useAuthStore()

const unsavedTag = isZh ? '有未保存变更' : 'Unsaved Changes'
const currentSaveLabel = peopleText.departmentPage.buttons.save
const resetLabel = isZh ? '重置输入' : 'Reset'
const addRowLabel = isZh ? '新增一行' : 'Add Row'
const saveCurrentLabel = isZh ? '保存当前' : 'Save Current'
const deleteCurrentLabel = isZh ? '删除当前' : 'Delete Current'
const companyActionLabel = isZh ? '去组织维护公司信息' : 'Open Company Page'
const companySummaryText = isZh
  ? '当前选中的是公司根节点。公司资料请在“组织 > 公司”中维护，这里只负责管理一级部门。'
  : 'You selected the company root. Maintain company details in Organization > Company and manage only direct departments here.'
const companyRootLabel = isZh ? '当前公司' : 'Current Company'
const directDepartmentCountLabel = isZh ? '直属部门数' : 'Direct Departments'
const currentCardHint = isZh
  ? '上半区维护当前节点，下半区维护当前节点的直属子部门。'
  : 'Use the top panel for the current node and the bottom panel for its direct child departments.'
const unsavedLeave = isZh ? '当前页面有未保存内容，确认离开吗？' : 'You have unsaved changes on this page. Leave anyway?'
const unsavedTitle = isZh ? '未保存变更' : 'Unsaved Changes'
const resetChildrenConfirm = isZh ? '当前直属子级区有未保存修改，确认重置吗？' : 'There are unsaved changes in the direct-child editor. Reset anyway?'
const deleteChildrenConfirm = isZh
  ? '检测到你清空了已有子部门，继续保存将删除这些子部门，确认继续吗？'
  : 'Some saved child departments were cleared. Continue and delete them?'

const treeData = ref<DepartmentTreeNode[]>([])
const selectedNode = ref<DepartmentTreeNode | null>(null)
const selectedNodeKey = ref<string | null>(null)
const childDepartments = ref<DepartmentTreeNode[]>([])
const childEditorRows = ref<ChildEditorRow[]>([])
const savingCurrent = ref(false)
const savingChildren = ref(false)

const currentSnapshot = ref('')
const childrenSnapshot = ref('')

const currentForm = reactive({
  id: null as number | null,
  companyId: null as number | null,
  name: '',
  parentId: null as number | null,
  sortOrder: 0,
  status: 'ACTIVE'
})

const selectedDepartmentId = computed(() => selectedNode.value?.nodeType === 'DEPARTMENT' ? selectedNode.value.id : null)
const selectedCompanyId = computed(() => {
  if (!selectedNode.value) return null
  return selectedNode.value.nodeType === 'COMPANY'
    ? selectedNode.value.id
    : (selectedNode.value.companyId ?? null)
})
const canCreateDepartment = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.departmentCreate))
const canUpdateDepartment = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.departmentUpdate))
const canDeleteDepartment = computed(() => authStore.canAccess(ACTION_PERMISSION_MAP.departmentDelete))
const canDeleteCurrent = computed(() => selectedNode.value?.nodeType === 'DEPARTMENT' && canDeleteDepartment.value)
const canSaveCurrent = computed(() => selectedNode.value?.nodeType === 'DEPARTMENT' && isCurrentDirty.value && canUpdateDepartment.value)
const canManageChildren = computed(() => Boolean(selectedNode.value && selectedCompanyId.value))
const isCurrentDirty = computed(() => currentSnapshot.value !== currentStateSnapshot())
const isChildrenDirty = computed(() => childrenSnapshot.value !== childrenStateSnapshot())
const hasPendingChanges = computed(() => isCurrentDirty.value || isChildrenDirty.value)
const currentCardTitle = computed(() => selectedNode.value?.nodeType === 'COMPANY'
  ? (isZh ? '当前节点：公司摘要' : 'Current Node: Company Summary')
  : (isZh ? '当前节点：部门信息' : 'Current Node: Department Details'))
const childrenCardTitle = computed(() => selectedNode.value?.nodeType === 'COMPANY'
  ? (isZh ? '直属部门快速维护' : 'Direct Departments')
  : (isZh ? '直属子部门快速维护' : 'Direct Child Departments'))
const childrenCardHint = computed(() => selectedNode.value?.nodeType === 'COMPANY'
  ? (isZh ? '这里维护公司下面的一级部门。' : 'Maintain the first-level departments under this company here.')
  : (isZh ? '这里维护当前部门的直属子部门，支持行内批量操作。' : 'Maintain direct child departments here with fast inline batch editing.'))
const breadcrumbText = computed(() => {
  if (!selectedNodeKey.value) return ''
  return buildPathByKey(selectedNodeKey.value, treeData.value).join(' / ')
})
const parentOptions = computed(() => flattenNodes(treeData.value)
  .filter((item) => item.nodeType === 'DEPARTMENT' && item.id !== currentForm.id))

async function loadTree(preferredNodeKey?: string | null) {
  const response: any = await adminDepartmentApi.tree()
  treeData.value = response.data || []

  const nextNode = findNodeByKey(preferredNodeKey ?? selectedNodeKey.value, treeData.value)
    ?? findFirstDepartment(treeData.value)
    ?? treeData.value[0]

  if (!nextNode) {
    selectedNode.value = null
    selectedNodeKey.value = null
    childDepartments.value = []
    resetChildEditorFromSource()
    return
  }

  await applySelection(nextNode)
}

async function applySelection(node: DepartmentTreeNode) {
  selectedNodeKey.value = node.nodeKey

  if (node.nodeType === 'COMPANY') {
    selectedNode.value = node
    currentForm.id = null
    currentForm.companyId = node.id
    currentForm.name = node.name
    currentForm.parentId = null
    currentForm.sortOrder = 0
    currentForm.status = node.status || 'ACTIVE'
    childDepartments.value = filterDepartmentChildren(node.children)
    resetChildEditorFromSource()
    refreshSnapshots()
    return
  }

  const response: any = await adminDepartmentApi.get(node.id)
  const detail: DepartmentDetail = response.data || {}
  selectedNode.value = {
    ...node,
    companyId: detail.companyId ?? node.companyId,
    parentId: detail.parentId ?? node.parentId,
    sortOrder: detail.sortOrder ?? node.sortOrder,
    status: detail.status ?? node.status,
    children: detail.children || node.children || []
  }
  currentForm.id = node.id
  currentForm.companyId = detail.companyId ?? node.companyId ?? null
  currentForm.name = detail.name || node.name
  currentForm.parentId = detail.parentId ?? node.parentId ?? null
  currentForm.sortOrder = detail.sortOrder ?? node.sortOrder ?? 0
  currentForm.status = detail.status ?? node.status ?? 'ACTIVE'
  childDepartments.value = filterDepartmentChildren(detail.children)
  resetChildEditorFromSource()
  refreshSnapshots()
}

async function handleNodeClick(node: DepartmentTreeNode) {
  if (node.nodeKey === selectedNodeKey.value) {
    return
  }
  const canLeave = await confirmDiscardChanges()
  if (!canLeave) {
    return
  }
  await applySelection(node)
}

async function saveCurrentNode() {
  if (!canUpdateDepartment.value) return
  if (currentForm.id == null) {
    return
  }
  if (!currentForm.name.trim()) {
    ElMessage.warning(peopleText.departmentPage.messages.nameRequired)
    return
  }

  savingCurrent.value = true
  try {
    await adminDepartmentApi.update(currentForm.id, {
      name: currentForm.name.trim(),
      companyId: currentForm.companyId,
      parentId: currentForm.parentId,
      sortOrder: currentForm.sortOrder,
      status: currentForm.status
    })
    ElMessage.success(peopleText.departmentPage.messages.updated)
    await loadTree(selectedNodeKey.value)
  } finally {
    savingCurrent.value = false
  }
}

async function saveChildDepartments() {
  if (!selectedNode.value || !selectedCompanyId.value) {
    return
  }

  const rowsToDelete = childEditorRows.value.filter((row) => row.id && !row.name.trim())
  if (rowsToDelete.length) {
    try {
      await ElMessageBox.confirm(deleteChildrenConfirm, unsavedTitle, { type: 'warning' })
    } catch {
      return
    }
  }

  savingChildren.value = true
  try {
    const parentId = selectedNode.value.nodeType === 'DEPARTMENT' ? selectedNode.value.id : null
    let skippedCreate = false
    let skippedUpdate = false
    let skippedDelete = false

    for (const row of childEditorRows.value) {
      const trimmedName = row.name.trim()
      if (row.id && !trimmedName) {
        if (!canDeleteDepartment.value) {
          skippedDelete = true
          continue
        }
        await adminDepartmentApi.delete(row.id)
        continue
      }

      if (row.id && trimmedName) {
        if (!canUpdateDepartment.value) {
          skippedUpdate = true
          continue
        }
        const currentChild = childDepartments.value.find((item) => item.id === row.id)
        await adminDepartmentApi.update(row.id, {
          name: trimmedName,
          companyId: selectedCompanyId.value,
          parentId,
          sortOrder: currentChild?.sortOrder ?? 0,
          status: currentChild?.status ?? 'ACTIVE'
        })
        continue
      }

      if (!row.id && trimmedName) {
        if (!canCreateDepartment.value) {
          skippedCreate = true
          continue
        }
        await adminDepartmentApi.create({
          name: trimmedName,
          companyId: selectedCompanyId.value,
          parentId,
          sortOrder: 0,
          status: 'ACTIVE'
        })
      }
    }

    if (skippedCreate || skippedUpdate || skippedDelete) {
      ElMessage.warning(isZh ? '部分子部门操作因权限不足未执行' : 'Some child department changes were skipped due to insufficient permissions')
    }

    ElMessage.success(peopleText.departmentPage.messages.updated)
    await loadTree(selectedNodeKey.value)
  } finally {
    savingChildren.value = false
  }
}

function appendEmptyChildRow() {
  if (!canCreateDepartment.value) return
  childEditorRows.value.push({
    key: `new-${Date.now()}-${childEditorRows.value.length}`,
    id: null,
    name: ''
  })
}

function resetChildEditor() {
  resetChildEditorFromSource()
}

async function handleResetChildEditor() {
  if (!isChildrenDirty.value) {
    resetChildEditor()
    return
  }

  try {
    await ElMessageBox.confirm(resetChildrenConfirm, unsavedTitle, { type: 'warning' })
    resetChildEditor()
  } catch {
    return
  }
}

function resetChildEditorFromSource() {
  const rows: ChildEditorRow[] = childDepartments.value.map((child) => ({
    key: `existing-${child.id}`,
    id: child.id,
    name: child.name
  }))

  while (rows.length < MIN_EDITOR_ROWS) {
    rows.push({ key: `new-${rows.length}`, id: null, name: '' })
  }

  childEditorRows.value = rows
}

function childPlaceholder(index: number) {
  return selectedNode.value?.nodeType === 'COMPANY'
    ? (isZh ? `直属部门 ${index + 1}` : `Direct Department ${index + 1}`)
    : (isZh ? `下级部门 ${index + 1}` : `Child Department ${index + 1}`)
}

async function removeCurrent() {
  if (!canDeleteDepartment.value) return
  if (selectedDepartmentId.value == null) return

  try {
    await ElMessageBox.confirm(
      peopleText.departmentPage.messages.deleteConfirm,
      peopleText.departmentPage.messages.deleteTitle,
      { type: 'warning' }
    )
  } catch {
    return
  }

  const fallbackKey = selectedNodeKey.value ? findParentNodeKey(selectedNodeKey.value, treeData.value) : null
  await adminDepartmentApi.delete(selectedDepartmentId.value)
  ElMessage.success(peopleText.departmentPage.messages.deleted)
  await loadTree(fallbackKey)
}

function openCompanyPage() {
  void router.push('/organization/company')
}

function filterDepartmentChildren(children?: DepartmentTreeNode[]) {
  return (children || []).filter((item) => item.nodeType === 'DEPARTMENT')
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

function buildPathByKey(nodeKey: string, nodes: DepartmentTreeNode[], path: string[] = []): string[] {
  for (const node of nodes) {
    const nextPath = [...path, node.name]
    if (node.nodeKey === nodeKey) {
      return nextPath
    }
    const childPath = buildPathByKey(nodeKey, node.children || [], nextPath)
    if (childPath.length) {
      return childPath
    }
  }
  return []
}

function findParentNodeKey(nodeKey: string, nodes: DepartmentTreeNode[], parentKey: string | null = null): string | null {
  for (const node of nodes) {
    if (node.nodeKey === nodeKey) {
      return parentKey
    }
    const childParent = findParentNodeKey(nodeKey, node.children || [], node.nodeKey)
    if (childParent !== null) {
      return childParent
    }
  }
  return null
}

function currentStateSnapshot() {
  if (selectedNode.value?.nodeType !== 'DEPARTMENT') {
    return 'company'
  }
  return JSON.stringify({
    id: currentForm.id,
    companyId: currentForm.companyId,
    name: currentForm.name.trim(),
    parentId: currentForm.parentId,
    sortOrder: currentForm.sortOrder,
    status: currentForm.status
  })
}

function childrenStateSnapshot() {
  return JSON.stringify(
    childEditorRows.value
      .map((row) => ({ id: row.id, name: row.name.trim() }))
      .filter((row) => row.id || row.name)
  )
}

function refreshSnapshots() {
  currentSnapshot.value = currentStateSnapshot()
  childrenSnapshot.value = childrenStateSnapshot()
}

async function confirmDiscardChanges() {
  if (!hasPendingChanges.value) {
    return true
  }
  try {
    await ElMessageBox.confirm(unsavedLeave, unsavedTitle, { type: 'warning' })
    return true
  } catch {
    return false
  }
}

function handleBeforeUnload(event: BeforeUnloadEvent) {
  if (!hasPendingChanges.value) {
    return
  }
  event.preventDefault()
  event.returnValue = ''
}

onBeforeRouteLeave(async () => {
  return await confirmDiscardChanges()
})

onMounted(() => {
  void loadTree()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
.admin-department-page,
.admin-department-page {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.department-layout {
  flex: 1;
  min-height: 0;
}

.department-sidebar :deep(.el-card__body) {
  padding: var(--space-lg);
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  overflow: auto;
  min-width: 0;
}

.department-layout,
.department-main-column,
.department-detail,
.department-sidebar { min-width: 0; }

.workspace-node-card {
  min-width: 0;
}

.workspace-node-card :deep(.el-card__body) {
  padding: var(--space-lg);
}

.workspace-node-card .workspace-panel-head {
  margin-bottom: var(--space-sm);
}

.workspace-node-card .department-current-form {
  max-width: 100%;
}

.department-sidebar-head {
  margin-bottom: 0;
  align-items: center;
}

.department-sidebar :deep(.el-tree) {
  min-height: 200px;
}

.department-sidebar :deep(.el-tree-node__content) {
  padding: 4px 0;
}

.department-current-form {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.department-current-form :deep(.el-form-item) {
  margin-bottom: var(--space-md);
}

.department-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-md);
  margin-top: var(--space-md);
}

.child-editor-list {
  display: grid;
  grid-template-columns: 1fr;
  grid-auto-rows: min-content;
  gap: var(--space-sm);
  min-height: 120px;
  margin-bottom: var(--space-lg);
}

.child-editor-input { width: 100%; min-width: 0; }

.company-summary-text {
  margin-bottom: var(--space-md);
  line-height: 1.5;
}

@media (max-width: 1023px) {
  .department-sidebar {
    width: 100%;
  }

  .department-layout {
    flex-direction: column;
  }
}

@media (max-width: 1365px) {
  .department-current-form,
  .department-summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
