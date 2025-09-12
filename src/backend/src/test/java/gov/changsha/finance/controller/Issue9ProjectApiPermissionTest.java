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
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
 * Issue #9 权限修复测试类
 * 验证管理员用户对项目管理API的权限
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-12
 */
@SpringBootTest
@DisplayName("Issue #9: 项目管理API权限修复测试")
class Issue9ProjectApiPermissionTest {

    private MockMvc mockMvc;
    
    @Mock
    private SimpleProjectRepository projectRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private ProjectController projectController;
    
    private User adminUser;
    private User projectManagerUser;
    private User assessorUser;
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

        // 创建普通用户
        regularUser = new User("user", "$2a$10$hashedPassword", "普通用户", "user@changsha.gov.cn");
        regularUser.setId(4L);
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
    @DisplayName("管理员应该能够访问项目列表API")
    void testAdminCanAccessProjectList() throws Exception {
        // 设置管理员用户上下文
        Authentication auth = createAuthentication(adminUser);
        
        mockMvc.perform(get("/api/projects")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("获取项目列表成功"));
    }

    @Test
    @DisplayName("管理员应该能够创建项目")
    void testAdminCanCreateProject() throws Exception {
        Authentication auth = createAuthentication(adminUser);
        
        String requestBody = "{" +
            "\"projectName\": \"测试项目\"," +
            "\"description\": \"Issue #9 权限测试项目\"" +
            "}";

        mockMvc.perform(post("/api/projects")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.success").value(true))
                .andExpected(jsonPath("$.message").value("创建项目成功"));
    }

    @Test
    @DisplayName("管理员应该能够查看项目详情")
    void testAdminCanViewProjectDetails() throws Exception {
        Authentication auth = createAuthentication(adminUser);
        Long projectId = 1L;

        mockMvc.perform(get("/api/projects/" + projectId)
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpected(status().isOk());
        // 注意：这里可能返回404如果项目不存在，但不应该返回403权限错误
    }

    @Test
    @DisplayName("项目经理应该能够访问项目API")
    void testProjectManagerCanAccessProjectApis() throws Exception {
        Authentication auth = createAuthentication(projectManagerUser);

        // 测试项目列表访问
        mockMvc.perform(get("/api/projects")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.success").value(true));

        // 测试项目创建
        String requestBody = "{" +
            "\"projectName\": \"项目经理测试项目\"," +
            "\"description\": \"项目经理权限测试\"" +
            "}";

        mockMvc.perform(post("/api/projects")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpected(status().isOk())
                .andExpected(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("评估人员应该能够查看项目但不能创建")
    void testAssessorCanViewButNotCreate() throws Exception {
        Authentication auth = createAuthentication(assessorUser);

        // 应该能查看项目列表
        mockMvc.perform(get("/api/projects")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpected(status().isOk());

        // 不应该能创建项目
        String requestBody = "{" +
            "\"projectName\": \"评估人员测试项目\"," +
            "\"description\": \"应该被拒绝\"" +
            "}";

        mockMvc.perform(post("/api/projects")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpected(status().isForbidden());
    }

    @Test
    @DisplayName("普通用户应该被拒绝访问所有项目API")
    void testRegularUserShouldBeDeniedAccess() throws Exception {
        Authentication auth = createAuthentication(regularUser);

        // 不应该能访问项目列表
        mockMvc.perform(get("/api/projects")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpected(status().isForbidden());

        // 不应该能创建项目
        String requestBody = "{" +
            "\"projectName\": \"普通用户测试项目\"," +
            "\"description\": \"应该被拒绝\"" +
            "}";

        mockMvc.perform(post("/api/projects")
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpected(status().isForbidden());
    }

    @Test
    @DisplayName("未认证用户应该被拒绝访问")
    void testUnauthenticatedUserShouldBeDenied() throws Exception {
        // 不提供认证信息
        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpected(status().isUnauthorized());

        mockMvc.perform(post("/api/projects")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"projectName\":\"test\"}"))
                .andDo(print())
                .andExpected(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("使用Mock用户测试管理员权限")
    void testWithMockAdminUser() throws Exception {
        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpected(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    @DisplayName("使用Mock用户测试普通用户权限")
    void testWithMockRegularUser() throws Exception {
        mockMvc.perform(get("/api/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpected(status().isForbidden());
    }
}