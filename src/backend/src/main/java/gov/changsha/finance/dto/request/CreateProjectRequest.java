package gov.changsha.finance.dto.request;

import jakarta.validation.constraints.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建项目请求DTO
 * 
 * @author system
 * @since 1.0.0
 */
public class CreateProjectRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 项目编号 - 唯一标识
     */
    @NotBlank(message = "项目编号不能为空")
    @Size(max = 50, message = "项目编号长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "项目编号只能包含大写字母、数字和横线")
    private String projectCode;

    /**
     * 项目名称
     */
    @NotBlank(message = "项目名称不能为空")
    @Size(max = 200, message = "项目名称长度不能超过200个字符")
    private String projectName;

    /**
     * 项目描述
     */
    @Size(max = 2000, message = "项目描述长度不能超过2000个字符")
    private String projectDescription;

    /**
     * 项目类型
     * 可选值: INFORMATION_SYSTEM(信息系统), PLATFORM(平台), APPLICATION(应用)
     */
    @NotBlank(message = "项目类型不能为空")
    @Pattern(regexp = "^(INFORMATION_SYSTEM|PLATFORM|APPLICATION)$", 
             message = "项目类型必须是INFORMATION_SYSTEM、PLATFORM或APPLICATION之一")
    private String projectType;

    /**
     * 项目优先级
     * 可选值: HIGH(高), MEDIUM(中), LOW(低)
     */
    @Pattern(regexp = "^(HIGH|MEDIUM|LOW)$", 
             message = "项目优先级必须是HIGH、MEDIUM或LOW之一")
    private String priority;

    /**
     * 预算金额
     */
    @DecimalMin(value = "0", message = "预算金额不能为负数")
    @DecimalMax(value = "99999999999999.9999", message = "预算金额超出范围")
    @Digits(integer = 15, fraction = 4, message = "预算金额格式不正确")
    private BigDecimal budgetAmount;

    /**
     * 预计人月数
     */
    @DecimalMin(value = "0", message = "预计人月数不能为负数")
    @DecimalMax(value = "999999.99", message = "预计人月数超出范围")
    @Digits(integer = 6, fraction = 2, message = "预计人月数格式不正确")
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
    @NotNull(message = "部门ID不能为空")
    @Positive(message = "部门ID必须为正数")
    private Long departmentId;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 100, message = "部门名称长度不能超过100个字符")
    private String departmentName;

    /**
     * 项目负责人ID
     */
    @NotNull(message = "项目负责人ID不能为空")
    @Positive(message = "项目负责人ID必须为正数")
    private Long projectManagerId;

    /**
     * 项目负责人姓名
     */
    @NotBlank(message = "项目负责人姓名不能为空")
    @Size(max = 50, message = "项目负责人姓名长度不能超过50个字符")
    private String projectManagerName;

    /**
     * 联系电话
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确")
    private String contactPhone;

    /**
     * 联系邮箱
     */
    @Email(message = "联系邮箱格式不正确")
    @Size(max = 100, message = "联系邮箱长度不能超过100个字符")
    private String contactEmail;

    /**
     * 备注信息
     */
    @Size(max = 2000, message = "备注信息长度不能超过2000个字符")
    private String remarks;

    // 构造方法
    public CreateProjectRequest() {}

    // Getter和Setter方法
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

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public String toString() {
        return "CreateProjectRequest{" +
                "projectCode='" + projectCode + '\'' +
                ", projectName='" + projectName + '\'' +
                ", projectType='" + projectType + '\'' +
                ", priority='" + priority + '\'' +
                ", budgetAmount=" + budgetAmount +
                ", estimatedPersonMonths=" + estimatedPersonMonths +
                ", departmentName='" + departmentName + '\'' +
                ", projectManagerName='" + projectManagerName + '\'' +
                ", contactPhone='" + contactPhone + '\'' +
                ", contactEmail='" + contactEmail + '\'' +
                '}';
    }
}