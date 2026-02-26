<template>
  <div class="dashboard">
    <div class="page-header">
      <h2>工作台总览</h2>
    </div>

    <el-row :gutter="16" v-loading="loading">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value" style="color: #409eff">{{ overview?.activeProjects ?? '-' }}</div>
          <div class="stat-label">进行中项目</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value" style="color: #e6a23c">{{ overview?.overdueProjects ?? '-' }}</div>
          <div class="stat-label">延期项目</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value" style="color: #f56c6c">{{ overview?.openBugs ?? '-' }}</div>
          <div class="stat-label">未关闭 Bug</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-value" style="color: #67c23a">{{ overview?.inProgressTasks ?? '-' }}</div>
          <div class="stat-label">进行中任务</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="12">
        <el-card>
          <template #header><span>需求统计</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总需求数">{{ overview?.totalRequirements ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="已完成">{{ overview?.doneRequirements ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="完成率">
              {{ overview ? completionRate(overview.doneRequirements, overview.totalRequirements) : '-' }}
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header><span>Bug 统计</span></template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="总 Bug 数">{{ overview?.totalBugs ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="未关闭">{{ overview?.openBugs ?? '-' }}</el-descriptions-item>
            <el-descriptions-item label="严重/阻塞">
              <el-tag type="danger">{{ overview?.criticalOpenBugs ?? '-' }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { dashboardApi } from '@/api/dashboard'

const loading = ref(false)
const overview = ref<any>(null)

function completionRate(done: number, total: number) {
  if (!total) return '0%'
  return Math.round((done / total) * 100) + '%'
}

async function loadOverview() {
  loading.value = true
  try {
    const res = await dashboardApi.overview()
    overview.value = (res as any).data
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>

<style scoped>
.dashboard { padding: 0; }
.page-header { margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 20px; color: #1a1a1a; }

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: box-shadow 0.2s;
}
.stat-card:hover { box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
.stat-value { font-size: 36px; font-weight: 700; line-height: 1.2; }
.stat-label { color: #666; font-size: 13px; margin-top: 4px; }
</style>
