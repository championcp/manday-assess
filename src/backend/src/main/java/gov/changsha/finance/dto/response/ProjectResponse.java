package gov.changsha.finance.dto.response;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目响应DTO
 * 
 * @author system
 * @since 1.0.0
 */
public class ProjectResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID
     */
    private Long id;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String projectDescription;

    /**
     * 项目类型
     */
    private String projectType;

    /**
     * 项目类型显示名称
     */
    private String projectTypeDisplay;

    /**
     * 项目状态
     */
    private String projectStatus;

    /**
     * 项目状态显示名称
     */
    private String projectStatusDisplay;

    /**
     * 项目优先级
     */
    private String priority;

    /**
     * 项目优先级显示名称
     */
    private String priorityDisplay;

    /**
     * 预算金额
     */
    private BigDecimal budgetAmount;

    /**
     * 预计人月数
     */
    private BigDecimal estimatedPersonMonths;

    /**
     * 项目开始日期
     */
    private LocalDateTime startDate;

    /**
     * 项目结束日期
     */
    private LocalDateTime endDate;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 项目负责人ID
     */
    private Long projectManagerId;

    /**
     * 项目负责人姓名
     */
    private String projectManagerName;

    /**
     * 联系电话
     */
    private String contactPhone;

    /**
     * 联系邮箱
     */
    private String contactEmail;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 备注信息
     */
    private String remarks;

    // 统计信息

    /**
     * 功能点总数
     */
    private Integer totalFunctionPoints;

    /**
     * ILF功能点数量
     */
    private Integer ilfCount;

    /**
     * EIF功能点数量
     */
    private Integer eifCount;

    /**
     * EI功能点数量
     */
    private Integer eiCount;

    /**
     * EO功能点数量
     */
    private Integer eoCount;

    /**
     * EQ功能点数量
     */
    private Integer eqCount;

    /**
     * 功能点总分值
     */
    private BigDecimal totalFunctionPointValue;

    /**
     * 计算结果数量
     */
    private Integer calculationResultCount;

    /**
     * 最新计算时间
     */
    private LocalDateTime lastCalculationTime;

    // 关联数据

    /**
     * 功能点列表（简要信息）
     */
    private List<FunctionPointSummary> functionPoints;

    /**
     * 最新计算结果
     */
    private CalculationResultSummary latestCalculationResult;

    // 构造方法
    public ProjectResponse() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
        this.projectTypeDisplay = getTypeDisplay(projectType);
    }

    public String getProjectTypeDisplay() {
        return projectTypeDisplay;
    }

    public void setProjectTypeDisplay(String projectTypeDisplay) {
        this.projectTypeDisplay = projectTypeDisplay;
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
        this.projectStatusDisplay = getStatusDisplay(projectStatus);
    }

    public String getProjectStatusDisplay() {
        return projectStatusDisplay;
    }

    public void setProjectStatusDisplay(String projectStatusDisplay) {
        this.projectStatusDisplay = projectStatusDisplay;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
        this.priorityDisplay = getPriorityDisplay(priority);
    }

    public String getPriorityDisplay() {
        return priorityDisplay;
    }

    public void setPriorityDisplay(String priorityDisplay) {
        this.priorityDisplay = priorityDisplay;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public BigDecimal getEstimatedPersonMonths() {
        return estimatedPersonMonths;
    }

    public void setEstimatedPersonMonths(BigDecimal estimatedPersonMonths) {
        this.estimatedPersonMonths = estimatedPersonMonths;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public Long getProjectManagerId() {
        return projectManagerId;
    }

    public void setProjectManagerId(Long projectManagerId) {
        this.projectManagerId = projectManagerId;
    }

    public String getProjectManagerName() {
        return projectManagerName;
    }

    public void setProjectManagerName(String projectManagerName) {
        this.projectManagerName = projectManagerName;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public Integer getTotalFunctionPoints() {
        return totalFunctionPoints;
    }

    public void setTotalFunctionPoints(Integer totalFunctionPoints) {
        this.totalFunctionPoints = totalFunctionPoints;
    }

    public Integer getIlfCount() {
        return ilfCount;
    }

    public void setIlfCount(Integer ilfCount) {
        this.ilfCount = ilfCount;
    }

    public Integer getEifCount() {
        return eifCount;
    }

    public void setEifCount(Integer eifCount) {
        this.eifCount = eifCount;
    }

    public Integer getEiCount() {
        return eiCount;
    }

    public void setEiCount(Integer eiCount) {
        this.eiCount = eiCount;
    }

    public Integer getEoCount() {
        return eoCount;
    }

    public void setEoCount(Integer eoCount) {
        this.eoCount = eoCount;
    }

    public Integer getEqCount() {
        return eqCount;
    }

    public void setEqCount(Integer eqCount) {
        this.eqCount = eqCount;
    }

    public BigDecimal getTotalFunctionPointValue() {
        return totalFunctionPointValue;
    }

    public void setTotalFunctionPointValue(BigDecimal totalFunctionPointValue) {
        this.totalFunctionPointValue = totalFunctionPointValue;
    }

    public Integer getCalculationResultCount() {
        return calculationResultCount;
    }

    public void setCalculationResultCount(Integer calculationResultCount) {
        this.calculationResultCount = calculationResultCount;
    }

    public LocalDateTime getLastCalculationTime() {
        return lastCalculationTime;
    }

    public void setLastCalculationTime(LocalDateTime lastCalculationTime) {
        this.lastCalculationTime = lastCalculationTime;
    }

    public List<FunctionPointSummary> getFunctionPoints() {
        return functionPoints;
    }

    public void setFunctionPoints(List<FunctionPointSummary> functionPoints) {
        this.functionPoints = functionPoints;
    }

    public CalculationResultSummary getLatestCalculationResult() {
        return latestCalculationResult;
    }

    public void setLatestCalculationResult(CalculationResultSummary latestCalculationResult) {
        this.latestCalculationResult = latestCalculationResult;
    }

    // 辅助方法

    /**
     * 获取项目类型显示名称
     */
    private String getTypeDisplay(String type) {
        if (type == null) return null;
        switch (type) {
            case "INFORMATION_SYSTEM": return "信息系统";
            case "PLATFORM": return "平台";
            case "APPLICATION": return "应用";
            default: return type;
        }
    }

    /**
     * 获取项目状态显示名称
     */
    private String getStatusDisplay(String status) {
        if (status == null) return null;
        switch (status) {
            case "DRAFT": return "草稿";
            case "SUBMITTED": return "已提交";
            case "REVIEWING": return "评审中";
            case "APPROVED": return "已通过";
            case "REJECTED": return "已拒绝";
            default: return status;
        }
    }

    /**
     * 获取优先级显示名称
     */
    private String getPriorityDisplay(String priority) {
        if (priority == null) return null;
        switch (priority) {
            case "HIGH": return "高";
            case "MEDIUM": return "中";
            case "LOW": return "低";
            default: return priority;
        }
    }

    /**
     * 功能点摘要信息
     */
    public static class FunctionPointSummary implements Serializable {
        private Long id;
        private String functionPointCode;
        private String functionPointName;
        private String functionPointType;
        private String complexityLevel;
        private BigDecimal functionPointValue;
        private String status;

        // 构造方法、Getter和Setter省略...
        public FunctionPointSummary() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getFunctionPointCode() {
            return functionPointCode;
        }

        public void setFunctionPointCode(String functionPointCode) {
            this.functionPointCode = functionPointCode;
        }

        public String getFunctionPointName() {
            return functionPointName;
        }

        public void setFunctionPointName(String functionPointName) {
            this.functionPointName = functionPointName;
        }

        public String getFunctionPointType() {
            return functionPointType;
        }

        public void setFunctionPointType(String functionPointType) {
            this.functionPointType = functionPointType;
        }

        public String getComplexityLevel() {
            return complexityLevel;
        }

        public void setComplexityLevel(String complexityLevel) {
            this.complexityLevel = complexityLevel;
        }

        public BigDecimal getFunctionPointValue() {
            return functionPointValue;
        }

        public void setFunctionPointValue(BigDecimal functionPointValue) {
            this.functionPointValue = functionPointValue;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    /**
     * 计算结果摘要信息
     */
    public static class CalculationResultSummary implements Serializable {
        private Long id;
        private BigDecimal totalFunctionPoints;
        private BigDecimal adjustedFunctionPoints;
        private BigDecimal estimatedPersonMonths;
        private BigDecimal estimatedCost;
        private LocalDateTime calculationTime;

        // 构造方法、Getter和Setter省略...
        public CalculationResultSummary() {}

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public BigDecimal getTotalFunctionPoints() {
            return totalFunctionPoints;
        }

        public void setTotalFunctionPoints(BigDecimal totalFunctionPoints) {
            this.totalFunctionPoints = totalFunctionPoints;
        }

        public BigDecimal getAdjustedFunctionPoints() {
            return adjustedFunctionPoints;
        }

        public void setAdjustedFunctionPoints(BigDecimal adjustedFunctionPoints) {
            this.adjustedFunctionPoints = adjustedFunctionPoints;
        }

        public BigDecimal getEstimatedPersonMonths() {
            return estimatedPersonMonths;
        }

        public void setEstimatedPersonMonths(BigDecimal estimatedPersonMonths) {
            this.estimatedPersonMonths = estimatedPersonMonths;
        }

        public BigDecimal getEstimatedCost() {
            return estimatedCost;
        }

        public void setEstimatedCost(BigDecimal estimatedCost) {
            this.estimatedCost = estimatedCost;
        }

        public LocalDateTime getCalculationTime() {
            return calculationTime;
        }

        public void setCalculationTime(LocalDateTime calculationTime) {
            this.calculationTime = calculationTime;
        }
    }

    @Override
    public String toString() {
        return "ProjectResponse{" +
                "id=" + id +
                ", projectCode='" + projectCode + '\'' +
                ", projectName='" + projectName + '\'' +
                ", projectType='" + projectType + '\'' +
                ", projectStatus='" + projectStatus + '\'' +
                ", priority='" + priority + '\'' +
                ", totalFunctionPoints=" + totalFunctionPoints +
                ", totalFunctionPointValue=" + totalFunctionPointValue +
                ", budgetAmount=" + budgetAmount +
                ", departmentName='" + departmentName + '\'' +
                ", projectManagerName='" + projectManagerName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}