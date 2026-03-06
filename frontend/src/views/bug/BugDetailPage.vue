<template>
  <div class="bug-detail-page">
    <!-- 顶部面包屑 -->
    <div class="detail-header">
      <div class="header-left">
        <el-button link :icon="ArrowLeft" @click="goBack" class="back-btn">返回</el-button>
        <span class="divider">/</span>
        <span class="breadcrumb">Bug</span>
        <span class="divider">/</span>
        <span class="bug-id-crumb">#{{ bugId }}</span>
        <span class="bug-title-crumb">{{ bug?.title }}</span>
      </div>
      <el-button type="primary" size="small" :icon="Plus"
        @click="$router.push(`/projects/${projectId}/bugs/create`)">
        + 报Bug
      </el-button>
    </div>

    <!-- 操作工具栏（面包屑下方，随页面流动）-->
    <div v-if="bug" class="action-toolbar">
      <!-- 左侧：状态流转主操作 -->
      <div class="toolbar-left">
        <template v-if="['ACTIVE','NEW','CONFIRMED','ASSIGNED'].includes(bug.status)">
          <el-button type="success" size="default" @click="openResolveDialog">
            ✓ 解决
          </el-button>
        </template>
        <template v-if="['RESOLVED','CLOSED','VERIFIED'].includes(bug.status)">
          <el-button size="default" @click="openReopenDialog">
            ↺ 重开
          </el-button>
        </template>
        <template v-if="['ACTIVE','NEW','CONFIRMED','ASSIGNED','RESOLVED','VERIFIED'].includes(bug.status)">
          <el-button size="default" type="danger" plain @click="openCloseDialog">
            关闭
          </el-button>
        </template>
      </div>

      <!-- 右侧：辅助操作 -->
      <div class="toolbar-right">
        <el-button
          size="default"
          plain
          @click="confirmConvert"
          :disabled="bug.status === 'CLOSED'"
        >转研发需求</el-button>
        <el-button size="default" plain :icon="EditPen"
          @click="$router.push(`/projects/${projectId}/bugs/${bugId}/edit`)">
          编辑
        </el-button>
        <el-button size="default" plain :icon="Delete" type="danger" @click="handleDelete">
          删除
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-wrap">
      <el-skeleton :rows="8" animated />
    </div>

    <template v-else-if="bug">
      <div class="detail-body">
        <!-- 左侧主内容 -->
        <div class="detail-main">
          <!-- 标题 -->
          <div class="main-title">{{ bug.title }}</div>

          <!-- 重现步骤 -->
          <div class="section">
            <div class="section-label">重现步骤</div>
            <div class="section-content pre-wrap">{{ bug.steps || '—' }}</div>
          </div>

          <div class="section-row">
            <div class="section" style="flex:1">
              <div class="section-label">期望结果</div>
              <div class="section-content pre-wrap">{{ bug.expectedResult || '—' }}</div>
            </div>
            <div class="section" style="flex:1">
              <div class="section-label">实际结果</div>
              <div class="section-content pre-wrap">{{ bug.actualResult || '—' }}</div>
            </div>
          </div>

          <div v-if="bug.description" class="section">
            <div class="section-label">描述</div>
            <div class="section-content pre-wrap">{{ bug.description }}</div>
          </div>

          <!-- 历史记录 -->
          <div class="section history-section">
            <div class="section-label-row">
              <span class="section-label">历史记录</span>
              <el-button link size="small" :icon="ChatLineRound" @click="showCommentInput = !showCommentInput">
                添加备注
              </el-button>
            </div>

            <div v-if="showCommentInput" class="comment-input-wrap">
              <el-input
                v-model="commentText"
                type="textarea"
                :rows="3"
                placeholder="添加备注内容..."
                resize="none"
              />
              <div class="comment-actions">
                <el-button size="small" @click="showCommentInput = false; commentText = ''">取消</el-button>
                <el-button size="small" type="primary" :loading="commentSubmitting" @click="submitComment">
                  提交备注
                </el-button>
              </div>
            </div>

            <div v-if="comments.length === 0 && !showCommentInput" class="empty-comments">
              暂无记录
            </div>
            <div v-else class="comment-list">
              <div v-for="c in comments" :key="c.id" class="comment-item">
                <div class="comment-meta">
                  <span class="comment-author">{{ c.username || c.userId }}</span>
                  <span class="comment-time">{{ formatDate(c.createdAt) }}</span>
                </div>
                <div class="comment-body">{{ c.content }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧信息栏 -->
        <div class="detail-sidebar">
          <el-tabs v-model="sideTab" class="side-tabs">
            <el-tab-pane label="基本信息" name="info" />
            <el-tab-pane label="Bug的一生" name="life" />
          </el-tabs>

          <template v-if="sideTab === 'info'">
            <div class="info-list">
              <div class="info-row">
                <span class="info-key">所属模块</span>
                <span class="info-val">{{ bug.module || '—' }}</span>
              </div>
              <div class="info-row">
                <span class="info-key">Bug类型</span>
                <span class="info-val">{{ bug.bugType || '代码错误' }}</span>
              </div>
              <div class="info-row">
                <span class="info-key">严重程度</span>
                <span class="info-val">
                  <el-tag :type="severityType(bug.severity)" size="small" round>
                    {{ severityLabel(bug.severity) }}
                  </el-tag>
                </span>
              </div>
              <div class="info-row">
                <span class="info-key">优先级</span>
                <span class="info-val">
                  <el-tag :type="priorityType(bug.priority)" size="small" round>
                    {{ priorityLabel(bug.priority) }}
                  </el-tag>
                </span>
              </div>
              <div class="info-row">
                <span class="info-key">Bug状态</span>
                <span class="info-val">
                  <el-tag :type="statusType(bug.status)" size="small" round>
                    {{ statusLabel(bug.status) }}
                  </el-tag>
                </span>
              </div>
              <div class="info-row" v-if="bug.resolveType">
                <span class="info-key">解决方案</span>
                <span class="info-val">{{ resolveTypeLabel(bug.resolveType) }}</span>
              </div>
              <div class="info-row">
                <span class="info-key">激活次数</span>
                <span class="info-val">—</span>
              </div>
              <div class="info-row">
                <span class="info-key">报源姓名</span>
                <span class="info-val">{{ bug.reporterName || '—' }}</span>
              </div>
              <div class="info-row">
                <span class="info-key">处理人</span>
                <span class="info-val assignee-val">
                  <span>{{ bug.assigneeName || '未指派' }}</span>
                  <el-button
                    v-if="bug.status !== 'CLOSED'"
                    link
                    size="small"
                    type="primary"
                    @click="openAssignDialog"
                    style="padding:0;height:auto;font-size:12px;margin-left:4px"
                  >指派</el-button>
                </span>
              </div>
              <div class="info-row">
                <span class="info-key">操作系统</span>
                <span class="info-val">{{ bug.environment || '—' }}</span>
              </div>
            </div>
          </template>

          <template v-else>
            <div class="life-timeline">
              <div v-for="(step, i) in lifeSteps" :key="i"
                class="life-step"
                :class="{ active: isActiveStatus(step.status), done: isDoneStatus(step.status) }">
                <div class="life-dot" />
                <div class="life-content">
                  <div class="life-name">{{ step.label }}</div>
                  <div class="life-time" v-if="step.time">{{ formatDate(step.time) }}</div>
                </div>
              </div>
            </div>
          </template>

          <!-- 关联信息 -->
          <div class="sidebar-section-title">项目/迭代/研发需求/任务</div>
          <div class="info-list">
            <div class="info-row">
              <span class="info-key">所属项目</span>
              <span class="info-val">{{ projectName }}</span>
            </div>
          </div>
        </div>
      </div>

    </template>

    <!-- 指派对话框 -->
    <el-dialog
      v-model="assignVisible"
      :title="`指派  ${bug?.title ?? ''}  #${bugId}`"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="assignForm" label-width="72px" label-position="left">
        <el-form-item label="指派给">
          <el-select v-model="assignForm.assigneeId" placeholder="选择处理人" clearable style="width:260px">
            <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="抄送给">
          <div style="display:flex;gap:6px;align-items:center;width:100%">
            <el-select
              v-model="assignForm.ccIds"
              multiple
              collapse-tags
              collapse-tags-tooltip
              placeholder="选择抄送人员"
              style="flex:1"
            >
              <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
            </el-select>
            <el-button :icon="UserFilled" circle size="small" title="从常用联系人选择" />
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="assignForm.note"
            type="textarea"
            :rows="5"
            placeholder="可以在编辑器直接贴图。"
            resize="none"
            style="width:100%"
          />
        </el-form-item>
      </el-form>

      <el-button type="primary" style="display:block;margin:16px auto 0;width:120px" @click="submitAssign" :loading="assigning">
        指派
      </el-button>

      <!-- 历史记录 -->
      <div class="assign-history">
        <div class="assign-history-title">
          <span>历史记录</span>
          <el-button link size="small" :icon="ChatLineRound" @click="showCommentInput = true">添加备注</el-button>
        </div>
        <div v-if="comments.length === 0" style="color:#9ca3af;font-size:13px;padding:8px 0">暂无记录</div>
        <div v-for="(c, i) in comments" :key="c.id" class="assign-history-item">
          <span class="ah-index">{{ i + 1 }}</span>
          <span class="ah-text">{{ formatDate(c.createdAt) }}，由 <b>{{ c.username || c.userId }}</b> {{ c.content }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 解决 Bug 对话框 -->
    <el-dialog
      v-model="resolveVisible"
      :title="`解决Bug  ${bug?.title ?? ''}  #${bugId}`"
      width="600px"
      :close-on-click-modal="false"
    >
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
        <el-form-item label="解决版本">
          <el-select v-model="resolveForm.resolvedVersion" placeholder="请选择" clearable style="width:180px">
            <el-option label="主干" value="TRUNK" />
            <el-option label="1.0" value="1.0" />
            <el-option label="2.0" value="2.0" />
          </el-select>
          <el-checkbox v-model="resolveForm.createVersion" label="创建" style="margin-left:10px" />
        </el-form-item>
        <el-form-item label="解决日期">
          <el-date-picker
            v-model="resolveForm.resolvedDate"
            type="datetime"
            placeholder="选择解决日期"
            format="YYYY-MM-DD HH:mm"
            value-format="YYYY-MM-DD HH:mm"
            style="width:220px"
          />
        </el-form-item>
        <el-form-item label="指派给">
          <el-select v-model="resolveForm.assigneeId" placeholder="选择成员" clearable style="width:220px">
            <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="resolveForm.note"
            type="textarea"
            :rows="4"
            placeholder="可以在编辑器直接贴图。"
            resize="none"
            style="width:100%"
          />
        </el-form-item>
      </el-form>

      <el-button type="primary" style="display:block;margin:16px auto 0;width:120px" :loading="resolving" @click="submitResolve">
        解决
      </el-button>

      <!-- 历史记录 -->
      <div class="assign-history">
        <div class="assign-history-title"><span>历史记录</span></div>
        <div v-if="comments.length === 0" style="color:#9ca3af;font-size:13px;padding:8px 0">暂无记录</div>
        <div v-for="(c, i) in comments" :key="c.id" class="assign-history-item">
          <span class="ah-index">{{ i + 1 }}</span>
          <span class="ah-text">{{ formatDate(c.createdAt) }}，由 <b>{{ c.username || c.userId }}</b> {{ c.content }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 激活 Bug 对话框（CLOSED/RESOLVED → ACTIVE）-->
    <el-dialog
      v-model="reopenVisible"
      :title="`激活Bug  ${bug?.title ?? ''}  #${bugId}`"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form ref="reopenFormRef" :model="reopenForm" :rules="reopenRules" label-width="80px" label-position="left">
        <el-form-item label="指派给">
          <el-select v-model="reopenForm.assigneeId" placeholder="选择处理人（可选）" clearable style="width:260px">
            <el-option v-for="m in members" :key="m.userId" :label="m.nickname || m.username" :value="m.userId" />
          </el-select>
        </el-form-item>
        <el-form-item label="影响版本" prop="affectedVersion" required>
          <el-select v-model="reopenForm.affectedVersion" placeholder="请选择" style="width:260px">
            <el-option label="主干" value="TRUNK" />
            <el-option label="1.0" value="1.0" />
            <el-option label="2.0" value="2.0" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="reopenForm.note"
            type="textarea"
            :rows="4"
            placeholder="可以在编辑器直接贴图。"
            resize="none"
            style="width:100%"
          />
        </el-form-item>
      </el-form>

      <el-button type="primary" style="display:block;margin:16px auto 0;width:120px" :loading="reopening" @click="submitReopen">
        激活
      </el-button>

      <!-- 历史记录 -->
      <div class="assign-history">
        <div class="assign-history-title"><span>历史记录</span></div>
        <div v-if="comments.length === 0" style="color:#9ca3af;font-size:13px;padding:8px 0">暂无记录</div>
        <div v-for="(c, i) in comments" :key="c.id" class="assign-history-item">
          <span class="ah-index">{{ i + 1 }}</span>
          <span class="ah-text">{{ formatDate(c.createdAt) }}，由 <b>{{ c.username || c.userId }}</b> {{ c.content }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="reopenVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 关闭 Bug 对话框 -->
    <el-dialog
      v-model="closeVisible"
      :title="`关闭Bug  ${bug?.title ?? ''}  #${bugId}`"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="closeForm" label-width="80px" label-position="left">
        <el-form-item label="备注">
          <el-input
            v-model="closeForm.note"
            type="textarea"
            :rows="5"
            placeholder="可以在编辑器直接贴图。"
            resize="none"
            style="width:100%"
          />
        </el-form-item>
      </el-form>

      <el-button type="primary" style="display:block;margin:16px auto 0;width:120px" :loading="closing" @click="submitClose">
        关闭
      </el-button>

      <!-- 历史记录 -->
      <div class="assign-history">
        <div class="assign-history-title"><span>历史记录</span></div>
        <div v-if="comments.length === 0" style="color:#9ca3af;font-size:13px;padding:8px 0">暂无记录</div>
        <div v-for="(c, i) in comments" :key="c.id" class="assign-history-item">
          <span class="ah-index">{{ i + 1 }}</span>
          <span class="ah-text">{{ formatDate(c.createdAt) }}，由 <b>{{ c.username || c.userId }}</b> {{ c.content }}</span>
        </div>
      </div>

      <template #footer>
        <el-button @click="closeVisible = false">取消</el-button>
      </template>
    </el-dialog>

    <!-- 转需求确认对话框 -->
    <el-dialog v-model="convertVisible" title="转为研发需求" width="380px">
      <div class="convert-tip">
        <el-icon color="#e6a23c" size="18"><Warning /></el-icon>
        <span>转需求后Bug将自动关闭，关闭原因为转为需求。</span>
      </div>
      <template #footer>
        <el-button @click="convertVisible = false">取消</el-button>
        <el-button type="primary" :loading="converting" @click="doConvert">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, EditPen, Delete, ChatLineRound, Warning, UserFilled } from '@element-plus/icons-vue'
import { bugApi } from '@/api/bug'
import { projectApi } from '@/api/project'

const route = useRoute()
const router = useRouter()
const projectId = Number(route.params.id)
const bugId = Number(route.params.bugId)

const bug = ref<any>(null)
const loading = ref(true)
const members = ref<any[]>([])
const projectName = ref('')
const comments = ref<any[]>([])

// 侧栏 tab
const sideTab = ref<'info' | 'life'>('info')

// 历史记录
const showCommentInput = ref(false)
const commentText = ref('')
const commentSubmitting = ref(false)

// 指派
const assignVisible = ref(false)
const assigning = ref(false)
const assignForm = ref({
  assigneeId: null as number | null,
  ccIds: [] as number[],
  note: ''
})

// 解决
const resolveVisible = ref(false)
const resolving = ref(false)
const resolveFormRef = ref()
const resolveForm = ref({
  resolveType: '',
  resolvedVersion: '',
  createVersion: false,
  resolvedDate: '',
  assigneeId: null as number | null,
  note: ''
})
const resolveRules = {
  resolveType: [{ required: true, message: '请选择解决方案', trigger: 'change' }]
}

// 重开（激活）
const reopenVisible = ref(false)
const reopening = ref(false)
const reopenFormRef = ref()
const reopenForm = ref({
  assigneeId: null as number | null,
  affectedVersion: '',
  note: ''
})
const reopenRules = {
  affectedVersion: [{ required: true, message: '请选择影响版本', trigger: 'change' }]
}

// 关闭
const closeVisible = ref(false)
const closing = ref(false)
const closeForm = ref({ note: '' })

// 转需求
const convertVisible = ref(false)
const converting = ref(false)

// ---- 数据加载 ----
async function loadBug() {
  loading.value = true
  try {
    const res = await bugApi.get(bugId)
    bug.value = (res as any).data ?? res
  } finally {
    loading.value = false
  }
}

async function loadComments() {
  try {
    const res = await bugApi.listComments(bugId)
    comments.value = (res as any).data ?? res ?? []
  } catch {
    comments.value = []
  }
}

async function loadMembers() {
  try {
    const res = await projectApi.getMembers(projectId)
    members.value = (res as any).data ?? res ?? []
  } catch {
    members.value = []
  }
}

async function loadProjectName() {
  try {
    const res = await projectApi.get(projectId)
    const d = (res as any).data ?? res
    projectName.value = d?.name ?? ''
  } catch { /* ignore */ }
}

onMounted(async () => {
  await Promise.all([loadBug(), loadMembers(), loadProjectName()])
  await loadComments()
})

// ---- 状态操作 ----
async function doStatus(status: string, resolveType?: string) {
  try {
    await bugApi.updateStatus(bugId, status, resolveType)
    await loadBug()
    ElMessage.success('状态已更新')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '操作失败')
  }
}

function openAssignDialog() {
  assignForm.value = {
    assigneeId: bug.value?.assigneeId ?? null,
    ccIds: [],
    note: ''
  }
  assignVisible.value = true
}

function openResolveDialog() {
  resolveForm.value = {
    resolveType: '',
    resolvedVersion: '',
    createVersion: false,
    resolvedDate: new Date().toISOString().slice(0, 16).replace('T', ' '),
    assigneeId: bug.value?.assigneeId ?? null,
    note: ''
  }
  resolveVisible.value = true
}

async function submitResolve() {
  await resolveFormRef.value?.validate()
  resolving.value = true
  try {
    // 如有指派变更，先更新指派
    if (resolveForm.value.assigneeId && resolveForm.value.assigneeId !== bug.value?.assigneeId) {
      await bugApi.assign(bugId, resolveForm.value.assigneeId)
    }
    // 更新状态为已解决
    await bugApi.updateStatus(bugId, 'RESOLVED', resolveForm.value.resolveType)
    // 若有备注，写入评论
    if (resolveForm.value.note.trim()) {
      await bugApi.addComment(bugId, `[解决备注] ${resolveForm.value.note.trim()}`)
    }
    resolveVisible.value = false
    await loadBug()
    await loadComments()
    ElMessage.success('Bug 已标记为解决')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '操作失败')
  } finally {
    resolving.value = false
  }
}

async function submitAssign() {
  if (!assignForm.value.assigneeId) {
    ElMessage.warning('请选择处理人')
    return
  }
  assigning.value = true
  try {
    await bugApi.assign(bugId, assignForm.value.assigneeId)
    if (assignForm.value.note.trim()) {
      await bugApi.addComment(bugId, `[指派备注] ${assignForm.value.note.trim()}`)
    }
    assignVisible.value = false
    await loadBug()
    await loadComments()
    ElMessage.success('指派成功')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '指派失败')
  } finally {
    assigning.value = false
  }
}

