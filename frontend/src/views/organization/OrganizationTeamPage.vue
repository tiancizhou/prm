<template>
  <div class="app-page team-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ teamTitle }}</h1>
        <p class="page-subtitle">{{ teamSubtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Plus" @click="showCreate = true">
          {{ organizationText.peoplePage.addUser }}
        </el-button>
      </div>
    </header>

    <el-card class="surface-card team-surface" shadow="never">
      <el-row :gutter="12" class="filter-row">
        <el-col :xs="24" :sm="12" :md="8">
          <el-input
            v-model="keyword"
            name="teamKeyword"
            autocomplete="off"
            :placeholder="searchPlaceholder"
            clearable
            :prefix-icon="Search"
            @input="onSearchInput"
          />
        </el-col>
      </el-row>

      <el-table :data="list" v-loading="loading" class="team-table" :empty-text="emptyText">
        <el-table-column prop="id" :label="userText.labels.id" width="80" />
        <el-table-column prop="realName" :label="userText.labels.realName" min-width="120">
          <template #default="{ row }">
            <RouterLink :to="`/organization/team/${row.id}`" class="workspace-link-text workspace-link-text--primary">
              {{ row.realName || row.nickname || '-' }}
            </RouterLink>
          </template>
        </el-table-column>
        <el-table-column min-width="180" :label="accountLabel">
          <template #default="{ row }">
            <div class="account-cell">
              <div class="account-main">{{ row.username || '-' }}</div>
              <div v-if="showEmployeeNo(row)" class="account-sub">{{ row.employeeNo }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column :label="userText.labels.department" min-width="200">
          <template #default="{ row }">
            <span :title="row.departmentPath || row.department || '-'">{{ row.departmentPath || row.department || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column min-width="140" :label="positionLabel">
          <template #default="{ row }">
            {{ resolvePosition(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" :label="userText.labels.phone" min-width="140" />
        <el-table-column prop="email" :label="userText.labels.email" min-width="180" />
      </el-table>

      <div class="table-footer">
        <el-pagination
          v-model:current-page="page"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, sizes, prev, pager, next"
          class="page-pagination"
          @current-change="load"
          @size-change="load"
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
          <el-input v-model="form.realName" name="createRealName" autocomplete="off" :placeholder="userText.placeholders.realName" />
        </el-form-item>
        <el-form-item :label="userText.labels.employeeNo">
          <el-input v-model="form.employeeNo" name="createEmployeeNo" autocomplete="off" :placeholder="userText.placeholders.employeeNo" />
        </el-form-item>
        <el-form-item :label="userText.labels.department">
          <el-input v-model="form.department" name="createDepartment" autocomplete="off" :placeholder="userText.placeholders.department" />
        </el-form-item>
        <el-form-item :label="userText.labels.phone">
          <el-input v-model="form.phone" name="createPhone" autocomplete="off" :placeholder="userText.placeholders.phone" />
        </el-form-item>
        <el-form-item :label="userText.labels.email">
          <el-input v-model="form.email" name="createEmail" autocomplete="off" :placeholder="userText.placeholders.email" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">{{ userText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="creating" @click="create">{{ userText.buttons.create }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Plus, Search } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import http from '@/api/http'
import { ORGANIZATION_I18N } from '@/constants/organization'
import { resolveThemeLocale } from '@/constants/theme'
import { USER_PAGE_I18N } from '@/constants/user'

interface Role { id: number; name: string; code: string }

const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const organizationText = ORGANIZATION_I18N[currentLocale]
const userText = USER_PAGE_I18N[currentLocale]
const isZh = currentLocale === 'zh-CN'
const teamTitle = isZh ? '团队成员' : 'Team Members'
const teamSubtitle = isZh ? '查看与管理组织内成员' : 'View and manage organization members'
const accountLabel = isZh ? '账号/工号' : 'Account / Employee No'
const positionLabel = isZh ? '职位' : 'Position'
const searchPlaceholder = isZh ? '搜索姓名、账号、工号' : 'Search by name, account, employee no'
const emptyText = isZh ? '暂无成员' : 'No members yet'

const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(15)
const keyword = ref('')
const allRoles = ref<Role[]>([])
const roleNameMap = computed<Record<string, string>>(() => Object.fromEntries(allRoles.value.map((role) => [role.code, role.name])))

const showCreate = ref(false)
const creating = ref(false)
const form = reactive({ username: '', password: '', realName: '', employeeNo: '', department: '', team: isZh ? '默认团队' : 'Default Team', email: '', phone: '', roleIds: [] as number[] })

async function load() {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: page.value, size: pageSize.value }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    const response: any = await http.get('/system/users', { params })
    list.value = response.data?.records || []
    total.value = response.data?.total ?? 0
  } finally {
    loading.value = false
  }
}

function onSearchInput() {
  page.value = 1
  void load()
}

async function loadRoles() {
  try {
    const response: any = await http.get('/system/users/roles')
    allRoles.value = response.data || []
  } catch {
    allRoles.value = []
  }
}

function resolvePosition(row: any) {
  const roleCode = Array.isArray(row.roles) ? row.roles[0] : ''
  if (!roleCode) return '-'
  return roleNameMap.value[roleCode] ?? roleCode
}

function showEmployeeNo(row: any) {
  return Boolean(row.employeeNo) && row.employeeNo !== row.username
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
    form.team = isZh ? '默认团队' : 'Default Team'
    form.email = ''
    form.phone = ''
    form.roleIds = []
    showCreate.value = false
    await load()
  } finally {
    creating.value = false
  }
}

onMounted(() => {
  void load()
  void loadRoles()
})
</script>

<style scoped>
.team-page { min-width: 0; }

.title-block {
  display: flex;
  flex-direction: column;
}

.filter-row { margin-bottom: var(--space-md); }

.team-surface { transition: border-color 0.2s ease, box-shadow 0.2s ease; }

.team-surface:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 28%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.team-table {
  --el-table-border-color: var(--app-border-soft);
  --el-table-header-bg-color: var(--app-bg-muted);
}

.team-table :deep(.el-table__row:hover) {
  background-color: var(--app-bg-muted) !important;
}

.table-footer {
  margin-top: var(--space-lg);
  display: flex;
  justify-content: flex-end;
}

.account-cell { display: flex; flex-direction: column; gap: 2px; }
.account-main { color: var(--app-text-primary); }
.account-sub { font-size: 12px; line-height: 1.4; color: var(--app-text-muted); }
</style>
