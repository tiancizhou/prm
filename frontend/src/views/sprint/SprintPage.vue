<template>
  <div class="app-page sprint-page">
    <header class="page-header">
      <div class="title-block">
        <h1 class="page-title">{{ sprintText.pageTitle }}</h1>
        <p class="page-subtitle">{{ sprintText.pageSubtitle }}</p>
      </div>
      <div class="page-actions">
        <el-button type="primary" :icon="Plus" @click="showCreate = true">{{ sprintText.buttons.createSprint }}</el-button>
      </div>
    </header>

    <el-row :gutter="16" v-loading="loading" class="sprint-grid">
      <el-col v-for="sprint in list" :key="sprint.id" :xs="24" :sm="12" :lg="8">
        <el-card class="sprint-card surface-card" shadow="never">
          <template #header>
            <div class="sprint-header">
              <span>{{ sprint.name }}</span>
              <el-tag :type="sprintStatusType(sprint.status)">{{ sprintStatusLabel(sprint.status) }}</el-tag>
            </div>
          </template>
          <p class="goal">{{ sprint.goal || sprintText.labels.noGoal }}</p>
          <el-descriptions :column="2" size="small">
            <el-descriptions-item :label="sprintText.labels.startDate">{{ sprint.startDate || sprintText.labels.noGoal }}</el-descriptions-item>
            <el-descriptions-item :label="sprintText.labels.endDate">{{ sprint.endDate || sprintText.labels.noGoal }}</el-descriptions-item>
            <el-descriptions-item :label="sprintText.labels.capacity">{{ sprint.capacityHours }}{{ sprintText.labels.hourSuffix }}</el-descriptions-item>
          </el-descriptions>
          <div class="sprint-actions">
            <el-button v-if="sprint.status === 'PLANNING'" size="small" type="primary" @click="startSprint(sprint)">{{ sprintText.buttons.startSprint }}</el-button>
            <el-button v-if="sprint.status === 'ACTIVE'" size="small" type="warning" @click="closeSprint(sprint)">{{ sprintText.buttons.closeSprint }}</el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showCreate" :title="sprintText.dialogs.createSprint" width="520px">
      <el-form :model="form" label-width="110px">
        <el-form-item :label="sprintText.labels.name" required>
          <el-input v-model="form.name" name="sprintName" autocomplete="off" :placeholder="sprintText.placeholders.sprintName" />
        </el-form-item>
        <el-form-item :label="sprintText.labels.goal">
          <el-input v-model="form.goal" name="sprintGoal" autocomplete="off" type="textarea" :rows="2" :placeholder="sprintText.placeholders.sprintGoal" />
        </el-form-item>
        <el-form-item :label="sprintText.labels.capacityHours">
          <el-input-number v-model="form.capacityHours" :min="0" class="full-width-control" />
        </el-form-item>
        <el-form-item :label="sprintText.labels.dateRange">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            :range-separator="sprintText.placeholders.rangeSeparator"
            :start-placeholder="sprintText.placeholders.startDate"
            :end-placeholder="sprintText.placeholders.endDate"
            value-format="YYYY-MM-DD"
            class="full-width-control"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreate = false">{{ sprintText.buttons.cancel }}</el-button>
        <el-button type="primary" :loading="creating" @click="create">{{ sprintText.buttons.create }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { sprintApi } from '@/api/sprint'
import { SPRINT_PAGE_I18N } from '@/constants/sprint'
import { resolveThemeLocale } from '@/constants/theme'

const route = useRoute()
const projectId = Number(route.params.id)
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const sprintText = SPRINT_PAGE_I18N[currentLocale]

const loading = ref(false)
const list = ref<any[]>([])
const showCreate = ref(false)
const creating = ref(false)
const dateRange = ref<string[]>([])
const form = reactive({ name: '', goal: '', capacityHours: 0 })

function sprintStatusType(status: string): 'primary' | 'success' | 'warning' | 'info' | 'danger' {
  return status === 'PLANNING' ? 'info' : status === 'ACTIVE' ? 'success' : 'warning'
}

function sprintStatusLabel(status: string) {
  return sprintText.statusLabels[status as keyof typeof sprintText.statusLabels] || status
}

function withName(template: string, name: string) {
  return template.replace('{name}', name)
}

async function load() {
  loading.value = true
  try {
    const response = await sprintApi.list({ projectId })
    list.value = (response as any).data?.records || []
  } finally {
    loading.value = false
  }
}

async function create() {
  creating.value = true
  try {
    const payload: any = { ...form, projectId }
    if (dateRange.value?.length === 2) {
      payload.startDate = dateRange.value[0]
      payload.endDate = dateRange.value[1]
    }
    await sprintApi.create(payload)
    ElMessage.success(sprintText.messages.createSuccess)
    showCreate.value = false
    form.name = ''
    form.goal = ''
    form.capacityHours = 0
    dateRange.value = []
    await load()
  } finally {
    creating.value = false
  }
}

async function startSprint(sprint: any) {
  await ElMessageBox.confirm(withName(sprintText.confirms.startSprint, sprint.name), sprintText.dialogs.confirmTitle)
  await sprintApi.start(sprint.id)
  ElMessage.success(sprintText.messages.started)
  await load()
}

async function closeSprint(sprint: any) {
  await ElMessageBox.confirm(withName(sprintText.confirms.closeSprint, sprint.name), sprintText.dialogs.confirmTitle, { type: 'warning' })
  try {
    await sprintApi.close(sprint.id)
    ElMessage.success(sprintText.messages.closed)
    await load()
  } catch {
    return
  }
}

onMounted(() => {
  void load()
})
</script>

<style scoped>
.sprint-page {
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

.sprint-grid {
  margin: 0;
}

.sprint-card {
  height: 100%;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.sprint-card:hover {
  border-color: color-mix(in srgb, var(--app-color-primary) 28%, var(--app-border-soft));
  box-shadow: var(--app-shadow-soft);
}

.sprint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goal {
  color: var(--app-text-secondary);
  font-size: 13px;
  margin: 0 0 var(--space-md);
  min-height: 36px;
}

.sprint-actions {
  margin-top: var(--space-md);
}

.full-width-control {
  width: 100%;
}
</style>