// ---- 激活（重开）----
function openReopenDialog() {
  reopenForm.value = { assigneeId: bug.value?.assigneeId ?? null, affectedVersion: '', note: '' }
  reopenVisible.value = true
}

async function submitReopen() {
  await reopenFormRef.value?.validate()
  reopening.value = true
  try {
    await bugApi.updateStatus(bugId, 'ACTIVE')
    if (reopenForm.value.assigneeId && reopenForm.value.assigneeId !== bug.value?.assigneeId) {
      await bugApi.assign(bugId, reopenForm.value.assigneeId)
    }
    const parts: string[] = []
    if (reopenForm.value.affectedVersion) parts.push(`影响版本：${reopenForm.value.affectedVersion}`)
    if (reopenForm.value.note.trim()) parts.push(reopenForm.value.note.trim())
    if (parts.length) await bugApi.addComment(bugId, `[激活] ${parts.join('；')}`)
    reopenVisible.value = false
    await loadBug()
    await loadComments()
    ElMessage.success('Bug 已激活')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '激活失败')
  } finally {
    reopening.value = false
  }
}

// ---- 关闭 ----
function openCloseDialog() {
  closeForm.value = { note: '' }
  closeVisible.value = true
}

async function submitClose() {
  closing.value = true
  try {
    await bugApi.updateStatus(bugId, 'CLOSED')
    if (closeForm.value.note.trim()) {
      await bugApi.addComment(bugId, `[关闭] ${closeForm.value.note.trim()}`)
    }
    closeVisible.value = false
    await loadBug()
    await loadComments()
    ElMessage.success('Bug 已关闭')
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '关闭失败')
  } finally {
    closing.value = false
  }
}

