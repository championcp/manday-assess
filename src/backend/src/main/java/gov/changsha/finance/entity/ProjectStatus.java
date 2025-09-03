package gov.changsha.finance.entity;

/**
 * 项目状态枚举
 * 
 * @author system
 * @since 1.0.0
 */
public enum ProjectStatus {
    /**
     * 草稿
     */
    DRAFT("草稿"),
    
    /**
     * 已提交
     */
    SUBMITTED("已提交"),
    
    /**
     * 评审中
     */
    UNDER_REVIEW("评审中"),
    
    /**
     * 已通过
     */
    APPROVED("已批准"),
    
    /**
     * 已拒绝
     */
    REJECTED("被驳回"),
    
    /**
     * 已完成
     */
    COMPLETED("已完成");
    
    private final String displayName;
    
    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}