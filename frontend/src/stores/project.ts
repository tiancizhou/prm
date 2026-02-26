import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { Project } from '@/api/project'

export const useProjectStore = defineStore('project', () => {
  const currentProject = ref<Project | null>(null)

  function setCurrentProject(project: Project | null) {
    currentProject.value = project
  }

  return { currentProject, setCurrentProject }
})
