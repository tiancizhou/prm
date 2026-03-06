<template>
  <div class="bug-page">
    <!-- 左侧模块侧栏 -->
    <div class="module-sidebar" :class="{ collapsed: sidebarCollapsed }">
      <template v-if="!sidebarCollapsed">
        <!-- 顶部：全部 + 操作图标 -->
        <div class="sidebar-top">
          <div
            class="sidebar-all"
            :class="{ active: selectedModule === null }"
            @click="selectModule(null)"
          >全部</div>
          <div class="sidebar-icons">
            <el-tooltip content="显示设置" placement="top">
              <el-button circle size="default" @click="openSettings">
                <el-icon style="font-size:18px"><Setting /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="收起侧栏" placement="top">
              <el-button circle size="default" @click="sidebarCollapsed = true">
                <el-icon style="font-size:18px"><DArrowLeft /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
        </div>

        <ul class="module-list">
          <template v-for="m in moduleTree" :key="m.id">
            <li class="module-item" :class="{ active: selectedModule === m.name }">
              <!-- 展开/收起箭头 -->
              <span
                v-if="m.children?.length"
                class="tree-arrow"
                :class="{ expanded: expandedIds.has(m.id) }"
                @click.stop="toggleExpand(m.id)"
              >
                <el-icon><ArrowRight /></el-icon>
              </span>
              <span v-else class="tree-arrow-placeholder" />
              <span class="module-name" @click="selectModule(m.name)">{{ m.name }}</span>
            </li>
            <!-- 子模块（当父级展开时显示） -->
            <template v-if="m.children?.length && expandedIds.has(m.id)">
              <li
                v-for="child in m.children"
                :key="child.id"
                class="module-item module-item--child"
                :class="{ active: selectedModule === child.name }"
                @click="selectModule(child.name)"
              >
                <span class="tree-arrow-placeholder" />
                <span class="module-name">{{ child.name }}</span>
              </li>
            </template>
          </template>
        </ul>
      </template>

      <!-- 折叠状态：只显示展开箭头 -->
      <template v-else>
        <div class="sidebar-expand">
          <el-button circle size="default" @click="sidebarCollapsed = false">
            <el-icon style="font-size:18px"><DArrowRight /></el-icon>
          </el-button>
        </div>
      </template>
    </div>

    <!-- 右侧内容区 -->
    <div class="content-area">
      <!-- 顶部 Tabs + 操作栏 -->
      <div class="top-bar">
        <div class="quick-tabs">
          <button
            class="qtab"
            :class="{ active: quickTab === 'all' }"
            @click="quickTab = 'all'; load()"
          >全部</button>
          <button
            class="qtab"
            :class="{ active: quickTab === 'unresolved' }"
            @click="quickTab = 'unresolved'; load()"
          >未解决</button>
        </div>

        <div class="top-actions">
          <el-input
            v-model="keyword"
            placeholder="搜索"
            :prefix-icon="Search"
            clearable
            size="small"
            style="width: 160px"
            @change="load"
          />
          <el-button size="small" :icon="Download" @click="handleExport">导出</el-button>
          <el-button type="primary" size="small" :icon="Plus" @click="router.push(`/projects/${projectId}/bugs/create`)">报Bug</el-button>
        </div>
      </div>

      <!-- 表格 -->
      <div class="table-wrap">
        <el-table
          :data="list"
          v-loading="loading"
          class="bug-table"
          :empty-text="' '"
          row-class-name="bug-row"
        >
          <el-table-column prop="id" label="ID" width="64" align="center">
            <template #default="{ row }">
              <span class="bug-id">#{{ row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column label="标题" min-width="240">
            <template #default="{ row }">
              <span class="bug-title title-link" @click="router.push(`/projects/${projectId}/bugs/${row.id}`)">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column v-if="moduleDisplayCol" label="模块" width="110">
            <template #default="{ row }">
              <span v-if="row.module" class="module-tag">{{ moduleDisplayText(row.module) }}</span>
              <span v-else class="text-muted">—</span>
            </template>
          </el-table-column>
          <el-table-column label="严重程度" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="severityType(row.severity)" size="small" round>
                {{ severityLabel(row.severity) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag :type="statusType(row.status)" size="small" round>
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="处理人" width="140">
            <template #default="{ row }">
              <div class="assignee-cell">
                <span class="assignee-name">{{ row.assigneeName || '未指派' }}</span>
                <el-button
                  v-if="row.status !== 'CLOSED'"
                  link
                  size="small"
                  type="primary"
                  class="inline-assign-btn"
                  @click.stop="assign(row)"
                >指派</el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="提报人" width="100">
            <template #default="{ row }">{{ row.reporterName || '—' }}</template>
          </el-table-column>
          <el-table-column label="提报时间" width="150">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="160" align="right" fixed="right">
            <template #default="{ row }">
              <el-button v-if="['ACTIVE','NEW','CONFIRMED','ASSIGNED'].includes(row.status)" size="small" link type="success" @click="resolve(row)">解决</el-button>
              <el-button v-if="['ACTIVE','NEW','CONFIRMED','ASSIGNED','RESOLVED','VERIFIED'].includes(row.status)" size="small" link type="danger" @click="close(row)">关闭</el-button>
              <el-button v-if="row.status === 'RESOLVED' || row.status === 'CLOSED' || row.status === 'VERIFIED'" size="small" link type="warning" @click="reopen(row)">重开</el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="!loading && list.length === 0" class="empty-state">
          <div class="empty-icon">🐛</div>
          <p class="empty-text">暂时没有 Bug。</p>
          <el-button type="primary" :icon="Plus" @click="router.push(`/projects/${projectId}/bugs/create`)">报Bug</el-button>
        </div>

        <!-- 分页 -->
        <div v-if="total > 0" class="pagination-bar">
          <span class="total-text">共 {{ total }} 条</span>
          <el-pagination
            v-model:current-page="page"
            v-model:page-size="pageSize"
            :total="total"
            :page-sizes="[20, 50, 100]"
            layout="sizes, prev, pager, next"
            small
            @change="load"
          />
        </div>
      </div>
    </div>


    <!-- 指派对话框 -->
    <el-dialog
      v-model="showAssign"
      :title="`指派  ${assignTarget?.title ?? ''}  #${assignTarget?.id ?? ''}`"
      width="520px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form :model="assignForm" label-width="72px" label-position="left">
        <el-form-item label="指派给">
          <el-select v-model="assignForm.assigneeId" placeholder="选择处理人" clearable style="width:240px">
            <el-option
              v-for="m in members"
              :key="m.userId"
              :label="m.nickname || m.username"
              :value="m.userId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="assignForm.note"
            type="textarea"
            :rows="4"
            placeholder="可填写指派说明..."
            resize="none"
            style="width:100%"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssign = false">取消</el-button>
        <el-button type="primary" :loading="assigning" @click="submitAssign">指派</el-button>
      </template>
    </el-dialog>

    <!-- 显示设置弹窗 -->
    <el-dialog v-model="showSettings" title="显示设置" width="480px" destroy-on-close>
      <el-form label-width="150px">
        <el-form-item label="列表页是否显示模块名">
          <el-radio-group v-model="pendingDisplayMode">
            <el-radio value="none">不显示</el-radio>
            <el-radio value="first">只显示一级模块</el-radio>
            <el-radio value="last">只显示最后一级模块</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button type="primary" @click="saveSettings">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Plus, Search, Download, Setting, DArrowLeft, DArrowRight, ArrowRight } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { bugApi } from '@/api/bug'
import { moduleApi, type ModuleDTO } from '@/api/module'
import { projectApi } from '@/api/project'

const route = useRoute()
const router = useRouter()
const projectId = Number(route.params.id)

// ---- 侧栏折叠 ----
const sidebarCollapsed = ref(false)

// ---- 显示设置 ----
const showSettings = ref(false)
// none | first | last
const moduleDisplayMode = ref<'none' | 'first' | 'last'>('none')
const pendingDisplayMode = ref<'none' | 'first' | 'last'>('none')

const moduleDisplayCol = computed(() => moduleDisplayMode.value !== 'none')

function moduleDisplayText(module: string): string {
  if (!module) return '—'
  const parts = module.split('/')
  if (moduleDisplayMode.value === 'first') return parts[0]
  if (moduleDisplayMode.value === 'last') return parts[parts.length - 1]
  return module
}

function saveSettings() {
  moduleDisplayMode.value = pendingDisplayMode.value
  showSettings.value = false
}

// 打开设置时同步当前值
const openSettings = () => {
  pendingDisplayMode.value = moduleDisplayMode.value
  showSettings.value = true
}

// ---- 模块侧栏 ----
const moduleTree = ref<ModuleDTO[]>([])
const expandedIds = ref<Set<number>>(new Set())
const selectedModule = ref<string | null>(null)

// 用于下拉选择的扁平列表
const modules = ref<Array<{ id: number; name: string }>>([])

function flatForSelect(nodes: ModuleDTO[]): Array<{ id: number; name: string }> {
  const result: Array<{ id: number; name: string }> = []
  for (const n of nodes) {
    result.push({ id: n.id, name: n.name })
    if (n.children?.length) result.push(...flatForSelect(n.children))
  }
  return result
}

function toggleExpand(id: number) {
  if (expandedIds.value.has(id)) {
    expandedIds.value.delete(id)
  } else {
    expandedIds.value.add(id)
  }
}

async function loadModules() {
  try {
    const res = await moduleApi.listTree(projectId)
    const tree: ModuleDTO[] = (res as any)?.data ?? res ?? []
    moduleTree.value = tree
    modules.value = flatForSelect(tree)
  } catch {
    moduleTree.value = []
    modules.value = []
  }
}

/** 收集指定模块名及其所有后代模块名列表 */
function collectModuleNames(targetName: string, nodes: ModuleDTO[]): string[] {
  for (const n of nodes) {
    if (n.name === targetName) {
      const names: string[] = [n.name]
      function collectChildren(children: ModuleDTO[] | undefined) {
        for (const c of children ?? []) {
          names.push(c.name)
          collectChildren(c.children)
        }
      }
      collectChildren(n.children)
      return names
    }
    const found = collectModuleNames(targetName, n.children ?? [])
    if (found.length > 0) return found
  }
  return []
}

function selectModule(name: string | null) {
  selectedModule.value = name
  page.value = 1
  load()
}

// ---- 快速 Tab ----
const quickTab = ref<'all' | 'unresolved'>('all')
const UNRESOLVED_STATUSES = ['ACTIVE']

// ---- 列表 ----
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')

async function load() {
  loading.value = true
  try {
    const params: any = {
      projectId,
      page: page.value,
      size: pageSize.value,
      keyword: keyword.value || undefined,
      modules: selectedModule.value
        ? collectModuleNames(selectedModule.value, moduleTree.value).join(',')
        : undefined
    }
    if (quickTab.value === 'unresolved') {
      params.statuses = UNRESOLVED_STATUSES.join(',')
    }
    const res = await bugApi.list(params)
    const data = (res as any).data
    let records: any[] = data.records ?? []

    // 前端过滤 unresolved（后端若不支持 statuses 参数则降级）
    if (quickTab.value === 'unresolved') {
      records = records.filter(r => UNRESOLVED_STATUSES.includes(r.status))
    }
    list.value = records
    total.value = quickTab.value === 'unresolved' ? records.length : (data.total ?? records.length)
  } finally {
    loading.value = false
  }
}

// ---- 成员 ----
const members = ref<any[]>([])
async function loadMembers() {
  try {
    const res = await projectApi.getMembers(projectId)
    members.value = (res as any).data || []
  } catch {
    members.value = []
  }
}


// ---- 指派 ----
const showAssign = ref(false)
const assigning = ref(false)
const assignTarget = ref<any>(null)
const assignForm = ref({ assigneeId: null as number | null, note: '' })

function assign(row: any) {
  assignTarget.value = row
  assignForm.value = { assigneeId: row.assigneeId ?? null, note: '' }
  showAssign.value = true
}

async function submitAssign() {
  if (!assignForm.value.assigneeId) { ElMessage.warning('请选择处理人'); return }
  assigning.value = true
  try {
    await bugApi.assign(assignTarget.value.id, assignForm.value.assigneeId)
    if (assignForm.value.note.trim()) {
      await bugApi.addComment(assignTarget.value.id, `[指派备注] ${assignForm.value.note.trim()}`)
    }
    ElMessage.success('指派成功')
    showAssign.value = false
    await load()
  } finally {
    assigning.value = false
  }
}

// ---- 状态变更 ----
async function resolve(row: any) {
  await bugApi.updateStatus(row.id, 'RESOLVED', 'FIXED')
  ElMessage.success('已标记解决')
  await load()
}

async function verify(row: any) {
  await bugApi.updateStatus(row.id, 'CLOSED')
  ElMessage.success('已关闭')
  await load()
}

async function close(row: any) {
  await bugApi.updateStatus(row.id, 'CLOSED')
  ElMessage.success('已关闭')
  await load()
}

async function reopen(row: any) {
  try {
    await bugApi.updateStatus(row.id, 'ACTIVE')
    ElMessage.success('已重开')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '重开失败')
  }
}

