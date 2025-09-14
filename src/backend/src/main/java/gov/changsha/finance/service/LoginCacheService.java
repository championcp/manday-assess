package gov.changsha.finance.service;

import gov.changsha.finance.security.jwt.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * 登录缓存服务
 * 专门用于优化登录认证性能，缓存用户基本信息和权限
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-14
 */
@Service
public class LoginCacheService {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginCacheService.class);
    
    // 缓存前缀
    private static final String USER_CACHE_PREFIX = "login:user:";
    private static final String USER_ROLES_CACHE_PREFIX = "login:user:roles:";
    private static final String USER_PERMISSIONS_CACHE_PREFIX = "login:user:permissions:";
    
    // 缓存过期时间
    private static final Duration USER_CACHE_DURATION = Duration.ofMinutes(30);
    private static final Duration ROLES_CACHE_DURATION = Duration.ofMinutes(60);
    private static final Duration PERMISSIONS_CACHE_DURATION = Duration.ofMinutes(60);
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    /**
     * 缓存用户基本信息
     */
    public void cacheUserInfo(Long userId, UserPrincipal userPrincipal) {
        try {
            String cacheKey = USER_CACHE_PREFIX + userId;
            redisTemplate.opsForValue().set(cacheKey, userPrincipal, USER_CACHE_DURATION);
            logger.debug("缓存用户信息成功 - 用户ID: {}", userId);
        } catch (Exception ex) {
            logger.warn("缓存用户信息失败 - 用户ID: {}, 错误: {}", userId, ex.getMessage());
        }
    }
    
    /**
     * 获取缓存的用户信息
     */
    public UserPrincipal getCachedUserInfo(Long userId) {
        try {
            String cacheKey = USER_CACHE_PREFIX + userId;
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof UserPrincipal) {
                logger.debug("获取缓存用户信息成功 - 用户ID: {}", userId);
                return (UserPrincipal) cached;
            }
        } catch (Exception ex) {
            logger.warn("获取缓存用户信息失败 - 用户ID: {}, 错误: {}", userId, ex.getMessage());
        }
        return null;
    }
    
    /**
     * 根据用户名获取缓存的用户信息
     */
    public UserPrincipal getCachedUserInfoByUsername(String username) {
        try {
            // 构建用户名到用户ID的映射缓存键
            String usernameCacheKey = "login:username:" + username;
            Object userIdObj = redisTemplate.opsForValue().get(usernameCacheKey);
            
            if (userIdObj instanceof Long) {
                Long userId = (Long) userIdObj;
                return getCachedUserInfo(userId);
            }
        } catch (Exception ex) {
            logger.warn("根据用户名获取缓存信息失败 - 用户名: {}, 错误: {}", username, ex.getMessage());
        }
        return null;
    }
    
    /**
     * 缓存用户名到用户ID的映射
     */
    public void cacheUsernameMapping(String username, Long userId) {
        try {
            String usernameCacheKey = "login:username:" + username;
            redisTemplate.opsForValue().set(usernameCacheKey, userId, USER_CACHE_DURATION);
            logger.debug("缓存用户名映射成功 - 用户名: {}, 用户ID: {}", username, userId);
        } catch (Exception ex) {
            logger.warn("缓存用户名映射失败 - 用户名: {}, 错误: {}", username, ex.getMessage());
        }
    }
    
    /**
     * 清除用户相关的所有缓存
     */
    public void clearUserCache(Long userId, String username) {
        try {
            // 清除用户信息缓存
            String userCacheKey = USER_CACHE_PREFIX + userId;
            redisTemplate.delete(userCacheKey);
            
            // 清除用户名映射缓存
            String usernameCacheKey = "login:username:" + username;
            redisTemplate.delete(usernameCacheKey);
            
            // 清除角色和权限缓存
            String rolesCacheKey = USER_ROLES_CACHE_PREFIX + userId;
            String permissionsCacheKey = USER_PERMISSIONS_CACHE_PREFIX + userId;
            redisTemplate.delete(rolesCacheKey);
            redisTemplate.delete(permissionsCacheKey);
            
            logger.debug("清除用户缓存成功 - 用户ID: {}, 用户名: {}", userId, username);
        } catch (Exception ex) {
            logger.warn("清除用户缓存失败 - 用户ID: {}, 错误: {}", userId, ex.getMessage());
        }
    }
    
    /**
     * 缓存用户会话Token（用于快速验证）
     */
    public void cacheUserSession(String sessionToken, Long userId) {
        try {
            String sessionCacheKey = "login:session:" + sessionToken;
            redisTemplate.opsForValue().set(sessionCacheKey, userId, Duration.ofHours(24));
            logger.debug("缓存用户会话成功 - Token: {}, 用户ID: {}", sessionToken.substring(0, 8) + "...", userId);
        } catch (Exception ex) {
            logger.warn("缓存用户会话失败 - Token: {}, 错误: {}", sessionToken.substring(0, 8) + "...", ex.getMessage());
        }
    }
    
    /**
     * 获取缓存的会话信息
     */
    public Long getCachedSession(String sessionToken) {
        try {
            String sessionCacheKey = "login:session:" + sessionToken;
            Object cached = redisTemplate.opsForValue().get(sessionCacheKey);
            if (cached instanceof Long) {
                logger.debug("获取缓存会话成功 - Token: {}", sessionToken.substring(0, 8) + "...");
                return (Long) cached;
            }
        } catch (Exception ex) {
            logger.warn("获取缓存会话失败 - Token: {}, 错误: {}", sessionToken.substring(0, 8) + "...", ex.getMessage());
        }
        return null;
    }
    
    /**
     * 清除用户会话缓存
     */
    public void clearUserSession(String sessionToken) {
        try {
            String sessionCacheKey = "login:session:" + sessionToken;
            redisTemplate.delete(sessionCacheKey);
            logger.debug("清除用户会话成功 - Token: {}", sessionToken.substring(0, 8) + "...");
        } catch (Exception ex) {
            logger.warn("清除用户会话失败 - Token: {}, 错误: {}", sessionToken.substring(0, 8) + "...", ex.getMessage());
        }
    }
    
    /**
     * 设置登录失败计数缓存（防暴力破解）
     */
    public void cacheLoginFailureCount(String username, String clientIp) {
        try {
            String failureCacheKey = "login:failure:" + username + ":" + clientIp;
            String countStr = (String) redisTemplate.opsForValue().get(failureCacheKey);
            int count = (countStr != null) ? Integer.parseInt(countStr) + 1 : 1;
            
            redisTemplate.opsForValue().set(failureCacheKey, String.valueOf(count), Duration.ofMinutes(15));
            logger.debug("更新登录失败计数 - 用户: {}, IP: {}, 次数: {}", username, clientIp, count);
        } catch (Exception ex) {
            logger.warn("缓存登录失败计数失败 - 用户: {}, 错误: {}", username, ex.getMessage());
        }
    }
    
    /**
     * 获取登录失败计数
     */
    public int getLoginFailureCount(String username, String clientIp) {
        try {
            String failureCacheKey = "login:failure:" + username + ":" + clientIp;
            String countStr = (String) redisTemplate.opsForValue().get(failureCacheKey);
            return (countStr != null) ? Integer.parseInt(countStr) : 0;
        } catch (Exception ex) {
            logger.warn("获取登录失败计数失败 - 用户: {}, 错误: {}", username, ex.getMessage());
            return 0;
        }
    }
    
    /**
     * 清除登录失败计数（登录成功后）
     */
    public void clearLoginFailureCount(String username, String clientIp) {
        try {
            String failureCacheKey = "login:failure:" + username + ":" + clientIp;
            redisTemplate.delete(failureCacheKey);
            logger.debug("清除登录失败计数 - 用户: {}, IP: {}", username, clientIp);
        } catch (Exception ex) {
            logger.warn("清除登录失败计数失败 - 用户: {}, 错误: {}", username, ex.getMessage());
        }
    }
}