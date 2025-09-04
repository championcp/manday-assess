package gov.changsha.finance.integration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testcontainers基础功能验证测试
 * 验证政府项目Testcontainers集成测试环境的基础功能
 * 
 * 测试目标：
 * 1. 验证Testcontainers PostgreSQL容器能正常启动
 * 2. 验证数据库连接和基本操作
 * 3. 验证数据持久化能力
 * 4. 为政府项目提供真实数据库测试环境验证
 * 
 * @author QA Test Engineer
 * @version 1.0.0
 * @since 2025-09-04
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestcontainersBasicTest {

    /**
     * 测试PostgreSQL容器启动和基础连接
     * 验证Testcontainers环境的基础可用性
     */
    @Test
    @Order(1)
    void testPostgreSQLContainerStartup() {
        // 创建PostgreSQL容器
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:15.4-alpine"))
                .withDatabaseName("manday_assess_test")
                .withUsername("postgres")
                .withPassword("test_password")) {
            
            // 启动容器
            postgres.start();
            
            // 验证容器状态
            assertTrue(postgres.isRunning(), "PostgreSQL容器应该正在运行");
            assertNotNull(postgres.getJdbcUrl(), "JDBC URL不应为空");
            assertNotNull(postgres.getUsername(), "用户名不应为空");
            assertNotNull(postgres.getPassword(), "密码不应为空");
            
            // 验证数据库连接
            try (Connection connection = postgres.createConnection("")) {
                assertNotNull(connection, "数据库连接不应为空");
                assertFalse(connection.isClosed(), "数据库连接应该是活跃的");
                
                // 验证数据库类型和版本
                DatabaseMetaData metaData = connection.getMetaData();
                assertEquals("PostgreSQL", metaData.getDatabaseProductName());
                
                System.out.println("PostgreSQL容器启动成功!");
                System.out.println("JDBC URL: " + postgres.getJdbcUrl());
                System.out.println("数据库版本: " + metaData.getDatabaseProductVersion());
            }
        } catch (Exception e) {
            fail("PostgreSQL容器启动或连接失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试基础数据库操作
     * 验证能够在容器中执行SQL操作
     */
    @Test
    @Order(2)
    void testBasicDatabaseOperations() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:15.4-alpine"))
                .withDatabaseName("test_db")
                .withUsername("test_user")
                .withPassword("test_pass")) {
            
            postgres.start();
            
            try (Connection connection = postgres.createConnection("")) {
                // 创建测试表
                try (Statement stmt = connection.createStatement()) {
                    stmt.execute("CREATE TABLE test_projects (" +
                               "id SERIAL PRIMARY KEY, " +
                               "project_code VARCHAR(50) UNIQUE NOT NULL, " +
                               "project_name VARCHAR(200) NOT NULL, " +
                               "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                               ")");
                    
                    System.out.println("测试表创建成功");
                }
                
                // 插入测试数据
                try (Statement stmt = connection.createStatement()) {
                    int rowsInserted = stmt.executeUpdate(
                        "INSERT INTO test_projects (project_code, project_name) " +
                        "VALUES ('TEST-001', '测试项目1'), ('TEST-002', '测试项目2')"
                    );
                    assertEquals(2, rowsInserted, "应该插入2行数据");
                    
                    System.out.println("测试数据插入成功");
                }
                
                // 查询数据验证
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM test_projects")) {
                    
                    assertTrue(rs.next(), "查询结果不应为空");
                    assertEquals(2, rs.getInt(1), "应该查询到2行数据");
                    
                    System.out.println("数据查询验证成功");
                }
                
                // 验证数据完整性约束
                try (Statement stmt = connection.createStatement()) {
                    // 尝试插入重复的项目代码，应该失败
                    assertThrows(Exception.class, () -> {
                        stmt.executeUpdate(
                            "INSERT INTO test_projects (project_code, project_name) " +
                            "VALUES ('TEST-001', '重复项目')"
                        );
                    }, "重复项目代码应该引发约束异常");
                    
                    System.out.println("数据完整性约束验证成功");
                }
                
            }
        } catch (Exception e) {
            fail("数据库操作测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试容器数据持久化和多次连接
     * 验证在容器生命周期内数据的持久性
     */
    @Test
    @Order(3)
    void testDataPersistenceWithinContainer() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:15.4-alpine"))
                .withDatabaseName("persistence_test")
                .withUsername("postgres")
                .withPassword("test123")) {
            
            postgres.start();
            
            // 第一次连接：创建数据
            try (Connection connection1 = postgres.createConnection("")) {
                try (Statement stmt = connection1.createStatement()) {
                    stmt.execute("CREATE TABLE persistence_test (" +
                               "id SERIAL PRIMARY KEY, " +
                               "test_data VARCHAR(100)" +
                               ")");
                    
                    stmt.executeUpdate("INSERT INTO persistence_test (test_data) VALUES ('持久化测试数据')");
                }
                System.out.println("第一次连接：数据创建成功");
            }
            
            // 第二次连接：验证数据仍然存在
            try (Connection connection2 = postgres.createConnection("")) {
                try (Statement stmt = connection2.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT test_data FROM persistence_test WHERE id = 1")) {
                    
                    assertTrue(rs.next(), "应该能查询到插入的数据");
                    assertEquals("持久化测试数据", rs.getString("test_data"), "数据内容应该正确");
                }
                System.out.println("第二次连接：数据持久性验证成功");
            }
            
            // 第三次连接：追加更多数据
            try (Connection connection3 = postgres.createConnection("")) {
                try (Statement stmt = connection3.createStatement()) {
                    stmt.executeUpdate("INSERT INTO persistence_test (test_data) VALUES ('追加数据1'), ('追加数据2')");
                    
                    ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM persistence_test");
                    rs.next();
                    assertEquals(3, rs.getInt(1), "应该有3条数据记录");
                }
                System.out.println("第三次连接：数据追加和统计验证成功");
            }
            
        } catch (Exception e) {
            fail("数据持久化测试失败: " + e.getMessage());
        }
    }
    
    /**
     * 测试政府项目模拟场景
     * 模拟政府项目NESMA评估的数据结构
     */
    @Test
    @Order(4)
    void testGovernmentProjectSimulation() {
        try (PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("postgres:15.4-alpine"))
                .withDatabaseName("gov_nesma_test")
                .withUsername("gov_user")
                .withPassword("secure_pass_2025")) {
            
            postgres.start();
            
            try (Connection connection = postgres.createConnection("")) {
                // 创建政府项目表结构
                try (Statement stmt = connection.createStatement()) {
                    // 项目表
                    stmt.execute("CREATE TABLE gov_projects (" +
                               "id SERIAL PRIMARY KEY, " +
                               "project_code VARCHAR(50) UNIQUE NOT NULL, " +
                               "project_name VARCHAR(200) NOT NULL, " +
                               "project_type VARCHAR(50) NOT NULL, " +
                               "department VARCHAR(100), " +
                               "budget_amount DECIMAL(19,4), " +
                               "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                               "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                               ")");
                    
                    // VAF因子表
                    stmt.execute("CREATE TABLE vaf_factors (" +
                               "id SERIAL PRIMARY KEY, " +
                               "project_id INTEGER NOT NULL, " +
                               "factor_type VARCHAR(10) NOT NULL, " +
                               "factor_name VARCHAR(100) NOT NULL, " +
                               "influence_score INTEGER CHECK (influence_score >= 0 AND influence_score <= 5), " +
                               "weight DECIMAL(8,4) DEFAULT 1.0000, " +
                               "FOREIGN KEY (project_id) REFERENCES gov_projects(id)" +
                               ")");
                    
                    System.out.println("政府项目表结构创建成功");
                }
                
                // 插入模拟政府项目数据
                try (Statement stmt = connection.createStatement()) {
                    // 插入项目
                    stmt.executeUpdate(
                        "INSERT INTO gov_projects (project_code, project_name, project_type, department, budget_amount) " +
                        "VALUES ('GOV-CHANGSHA-2025-001', '长沙市财政评审系统NESMA评估', 'INFORMATION_SYSTEM', " +
                        "'长沙市财政局', 2500000.0000)"
                    );
                    
                    // 插入VAF因子（模拟PDF案例的14个标准因子）
                    String[] factors = {"TF01", "TF02", "TF03", "TF04", "TF05", "TF06", "TF07",
                                       "TF08", "TF09", "TF10", "TF11", "TF12", "TF13", "TF14"};
                    String[] names = {"数据通信", "分布式数据处理", "性能", "高度使用配置", "交易率",
                                     "在线数据录入", "最终用户效率", "在线更新", "复杂处理", "重用性",
                                     "安装简便性", "操作简便性", "多个场地", "变更便利性"};
                    int[] scores = {4, 3, 4, 3, 3, 4, 4, 3, 3, 2, 2, 3, 1, 3}; // PDF案例评分
                    
                    for (int i = 0; i < 14; i++) {
                        stmt.executeUpdate(String.format(
                            "INSERT INTO vaf_factors (project_id, factor_type, factor_name, influence_score) " +
                            "VALUES (1, '%s', '%s', %d)",
                            factors[i], names[i], scores[i]
                        ));
                    }
                    
                    System.out.println("政府项目模拟数据插入成功");
                }
                
                // 验证政府项目数据完整性
                try (Statement stmt = connection.createStatement()) {
                    // 验证项目数据
                    ResultSet rs = stmt.executeQuery(
                        "SELECT project_code, department, budget_amount FROM gov_projects WHERE id = 1"
                    );
                    assertTrue(rs.next(), "应该查询到政府项目数据");
                    assertEquals("GOV-CHANGSHA-2025-001", rs.getString("project_code"));
                    assertEquals("长沙市财政局", rs.getString("department"));
                    
                    // 验证VAF因子完整性
                    rs = stmt.executeQuery("SELECT COUNT(*) FROM vaf_factors WHERE project_id = 1");
                    rs.next();
                    assertEquals(14, rs.getInt(1), "应该有14个VAF因子");
                    
                    // 验证VAF总评分（应该是42，对应PDF案例）
                    rs = stmt.executeQuery("SELECT SUM(influence_score) FROM vaf_factors WHERE project_id = 1");
                    rs.next();
                    assertEquals(42, rs.getInt(1), "VAF总评分应该为42（PDF案例标准）");
                    
                    System.out.println("政府项目数据完整性验证成功 - VAF总评分: 42");
                }
                
            }
        } catch (Exception e) {
            fail("政府项目模拟测试失败: " + e.getMessage());
        }
    }
}