function canClose(row: any) {
  return ['ACTIVE', 'NEW', 'CONFIRMED', 'ASSIGNED', 'RESOLVED', 'VERIFIED'].includes(row.status)
}

// ---- 导出 ----
function handleExport() {
  ElMessage.info('导出功能即将上线')
}

// ---- 辅助函数 ----
type TagType = '' | 'success' | 'warning' | 'info' | 'danger'

function severityType(s: string): TagType {
  const m: Record<string, TagType> = { BLOCKER: 'danger', CRITICAL: 'warning', NORMAL: '', MINOR: 'info' }
  return m[s] ?? ''
}

function severityLabel(s: string) {
  const m: Record<string, string> = { BLOCKER: '阻塞', CRITICAL: '严重', NORMAL: '一般', MINOR: '轻微' }
  return m[s] ?? s
}

function statusType(s: string): TagType {
  const m: Record<string, TagType> = {
    ACTIVE: 'warning', RESOLVED: 'success', CLOSED: 'info',
    NEW: 'warning', CONFIRMED: 'warning', ASSIGNED: 'warning', VERIFIED: 'info'
  }
  return m[s] ?? 'info'
}

function statusLabel(s: string) {
  const m: Record<string, string> = {
    ACTIVE: '已激活', RESOLVED: '已解决', CLOSED: '已关闭',
    NEW: '已激活', CONFIRMED: '已激活', ASSIGNED: '已激活', VERIFIED: '已关闭'
  }
  return m[s] ?? s
}

