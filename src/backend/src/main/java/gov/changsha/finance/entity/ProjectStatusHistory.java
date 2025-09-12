package gov.changsha.finance.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;

/**
 * 项目状态历史实体类
 * 记录项目状态变更的历史轨迹
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Entity
@Table(name = "project_status_history", indexes = {
    @Index(name = "idx_project_status_history_project_id", columnList = "project_id"),
    @Index(name = "idx_project_status_history_created_at", columnList = "created_at")
})
public class ProjectStatusHistory extends BaseEntity {

    /**
     * 项目ID
     */
    @Column(name = "project_id", nullable = false)
    private Long projectId;

    /**
     * 原状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 20)
    private ProjectStatus fromStatus;

    /**
     * 新状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false, length = 20)
    private ProjectStatus toStatus;

    /**
     * 状态变更说明
     */
    @Column(name = "change_reason", columnDefinition = "TEXT")
    private String changeReason;

    /**
     * 操作备注
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;

    /**
     * 操作人
     */
    @CreatedBy
    @Column(name = "changed_by", updatable = false, length = 50)
    private String changedBy;

    /**
     * 关联项目
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Project project;

    /**
     * 操作人用户
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", referencedColumnName = "username", insertable = false, updatable = false)
    private User changedByUser;

    // 构造函数
    public ProjectStatusHistory() {}

    public ProjectStatusHistory(Long projectId, ProjectStatus fromStatus, 
                               ProjectStatus toStatus, String changeReason) {
        this.projectId = projectId;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.changeReason = changeReason;
    }

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public ProjectStatus getFromStatus() {
        return fromStatus;
    }

    public void setFromStatus(ProjectStatus fromStatus) {
        this.fromStatus = fromStatus;
    }

    public ProjectStatus getToStatus() {
        return toStatus;
    }

    public void setToStatus(ProjectStatus toStatus) {
        this.toStatus = toStatus;
    }

    public String getChangeReason() {
        return changeReason;
    }

    public void setChangeReason(String changeReason) {
        this.changeReason = changeReason;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public void setChangedBy(String changedBy) {
        this.changedBy = changedBy;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public User getChangedByUser() {
        return changedByUser;
    }

    public void setChangedByUser(User changedByUser) {
        this.changedByUser = changedByUser;
    }

    // 业务方法
    
    /**
     * 检查是否为状态创建记录
     */
    public boolean isCreation() {
        return fromStatus == null;
    }

    /**
     * 检查是否为状态变更记录
     */
    public boolean isStatusChange() {
        return fromStatus != null && !fromStatus.equals(toStatus);
    }

    /**
     * 获取状态变更描述
     */
    public String getStatusChangeDescription() {
        if (isCreation()) {
            return "创建项目，初始状态为：" + getStatusDisplayName(toStatus);
        } else if (isStatusChange()) {
            return "状态从 " + getStatusDisplayName(fromStatus) + " 变更为 " + getStatusDisplayName(toStatus);
        } else {
            return "状态更新：" + getStatusDisplayName(toStatus);
        }
    }

    /**
     * 获取状态显示名称
     */
    private String getStatusDisplayName(ProjectStatus status) {
        if (status == null) return "未知";
        switch (status) {
            case DRAFT: return "草稿";
            case SUBMITTED: return "已提交";
            case UNDER_REVIEW: return "评审中";
            case APPROVED: return "已批准";
            case REJECTED: return "被驳回";
            case COMPLETED: return "已完成";
            default: return status.name();
        }
    }

    @Override
    public String toString() {
        return "ProjectStatusHistory{" +
                "id=" + getId() +
                ", projectId=" + projectId +
                ", fromStatus=" + fromStatus +
                ", toStatus=" + toStatus +
                ", changeReason='" + changeReason + '\'' +
                ", changedBy='" + changedBy + '\'' +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}