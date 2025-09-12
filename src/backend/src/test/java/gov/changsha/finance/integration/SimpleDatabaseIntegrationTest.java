package gov.changsha.finance.integration;

import gov.changsha.finance.config.TestContainerConfiguration;
import gov.changsha.finance.entity.Project;
import gov.changsha.finance.entity.VafFactor;
import gov.changsha.finance.repository.ProjectRepository;
import gov.changsha.finance.repository.VafFactorRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import jakarta.persistence.EntityManager;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 简化版数据库持久化集成测试
 * 政府项目数据完整性验证
 * 
 * 测试目标：
 * 1. 验证Testcontainers PostgreSQL环境正常工作
 * 2. 验证Project和VafFactor实体的数据持久化
 * 3. 验证数据库Schema结构
 * 4. 验证数据完整性和约束
 * 
 * @author QA Test Engineer
 * @version 1.0.0
 * @since 2025-09-04
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = {SimpleDatabaseIntegrationTest.Initializer.class})
@ActiveProfiles("integration-test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
public class SimpleDatabaseIntegrationTest {

    static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15.4-alpine"))
            .withDatabaseName("manday_assess_test")
            .withUsername("postgres")
            .withPassword("test_password_2025");

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            postgreSQLContainer.start();
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private VafFactorRepository vafFactorRepository;
    
    private Project testProject;
    
    @BeforeEach
    void setUp() {
        // 创建基础测试项目
        testProject = new Project();
        testProject.setProjectCode("TEST-SIMPLE-001");
        testProject.setProjectName("简化集成测试项目");
        testProject.setProjectType("INFORMATION_SYSTEM");
        testProject.setProjectDescription("用于验证Testcontainers集成测试环境");
        testProject.setCreatedBy(1L);
        testProject.setUpdatedBy(1L);
    }
    
    /**
     * 测试数据库连接和基础功能
     * 验证Testcontainers PostgreSQL容器正常启动并可连接
     */
    @Test
    @Order(1)
    void testDatabaseConnection() throws SQLException {
        assertNotNull(dataSource, "数据源不应为空");
        
        try (Connection connection = dataSource.getConnection()) {
            assertNotNull(connection, "数据库连接不应为空");
            assertFalse(connection.isClosed(), "数据库连接应该是活跃的");
            
            // 验证数据库类型
            DatabaseMetaData metaData = connection.getMetaData();
            String databaseProductName = metaData.getDatabaseProductName();
            assertEquals("PostgreSQL", databaseProductName, "应该使用PostgreSQL数据库");
            
            System.out.println("数据库连接验证通过: " + databaseProductName);
            System.out.println("数据库URL: " + connection.getMetaData().getURL());
        }
    }
    
    /**
     * 测试项目数据的基本持久化
     * 验证Project实体的CRUD操作
     */
    @Test
    @Order(2)
    void testProjectPersistence() {
        // 1. 保存项目
        Project savedProject = projectRepository.save(testProject);
        assertNotNull(savedProject.getId(), "项目ID应该自动生成");
        entityManager.flush();
        
        // 2. 从数据库查询项目
        entityManager.clear(); // 清除一级缓存
        Project retrievedProject = projectRepository.findById(savedProject.getId()).orElse(null);
        
        // 3. 验证数据完整性
        assertNotNull(retrievedProject, "应该能够从数据库检索项目");
        assertEquals(testProject.getProjectCode(), retrievedProject.getProjectCode());
        assertEquals(testProject.getProjectName(), retrievedProject.getProjectName());
        assertEquals(testProject.getProjectType(), retrievedProject.getProjectType());
        assertEquals(testProject.getProjectDescription(), retrievedProject.getProjectDescription());
        
        // 4. 验证审计字段自动生成
        assertNotNull(retrievedProject.getCreatedAt(), "创建时间应该自动生成");
        assertNotNull(retrievedProject.getUpdatedAt(), "更新时间应该自动生成");
        
        System.out.println("项目持久化验证通过 - ID: " + retrievedProject.getId());
    }
    
    /**
     * 测试VAF因子的数据持久化
     * 验证VAF因子与项目的关联关系
     */
    @Test
    @Order(3)
    void testVafFactorPersistence() {
        // 1. 先保存项目
        Project savedProject = projectRepository.save(testProject);
        entityManager.flush();
        
        // 2. 创建VAF因子数据
        List<VafFactor> vafFactors = createTestVafFactors(savedProject);
        List<VafFactor> savedFactors = vafFactorRepository.saveAll(vafFactors);
        entityManager.flush();
        
        // 3. 验证VAF因子保存
        assertEquals(14, savedFactors.size(), "应该保存14个VAF因子");
        
        // 4. 从数据库查询VAF因子
        entityManager.clear();
        List<VafFactor> retrievedFactors = vafFactorRepository.findByProjectId(savedProject.getId());
        
        // 5. 验证数据完整性
        assertEquals(14, retrievedFactors.size(), "应该检索到14个VAF因子");
        
        // 6. 验证VAF因子数据正确性
        for (VafFactor factor : retrievedFactors) {
            assertNotNull(factor.getId(), "VAF因子ID不能为空");
            assertNotNull(factor.getFactorType(), "VAF因子类型不能为空");
            assertNotNull(factor.getFactorName(), "VAF因子名称不能为空");
            assertNotNull(factor.getInfluenceScore(), "影响度评分不能为空");
            assertEquals(savedProject.getId(), factor.getProject().getId(), "项目关联应该正确");
            
            assertTrue(factor.getInfluenceScore() >= 0 && factor.getInfluenceScore() <= 5,
                      "影响度评分应该在0-5范围内");
        }
        
        System.out.println("VAF因子持久化验证通过 - 因子数量: " + retrievedFactors.size());
    }
    
    /**
     * 测试数据库Schema结构
     * 验证必要的表和字段存在
     */
    @Test
    @Order(4)
    void testDatabaseSchema() throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            
            // 验证projects表存在
            boolean projectsTableExists = checkTableExists(metaData, "projects");
            assertTrue(projectsTableExists, "projects表应该存在");
            
            // 验证vaf_factors表存在
            boolean vafFactorsTableExists = checkTableExists(metaData, "vaf_factors");
            assertTrue(vafFactorsTableExists, "vaf_factors表应该存在");
            
            System.out.println("数据库Schema验证通过");
        }
    }
    
    /**
     * 测试数据统计和查询功能
     * 验证Repository的查询方法
     */
    @Test
    @Order(5)
    void testDataStatistics() {
        // 1. 创建多个测试项目
        Project project1 = createTestProject("STAT-001", "统计测试项目1");
        Project project2 = createTestProject("STAT-002", "统计测试项目2");
        
        projectRepository.save(project1);
        projectRepository.save(project2);
        entityManager.flush();
        
        // 2. 为每个项目创建VAF因子
        List<VafFactor> factors1 = createTestVafFactors(project1);
        List<VafFactor> factors2 = createTestVafFactors(project2);
        
        vafFactorRepository.saveAll(factors1);
        vafFactorRepository.saveAll(factors2);
        entityManager.flush();
        
        // 3. 验证统计功能
        long totalProjects = projectRepository.count();
        assertTrue(totalProjects >= 2, "项目总数应该至少为2");
        
        long totalVafFactors = vafFactorRepository.count();
        assertTrue(totalVafFactors >= 28, "VAF因子总数应该至少为28（2×14）");
        
        // 4. 验证按项目查询VAF因子
        List<VafFactor> project1Factors = vafFactorRepository.findByProjectId(project1.getId());
        List<VafFactor> project2Factors = vafFactorRepository.findByProjectId(project2.getId());
        
        assertEquals(14, project1Factors.size(), "项目1应该有14个VAF因子");
        assertEquals(14, project2Factors.size(), "项目2应该有14个VAF因子");
        
        System.out.println("数据统计验证通过 - 项目数: " + totalProjects + ", VAF因子数: " + totalVafFactors);
    }
    
    /**
     * 测试数据完整性约束
     * 验证唯一性约束和外键约束
     */
    @Test
    @Order(6)
    void testDataIntegrityConstraints() {
        // 1. 测试项目代码唯一性约束
        Project project1 = createTestProject("UNIQUE-TEST", "唯一性测试项目1");
        projectRepository.save(project1);
        entityManager.flush();
        
        // 尝试创建相同项目代码的项目
        Project project2 = createTestProject("UNIQUE-TEST", "唯一性测试项目2");
        
        // 应该抛出约束异常
        assertThrows(Exception.class, () -> {
            projectRepository.save(project2);
            entityManager.flush();
        }, "相同项目代码应该引发约束异常");
        
        System.out.println("数据完整性约束验证通过");
    }
    
    /**
     * 检查表是否存在
     */
    private boolean checkTableExists(DatabaseMetaData metaData, String tableName) throws SQLException {
        try (ResultSet tables = metaData.getTables(null, null, tableName.toUpperCase(), null)) {
            return tables.next();
        }
    }
    
    /**
     * 创建测试用的VAF因子数据
     */
    private List<VafFactor> createTestVafFactors(Project project) {
        List<VafFactor> factors = new ArrayList<>();
        String[] factorTypes = {"TF01", "TF02", "TF03", "TF04", "TF05", "TF06", "TF07",
                               "TF08", "TF09", "TF10", "TF11", "TF12", "TF13", "TF14"};
        String[] factorNames = {"数据通信", "分布式数据处理", "性能", "高度使用配置", "交易率",
                               "在线数据录入", "最终用户效率", "在线更新", "复杂处理", "重用性",
                               "安装简便性", "操作简便性", "多个场地", "变更便利性"};
        
        // 使用标准的评分：总分42，VAF=1.07
        int[] scores = {4, 3, 4, 3, 3, 4, 4, 3, 3, 2, 2, 3, 1, 3};
        
        for (int i = 0; i < 14; i++) {
            VafFactor factor = new VafFactor(project, factorTypes[i], factorNames[i]);
            factor.setInfluenceScore(scores[i]);
            factor.setWeight(BigDecimal.ONE);
            factors.add(factor);
        }
        
        return factors;
    }
    
    /**
     * 创建测试项目
     */
    private Project createTestProject(String code, String name) {
        Project project = new Project();
        project.setProjectCode(code);
        project.setProjectName(name);
        project.setProjectType("INFORMATION_SYSTEM");
        project.setProjectDescription("测试项目：" + name);
        project.setCreatedBy(1L);
        project.setUpdatedBy(1L);
        return project;
    }
}