function formatDate(d: string) {
  if (!d) return '—'
  return d.replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  await Promise.all([loadModules(), loadMembers()])
  await load()
})
</script>

<style scoped>
.bug-page {
  display: flex;
  height: 100%;
  background: #f5f7fa;
  overflow: hidden;
}

/* ---- 左侧模块侧栏 ---- */
.module-sidebar {
  width: 160px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid #e5e7eb;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: width 0.2s ease;
}

.module-sidebar.collapsed {
  width: 36px;
}

/* 顶部行：全部 + 图标 */
.sidebar-top {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 10px 8px 6px;
  flex-shrink: 0;
}

/* 全部 - 带右箭头高亮样式 */
.sidebar-all {
  flex: 1;
  padding: 6px 12px;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
  border-radius: 4px;
  background: #f3f4f6;
  text-align: center;
  clip-path: polygon(0 0, calc(100% - 8px) 0, 100% 50%, calc(100% - 8px) 100%, 0 100%);
  transition: background 0.15s, color 0.15s;
  white-space: nowrap;
  overflow: hidden;
}

.sidebar-all.active {
  background: #dbeafe;
  color: #1d4ed8;
}

.sidebar-all:hover:not(.active) {
  background: #e5e7eb;
}

/* 顶部图标组 */
.sidebar-icons {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.module-list {
  list-style: none;
  margin: 0;
  padding: 4px 0;
  flex: 1;
  overflow-y: auto;
}

.module-item {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 5px 8px;
  font-size: 13px;
  color: #374151;
  line-height: 1.4;
  border-radius: 4px;
  margin: 1px 4px;
  transition: background 0.15s, color 0.15s;
}

.module-item:hover {
  background: #f3f4f6;
}

.module-item.active .module-name {
  color: #4080ff;
  font-weight: 500;
}

.module-item--child {
  padding-left: 20px;
}

/* 展开箭头 */
.tree-arrow {
  flex-shrink: 0;
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #9ca3af;
  cursor: pointer;
  border-radius: 3px;
  transition: transform 0.2s, color 0.15s;
}

.tree-arrow:hover {
  color: #4080ff;
  background: #eff6ff;
}

.tree-arrow.expanded {
  transform: rotate(90deg);
  color: #4080ff;
}

.tree-arrow-placeholder {
  flex-shrink: 0;
  width: 18px;
}

.module-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: pointer;
}

