import api from './api'

/**
 * NESMA计算相关的API接口
 */
export interface FunctionPointElement {
  name: string
  description: string
  det: number
  ret?: number
  ftr?: number
  complexity: string
  points: number
}

export interface FunctionPointData {
  ilf: FunctionPointElement[]
  eif: FunctionPointElement[]
  ei: FunctionPointElement[]
  eo: FunctionPointElement[]
  eq: FunctionPointElement[]
}

export interface VafData {
  TF01: number
  TF02: number
  TF03: number
  TF04: number
  TF05: number
  TF06: number
  TF07: number
  TF08: number
  TF09: number
  TF10: number
  TF11: number
  TF12: number
  TF13: number
  TF14: number
}

export interface CalculationRequest {
  projectId: number
  functionPointData: FunctionPointData
  vafData: VafData
  reuseLevel: string
}

export interface CalculationResult {
  projectId: number
  unadjustedPoints: number
  vafTotalScore: number
  vafAdjustmentFactor: number
  adjustedFunctionPoints: number
  reuseLevel: string
  reuseCoefficient: number
  finalFunctionPoints: number
  estimatedCost: number
  estimatedDuration: number
  calculationDetails: {
    elementsSummary: Array<Record<string, unknown>>
    vafBreakdown: Array<Record<string, unknown>>
    costBreakdown: Record<string, unknown>
  }
  calculationTime: string
}

export interface SaveDraftRequest {
  projectId: number
  functionPointData?: FunctionPointData
  vafData?: VafData
  reuseLevel?: string
  step: number
}

class NesmaApiService {
  /**
   * 获取项目的NESMA计算数据
   */
  async getCalculationData(projectId: number): Promise<{ functionPointData: FunctionPointData; vafData: VafData; reuseLevel: string; step: number } | null> {
    return api.get(`/api/nesma/projects/${projectId}/calculation-data`)
  }

  /**
   * 保存NESMA计算草稿
   */
  async saveDraft(data: SaveDraftRequest): Promise<void> {
    return api.post('/api/nesma/draft', data)
  }

  /**
   * 获取NESMA计算草稿
   */
  async getDraft(projectId: number): Promise<SaveDraftRequest | null> {
    return api.get(`/api/nesma/projects/${projectId}/draft`)
  }

  /**
   * 执行NESMA功能点计算
   */
  async calculate(data: CalculationRequest): Promise<CalculationResult> {
    return api.post('/api/nesma/calculate', data)
  }

  /**
   * 获取计算历史
   */
  async getCalculationHistory(projectId: number): Promise<CalculationResult[]> {
    return api.get(`/api/nesma/projects/${projectId}/history`)
  }

  /**
   * 获取计算结果
   */
  async getCalculationResult(projectId: number): Promise<CalculationResult> {
    return api.get(`/api/nesma/projects/${projectId}/result`)
  }

  /**
   * 删除计算结果
   */
  async deleteCalculationResult(projectId: number): Promise<void> {
    return api.delete(`/api/nesma/projects/${projectId}/result`)
  }

  /**
   * 验证功能点数据
   */
  async validateFunctionPointData(data: FunctionPointData): Promise<{ isValid: boolean; errors: string[] }> {
    return api.post('/api/nesma/validate/function-points', data)
  }

  /**
   * 验证VAF数据
   */
  async validateVafData(data: VafData): Promise<{ isValid: boolean; errors: string[] }> {
    return api.post('/api/nesma/validate/vaf', data)
  }

  /**
   * 获取NESMA配置参数
   */
  async getNesmaConfig(): Promise<Record<string, unknown>> {
    return api.get('/api/nesma/config')
  }

  /**
   * 获取复用度建议
   */
  async getReuseLevelRecommendation(projectId: number): Promise<{ recommendedLevel: string; reason: string; confidence: number }> {
    return api.get(`/api/nesma/projects/${projectId}/reuse-recommendation`)
  }

  /**
   * 导出计算报告
   */
  async exportCalculationReport(projectId: number, format = 'pdf'): Promise<Blob> {
    const response = await api.get(`/api/nesma/projects/${projectId}/export`, {
      params: { format },
      responseType: 'blob'
    })
    return response.data
  }

  /**
   * 比较计算结果
   */
  async compareResults(projectIds: number[]): Promise<{ comparison: CalculationResult[]; analysis: Record<string, unknown> }> {
    return api.post('/api/nesma/compare', { projectIds })
  }

  /**
   * 获取行业基准数据
   */
  async getBenchmarkData(category?: string): Promise<Record<string, unknown>> {
    return api.get('/api/nesma/benchmark', { params: { category } })
  }
}

export const nesmaApi = new NesmaApiService()
export default nesmaApi