import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { Project, ProjectListParams } from '../utils/projectApi'
import projectApi from '../utils/projectApi'

export const useProjectStore = defineStore('project', () => {
  // 状态
  const projects = ref<Project[]>([])
  const currentProject = ref<Project | null>(null)
  const loading = ref(false)
  const pagination = ref({
    current: 1,
    size: 20,
    total: 0,
  })
  const searchParams = ref<Partial<ProjectListParams>>({})

  // 计算属性
  const totalProjects = computed(() => pagination.value.total)
  const hasProjects = computed(() => projects.value.length > 0)
  
  const projectsByStatus = computed(() => {
    const statusGroups: Record<string, Project[]> = {}
    projects.value.forEach(project => {
      const status = project.status
      if (!statusGroups[status]) {
        statusGroups[status] = []
      }
      statusGroups[status].push(project)
    })
    return statusGroups
  })

  const projectStats = computed(() => {
    const stats = {
      draft: 0,
      calculating: 0,
      completed: 0,
      reviewing: 0,
      approved: 0,
      rejected: 0,
    }
    
    projects.value.forEach(project => {
      const status = project.status.toLowerCase()
      if (status in stats) {
        stats[status as keyof typeof stats]++
      }
    })
    
    return stats
  })

  // 方法
  const loadProjects = async (params?: Partial<ProjectListParams>) => {
    loading.value = true
    try {
      const searchData = {
        current: pagination.value.current,
        size: pagination.value.size,
        ...searchParams.value,
        ...params,
      }
      
      const response = await projectApi.getProjectList(searchData)
      
      projects.value = response.records
      pagination.value = {
        current: response.current,
        size: response.size,
        total: response.total,
      }
      
      // 保存搜索参数
      if (params) {
        Object.assign(searchParams.value, params)
      }
    } catch (error) {
      console.error('加载项目列表失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const loadProjectDetail = async (id: number) => {
    loading.value = true
    try {
      const project = await projectApi.getProjectDetail(id)
      currentProject.value = project
      return project
    } catch (error) {
      console.error('加载项目详情失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const createProject = async (data: import('../utils/projectApi').CreateProjectRequest) => {
    loading.value = true
    try {
      const project = await projectApi.createProject(data)
      projects.value.unshift(project)
      pagination.value.total++
      return project
    } catch (error) {
      console.error('创建项目失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateProject = async (id: number, data: import('../utils/projectApi').UpdateProjectRequest) => {
    loading.value = true
    try {
      const project = await projectApi.updateProject(id, data)
      
      // 更新列表中的项目
      const index = projects.value.findIndex(p => p.id === id)
      if (index !== -1) {
        projects.value[index] = project
      }
      
      // 更新当前项目
      if (currentProject.value?.id === id) {
        currentProject.value = project
      }
      
      return project
    } catch (error) {
      console.error('更新项目失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const deleteProject = async (id: number) => {
    loading.value = true
    try {
      await projectApi.deleteProject(id)
      
      // 从列表中移除
      const index = projects.value.findIndex(p => p.id === id)
      if (index !== -1) {
        projects.value.splice(index, 1)
        pagination.value.total--
      }
      
      // 清除当前项目
      if (currentProject.value?.id === id) {
        currentProject.value = null
      }
    } catch (error) {
      console.error('删除项目失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const batchDeleteProjects = async (ids: number[]) => {
    loading.value = true
    try {
      await projectApi.batchDeleteProjects(ids)
      
      // 从列表中移除
      projects.value = projects.value.filter(p => !ids.includes(p.id))
      pagination.value.total -= ids.length
      
      // 如果当前项目被删除，清除
      if (currentProject.value && ids.includes(currentProject.value.id)) {
        currentProject.value = null
      }
    } catch (error) {
      console.error('批量删除项目失败:', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const updateProjectStatus = async (id: number, status: string) => {
    try {
      await projectApi.updateProjectStatus(id, status)
      
      // 更新列表中的项目状态
      const index = projects.value.findIndex(p => p.id === id)
      if (index !== -1) {
        projects.value[index].status = status
      }
      
      // 更新当前项目状态
      if (currentProject.value?.id === id) {
        currentProject.value.status = status
      }
    } catch (error) {
      console.error('更新项目状态失败:', error)
      throw error
    }
  }

  const searchProjects = async (keyword: string) => {
    searchParams.value.keyword = keyword
    pagination.value.current = 1
    await loadProjects()
  }

  const filterProjectsByStatus = async (status: string) => {
    searchParams.value.status = status
    pagination.value.current = 1
    await loadProjects()
  }

  const resetSearch = async () => {
    searchParams.value = {}
    pagination.value.current = 1
    await loadProjects()
  }

  const setPage = async (page: number) => {
    pagination.value.current = page
    await loadProjects()
  }

  const setPageSize = async (size: number) => {
    pagination.value.size = size
    pagination.value.current = 1
    await loadProjects()
  }

  // 清理方法
  const clearCurrentProject = () => {
    currentProject.value = null
  }

  const clearProjects = () => {
    projects.value = []
    pagination.value = {
      current: 1,
      size: 20,
      total: 0,
    }
    searchParams.value = {}
  }

  // 获取项目
  const getProjectById = (id: number) => {
    return projects.value.find(p => p.id === id)
  }

  return {
    // 状态
    projects,
    currentProject,
    loading,
    pagination,
    searchParams,
    
    // 计算属性
    totalProjects,
    hasProjects,
    projectsByStatus,
    projectStats,
    
    // 方法
    loadProjects,
    loadProjectDetail,
    createProject,
    updateProject,
    deleteProject,
    batchDeleteProjects,
    updateProjectStatus,
    searchProjects,
    filterProjectsByStatus,
    resetSearch,
    setPage,
    setPageSize,
    clearCurrentProject,
    clearProjects,
    getProjectById,
  }
})