// ---- 转需求 ----
function confirmConvert() {
  convertVisible.value = true
}

async function doConvert() {
  converting.value = true
  try {
    const res = await bugApi.convertToRequirement(bugId)
    const newReqId = (res as any).data ?? res
    convertVisible.value = false
    ElMessage.success('已转为研发需求，Bug 已关闭')
    await loadBug()
    // 可选：跳转到新建的需求详情
    if (newReqId) {
      router.push(`/projects/${projectId}/requirements/${newReqId}`)
    }
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '转换失败')
  } finally {
    converting.value = false
  }
}

// ---- 删除 ----
async function handleDelete() {
  await ElMessageBox.confirm('确认删除该 Bug？此操作不可恢复。', '删除确认', {
    type: 'warning',
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    confirmButtonClass: 'el-button--danger'
  })
  try {
    await bugApi.delete(bugId)
    ElMessage.success('已删除')
    router.push(`/projects/${projectId}/bugs`)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? '删除失败')
  }
}

// ---- 备注 ----
async function submitComment() {
  if (!commentText.value.trim()) return
  commentSubmitting.value = true
  try {
    await bugApi.addComment(bugId, commentText.value.trim())
    commentText.value = ''
    showCommentInput.value = false
    await loadComments()
    ElMessage.success('备注已添加')
  } catch {
    ElMessage.error('添加备注失败')
  } finally {
    commentSubmitting.value = false
  }
}

