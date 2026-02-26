<template>
  <div>
    <div class="page-header">
      <h2>迭代管理</h2>
      <el-button type="primary" icon="Plus" @click="showCreate = true">创建迭代</el-button>
    </div>
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="8" v-for="sprint in list" :key="sprint.id">
        <el-card class="sprint-card">
          <template #header>
            <div class="sprint-header">
              <span>{{ sprint.name }}</span>
              <el-tag :type="sprintStatusType(sprint.status)">{{ sprintStatusLabel(sprint.status) }}</el-tag>
            </div>
          </template>
          <p class="goal">{{ sprint.goal || '暂无目标' }}</p>
          <el-descriptions :column="2" size="small">
            <el-descriptions-item label="开始">{{ sprint.startDate }}</el-descriptions-item>
            <el-descriptions-item label="结束">{{ sprint.endDate }}</el-descriptions-item>
            <el-descriptions-item label="容量">{{ sprint.capacityHours }}h</el-descriptions-item>
          </el-descriptions>
          <div class="sprint-actions">
            <el-button size="small" type="primary" v-if="sprint.status === 'PLANNING'" @click="startSprint(sprint)">开始迭代</el-button>
            <el-button size="small" type="warning" v-if="sprint.status === 'ACTIVE'" @click="closeSprint(sprint)">关闭迭代</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showCreate" title="创建迭代" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="迭代名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="迭代目标"><el-input v-model="form.goal" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="容量(小时)"><el-input-number v-model="form.capacityHours" :min="0" /></el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker v-model="dateRange" type="daterange" range-separator="至"
            start-placeholder="开始日期" end-placeholder="结束日期" value-format="YYYY-MM-DD" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="create">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { sprintApi } from '@/api/sprint'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const projectId = Number(route.params.id)
const loading = ref(false)
const list = ref<any[]>([])
const showCreate = ref(false)
const creating = ref(false)
const dateRange = ref<string[]>([])
const form = reactive({ name: '', goal: '', capacityHours: 0 })

function sprintStatusType(s: string) {
  return s === 'PLANNING' ? 'info' : s === 'ACTIVE' ? 'success' : 'default'
}
function sprintStatusLabel(s: string) {
  return { PLANNING: '规划中', ACTIVE: '进行中', CLOSED: '已关闭' }[s] || s
}

async function load() {
  loading.value = true
  try {
    const res = await sprintApi.list({ projectId })
    list.value = (res as any).data?.records || []
  } finally { loading.value = false }
}

async function create() {
  creating.value = true
  try {
    const data: any = { ...form, projectId }
    if (dateRange.value?.length === 2) { data.startDate = dateRange.value[0]; data.endDate = dateRange.value[1] }
    await sprintApi.create(data)
    ElMessage.success('创建成功')
    showCreate.value = false; load()
  } finally { creating.value = false }
}

async function startSprint(sprint: any) {
  await ElMessageBox.confirm(`确定开始迭代 "${sprint.name}" 吗？`, '确认')
  await sprintApi.start(sprint.id)
  ElMessage.success('迭代已开始'); load()
}

async function closeSprint(sprint: any) {
  await ElMessageBox.confirm(`确定关闭迭代 "${sprint.name}" 吗？系统将检查未关闭的严重Bug。`, '确认', { type: 'warning' })
  try {
    await sprintApi.close(sprint.id)
    ElMessage.success('迭代已关闭'); load()
  } catch (e: any) {
    // error handled in http interceptor
  }
}

onMounted(load)
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
.sprint-card { height: 100%; }
.sprint-header { display: flex; justify-content: space-between; align-items: center; }
.goal { color: #666; font-size: 13px; margin: 0 0 12px; }
.sprint-actions { margin-top: 12px; }
</style>
