package gov.changsha.finance.entity;

import javax.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 项目实体类
 * 对应数据库表: projects
 * 
 * @author system
 * @since 1.0.0
 */
@Entity
@Table(name = "projects")
public class Project implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目ID - 主键
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /**
     * 项目编号 - 唯一标识
     */
    @Column(name = "project_code", nullable = false, unique = true, length = 50)
    private String projectCode;

    /**
     * 项目名称
     */
    @Column(name = "name", nullable = false, length = 200)
    private String projectName;

    /**
     * 项目描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String projectDescription;

    /**
     * 项目类型
     * 可选值: INFORMATION_SYSTEM(信息系统), PLATFORM(平台), APPLICATION(应用)
     */
    @Column(name = "project_type", nullable = false, length = 50)
    private String projectType;

    /**
     * 项目状态
     * 可选值: DRAFT(草稿), SUBMITTED(已提交), REVIEWING(评审中), APPROVED(已通过), REJECTED(已拒绝)
     */
    @Column(name = "status", nullable = false, length = 30)
    private String projectStatus = "DRAFT";

    /**
     * 项目优先级
     * 可选值: HIGH(高), MEDIUM(中), LOW(低)
     */
    @Column(name = "priority", length = 20)
    private String priority;

    /**
     * 预算金额
     */
    @Column(name = "budget_amount", precision = 19, scale = 4)
    private BigDecimal budgetAmount;

    /**
     * 预计人月数
     */
    @Column(name = "estimated_person_months", precision = 8, scale = 2)
    private BigDecimal estimatedPersonMonths;

    /**
     * 项目开始日期
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;

    /**
     * 项目结束日期
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;

    /**
     * 部门ID
     */
    @Column(name = "department_id")
    private Long departmentId;

    /**
     * 部门名称
     */
    @Column(name = "department_name", length = 100)
    private String departmentName;

    /**
     * 项目负责人ID
     */
    @Column(name = "project_manager_id")
    private Long projectManagerId;

    /**
     * 项目负责人姓名
     */
    @Column(name = "project_manager_name", length = 50)
    private String projectManagerName;

    /**
     * 联系电话
     */
    @Column(name = "contact_phone", length = 20)
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Column(name = "contact_email", length = 100)
    private String contactEmail;

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
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;


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
     * 项目功能点列表
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<FunctionPoint> functionPoints;

    /**
     * 项目状态历史记录
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectStatusHistory> statusHistories;

    /**
     * 计算结果列表
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CalculationResult> calculationResults;

    /**
     * VAF调整因子列表
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<VafFactor> vafFactors;

    // 构造方法
    public Project() {}

    public Project(String projectCode, String projectName, String projectType) {
        this.projectCode = projectCode;
        this.projectName = projectName;
        this.projectType = projectType;
        this.projectStatus = "DRAFT";
    }

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
    }

    public String getProjectStatus() {
        return projectStatus;
    }

    public void setProjectStatus(String projectStatus) {
        this.projectStatus = projectStatus;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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

    public List<FunctionPoint> getFunctionPoints() {
        return functionPoints;
    }

    public void setFunctionPoints(List<FunctionPoint> functionPoints) {
        this.functionPoints = functionPoints;
    }

    public List<ProjectStatusHistory> getStatusHistories() {
        return statusHistories;
    }

    public void setStatusHistories(List<ProjectStatusHistory> statusHistories) {
        this.statusHistories = statusHistories;
    }

    public List<CalculationResult> getCalculationResults() {
        return calculationResults;
    }

    public void setCalculationResults(List<CalculationResult> calculationResults) {
        this.calculationResults = calculationResults;
    }

    public List<VafFactor> getVafFactors() {
        return vafFactors;
    }

    public void setVafFactors(List<VafFactor> vafFactors) {
        this.vafFactors = vafFactors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Project project = (Project) o;
        return Objects.equals(id, project.id) && 
               Objects.equals(projectCode, project.projectCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, projectCode);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", projectCode='" + projectCode + '\'' +
                ", projectName='" + projectName + '\'' +
                ", projectType='" + projectType + '\'' +
                ", projectStatus='" + projectStatus + '\'' +
                ", priority='" + priority + '\'' +
                ", budgetAmount=" + budgetAmount +
                ", departmentName='" + departmentName + '\'' +
                ", projectManagerName='" + projectManagerName + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}