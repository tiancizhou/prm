<template>
  <div class="app-page user-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ userText.pageTitle }}</h1>
        <p class="page-subtitle">{{ userText.pageSubtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Plus" @click="showCreate = true">{{ userText.buttons.newUser }}</el-button>
      </div>
    </header>

    <el-card class="surface-card user-surface" shadow="never">
      <el-row :gutter="12" class="filter-row">
        <el-col :xs="24" :sm="12" :md="8">
          <el-input
            v-model="keyword"
            name="userKeyword"
            autocomplete="off"
            :aria-label="userText.aria.searchUsers"
            :placeholder="userText.placeholders.searchUsers"
            clearable
            :prefix-icon="Search"
            @change="load"
          />
        </el-col>
      </el-row>

      <el-table :data="list" v-loading="loading" class="user-table">
        <el-table-column prop="id" :label="userText.labels.id" width="70" />
        <el-table-column prop="employeeNo" :label="userText.labels.employeeNo" width="120" />
        <el-table-column prop="realName" :label="userText.labels.realName" width="120" />
        <el-table-column prop="username" :label="userText.labels.username" width="140" />
        <el-table-column prop="department" :label="userText.labels.department" width="130" />
        <el-table-column prop="team" :label="userText.labels.team" width="120" />
        <el-table-column prop="email" :label="userText.labels.email" min-width="180" />
        <el-table-column :label="userText.labels.status" width="110">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
              {{ row.status === 'ACTIVE' ? userText.labels.active : userText.labels.disabled }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="userText.labels.roles" min-width="170">
          <template #default="{ row }">
            <el-tag v-for="roleCode in row.roles" :key="roleCode" size="small" class="role-tag">{{ roleNameMap[roleCode] || roleCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="userText.labels.actions" width="230" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="openEdit(row)">{{ userText.buttons.edit }}</el-button>
            <el-button v-if="row.status === 'ACTIVE'" size="small" type="warning" @click="toggleStatus(row, 'DISABLED')">{{ userText.buttons.disable }}</el-button>
            <el-button v-else size="small" type="success" @click="toggleStatus(row, 'ACTIVE')">{{ userText.buttons.enable }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          class="page-pagination"
          @change="load"
        />
      </div>
    </el-card>

    <el-dialog v-model="showCreate" :title="userText.dialogs.createUser" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item :label="userText.labels.username" required>
          <el-input v-model="form.username" name="createUsername" autocomplete="off" :placeholder="userText.placeholders.username" />
        </el-form-item>
        <el-form-item :label="userText.labels.password" required>
          <el-input v-model="form.password" name="createPassword" autocomplete="new-password" type="password" show-password :placeholder="userText.placeholders.password" />
        </el-form-item>
        <el-form-item :label="userText.labels.realName">
          <el-input v-model="form.realName" name="createRealName" autocomplete="off" :placeholder="userText.placeholders.syncFromMaster" />
        </el-form-item>
        <el-form-item :label="userText.labels.employeeNo">
          <el-input v-model="form.employeeNo" name="createEmployeeNo" autocomplete="off" :placeholder="userText.placeholders.syncFromMaster" />
        </el-form-item>
        <el-form-item :label="userText.labels.department">
          <el-input v-model="form.department" name="createDepartment" autocomplete="off" :placeholder="userText.placeholders.department" />
        </el-form-item>
        <el-form-item :label="userText.labels.team">
          <el-input v-model="form.team" name="createTeam" autocomplete="off" :placeholder="userText.placeholders.team" />
        </el-form-item>
        <el-form-item :label="userText.labels.email">
          <el-input v-model="form.email" name="createEmail" autocomplete="off" :placeholder="userText.placeholders.email" />
        </el-form-item>
        <el-form-item :label="userText.labels.roles">
          <el-select v-model="form.roleIds" multiple :placeholder="userText.placeholders.selectRoles" class="form-full-width">
            <el-option v-for="role in allRoles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">{{ userText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="creating" @click="create">{{ userText.buttons.create }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showEdit" :title="userText.dialogs.editUser" width="560px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item :label="userText.labels.username">
          <el-input v-model="editForm.username" name="editUsername" autocomplete="off" disabled />
        </el-form-item>
        <el-form-item :label="userText.labels.realName">
          <el-input v-model="editForm.realName" name="editRealName" autocomplete="off" :placeholder="userText.placeholders.syncFromMaster" />
        </el-form-item>
        <el-form-item :label="userText.labels.employeeNo">
          <el-input v-model="editForm.employeeNo" name="editEmployeeNo" autocomplete="off" :placeholder="userText.placeholders.syncFromMaster" />
        </el-form-item>
        <el-form-item :label="userText.labels.department">
          <el-input v-model="editForm.department" name="editDepartment" autocomplete="off" :placeholder="userText.placeholders.department" />
        </el-form-item>
        <el-form-item :label="userText.labels.team">
          <el-input v-model="editForm.team" name="editTeam" autocomplete="off" :placeholder="userText.placeholders.team" />
        </el-form-item>
        <el-form-item :label="userText.labels.email">
          <el-input v-model="editForm.email" name="editEmail" autocomplete="off" :placeholder="userText.placeholders.email" />
        </el-form-item>
        <el-form-item :label="userText.labels.phone">
          <el-input v-model="editForm.phone" name="editPhone" autocomplete="off" :placeholder="userText.placeholders.phone" />
        </el-form-item>
        <el-form-item :label="userText.labels.roles">
          <el-select v-model="editForm.roleIds" multiple :placeholder="userText.placeholders.selectRoles" class="form-full-width">
            <el-option v-for="role in allRoles" :key="role.id" :label="role.name" :value="role.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEdit = false">{{ userText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="updating" @click="update">{{ userText.buttons.save }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { resolveThemeLocale } from '@/constants/theme'
import { USER_PAGE_I18N } from '@/constants/user'

interface Role {
  id: number
  name: string
  code: string
}

const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const userText = USER_PAGE_I18N[currentLocale]

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(20)
const keyword = ref('')
const allRoles = ref<Role[]>([])
const roleNameMap = computed<Record<string, string>>(() => Object.fromEntries(allRoles.value.map(role => [role.code, role.name])))

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
    const response: any = await http.get('/system/users/roles')
    allRoles.value = response.data || []
  } catch {
    allRoles.value = []
  }
}

async function load() {
  loading.value = true
  try {
    const response: any = await http.get('/system/users', {
      params: {
        page: page.value,
        size: pageSize.value,
        keyword: keyword.value
      }
    })
    list.value = response.data?.records || []
    total.value = response.data?.total || 0
  } finally {
    loading.value = false
  }
}

async function create() {
  if (!form.username.trim() || !form.password.trim()) {
    ElMessage.warning(userText.messages.requiredUsernamePassword)
    return
  }
  creating.value = true
  try {
    await http.post('/system/users', { ...form })
    ElMessage.success(userText.messages.created)
    form.username = ''
    form.password = ''
    form.realName = ''
    form.employeeNo = ''
    form.department = ''
    form.team = ''
    form.email = ''
    form.roleIds = []
    showCreate.value = false
    await load()
  } finally {
    creating.value = false
  }
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
    ElMessage.success(userText.messages.saved)
    showEdit.value = false
    await load()
  } finally {
    updating.value = false
  }
}

async function toggleStatus(user: any, status: 'ACTIVE' | 'DISABLED') {
  await http.put(`/system/users/${user.id}/status`, null, { params: { status } })
  ElMessage.success(userText.messages.updated)
  await load()
}

onMounted(() => {
  void load()
  void loadRoles()
})
</script>

<style scoped>
.user-page {
  min-width: 0;
}

.title-block {
  display: flex;
  flex-direction: column;
  position: relative;
  padding-left: var(--space-md);
}

.title-block::before {
  content: '';
  position: absolute;
  left: 0;
  top: 4px;
  bottom: 4px;
  width: 3px;
  border-radius: var(--app-radius-pill);
  background: linear-gradient(180deg, var(--app-color-primary), var(--app-color-accent));
}

.user-surface {
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.user-surface:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 28%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.filter-row {
  margin-bottom: var(--space-lg);
}

.user-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.user-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.role-tag {
  margin-right: var(--space-xs);
  margin-bottom: var(--space-xs);
}

.table-footer {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: flex-end;
}

.form-full-width {
  width: 100%;
}
</style>
