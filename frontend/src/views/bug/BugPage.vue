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
          <el-button v-if="canCreateBug" type="primary" size="small" :icon="Plus" @click="router.push(`/projects/${projectId}/bugs/create`)">报Bug</el-button>
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
                  v-if="canAssignBug(row)"
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
          <el-table-column label="操作" width="170" align="center">
            <template #default="{ row }">
              <div class="op-btns">
                <el-button v-if="canResolveBug(row)" size="small" type="success" plain @click.stop="resolve(row)">解决</el-button>
                <el-button v-if="canCloseBug(row)" size="small" type="danger" plain @click.stop="close(row)">关闭</el-button>
                <el-button v-if="canReopenBug(row)" size="small" type="warning" plain @click.stop="reopen(row)">重开</el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 空状态 -->
        <div v-if="!loading && list.length === 0" class="empty-state">
          <div class="empty-icon">🐛</div>
          <p class="empty-text">暂时没有 Bug。</p>
          <el-button v-if="canCreateBug" type="primary" :icon="Plus" @click="router.push(`/projects/${projectId}/bugs/create`)">报Bug</el-button>
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

    <!-- 解决 Bug 对话框 -->
    <el-dialog v-model="showResolve" :title="`解决Bug  ${resolveTarget?.title ?? ''}  #${resolveTarget?.id ?? ''}`"
      width="560px" destroy-on-close :close-on-click-modal="false">
      <el-form ref="resolveFormRef" :model="resolveForm" :rules="resolveRules" label-width="80px" label-position="left">
        <el-form-item label="解决方案" prop="resolveType" required>
          <el-select v-model="resolveForm.resolveType" placeholder="请选择" style="width:220px">
            <el-option label="已修复" value="FIXED" />
            <el-option label="无法重现" value="WONT_REPRODUCE" />
            <el-option label="重复Bug" value="DUPLICATE" />
            <el-option label="设计如此" value="BY_DESIGN" />
            <el-option label="外部原因" value="EXTERNAL" />
            <el-option label="挂起" value="SUSPENDED" />
          </el-select>
        </el-form-item>
        <el-form-item label="解决日期">
          <el-date-picker v-model="resolveForm.resolvedDate" type="datetime" placeholder="选择解决日期"
            format="YYYY-MM-DD HH:mm" value-format="YYYY-MM-DD HH:mm" style="width:220px" />
        </el-form-item>
        <el-form-item label="指派给">
          <el-select v-model="resolveForm.assigneeId" placeholder="选择成员" clearable style="width:220px">
            <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="resolveForm.note" type="textarea" :rows="3"
            placeholder="可以在编辑器直接贴图。" resize="none" style="width:100%" />
        </el-form-item>
      </el-form>
      <el-button type="primary" style="display:block;margin:16px auto 0;width:120px" :loading="resolving" @click="submitResolve">
        解决
      </el-button>
      <div class="dlg-history">
        <div class="dlg-history-title">历史记录</div>
        <div v-if="dialogComments.length === 0" class="dlg-history-empty">暂无记录</div>
        <div v-for="(c, i) in dialogComments" :key="c.id" class="dlg-history-item">
          <span class="dhi-idx">{{ i + 1 }}</span>
          <span class="dhi-text">{{ fmtDate(c.createdAt) }}，由 <b>{{ c.username || c.userId }}</b> {{ c.content }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showResolve = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 关闭 Bug 对话框 -->
    <el-dialog v-model="showClose" :title="`关闭Bug  ${closeTarget?.title ?? ''}  #${closeTarget?.id ?? ''}`"
      width="560px" destroy-on-close :close-on-click-modal="false">
      <el-form :model="closeForm" label-width="80px" label-position="left">
        <el-form-item label="备注">
          <el-input v-model="closeForm.note" type="textarea" :rows="4"
            placeholder="可以在编辑器直接贴图。" resize="none" style="width:100%" />
        </el-form-item>
      </el-form>
      <el-button type="primary" style="display:block;margin:16px auto 0;width:120px" :loading="closing" @click="submitClose">
        关闭
      </el-button>
      <div class="dlg-history">
        <div class="dlg-history-title">历史记录</div>
        <div v-if="dialogComments.length === 0" class="dlg-history-empty">暂无记录</div>
        <div v-for="(c, i) in dialogComments" :key="c.id" class="dlg-history-item">
          <span class="dhi-idx">{{ i + 1 }}</span>
          <span class="dhi-text">{{ fmtDate(c.createdAt) }}，由 <b>{{ c.username || c.userId }}</b> {{ c.content }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showClose = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 激活 Bug 对话框（重开）-->
    <el-dialog v-model="showReopen" :title="`激活Bug  ${reopenTarget?.title ?? ''}  #${reopenTarget?.id ?? ''}`"
      width="560px" destroy-on-close :close-on-click-modal="false">
      <el-form :model="reopenForm" label-width="80px" label-position="left">
        <el-form-item label="指派给">
          <el-select v-model="reopenForm.assigneeId" placeholder="选择处理人（可选）" clearable style="width:240px">
            <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="reopenForm.note" type="textarea" :rows="3"
            placeholder="可以在编辑器直接贴图。" resize="none" style="width:100%" />
        </el-form-item>
      </el-form>
      <el-button type="primary" style="display:block;margin:16px auto 0;width:120px" :loading="reopening" @click="submitReopen">
        激活
      </el-button>
      <div class="dlg-history">
        <div class="dlg-history-title">历史记录</div>
        <div v-if="dialogComments.length === 0" class="dlg-history-empty">暂无记录</div>
        <div v-for="(c, i) in dialogComments" :key="c.id" class="dlg-history-item">
          <span class="dhi-idx">{{ i + 1 }}</span>
          <span class="dhi-text">{{ fmtDate(c.createdAt) }}，由 <b>{{ c.username || c.userId }}</b> {{ c.content }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="showReopen = false">取消</el-button>
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
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'

const route = useRoute()
const router = useRouter()
const projectId = Number(route.params.id)
const authStore = useAuthStore()
const projectStore = useProjectStore()
const currentUserId = computed(() => authStore.user?.userId ?? null)
const isProjectManager = computed(() => projectStore.currentProject?.id === projectId && projectStore.currentProject?.canEdit === true)
const canCreateBug = computed(() => projectStore.currentProject?.id === projectId)

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
  if (moduleDisplayMode.value === 'first') return parts[0] ?? module
  if (moduleDisplayMode.value === 'last') return parts[parts.length - 1] ?? module
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

async function loadProject() {
  try {
    const res = await projectApi.get(projectId)
    projectStore.setCurrentProject((res as any).data ?? res)
  } catch {
    projectStore.setCurrentProject(null)
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
const UNRESOLVED_STATUSES = ['ACTIVE', 'RESOLVED']

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
    const res = await bugApi.list(params)
    const data = (res as any).data
    let records: any[] = data.records ?? []

    // 前端过滤 unresolved（后端若不支持 statuses 参数则降级）
    if (quickTab.value === 'unresolved') {
      records = records.filter(r => UNRESOLVED_STATUSES.includes(normalizeBugStatus(r.status)))
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

// ---- 弹窗公共历史记录 ----
const dialogComments = ref<any[]>([])
async function loadDialogComments(bugId: number) {
  try {
    const res = await bugApi.listComments(bugId)
    dialogComments.value = (res as any).data ?? res ?? []
  } catch {
    dialogComments.value = []
  }
}
function fmtDate(val: string) { return val ? String(val).slice(0, 16).replace('T', ' ') : '' }

// ---- 解决弹窗 ----
const showResolve = ref(false)
const resolving = ref(false)
const resolveTarget = ref<any>(null)
const resolveFormRef = ref()
const resolveForm = ref({ resolveType: '', resolvedDate: '', assigneeId: null as number | null, note: '' })
const resolveRules = { resolveType: [{ required: true, message: '请选择解决方案', trigger: 'change' }] }

function resolve(row: any) {
  resolveTarget.value = row
  resolveForm.value = {
    resolveType: '',
    resolvedDate: new Date().toISOString().slice(0, 16).replace('T', ' '),
    assigneeId: row.assigneeId ?? null, note: ''
  }
  dialogComments.value = []
  showResolve.value = true
  loadDialogComments(row.id)
}

async function submitResolve() {
  await resolveFormRef.value?.validate()
  resolving.value = true
  try {
    if (resolveForm.value.assigneeId && resolveForm.value.assigneeId !== resolveTarget.value?.assigneeId) {
      await bugApi.assign(resolveTarget.value.id, resolveForm.value.assigneeId)
    }
    await bugApi.updateStatus(resolveTarget.value.id, 'RESOLVED', resolveForm.value.resolveType)
    if (resolveForm.value.note.trim()) {
      await bugApi.addComment(resolveTarget.value.id, `[解决备注] ${resolveForm.value.note.trim()}`)
    }
    showResolve.value = false
    ElMessage.success('Bug 已标记为解决')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '操作失败')
  } finally {
    resolving.value = false
  }
}

// ---- 关闭弹窗 ----
const showClose = ref(false)
const closing = ref(false)
const closeTarget = ref<any>(null)
const closeForm = ref({ note: '' })

function close(row: any) {
  closeTarget.value = row
  closeForm.value = { note: '' }
  dialogComments.value = []
  showClose.value = true
  loadDialogComments(row.id)
}

async function submitClose() {
  closing.value = true
  try {
    await bugApi.updateStatus(closeTarget.value.id, 'CLOSED')
    if (closeForm.value.note.trim()) {
      await bugApi.addComment(closeTarget.value.id, `[关闭] ${closeForm.value.note.trim()}`)
    }
    showClose.value = false
    ElMessage.success('Bug 已关闭')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '操作失败')
  } finally {
    closing.value = false
  }
}

// ---- 激活（重开）弹窗 ----
const showReopen = ref(false)
const reopening = ref(false)
const reopenTarget = ref<any>(null)
const reopenForm = ref({ assigneeId: null as number | null, note: '' })

function reopen(row: any) {
  reopenTarget.value = row
  reopenForm.value = { assigneeId: row.assigneeId ?? null, note: '' }
  dialogComments.value = []
  showReopen.value = true
  loadDialogComments(row.id)
}

async function submitReopen() {
  reopening.value = true
  try {
    await bugApi.updateStatus(reopenTarget.value.id, 'ACTIVE')
    if (reopenForm.value.assigneeId && reopenForm.value.assigneeId !== reopenTarget.value?.assigneeId) {
      await bugApi.assign(reopenTarget.value.id, reopenForm.value.assigneeId)
    }
    const parts: string[] = []
    if (reopenForm.value.note.trim()) parts.push(reopenForm.value.note.trim())
    if (parts.length) await bugApi.addComment(reopenTarget.value.id, `[激活] ${parts.join('；')}`)
    showReopen.value = false
    ElMessage.success('Bug 已激活')
    await load()
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '操作失败')
  } finally {
    reopening.value = false
  }
}

// ---- 导出 ----
function handleExport() {
  ElMessage.info('导出功能即将上线')
}

// ---- 辅助函数 ----
type TagType = 'success' | 'warning' | 'info' | 'danger'

function severityType(s: string): TagType {
  const m: Record<string, TagType> = { BLOCKER: 'danger', CRITICAL: 'warning', NORMAL: 'info', MINOR: 'info' }
  return m[s] ?? 'info'
}

function severityLabel(s: string) {
  const m: Record<string, string> = { BLOCKER: '阻塞', CRITICAL: '严重', NORMAL: '一般', MINOR: '轻微' }
  return m[s] ?? s
}

function normalizeBugStatus(status: string) {
  if (['NEW', 'CONFIRMED', 'ASSIGNED'].includes(status)) return 'ACTIVE'
  if (status === 'VERIFIED') return 'RESOLVED'
  return status
}

function isClosedStatus(status: string) {
  return normalizeBugStatus(status) === 'CLOSED'
}

function canResolveStatus(status: string) {
  return normalizeBugStatus(status) === 'ACTIVE'
}

function canCloseStatus(status: string) {
  return ['ACTIVE', 'RESOLVED'].includes(normalizeBugStatus(status))
}

function canReopenStatus(status: string) {
  return ['RESOLVED', 'CLOSED'].includes(normalizeBugStatus(status))
}

function isAssignee(row: { assigneeId?: number | null }) {
  return currentUserId.value != null && row.assigneeId != null && row.assigneeId === currentUserId.value
}

function canOperateBug(row: { assigneeId?: number | null }) {
  return isProjectManager.value || isAssignee(row)
}

function canAssignBug(row: { status: string }) {
  return isProjectManager.value && !isClosedStatus(row.status)
}

function canResolveBug(row: { status: string; assigneeId?: number | null }) {
  return canOperateBug(row) && canResolveStatus(row.status)
}

function canCloseBug(row: { status: string; assigneeId?: number | null }) {
  return canOperateBug(row) && canCloseStatus(row.status)
}

function canReopenBug(row: { status: string; assigneeId?: number | null }) {
  return canOperateBug(row) && canReopenStatus(row.status)
}

function statusType(s: string): TagType {
  const m: Record<string, TagType> = { ACTIVE: 'warning', RESOLVED: 'success', CLOSED: 'info' }
  return m[normalizeBugStatus(s)] ?? 'info'
}

function statusLabel(s: string) {
  const m: Record<string, string> = { ACTIVE: '已激活', RESOLVED: '已解决', CLOSED: '已关闭' }
  return m[normalizeBugStatus(s)] ?? s
}

function formatDate(d: string) {
  if (!d) return '—'
  return d.replace('T', ' ').slice(0, 16)
}

onMounted(async () => {
  await Promise.all([loadProject(), loadModules(), loadMembers()])
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

.op-btns {
  display: flex;
  gap: 4px;
  justify-content: center;
  flex-wrap: nowrap;
}

/* ---- 弹窗历史记录 ---- */
.dlg-history {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
}
.dlg-history-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 10px;
}
.dlg-history-empty {
  font-size: 13px;
  color: #9ca3af;
  padding: 4px 0;
}
.dlg-history-item {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 5px 0;
  font-size: 13px;
  color: #374151;
  border-bottom: 1px dashed #f3f4f6;
}
.dlg-history-item:last-child { border-bottom: none; }
.dhi-idx {
  min-width: 20px;
  color: #9ca3af;
  font-size: 12px;
  padding-top: 1px;
}
.dhi-text { flex: 1; line-height: 1.5; }

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
