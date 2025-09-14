package gov.changsha.finance.service;

import gov.changsha.finance.dto.request.RegisterRequest;
import gov.changsha.finance.dto.response.RegisterResponse;
import gov.changsha.finance.entity.User;
import gov.changsha.finance.repository.UserRepository;
import gov.changsha.finance.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.scheduling.annotation.Async;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证服务类
 * 处理用户认证相关业务逻辑，增加性能优化和缓存机制
 * 
 * @author 开发团队
 * @version 1.1.0
 * @since 2025-09-09
 */
@Service
@Transactional
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private AuditLogService auditLogService; // 将在后续创建
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private LoginCacheService loginCacheService;
    
    /**
     * 更新用户登录信息（性能优化版本）
     */
    public void updateUserLoginInfo(Long userId, String loginIp, String userAgent) {
        long startTime = System.currentTimeMillis();
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastLoginAt(LocalDateTime.now());
                user.resetFailedLoginAttempts(); // 登录成功后重置失败次数
                userRepository.save(user);
                
                // 清除用户缓存，确保下次获取最新信息
                userDetailsService.clearUserCache(user.getId(), user.getUsername());
                
                // 清除登录失败计数缓存
                loginCacheService.clearLoginFailureCount(user.getUsername(), loginIp);
                
                // 异步记录登录审计日志（不影响登录响应时间）
                recordLoginAuditAsync(user, loginIp, userAgent);
                
                long duration = System.currentTimeMillis() - startTime;
                logger.debug("更新用户登录信息成功 - 用户ID: {}, IP: {}, 耗时: {}ms", userId, loginIp, duration);
            }
        } catch (Exception ex) {
            logger.error("更新用户登录信息失败 - 用户ID: {}, 耗时: {}ms", userId, System.currentTimeMillis() - startTime, ex);
        }
    }
    
    /**
     * 异步记录登录审计日志
     */
    @Async
    void recordLoginAuditAsync(User user, String loginIp, String userAgent) {
        try {
            auditLogService.recordLogin(user, loginIp, userAgent);
            logger.debug("异步记录登录审计日志成功 - 用户: {}", user.getUsername());
        } catch (Exception auditEx) {
            logger.warn("异步记录登录审计日志失败 - 用户: {}, 错误: {}", user.getUsername(), auditEx.getMessage());
        }
    }
    
    /**
     * 处理登录失败（性能优化版本，加入缓存防暴力破解）
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailedLogin(String username, String loginIp) {
        long startTime = System.currentTimeMillis();
        try {
            // 先更新缓存中的失败计数
            loginCacheService.cacheLoginFailureCount(username, loginIp);
            int failureCount = loginCacheService.getLoginFailureCount(username, loginIp);
            
            // 如果失败次数超过阈值，记录警告但继续处理数据库更新
            if (failureCount >= 5) {
                logger.warn("检测到可能的暴力破解攻击 - 用户: {}, IP: {}, 失败次数: {}", username, loginIp, failureCount);
            }
            
            // 更新数据库中的用户失败计数
            Optional<User> userOpt = userRepository.findByUsernameOrEmailOrEmployeeId(username, username, username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.incrementFailedLoginAttempts();
                userRepository.save(user);
                
                // 清除用户缓存，确保下次获取最新状态
                userDetailsService.clearUserCache(user.getId(), user.getUsername());
                
                logger.warn("用户登录失败 - 用户: {}, 数据库失败次数: {}, 缓存失败次数: {}, IP: {}", 
                          username, user.getFailedLoginAttempts(), failureCount, loginIp);
                
                // 如果达到锁定阈值，发送警告
                if (user.getFailedLoginAttempts() >= 5) {
                    logger.warn("用户账户被锁定 - 用户: {}, IP: {}", username, loginIp);
                }
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("处理登录失败完成 - 用户: {}, 耗时: {}ms", username, duration);
            
        } catch (Exception ex) {
            logger.error("处理登录失败异常 - 用户: {}, 耗时: {}ms", username, System.currentTimeMillis() - startTime, ex);
        }
    }
    
    /**
     * 根据用户名获取认证对象
     */
    public Authentication getAuthenticationByUsername(String username) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
        } catch (Exception ex) {
            logger.error("根据用户名获取认证对象失败 - 用户: {}", username, ex);
            return null;
        }
    }
    
    /**
     * 记录用户登出
     */
    public void recordLogout(Long userId, String logoutIp) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                // 记录登出审计日志
                auditLogService.recordLogout(user, logoutIp);
                
                logger.debug("记录用户登出成功 - 用户ID: {}, IP: {}", userId, logoutIp);
            }
        } catch (Exception ex) {
            logger.error("记录用户登出失败 - 用户ID: {}", userId, ex);
        }
    }
    
    /**
     * 检查用户账户状态
     */
    @Transactional(readOnly = true)
    public boolean isUserAccountValid(String username) {
        try {
            Optional<User> userOpt = userRepository.findByUsernameOrEmailOrEmployeeId(username, username, username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                return user.isEnabled() && user.isAccountNonLocked();
            }
            return false;
        } catch (Exception ex) {
            logger.error("检查用户账户状态异常 - 用户: {}", username, ex);
            return false;
        }
    }
    
    /**
     * 解锁用户账户（管理员操作）
     */
    public boolean unlockUserAccount(Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.resetFailedLoginAttempts();
                user.setAccountStatus(User.AccountStatus.ACTIVE);
                userRepository.save(user);
                
                // 记录解锁操作审计日志
                auditLogService.recordAccountUnlock(user);
                
                logger.info("用户账户解锁成功 - 用户: {}", user.getUsername());
                return true;
            }
            return false;
        } catch (Exception ex) {
            logger.error("解锁用户账户失败 - 用户ID: {}", userId, ex);
            return false;
        }
    }
    
    /**
     * 锁定用户账户（管理员操作）
     */
    public boolean lockUserAccount(Long userId, String reason) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setAccountStatus(User.AccountStatus.LOCKED);
                user.setLockedAt(LocalDateTime.now());
                userRepository.save(user);
                
                // 记录锁定操作审计日志
                auditLogService.recordAccountLock(user, reason);
                
                logger.info("用户账户锁定成功 - 用户: {}, 原因: {}", user.getUsername(), reason);
                return true;
            }
            return false;
        } catch (Exception ex) {
            logger.error("锁定用户账户失败 - 用户ID: {}", userId, ex);
            return false;
        }
    }
    
    /**
     * 检查密码是否即将过期
     */
    @Transactional(readOnly = true)
    public boolean isPasswordExpiringSoon(Long userId, int daysThreshold) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                if (user.getPasswordExpiresAt() != null) {
                    LocalDateTime threshold = LocalDateTime.now().plusDays(daysThreshold);
                    return user.getPasswordExpiresAt().isBefore(threshold);
                }
            }
            return false;
        } catch (Exception ex) {
            logger.error("检查密码过期状态异常 - 用户ID: {}", userId, ex);
            return false;
        }
    }
    
    /**
     * 用户注册
     * 创建新用户账户，包含完整的数据验证和安全处理
     */
    @Transactional
    public RegisterResponse registerUser(RegisterRequest registerRequest, String clientIp) {
        try {
            logger.info("开始用户注册 - 用户名: {}, 邮箱: {}, IP: {}", 
                       registerRequest.getUsername(), registerRequest.getEmail(), clientIp);
            
            // 1. 验证密码确认
            if (!registerRequest.isPasswordConfirmed()) {
                throw new IllegalArgumentException("密码与确认密码不匹配");
            }
            
            // 2. 检查用户名是否已存在
            if (userRepository.existsByUsername(registerRequest.getUsername())) {
                throw new IllegalArgumentException("用户名已存在");
            }
            
            // 3. 检查邮箱是否已存在
            if (userRepository.existsByEmail(registerRequest.getEmail())) {
                throw new IllegalArgumentException("邮箱已被注册");
            }
            
            // 4. 检查工号是否已存在（如果提供了工号）
            if (registerRequest.getEmployeeId() != null && 
                !registerRequest.getEmployeeId().trim().isEmpty() && 
                userRepository.existsByEmployeeId(registerRequest.getEmployeeId())) {
                throw new IllegalArgumentException("工号已存在");
            }
            
            // 5. 创建用户对象
            User newUser = new User();
            newUser.setUsername(registerRequest.getUsername());
            newUser.setPasswordHash(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setRealName(registerRequest.getRealName());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPhone(registerRequest.getPhone());
            newUser.setEmployeeId(registerRequest.getEmployeeId());
            newUser.setDepartment(registerRequest.getDepartment());
            newUser.setPosition(registerRequest.getPosition());
            newUser.setAccountStatus(User.AccountStatus.ACTIVE);
            newUser.setCreatedBy("SYSTEM"); // 自注册由系统创建
            newUser.setUpdatedBy("SYSTEM");
            
            // 6. 设置密码过期时间（90天）
            newUser.setPasswordExpiresAt(LocalDateTime.now().plusDays(90));
            
            // 7. 保存用户
            User savedUser = userRepository.save(newUser);
            
            // 8. 记录注册审计日志
            try {
                auditLogService.recordRegistration(savedUser, clientIp, registerRequest.getClientInfo());
            } catch (Exception auditEx) {
                logger.warn("记录注册审计日志失败，但不影响业务操作", auditEx);
            }
            
            // 9. 构建响应
            RegisterResponse response = new RegisterResponse();
            response.setUserId(savedUser.getId());
            response.setUsername(savedUser.getUsername());
            response.setRealName(savedUser.getRealName());
            response.setEmail(savedUser.getEmail());
            response.setEmployeeId(savedUser.getEmployeeId());
            response.setDepartment(savedUser.getDepartment());
            response.setPosition(savedUser.getPosition());
            response.setAccountStatus(savedUser.getAccountStatus().toString());
            response.setRegisteredAt(savedUser.getCreatedAt());
            response.setRegisteredIp(clientIp);
            
            logger.info("用户注册成功 - 用户ID: {}, 用户名: {}, IP: {}", 
                       savedUser.getId(), savedUser.getUsername(), clientIp);
            
            return response;
            
        } catch (IllegalArgumentException ex) {
            logger.warn("用户注册失败 - 数据验证错误: {}, 用户名: {}", ex.getMessage(), registerRequest.getUsername());
            throw ex;
        } catch (Exception ex) {
            logger.error("用户注册异常 - 用户名: {}", registerRequest.getUsername(), ex);
            throw new RuntimeException("用户注册失败，请稍后重试", ex);
        }
    }
    
    /**
     * 验证注册数据的业务规则
     */
    private void validateRegistrationData(RegisterRequest registerRequest) {
        // 用户名不能包含特殊字符（已在DTO注解中验证）
        // 邮箱格式验证（已在DTO注解中验证）
        // 密码强度验证（已在DTO注解中验证）
        
        // 可在此处添加额外的业务规则验证
        // 例如：特定部门的限制、用户名黑名单等
    }
    
    /**
     * 检查用户名是否可用
     */
    @Transactional(readOnly = true)
    public boolean isUsernameAvailable(String username) {
        try {
            return !userRepository.existsByUsername(username);
        } catch (Exception ex) {
            logger.error("检查用户名可用性异常 - 用户名: {}", username, ex);
            return false;
        }
    }
    
    /**
     * 检查邮箱是否可用
     */
    @Transactional(readOnly = true)
    public boolean isEmailAvailable(String email) {
        try {
            return !userRepository.existsByEmail(email);
        } catch (Exception ex) {
            logger.error("检查邮箱可用性异常 - 邮箱: {}", email, ex);
            return false;
        }
    }
    
    /**
     * 检查工号是否可用
     */
    @Transactional(readOnly = true)
    public boolean isEmployeeIdAvailable(String employeeId) {
        try {
            if (employeeId == null || employeeId.trim().isEmpty()) {
                return true; // 工号为空时认为可用
            }
            return !userRepository.existsByEmployeeId(employeeId);
        } catch (Exception ex) {
            logger.error("检查工号可用性异常 - 工号: {}", employeeId, ex);
            return false;
        }
    }
}