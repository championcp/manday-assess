package gov.changsha.finance.security;

import gov.changsha.finance.entity.User;
import gov.changsha.finance.repository.UserRepository;
import gov.changsha.finance.security.jwt.UserPrincipal;
import gov.changsha.finance.service.LoginCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 自定义用户详情服务
 * 实现Spring Security UserDetailsService接口
 * 符合政府级安全要求，增加缓存机制优化登录性能
 * 
 * @author 开发团队
 * @version 1.1.0
 * @since 2025-09-09
 */
@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private LoginCacheService loginCacheService;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        long startTime = System.currentTimeMillis();
        
        try {
            // 首先尝试从缓存获取用户信息
            UserPrincipal cachedUser = loginCacheService.getCachedUserInfoByUsername(username);
            if (cachedUser != null) {
                logger.debug("从缓存加载用户成功: {} (耗时: {}ms)", username, System.currentTimeMillis() - startTime);
                return cachedUser;
            }
            
            // 缓存未命中，从数据库查询
            User user = userRepository.findByUsernameOrEmailOrEmployeeId(username, username, username)
                    .orElseThrow(() -> {
                        logger.warn("用户不存在: {} (耗时: {}ms)", username, System.currentTimeMillis() - startTime);
                        return new UsernameNotFoundException("用户不存在: " + username);
                    });
            
            // 创建UserPrincipal并缓存
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            
            // 缓存用户信息和用户名映射
            loginCacheService.cacheUserInfo(user.getId(), userPrincipal);
            loginCacheService.cacheUsernameMapping(user.getUsername(), user.getId());
            
            // 如果用户通过邮箱或工号登录，也要缓存这些映射
            if (!username.equals(user.getUsername())) {
                loginCacheService.cacheUsernameMapping(username, user.getId());
            }
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("从数据库加载用户成功: {} (耗时: {}ms)", user.getUsername(), duration);
            
            return userPrincipal;
            
        } catch (UsernameNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("加载用户异常: {} (耗时: {}ms)", username, System.currentTimeMillis() - startTime, ex);
            throw new UsernameNotFoundException("加载用户信息失败: " + username, ex);
        }
    }
    
    /**
     * 根据用户ID加载用户详情（优化版本，支持缓存）
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 首先尝试从缓存获取用户信息
            UserPrincipal cachedUser = loginCacheService.getCachedUserInfo(id);
            if (cachedUser != null) {
                logger.debug("从缓存加载用户成功(ID: {}): {} (耗时: {}ms)", 
                           id, cachedUser.getUsername(), System.currentTimeMillis() - startTime);
                return cachedUser;
            }
            
            // 缓存未命中，从数据库查询
            User user = userRepository.findById(id)
                    .orElseThrow(() -> {
                        logger.warn("用户ID不存在: {} (耗时: {}ms)", id, System.currentTimeMillis() - startTime);
                        return new UsernameNotFoundException("用户ID不存在: " + id);
                    });
            
            // 创建UserPrincipal并缓存
            UserPrincipal userPrincipal = UserPrincipal.create(user);
            
            // 缓存用户信息和用户名映射
            loginCacheService.cacheUserInfo(user.getId(), userPrincipal);
            loginCacheService.cacheUsernameMapping(user.getUsername(), user.getId());
            
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("从数据库加载用户成功(ID: {}): {} (耗时: {}ms)", id, user.getUsername(), duration);
            
            return userPrincipal;
            
        } catch (UsernameNotFoundException ex) {
            throw ex;
        } catch (Exception ex) {
            logger.error("加载用户异常(ID: {}) (耗时: {}ms)", id, System.currentTimeMillis() - startTime, ex);
            throw new UsernameNotFoundException("加载用户信息失败(ID: " + id + ")", ex);
        }
    }
    
    /**
     * 清除用户缓存（当用户信息更新时调用）
     */
    public void clearUserCache(Long userId, String username) {
        loginCacheService.clearUserCache(userId, username);
        logger.debug("清除用户缓存: {} (ID: {})", username, userId);
    }
}