function goBack() {
  router.push(`/projects/${projectId}/bugs`)
}

// ---- Bug的一生 ----
const STATUS_ORDER = ['ACTIVE', 'RESOLVED', 'CLOSED']
const STATUS_LABELS: Record<string, string> = {
  ACTIVE: '已激活', RESOLVED: '已解决', CLOSED: '已关闭'
}
const lifeSteps = computed(() => STATUS_ORDER.map(s => ({
  status: s,
  label: STATUS_LABELS[s],
  time: s === 'ACTIVE' ? bug.value?.createdAt
      : s === 'RESOLVED' ? bug.value?.resolvedAt
      : s === 'CLOSED' ? bug.value?.closedAt
      : null
})))

function currentStatusIdx() {
  return STATUS_ORDER.indexOf(bug.value?.status ?? 'ACTIVE')
}
function isActiveStatus(s: string) {
  return s === bug.value?.status
}
function isDoneStatus(s: string) {
  return STATUS_ORDER.indexOf(s) < currentStatusIdx()
}

// ---- 格式化 ----
function formatDate(val: string | null | undefined) {
  if (!val) return ''
  return String(val).slice(0, 16).replace('T', ' ')
}

const SEVERITY_MAP: Record<string, { label: string; type: string }> = {
  BLOCKER:  { label: '阻塞', type: 'danger' },
  CRITICAL: { label: '严重', type: 'danger' },
  NORMAL:   { label: '一般', type: 'warning' },
  MINOR:    { label: '轻微', type: 'info' }
}
function severityLabel(v: string) { return SEVERITY_MAP[v]?.label ?? v }
function severityType(v: string)  { return SEVERITY_MAP[v]?.type ?? '' }

