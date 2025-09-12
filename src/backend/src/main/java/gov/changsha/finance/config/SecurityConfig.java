package gov.changsha.finance.config;

import gov.changsha.finance.security.CustomUserDetailsService;
import gov.changsha.finance.security.jwt.JwtAuthenticationFilter;
import gov.changsha.finance.security.jwt.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security 安全配置类
 * 配置JWT认证、授权策略和安全防护
 * 符合政府级安全要求（等保三级）
 * 
 * @author 开发团队
 * @version 2.0.0
 * @since 2025-09-09
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true,
    prePostEnabled = true
)
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 密码编码器 - 使用BCrypt强加密
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10); // 临时使用强度10，匹配现有数据
    }

    /**
     * JWT认证过滤器
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter();
    }

    /**
     * 认证管理器配置
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, 
                                                     PasswordEncoder passwordEncoder) throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(customUserDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    /**
     * CORS配置 - 生产环境严格控制
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 开发和生产环境允许的域名
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:*", // 允许localhost的所有端口
            "http://127.0.0.1:*", // 允许127.0.0.1的所有端口
            "http://localhost:3000",
            "http://localhost:5173", // Vite开发服务器
            "http://localhost:8080",
            "http://localhost:8081",
            "http://localhost:9000",
            "https://*.changsha.gov.cn",
            "https://*.finance.changsha.gov.cn"
        ));
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1小时
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 开发环境下的安全配置 - 允许测试访问
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests(authz -> authz
                // 公共端点 - 不需要认证
                .antMatchers("/api/auth/**").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/actuator/health", "/actuator/info").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .antMatchers("/favicon.ico", "/error").permitAll()
                
                // 开发环境下API允许访问（但仍需要有效JWT）
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/management/**").hasAnyRole("ADMIN", "PROJECT_MANAGER")
                .antMatchers("/api/projects/**").hasAnyRole("PROJECT_MANAGER", "ADMIN", "ASSESSOR")
                .antMatchers("/api/**").authenticated()
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            );

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }

    /**
     * 生产环境下的安全配置 - 严格的认证和授权
     */
    @Bean
    @Profile({"test", "prod"})
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().configurationSource(corsConfigurationSource())
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .headers(headers -> headers
                .frameOptions().deny()
                .contentTypeOptions().and()
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
                .and()
            )
            .authorizeRequests(authz -> authz
                // 公共端点 - 不需要认证
                .antMatchers("/api/auth/login", "/api/auth/refresh").permitAll()
                .antMatchers("/actuator/health").permitAll()
                
                // 管理端点 - 需要管理员权限
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/management/**").hasAnyRole("ADMIN", "MANAGER")
                .antMatchers("/api/audit/**").hasRole("AUDITOR")
                
                // NESMA计算相关 - 需要评审员权限
                .antMatchers("/api/nesma/**").hasAnyRole("ASSESSOR", "PROJECT_MANAGER", "ADMIN")
                .antMatchers("/api/projects/**").hasAnyRole("PROJECT_MANAGER", "ADMIN")
                
                // 用户相关 - 认证用户可访问
                .antMatchers("/api/user/**").authenticated()
                
                // API文档需要认证（生产环境）
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
                
                // 其他API需要认证
                .antMatchers("/api/**").authenticated()
                
                // 拒绝所有其他请求
                .anyRequest().denyAll()
            );

        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
            
        return http.build();
    }
}