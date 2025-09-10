package gov.changsha.finance.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 简化功能点实体类
 * 对应数据库表: function_points
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */
@Entity
@Table(name = "function_points")
public class SimpleFunctionPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false)
    private Long projectId;

    @Column(name = "fp_type", nullable = false, length = 10)
    private String fpType;

    @Column(name = "fp_name", nullable = false, length = 200)
    private String fpName;

    @Column(name = "fp_description", columnDefinition = "TEXT")
    private String fpDescription;

    @Column(name = "business_purpose", columnDefinition = "TEXT")
    private String businessPurpose;

    @Column(name = "complexity_level", nullable = false, length = 20)
    private String complexityLevel;

    @Column(name = "complexity_weight", nullable = false, precision = 19, scale = 4)
    private BigDecimal complexityWeight;

    @Column(name = "adjusted_complexity_weight", precision = 19, scale = 4)
    private BigDecimal adjustedComplexityWeight;

    @Column(name = "function_point_count", nullable = false, precision = 19, scale = 4)
    private BigDecimal functionPointCount = BigDecimal.ONE;

    @Column(name = "calculated_fp_value", nullable = false, precision = 19, scale = 4)
    private BigDecimal calculatedFpValue = BigDecimal.ZERO;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "DRAFT";

    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @Column(name = "parent_fp_id")
    private Long parentFpId;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "is_baseline", nullable = false)
    private Boolean isBaseline = false;

    @Column(name = "baseline_date")
    private LocalDateTime baselineDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 关联的复杂度评估（暂时简化，不建立关系）
    // 注意：这些字段不映射到数据库，只用于内存计算
    @Transient
    private Integer detCount;
    @Transient
    private Integer retCount;
    @Transient
    private Integer ftrCount;

    // 构造方法
    public SimpleFunctionPoint() {}

    public SimpleFunctionPoint(String fpName, String fpType) {
        this.fpName = fpName;
        this.fpType = fpType;
    }

    // Getter和Setter方法
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProjectId() { return projectId; }
    public void setProjectId(Long projectId) { this.projectId = projectId; }

    public String getFpType() { return fpType; }
    public void setFpType(String fpType) { this.fpType = fpType; }

    public String getFpName() { return fpName; }
    public void setFpName(String fpName) { this.fpName = fpName; }

    public String getFpDescription() { return fpDescription; }
    public void setFpDescription(String fpDescription) { this.fpDescription = fpDescription; }

    public String getBusinessPurpose() { return businessPurpose; }
    public void setBusinessPurpose(String businessPurpose) { this.businessPurpose = businessPurpose; }

    public String getComplexityLevel() { return complexityLevel; }
    public void setComplexityLevel(String complexityLevel) { this.complexityLevel = complexityLevel; }

    public BigDecimal getComplexityWeight() { return complexityWeight; }
    public void setComplexityWeight(BigDecimal complexityWeight) { this.complexityWeight = complexityWeight; }

    public BigDecimal getAdjustedComplexityWeight() { return adjustedComplexityWeight; }
    public void setAdjustedComplexityWeight(BigDecimal adjustedComplexityWeight) { this.adjustedComplexityWeight = adjustedComplexityWeight; }

    public BigDecimal getFunctionPointCount() { return functionPointCount; }
    public void setFunctionPointCount(BigDecimal functionPointCount) { this.functionPointCount = functionPointCount; }

    public BigDecimal getCalculatedFpValue() { return calculatedFpValue; }
    public void setCalculatedFpValue(BigDecimal calculatedFpValue) { this.calculatedFpValue = calculatedFpValue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public Long getParentFpId() { return parentFpId; }
    public void setParentFpId(Long parentFpId) { this.parentFpId = parentFpId; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Boolean getIsBaseline() { return isBaseline; }
    public void setIsBaseline(Boolean isBaseline) { this.isBaseline = isBaseline; }

    public LocalDateTime getBaselineDate() { return baselineDate; }
    public void setBaselineDate(LocalDateTime baselineDate) { this.baselineDate = baselineDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Long getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(Long updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }

    // 便利的方法，用于兼容现有的NESMA计算
    public Integer getDetCount() { return detCount; }
    public void setDetCount(Integer detCount) { this.detCount = detCount; }

    public Integer getRetCount() { return retCount; }
    public void setRetCount(Integer retCount) { this.retCount = retCount; }

    public Integer getFtrCount() { return ftrCount; }
    public void setFtrCount(Integer ftrCount) { this.ftrCount = ftrCount; }

    // 兼容方法
    public String getFunctionPointType() { return fpType; }
    public void setFunctionPointType(String type) { this.fpType = type; }

    public String getFunctionPointName() { return fpName; }
    public void setFunctionPointName(String name) { this.fpName = name; }

    public String getFunctionPointDescription() { return fpDescription; }
    public void setFunctionPointDescription(String description) { this.fpDescription = description; }

    public BigDecimal getFunctionPointValue() { return calculatedFpValue; }
    public void setFunctionPointValue(BigDecimal value) { this.calculatedFpValue = value; }

    // 检查是否已删除
    public boolean isDeleted() {
        return deletedAt != null;
    }

    // 软删除
    public void delete(Long deletedBy) {
        this.deletedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = deletedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleFunctionPoint that = (SimpleFunctionPoint) o;
        return Objects.equals(id, that.id) && Objects.equals(fpName, that.fpName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, fpName);
    }

    @Override
    public String toString() {
        return "SimpleFunctionPoint{" +
                "id=" + id +
                ", projectId=" + projectId +
                ", fpType='" + fpType + '\'' +
                ", fpName='" + fpName + '\'' +
                ", complexityLevel='" + complexityLevel + '\'' +
                ", calculatedFpValue=" + calculatedFpValue +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}