<template>
  <div class="module-page">
    <!-- 顶部标题栏 -->
    <div class="page-header">
      <div class="header-left">
        <el-icon class="back-placeholder" />
        <span class="page-title">模块维护 - {{ projectName }}</span>
      </div>
    </div>

    <div class="module-layout">
      <!-- ===== 左侧：根模块列表 ===== -->
      <div class="left-panel">
        <div class="left-panel-header">
          <span class="panel-label">模块维护</span>
          <el-tooltip v-if="canManage" content="新建根模块" placement="right">
            <el-icon class="add-root-btn" @click="addRootModule"><Plus /></el-icon>
          </el-tooltip>
        </div>

        <div v-if="loading" class="left-loading">
          <el-skeleton :rows="4" animated />
        </div>
        <div v-else-if="rootModules.length === 0" class="left-empty">
          <span>暂无模块</span>
        </div>
        <ul v-else class="module-list">
          <li
            v-for="m in rootModules"
            :key="m.id"
            class="module-item"
            :class="{ active: selectedRootId === m.id }"
            @click="selectRoot(m)"
          >
            <span class="module-item-name" :title="m.name">{{ m.name }}</span>
            <span v-if="canManage" class="module-item-actions" @click.stop>
              <el-tooltip content="新增子模块" placement="top">
                <el-icon class="mi-icon" @click="addChildInline(m)"><Plus /></el-icon>
              </el-tooltip>
              <el-tooltip content="编辑" placement="top">
                <el-icon class="mi-icon" @click="renameRoot(m)"><EditPen /></el-icon>
              </el-tooltip>
              <el-tooltip content="删除" placement="top">
                <el-icon class="mi-icon danger" @click="deleteModule(m)"><Delete /></el-icon>
              </el-tooltip>
            </span>
          </li>
        </ul>
      </div>

      <!-- ===== 右侧：子模块编辑区 ===== -->
      <div class="right-panel">
        <div class="right-panel-header">
          <span class="right-title">维护子模块</span>
          <el-button v-if="canManage && selectedRoot" size="small" type="primary" plain @click="addChildInline(selectedRoot)">
            <el-icon><Plus /></el-icon> 新增子模块
          </el-button>
        </div>

        <div v-if="!selectedRoot" class="right-empty">
          <el-empty description="请在左侧选择一个模块" :image-size="80" />
        </div>
        <div v-else>
          <!-- 面包屑 -->
          <div class="breadcrumb">
            <span class="breadcrumb-item">{{ projectName }}</span>
            <el-icon><ArrowRight /></el-icon>
            <span class="breadcrumb-item active">{{ selectedRoot.name }}</span>
          </div>

          <!-- 子模块列表 -->
          <div v-if="childRows.length === 0 && !canManage" class="right-empty">
            <el-empty description="暂无子模块" :image-size="60" />
          </div>
          <div v-else class="child-list">
            <div
              v-for="(row, idx) in childRows"
              :key="row.key"
              class="child-row"
            >
              <el-input
                v-model="row.name"
                placeholder="模块名称"
                class="child-name-input"
                maxlength="100"
                :readonly="!canManage"
              />
              <span v-if="canManage" class="child-row-actions">
                <el-tooltip content="删除此子模块" placement="top">
                  <el-icon class="row-icon danger" @click="removeChildRow(idx)"><Close /></el-icon>
                </el-tooltip>
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div v-if="canManage" class="right-actions">
            <el-button type="primary" :loading="saving" @click="saveChildren">保存</el-button>
            <el-button @click="cancelEdit">返回</el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 重命名弹窗（根模块编辑） -->
    <el-dialog v-model="showRenameDialog" title="编辑模块名称" width="400px" destroy-on-close>
      <el-input v-model="renameValue" placeholder="请输入模块名称" maxlength="100" show-word-limit clearable @keyup.enter="submitRename" />
      <template #footer>
        <el-button @click="showRenameDialog = false">取消</el-button>
        <el-button type="primary" :loading="renaming" @click="submitRename">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, EditPen, Delete, ArrowRight, Close } from '@element-plus/icons-vue'
import { moduleApi, type ModuleDTO } from '@/api/module'
import { useProjectStore } from '@/stores/project'

const route = useRoute()
const projectId = Number(route.params.id)
const projectStore = useProjectStore()

const projectName = computed(() => projectStore.currentProject?.name ?? '项目')
const canManage = computed(() => {
  const p = projectStore.currentProject
  return p?.id === projectId && p?.canEdit === true
})

// ---- 数据 ----
const loading = ref(false)
const allModules = ref<ModuleDTO[]>([])

// 根模块（parentId 为 null 的）
const rootModules = computed(() =>
  allModules.value.filter(m => !m.parentId)
)

