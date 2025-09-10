package gov.changsha.finance.security.jwt;

import com.fasterxml.jackson.annotation.JsonIgnore;
import gov.changsha.finance.entity.Permission;
import gov.changsha.finance.entity.Role;
import gov.changsha.finance.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Security UserDetails实现类
 * 封装用户认证和授权信息
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
public class UserPrincipal implements UserDetails {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private String realName;
    private String email;
    private String employeeId;
    private String department;
    private String position;
    
    @JsonIgnore
    private String password;
    
    private Collection<? extends GrantedAuthority> authorities;
    
    private boolean enabled;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    
    private LocalDateTime lastLoginAt;
    
    public UserPrincipal(Long id, String username, String realName, String email, 
                        String employeeId, String department, String position,
                        String password, Collection<? extends GrantedAuthority> authorities,
                        boolean enabled, boolean accountNonLocked, 
                        boolean accountNonExpired, boolean credentialsNonExpired,
                        LocalDateTime lastLoginAt) {
        this.id = id;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.employeeId = employeeId;
        this.department = department;
        this.position = position;
        this.password = password;
        this.authorities = authorities;
        this.enabled = enabled;
        this.accountNonLocked = accountNonLocked;
        this.accountNonExpired = accountNonExpired;
        this.credentialsNonExpired = credentialsNonExpired;
        this.lastLoginAt = lastLoginAt;
    }
    
    /**
     * 从User实体创建UserPrincipal
     */
    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = mapRolesToAuthorities(user.getRoles());
        
        // 检查账户状态
        boolean enabled = user.isEnabled();
        boolean accountNonLocked = user.isAccountNonLocked();
        boolean accountNonExpired = true; // 可根据业务需要实现
        boolean credentialsNonExpired = isPasswordNonExpired(user.getPasswordExpiresAt());
        
        return new UserPrincipal(
            user.getId(),
            user.getUsername(),
            user.getRealName(),
            user.getEmail(),
            user.getEmployeeId(),
            user.getDepartment(),
            user.getPosition(),
            user.getPasswordHash(),
            authorities,
            enabled,
            accountNonLocked,
            accountNonExpired,
            credentialsNonExpired,
            user.getLastLoginAt()
        );
    }
    
    /**
     * 将角色和权限映射为Spring Security权限
     */
    private static Collection<GrantedAuthority> mapRolesToAuthorities(Set<Role> roles) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        for (Role role : roles) {
            // 添加角色权限 (ROLE_前缀)
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
            
            // 添加具体权限
            Set<Permission> permissions = role.getPermissions();
            if (permissions != null) {
                for (Permission permission : permissions) {
                    authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                }
            }
        }
        
        return authorities;
    }
    
    /**
     * 检查密码是否过期
     */
    private static boolean isPasswordNonExpired(LocalDateTime passwordExpiresAt) {
        if (passwordExpiresAt == null) {
            return true; // 未设置过期时间，认为未过期
        }
        return LocalDateTime.now().isBefore(passwordExpiresAt);
    }
    
    // UserDetails接口实现
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    
    @Override
    public String getPassword() {
        return password;
    }
    
    @Override
    public String getUsername() {
        return username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    // Getters
    
    public Long getId() {
        return id;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public String getPosition() {
        return position;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    /**
     * 检查是否有指定权限
     */
    public boolean hasAuthority(String authority) {
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(authority));
    }
    
    /**
     * 检查是否有指定角色
     */
    public boolean hasRole(String role) {
        String roleWithPrefix = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(roleWithPrefix));
    }
    
    /**
     * 获取所有角色代码（不含ROLE_前缀）
     */
    public List<String> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> auth.startsWith("ROLE_"))
                .map(auth -> auth.substring(5)) // 移除ROLE_前缀
                .collect(Collectors.toList());
    }
    
    /**
     * 获取所有权限代码
     */
    public List<String> getPermissions() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(auth -> !auth.startsWith("ROLE_"))
                .collect(Collectors.toList());
    }
    
    // equals & hashCode
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}