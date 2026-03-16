<template>
  <div class="req-detail-page">
    <!-- 顶部导航栏 -->
    <div class="detail-header">
      <div class="header-left">
        <el-button link :icon="ArrowLeft" @click="goBack" class="back-btn">返回</el-button>
        <span class="header-id">#{{ req?.id }}</span>
        <span class="header-title">{{ req?.title }}</span>
      </div>
      <div class="header-right">
        <el-button v-if="canManage" type="primary" :icon="Plus" @click="goCreate">提研发需求</el-button>
      </div>
    </div>

    <!-- 主体 -->
    <div v-if="loading" class="detail-loading">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="req" class="detail-body">
      <!-- 左侧内容区 -->
      <div class="detail-main">
        <!-- 需求描述 -->
        <div class="content-card">
          <div class="card-title">需求描述</div>
          <div class="card-content text-content">
            {{ req.description || '暂无描述' }}
          </div>
        </div>

        <!-- 验收标准 -->
        <div class="content-card">
          <div class="card-title">验收标准</div>
          <div class="card-content text-content">
            {{ req.acceptanceCriteria || '暂无描述' }}
          </div>
        </div>

        <!-- 附件 -->
        <div class="content-card">
          <div class="card-title">附件</div>
          <div class="card-content">
            <div v-if="attachments.length" class="attachment-list">
              <div v-for="att in attachments" :key="att.id" class="attachment-item">
                <el-icon class="att-icon"><Document /></el-icon>
                <span class="att-name" :title="att.filename">{{ att.filename }}</span>
                <span class="att-size">{{ formatFileSize(att.fileSize) }}</span>
                <el-button type="primary" link size="small" @click="downloadAtt(att)">下载</el-button>
              </div>
            </div>
            <div v-else class="empty-tip">暂无附件</div>
          </div>
        </div>

        <!-- 子需求 -->
        <div class="content-card">
          <div class="card-title-row">
            <span class="card-title">子需求</span>
            <el-button v-if="canManage" size="small" :icon="Plus" @click="goCreateSub">新建子需求</el-button>
          </div>
          <div class="card-content">
            <div v-if="children.length" class="sub-req-list">
              <div
                v-for="child in children"
                :key="child.id"
                class="sub-req-item"
                @click="goDetail(child.id)"
              >
                <el-tag size="small" round :type="statusTagType(child.status)" class="sub-status">
                  {{ child.statusLabel }}
                </el-tag>
                <span class="sub-title">#{{ child.id }} {{ child.title }}</span>
                <span class="sub-assignee">{{ child.assigneeName || '-' }}</span>
              </div>
            </div>
            <div v-else class="empty-tip">暂无子需求</div>
          </div>
        </div>

        <!-- 历史记录 -->
        <div class="content-card">
          <div class="card-title-row">
            <span class="card-title">历史记录</span>
            <el-button size="small" :icon="ChatLineRound" @click="showCommentInput = !showCommentInput">
              添加备注
            </el-button>
          </div>
          <!-- 添加备注输入框 -->
          <div v-if="showCommentInput" class="comment-input-wrap">
            <el-input
              v-model="commentText"
              type="textarea"
              :rows="3"
              placeholder="请输入备注内容..."
              class="comment-input"
            />
            <div class="comment-input-actions">
              <el-button type="primary" size="small" :loading="commentSubmitting" @click="submitComment">提交</el-button>
              <el-button size="small" @click="showCommentInput = false; commentText = ''">取消</el-button>
            </div>
          </div>
          <div class="card-content">
            <div v-if="logs.length" class="log-list">
              <div v-for="(log, index) in logs" :key="log.id" class="log-item">
                <div class="log-index">{{ index + 1 }}</div>
                <div class="log-body">
                  <span class="log-time">{{ formatDateTime(log.createdAt) }}</span>
                  <span class="log-sep">，由</span>
                  <span class="log-user">{{ log.username }}</span>
                  <span v-if="log.logType === 'AUTO'" class="log-content">{{ log.content }}。</span>
                  <span v-else class="log-comment">备注：{{ log.content }}</span>
                </div>
              </div>
            </div>
            <div v-else class="empty-tip">暂无记录</div>
          </div>
        </div>
      </div>

      <!-- 右侧信息面板 -->
      <div class="detail-sidebar">
        <!-- 基本信息卡 -->
        <div class="sidebar-card">
          <div class="sidebar-card-header">
            <div class="sidebar-tabs">
              <button class="stab" :class="{ active: sideTab === 'info' }" @click="sideTab = 'info'">基本信息</button>
            </div>
            <div class="sidebar-actions">
              <el-button v-if="canEdit" size="small" :icon="EditPen" link @click="goEdit">编辑</el-button>
              <el-dropdown v-if="canEdit && nextStatusOpts.length" @command="changeStatus" trigger="click">
                <el-button size="small" link type="primary">
                  变更状态 <el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-for="opt in nextStatusOpts" :key="opt.command" :command="opt.command">
                      {{ opt.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <div class="info-list">
            <div class="info-row">
              <span class="info-label">所属模块</span>
              <span class="info-value">{{ req.moduleName || '/' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">当前状态</span>
              <span class="info-value">
                <el-tag size="small" round :type="statusTagType(req.status)">{{ req.statusLabel }}</el-tag>
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">优先级</span>
              <span class="info-value">
                <el-tag size="small" round :type="priorityTagType(req.priority)" effect="plain">
                  {{ priorityLabel(req.priority) }}
                </el-tag>
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">负责人</span>
              <span class="info-value">{{ req.assigneeName || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="info-label">预计工时</span>
              <span class="info-value">{{ req.estimatedHours != null ? req.estimatedHours + 'h' : '-' }}</span>
            </div>
            <div class="info-row" v-if="req.startDate">
              <span class="info-label">开始日期</span>
              <span class="info-value">{{ formatDate(req.startDate) }}</span>
            </div>
            <div class="info-row" v-if="req.dueDate">
              <span class="info-label">截止日期</span>
              <span class="info-value">{{ formatDate(req.dueDate) }}</span>
            </div>
            <div class="info-row" v-if="req.parentId">
              <span class="info-label">父需求</span>
              <span class="info-value link" @click="goDetail(req.parentId!)">
                #{{ req.parentId }} {{ req.parentTitle }}
              </span>
            </div>
            <div class="info-row">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDateTime(req.createdAt) }}</span>
            </div>
            <div class="info-row" v-if="req.updatedAt">
              <span class="info-label">更新时间</span>
              <span class="info-value">{{ formatDateTime(req.updatedAt) }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="detail-loading">
      <el-empty description="需求不存在或已被删除" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Plus, EditPen, ArrowDown, Document, ChatLineRound } from '@element-plus/icons-vue'
import { requirementApi } from '@/api/requirement'
import { ACTION_PERMISSION_MAP } from '@/utils/permission'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const projectStore = useProjectStore()

const projectId = Number(route.params.id)
const reqId = Number(route.params.reqId)

const loading = ref(true)
const req = ref<any>(null)
const attachments = ref<any[]>([])
const children = ref<any[]>([])
const logs = ref<any[]>([])
const sideTab = ref('info')

// 备注
const showCommentInput = ref(false)
const commentText = ref('')
const commentSubmitting = ref(false)

// ---- 权限 ----
const currentUserId = computed(() => authStore.user?.userId)
const canManage = computed(() => {
  if (authStore.canAccess(ACTION_PERMISSION_MAP.requirementUpdate)) return true
  return projectStore.currentProject?.id === projectId && projectStore.currentProject?.canEdit === true
})
const canEdit = computed(() => {
  if (canManage.value) return true
  return req.value?.assigneeId && req.value.assigneeId === currentUserId.value
})

// ---- 状态流转 ----
const STATUS_TRANSITIONS: Record<string, { command: string; label: string }[]> = {
  DRAFT: [{ command: 'IN_PROGRESS', label: '开始进行' }],
  IN_PROGRESS: [{ command: 'DONE', label: '标记完成' }],
  DONE: [{ command: 'IN_PROGRESS', label: '重新进行' }]
}
const nextStatusOpts = computed(() => STATUS_TRANSITIONS[req.value?.status] ?? [])

async function changeStatus(command: string) {
  if (!req.value) return
  try {
    await requirementApi.updateStatus(req.value.id, command)
    ElMessage.success('状态已更新')
    await loadReq()
  } catch {
    ElMessage.error('状态更新失败')
  }
}

// ---- 数据加载 ----
async function loadReq() {
  try {
    const res = await requirementApi.get(reqId)
    req.value = (res as any).data
  } catch {
    req.value = null
  }
}

async function loadChildren() {
  try {
    const res = await requirementApi.list({ projectId, parentId: reqId, page: 1, size: 200 })
    children.value = (res as any).data?.records ?? []
  } catch {
    children.value = []
  }
}

async function loadLogs() {
  try {
    const res = await requirementApi.listLogs(reqId)
    logs.value = (res as any).data ?? []
  } catch {
    logs.value = []
  }
}

async function submitComment() {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入备注内容')
    return
  }
  commentSubmitting.value = true
  try {
    await requirementApi.addComment(reqId, commentText.value.trim())
    commentText.value = ''
    showCommentInput.value = false
    await loadLogs()
    ElMessage.success('备注已添加')
  } catch {
    ElMessage.error('添加备注失败')
  } finally {
    commentSubmitting.value = false
  }
}

async function loadAttachments() {
  try {
    const res = await requirementApi.listAttachments(reqId)
    attachments.value = (res as any).data ?? []
  } catch {
    attachments.value = []
  }
}

// ---- 导航 ----
function goBack() {
  router.push(`/projects/${projectId}/requirements`)
}

function goEdit() {
  router.push(`/projects/${projectId}/requirements/${reqId}/edit`)
}

function goCreate() {
  router.push(`/projects/${projectId}/requirements/create`)
}

function goCreateSub() {
  router.push(`/projects/${projectId}/requirements/create?parentId=${reqId}`)
}

function goDetail(id: number) {
  router.push(`/projects/${projectId}/requirements/${id}`)
}

// ---- 附件 ----
async function downloadAtt(att: any) {
  try {
    await requirementApi.downloadAttachment(reqId, att.id, att.filename)
    ElMessage.success('开始下载')
  } catch {
    ElMessage.error('下载失败')
  }
}

function formatFileSize(size: number): string {
  if (!size) return ''
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

// ---- 格式化 ----
function formatDate(d: any): string {
  if (!d) return '-'
  if (typeof d === 'string') return d.slice(0, 10)
  return String(d)
}

function formatDateTime(d: any): string {
  if (!d) return '-'
  if (typeof d === 'string') return d.slice(0, 16).replace('T', ' ')
  return String(d)
}

function statusTagType(status: string): 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    DRAFT: 'info',
    IN_PROGRESS: 'warning',
    DONE: 'success'
  }
  return map[status] ?? 'info'
}

function priorityTagType(p: string): 'success' | 'warning' | 'info' | 'danger' {
  const map: Record<string, 'success' | 'warning' | 'info' | 'danger'> = {
    LOW: 'info',
    MEDIUM: 'info',
    HIGH: 'warning',
    CRITICAL: 'danger'
  }
  return map[p] ?? 'info'
}

function priorityLabel(p: string): string {
  const map: Record<string, string> = { LOW: '低', MEDIUM: '中', HIGH: '高', CRITICAL: '紧急' }
  return map[p] ?? p
}

onMounted(async () => {
  await Promise.all([loadReq(), loadAttachments(), loadChildren(), loadLogs()])
  loading.value = false
})
</script>

<style scoped>
.req-detail-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
  overflow: hidden;
}

/* ---- 顶部 ---- */
.detail-header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 24px;
  height: 52px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;
  gap: 12px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.back-btn {
  font-size: 13px;
  color: #4080ff;
  padding: 0;
  flex-shrink: 0;
}

.header-id {
  font-size: 13px;
  color: #9ca3af;
  flex-shrink: 0;
}

.header-title {
  font-size: 15px;
  font-weight: 600;
  color: #1d2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.header-right {
  flex-shrink: 0;
}

/* ---- 主体 ---- */
.detail-loading {
  padding: 32px;
}

.detail-body {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  overflow: hidden;
}

/* 左侧内容 */
.detail-main {
  flex: 1;
  min-width: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.content-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 16px 20px;
}

.card-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
  margin-bottom: 12px;
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-title-row .card-title {
  margin-bottom: 0;
}

.card-content {
  color: #374151;
}

.text-content {
  font-size: 13px;
  line-height: 1.7;
  white-space: pre-wrap;
  color: #6b7280;
}

.empty-tip {
  font-size: 13px;
  color: #9ca3af;
  padding: 8px 0;
}

/* 附件 */
.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.att-icon {
  color: #6b7280;
  flex-shrink: 0;
}

.att-name {
  flex: 1;
  min-width: 0;
  font-size: 13px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.att-size {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

/* 子需求 */
.sub-req-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.sub-req-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.15s;
}

.sub-req-item:hover {
  background: #f3f4f6;
}

.sub-status {
  flex-shrink: 0;
}

.sub-title {
  flex: 1;
  font-size: 13px;
  color: #1d2129;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.sub-assignee {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

/* 历史记录 */
.log-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.log-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  font-size: 13px;
  color: #374151;
  line-height: 1.6;
}

.log-index {
  flex-shrink: 0;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #6b7280;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 2px;
}

.log-body {
  flex: 1;
}

.log-time {
  color: #6b7280;
}

.log-sep {
  color: #9ca3af;
  margin: 0 2px;
}

.log-user {
  font-weight: 600;
  color: #1d2129;
}

.log-content {
  color: #374151;
  margin-left: 2px;
}

.log-comment {
  color: #4080ff;
  margin-left: 2px;
  font-style: italic;
}

/* 备注输入 */
.comment-input-wrap {
  margin-bottom: 14px;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 10px;
  background: #f9fafb;
}

.comment-input {
  width: 100%;
}

.comment-input-actions {
  display: flex;
  gap: 8px;
  margin-top: 8px;
  justify-content: flex-end;
}

/* 右侧面板 */
.detail-sidebar {
  width: 280px;
  flex-shrink: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sidebar-card {
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  overflow: hidden;
}

.sidebar-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
}

.sidebar-tabs {
  display: flex;
  gap: 4px;
}

.stab {
  padding: 4px 10px;
  font-size: 13px;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  border-radius: 4px;
  transition: background 0.15s, color 0.15s;
}

.stab.active {
  background: #eff6ff;
  color: #4080ff;
  font-weight: 500;
}

.sidebar-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 信息列表 */
.info-list {
  padding: 8px 0;
}

.info-row {
  display: flex;
  align-items: center;
  padding: 7px 16px;
  gap: 8px;
  font-size: 13px;
  min-height: 36px;
}

.info-row:hover {
  background: #f9fafb;
}

.info-label {
  color: #9ca3af;
  flex-shrink: 0;
  width: 72px;
}

.info-value {
  color: #1d2129;
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.info-value.link {
  color: #4080ff;
  cursor: pointer;
  text-decoration: none;
}

.info-value.link:hover {
  text-decoration: underline;
}
</style>
