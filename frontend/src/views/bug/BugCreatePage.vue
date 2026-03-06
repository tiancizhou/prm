<template>
  <div class="bug-create-page">
    <!-- 顶部标题栏 -->
    <div class="create-header">
      <div class="header-left">
        <el-button link :icon="ArrowLeft" @click="goBack" class="back-btn">返回</el-button>
        <span class="divider">/</span>
        <span class="breadcrumb-title">提 Bug</span>
      </div>
    </div>

    <!-- 表单主体 -->
    <div class="create-body">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="create-form"
      >

        <!-- 第一行：所属模块 + 当前指派 -->
        <div class="form-row">
          <el-form-item label="所属模块" class="form-col">
            <el-select v-model="form.module" placeholder="请选择模块" clearable class="form-ctrl" filterable>
              <el-option v-for="m in flatModules" :key="m.id" :label="m.name" :value="m.name">
                <span :style="{ paddingLeft: m.depth * 16 + 'px', display: 'flex', alignItems: 'center', gap: '4px' }">
                  <span v-if="m.depth > 0" style="color: #9ca3af;">└</span>
                  <span>{{ m.name }}</span>
                </span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="当前指派" class="form-col">
            <el-select v-model="form.assigneeId" placeholder="选择处理人" clearable class="form-ctrl">
              <el-option
                v-for="m in members"
                :key="m.userId"
                :label="m.nickname || m.username"
                :value="m.userId"
              />
            </el-select>
          </el-form-item>
        </div>

        <!-- 第二行：截止日期 -->
        <div class="form-row">
          <el-form-item label="截止日期" class="form-col">
            <el-date-picker
              v-model="form.dueDate"
              type="date"
              value-format="YYYY-MM-DD"
              placeholder="选择截止日期"
              class="form-ctrl"
            />
          </el-form-item>
          <div class="form-col" />
        </div>

        <!-- Bug标题（必填）+ Bug类型 + 严重程度 + 优先级 -->
        <div class="form-row form-row--title">
          <el-form-item label="Bug 标题" prop="title" required class="form-col form-col--grow">
            <el-input
              v-model="form.title"
              placeholder="请输入 Bug 标题"
              maxlength="200"
              show-word-limit
              class="form-ctrl"
            />
          </el-form-item>
          <el-form-item label="Bug 类型" class="form-col form-col--shrink">
            <el-select v-model="form.bugType" class="form-ctrl">
              <el-option label="代码错误" value="CODE_ERROR" />
              <el-option label="功能问题" value="FUNCTION" />
              <el-option label="界面优化" value="UI" />
              <el-option label="性能问题" value="PERFORMANCE" />
              <el-option label="安全问题" value="SECURITY" />
              <el-option label="其他" value="OTHER" />
            </el-select>
          </el-form-item>
          <el-form-item label="严重程度" class="form-col form-col--shrink">
            <el-select v-model="form.severity" class="form-ctrl">
              <el-option label="阻塞" value="BLOCKER" />
              <el-option label="严重" value="CRITICAL" />
              <el-option label="一般" value="NORMAL" />
              <el-option label="轻微" value="MINOR" />
            </el-select>
          </el-form-item>
          <el-form-item label="优先级" class="form-col form-col--shrink">
            <el-select v-model="form.priority" class="form-ctrl">
              <el-option label="高" value="HIGH" />
              <el-option label="中" value="MEDIUM" />
              <el-option label="低" value="LOW" />
            </el-select>
          </el-form-item>
        </div>

        <!-- 重现步骤 -->
        <el-form-item label="重现步骤">
          <el-input
            v-model="form.steps"
            type="textarea"
            :rows="6"
            placeholder="[步骤]&#10;&#10;[结果]&#10;&#10;[期望]"
            class="form-ctrl"
          />
        </el-form-item>

        <!-- 附件 -->
        <el-form-item label="附件">
          <el-upload
            v-model:file-list="uploadFileList"
            :auto-upload="false"
            :limit="10"
            multiple
            drag
            class="form-ctrl upload-area"
          >
            <div class="upload-inner">
              <span class="upload-link">选择文件</span>
              <span class="upload-tip">可点击添加或拖拽上传，不超过 100MB</span>
            </div>
          </el-upload>
        </el-form-item>

        <!-- 期望结果 / 实际结果 -->
        <div class="form-row">
          <el-form-item label="期望结果" class="form-col">
            <el-input
              v-model="form.expectedResult"
              type="textarea"
              :rows="3"
              placeholder="请输入期望结果"
              class="form-ctrl"
            />
          </el-form-item>
          <el-form-item label="实际结果" class="form-col">
            <el-input
              v-model="form.actualResult"
              type="textarea"
              :rows="3"
              placeholder="请输入实际结果"
              class="form-ctrl"
            />
          </el-form-item>
        </div>

        <!-- 环境信息 -->
        <el-form-item label="环境信息">
          <el-input
            v-model="form.environment"
            placeholder="如：Chrome 120 / iOS 17 / 测试环境"
            class="form-ctrl"
          />
        </el-form-item>

        <!-- 分割线 -->
        <el-divider />

        <!-- 相关需求 + 相关任务 -->
        <div class="form-row">
          <el-form-item label="相关需求" class="form-col">
            <el-select
              v-model="form.relatedRequirementId"
              placeholder="选择相关需求"
              clearable
              filterable
              class="form-ctrl"
            >
              <el-option
                v-for="r in requirements"
                :key="r.id"
                :label="`#${r.id} ${r.title}（优先级:${priorityNum(r.priority)},预计工时:${r.estimatedHours ?? 0}）`"
                :value="r.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="相关任务" class="form-col">
            <el-select
              v-model="form.relatedRequirementId2"
              placeholder="暂不支持（预留）"
              disabled
              class="form-ctrl"
            />
          </el-form-item>
        </div>

        <!-- 浏览器 + 操作系统 -->
        <div class="form-row">
          <el-form-item label="浏览器" class="form-col">
            <el-select v-model="form.browser" placeholder="请选择" clearable class="form-ctrl">
              <el-option label="Chrome" value="Chrome" />
              <el-option label="Firefox" value="Firefox" />
              <el-option label="Safari" value="Safari" />
              <el-option label="Edge" value="Edge" />
              <el-option label="其他" value="Other" />
            </el-select>
          </el-form-item>
          <el-form-item label="操作系统" class="form-col">
            <el-select v-model="form.os" placeholder="请选择" clearable class="form-ctrl">
              <el-option label="Windows" value="Windows" />
              <el-option label="macOS" value="macOS" />
              <el-option label="Linux" value="Linux" />
              <el-option label="iOS" value="iOS" />
              <el-option label="Android" value="Android" />
              <el-option label="其他" value="Other" />
            </el-select>
          </el-form-item>
        </div>

        <!-- 抄送给 + 关键词 -->
        <div class="form-row">
          <el-form-item label="抄送给" class="form-col">
            <div class="cc-wrap">
              <el-select
                v-model="form.ccUserIds"
                placeholder="选择抄送人"
                clearable
                multiple
                collapse-tags
                class="form-ctrl"
              >
                <el-option
                  v-for="m in members"
                  :key="m.userId"
                  :label="m.nickname || m.username"
                  :value="m.userId"
                />
              </el-select>
            </div>
          </el-form-item>
          <el-form-item label="关键词" class="form-col">
            <el-input
              v-model="form.keywords"
              placeholder="输入关键词，用逗号分隔"
              class="form-ctrl"
            />
          </el-form-item>
        </div>

      </el-form>
    </div>

    <!-- 底部操作栏 -->
    <div class="create-footer">
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      <el-button @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { bugApi } from '@/api/bug'
import { projectApi } from '@/api/project'
import { moduleApi, type ModuleDTO } from '@/api/module'
import { requirementApi } from '@/api/requirement'

