<template>
  <div>
    <div class="page-header">
      <h2>Bug 管理</h2>
      <el-button type="primary" icon="Plus" @click="showCreate = true">提交 Bug</el-button>
    </div>
    <el-card>
      <el-row :gutter="12" style="margin-bottom: 16px">
        <el-col :span="6">
          <el-select v-model="query.status" placeholder="状态" clearable @change="load">
            <el-option label="新建" value="NEW" /><el-option label="已确认" value="CONFIRMED" />
            <el-option label="已指派" value="ASSIGNED" /><el-option label="已解决" value="RESOLVED" />
            <el-option label="已验证" value="VERIFIED" /><el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-col>
        <el-col :span="6">
          <el-select v-model="query.severity" placeholder="严重程度" clearable @change="load">
            <el-option label="阻塞" value="BLOCKER" /><el-option label="严重" value="CRITICAL" />
            <el-option label="一般" value="NORMAL" /><el-option label="轻微" value="MINOR" />
          </el-select>
        </el-col>
        <el-col :span="8">
          <el-input v-model="query.keyword" placeholder="搜索标题" clearable @change="load" prefix-icon="Search" />
        </el-col>
      </el-row>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="severityType(row.severity)" size="small">{{ row.severity }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="statusLabel" label="状态" width="100">
          <template #default="{ row }"><el-tag>{{ row.statusLabel }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="处理人" width="100" />
        <el-table-column prop="reporterName" label="提报人" width="100" />
        <el-table-column prop="createdAt" label="提报时间" width="160" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="assign(row)" v-if="row.status === 'CONFIRMED'">指派</el-button>
            <el-button size="small" type="success" @click="resolve(row)" v-if="row.status === 'ASSIGNED'">解决</el-button>
            <el-button size="small" type="info" @click="verify(row)" v-if="row.status === 'RESOLVED'">验证</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="query.page" v-model:page-size="query.size" :total="total"
        layout="total, prev, pager, next" style="margin-top: 16px; justify-content: flex-end" @change="load" />
    </el-card>

    <el-dialog v-model="showCreate" title="提交 Bug" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="Bug标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="所属模块"><el-input v-model="form.module" /></el-form-item>
        <el-row>
          <el-col :span="12">
            <el-form-item label="严重程度">
              <el-select v-model="form.severity">
                <el-option label="阻塞" value="BLOCKER" /><el-option label="严重" value="CRITICAL" />
                <el-option label="一般" value="NORMAL" /><el-option label="轻微" value="MINOR" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="form.priority">
                <el-option label="高" value="HIGH" /><el-option label="中" value="MEDIUM" /><el-option label="低" value="LOW" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="重现步骤"><el-input v-model="form.steps" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="期望结果"><el-input v-model="form.expectedResult" /></el-form-item>
        <el-form-item label="实际结果"><el-input v-model="form.actualResult" /></el-form-item>
        <el-form-item label="环境信息"><el-input v-model="form.environment" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="create">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { bugApi } from '@/api/bug'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const projectId = Number(route.params.id)
const loading = ref(false)
const list = ref<any[]>([])
const total = ref(0)
const query = reactive({ page: 1, size: 20, projectId, status: '', severity: '', keyword: '' })
const showCreate = ref(false)
const creating = ref(false)
const form = reactive({ title: '', module: '', severity: 'NORMAL', priority: 'MEDIUM', steps: '', expectedResult: '', actualResult: '', environment: '' })

function severityType(s: string) {
  const m: Record<string, any> = { BLOCKER: 'danger', CRITICAL: 'warning', NORMAL: '', MINOR: 'info' }
  return m[s] || ''
}

async function load() {
  loading.value = true
  try {
    const res = await bugApi.list({ ...query })
    const data = (res as any).data
    list.value = data.records; total.value = data.total
  } finally { loading.value = false }
}

async function create() {
  creating.value = true
  try {
    await bugApi.create({ ...form, projectId })
    ElMessage.success('提交成功')
    showCreate.value = false; load()
  } finally { creating.value = false }
}

async function assign(row: any) {
  const result = await ElMessageBox.prompt('请输入处理人用户ID', '指派', { inputType: 'number' })
  const assigneeId = (result as any).value
  await bugApi.assign(row.id, Number(assigneeId))
  ElMessage.success('已指派'); load()
}

async function resolve(row: any) {
  await bugApi.updateStatus(row.id, 'RESOLVED', 'FIXED')
  ElMessage.success('已标记解决'); load()
}

async function verify(row: any) {
  await bugApi.updateStatus(row.id, 'VERIFIED')
  ElMessage.success('已验证'); load()
}

onMounted(load)
</script>
<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
</style>
