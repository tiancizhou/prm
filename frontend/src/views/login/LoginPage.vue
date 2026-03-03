<template>
  <main class="app-page login-page">
    <section class="login-hero" aria-hidden="true">
      <div class="hero-content">
        <p class="hero-kicker">{{ loginText.heroKicker }}</p>
        <h2 class="hero-title">
          {{ loginText.heroTitleLine1 }}
          <br />
          {{ loginText.heroTitleLine2 }}
        </h2>
        <p class="hero-description">
          {{ loginText.heroDescription }}
        </p>
        <ul class="hero-points">
          <li v-for="point in loginText.heroPoints" :key="point">{{ point }}</li>
        </ul>
      </div>
    </section>

    <section class="login-panel">
      <el-card class="surface-card login-card" shadow="never">
        <header class="page-header login-header">
          <div>
            <h1 class="page-title">{{ loginText.pageTitle }}</h1>
            <p class="page-subtitle">{{ loginText.pageSubtitle }}</p>
          </div>
          <div class="page-actions">
            <el-tag type="primary" effect="light">{{ BRAND_SHORT_NAME }}</el-tag>
          </div>
        </header>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          size="large"
          label-width="72px"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item :label="loginText.usernameLabel" prop="username">
            <el-input
              v-model="form.username"
              name="username"
              autocomplete="username"
              :placeholder="loginText.usernamePlaceholder"
              prefix-icon="User"
            />
          </el-form-item>
          <el-form-item :label="loginText.passwordLabel" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              name="current-password"
              autocomplete="current-password"
              :placeholder="loginText.passwordPlaceholder"
              prefix-icon="Lock"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="login-btn" :loading="loading" @click="handleLogin">
              {{ loginText.loginButton }}
            </el-button>
          </el-form-item>
        </el-form>

        <p class="login-tip">{{ loginText.loginTip }}</p>
        <p class="visually-hidden" aria-live="polite">{{ liveMessage }}</p>
      </el-card>
    </section>
  </main>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import { LOGIN_PAGE_I18N } from '@/constants/login'
import { BRAND_SHORT_NAME } from '@/constants/brand'
import { resolveThemeLocale } from '@/constants/theme'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const liveMessage = ref('')

const currentLocale = resolveThemeLocale(typeof navigator === 'undefined' ? 'en-US' : navigator.language)
const loginText = LOGIN_PAGE_I18N[currentLocale]

const form = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [{ required: true, message: loginText.validationUsername, trigger: 'blur' }],
  password: [{ required: true, message: loginText.validationPassword, trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) {
    return
  }

  await formRef.value.validate(async (valid) => {
    if (!valid) {
      liveMessage.value = loginText.liveIncomplete
      return
    }

    loading.value = true
    liveMessage.value = loginText.liveValidating
    try {
      await authStore.login(form.username, form.password)
      ElMessage.success(loginText.successMessage)
      liveMessage.value = loginText.liveSuccess
      await router.push('/')
    } catch {
      liveMessage.value = loginText.liveFailed
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.15fr minmax(420px, 0.85fr);
  background: var(--app-login-shell-bg);
}

.login-hero {
  position: relative;
  padding: var(--space-5xl) var(--space-6xl);
  display: flex;
  align-items: center;
  background: var(--app-login-hero-bg);
}

.hero-content {
  max-width: 640px;
}

.hero-kicker {
  display: inline-flex;
  align-items: center;
  padding: var(--space-xs) var(--space-md);
  border-radius: var(--app-radius-pill);
  border: 1px solid var(--app-login-kicker-border);
  color: var(--app-login-kicker-text);
  margin: 0 0 var(--space-xl);
  font-size: 13px;
}

.hero-title {
  margin: 0;
  font-family: var(--app-font-heading);
  color: var(--app-login-title);
  font-size: 44px;
  line-height: 1.2;
  letter-spacing: 0.02em;
}

.hero-description {
  margin: var(--space-xl) 0 var(--space-lg);
  color: var(--app-login-description);
  font-size: 17px;
  line-height: 1.8;
  max-width: 520px;
}

.hero-points {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: var(--space-sm);
}

.hero-points li {
  font-size: 15px;
  color: var(--app-login-point);
  display: flex;
  align-items: center;
  gap: var(--space-md);
}

.hero-points li::before {
  content: '';
  width: 8px;
  height: 8px;
  border-radius: var(--app-radius-pill);
  background: var(--app-color-primary);
}

.login-panel {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: var(--space-4xl) var(--space-5xl);
  background: var(--app-login-panel-bg);
  backdrop-filter: blur(10px);
  border-left: 1px solid var(--app-login-panel-border);
}

.login-card :deep(.el-card__body) {
  padding: var(--space-2xl);
}

.login-header {
  margin-bottom: var(--space-xl);
}

.login-header .page-title {
  font-size: 32px;
}

.login-form :deep(.el-form-item__label) {
  font-weight: 600;
  color: var(--app-login-label);
}

.login-form :deep(.el-input) {
  width: 100%;
}

.login-btn {
  width: 100%;
  min-height: 46px;
  font-size: 15px;
  letter-spacing: 0.08em;
}

.login-tip {
  margin: var(--space-md) 0 0;
  color: var(--app-login-tip);
  font-size: 13px;
}

@media (max-width: 1919px) {
  .hero-title {
    font-size: 40px;
  }

  .login-card :deep(.el-card__body) {
    padding: var(--space-xl);
  }
}

@media (max-width: 1439px) {
  .login-page {
    grid-template-columns: 1.1fr minmax(380px, 0.9fr);
  }

  .login-hero {
    padding: var(--space-3xl) var(--space-4xl);
  }

  .hero-title {
    font-size: 36px;
  }

  .hero-description {
    font-size: 16px;
  }

  .login-panel {
    padding: var(--space-xl) var(--space-2xl);
  }

  .login-header .page-title {
    font-size: 30px;
  }
}

@media (max-width: 1365px) {
  .login-page {
    grid-template-columns: 1fr 1fr;
  }

  .login-hero {
    padding: var(--space-2xl);
  }

  .hero-title {
    font-size: 32px;
  }

  .hero-description {
    font-size: 15px;
    line-height: 1.7;
  }

  .login-panel {
    padding: var(--space-xl);
  }

  .login-card :deep(.el-card__body) {
    padding: var(--space-lg);
  }

  .login-header .page-title {
    font-size: 28px;
  }
}
</style>
