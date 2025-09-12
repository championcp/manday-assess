package gov.changsha.finance.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 功能点实体类
 * 对应数据库表: function_points
 * 
 * @author system
 * @since 1.0.0
 */
@Entity
@Table(name = "function_points")
public class FunctionPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 功能点ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 功能点编号
     */
    @Column(name = "function_point_code", nullable = false, length = 50)
    private String functionPointCode;

    /**
     * 功能点名称
     */
    @Column(name = "fp_name", nullable = false, length = 200)
    private String functionPointName;

    /**
     * 功能点描述
     */
    @Column(name = "fp_description", columnDefinition = "TEXT")
    private String functionPointDescription;

    /**
     * 功能点类型
     * 可选值: ILF(内部逻辑文件), EIF(外部接口文件), EI(外部输入), EO(外部输出), EQ(外部查询)
     */
    @Column(name = "fp_type", nullable = false, length = 10)
    private String functionPointType;

    /**
     * 复杂度等级
     * 可选值: LOW(低), MEDIUM(中), HIGH(高)
     */
    @Column(name = "complexity_level", nullable = false, length = 20)
    private String complexityLevel;

    /**
     * 复杂度权重值
     */
    @Column(name = "complexity_weight", nullable = false, precision = 8, scale = 2)
    private BigDecimal complexityWeight;

    /**
     * 功能点数值
     */
    @Column(name = "function_point_value", nullable = false, precision = 8, scale = 2)
    private BigDecimal functionPointValue;

    /**
     * 数据元素类型数量（DET）
     */
    @Column(name = "det_count", nullable = false)
    private Integer detCount;

    /**
     * 记录元素类型数量（RET）
     */
    @Column(name = "ret_count", nullable = false)
    private Integer retCount;

    /**
     * 文件类型引用数量（FTR）
     */
    @Column(name = "ftr_count", nullable = false)
    private Integer ftrCount;

    /**
     * 功能点状态
     * 可选值: DRAFT(草稿), CONFIRMED(已确认), VALIDATED(已验证)
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * 所属项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

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

    /**
     * ILF详细信息
     */
    @OneToOne(mappedBy = "functionPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private IlfDetail ilfDetail;

    /**
     * EIF详细信息
     */
    @OneToOne(mappedBy = "functionPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EifDetail eifDetail;

    /**
     * EI详细信息
     */
    @OneToOne(mappedBy = "functionPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EiDetail eiDetail;

    /**
     * EO详细信息
     */
    @OneToOne(mappedBy = "functionPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EoDetail eoDetail;

    /**
     * EQ详细信息
     */
    @OneToOne(mappedBy = "functionPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EqDetail eqDetail;

    /**
     * 功能点历史记录
     */
    @OneToMany(mappedBy = "functionPoint", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FunctionPointHistory> historyRecords;

    // 构造方法
    public FunctionPoint() {}

    public FunctionPoint(String functionPointCode, String functionPointName, 
                        String functionPointType, Long projectId) {
        this.functionPointCode = functionPointCode;
        this.functionPointName = functionPointName;
        this.functionPointType = functionPointType;
        this.projectId = projectId;
        this.status = "DRAFT";
        this.detCount = 0;
        this.retCount = 0;
        this.ftrCount = 0;
        this.functionPointValue = BigDecimal.ZERO;
        this.complexityWeight = BigDecimal.ZERO;
    }

    // Getter和Setter方法
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

    public String getFunctionPointDescription() {
        return functionPointDescription;
    }

    public void setFunctionPointDescription(String functionPointDescription) {
        this.functionPointDescription = functionPointDescription;
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

    public BigDecimal getComplexityWeight() {
        return complexityWeight;
    }

    public void setComplexityWeight(BigDecimal complexityWeight) {
        this.complexityWeight = complexityWeight;
    }

    public BigDecimal getFunctionPointValue() {
        return functionPointValue;
    }

    public void setFunctionPointValue(BigDecimal functionPointValue) {
        this.functionPointValue = functionPointValue;
    }

    public Integer getDetCount() {
        return detCount;
    }

    public void setDetCount(Integer detCount) {
        this.detCount = detCount;
    }

    public Integer getRetCount() {
        return retCount;
    }

    public void setRetCount(Integer retCount) {
        this.retCount = retCount;
    }

    public Integer getFtrCount() {
        return ftrCount;
    }

    public void setFtrCount(Integer ftrCount) {
        this.ftrCount = ftrCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public IlfDetail getIlfDetail() {
        return ilfDetail;
    }

    public void setIlfDetail(IlfDetail ilfDetail) {
        this.ilfDetail = ilfDetail;
    }

    public EifDetail getEifDetail() {
        return eifDetail;
    }

    public void setEifDetail(EifDetail eifDetail) {
        this.eifDetail = eifDetail;
    }

    public EiDetail getEiDetail() {
        return eiDetail;
    }

    public void setEiDetail(EiDetail eiDetail) {
        this.eiDetail = eiDetail;
    }

    public EoDetail getEoDetail() {
        return eoDetail;
    }

    public void setEoDetail(EoDetail eoDetail) {
        this.eoDetail = eoDetail;
    }

    public EqDetail getEqDetail() {
        return eqDetail;
    }

    public void setEqDetail(EqDetail eqDetail) {
        this.eqDetail = eqDetail;
    }

    public List<FunctionPointHistory> getHistoryRecords() {
        return historyRecords;
    }

    public void setHistoryRecords(List<FunctionPointHistory> historyRecords) {
        this.historyRecords = historyRecords;
    }

    // 业务方法

    /**
     * 计算功能点数值
     * 根据复杂度等级设置对应的权重和数值
     */
    public void calculateFunctionPointValue() {
        if (functionPointType == null || complexityLevel == null) {
            return;
        }

        BigDecimal weight = getStandardWeight(functionPointType, complexityLevel);
        this.complexityWeight = weight;
        this.functionPointValue = weight;
    }

    /**
     * 获取标准权重值
     * 根据NESMA标准设置权重值
     */
    private BigDecimal getStandardWeight(String type, String complexity) {
        switch (type) {
            case "ILF":
                switch (complexity) {
                    case "LOW": return new BigDecimal("7");
                    case "MEDIUM": return new BigDecimal("10");
                    case "HIGH": return new BigDecimal("15");
                }
                break;
            case "EIF":
                switch (complexity) {
                    case "LOW": return new BigDecimal("5");
                    case "MEDIUM": return new BigDecimal("7");
                    case "HIGH": return new BigDecimal("10");
                }
                break;
            case "EI":
                switch (complexity) {
                    case "LOW": return new BigDecimal("3");
                    case "MEDIUM": return new BigDecimal("4");
                    case "HIGH": return new BigDecimal("6");
                }
                break;
            case "EO":
                switch (complexity) {
                    case "LOW": return new BigDecimal("4");
                    case "MEDIUM": return new BigDecimal("5");
                    case "HIGH": return new BigDecimal("7");
                }
                break;
            case "EQ":
                switch (complexity) {
                    case "LOW": return new BigDecimal("3");
                    case "MEDIUM": return new BigDecimal("4");
                    case "HIGH": return new BigDecimal("6");
                }
                break;
        }
        return BigDecimal.ZERO;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FunctionPoint that = (FunctionPoint) o;
        return Objects.equals(id, that.id) && 
               Objects.equals(functionPointCode, that.functionPointCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, functionPointCode);
    }

    @Override
    public String toString() {
        return "FunctionPoint{" +
                "id=" + id +
                ", functionPointCode='" + functionPointCode + '\'' +
                ", functionPointName='" + functionPointName + '\'' +
                ", functionPointType='" + functionPointType + '\'' +
                ", complexityLevel='" + complexityLevel + '\'' +
                ", functionPointValue=" + functionPointValue +
                ", status='" + status + '\'' +
                ", projectId=" + projectId +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}