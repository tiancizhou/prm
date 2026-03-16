<template>
  <div class="app-page organization-company-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ copy.pageTitle }}</h1>
        <p class="page-subtitle">{{ copy.pageSubtitle }}</p>
      </div>
    </header>

    <div class="workspace-layout--sidebar company-layout">
      <aside class="company-side-panel">
        <div class="company-side-head">
          <span class="company-side-label">{{ copy.previewTitle }}</span>
          <el-tag size="small" effect="plain">{{ copy.rootTag }}</el-tag>
        </div>
        <div class="company-root-card">
          <div class="company-root-name">{{ form.name || copy.emptyName }}</div>
          <p class="company-root-note">{{ copy.rootNote }}</p>
        </div>
        <el-tree
          :data="treeData"
          node-key="nodeKey"
          default-expand-all
          :expand-on-click-node="false"
          :props="treeProps"
          class="company-tree"
        />
      </aside>

      <div class="workspace-stack company-main-column">
        <el-card class="surface-card company-card" shadow="never">
          <div class="workspace-panel-head">
            <div>
              <div class="workspace-path company-path">{{ currentPath }}</div>
              <h2 class="workspace-panel-title">{{ copy.currentPanelTitle }}</h2>
            </div>
            <div class="workspace-action-row">
              <el-tag v-if="isCompanyDirty" type="warning" effect="light" size="small">{{ copy.unsavedTag }}</el-tag>
              <el-button type="primary" size="default" :loading="savingCompany" @click="saveCompany">{{ copy.saveCompany }}</el-button>
            </div>
          </div>
          <p class="workspace-helper-text">{{ copy.currentPanelHint }}</p>
          <el-form label-width="96px" class="company-form">
            <el-row :gutter="16">
              <el-col :xs="24" :sm="12">
                <el-form-item :label="copy.fields.name" required>
                  <el-input v-model="form.name" :placeholder="copy.fields.name" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item :label="copy.fields.shortName">
                  <el-input v-model="form.shortName" :placeholder="copy.fields.shortName" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item :label="copy.fields.contactName">
                  <el-input v-model="form.contactName" :placeholder="copy.fields.contactName" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item :label="copy.fields.phone">
                  <el-input v-model="form.phone" :placeholder="copy.fields.phone" clearable />
                </el-form-item>
              </el-col>
              <el-col :xs="24" :sm="12">
                <el-form-item :label="copy.fields.email">
                  <el-input v-model="form.email" :placeholder="copy.fields.email" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item :label="copy.fields.address">
                  <el-input v-model="form.address" :placeholder="copy.fields.address" clearable />
                </el-form-item>
              </el-col>
              <el-col :span="24">
                <el-form-item :label="copy.fields.description">
                  <el-input v-model="form.description" type="textarea" :rows="4" :placeholder="copy.fields.description" />
                </el-form-item>
              </el-col>
            </el-row>
          </el-form>
        </el-card>

        <el-card class="surface-card company-card" shadow="never">
          <div class="workspace-panel-head">
            <div>
              <h2 class="workspace-panel-title">{{ copy.childrenPanelTitle }}</h2>
              <p class="workspace-helper-text">{{ copy.childrenPanelHint }}</p>
            </div>
            <div class="workspace-action-row">
              <el-tag v-if="isDepartmentsDirty" type="warning" effect="light" size="small">{{ copy.unsavedTag }}</el-tag>
              <el-button type="primary" link :icon="Plus" @click="appendEmptyDepartmentRow">{{ copy.addDepartment }}</el-button>
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
          <div class="workspace-footer company-actions">
            <el-button type="primary" :loading="savingDepartments" :disabled="!currentCompanyId" @click="saveDepartments">{{ copy.saveDepartments }}</el-button>
            <el-button @click="handleResetDepartments">{{ copy.resetDepartments }}</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { onBeforeRouteLeave } from 'vue-router'
