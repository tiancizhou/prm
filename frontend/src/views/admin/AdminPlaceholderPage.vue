<template>
  <div class="app-page admin-placeholder-page">
    <header class="page-header">
      <div>
        <h1 class="page-title">{{ section.title }}</h1>
        <p class="page-subtitle">{{ section.description }}</p>
      </div>
      <div class="page-actions">
        <el-tag :type="sectionKey === 'system' ? 'success' : 'info'" effect="light">
          {{ sectionKey === 'system' ? adminText.status.available : adminText.status.comingSoon }}
        </el-tag>
      </div>
    </header>

    <section class="admin-summary-grid">
      <el-card
        v-for="card in section.summaryCards"
        :key="card.title"
        class="surface-card admin-summary-card"
        shadow="never"
      >
        <h2 class="admin-summary-title">{{ card.title }}</h2>
        <p class="admin-summary-description">{{ card.description }}</p>
      </el-card>
    </section>

    <el-card class="surface-card admin-planning-card" shadow="never">
      <h2 class="admin-planning-title">{{ section.planningTitle }}</h2>
      <ul class="admin-planning-list">
        <li v-for="point in section.planningPoints" :key="point" class="admin-planning-item">
          {{ point }}
        </li>
      </ul>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ADMIN_I18N, type AdminSectionKey } from '@/constants/admin'
import { resolveThemeLocale } from '@/constants/theme'

const route = useRoute()
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const adminText = ADMIN_I18N[currentLocale]

const sectionKey = computed<AdminSectionKey>(() => {
  if (route.name === 'AdminActivity') return 'activity'
  if (route.name === 'AdminCompany') return 'company'
  return 'system'
})

const section = computed(() => adminText.sections[sectionKey.value])
</script>

<style scoped>
.admin-placeholder-page {
  min-width: 0;
}

.admin-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-lg);
}

.admin-summary-card,
.admin-planning-card {
  padding: var(--space-xl);
  box-shadow: var(--app-subtle-shadow);
}

.admin-summary-title,
.admin-planning-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.3;
  color: var(--app-text-primary);
}

.admin-summary-description {
  margin: var(--space-sm) 0 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--app-text-secondary);
}

.admin-planning-list {
  margin: var(--space-lg) 0 0;
  padding-left: var(--space-xl);
  display: grid;
  gap: var(--space-sm);
}

.admin-planning-item {
  color: var(--app-text-secondary);
  line-height: 1.7;
}

@media (max-width: 1365px) {
  .admin-summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
