<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="collapsed ? '64px' : '220px'" class="sidebar">
      <div class="logo">
        <el-icon size="24" color="#fff"><Management /></el-icon>
        <span v-if="!collapsed" class="logo-text">PRM</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="collapsed"
        background-color="#001529"
        text-color="#a6adb4"
        active-text-color="#fff"
        router
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>工作台</template>
        </el-menu-item>

        <el-menu-item index="/projects">
          <el-icon><Folder /></el-icon>
          <template #title>项目列表</template>
        </el-menu-item>

        <template v-if="currentProject">
          <el-divider style="margin: 8px 0; border-color: #1f3a5f" />
          <div v-if="!collapsed" class="project-label">{{ currentProject.name }}</div>

          <el-menu-item :index="`/projects/${currentProject.id}/overview`">
            <el-icon><House /></el-icon>
            <template #title>项目概览</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/requirements`">
            <el-icon><Document /></el-icon>
            <template #title>需求管理</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/tasks`">
            <el-icon><List /></el-icon>
            <template #title>任务管理</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/bugs`">
            <el-icon><Warning /></el-icon>
            <template #title>Bug 管理</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/sprints`">
            <el-icon><Calendar /></el-icon>
            <template #title>迭代管理</template>
          </el-menu-item>
          <el-menu-item :index="`/projects/${currentProject.id}/members`">
            <el-icon><User /></el-icon>
            <template #title>项目成员</template>
          </el-menu-item>
        </template>

        <el-divider v-if="isSuperAdmin" style="margin: 8px 0; border-color: #1f3a5f" />
        <el-menu-item v-if="isSuperAdmin" index="/system/users">
          <el-icon><Setting /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>

      <div class="collapse-btn" @click="collapsed = !collapsed">
        <el-icon>
          <ArrowLeft v-if="!collapsed" />
          <ArrowRight v-else />
        </el-icon>
      </div>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="username">{{ authStore.user?.nickname || authStore.user?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useProjectStore } from '@/stores/project'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const projectStore = useProjectStore()

const collapsed = ref(false)
const activeMenu = computed(() => route.path)
const currentProject = computed(() => projectStore.currentProject)
const isSuperAdmin = computed(() => authStore.user?.roles?.includes('SUPER_ADMIN'))

async function handleCommand(cmd: string) {
  if (cmd === 'logout') {
    await authStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #001529;
  display: flex;
  flex-direction: column;
  transition: width 0.3s;
  overflow: hidden;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  border-bottom: 1px solid #1f3a5f;
}

.logo-text {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 2px;
}

.project-label {
  color: #4a9eff;
  font-size: 12px;
  padding: 0 20px 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.el-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}

.collapse-btn {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  color: #a6adb4;
  border-top: 1px solid #1f3a5f;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #fff;
}

.header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.username {
  font-size: 14px;
  color: #333;
}

.main-content {
  background-color: #f5f5f5;
  overflow-y: auto;
}
</style>
