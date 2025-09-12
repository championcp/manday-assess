package gov.changsha.finance.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.time.LocalDate;

/**
 * 项目团队成员实体类
 * 记录项目团队成员信息和角色
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Entity
@Table(name = "project_team_members", indexes = {
    @Index(name = "idx_project_team_members_project_id", columnList = "project_id"),
    @Index(name = "idx_project_team_members_user_id", columnList = "user_id"),
    @Index(name = "idx_project_team_members_role", columnList = "member_role")
})
public class ProjectTeamMember extends BaseEntity {

    /**
     * 项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 成员角色：PROJECT_MANAGER-项目经理, BUSINESS_ANALYST-业务分析师, DEVELOPER-开发人员, 
     * TESTER-测试人员, REVIEWER-评审员, STAKEHOLDER-相关方
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "member_role", nullable = false, length = 30)
    private MemberRole memberRole;

    /**
     * 职责描述
     */
    @Column(name = "responsibilities", columnDefinition = "TEXT")
    private String responsibilities;

    /**
     * 加入时间
     */
    @Column(name = "joined_date", nullable = false)
    private LocalDate joinedDate;

    /**
     * 离开时间
     */
    @Column(name = "left_date")
    private LocalDate leftDate;

    /**
     * 成员状态：ACTIVE-活跃, INACTIVE-非活跃, LEFT-已离开
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    /**
     * 备注信息
     */
    @Column(name = "remarks", length = 500)
    private String remarks;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 关联项目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Project project;

    /**
     * 关联用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false)
    private User user;

    /**
     * 成员角色枚举
     */
    public enum MemberRole {
        /** 项目经理 */
        PROJECT_MANAGER,
        /** 业务分析师 */
        BUSINESS_ANALYST,
        /** 开发人员 */
        DEVELOPER,
        /** 测试人员 */
        TESTER,
        /** 评审员 */
        REVIEWER,
        /** 相关方 */
        STAKEHOLDER,
        /** 技术架构师 */
        ARCHITECT,
        /** 产品经理 */
        PRODUCT_MANAGER
    }

    /**
     * 成员状态枚举
     */
    public enum MemberStatus {
        /** 活跃状态 */
        ACTIVE,
        /** 非活跃状态 */
        INACTIVE,
        /** 已离开 */
        LEFT
    }

    // 构造函数
    public ProjectTeamMember() {
        this.joinedDate = LocalDate.now();
    }

    public ProjectTeamMember(Long projectId, Long userId, MemberRole memberRole) {
        this.projectId = projectId;
        this.userId = userId;
        this.memberRole = memberRole;
        this.joinedDate = LocalDate.now();
    }

    public ProjectTeamMember(Long projectId, Long userId, MemberRole memberRole, String responsibilities) {
        this.projectId = projectId;
        this.userId = userId;
        this.memberRole = memberRole;
        this.responsibilities = responsibilities;
        this.joinedDate = LocalDate.now();
    }

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public MemberRole getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(MemberRole memberRole) {
        this.memberRole = memberRole;
    }

    public String getResponsibilities() {
        return responsibilities;
    }

    public void setResponsibilities(String responsibilities) {
        this.responsibilities = responsibilities;
    }

    public LocalDate getJoinedDate() {
        return joinedDate;
    }

    public void setJoinedDate(LocalDate joinedDate) {
        this.joinedDate = joinedDate;
    }

    public LocalDate getLeftDate() {
        return leftDate;
    }

    public void setLeftDate(LocalDate leftDate) {
        this.leftDate = leftDate;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public void setStatus(MemberStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // 业务方法
    
    /**
     * 检查成员是否活跃
     */
    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    /**
     * 检查成员是否已离开
     */
    public boolean hasLeft() {
        return status == MemberStatus.LEFT;
    }

    /**
     * 检查是否为项目经理
     */
    public boolean isProjectManager() {
        return memberRole == MemberRole.PROJECT_MANAGER;
    }

    /**
     * 检查是否为评审员
     */
    public boolean isReviewer() {
        return memberRole == MemberRole.REVIEWER;
    }

    /**
     * 成员离开项目
     */
    public void leave() {
        this.status = MemberStatus.LEFT;
        this.leftDate = LocalDate.now();
    }

    /**
     * 成员重新加入项目
     */
    public void rejoin() {
        this.status = MemberStatus.ACTIVE;
        this.leftDate = null;
    }

    /**
     * 获取成员角色显示名称
     */
    public String getRoleDisplayName() {
        switch (memberRole) {
            case PROJECT_MANAGER: return "项目经理";
            case BUSINESS_ANALYST: return "业务分析师";
            case DEVELOPER: return "开发人员";
            case TESTER: return "测试人员";
            case REVIEWER: return "评审员";
            case STAKEHOLDER: return "相关方";
            case ARCHITECT: return "技术架构师";
            case PRODUCT_MANAGER: return "产品经理";
            default: return memberRole.name();
        }
    }

    /**
     * 获取成员状态显示名称
     */
    public String getStatusDisplayName() {
        switch (status) {
            case ACTIVE: return "活跃";
            case INACTIVE: return "非活跃";
            case LEFT: return "已离开";
            default: return status.name();
        }
    }

    /**
     * 计算参与项目的天数
     */
    public long getParticipationDays() {
        LocalDate endDate = leftDate != null ? leftDate : LocalDate.now();
        return joinedDate.until(endDate).getDays();
    }

    @Override
    public String toString() {
        return "ProjectTeamMember{" +
                "id=" + getId() +
                ", projectId=" + projectId +
                ", userId=" + userId +
                ", memberRole=" + memberRole +
                ", status=" + status +
                ", joinedDate=" + joinedDate +
                ", leftDate=" + leftDate +
                '}';
    }
}