const route = useRoute()
const router = useRouter()
const projectId = Number(route.params.id)

// ---- 表单 ----
const formRef = ref<FormInstance>()
const form = reactive({
  title: '',
  module: '',
  bugType: 'CODE_ERROR',
  severity: 'NORMAL',
  priority: 'MEDIUM',
  assigneeId: null as number | null,
  dueDate: '',
  steps: '',
  expectedResult: '',
  actualResult: '',
  environment: '',
  // 扩展字段
  relatedRequirementId: null as number | null,
  relatedRequirementId2: null as number | null,
  browser: '',
  os: '',
  ccUserIds: [] as number[],
  keywords: ''
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入 Bug 标题', trigger: 'blur' }]
}

const uploadFileList = ref<any[]>([])
const submitting = ref(false)

// ---- 数据 ----
const members = ref<any[]>([])
const modules = ref<ModuleDTO[]>([])
const requirements = ref<any[]>([])

function flattenModules(nodes: ModuleDTO[], depth = 0): Array<{ id: number; name: string; depth: number }> {
  const result: Array<{ id: number; name: string; depth: number }> = []
  for (const n of nodes) {
    result.push({ id: n.id, name: n.name, depth })
    if (n.children?.length) result.push(...flattenModules(n.children, depth + 1))
  }
  return result
}
const flatModules = computed(() => flattenModules(modules.value))

const PRIORITY_NUM: Record<string, number> = { LOW: 1, MEDIUM: 2, HIGH: 3, CRITICAL: 4 }
function priorityNum(p: string) { return PRIORITY_NUM[p] ?? 0 }

async function loadData() {
  try {
    const [membersRes, modulesRes, reqRes] = await Promise.all([
      projectApi.getMembers(projectId),
      moduleApi.listTree(projectId),
      requirementApi.list({ projectId, page: 1, size: 200 })
    ])
    members.value = (membersRes as any).data || []
    modules.value = (modulesRes as any)?.data ?? modulesRes ?? []
    requirements.value = (reqRes as any).data?.records ?? []
  } catch { /* silent */ }
}

// ---- 提交 ----
async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 拼接环境信息
    const envParts: string[] = []
    if (form.browser) envParts.push(`浏览器:${form.browser}`)
    if (form.os) envParts.push(`操作系统:${form.os}`)
    if (form.environment) envParts.push(form.environment)

    await bugApi.create({
      projectId,
      title: form.title.trim(),
      module: form.module || undefined,
      severity: form.severity,
      priority: form.priority,
      assigneeId: form.assigneeId ?? undefined,
      steps: form.steps || undefined,
      expectedResult: form.expectedResult || undefined,
      actualResult: form.actualResult || undefined,
      environment: envParts.join(' / ') || undefined
    })
    ElMessage.success('Bug 提交成功')
    router.push(`/projects/${projectId}/bugs`)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? e?.message ?? '提交失败')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push(`/projects/${projectId}/bugs`)
}

