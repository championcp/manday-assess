import api from './api'

/**
 * 项目相关的API接口
 */
export interface ProjectListParams {
  keyword?: string
  status?: string
  dateRange?: string[]
  current: number
  size: number
}

export interface ProjectListResponse {
  records: Project[]
  total: number
  current: number
  size: number
}

export interface Project {
  id: number
  projectCode: string
  projectName: string
  description: string
  projectType: string
  status: string
  totalFunctionPoints?: number
  adjustedFunctionPoints?: number
  estimatedCost?: number
  estimatedDuration?: number
  createdBy: string
  createTime: string
  updateTime: string
  updatedBy?: string
  calculationResult?: any
  costEstimation?: any
}

export interface CreateProjectRequest {
  projectCode: string
  projectName: string
  description: string
  projectType: string
  plannedStartDate: string
  plannedEndDate: string
  developmentPlatform: string[]
  databaseType: string[]
  architectureType: string
  deploymentType: string[]
  projectManager: string
  technicalLead: string
  teamSize: {
    developers: number
    testers: number
    others: number
  }
  budgetLimit: string
  budgetType: string
  remarks: string
}

export interface UpdateProjectRequest extends CreateProjectRequest {}

class ProjectApiService {
  /**
   * 获取项目列表
   */
  async getProjectList(params: ProjectListParams): Promise<ProjectListResponse> {
    return api.get('/api/projects', { params })
  }

  /**
   * 获取项目详情
   */
  async getProjectDetail(id: number): Promise<Project> {
    return api.get(`/api/projects/${id}`)
  }

  /**
   * 创建项目
   */
  async createProject(data: CreateProjectRequest): Promise<Project> {
    return api.post('/api/projects', data)
  }

  /**
   * 更新项目
   */
  async updateProject(id: number, data: UpdateProjectRequest): Promise<Project> {
    return api.put(`/api/projects/${id}`, data)
  }

  /**
   * 删除项目
   */
  async deleteProject(id: number): Promise<void> {
    return api.delete(`/api/projects/${id}`)
  }

  /**
   * 批量删除项目
   */
  async batchDeleteProjects(ids: number[]): Promise<void> {
    return api.delete('/api/projects/batch', { data: { ids } })
  }

  /**
   * 复制项目
   */
  async duplicateProject(id: number): Promise<Project> {
    return api.post(`/api/projects/${id}/duplicate`)
  }

  /**
   * 导出项目报告
   */
  async exportProjectReport(id: number, format = 'pdf'): Promise<Blob> {
    const response = await api.get(`/api/projects/${id}/export`, {
      params: { format },
      responseType: 'blob'
    })
    return response.data
  }

  /**
   * 获取项目状态统计
   */
  async getProjectStats(): Promise<any> {
    return api.get('/api/projects/stats')
  }

  /**
   * 更新项目状态
   */
  async updateProjectStatus(id: number, status: string): Promise<void> {
    return api.patch(`/api/projects/${id}/status`, { status })
  }
}

export const projectApi = new ProjectApiService()
export default projectApi