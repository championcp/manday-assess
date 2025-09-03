package gov.changsha.finance.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 计算结果实体类
 * 对应数据库表: calculation_results
 * 
 * @author system
 * @since 1.0.0
 */
@Entity
@Table(name = "calculation_results")
public class CalculationResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 计算结果ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 所属项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 计算类型
     * 可选值: NESMA_CALCULATION(NESMA计算), COST_ESTIMATION(成本估算)
     */
    @Column(name = "calculation_type", nullable = false, length = 50)
    private String calculationType;

    /**
     * 功能点总分值
     */
    @Column(name = "total_function_points", precision = 19, scale = 4)
    private BigDecimal totalFunctionPoints;

    /**
     * 调整后功能点数
     */
    @Column(name = "adjusted_function_points", precision = 19, scale = 4)
    private BigDecimal adjustedFunctionPoints;

    /**
     * 预估人月数
     */
    @Column(name = "estimated_person_months", precision = 19, scale = 4)
    private BigDecimal estimatedPersonMonths;

    /**
     * 预估成本
     */
    @Column(name = "estimated_cost", precision = 19, scale = 4)
    private BigDecimal estimatedCost;

    /**
     * 计算状态
     * 可选值: CALCULATING(计算中), COMPLETED(已完成), FAILED(计算失败)
     */
    @Column(name = "calculation_status", nullable = false, length = 20)
    private String calculationStatus;

    /**
     * 计算开始时间
     */
    @Column(name = "calculation_start_time")
    private LocalDateTime calculationStartTime;

    /**
     * 计算完成时间
     */
    @Column(name = "calculation_end_time")
    private LocalDateTime calculationEndTime;

    /**
     * 计算耗时（毫秒）
     */
    @Column(name = "calculation_duration_ms")
    private Long calculationDurationMs;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    /**
     * 更新人ID
     */
    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    /**
     * 软删除标记
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /**
     * 删除人ID
     */
    @Column(name = "deleted_by")
    private Long deletedBy;

    /**
     * 版本号 - 用于乐观锁
     */
    @Version
    @Column(name = "version")
    private Integer version;

    /**
     * 备注信息
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    // 关联关系

    /**
     * 所属项目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", insertable = false, updatable = false)
    private Project project;

    // 构造方法
    public CalculationResult() {}

    public CalculationResult(Long projectId, String calculationType) {
        this.projectId = projectId;
        this.calculationType = calculationType;
        this.calculationStatus = "CALCULATING";
        this.calculationStartTime = LocalDateTime.now();
        this.totalFunctionPoints = BigDecimal.ZERO;
        this.adjustedFunctionPoints = BigDecimal.ZERO;
        this.estimatedPersonMonths = BigDecimal.ZERO;
        this.estimatedCost = BigDecimal.ZERO;
    }

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getCalculationType() {
        return calculationType;
    }

    public void setCalculationType(String calculationType) {
        this.calculationType = calculationType;
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

    public String getCalculationStatus() {
        return calculationStatus;
    }

    public void setCalculationStatus(String calculationStatus) {
        this.calculationStatus = calculationStatus;
    }

    public LocalDateTime getCalculationStartTime() {
        return calculationStartTime;
    }

    public void setCalculationStartTime(LocalDateTime calculationStartTime) {
        this.calculationStartTime = calculationStartTime;
    }

    public LocalDateTime getCalculationEndTime() {
        return calculationEndTime;
    }

    public void setCalculationEndTime(LocalDateTime calculationEndTime) {
        this.calculationEndTime = calculationEndTime;
        if (calculationStartTime != null && calculationEndTime != null) {
            this.calculationDurationMs = java.time.Duration.between(
                calculationStartTime, calculationEndTime).toMillis();
        }
    }

    public Long getCalculationDurationMs() {
        return calculationDurationMs;
    }

    public void setCalculationDurationMs(Long calculationDurationMs) {
        this.calculationDurationMs = calculationDurationMs;
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(Long deletedBy) {
        this.deletedBy = deletedBy;
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

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    // 业务方法

    /**
     * 完成计算
     */
    public void completeCalculation() {
        this.calculationStatus = "COMPLETED";
        this.calculationEndTime = LocalDateTime.now();
        if (calculationStartTime != null) {
            this.calculationDurationMs = java.time.Duration.between(
                calculationStartTime, calculationEndTime).toMillis();
        }
    }

    /**
     * 标记计算失败
     */
    public void markCalculationFailed() {
        this.calculationStatus = "FAILED";
        this.calculationEndTime = LocalDateTime.now();
        if (calculationStartTime != null) {
            this.calculationDurationMs = java.time.Duration.between(
                calculationStartTime, calculationEndTime).toMillis();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CalculationResult that = (CalculationResult) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CalculationResult{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", calculationType='" + calculationType + '\'' +
                ", totalFunctionPoints=" + totalFunctionPoints +
                ", adjustedFunctionPoints=" + adjustedFunctionPoints +
                ", estimatedPersonMonths=" + estimatedPersonMonths +
                ", estimatedCost=" + estimatedCost +
                ", calculationStatus='" + calculationStatus + '\'' +
                ", calculationDurationMs=" + calculationDurationMs +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}