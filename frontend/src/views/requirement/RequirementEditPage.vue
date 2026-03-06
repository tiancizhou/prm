<template>
  <div class="req-create-page">
    <div class="create-header">
      <div class="header-left">
        <el-button link :icon="ArrowLeft" @click="goBack" class="back-btn">返回</el-button>
        <span class="divider">/</span>
        <span class="breadcrumb-title">编辑需求 <span v-if="reqId" class="req-id">#{{ reqId }}</span></span>
      </div>
    </div>

    <div class="create-body">
      <div v-if="loading" class="form-loading">
        <el-skeleton :rows="6" animated />
      </div>

      <el-form
        v-else
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="create-form"
      >
        <!-- 所属模块 -->
        <el-form-item label="所属模块">
          <el-select v-model="form.moduleId" placeholder="请选择模块" clearable class="form-ctrl" filterable>
            <el-option v-for="m in flatModules" :key="m.id" :label="m.name" :value="m.id">
              <span :style="{ paddingLeft: m.depth * 16 + 'px', display: 'flex', alignItems: 'center', gap: '4px' }">
                <span v-if="m.depth > 0" style="color: #9ca3af;">└</span>
                <span>{{ m.name }}</span>
              </span>
            </el-option>
          </el-select>
        </el-form-item>

        <!-- 父需求 -->
        <el-form-item label="父需求">
          <el-select
            v-model="form.parentId"
            placeholder="选择父需求（可选）"
            clearable
            filterable
            class="form-ctrl"
          >
            <el-option
              v-for="r in parentRequirements"
              :key="r.id"
              :label="`#${r.id} ${r.title}`"
              :value="r.id"
            />
          </el-select>
        </el-form-item>

        <!-- 指派给 -->
        <el-form-item label="指派给">
          <el-select v-model="form.assigneeId" placeholder="选择负责人" clearable class="form-ctrl">
            <el-option
              v-for="m in members"
              :key="m.userId"
              :label="m.nickname || m.username"
              :value="m.userId"
            />
          </el-select>
        </el-form-item>

        <!-- 需求名称 + 优先级 + 预计工时 -->
        <div class="form-row">
          <el-form-item label="研发需求名称" prop="title" required class="form-col form-col--grow">
            <el-input
              v-model="form.title"
              placeholder="请输入需求标题"
              maxlength="200"
              show-word-limit
              class="form-ctrl"
            />
          </el-form-item>
          <el-form-item label="优先级" class="form-col form-col--shrink">
            <el-select v-model="form.priority" class="form-ctrl">
              <el-option
                v-for="opt in priorityOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="预计（单位：工时）" class="form-col form-col--shrink">
            <el-input-number
              v-model="form.estimatedHours"
              :min="0"
              :precision="1"
              :controls="false"
              class="form-ctrl"
            />
          </el-form-item>
        </div>

        <!-- 描述 -->
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="5"
            placeholder="建议参考的模板：作为一名<某种类型的用户>，我希望<达成某些目的>，这样可以<开发的价值>。"
            class="form-ctrl"
          />
        </el-form-item>

        <!-- 验收标准 -->
        <el-form-item label="验收标准">
          <el-input
            v-model="form.acceptanceCriteria"
            type="textarea"
            :rows="4"
            placeholder="请填写验收标准"
            class="form-ctrl"
          />
        </el-form-item>

        <!-- 附件 -->
        <el-form-item label="附件">
          <!-- 已有附件 -->
          <div v-if="existingAttachments.length" class="existing-attachments">
            <div
              v-for="att in existingAttachments"
              :key="att.id"
              class="att-item"
            >
              <el-icon><Document /></el-icon>
              <span class="att-name" :title="att.filename">{{ att.filename }}</span>
              <span class="att-size">{{ formatFileSize(att.fileSize) }}</span>
              <el-button type="danger" link size="small" @click="deleteAttachment(att)">删除</el-button>
            </div>
          </div>
          <!-- 上传新附件 -->
          <el-upload
            v-model:file-list="uploadFileList"
            :auto-upload="false"
            :limit="10"
            :on-exceed="() => ElMessage.warning('最多上传10个文件')"
            multiple
            drag
            class="form-ctrl upload-area"
          >
            <el-icon class="el-icon--upload"><UploadFilled /></el-icon>
            <div class="el-upload__text">拖拽文件到此处，或<em>点击上传</em></div>
            <template #tip>
              <div class="el-upload__tip">支持 pdf/doc/xls/ppt/txt/图片/zip，单文件不超过 50MB</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
    </div>

    <div class="create-footer">
      <el-button type="primary" :loading="submitting" @click="submit">保存</el-button>
      <el-button @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { ArrowLeft, UploadFilled, Document } from '@element-plus/icons-vue'
import { requirementApi } from '@/api/requirement'
import { projectApi } from '@/api/project'
import { moduleApi, type ModuleDTO } from '@/api/module'

const route = useRoute()
const router = useRouter()

