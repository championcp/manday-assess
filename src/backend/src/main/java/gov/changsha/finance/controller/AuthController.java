package gov.changsha.finance.controller;

import gov.changsha.finance.dto.ApiResponse;
import gov.changsha.finance.dto.request.LoginRequest;
import gov.changsha.finance.dto.request.RegisterRequest;
import gov.changsha.finance.dto.response.JwtAuthenticationResponse;
import gov.changsha.finance.dto.response.RegisterResponse;
import gov.changsha.finance.security.jwt.JwtTokenProvider;
import gov.changsha.finance.security.jwt.UserPrincipal;
import gov.changsha.finance.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.LocalDateTime;

/**
 * 认证控制器
 * 处理用户登录、登出、令牌刷新等认证相关操作
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-09
 */
@Tag(name = "认证管理", description = "用户认证和授权相关API")
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 用户注册
     */
    @Operation(summary = "用户注册", description = "创建新用户账户，支持用户名、密码、邮箱、姓名、部门等字段验证")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @Valid @RequestBody RegisterRequest registerRequest,
            HttpServletRequest request) {
        
        try {
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            logger.info("用户注册尝试 - 用户名: {}, 邮箱: {}, IP: {}", 
                       registerRequest.getUsername(), registerRequest.getEmail(), clientIp);
            
            // 设置客户端信息用于审计
            registerRequest.setClientInfo(userAgent);
            
            // 执行注册
            RegisterResponse response = authService.registerUser(registerRequest, clientIp);
            
            logger.info("用户注册成功 - 用户ID: {}, 用户名: {}, IP: {}", 
                       response.getUserId(), response.getUsername(), clientIp);
            
            return ResponseEntity.ok(ApiResponse.success("注册成功", response));
            
        } catch (IllegalArgumentException ex) {
            logger.warn("用户注册失败 - 数据验证错误: {}, 用户名: {}", 
                       ex.getMessage(), registerRequest.getUsername());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
            
        } catch (Exception ex) {
            logger.error("用户注册异常 - 用户名: {}", registerRequest.getUsername(), ex);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("注册系统异常，请稍后重试"));
        }
    }
    
    /**
     * 检查用户名是否可用
     */
    @Operation(summary = "检查用户名可用性", description = "检查指定用户名是否已被注册")
    @GetMapping("/check-username")
    public ResponseEntity<ApiResponse<Boolean>> checkUsernameAvailability(@RequestParam String username) {
        try {
            boolean available = authService.isUsernameAvailable(username);
            String message = available ? "用户名可用" : "用户名已被占用";
            return ResponseEntity.ok(ApiResponse.success(message, available));
            
        } catch (Exception ex) {
            logger.error("检查用户名可用性异常 - 用户名: {}", username, ex);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("检查用户名可用性失败"));
        }
    }
    
    /**
     * 检查邮箱是否可用
     */
    @Operation(summary = "检查邮箱可用性", description = "检查指定邮箱是否已被注册")
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailAvailability(@RequestParam String email) {
        try {
            boolean available = authService.isEmailAvailable(email);
            String message = available ? "邮箱可用" : "邮箱已被注册";
            return ResponseEntity.ok(ApiResponse.success(message, available));
            
        } catch (Exception ex) {
            logger.error("检查邮箱可用性异常 - 邮箱: {}", email, ex);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("检查邮箱可用性失败"));
        }
    }
    
    /**
     * 检查工号是否可用
     */
    @Operation(summary = "检查工号可用性", description = "检查指定工号是否已被注册")
    @GetMapping("/check-employee-id")
    public ResponseEntity<ApiResponse<Boolean>> checkEmployeeIdAvailability(@RequestParam String employeeId) {
        try {
            boolean available = authService.isEmployeeIdAvailable(employeeId);
            String message = available ? "工号可用" : "工号已被占用";
            return ResponseEntity.ok(ApiResponse.success(message, available));
            
        } catch (Exception ex) {
            logger.error("检查工号可用性异常 - 工号: {}", employeeId, ex);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("检查工号可用性失败"));
        }
    }
    
    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户使用用户名/邮箱/工号和密码登录系统")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletRequest request) {
        
        try {
            String clientIp = getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            
            logger.info("用户登录尝试 - 用户: {}, IP: {}", loginRequest.getUsername(), clientIp);
            
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUsername(),
                    loginRequest.getPassword()
                )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            // 生成JWT令牌
            String accessToken = jwtTokenProvider.generateAccessToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            
            // 更新用户登录信息
            authService.updateUserLoginInfo(userPrincipal.getId(), clientIp, userAgent);
            
            // 构建响应
            JwtAuthenticationResponse response = new JwtAuthenticationResponse();
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setExpiresIn(86400L); // 24小时
            response.setRefreshExpiresIn(604800L); // 7天
            
            // 设置用户信息
            response.setUserId(userPrincipal.getId());
            response.setUsername(userPrincipal.getUsername());
            response.setRealName(userPrincipal.getRealName());
            response.setEmail(userPrincipal.getEmail());
            response.setEmployeeId(userPrincipal.getEmployeeId());
            response.setDepartment(userPrincipal.getDepartment());
            response.setPosition(userPrincipal.getPosition());
            response.setRoles(userPrincipal.getRoles());
            response.setPermissions(userPrincipal.getPermissions());
            response.setLoginTime(LocalDateTime.now());
            response.setLastLoginTime(userPrincipal.getLastLoginAt());
            response.setLoginIp(clientIp);
            
            logger.info("用户登录成功 - 用户: {}, IP: {}", userPrincipal.getUsername(), clientIp);
            
            return ResponseEntity.ok(ApiResponse.success("登录成功", response));
            
        } catch (BadCredentialsException ex) {
            logger.warn("用户登录失败 - 用户名或密码错误: {}", loginRequest.getUsername());
            authService.handleFailedLogin(loginRequest.getUsername(), getClientIpAddress(request));
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("用户名或密码错误"));
            
        } catch (LockedException ex) {
            logger.warn("用户登录失败 - 账户被锁定: {}", loginRequest.getUsername());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("账户已被锁定，请联系管理员"));
            
        } catch (DisabledException ex) {
            logger.warn("用户登录失败 - 账户被禁用: {}", loginRequest.getUsername());
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("账户已被禁用，请联系管理员"));
            
        } catch (Exception ex) {
            logger.error("用户登录异常 - 用户: {}", loginRequest.getUsername(), ex);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("登录系统异常，请稍后重试"));
        }
    }
    
    /**
     * 刷新令牌
     */
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtAuthenticationResponse>> refreshToken(
            @RequestParam String refreshToken,
            HttpServletRequest request) {
        
        try {
            String clientIp = getClientIpAddress(request);
            
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("刷新令牌无效或已过期"));
            }
            
            String username = jwtTokenProvider.getUsernameFromToken(refreshToken);
            Authentication authentication = authService.getAuthenticationByUsername(username);
            
            if (authentication == null) {
                return ResponseEntity.badRequest()
                    .body(ApiResponse.error("用户信息不存在"));
            }
            
            // 生成新的访问令牌
            String newAccessToken = jwtTokenProvider.generateAccessToken(authentication);
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            
            JwtAuthenticationResponse response = new JwtAuthenticationResponse();
            response.setAccessToken(newAccessToken);
            response.setRefreshToken(refreshToken); // 保持原有刷新令牌
            response.setExpiresIn(86400L);
            response.setUserId(userPrincipal.getId());
            response.setUsername(userPrincipal.getUsername());
            response.setLoginIp(clientIp);
            
            logger.info("令牌刷新成功 - 用户: {}, IP: {}", username, clientIp);
            
            return ResponseEntity.ok(ApiResponse.success("令牌刷新成功", response));
            
        } catch (Exception ex) {
            logger.error("令牌刷新失败", ex);
            return ResponseEntity.badRequest()
                .body(ApiResponse.error("令牌刷新失败"));
        }
    }
    
    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户退出系统，清除认证信息")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
                String clientIp = getClientIpAddress(request);
                
                // 记录登出日志
                authService.recordLogout(userPrincipal.getId(), clientIp);
                
                logger.info("用户登出 - 用户: {}, IP: {}", userPrincipal.getUsername(), clientIp);
            }
            
            // 清除Spring Security上下文
            SecurityContextHolder.clearContext();
            
            return ResponseEntity.ok(ApiResponse.success("登出成功", null));
            
        } catch (Exception ex) {
            logger.error("用户登出异常", ex);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("登出异常"));
        }
    }
    
    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的基本信息和权限")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserPrincipal>> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                // 检查principal类型，避免ClassCastException
                Object principal = authentication.getPrincipal();
                if (principal instanceof UserPrincipal) {
                    UserPrincipal userPrincipal = (UserPrincipal) principal;
                    logger.info("获取用户信息成功 - 用户: {}", userPrincipal.getUsername());
                    return ResponseEntity.ok(ApiResponse.success("获取用户信息成功", userPrincipal));
                } else {
                    // principal是String类型，说明是匿名用户或认证失败
                    logger.warn("获取用户信息失败 - 无效的认证类型: {}", principal.getClass().getSimpleName());
                    return ResponseEntity.status(401)
                        .body(ApiResponse.error(401, "用户未认证或认证已过期"));
                }
            }
            
            logger.warn("获取用户信息失败 - 未找到认证信息");
            return ResponseEntity.status(401)
                .body(ApiResponse.error(401, "用户未认证"));
            
        } catch (Exception ex) {
            logger.error("获取当前用户信息异常", ex);
            return ResponseEntity.internalServerError()
                .body(ApiResponse.error("获取用户信息异常"));
        }
    }
    
    /**
     * 检查令牌有效性
     */
    @Operation(summary = "检查令牌有效性", description = "验证JWT令牌是否有效")
    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@RequestParam String token) {
        try {
            boolean isValid = jwtTokenProvider.validateToken(token);
            return ResponseEntity.ok(ApiResponse.success("令牌验证完成", isValid));
            
        } catch (Exception ex) {
            logger.error("令牌验证异常", ex);
            return ResponseEntity.ok(ApiResponse.success("令牌验证完成", false));
        }
    }
    
    /**
     * 临时密码编码工具 (仅开发环境)
     */
    @GetMapping("/encode-password")
    public ResponseEntity<String> encodePassword(@RequestParam String password) {
        String encoded = passwordEncoder.encode(password);
        return ResponseEntity.ok("Password: " + password + ", Encoded: " + encoded);
    }
    
    /**
     * 获取客户端IP地址（支持代理）
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}