.module-name:hover {
  color: #4080ff;
}

.footer-icon {
  font-size: 20px;
  color: #6b7280;
  cursor: pointer;
  padding: 6px;
  border-radius: 6px;
  transition: color 0.15s, background 0.15s;
  flex-shrink: 0;
}

.footer-icon:hover {
  color: #4080ff;
  background: #eff6ff;
}

/* 折叠后展开按钮 */
.sidebar-expand {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  cursor: pointer;
  color: #9ca3af;
  font-size: 16px;
  transition: color 0.15s;
}

.sidebar-expand:hover {
  color: #4080ff;
}

/* 模块列标签 */
.module-tag {
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 1px 6px;
  border-radius: 4px;
}

.text-muted {
  color: #9ca3af;
}

/* ---- 右侧内容区 ---- */
.content-area {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ---- 顶部操作栏 ---- */
.top-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
  gap: 12px;
}

.quick-tabs {
  display: flex;
  gap: 2px;
}

.qtab {
  padding: 5px 14px;
  font-size: 13px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s, color 0.15s;
}

.qtab:hover {
  background: #f3f4f6;
  color: #374151;
}

.qtab.active {
  background: #eff6ff;
  color: #4080ff;
  font-weight: 500;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ---- 表格区 ---- */
.table-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bug-table {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.bug-table :deep(th) {
  background: #f9fafb !important;
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.3px;
}

.bug-table :deep(.bug-row:hover td) {
  background: #f5f7fa !important;
}

.bug-id {
  font-size: 12px;
  color: #9ca3af;
  font-family: monospace;
}

.assignee-cell {
  display: flex;
  align-items: center;
  gap: 4px;
}
.assignee-name {
  color: #374151;
  font-size: 13px;
  flex-shrink: 0;
}
.inline-assign-btn {
  opacity: 0;
  transition: opacity 0.15s;
  font-size: 12px;
  padding: 0;
  height: auto;
}
.bug-row:hover .inline-assign-btn {
  opacity: 1;
}

.bug-title {
  font-size: 13px;
  color: #1d2129;
  font-weight: 500;
}

.title-link {
  cursor: pointer;
  transition: color 0.15s;
}
.title-link:hover {
  color: #4080ff;
  text-decoration: underline;
}

/* 空状态 */
.empty-state {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 60px 0;
  color: #9ca3af;
}

.empty-icon {
  font-size: 40px;
  opacity: 0.4;
}

.empty-text {
  font-size: 14px;
  margin: 0;
}

/* 分页 */
.pagination-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 4px;
}

.total-text {
  font-size: 13px;
  color: #9ca3af;
}
</style>