const projectId = Number(route.params.id)
const reqId = Number(route.params.reqId)

const loading = ref(true)
const submitting = ref(false)

const formRef = ref<FormInstance>()
const form = reactive({
  title: '',
  description: '',
  acceptanceCriteria: '',
  priority: 'MEDIUM',
  estimatedHours: 0 as number,
  assigneeId: null as number | null,
  moduleId: null as number | null,
  parentId: null as number | null
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入需求名称', trigger: 'blur' }]
}

const priorityOptions = [
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' },
  { value: 'CRITICAL', label: '紧急' }
]

const uploadFileList = ref<any[]>([])
const existingAttachments = ref<any[]>([])
const members = ref<any[]>([])
const modules = ref<ModuleDTO[]>([])
const parentRequirements = ref<any[]>([])

function flattenModules(nodes: ModuleDTO[], depth = 0): Array<{ id: number; name: string; depth: number }> {
  const result: Array<{ id: number; name: string; depth: number }> = []
  for (const n of nodes) {
    result.push({ id: n.id, name: n.name, depth })
    if (n.children?.length) result.push(...flattenModules(n.children, depth + 1))
  }
  return result
}
const flatModules = computed(() => flattenModules(modules.value))

async function loadData() {
  try {
    const [reqRes, membersRes, modulesRes, reqListRes, attRes] = await Promise.all([
      requirementApi.get(reqId),
      projectApi.getMembers(projectId),
      moduleApi.listTree(projectId),
      requirementApi.list({ projectId, page: 1, size: 200 }),
      requirementApi.listAttachments(reqId)
    ])
    const req = (reqRes as any).data
    form.title = req.title ?? ''
    form.description = req.description ?? ''
    form.acceptanceCriteria = req.acceptanceCriteria ?? ''
    form.priority = req.priority ?? 'MEDIUM'
    form.estimatedHours = req.estimatedHours ?? 0
    form.assigneeId = req.assigneeId ?? null
    form.moduleId = req.moduleId ?? null
    form.parentId = req.parentId ?? null

    members.value = (membersRes as any).data || []
    modules.value = (modulesRes as any)?.data ?? modulesRes ?? []

    // 父需求列表（排除自己及子孙）
    const rd = (reqListRes as any).data
    parentRequirements.value = (rd?.records ?? []).filter(
      (r: any) => r.id !== reqId && r.parentId == null
    )

    existingAttachments.value = (attRes as any).data ?? []
  } catch {
    ElMessage.error('加载需求失败')
  } finally {
    loading.value = false
  }
}

async function deleteAttachment(att: any) {
  try {
    await ElMessageBox.confirm(`确认删除附件「${att.filename}」？`, '提示', { type: 'warning' })
    await requirementApi.deleteAttachment(reqId, att.id)
    existingAttachments.value = existingAttachments.value.filter(a => a.id !== att.id)
    ElMessage.success('附件已删除')
  } catch {
    // user cancelled or error – silent
  }
}

function formatFileSize(size: number): string {
  if (!size) return ''
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await requirementApi.update(reqId, {
      title: form.title.trim(),
      description: form.description,
      acceptanceCriteria: form.acceptanceCriteria,
      priority: form.priority,
      estimatedHours: form.estimatedHours,
      assigneeId: form.assigneeId ?? null,
      moduleId: form.moduleId ?? null,
      parentId: form.parentId ?? null
    })

    // 上传新附件
    if (uploadFileList.value.length) {
      for (const item of uploadFileList.value) {
        if (item.raw) await requirementApi.uploadAttachment(reqId, item.raw).catch(() => {})
      }
    }

    ElMessage.success('保存成功')
    router.push(`/projects/${projectId}/requirements/${reqId}`)
  } catch (e: any) {
    ElMessage.error(e?.response?.data?.msg ?? e?.message ?? '保存失败')
  } finally {
    submitting.value = false
  }
}

function goBack() {
  router.push(`/projects/${projectId}/requirements/${reqId}`)
}

onMounted(loadData)
</script>

<style scoped>
.req-create-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
  overflow: hidden;
}

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

.req-id {
  font-weight: 400;
  color: #6b7280;
}

.create-body {
  flex: 1;
  overflow-y: auto;
  padding: 28px 32px;
}

.form-loading {
  max-width: 900px;
  background: #fff;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
  padding: 28px 32px;
}

.create-form {
  max-width: 900px;
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

.form-row {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}

.form-col {
  flex: 1;
  min-width: 200px;
}

.form-col--grow {
  flex: 2;
}

.form-col--shrink {
  flex: 0 0 160px;
  min-width: 140px;
}

/* 已有附件 */
.existing-attachments {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 10px;
  width: 100%;
}

.att-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  background: #f9fafb;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  font-size: 13px;
}

.att-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.att-size {
  font-size: 12px;
  color: #9ca3af;
  flex-shrink: 0;
}

.upload-area {
  width: 100%;
}

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
