package gov.changsha.finance.dto.request;

import javax.validation.constraints.*;

/**
 * 用户注册请求DTO
 * 支持用户名、密码、邮箱、姓名、部门等字段验证
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-11
 */
public class RegisterRequest {
    
    /**
     * 用户名 - 必填，3-50个字符，只能包含字母、数字、下划线
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    /**
     * 密码 - 必填，8-100个字符，必须包含大小写字母和数字
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须在8-100个字符之间")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,}$", 
             message = "密码必须包含至少一个大写字母、一个小写字母和一个数字")
    private String password;
    
    /**
     * 确认密码 - 必填
     */
    @NotBlank(message = "确认密码不能为空")
    private String confirmPassword;
    
    /**
     * 真实姓名 - 必填，2-100个字符
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 100, message = "真实姓名长度必须在2-100个字符之间")
    private String realName;
    
    /**
     * 电子邮箱 - 必填，有效邮箱格式
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "请输入有效的邮箱地址")
    @Size(max = 255, message = "邮箱长度不能超过255个字符")
    private String email;
    
    /**
     * 联系电话 - 可选，11位手机号码
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号码")
    private String phone;
    
    /**
     * 工号 - 可选，3-50个字符
     */
    @Size(max = 50, message = "工号长度不能超过50个字符")
    private String employeeId;
    
    /**
     * 部门名称 - 必填，2-100个字符
     */
    @NotBlank(message = "部门名称不能为空")
    @Size(min = 2, max = 100, message = "部门名称长度必须在2-100个字符之间")
    private String department;
    
    /**
     * 职位 - 必填，2-100个字符
     */
    @NotBlank(message = "职位不能为空")
    @Size(min = 2, max = 100, message = "职位长度必须在2-100个字符之间")
    private String position;
    
    /**
     * 客户端信息（可选，用于审计）
     */
    private String clientInfo;
    
    // 默认构造函数
    public RegisterRequest() {}
    
    // 完整构造函数
    public RegisterRequest(String username, String password, String confirmPassword, 
                          String realName, String email, String department, String position) {
        this.username = username;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.realName = realName;
        this.email = email;
        this.department = department;
        this.position = position;
    }
    
    // Getters and Setters
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getConfirmPassword() {
        return confirmPassword;
    }
    
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
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
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
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
    
    public String getClientInfo() {
        return clientInfo;
    }
    
    public void setClientInfo(String clientInfo) {
        this.clientInfo = clientInfo;
    }
    
    /**
     * 验证密码确认是否匹配
     */
    public boolean isPasswordConfirmed() {
        return password != null && password.equals(confirmPassword);
    }
    
    @Override
    public String toString() {
        return "RegisterRequest{" +
                "username='" + username + '\'' +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", department='" + department + '\'' +
                ", position='" + position + '\'' +
                ", clientInfo='" + clientInfo + '\'' +
                '}';
    }
}