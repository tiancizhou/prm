<template>
  <div>
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" icon="Plus" @click="showCreate = true">新建用户</el-button>
    </div>
    <el-card>
      <el-row :gutter="12" style="margin-bottom: 16px">
        <el-col :span="8">
          <el-input v-model="keyword" placeholder="搜索 用户名/工号/姓名" clearable @change="load" prefix-icon="Search" />
        </el-col>
      </el-row>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="employeeNo" label="工号" width="100" />
        <el-table-column prop="realName" label="姓名" width="100" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="team" label="小组" width="100" />
        <el-table-column prop="email" label="邮箱" min-width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag v-for="r in row.roles" :key="r" size="small" style="margin-right: 4px">
              {{ roleNameMap[r] || r }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" v-if="row.status === 'ACTIVE'" @click="toggleStatus(row, 'DISABLED')">禁用</el-button>
            <el-button size="small" type="success" v-else @click="toggleStatus(row, 'ACTIVE')">启用</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" v-model:page-size="pageSize" :total="total"
        layout="total, prev, pager, next" style="margin-top: 16px; justify-content: flex-end" @change="load" />
    </el-card>

    <el-dialog v-model="showCreate" title="新建用户" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="用户名" required><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="初始密码" required><el-input v-model="form.password" type="password" show-password /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" placeholder="与主数据同步" /></el-form-item>
        <el-form-item label="工号"><el-input v-model="form.employeeNo" placeholder="与主数据同步" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="form.department" /></el-form-item>
        <el-form-item label="小组"><el-input v-model="form.team" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleIds" multiple placeholder="选择系统角色" style="width:100%">
            <el-option v-for="r in allRoles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="create">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEdit" title="编辑用户" width="520px">
      <el-form :model="editForm" label-width="90px">
        <el-form-item label="用户名"><el-input v-model="editForm.username" disabled /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="editForm.realName" placeholder="与主数据同步" /></el-form-item>
        <el-form-item label="工号"><el-input v-model="editForm.employeeNo" placeholder="与主数据同步" /></el-form-item>
        <el-form-item label="部门"><el-input v-model="editForm.department" /></el-form-item>
        <el-form-item label="小组"><el-input v-model="editForm.team" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="editForm.email" /></el-form-item>
        <el-form-item label="手机"><el-input v-model="editForm.phone" /></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.roleIds" multiple placeholder="选择系统角色" style="width:100%">
            <el-option v-for="r in allRoles" :key="r.id" :label="r.name" :value="r.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">取消</el-button>
        <el-button type="primary" :loading="updating" @click="update">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import http from '@/api/http'
import { ElMessage } from 'element-plus'

interface Role { id: number; name: string; code: string }

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const allRoles = ref<Role[]>([])
const roleNameMap = computed<Record<string, string>>(() =>
  Object.fromEntries(allRoles.value.map(r => [r.code, r.name]))
)
const showCreate = ref(false)
const showEdit = ref(false)
const creating = ref(false)
const updating = ref(false)
const form = reactive({
  username: '',
  password: '',
  realName: '',
  employeeNo: '',
  department: '',
  team: '',
  email: '',
  roleIds: [] as number[]
})
const editForm = reactive({
  id: 0,
  username: '',
  realName: '',
  employeeNo: '',
  department: '',
  team: '',
  email: '',
  phone: '',
  roleIds: [] as number[]
})

async function loadRoles() {
  try {
    const res: any = await http.get('/system/users/roles')
    allRoles.value = res.data || []
  } catch {}
}

async function load() {
  loading.value = true
  try {
    const res: any = await http.get('/system/users', { params: { page: page.value, size: pageSize.value, keyword: keyword.value } })
    list.value = res.data?.records || []; total.value = res.data?.total || 0
  } finally { loading.value = false }
}

async function create() {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning('用户名和密码为必填'); return
  }
  creating.value = true
  try {
    await http.post('/system/users', { ...form })
    ElMessage.success('创建成功')
    form.username = ''; form.password = ''; form.realName = ''
    form.employeeNo = ''; form.department = ''; form.team = ''
    form.email = ''; form.roleIds = []
    showCreate.value = false; load()
  } finally { creating.value = false }
}

function openEdit(row: any) {
  editForm.id = row.id
  editForm.username = row.username ?? ''
  editForm.realName = row.realName ?? ''
  editForm.employeeNo = row.employeeNo ?? ''
  editForm.department = row.department ?? ''
  editForm.team = row.team ?? ''
  editForm.email = row.email ?? ''
  editForm.phone = row.phone ?? ''
  editForm.roleIds = row.roleIds ?? []
  showEdit.value = true
}

async function update() {
  updating.value = true
  try {
    await http.put(`/system/users/${editForm.id}`, {
      realName: editForm.realName,
      employeeNo: editForm.employeeNo,
      department: editForm.department,
      team: editForm.team,
      email: editForm.email,
      phone: editForm.phone,
      roleIds: editForm.roleIds
    })
    ElMessage.success('保存成功')
    showEdit.value = false
    load()
  } finally {
    updating.value = false
  }
}

async function toggleStatus(user: any, status: string) {
  await http.put(`/system/users/${user.id}/status`, null, { params: { status } })
  ElMessage.success('已更新'); load()
}

onMounted(() => { load(); loadRoles() })
</script>
<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
</style>
