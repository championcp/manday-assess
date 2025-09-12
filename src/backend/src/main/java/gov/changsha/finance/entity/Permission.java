package gov.changsha.finance.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 权限实体类
 * 定义系统中的各种操作权限
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Entity
@Table(name = "permissions", indexes = {
    @Index(name = "idx_permission_code", columnList = "code"),
    @Index(name = "idx_permission_module", columnList = "module"),
    @Index(name = "idx_permission_parent_id", columnList = "parent_id")
})
public class Permission extends BaseEntity {

    /**
     * 权限代码（唯一）
     */
    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    /**
     * 权限名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 权限描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 权限类型：MENU-菜单, BUTTON-按钮, API-接口
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, length = 20)
    private PermissionType permissionType;

    /**
     * 所属模块
     */
    @Column(name = "module", nullable = false, length = 50)
    private String module;

    /**
     * 资源路径（URL或路由）
     */
    @Column(name = "resource_path", length = 200)
    private String resourcePath;

    /**
     * HTTP方法（GET, POST, PUT, DELETE等）
     */
    @Column(name = "http_method", length = 20)
    private String httpMethod;

    /**
     * 父权限ID（用于权限树结构）
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 权限层级
     */
    @Column(name = "level", nullable = false, columnDefinition = "integer default 1")
    private Integer level = 1;

    /**
     * 排序权重
     */
    @Column(name = "sort_order", nullable = false, columnDefinition = "integer default 0")
    private Integer sortOrder = 0;

    /**
     * 权限状态：ACTIVE-活跃, INACTIVE-非活跃
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PermissionStatus status = PermissionStatus.ACTIVE;

    /**
     * 图标样式类
     */
    @Column(name = "icon", length = 100)
    private String icon;

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
     * 权限角色关联（多对多）
     */
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();

    /**
     * 父权限
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Permission parent;

    /**
     * 子权限集合
     */
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Permission> children = new HashSet<>();

    /**
     * 权限类型枚举
     */
    public enum PermissionType {
        /** 菜单权限 */
        MENU,
        /** 按钮权限 */
        BUTTON,
        /** API接口权限 */
        API,
        /** 数据权限 */
        DATA
    }

    /**
     * 权限状态枚举
     */
    public enum PermissionStatus {
        /** 活跃状态 */
        ACTIVE,
        /** 非活跃状态 */
        INACTIVE
    }

    // 构造函数
    public Permission() {}

    public Permission(String code, String name, PermissionType permissionType, String module) {
        this.code = code;
        this.name = name;
        this.permissionType = permissionType;
        this.module = module;
    }

    public Permission(String code, String name, String description, PermissionType permissionType, String module) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.permissionType = permissionType;
        this.module = module;
    }

    // Getters and Setters
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public PermissionType getPermissionType() {
        return permissionType;
    }

    public void setPermissionType(PermissionType permissionType) {
        this.permissionType = permissionType;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public PermissionStatus getStatus() {
        return status;
    }

    public void setStatus(PermissionStatus status) {
        this.status = status;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Permission getParent() {
        return parent;
    }

    public void setParent(Permission parent) {
        this.parent = parent;
    }

    public Set<Permission> getChildren() {
        return children;
    }

    public void setChildren(Set<Permission> children) {
        this.children = children;
    }

    // 业务方法
    
    /**
     * 检查权限是否可用
     */
    public boolean isActive() {
        return status == PermissionStatus.ACTIVE;
    }

    /**
     * 检查是否为根权限
     */
    public boolean isRoot() {
        return parentId == null;
    }

    /**
     * 检查是否为叶子权限
     */
    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    /**
     * 检查是否为菜单权限
     */
    public boolean isMenu() {
        return permissionType == PermissionType.MENU;
    }

    /**
     * 检查是否为按钮权限
     */
    public boolean isButton() {
        return permissionType == PermissionType.BUTTON;
    }

    /**
     * 检查是否为API权限
     */
    public boolean isApi() {
        return permissionType == PermissionType.API;
    }

    /**
     * 添加子权限
     */
    public void addChild(Permission child) {
        child.setParent(this);
        child.setParentId(this.getId());
        child.setLevel(this.level + 1);
        this.children.add(child);
    }

    /**
     * 移除子权限
     */
    public void removeChild(Permission child) {
        child.setParent(null);
        child.setParentId(null);
        this.children.remove(child);
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Permission that = (Permission) o;
        return Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Permission{" +
                "id=" + getId() +
                ", code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", permissionType=" + permissionType +
                ", module='" + module + '\'' +
                ", resourcePath='" + resourcePath + '\'' +
                ", httpMethod='" + httpMethod + '\'' +
                ", parentId=" + parentId +
                ", level=" + level +
                ", status=" + status +
                '}';
    }
}