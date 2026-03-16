<template>
  <div class="app-page organization-placeholder-page">
    <header class="page-header">
      <div>
        <h1 class="page-title">{{ section.placeholderTitle }}</h1>
        <p class="page-subtitle">{{ section.placeholderDescription }}</p>
      </div>
      <div class="page-actions">
        <el-tag type="info" effect="light">{{ organizationText.status.comingSoon }}</el-tag>
      </div>
    </header>

    <section class="placeholder-summary-grid">
      <el-card
        v-for="card in section.summaryCards"
        :key="card.title"
        class="surface-card summary-card"
        shadow="never"
      >
        <h2 class="summary-card-title">{{ card.title }}</h2>
        <p class="summary-card-description">{{ card.description }}</p>
      </el-card>
    </section>

    <el-card class="surface-card placeholder-card" shadow="never">
      <div class="placeholder-card-head">
        <h2 class="placeholder-title">{{ section.planningTitle }}</h2>
      </div>

      <ul class="planning-list">
        <li v-for="point in section.planningPoints" :key="point" class="planning-item">
          {{ point }}
        </li>
      </ul>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ORGANIZATION_I18N, type OrganizationSectionKey } from '@/constants/organization'
import { resolveThemeLocale } from '@/constants/theme'

const route = useRoute()
const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const organizationText = ORGANIZATION_I18N[currentLocale]

const sectionKey = computed<OrganizationSectionKey>(() => {
  if (route.name === 'OrganizationCompany') {
    return 'company'
  }
  return 'activity'
})

const section = computed(() => organizationText.sections[sectionKey.value])
</script>

<style scoped>
.organization-placeholder-page {
  min-width: 0;
}

.placeholder-summary-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: var(--space-lg);
}

.summary-card,
.placeholder-card {
  padding: var(--space-xl);
  box-shadow: var(--app-subtle-shadow);
}

.summary-card-title,
.placeholder-title {
  margin: 0;
  font-size: 18px;
  line-height: 1.3;
  color: var(--app-text-primary);
}

.summary-card-description {
  margin: var(--space-sm) 0 0;
  font-size: 14px;
  line-height: 1.7;
  color: var(--app-text-secondary);
}

.planning-list {
  margin: 0;
  padding-left: var(--space-xl);
  display: grid;
  gap: var(--space-sm);
}

.planning-item {
  color: var(--app-text-secondary);
  line-height: 1.7;
}

@media (max-width: 1365px) {
  .placeholder-summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
