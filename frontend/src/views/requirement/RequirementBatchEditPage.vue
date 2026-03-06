<template>
  <div class="batch-edit-page">
    <!-- 顶部 -->
    <div class="batch-header">
      <div class="header-left">
        <el-button link :icon="ArrowLeft" @click="goBack" class="back-btn">返回</el-button>
        <span class="divider">/</span>
        <span class="header-title">批量编辑</span>
      </div>
    </div>

    <!-- 表格 -->
    <div class="batch-body">
      <div v-if="loading" class="loading-wrap">
        <el-skeleton :rows="4" animated />
      </div>

      <el-table v-else :data="rows" border class="batch-table" size="small">
        <el-table-column label="ID" width="60" prop="id" align="center" />

        <el-table-column label="所属模块" width="140">
          <template #default="{ row }">
            <el-select v-model="row.moduleId" placeholder="/" clearable size="small" style="width:100%">
              <el-option v-for="m in flatModules" :key="m.id" :label="m.name" :value="m.id" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="研发需求名称*" min-width="200">
          <template #default="{ row }">
            <el-input v-model="row.title" size="small" placeholder="需求标题" />
          </template>
        </el-table-column>

        <el-table-column label="预计工时" width="100">
          <template #default="{ row }">
            <el-input-number
              v-model="row.estimatedHours"
              :min="0"
              :precision="1"
              :controls="false"
              size="small"
              style="width:100%"
            />
          </template>
        </el-table-column>

        <el-table-column label="优先级" width="100">
          <template #default="{ row }">
            <el-select v-model="row.priority" size="small" style="width:100%">
              <el-option v-for="p in priorityOptions" :key="p.value" :label="p.label" :value="p.value" />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="负责人" width="130">
          <template #default="{ row }">
            <el-select v-model="row.assigneeId" placeholder="-" clearable size="small" style="width:100%">
              <el-option
                v-for="m in members"
                :key="m.userId"
                :label="m.nickname || m.username"
                :value="m.userId"
              />
            </el-select>
          </template>
        </el-table-column>

        <el-table-column label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" round :type="statusTagType(row.status)">{{ row.statusLabel }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 底部 -->
    <div class="batch-footer">
      <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      <el-button @click="goBack">返回</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { requirementApi } from '@/api/requirement'
import { projectApi } from '@/api/project'
import { moduleApi, type ModuleDTO } from '@/api/module'

const route = useRoute()
const router = useRouter()
const projectId = Number(route.params.id)
const ids = (route.query.ids as string)?.split(',').map(Number).filter(Boolean) ?? []

const loading = ref(true)
const saving = ref(false)
const rows = ref<any[]>([])
const members = ref<any[]>([])
const modules = ref<ModuleDTO[]>([])

function flattenModules(nodes: ModuleDTO[], prefix = ''): Array<{ id: number; name: string }> {
  const result: Array<{ id: number; name: string }> = []
  for (const n of nodes) {
    result.push({ id: n.id, name: prefix + n.name })
    if (n.children?.length) result.push(...flattenModules(n.children, prefix + n.name + ' / '))
  }
  return result
}
const flatModules = computed(() => flattenModules(modules.value))

const priorityOptions = [
  { value: 'LOW', label: '低' },
  { value: 'MEDIUM', label: '中' },
  { value: 'HIGH', label: '高' },
  { value: 'CRITICAL', label: '紧急' }
]

type TagType = '' | 'success' | 'warning' | 'info' | 'danger'
function statusTagType(status: string): TagType {
  const map: Record<string, TagType> = { DRAFT: 'info', IN_PROGRESS: '', DONE: 'success' }
  return map[status] ?? 'info'
}

async function loadData() {
  try {
    const [membersRes, modulesRes, ...reqResults] = await Promise.all([
      projectApi.getMembers(projectId),
      moduleApi.listTree(projectId),
      ...ids.map(id => requirementApi.get(id))
    ])
    members.value = (membersRes as any).data || []
    modules.value = (modulesRes as any)?.data ?? modulesRes ?? []
    rows.value = reqResults.map((r: any) => ({ ...(r as any).data }))
  } catch {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function save() {
  const invalid = rows.value.filter(r => !r.title?.trim())
  if (invalid.length) {
    ElMessage.warning('需求名称不能为空')
    return
  }
  saving.value = true
  try {
    await Promise.all(
      rows.value.map(row =>
        requirementApi.update(row.id, {
          title: row.title,
          priority: row.priority,
          estimatedHours: row.estimatedHours,
          assigneeId: row.assigneeId ?? null,
          moduleId: row.moduleId ?? null,
          description: row.description,
          acceptanceCriteria: row.acceptanceCriteria,
          startDate: row.startDate,
          dueDate: row.dueDate,
          parentId: row.parentId ?? null
        })
      )
    )
    ElMessage.success('保存成功')
    router.push(`/projects/${projectId}/requirements`)
  } catch {
    ElMessage.error('保存失败，请重试')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push(`/projects/${projectId}/requirements`)
}

onMounted(loadData)
</script>

<style scoped>
.batch-edit-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #f5f7fa;
  overflow: hidden;
}

.batch-header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  padding: 0 24px;
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

.header-title {
  font-size: 14px;
  font-weight: 600;
  color: #1d2129;
}

.batch-body {
  flex: 1;
  overflow: auto;
  padding: 24px;
}

.loading-wrap {
  padding: 16px;
}

.batch-table {
  background: #fff;
  border-radius: 8px;
}

.batch-table :deep(th) {
  background: #f9fafb;
  font-weight: 600;
  font-size: 13px;
  color: #374151;
}

.batch-footer {
  background: #fff;
  border-top: 1px solid #e5e7eb;
  padding: 14px 24px;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}
</style>
