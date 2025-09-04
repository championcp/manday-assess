package gov.changsha.finance.integration;

/**
 * 政府项目集成测试套件
 * 综合验证数据持久化、完整性和审计要求的测试套件
 * 
 * 测试覆盖范围：
 * 1. 数据库完整性和Schema验证
 * 2. NESMA计算结果的持久化验证
 * 3. 政府项目审计日志验证
 * 4. Testcontainers真实数据库环境测试
 * 
 * 执行方式：
 * 手动运行各个集成测试类：
 * - DatabaseIntegrityIntegrationTest: 数据库基础完整性
 * - NesmaCalculationPersistenceIntegrationTest: NESMA计算持久化
 * - AuditTrailIntegrationTest: 审计日志追踪
 * 
 * 质量保证：
 * - 政府项目100%准确性要求
 * - 数据完整性和一致性验证
 * - 审计追溯能力验证
 * - 实际数据库环境测试
 * 
 * @author QA Test Engineer
 * @version 1.0.0
 * @since 2025-09-04
 */
public class IntegrationTestSuite {
    // 注意：由于Spring Boot 2.7.18不支持JUnit 5 Platform Suite API
    // 这个类作为文档说明，实际执行需要单独运行各个测试类
    // 
    // 执行命令：
    // mvn test -Dtest="DatabaseIntegrityIntegrationTest"
    // mvn test -Dtest="NesmaCalculationPersistenceIntegrationTest"  
    // mvn test -Dtest="AuditTrailIntegrationTest"
}