const PRIORITY_MAP: Record<string, { label: string; type: string }> = {
  CRITICAL: { label: '紧急', type: 'danger' },
  HIGH:     { label: '高',   type: 'warning' },
  MEDIUM:   { label: '中',   type: '' },
  LOW:      { label: '低',   type: 'info' }
}
function priorityLabel(v: string) { return PRIORITY_MAP[v]?.label ?? v }
function priorityType(v: string)  { return PRIORITY_MAP[v]?.type ?? '' }

const STATUS_TYPE: Record<string, string> = {
  ACTIVE: 'warning', RESOLVED: 'success', CLOSED: 'info',
  NEW: 'warning', CONFIRMED: 'warning', ASSIGNED: 'warning', VERIFIED: 'info'
}
const STATUS_LABELS_COMPAT: Record<string, string> = {
  ...STATUS_LABELS,
  NEW: '已激活', CONFIRMED: '已激活', ASSIGNED: '已激活', VERIFIED: '已关闭'
}
function statusLabel(v: string) { return STATUS_LABELS_COMPAT[v] ?? v }
function statusType(v: string)  { return STATUS_TYPE[v] ?? '' }

const RESOLVE_TYPE_LABELS: Record<string, string> = {
  FIXED: '已修复', WONT_REPRODUCE: '无法重现', DUPLICATE: '重复Bug',
  BY_DESIGN: '设计如此', EXTERNAL: '外部原因', SUSPENDED: '挂起',
  CONVERTED_TO_REQ: '转为需求'
}
function resolveTypeLabel(v: string) { return RESOLVE_TYPE_LABELS[v] ?? v }
</script>

