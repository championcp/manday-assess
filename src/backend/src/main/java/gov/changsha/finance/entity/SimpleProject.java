package gov.changsha.finance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 简化的项目实体类
 * 匹配实际数据库表结构
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Entity
@Table(name = "projects")
public class SimpleProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_code")
    private String projectCode;

    @Column(name = "project_name")
    private String name;

    @Column(name = "project_description")
    private String description;

    @Column(name = "client_name")
    private String clientName;

    @Column(name = "project_type")
    private String projectType;

    @Column(name = "project_status")
    private String status;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "start_date")
    private java.sql.Date startDate;

    @Column(name = "end_date")
    private java.sql.Date endDate;

    @Column(name = "budget_amount")
    private BigDecimal budgetAmount;

    @Column(name = "technology_platform")
    private String technologyPlatform;

    @Column(name = "business_domain")
    private String businessDomain;

    @Column(name = "complexity_level")
    private String complexityLevel;

    @Column(name = "total_function_points")
    private BigDecimal totalFunctionPoints;

    @Column(name = "total_development_hours")
    private BigDecimal totalDevelopmentHours;

    @Column(name = "total_cost_estimate")
    private BigDecimal totalCostEstimate;

    @Column(name = "current_version")
    private Integer currentVersion;

    // Constructors
    public SimpleProject() {}

    // Getters and setters
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
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

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public java.sql.Date getStartDate() {
        return startDate;
    }

    public void setStartDate(java.sql.Date startDate) {
        this.startDate = startDate;
    }

    public java.sql.Date getEndDate() {
        return endDate;
    }

    public void setEndDate(java.sql.Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public String getTechnologyPlatform() {
        return technologyPlatform;
    }

    public void setTechnologyPlatform(String technologyPlatform) {
        this.technologyPlatform = technologyPlatform;
    }

    public String getBusinessDomain() {
        return businessDomain;
    }

    public void setBusinessDomain(String businessDomain) {
        this.businessDomain = businessDomain;
    }

    public String getComplexityLevel() {
        return complexityLevel;
    }

    public void setComplexityLevel(String complexityLevel) {
        this.complexityLevel = complexityLevel;
    }

    public BigDecimal getTotalFunctionPoints() {
        return totalFunctionPoints;
    }

    public void setTotalFunctionPoints(BigDecimal totalFunctionPoints) {
        this.totalFunctionPoints = totalFunctionPoints;
    }

    public BigDecimal getTotalDevelopmentHours() {
        return totalDevelopmentHours;
    }

    public void setTotalDevelopmentHours(BigDecimal totalDevelopmentHours) {
        this.totalDevelopmentHours = totalDevelopmentHours;
    }

    public BigDecimal getTotalCostEstimate() {
        return totalCostEstimate;
    }

    public void setTotalCostEstimate(BigDecimal totalCostEstimate) {
        this.totalCostEstimate = totalCostEstimate;
    }

    public Integer getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Integer currentVersion) {
        this.currentVersion = currentVersion;
    }
}