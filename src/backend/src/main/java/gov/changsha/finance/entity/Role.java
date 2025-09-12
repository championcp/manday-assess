package gov.changsha.finance.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * 角色实体类
 * 定义系统中的各种角色和权限组合
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "name"),
    @Index(name = "idx_role_code", columnList = "code")
})
public class Role extends BaseEntity {

    /**
     * 角色名称（唯一）
     */
    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 角色代码（唯一）
     */
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 角色描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 角色类型：SYSTEM-系统角色, BUSINESS-业务角色, CUSTOM-自定义角色
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "role_type", nullable = false, length = 20)
    private RoleType roleType = RoleType.BUSINESS;

    /**
     * 角色状态：ACTIVE-活跃, INACTIVE-非活跃
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoleStatus status = RoleStatus.ACTIVE;

    /**
     * 排序权重
     */
    @Column(name = "sort_order", nullable = false, columnDefinition = "integer default 0")
    private Integer sortOrder = 0;

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
     * 角色用户关联（多对多）
     */
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();

    /**
     * 角色权限关联（多对多）
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id"),
        indexes = {
            @Index(name = "idx_role_permissions_role_id", columnList = "role_id"),
            @Index(name = "idx_role_permissions_permission_id", columnList = "permission_id")
        }
    )
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 角色类型枚举
     */
    public enum RoleType {
        /** 系统角色，不可删除 */
        SYSTEM,
        /** 业务角色，可以修改 */
        BUSINESS,
        /** 自定义角色，用户创建 */
        CUSTOM
    }

    /**
     * 角色状态枚举
     */
    public enum RoleStatus {
        /** 活跃状态 */
        ACTIVE,
        /** 非活跃状态 */
        INACTIVE
    }

    // 构造函数
    public Role() {}

    public Role(String name, String code, String description) {
        this.name = name;
        this.code = code;
        this.description = description;
    }

    public Role(String name, String code, String description, RoleType roleType) {
        this.name = name;
        this.code = code;
        this.description = description;
        this.roleType = roleType;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public RoleStatus getStatus() {
        return status;
    }

    public void setStatus(RoleStatus status) {
        this.status = status;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
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

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    // 业务方法
    
    /**
     * 添加权限
     */
    public void addPermission(Permission permission) {
        this.permissions.add(permission);
        permission.getRoles().add(this);
    }

    /**
     * 移除权限
     */
    public void removePermission(Permission permission) {
        this.permissions.remove(permission);
        permission.getRoles().remove(this);
    }

    /**
     * 检查是否有指定权限
     */
    public boolean hasPermission(String permissionCode) {
        return permissions.stream().anyMatch(permission -> permission.getCode().equals(permissionCode));
    }

    /**
     * 检查角色是否可用
     */
    public boolean isActive() {
        return status == RoleStatus.ACTIVE;
    }

    /**
     * 检查是否为系统角色
     */
    public boolean isSystemRole() {
        return roleType == RoleType.SYSTEM;
    }

    /**
     * 获取所有权限代码
     */
    public Set<String> getPermissionCodes() {
        Set<String> codes = new HashSet<>();
        for (Permission permission : permissions) {
            codes.add(permission.getCode());
        }
        return codes;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Role role = (Role) o;
        return Objects.equals(code, role.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + getId() +
                ", name='" + name + '\'' +
                ", code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", roleType=" + roleType +
                ", status=" + status +
                ", sortOrder=" + sortOrder +
                '}';
    }
}