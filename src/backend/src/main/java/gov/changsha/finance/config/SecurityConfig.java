package gov.changsha.finance.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 安全配置类
 * 配置API访问权限和认证策略
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 开发环境下的安全配置 - 允许所有API访问
     */
    @Bean
    @Profile("dev")
    public SecurityFilterChain devSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .authorizeRequests(authz -> authz
                // 开发环境下允许所有API访问
                .antMatchers("/api/**").permitAll()
                .antMatchers("/actuator/health").permitAll()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // 其他请求保持默认认证
                .anyRequest().authenticated()
            )
            .httpBasic();
            
        return http.build();
    }

    /**
     * 生产环境下的安全配置 - 完整的认证和授权
     */
    @Bean
    @Profile({"test", "prod"})
    public SecurityFilterChain prodSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors().and()
            .csrf().disable()
            .authorizeRequests(authz -> authz
                // 公共端点
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/actuator/health").permitAll()
                // API端点需要认证
                .antMatchers("/api/**").authenticated()
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**").authenticated()
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            .httpBasic()
            .and()
            .formLogin();
            
        return http.build();
    }
}