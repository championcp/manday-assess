package gov.changsha.finance.service;

import gov.changsha.finance.entity.User;
import gov.changsha.finance.repository.UserRepository;
import gov.changsha.finance.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 认证服务类
 * 处理用户认证相关业务逻辑
 * 
 * @author 开发团队
 * @version 1.0.0
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
    
    /**
     * 更新用户登录信息
     */
    public void updateUserLoginInfo(Long userId, String loginIp, String userAgent) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.setLastLoginAt(LocalDateTime.now());
                user.resetFailedLoginAttempts(); // 登录成功后重置失败次数
                userRepository.save(user);
                
                // 记录登录审计日志（忽略异常以防止事务回滚）
                try {
                    auditLogService.recordLogin(user, loginIp, userAgent);
                } catch (Exception auditEx) {
                    logger.warn("记录审计日志失败，但不影响业务操作", auditEx);
                }
                
                logger.debug("更新用户登录信息成功 - 用户ID: {}, IP: {}", userId, loginIp);
            }
        } catch (Exception ex) {
            logger.error("更新用户登录信息失败 - 用户ID: {}", userId, ex);
        }
    }
    
    /**
     * 处理登录失败
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailedLogin(String username, String loginIp) {
        try {
            Optional<User> userOpt = userRepository.findByUsernameOrEmailOrEmployeeId(username, username, username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                user.incrementFailedLoginAttempts();
                userRepository.save(user);
                
                logger.warn("用户登录失败 - 用户: {}, 失败次数: {}, IP: {}", 
                          username, user.getFailedLoginAttempts(), loginIp);
                
                // 如果达到锁定阈值，发送警告
                if (user.getFailedLoginAttempts() >= 5) {
                    logger.warn("用户账户被锁定 - 用户: {}, IP: {}", username, loginIp);
                }
            }
        } catch (Exception ex) {
            logger.error("处理登录失败异常 - 用户: {}", username, ex);
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
}