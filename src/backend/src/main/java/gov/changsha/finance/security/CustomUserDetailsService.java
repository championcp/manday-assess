package gov.changsha.finance.security;

import gov.changsha.finance.entity.User;
import gov.changsha.finance.repository.UserRepository;
import gov.changsha.finance.security.jwt.UserPrincipal;
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
 * 符合政府级安全要求
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Service("userDetailsService")
public class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 支持用户名、邮箱、工号登录
        User user = userRepository.findByUsernameOrEmailOrEmployeeId(username, username, username)
                .orElseThrow(() -> {
                    logger.warn("用户不存在: {}", username);
                    return new UsernameNotFoundException("用户不存在: " + username);
                });
        
        logger.debug("成功加载用户: {}", user.getUsername());
        return UserPrincipal.create(user);
    }
    
    /**
     * 根据用户ID加载用户详情
     */
    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("用户ID不存在: {}", id);
                    return new UsernameNotFoundException("用户ID不存在: " + id);
                });
        
        logger.debug("成功加载用户(ID: {}): {}", id, user.getUsername());
        return UserPrincipal.create(user);
    }
}