async function loadAll() {
  loading.value = true
  try {
    const res = await moduleApi.listTree(projectId)
    const tree: ModuleDTO[] = (res as any)?.data ?? res ?? []
    // 展平树为列表（后端返回的是树形，这里只需要根+子两层）
    allModules.value = flattenTree(tree)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载模块失败')
  } finally {
    loading.value = false
  }
}

function flattenTree(nodes: ModuleDTO[]): ModuleDTO[] {
  const result: ModuleDTO[] = []
  for (const n of nodes) {
    result.push(n)
    if (n.children?.length) result.push(...flattenTree(n.children))
  }
  return result
}

// ---- 左侧选择 ----
const selectedRootId = ref<number | null>(null)
const selectedRoot = ref<ModuleDTO | null>(null)

function selectRoot(m: ModuleDTO) {
  if (selectedRootId.value === m.id) return
  selectedRootId.value = m.id
  selectedRoot.value = m
  loadChildRows(m.id)
}

// ---- 右侧子模块行 ----
interface ChildRow {
  key: string
  id: number | null  // null = 新增未保存
  name: string
  originalName: string
}

const childRows = ref<ChildRow[]>([])
let rowKeyCounter = 0

function loadChildRows(parentId: number) {
  const children = allModules.value.filter(m => m.parentId === parentId)
  childRows.value = children.map(c => ({
    key: `existing-${c.id}`,
    id: c.id,
    name: c.name,
    originalName: c.name
  }))
}

function addChildInline(parent: ModuleDTO) {
  if (!canManage.value) return
  if (selectedRootId.value !== parent.id) {
    selectRoot(parent)
  }
  childRows.value.push({
    key: `new-${++rowKeyCounter}`,
    id: null,
    name: '',
    originalName: ''
  })
}

