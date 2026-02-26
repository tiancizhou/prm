<template>
  <div>
    <div class="page-header">
      <h2>{{ project?.name }} · 概览</h2>
      <el-tag :type="statusType(project?.status)">{{ statusLabel(project?.status) }}</el-tag>
    </div>
    <el-card v-loading="loading">
      <el-descriptions :column="3" border>
        <el-descriptions-item label="项目代号">{{ project?.code }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ project?.ownerName }}</el-descriptions-item>
        <el-descriptions-item label="可见范围">{{ project?.visibility }}</el-descriptions-item>
        <el-descriptions-item label="开始日期">{{ project?.startDate }}</el-descriptions-item>
        <el-descriptions-item label="结束日期">{{ project?.endDate }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ project?.createdAt }}</el-descriptions-item>
        <el-descriptions-item label="项目描述" :span="3">{{ project?.description || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { projectApi } from '@/api/project'
import { useProjectStore } from '@/stores/project'

const route = useRoute()
const projectStore = useProjectStore()
const loading = ref(false)
const project = ref<any>(null)

function statusType(s?: string) {
  return s === 'ACTIVE' ? 'success' : s === 'ARCHIVED' ? 'info' : 'danger'
}
function statusLabel(s?: string) {
  const m: Record<string, string> = { ACTIVE: '进行中', ARCHIVED: '已归档', CLOSED: '已关闭' }
  return s ? (m[s] || s) : ''
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await projectApi.get(Number(route.params.id))
    project.value = (res as any).data
    projectStore.setCurrentProject(project.value)
  } finally { loading.value = false }
})
</script>
<style scoped>
.page-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; }
</style>
