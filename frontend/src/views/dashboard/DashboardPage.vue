<template>
  <div class="app-page dashboard-page" v-loading="loading">
    <header class="page-header">
      <div>
        <h1 class="page-title">{{ dashboardText.pageTitle }}</h1>
        <p class="page-subtitle">{{ dashboardText.pageSubtitle }}</p>
      </div>
      <div class="page-actions">
        <div class="dashboard-date">{{ todayLabel }}</div>
      </div>
    </header>

    <section class="metrics-grid">
      <el-card v-for="item in metricCards" :key="item.key" class="metric-card" shadow="never">
        <div class="metric-icon" :class="item.tone">
          <el-icon><component :is="item.icon" /></el-icon>
        </div>
        <div class="metric-main">
          <strong>{{ item.value }}</strong>
          <span>{{ item.label }}</span>
        </div>
      </el-card>
    </section>

    <section class="insight-grid">
      <el-card shadow="never" class="surface-card">
        <template #header>
          <div class="card-title-row">
            <span>{{ dashboardText.requirementDelivery }}</span>
            <el-tag type="success" effect="light">{{ requirementCompletion }}</el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="dashboardText.requirementLabels.totalRequirements">
            {{ overview?.totalRequirements ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="dashboardText.requirementLabels.doneRequirements">
            {{ overview?.doneRequirements ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="dashboardText.requirementLabels.completion">
            {{ requirementCompletion }}
          </el-descriptions-item>
          <el-descriptions-item :label="dashboardText.requirementLabels.inProgressTasks">
            {{ overview?.inProgressTasks ?? '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="surface-card">
        <template #header>
          <div class="card-title-row">
            <span>{{ dashboardText.qualityStatus }}</span>
            <el-tag type="danger" effect="light">
              {{ overview?.criticalOpenBugs ?? 0 }} {{ dashboardText.criticalSuffix }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item :label="dashboardText.qualityLabels.totalBugs">
            {{ overview?.totalBugs ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="dashboardText.qualityLabels.openBugs">
            {{ overview?.openBugs ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="dashboardText.qualityLabels.criticalOrBlocker">
            {{ overview?.criticalOpenBugs ?? '-' }}
          </el-descriptions-item>
          <el-descriptions-item :label="dashboardText.qualityLabels.overdueProjects">
            {{ overview?.overdueProjects ?? '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { CircleCheckFilled, DataAnalysis, FolderOpened, WarningFilled } from '@element-plus/icons-vue'
import { dashboardApi } from '@/api/dashboard'
import { DASHBOARD_I18N } from '@/constants/dashboard'
import { resolveThemeLocale } from '@/constants/theme'

interface Overview {
  activeProjects: number
  overdueProjects: number
  openBugs: number
  inProgressTasks: number
  totalRequirements: number
  doneRequirements: number
  totalBugs: number
  criticalOpenBugs: number
}

const loading = ref(false)
const overview = ref<Overview | null>(null)
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const dashboardText = DASHBOARD_I18N[currentLocale]

const todayLabel = computed(() => {
  return new Intl.DateTimeFormat(currentLocale, {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }).format(new Date())
})

const requirementCompletion = computed(() => {
  if (!overview.value?.totalRequirements) {
    return '0%'
  }
  return `${Math.round((overview.value.doneRequirements / overview.value.totalRequirements) * 100)}%`
})

const metricCards = computed(() => {
  return [
    {
      key: 'activeProjects',
      label: dashboardText.metricLabels.activeProjects,
      value: overview.value?.activeProjects ?? '-',
      icon: FolderOpened,
      tone: 'tone-blue'
    },
    {
      key: 'overdueProjects',
      label: dashboardText.metricLabels.overdueProjects,
      value: overview.value?.overdueProjects ?? '-',
      icon: WarningFilled,
      tone: 'tone-amber'
    },
    {
      key: 'openBugs',
      label: dashboardText.metricLabels.openBugs,
      value: overview.value?.openBugs ?? '-',
      icon: DataAnalysis,
      tone: 'tone-red'
    },
    {
      key: 'inProgressTasks',
      label: dashboardText.metricLabels.inProgressTasks,
      value: overview.value?.inProgressTasks ?? '-',
      icon: CircleCheckFilled,
      tone: 'tone-green'
    }
  ]
})

async function loadOverview() {
  loading.value = true
  try {
    const response = await dashboardApi.overview()
    overview.value = (response as any).data
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>

<style scoped>
.dashboard-date {
  font-size: 13px;
  color: var(--app-text-secondary);
  background: var(--app-bg-elevated);
  border: 1px solid var(--app-border-soft);
  border-radius: var(--app-radius-pill);
  padding: var(--space-sm) var(--space-md);
}

.metrics-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(220px, 1fr));
  gap: var(--space-lg);
}

.metric-card {
  border-radius: var(--app-radius-md);
  min-height: 112px;
}

.metric-card :deep(.el-card__body) {
  display: grid;
  grid-template-columns: 52px 1fr;
  align-items: center;
  gap: var(--space-md);
}

.metric-icon {
  width: 52px;
  height: 52px;
  border-radius: var(--app-radius-sm);
  display: grid;
  place-items: center;
  font-size: 22px;
}

.tone-blue {
  color: var(--app-color-primary);
  background: var(--app-color-primary-soft);
}

.tone-amber {
  color: var(--app-color-warning);
  background: var(--app-color-warning-soft);
}

.tone-red {
  color: var(--app-color-danger);
  background: var(--app-color-danger-soft);
}

.tone-green {
  color: var(--app-color-success);
  background: var(--app-color-success-soft);
}

.metric-main {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}

.metric-main strong {
  font-size: 34px;
  line-height: 1;
  color: var(--app-text-primary);
}

.metric-main span {
  color: var(--app-text-secondary);
  font-size: 14px;
}

.insight-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-lg);
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 15px;
  color: var(--app-text-primary);
}

@media (max-width: 1439px) {
  .metrics-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1919px) {
  .metrics-grid {
    gap: var(--space-md);
  }

  .metric-main strong {
    font-size: 32px;
  }
}

@media (max-width: 1365px) {
  .insight-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 1199px) {
  .metrics-grid {
    grid-template-columns: 1fr;
  }
}
</style>
