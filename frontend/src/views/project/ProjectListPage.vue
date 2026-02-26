<template>
  <div>
    <div class="page-header">
      <h2>项目列表</h2>
      <el-button v-if="canCreateProject" type="primary" icon="Plus" @click="showCreate = true">新建项目</el-button>
    </div>

    <el-card>
      <el-row :gutter="12" style="margin-bottom: 16px">
        <el-col :span="8">
          <el-input v-model="query.keyword" placeholder="搜索项目名称/代号" clearable @change="loadProjects" prefix-icon="Search" />
        </el-col>
        <el-col :span="6">
          <el-select v-model="query.status" placeholder="项目状态" clearable @change="loadProjects">
            <el-option label="进行中" value="ACTIVE" />
            <el-option label="已归档" value="ARCHIVED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-col>
      </el-row>

      <el-table :data="projects" v-loading="loading" stripe>
        <el-table-column prop="name" label="项目名称" min-width="160">
          <template #default="{ row }">
            <el-link type="primary" @click="enterProject(row)">{{ row.name }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="code" label="代号" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="100" />
        <el-table-column prop="startDate" label="开始日期" width="120" />
        <el-table-column prop="endDate" label="结束日期" width="120" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="enterProject(row)">进入</el-button>
            <el-button size="small" type="warning" @click="archiveProject(row)" v-if="row.canEdit && row.status === 'ACTIVE'">归档</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :total="total"
        layout="total, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end"
        @change="loadProjects"
      />
    </el-card>

    <!-- 新建项目对话框 -->
    <el-dialog v-model="showCreate" title="新建项目" width="520px">
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="createForm.name" placeholder="如：PRM系统" />
        </el-form-item>
        <el-form-item label="项目代号" prop="code">
          <el-input v-model="createForm.code" placeholder="如：PRM（英文大写）" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="可见范围">
          <el-radio-group v-model="createForm.visibility">
            <el-radio value="PRIVATE">私有</el-radio>
            <el-radio value="PUBLIC">公开</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="createProject">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { projectApi, type Project } from '@/api/project'
import { useProjectStore } from '@/stores/project'
import { useAuthStore } from '@/stores/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const projectStore = useProjectStore()
const authStore = useAuthStore()

const canCreateProject = computed(() => {
  const roles = authStore.user?.roles ?? []
  return roles.includes('SUPER_ADMIN') || roles.includes('PROJECT_ADMIN')
})

const loading = ref(false)
const projects = ref<Project[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, keyword: '', status: '' })

const showCreate = ref(false)
const creating = ref(false)
const createFormRef = ref<FormInstance>()
const dateRange = ref<string[]>([])
const createForm = reactive({
  name: '', code: '', description: '', visibility: 'PRIVATE', startDate: '', endDate: ''
})
const createRules = {
  name: [{ required: true, message: '项目名称不能为空', trigger: 'blur' }],
  code: [{ required: true, message: '项目代号不能为空', trigger: 'blur' }]
}

function statusType(status: string) {
  const map: Record<string, any> = { ACTIVE: 'success', ARCHIVED: 'info', CLOSED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status: string) {
  const map: Record<string, string> = { ACTIVE: '进行中', ARCHIVED: '已归档', CLOSED: '已关闭' }
  return map[status] || status
}

function enterProject(project: Project) {
  projectStore.setCurrentProject(project)
  router.push(`/projects/${project.id}/overview`)
}

async function loadProjects() {
  loading.value = true
  try {
    const res = await projectApi.list({ ...query })
    const data = (res as any).data
    projects.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function createProject() {
  await createFormRef.value?.validate(async (valid) => {
    if (!valid) return
    creating.value = true
    try {
      if (dateRange.value?.length === 2) {
        createForm.startDate = dateRange.value[0] ?? ''
        createForm.endDate = dateRange.value[1] ?? ''
      }
      await projectApi.create({ ...createForm })
      ElMessage.success('项目创建成功')
      showCreate.value = false
      loadProjects()
    } finally {
      creating.value = false
    }
  })
}

async function archiveProject(project: Project) {
  await ElMessageBox.confirm(`确定归档项目 "${project.name}" 吗？`, '确认', { type: 'warning' })
  await projectApi.archive(project.id)
  ElMessage.success('已归档')
  loadProjects()
}

onMounted(loadProjects)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.page-header h2 { margin: 0; font-size: 20px; }
</style>