import { adminDepartmentApi, type DepartmentTreeNode } from '@/api/adminDepartment'
import { organizationCompanyApi } from '@/api/organizationCompany'
import { resolveThemeLocale } from '@/constants/theme'

interface ChildEditorRow {
  key: string
  id: number | null
  name: string
}

const MIN_EDITOR_ROWS = 8
const treeProps = { label: 'name', children: 'children' }
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const isZh = currentLocale === 'zh-CN'

const copy = computed(() => isZh
  ? {
      pageTitle: '公司信息',
      pageSubtitle: '维护公司基本资料与直属部门，保存后与后台部门树同步。',
      previewTitle: '组织层级预览',
      rootTag: '顶层公司',
      rootNote: '公司作为组织的最顶层，下面直接维护一级部门。',
      emptyName: '未设置公司名称',
      currentPanelTitle: '公司资料',
      currentPanelHint: '此处为公司资料唯一维护入口，保存后后台部门树根名称会同步更新。',
      childrenPanelTitle: '直属部门',
      childrenPanelHint: '在下方行内新增、改名或清空以删除一级部门。',
      addDepartment: '新增部门',
      saveCompany: '保存公司信息',
      saveDepartments: '保存直属部门',
      resetDepartments: '重置',
      resetConfirm: '当前直属部门区有未保存修改，确认重置吗？',
      unsavedTag: '有未保存变更',
      unsavedLeave: '当前页有未保存内容，确认离开吗？',
      unsavedTitle: '未保存变更',
      successCompany: '公司信息已保存',
      successDepartments: '直属部门已保存',
      requiredName: '请先填写公司名称',
      deleteConfirm: '检测到你清空了已有部门，继续保存将删除这些部门，确认继续吗？',
      fields: {
        name: '公司名称',
        shortName: '公司简称',
        contactName: '联系人',
        phone: '联系电话',
        email: '邮箱',
        address: '地址',
        description: '公司简介'
      }
    }
  : {
      pageTitle: 'Company Profile',
      pageSubtitle: 'Maintain company details and direct departments; changes sync with the admin department tree.',
      previewTitle: 'Hierarchy Preview',
      rootTag: 'Company Root',
      rootNote: 'The company is the top-level node and owns direct departments below it.',
      emptyName: 'Company name not set',
      currentPanelTitle: 'Company Details',
      currentPanelHint: 'This is the only place to maintain company details. Saving here syncs the admin department tree root.',
      childrenPanelTitle: 'Direct Departments',
      childrenPanelHint: 'Add, rename, or clear rows to manage direct departments inline.',
      addDepartment: 'Add Department',
      saveCompany: 'Save Company',
      saveDepartments: 'Save Direct Departments',
      resetDepartments: 'Reset',
      resetConfirm: 'There are unsaved changes in direct departments. Reset anyway?',
      unsavedTag: 'Unsaved Changes',
      unsavedLeave: 'You have unsaved changes on this page. Leave anyway?',
      unsavedTitle: 'Unsaved Changes',
      successCompany: 'Company profile saved',
      successDepartments: 'Direct departments saved',
      requiredName: 'Company name is required',
      deleteConfirm: 'Some saved departments were cleared. Continue and delete them?',
      fields: {
        name: 'Company Name',
        shortName: 'Short Name',
        contactName: 'Contact',
        phone: 'Phone',
        email: 'Email',
        address: 'Address',
        description: 'Description'
      }
    })

const treeData = ref<DepartmentTreeNode[]>([])
const childEditorRows = ref<ChildEditorRow[]>([])
const savingCompany = ref(false)
const savingDepartments = ref(false)
const companySnapshot = ref('')
const departmentSnapshot = ref('')

const form = reactive({
  id: null as number | null,
  name: '',
  shortName: '',
  contactName: '',
  phone: '',
  email: '',
  address: '',
  description: '',
  status: 'ACTIVE'
})