<style scoped>
.bug-detail-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f6fa;
}

/* ── 顶部面包屑 ── */
.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}
.back-btn { font-size: 13px; padding: 0; }
.divider  { color: #d1d5db; }
.breadcrumb { color: #6b7280; }
.bug-id-crumb { color: #4080ff; font-weight: 600; }
.bug-title-crumb { color: #1d2129; font-weight: 500; max-width: 400px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* ── 主体 ── */
.loading-wrap { padding: 40px 24px; }
.detail-body {
  display: flex;
  flex: 1;
  overflow: hidden;
  gap: 0;
}

/* ── 左侧主内容 ── */
.detail-main {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.main-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d2129;
  line-height: 1.4;
}
.section {
  background: #fff;
  border-radius: 8px;
  padding: 16px 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,.06);
}
.section-row {
  display: flex;
  gap: 16px;
}
.section-label {
  font-size: 13px;
  font-weight: 600;
  color: #6b7280;
  margin-bottom: 10px;
}
.section-content {
  font-size: 14px;
  color: #374151;
  line-height: 1.7;
  min-height: 32px;
}
.pre-wrap { white-space: pre-wrap; }

/* ── 历史记录 ── */
.history-section {}
.section-label-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.comment-input-wrap { margin-bottom: 16px; }
.comment-actions { display: flex; justify-content: flex-end; gap: 8px; margin-top: 8px; }
.empty-comments { color: #9ca3af; font-size: 13px; text-align: center; padding: 16px 0; }
.comment-list { display: flex; flex-direction: column; gap: 12px; }
.comment-item {
  padding: 12px 16px;
  background: #f9fafb;
  border-radius: 6px;
  border-left: 3px solid #4080ff;
}
.comment-meta { display: flex; gap: 12px; margin-bottom: 6px; }
.comment-author { font-size: 13px; font-weight: 600; color: #374151; }
.comment-time   { font-size: 12px; color: #9ca3af; }
.comment-body   { font-size: 13px; color: #4b5563; line-height: 1.6; white-space: pre-wrap; }

/* ── 右侧信息栏 ── */
.detail-sidebar {
  width: 260px;
  flex-shrink: 0;
  background: #fff;
  border-left: 1px solid #e5e7eb;
  overflow-y: auto;
  padding: 0 0 80px;
}
.side-tabs { padding: 0 16px; }
.info-list { padding: 0 16px 16px; }
.info-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid #f3f4f6;
  font-size: 13px;
}
.info-key { color: #9ca3af; width: 72px; flex-shrink: 0; padding-top: 2px; }
.info-val { color: #374151; flex: 1; word-break: break-all; }
.assignee-val { display: flex; align-items: center; gap: 2px; }

/* 指派对话框历史记录 */
.assign-history {
  margin-top: 24px;
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}
.assign-history-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
  margin-bottom: 12px;
}
.assign-history-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: #4b5563;
  padding: 6px 0;
  border-bottom: 1px solid #f9fafb;
}
.ah-index {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #6b7280;
  font-size: 11px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
}
.ah-text { flex: 1; line-height: 1.5; }

.sidebar-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 12px 16px 6px;
}

/* Bug的一生 */
.life-timeline { padding: 12px 16px; display: flex; flex-direction: column; gap: 0; }
.life-step {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 0;
  position: relative;
}
.life-step:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 5px;
  top: 20px;
  bottom: -8px;
  width: 2px;
  background: #e5e7eb;
}
.life-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #d1d5db;
  flex-shrink: 0;
  margin-top: 3px;
  z-index: 1;
}
.life-step.done .life-dot  { background: #34d399; }
.life-step.active .life-dot { background: #4080ff; box-shadow: 0 0 0 3px #dbeafe; }
.life-step.done::after { background: #34d399; }
.life-content {}
.life-name { font-size: 13px; color: #374151; }
.life-step.active .life-name { color: #4080ff; font-weight: 600; }
.life-step.done .life-name   { color: #6b7280; }
.life-time { font-size: 11px; color: #9ca3af; margin-top: 2px; }

/* ── 操作工具栏 ── */
.action-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 20px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
  gap: 12px;
}
.toolbar-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* 转需求提示 */
.convert-tip {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  color: #374151;
  line-height: 1.6;
}
</style>
