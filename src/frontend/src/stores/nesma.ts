import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { 
  FunctionPointData, 
  VafData, 
  CalculationRequest, 
  CalculationResult,
  SaveDraftRequest 
} from '../utils/nesmaApi'
import nesmaApi from '../utils/nesmaApi'

export const useNesmaStore = defineStore('nesma', () => {
  // 状态
  const loading = ref(false)
  const calculating = ref(false)
  const saving = ref(false)
  
  // 计算数据
  const currentProjectId = ref<number | null>(null)
  const currentStep = ref(0)
  
  // 功能点数据
  const functionPointData = ref<FunctionPointData>({
    ilf: [],
    eif: [],
    ei: [],
    eo: [],
    eq: [],
  })
  
  // VAF数据
  const vafData = ref<VafData>({
    TF01: 0, TF02: 0, TF03: 0, TF04: 0, TF05: 0,
    TF06: 0, TF07: 0, TF08: 0, TF09: 0, TF10: 0,
    TF11: 0, TF12: 0, TF13: 0, TF14: 0,
  })
  
  // 复用度
  const selectedReuseLevel = ref<string>('LOW')
  
  // 计算结果
  const calculationResult = ref<CalculationResult | null>(null)
  const calculationHistory = ref<any[]>([])
  
  // 配置数据
  const nesmaConfig = ref<any>(null)

  // 计算属性
  const totalUnadjustedPoints = computed(() => {
    const ilfPoints = functionPointData.value.ilf.reduce((sum, item) => sum + item.points, 0)
    const eifPoints = functionPointData.value.eif.reduce((sum, item) => sum + item.points, 0)
    const eiPoints = functionPointData.value.ei.reduce((sum, item) => sum + item.points, 0)
    const eoPoints = functionPointData.value.eo.reduce((sum, item) => sum + item.points, 0)
    const eqPoints = functionPointData.value.eq.reduce((sum, item) => sum + item.points, 0)
    
    return ilfPoints + eifPoints + eiPoints + eoPoints + eqPoints
  })

  const vafTotalScore = computed(() => {
    return Object.values(vafData.value).reduce((sum, score) => sum + score, 0)
  })

  const vafAdjustmentFactor = computed(() => {
    return Number((0.65 + 0.01 * vafTotalScore.value).toFixed(4))
  })

  const adjustedFunctionPoints = computed(() => {
    return Number((totalUnadjustedPoints.value * vafAdjustmentFactor.value).toFixed(2))
  })

  const finalFunctionPoints = computed(() => {
    if (!calculationResult.value) return adjustedFunctionPoints.value
    return calculationResult.value.finalFunctionPoints
  })

  const estimatedCost = computed(() => {
    if (!calculationResult.value) {
      // 基于默认参数估算
      return Math.round(adjustedFunctionPoints.value / 7.0 * 18000)
    }
    return calculationResult.value.estimatedCost
  })

  const elementsSummary = computed(() => {
    const ilf = functionPointData.value.ilf
    const eif = functionPointData.value.eif
    const ei = functionPointData.value.ei
    const eo = functionPointData.value.eo
    const eq = functionPointData.value.eq

    return [
      {
        type: 'ILF',
        count: ilf.length,
        points: ilf.reduce((sum, item) => sum + item.points, 0),
        percentage: totalUnadjustedPoints.value > 0 
          ? `${((ilf.reduce((sum, item) => sum + item.points, 0) / totalUnadjustedPoints.value) * 100).toFixed(1)}%`
          : '0%'
      },
      {
        type: 'EIF',
        count: eif.length,
        points: eif.reduce((sum, item) => sum + item.points, 0),
        percentage: totalUnadjustedPoints.value > 0 
          ? `${((eif.reduce((sum, item) => sum + item.points, 0) / totalUnadjustedPoints.value) * 100).toFixed(1)}%`
          : '0%'
      },
      {
        type: 'EI',
        count: ei.length,
        points: ei.reduce((sum, item) => sum + item.points, 0),
        percentage: totalUnadjustedPoints.value > 0 
          ? `${((ei.reduce((sum, item) => sum + item.points, 0) / totalUnadjustedPoints.value) * 100).toFixed(1)}%`
          : '0%'
      },
      {
        type: 'EO',
        count: eo.length,
        points: eo.reduce((sum, item) => sum + item.points, 0),
        percentage: totalUnadjustedPoints.value > 0 
          ? `${((eo.reduce((sum, item) => sum + item.points, 0) / totalUnadjustedPoints.value) * 100).toFixed(1)}%`
          : '0%'
      },
      {
        type: 'EQ',
        count: eq.length,
        points: eq.reduce((sum, item) => sum + item.points, 0),
        percentage: totalUnadjustedPoints.value > 0 
          ? `${((eq.reduce((sum, item) => sum + item.points, 0) / totalUnadjustedPoints.value) * 100).toFixed(1)}%`
          : '0%'
      },
    ]
  })

  const isCalculationComplete = computed(() => {
    return calculationResult.value !== null
  })

  const canProceedToNextStep = computed(() => {
    switch (currentStep.value) {
      case 0: // 功能点录入
        return totalUnadjustedPoints.value > 0
      case 1: // VAF调整
        return vafTotalScore.value >= 0
      case 2: // 复用度设置
        return selectedReuseLevel.value !== ''
      case 3: // 结果查看
        return true
      default:
        return false
    }
  })

  // 方法
  const initializeCalculation = async (projectId: number) => {
    currentProjectId.value = projectId
    currentStep.value = 0
    
    // 尝试加载草稿数据
    try {
      const draft = await nesmaApi.getDraft(projectId)
      if (draft) {
        if (draft.functionPointData) {
          Object.assign(functionPointData.value, draft.functionPointData)
        }
        if (draft.vafData) {
          Object.assign(vafData.value, draft.vafData)
        }
        if (draft.reuseLevel) {
          selectedReuseLevel.value = draft.reuseLevel
        }
        currentStep.value = draft.step || 0
      }
    } catch (error) {
      console.warn('未找到草稿数据:', error)
    }
    
    // 加载配置数据
    await loadNesmaConfig()
    
    // 加载计算历史
    await loadCalculationHistory(projectId)
    
    // 尝试加载已有的计算结果
    try {
      const result = await nesmaApi.getCalculationResult(projectId)
      calculationResult.value = result
    } catch (error) {
      console.warn('未找到计算结果:', error)
    }
  }

  const saveDraft = async () => {
    if (!currentProjectId.value) return
    
    saving.value = true
    try {
      const draftData: SaveDraftRequest = {
        projectId: currentProjectId.value,
        functionPointData: functionPointData.value,
        vafData: vafData.value,
        reuseLevel: selectedReuseLevel.value,
        step: currentStep.value,
      }
      
      await nesmaApi.saveDraft(draftData)
    } catch (error) {
      console.error('保存草稿失败:', error)
      throw error
    } finally {
      saving.value = false
    }
  }

  const calculate = async () => {
    if (!currentProjectId.value) return
    
    calculating.value = true
    try {
      const request: CalculationRequest = {
        projectId: currentProjectId.value,
        functionPointData: functionPointData.value,
        vafData: vafData.value,
        reuseLevel: selectedReuseLevel.value,
      }
      
      const result = await nesmaApi.calculate(request)
      calculationResult.value = result
      
      // 刷新计算历史
      await loadCalculationHistory(currentProjectId.value)
      
      return result
    } catch (error) {
      console.error('计算失败:', error)
      throw error
    } finally {
      calculating.value = false
    }
  }

  const loadCalculationHistory = async (projectId: number) => {
    try {
      const history = await nesmaApi.getCalculationHistory(projectId)
      calculationHistory.value = history
    } catch (error) {
      console.error('加载计算历史失败:', error)
    }
  }

  const loadNesmaConfig = async () => {
    try {
      const config = await nesmaApi.getNesmaConfig()
      nesmaConfig.value = config
    } catch (error) {
      console.error('加载NESMA配置失败:', error)
    }
  }

  const validateFunctionPointData = async () => {
    try {
      return await nesmaApi.validateFunctionPointData(functionPointData.value)
    } catch (error) {
      console.error('验证功能点数据失败:', error)
      throw error
    }
  }

  const validateVafData = async () => {
    try {
      return await nesmaApi.validateVafData(vafData.value)
    } catch (error) {
      console.error('验证VAF数据失败:', error)
      throw error
    }
  }

  const getReuseLevelRecommendation = async (projectId: number) => {
    try {
      return await nesmaApi.getReuseLevelRecommendation(projectId)
    } catch (error) {
      console.error('获取复用度建议失败:', error)
      throw error
    }
  }

  const exportReport = async (projectId: number, format = 'pdf') => {
    try {
      return await nesmaApi.exportCalculationReport(projectId, format)
    } catch (error) {
      console.error('导出报告失败:', error)
      throw error
    }
  }

  // 步骤控制
  const nextStep = () => {
    if (currentStep.value < 3 && canProceedToNextStep.value) {
      currentStep.value++
    }
  }

  const prevStep = () => {
    if (currentStep.value > 0) {
      currentStep.value--
    }
  }

  const goToStep = (step: number) => {
    if (step >= 0 && step <= 3) {
      currentStep.value = step
    }
  }

  // 数据管理
  const addFunctionPointElement = (type: keyof FunctionPointData, element: any) => {
    functionPointData.value[type].push(element)
  }

  const removeFunctionPointElement = (type: keyof FunctionPointData, index: number) => {
    functionPointData.value[type].splice(index, 1)
  }

  const updateFunctionPointElement = (type: keyof FunctionPointData, index: number, element: any) => {
    functionPointData.value[type][index] = element
  }

  const updateVafFactor = (factor: keyof VafData, score: number) => {
    vafData.value[factor] = score
  }

  const setReuseLevel = (level: string) => {
    selectedReuseLevel.value = level
  }

  // 清理方法
  const resetCalculation = () => {
    currentProjectId.value = null
    currentStep.value = 0
    
    functionPointData.value = {
      ilf: [],
      eif: [],
      ei: [],
      eo: [],
      eq: [],
    }
    
    vafData.value = {
      TF01: 0, TF02: 0, TF03: 0, TF04: 0, TF05: 0,
      TF06: 0, TF07: 0, TF08: 0, TF09: 0, TF10: 0,
      TF11: 0, TF12: 0, TF13: 0, TF14: 0,
    }
    
    selectedReuseLevel.value = 'LOW'
    calculationResult.value = null
    calculationHistory.value = []
  }

  const clearResult = () => {
    calculationResult.value = null
  }

  return {
    // 状态
    loading,
    calculating,
    saving,
    currentProjectId,
    currentStep,
    functionPointData,
    vafData,
    selectedReuseLevel,
    calculationResult,
    calculationHistory,
    nesmaConfig,
    
    // 计算属性
    totalUnadjustedPoints,
    vafTotalScore,
    vafAdjustmentFactor,
    adjustedFunctionPoints,
    finalFunctionPoints,
    estimatedCost,
    elementsSummary,
    isCalculationComplete,
    canProceedToNextStep,
    
    // 方法
    initializeCalculation,
    saveDraft,
    calculate,
    loadCalculationHistory,
    loadNesmaConfig,
    validateFunctionPointData,
    validateVafData,
    getReuseLevelRecommendation,
    exportReport,
    nextStep,
    prevStep,
    goToStep,
    addFunctionPointElement,
    removeFunctionPointElement,
    updateFunctionPointElement,
    updateVafFactor,
    setReuseLevel,
    resetCalculation,
    clearResult,
  }
})