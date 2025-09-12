package gov.changsha.finance.controller;

import gov.changsha.finance.entity.Role;
import gov.changsha.finance.entity.User;
import gov.changsha.finance.repository.SimpleProjectRepository;
import gov.changsha.finance.repository.UserRepository;
import gov.changsha.finance.security.jwt.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureTestMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Issue #10 NESMA权限修复测试类
 * 验证NESMA计算API的权限配置
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-12
 */
@SpringBootTest
@AutoConfigureTestMvc
@ActiveProfiles("test")
@DisplayName("Issue #10: NESMA计算功能权限修复测试")
class Issue10NesmaPermissionTest {

    private MockMvc mockMvc;
    
    @Mock
    private SimpleProjectRepository projectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    private User adminUser;
    private User projectManagerUser;
    private User assessorUser;
    private User auditorUser;
    private User regularUser;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext) {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
                
        setupTestUsers();
    }

    /**
     * 设置测试用户
     */
    private void setupTestUsers() {
        // 创建管理员角色
        Role adminRole = new Role("系统管理员", "ADMIN", "系统最高权限管理员");
        Set<Role> adminRoles = new HashSet<>();
        adminRoles.add(adminRole);

        // 创建项目经理角色
        Role pmRole = new Role("项目经理", "PROJECT_MANAGER", "项目管理权限");
        Set<Role> pmRoles = new HashSet<>();
        pmRoles.add(pmRole);

        // 创建评估人员角色
        Role assessorRole = new Role("评估人员", "ASSESSOR", "软件规模评估权限");
        Set<Role> assessorRoles = new HashSet<>();
        assessorRoles.add(assessorRole);

        // 创建审计员角色
        Role auditorRole = new Role("审计员", "AUDITOR", "审计监督权限");
        Set<Role> auditorRoles = new HashSet<>();
        auditorRoles.add(auditorRole);

        // 创建普通用户角色
        Role userRole = new Role("普通用户", "USER", "基础查看权限");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);

        // 创建管理员用户
        adminUser = new User("admin", "$2a$10$hashedPassword", "系统管理员", "admin@changsha.gov.cn");
        adminUser.setId(1L);
        adminUser.setRoles(adminRoles);
        adminUser.setAccountStatus(User.AccountStatus.ACTIVE);
        adminUser.setCreatedAt(LocalDateTime.now());

        // 创建项目经理用户
        projectManagerUser = new User("pm", "$2a$10$hashedPassword", "项目经理", "pm@changsha.gov.cn");
        projectManagerUser.setId(2L);
        projectManagerUser.setRoles(pmRoles);
        projectManagerUser.setAccountStatus(User.AccountStatus.ACTIVE);

        // 创建评估人员用户
        assessorUser = new User("assessor", "$2a$10$hashedPassword", "评估人员", "assessor@changsha.gov.cn");
        assessorUser.setId(3L);
        assessorUser.setRoles(assessorRoles);
        assessorUser.setAccountStatus(User.AccountStatus.ACTIVE);

        // 创建审计员用户
        auditorUser = new User("auditor", "$2a$10$hashedPassword", "审计员", "auditor@changsha.gov.cn");
        auditorUser.setId(4L);
        auditorUser.setRoles(auditorRoles);
        auditorUser.setAccountStatus(User.AccountStatus.ACTIVE);

        // 创建普通用户
        regularUser = new User("user", "$2a$10$hashedPassword", "普通用户", "user@changsha.gov.cn");
        regularUser.setId(5L);
        regularUser.setRoles(userRoles);
        regularUser.setAccountStatus(User.AccountStatus.ACTIVE);
    }

    /**
     * 创建认证对象
     */
    private Authentication createAuthentication(User user) {
        UserPrincipal userPrincipal = UserPrincipal.create(user);
        return new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
    }

    @Test
    @DisplayName("管理员应该能够访问NESMA计算API")
    void testAdminCanAccessNesmaApis() throws Exception {
        Authentication auth = createAuthentication(adminUser);
        Long projectId = 1L;

        // 测试标准NESMA API
        mockMvc.perform(post("/api/nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // 测试简化NESMA API
        mockMvc.perform(post("/api/simple-nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("项目经理应该能够访问NESMA计算API")
    void testProjectManagerCanAccessNesmaApis() throws Exception {
        Authentication auth = createAuthentication(projectManagerUser);
        Long projectId = 1L;

        // 测试标准NESMA API
        mockMvc.perform(post("/api/nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // 测试简化NESMA API  
        mockMvc.perform(post("/api/simple-nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("评估人员应该能够访问NESMA计算API")
    void testAssessorCanAccessNesmaApis() throws Exception {
        Authentication auth = createAuthentication(assessorUser);
        Long projectId = 1L;

        // 测试标准NESMA API
        mockMvc.perform(post("/api/nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        // 测试简化NESMA API
        mockMvc.perform(post("/api/simple-nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("审计员不应该能够访问NESMA计算API")
    void testAuditorShouldNotAccessNesmaApis() throws Exception {
        Authentication auth = createAuthentication(auditorUser);
        Long projectId = 1L;

        // 标准NESMA API应该被拒绝
        mockMvc.perform(post("/api/nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        // 简化NESMA API也应该被拒绝
        mockMvc.perform(post("/api/simple-nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("普通用户不应该能够访问NESMA计算API")
    void testRegularUserShouldNotAccessNesmaApis() throws Exception {
        Authentication auth = createAuthentication(regularUser);
        Long projectId = 1L;

        // 标准NESMA API应该被拒绝
        mockMvc.perform(post("/api/nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        // 简化NESMA API也应该被拒绝
        mockMvc.perform(post("/api/simple-nesma/calculate/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("未认证用户应该被拒绝访问")
    void testUnauthenticatedUserShouldBeDenied() throws Exception {
        Long projectId = 1L;

        // 不提供认证信息
        mockMvc.perform(post("/api/nesma/calculate/" + projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/simple-nesma/calculate/" + projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("使用Mock管理员用户测试NESMA权限")
    void testWithMockAdminUser() throws Exception {
        Long projectId = 1L;

        mockMvc.perform(post("/api/nesma/calculate/" + projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ASSESSOR"})
    @DisplayName("使用Mock评估员用户测试NESMA权限")
    void testWithMockAssessorUser() throws Exception {
        Long projectId = 1L;

        mockMvc.perform(post("/api/simple-nesma/calculate/" + projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("使用Mock普通用户测试NESMA权限拒绝")
    void testWithMockRegularUserShouldBeDenied() throws Exception {
        Long projectId = 1L;

        mockMvc.perform(post("/api/nesma/calculate/" + projectId)
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }
}