package gov.changsha.finance.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * JWT认证令牌类
 * 封装JWT认证相关信息
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    
    private static final long serialVersionUID = 1L;
    
    private final Object principal;
    private final String token;
    
    /**
     * 未认证的构造函数
     */
    public JwtAuthenticationToken(String token) {
        super(null);
        this.principal = null;
        this.token = token;
        setAuthenticated(false);
    }
    
    /**
     * 已认证的构造函数
     */
    public JwtAuthenticationToken(Object principal, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        super.setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return token;
    }
    
    @Override
    public Object getPrincipal() {
        return principal;
    }
    
    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        if (isAuthenticated) {
            throw new IllegalArgumentException(
                "Cannot set this token to trusted - use constructor which takes a GrantedAuthority list instead");
        }
        super.setAuthenticated(false);
    }
}