onMounted(loadData)
</script>

<style scoped>
.bug-create-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
  overflow: hidden;
}

/* ---- 顶部 ---- */
.create-header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 32px;
  height: 52px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.back-btn {
  font-size: 13px;
  color: #4080ff;
  padding: 0;
}

.divider {
  color: #c0c4cc;
  font-size: 14px;
}

.breadcrumb-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
}

/* ---- 表单主体 ---- */
.create-body {
  flex: 1;
  overflow-y: auto;
  padding: 28px 32px;
}

.create-form {
  max-width: 960px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 28px 32px;
}

.create-form :deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  line-height: 1.4;
  margin-bottom: 6px;
}

.create-form :deep(.el-form-item) {
  margin-bottom: 20px;
}

.form-ctrl {
  width: 100%;
}

/* 多列行 */
.form-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.form-row--title {
  align-items: flex-start;
}

.form-col {
  flex: 1;
  min-width: 180px;
}

.form-col--grow {
  flex: 2;
  min-width: 220px;
}

.form-col--shrink {
  flex: 0 0 140px;
  min-width: 120px;
}

/* 附件上传 */
.upload-area {
  width: 100%;
}

.upload-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 12px 0;
}

.upload-link {
  color: #4080ff;
  font-size: 14px;
  font-weight: 500;
}

.upload-tip {
  font-size: 12px;
  color: #9ca3af;
}

.cc-wrap {
  display: flex;
  gap: 8px;
  width: 100%;
}

/* ---- 底部 ---- */
.create-footer {
  background: #fff;
  border-top: 1px solid #e5e7eb;
  padding: 16px 32px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
</style>
