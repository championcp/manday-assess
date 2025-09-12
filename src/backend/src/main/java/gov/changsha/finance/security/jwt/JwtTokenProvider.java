package gov.changsha.finance.security.jwt;

import gov.changsha.finance.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT令牌提供者
 * 负责JWT令牌的生成、解析和验证
 * 符合政府级安全要求
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Component
public class JwtTokenProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);
    
    private static final String AUTHORITIES_KEY = "authorities";
    private static final String USER_ID_KEY = "userId";
    private static final String USERNAME_KEY = "username";
    private static final String REAL_NAME_KEY = "realName";
    private static final String EMPLOYEE_ID_KEY = "employeeId";
    private static final String DEPARTMENT_KEY = "department";
    
    @Value("${app.jwt.secret:}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400}")  // 24小时，符合政府安全要求
    private int jwtExpirationInSeconds;
    
    @Value("${app.jwt.refresh-expiration:604800}")  // 7天
    private int jwtRefreshExpirationInSeconds;
    
    private SecretKey key;
    
    /**
     * 初始化密钥
     */
    public void init() {
        if (jwtSecret != null && !jwtSecret.trim().isEmpty()) {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            this.key = Keys.hmacShaKeyFor(keyBytes);
            logger.info("JWT TokenProvider初始化完成，使用配置密钥，长度: {} bits", key.getEncoded().length * 8);
        } else {
            this.key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
            logger.info("JWT TokenProvider初始化完成，动态生成密钥，长度: {} bits", key.getEncoded().length * 8);
        }
    }
    
    private SecretKey getKey() {
        if (key == null) {
            init();
        }
        return key;
    }
    
    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationInSeconds * 1000L);
        
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim(USER_ID_KEY, userPrincipal.getId())
                .claim(USERNAME_KEY, userPrincipal.getUsername())
                .claim(REAL_NAME_KEY, userPrincipal.getRealName())
                .claim(EMPLOYEE_ID_KEY, userPrincipal.getEmployeeId())
                .claim(DEPARTMENT_KEY, userPrincipal.getDepartment())
                .claim(AUTHORITIES_KEY, authorities)
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .setIssuer("manday-assess-system")
                .setAudience("changsha-finance-gov")
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtRefreshExpirationInSeconds * 1000L);
        
        return Jwts.builder()
                .setSubject(userPrincipal.getUsername())
                .claim(USER_ID_KEY, userPrincipal.getId())
                .claim(USERNAME_KEY, userPrincipal.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .setIssuer("manday-assess-system")
                .setAudience("changsha-finance-gov")
                .signWith(getKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getKey())
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }
    
    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getKey())
                .parseClaimsJws(token)
                .getBody();
        
        return claims.get(USER_ID_KEY, Long.class);
    }
    
    /**
     * 从令牌中获取权限列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getAuthoritiesFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getKey())
                .parseClaimsJws(token)
                .getBody();
        
        return (List<String>) claims.get(AUTHORITIES_KEY);
    }
    
    /**
     * 获取令牌过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getKey())
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }
    
    /**
     * 验证令牌是否有效
     */
    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .setSigningKey(getKey())
                    .parseClaimsJws(authToken);
            
            return true;
        } catch (SecurityException ex) {
            logger.error("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            logger.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            logger.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            logger.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            logger.error("JWT claims string is empty: {}", ex.getMessage());
        }
        
        return false;
    }
    
    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 检查令牌是否即将过期（30分钟内）
     */
    public boolean isTokenExpiringSoon(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            long diffInMilliseconds = expiration.getTime() - now.getTime();
            long diffInMinutes = diffInMilliseconds / (60 * 1000);
            return diffInMinutes <= 30; // 30分钟内过期
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 从令牌中提取所有声明
     */
    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(getKey())
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 获取令牌剩余有效时间（秒）
     */
    public long getTokenRemainingTime(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            Date now = new Date();
            return (expiration.getTime() - now.getTime()) / 1000;
        } catch (Exception e) {
            return 0;
        }
    }
}