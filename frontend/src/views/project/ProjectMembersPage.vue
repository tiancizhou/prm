<template>
  <div>
    <div class="page-header">
      <h2>项目成员</h2>
      <el-button type="primary" icon="Plus" @click="openAdd">添加成员</el-button>
    </div>

    <el-card>
      <el-table :data="members" v-loading="loading" stripe>
        <el-table-column label="工号" prop="employeeNo" width="100" />
        <el-table-column label="姓名" prop="nickname" width="120" />
        <el-table-column label="用户名" prop="username" width="120" />
        <el-table-column label="系统角色" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.roleName" :type="roleTagType(row.role)" size="small">
              {{ row.roleName }}
            </el-tag>
            <span v-else class="text-muted">—</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="danger" link @click="removeMember(row)">移除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="showAdd" title="添加成员" width="460px">
      <el-form :model="addForm" label-width="90px">
        <el-form-item label="选择成员" required>
          <el-select
            v-model="addForm.userId"
            filterable
            remote
            :remote-method="searchUsers"
            :loading="userSearchLoading"
            placeholder="输入工号或姓名搜索"
            style="width: 100%"
          >
            <el-option
              v-for="u in userOptions"
              :key="u.id"
              :label="`${u.realName || u.username}${u.employeeNo ? ' (' + u.employeeNo + ')' : ''}`"
              :value="u.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="addMember">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { projectApi } from '@/api/project'
import http from '@/api/http'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const projectId = Number(route.params.id)

const loading = ref(false)
const members = ref<any[]>([])

const showAdd = ref(false)
const adding = ref(false)
const addForm = reactive({ userId: null as number | null })

const userOptions = ref<any[]>([])
const userSearchLoading = ref(false)

const roleTagTypeMap: Record<string, any> = {
  SUPER_ADMIN: 'danger',
  PROJECT_ADMIN: 'warning',
  PM: 'warning',
  DEV: 'primary',
  QA: 'success',
  GUEST: 'info'
}
function roleTagType(code: string) { return roleTagTypeMap[code] || '' }

async function loadMembers() {
  loading.value = true
  try {
    const res = await projectApi.getMembers(projectId)
    members.value = (res as any).data || []
  } finally { loading.value = false }
}

async function searchUsers(query: string) {
  if (!query) { userOptions.value = []; return }
  userSearchLoading.value = true
  try {
    const res: any = await http.get('/system/users', { params: { keyword: query, page: 1, size: 20 } })
    userOptions.value = res.data?.records || []
  } finally { userSearchLoading.value = false }
}

function openAdd() {
  addForm.userId = null
  userOptions.value = []
  showAdd.value = true
}

async function addMember() {
  if (!addForm.userId) { ElMessage.warning('请选择成员'); return }
  adding.value = true
  try {
    await projectApi.addMember(projectId, addForm.userId)
    ElMessage.success('添加成功')
    showAdd.value = false
    loadMembers()
  } finally { adding.value = false }
}

async function removeMember(m: any) {
  await ElMessageBox.confirm(`确定移除成员「${m.nickname || m.username}」？`, '确认', { type: 'warning' })
  await projectApi.removeMember(projectId, m.userId)
  ElMessage.success('已移除')
  loadMembers()
}

onMounted(loadMembers)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
.text-muted { color: #ccc; font-size: 12px; }
</style>
