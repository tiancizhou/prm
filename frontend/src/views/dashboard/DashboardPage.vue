<template>
  <div class="app-page dashboard-page" v-loading="loading">
    <header class="page-header dashboard-header">
      <div class="dashboard-header-top">
        <div class="dashboard-date">{{ todayLabel }}</div>
        <p class="dashboard-summary">{{ dashboardText.summaryMessage }}</p>
      </div>
      <div class="dashboard-greeting-row">
        <div class="greeting-block">
          <el-avatar :size="48" class="greeting-avatar">
            {{ userInitial }}
          </el-avatar>
          <div class="greeting-text">
            <h1 class="page-title greeting-title">{{ greetingText }}</h1>
            <p class="page-subtitle greeting-subtitle">{{ dashboardText.pageSubtitle }}</p>
          </div>
        </div>
      </div>
    </header>

    <section class="dashboard-my-stats">
      <div class="stats-section stats-section--assigned">
        <h2 class="stats-section-title">{{ dashboardText.assignedToMe }}</h2>
        <div class="stat-cards-row">
          <el-card class="stat-card" shadow="never">
            <div class="stat-value stat-value--primary">{{ myBugCount }}</div>
            <div class="stat-label">{{ dashboardText.bugCount }}</div>
          </el-card>
          <el-card class="stat-card" shadow="never">
            <div class="stat-value stat-value--primary">{{ myRequirementCount }}</div>
            <div class="stat-label">{{ dashboardText.requirementCount }}</div>
          </el-card>
        </div>
      </div>
      <div class="stats-section stats-section--assigned">
        <h2 class="stats-section-title">{{ dashboardText.completedTitle }}</h2>
        <div class="stat-cards-row">
          <el-card class="stat-card" shadow="never">
            <div class="stat-value stat-value--success">{{ myCompletedRequirementCount }}</div>
            <div class="stat-label">{{ dashboardText.completedRequirementCount }}</div>
          </el-card>
          <el-card class="stat-card" shadow="never">
            <div class="stat-value stat-value--success">{{ myResolvedBugCount }}</div>
            <div class="stat-label">{{ dashboardText.resolvedBugCount }}</div>
          </el-card>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { bugApi } from '@/api/bug'
import { requirementApi } from '@/api/requirement'
import { DASHBOARD_I18N } from '@/constants/dashboard'
import { resolveThemeLocale } from '@/constants/theme'

const authStore = useAuthStore()
const loading = ref(false)
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const dashboardText = DASHBOARD_I18N[currentLocale]

const myBugCount = ref(0)
const myRequirementCount = ref(0)
const myCompletedRequirementCount = ref(0)
const myResolvedBugCount = ref(0)

const userId = computed(() => authStore.user?.userId ?? 0)

const todayLabel = computed(() => {
  return new Intl.DateTimeFormat(currentLocale === 'zh-CN' ? 'zh-CN' : 'en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }).format(new Date())
})

const greetingPeriod = computed(() => {
  const hour = new Date().getHours()
  if (hour < 12) return dashboardText.greetingPeriod.morning
  if (hour < 18) return dashboardText.greetingPeriod.afternoon
  return dashboardText.greetingPeriod.evening
})

const greetingText = computed(() => {
  const name = authStore.user?.nickname || authStore.user?.username || 'User'
  return dashboardText.greeting(name, greetingPeriod.value)
})

const userInitial = computed(() => {
  const name = authStore.user?.nickname || authStore.user?.username || 'U'
  return name.slice(0, 1).toUpperCase()
})

async function loadMyStats() {
  const uid = userId.value
  if (!uid) {
    loading.value = false
    return
  }
  loading.value = true
  try {
    const [
      bugRes,
      reqRes,
      reqDoneRes,
      reqClosedRes,
      bugResolvedRes,
      bugClosedRes
    ] = await Promise.all([
      bugApi.list({ assigneeId: uid, page: 1, size: 1 }),
      requirementApi.list({ assigneeId: uid, page: 1, size: 1 }),
      requirementApi.list({ assigneeId: uid, status: 'DONE', page: 1, size: 1 }),
      requirementApi.list({ assigneeId: uid, status: 'CLOSED', page: 1, size: 1 }),
      bugApi.list({ assigneeId: uid, status: 'RESOLVED', page: 1, size: 1 }),
      bugApi.list({ assigneeId: uid, status: 'CLOSED', page: 1, size: 1 })
    ])
    myBugCount.value = (bugRes as any).data?.total ?? 0
    myRequirementCount.value = (reqRes as any).data?.total ?? 0
    myCompletedRequirementCount.value =
      ((reqDoneRes as any).data?.total ?? 0) + ((reqClosedRes as any).data?.total ?? 0)
    myResolvedBugCount.value =
      ((bugResolvedRes as any).data?.total ?? 0) + ((bugClosedRes as any).data?.total ?? 0)
  } finally {
    loading.value = false
  }
}

onMounted(loadMyStats)
</script>

<style scoped>
.dashboard-header {
  display: flex;
  flex-direction: column;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
}
.dashboard-header-top {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: var(--space-md);
}
.dashboard-date {
  font-size: 14px;
  color: var(--app-text-secondary);
}
.dashboard-summary {
  font-size: 14px;
  color: var(--app-text-secondary);
  margin: 0;
}
.dashboard-greeting-row {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}
.greeting-block {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
}
.greeting-avatar {
  background: var(--app-color-primary);
  color: #fff;
  font-weight: 600;
  font-size: 18px;
  flex-shrink: 0;
}
.greeting-text {
  display: flex;
  flex-direction: column;
  gap: var(--space-xs);
}
.greeting-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--app-text-primary);
}
.greeting-subtitle {
  margin: 0;
  font-size: 14px;
  color: var(--app-text-secondary);
}

.dashboard-my-stats {
  display: flex;
  flex-direction: column;
  gap: var(--space-xl);
}
.stats-section-title {
  margin: 0 0 var(--space-md);
  font-size: 16px;
  font-weight: 600;
  color: var(--app-text-primary);
}
.stats-section--assigned .stat-cards-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(160px, 1fr));
  gap: var(--space-lg);
}
.stat-card {
  border-radius: var(--app-radius-md);
  text-align: center;
  padding: var(--space-lg);
  min-height: 100px;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--space-sm);
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
  color: var(--app-text-primary);
}
.stat-value--primary {
  color: var(--app-color-primary);
}
.stat-value--success {
  color: var(--app-color-success);
}
.stat-label {
  font-size: 14px;
  color: var(--app-text-secondary);
}
@media (max-width: 767px) {
  .stats-section--assigned .stat-cards-row {
    grid-template-columns: 1fr;
  }
}
</style>
