package gov.changsha.finance.dto.response;

import java.time.LocalDateTime;

/**
 * 用户注册响应DTO
 * 返回注册成功后的用户基本信息
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-11
 */
public class RegisterResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 工号
     */
    private String employeeId;
    
    /**
     * 部门名称
     */
    private String department;
    
    /**
     * 职位
     */
    private String position;
    
    /**
     * 账户状态
     */
    private String accountStatus;
    
    /**
     * 注册时间
     */
    private LocalDateTime registeredAt;
    
    /**
     * 注册IP地址
     */
    private String registeredIp;
    
    // 默认构造函数
    public RegisterResponse() {}
    
    // 完整构造函数
    public RegisterResponse(Long userId, String username, String realName, 
                           String email, String employeeId, String department, 
                           String position, String accountStatus, 
                           LocalDateTime registeredAt, String registeredIp) {
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.email = email;
        this.employeeId = employeeId;
        this.department = department;
        this.position = position;
        this.accountStatus = accountStatus;
        this.registeredAt = registeredAt;
        this.registeredIp = registeredIp;
    }
    
    // Getters and Setters
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public void setRealName(String realName) {
        this.realName = realName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getEmployeeId() {
        return employeeId;
    }
    
    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public String getPosition() {
        return position;
    }
    
    public void setPosition(String position) {
        this.position = position;
    }
    
    public String getAccountStatus() {
        return accountStatus;
    }
    
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }
    
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
    
    public String getRegisteredIp() {
        return registeredIp;
    }
    
    public void setRegisteredIp(String registeredIp) {
        this.registeredIp = registeredIp;
    }
    
    @Override
    public String toString() {
        return "RegisterResponse{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", registeredAt=" + registeredAt +
                ", registeredIp='" + registeredIp + '\'' +
                '}';
    }
}