function removeChildRow(idx: number) {
  const row = childRows.value[idx]
  if (!row) return
  if (row.id !== null) {
    // 已有模块需要确认删除
    ElMessageBox.confirm(
      `确认删除子模块「${row.name || '（未命名）'}」？该模块下的需求将取消关联。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    ).then(async () => {
      try {
        await moduleApi.remove(projectId, row.id!)
        childRows.value.splice(idx, 1)
        await loadAll()
        if (selectedRoot.value) loadChildRows(selectedRoot.value.id)
        ElMessage.success('已删除')
      } catch (e: any) {
        ElMessage.error(e?.response?.data?.msg ?? '删除失败')
      }
    }).catch(() => {})
  } else {
    childRows.value.splice(idx, 1)
  }
}

// ---- 保存子模块 ----
const saving = ref(false)

async function saveChildren() {
  if (!selectedRoot.value) return
  const parentId = selectedRoot.value.id

  // 验证所有行都有名称
  for (const row of childRows.value) {
    if (!row.name.trim()) {
      ElMessage.warning('模块名称不能为空，请填写后再保存')
      return
    }
  }

  saving.value = true
  try {
    for (const row of childRows.value) {
      if (row.id === null) {
        // 新建
        await moduleApi.create(projectId, { name: row.name.trim(), parentId, sortOrder: 0 })
      } else if (row.name.trim() !== row.originalName) {
        // 修改了名称
        await moduleApi.update(projectId, row.id, { name: row.name.trim() })
      }
    }
    ElMessage.success('保存成功')
    await loadAll()
    // 刷新右侧
    const freshParent = allModules.value.find(m => m.id === parentId)
    if (freshParent) {
      selectedRoot.value = freshParent
      loadChildRows(parentId)
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? e?.message ?? '保存失败')
  } finally {
    saving.value = false
  }
}

function cancelEdit() {
  if (!selectedRoot.value) return
  loadChildRows(selectedRoot.value.id)
}

// ---- 新增根模块 ----
function addRootModule() {
  showRenameDialog.value = true
  renamingModule.value = null
  renameValue.value = ''
}

// ---- 重命名根模块 ----
const showRenameDialog = ref(false)
const renamingModule = ref<ModuleDTO | null>(null)
const renameValue = ref('')
const renaming = ref(false)

function renameRoot(m: ModuleDTO) {
  renamingModule.value = m
  renameValue.value = m.name
  showRenameDialog.value = true
}

async function submitRename() {
  if (!renameValue.value.trim()) {
    ElMessage.warning('请输入模块名称')
    return
  }
  renaming.value = true
  try {
    if (renamingModule.value) {
      // 编辑已有
      await moduleApi.update(projectId, renamingModule.value.id, { name: renameValue.value.trim() })
      ElMessage.success('已更新')
    } else {
      // 新建根模块
      await moduleApi.create(projectId, { name: renameValue.value.trim(), parentId: null, sortOrder: 0 })
      ElMessage.success('根模块已创建')
    }
    showRenameDialog.value = false
    await loadAll()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? e?.message ?? '操作失败')
  } finally {
    renaming.value = false
  }
}

// ---- 删除模块 ----
async function deleteModule(m: ModuleDTO) {
  try {
    await ElMessageBox.confirm(
      `确认删除模块「${m.name}」？该操作将同时清除需求的模块关联。`,
      '删除确认',
      { type: 'warning', confirmButtonText: '删除', cancelButtonText: '取消' }
    )
    await moduleApi.remove(projectId, m.id)
    ElMessage.success('已删除')
    if (selectedRootId.value === m.id) {
      selectedRootId.value = null
      selectedRoot.value = null
      childRows.value = []
    }
    await loadAll()
  } catch (e: any) {
    if (e === 'cancel') return
    ElMessage.error(e?.response?.data?.msg ?? e?.message ?? '删除失败')
  }
}

onMounted(loadAll)
</script>

<style scoped>
.module-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 100%;
  background: #f0f2f5;
}

/* ---- 顶部 ---- */
.page-header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 14px 24px;
  display: flex;
  align-items: center;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
}

/* ---- 布局 ---- */
.module-layout {
  display: flex;
  flex: 1;
  overflow: hidden;
}

/* ---- 左侧 ---- */
.left-panel {
  width: 220px;
  min-width: 180px;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.left-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px 10px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-label {
  font-size: 13px;
  font-weight: 600;
  color: #1d2129;
}

.add-root-btn {
  font-size: 16px;
  color: #4080ff;
  cursor: pointer;
  padding: 2px;
  border-radius: 4px;
  transition: background 0.2s;
}

.add-root-btn:hover {
  background: #e8f0fe;
}

.left-loading,
.left-empty {
  padding: 20px 16px;
  color: #86909c;
  font-size: 13px;
}

.module-list {
  list-style: none;
  margin: 0;
  padding: 6px 0;
  overflow-y: auto;
  flex: 1;
}

.module-item {
  display: flex;
  align-items: center;
  padding: 8px 16px;
  cursor: pointer;
  gap: 4px;
  user-select: none;
  transition: background 0.15s;
}

.module-item:hover {
  background: #f5f7fa;
}

.module-item.active {
  background: #e8f0fe;
  color: #4080ff;
}

.module-item.active .module-item-name {
  color: #4080ff;
  font-weight: 500;
}

.module-item-name {
  flex: 1;
  font-size: 13px;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.module-item-actions {
  display: none;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.module-item:hover .module-item-actions,
.module-item.active .module-item-actions {
  display: flex;
}

.mi-icon {
  font-size: 14px;
  padding: 3px;
  border-radius: 3px;
  cursor: pointer;
  color: #9ca3af;
  transition: color 0.15s, background 0.15s;
}

.mi-icon:hover {
  color: #4080ff;
  background: #dbeafe;
}

.mi-icon.danger:hover {
  color: #f53f3f;
  background: #fef2f2;
}

/* ---- 右侧 ---- */
.right-panel {
  flex: 1;
  overflow-y: auto;
  padding: 24px 32px;
  background: #f0f2f5;
}

.right-panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.right-title {
  font-size: 16px;
  font-weight: 600;
  color: #1d2129;
}

.right-empty {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

/* 面包屑 */
.breadcrumb {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #86909c;
  margin-bottom: 14px;
}

.breadcrumb .el-icon {
  font-size: 12px;
}

.breadcrumb-item {
  color: #86909c;
}

.breadcrumb-item.active {
  color: #4080ff;
  font-weight: 500;
}

/* 子模块行 */
.child-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 20px;
  max-width: 560px;
}

.child-row {
  display: flex;
  align-items: center;
  gap: 8px;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px 12px;
  transition: border-color 0.15s;
}

.child-row:hover {
  border-color: #4080ff;
}

.child-name-input {
  flex: 1;
}

.child-name-input :deep(.el-input__wrapper) {
  box-shadow: none;
  padding: 0;
  background: transparent;
}

.child-name-input :deep(.el-input__inner) {
  font-size: 13px;
}

.child-row-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.row-icon {
  font-size: 14px;
  padding: 4px;
  border-radius: 4px;
  cursor: pointer;
  color: #9ca3af;
  transition: color 0.15s, background 0.15s;
}

.row-icon:hover {
  color: #4080ff;
  background: #dbeafe;
}

.row-icon.danger:hover {
  color: #f53f3f;
  background: #fef2f2;
}

/* 底部按钮 */
.right-actions {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}
</style>