const currentCompanyNode = computed(() => {
  return treeData.value.find((node) => node.nodeType === 'COMPANY' && (form.id == null || node.id === form.id))
    ?? treeData.value.find((node) => node.nodeType === 'COMPANY')
    ?? null
})
const currentCompanyId = computed(() => form.id ?? currentCompanyNode.value?.id ?? null)
const directDepartments = computed(() => (currentCompanyNode.value?.children || []).filter((item) => item.nodeType === 'DEPARTMENT'))
const currentPath = computed(() => form.name?.trim() || copy.value.emptyName)
const isCompanyDirty = computed(() => companySnapshot.value !== companyStateSnapshot())
const isDepartmentsDirty = computed(() => departmentSnapshot.value !== departmentStateSnapshot())
const hasPendingChanges = computed(() => isCompanyDirty.value || isDepartmentsDirty.value)

async function load() {
  const [companyResponse, treeResponse]: any = await Promise.all([
    organizationCompanyApi.get(),
    adminDepartmentApi.tree()
  ])

  const company = companyResponse.data || {}
  form.id = company.id ?? null
  form.name = company.name || ''
  form.shortName = company.shortName || ''
  form.contactName = company.contactName || ''
  form.phone = company.phone || ''
  form.email = company.email || ''
  form.address = company.address || ''
  form.description = company.description || ''
  form.status = company.status || 'ACTIVE'

  treeData.value = treeResponse.data || []
  resetDepartmentEditorFromTree()
  refreshSnapshots()
}

async function saveCompany() {
  if (!form.name.trim()) {
    ElMessage.warning(copy.value.requiredName)
    return
  }

  savingCompany.value = true
  try {
    await organizationCompanyApi.update({
      id: form.id,
      name: form.name.trim(),
      shortName: form.shortName || null,
      contactName: form.contactName || null,
      phone: form.phone || null,
      email: form.email || null,
      address: form.address || null,
      description: form.description || null,
      status: form.status
    })
    ElMessage.success(copy.value.successCompany)
    await load()
  } finally {
    savingCompany.value = false
  }
}

async function saveDepartments() {
  if (!currentCompanyId.value) {
    return
  }

  const rowsToDelete = childEditorRows.value.filter((row) => row.id && !row.name.trim())
  if (rowsToDelete.length) {
    try {
      await ElMessageBox.confirm(copy.value.deleteConfirm, copy.value.unsavedTitle, { type: 'warning' })
    } catch {
      return
    }
  }

  savingDepartments.value = true
  try {
    for (const row of childEditorRows.value) {
      const trimmedName = row.name.trim()
      if (row.id && !trimmedName) {
        await adminDepartmentApi.delete(row.id)
        continue
      }

      if (row.id && trimmedName) {
        const currentDepartment = directDepartments.value.find((item) => item.id === row.id)
        await adminDepartmentApi.update(row.id, {
          name: trimmedName,
          companyId: currentCompanyId.value,
          parentId: null,
          sortOrder: currentDepartment?.sortOrder ?? 0,
          status: currentDepartment?.status ?? 'ACTIVE'
        })
        continue
      }

      if (!row.id && trimmedName) {
        await adminDepartmentApi.create({
          name: trimmedName,
          companyId: currentCompanyId.value,
          parentId: null,
          sortOrder: 0,
          status: 'ACTIVE'
        })
      }
    }

    ElMessage.success(copy.value.successDepartments)
    await load()
  } finally {
    savingDepartments.value = false
  }
}

function appendEmptyDepartmentRow() {
  childEditorRows.value.push({
    key: `new-${Date.now()}-${childEditorRows.value.length}`,
    id: null,
    name: ''
  })
}

function resetDepartmentEditor() {
  resetDepartmentEditorFromTree()
}

async function handleResetDepartments() {
  if (!isDepartmentsDirty.value) {
    resetDepartmentEditor()
    return
  }

  try {
    await ElMessageBox.confirm(copy.value.resetConfirm, copy.value.unsavedTitle, { type: 'warning' })
    resetDepartmentEditor()
  } catch {
    return
  }
}

function resetDepartmentEditorFromTree() {
  const rows: ChildEditorRow[] = directDepartments.value.map((department) => ({
    key: `existing-${department.id}`,
    id: department.id,
    name: department.name
  }))

  while (rows.length < MIN_EDITOR_ROWS) {
    rows.push({ key: `new-${rows.length}`, id: null, name: '' })
  }

  childEditorRows.value = rows
}

function childPlaceholder(index: number) {
  return isZh ? `直属部门 ${index + 1}` : `Direct Department ${index + 1}`
}

function companyStateSnapshot() {
  return JSON.stringify({
    name: form.name.trim(),
    shortName: form.shortName.trim(),
    contactName: form.contactName.trim(),
    phone: form.phone.trim(),
    email: form.email.trim(),
    address: form.address.trim(),
    description: form.description.trim()
  })
}

function departmentStateSnapshot() {
  return JSON.stringify(
    childEditorRows.value
      .map((row) => ({ id: row.id, name: row.name.trim() }))
      .filter((row) => row.id || row.name)
  )
}

function refreshSnapshots() {
  companySnapshot.value = companyStateSnapshot()
  departmentSnapshot.value = departmentStateSnapshot()
}

async function confirmLeaveIfDirty() {
  if (!hasPendingChanges.value) {
    return true
  }
  try {
    await ElMessageBox.confirm(copy.value.unsavedLeave, copy.value.unsavedTitle, { type: 'warning' })
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
  return await confirmLeaveIfDirty()
})

onMounted(() => {
  void load()
  window.addEventListener('beforeunload', handleBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
})
</script>

<style scoped>
.organization-company-page { min-width: 0; }
.company-layout { min-width: 0; }
.company-main-column { min-width: 0; }

.company-side-panel {
  flex-shrink: 0;
  width: 280px;
  min-height: 200px;
  padding: var(--space-lg);
  border-radius: var(--app-radius-md);
  background: var(--app-bg-card, #fff);
  border: 1px solid var(--app-border-soft);
  box-shadow: var(--app-shadow-sm, 0 1px 2px rgba(0, 0, 0, 0.04));
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  overflow: auto;
}
.company-side-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-sm);
}
.company-side-label {
  font-size: 14px;
  font-weight: 600;
  color: var(--app-text-primary);
}
.company-root-card {
  padding: var(--space-md) var(--space-lg);
  border-radius: var(--app-radius-sm);
  background: var(--app-bg-muted);
  border: 1px solid var(--app-border-soft);
}
.company-root-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text-primary);
}
.company-root-note {
  margin: var(--space-sm) 0 0;
  font-size: 13px;
  color: var(--app-text-secondary);
  line-height: 1.45;
}

.company-tree {
  min-height: 200px;
  flex: 1;
}
.company-tree :deep(.el-tree-node__content) {
  padding: 4px 0;
}

.company-card {
  min-width: 0;
}
.company-card .workspace-panel-head {
  margin-bottom: var(--space-sm);
}
.company-path {
  font-size: 12px;
  color: var(--app-text-secondary);
  margin-bottom: 2px;
}
.company-form {
  max-width: 720px;
}
.company-form :deep(.el-form-item) {
  margin-bottom: var(--space-md);
}

.child-editor-list {
  display: grid;
  grid-template-columns: 1fr;
  grid-auto-rows: min-content;
  gap: var(--space-sm);
  min-height: 120px;
  margin-bottom: var(--space-lg);
}
.child-editor-input {
  width: 100%;
  min-width: 0;
  max-width: 100%;
}
.company-actions {
  padding-top: var(--space-md);
  border-top: 1px solid var(--app-border-soft);
}

@media (max-width: 1023px) {
  .company-side-panel { width: 100%; }
  .company-layout { flex-direction: column; }
}
</style>
