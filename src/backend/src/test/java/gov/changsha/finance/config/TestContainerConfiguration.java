package gov.changsha.finance.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;

/**
 * Testcontainers测试容器配置
 * 为政府项目提供真实数据库集成测试环境
 * 
 * 核心功能：
 * 1. 配置PostgreSQL测试容器
 * 2. 支持数据持久化验证
 * 3. 确保测试数据完整性
 * 4. 满足政府项目审计要求
 * 
 * @author QA Test Engineer
 * @version 1.0.0
 * @since 2025-09-04
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfiguration {

    /**
     * PostgreSQL测试容器配置
     * 
     * 配置说明：
     * - 使用PostgreSQL 15.4版本确保兼容性
     * - 配置持久化数据卷支持数据完整性验证
     * - 设置政府项目标准的数据库参数
     * - 启用审计日志相关配置
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15.4-alpine"))
                // 基础数据库配置
                .withDatabaseName("manday_assess_test")
                .withUsername("postgres")
                .withPassword("test_password_2025")
                
                // 性能调优配置，确保测试环境稳定性
                .withEnv("POSTGRES_INITDB_ARGS", "--encoding=UTF-8 --locale=C")
                .withEnv("POSTGRES_MAX_CONNECTIONS", "100")
                .withEnv("POSTGRES_SHARED_BUFFERS", "128MB")
                .withEnv("POSTGRES_EFFECTIVE_CACHE_SIZE", "256MB")
                
                // 审计和安全配置，满足政府项目要求
                .withEnv("POSTGRES_LOG_STATEMENT", "all")
                .withEnv("POSTGRES_LOG_MIN_MESSAGES", "info")
                .withEnv("POSTGRES_LOG_MIN_ERROR_STATEMENT", "error")
                .withEnv("POSTGRES_LOG_CONNECTIONS", "on")
                .withEnv("POSTGRES_LOG_DISCONNECTIONS", "on")
                
                // 数据完整性配置
                .withEnv("POSTGRES_FSYNC", "on")
                .withEnv("POSTGRES_SYNCHRONOUS_COMMIT", "on")
                .withEnv("POSTGRES_WAL_LEVEL", "replica")
                
                // 内存配置优化
                .withSharedMemorySize(128 * 1024 * 1024L) // 128MB
                
                // 容器重用配置，提高测试效率
                .withReuse(false) // 禁用重用确保每次测试都是全新环境
                
                // 启动等待策略
                .waitingFor(
                    org.testcontainers.containers.wait.strategy.Wait
                        .forLogMessage(".*database system is ready to accept connections.*", 2)
                        .withStartupTimeout(java.time.Duration.ofMinutes(2))
                );
    }

    /**
     * Redis测试容器配置（可选）
     * 用于缓存相关的集成测试
     * 
     * 注释原因：当前Sprint主要关注数据持久化，Redis缓存测试可在后续Sprint中启用
     */
    /*
    @Bean
    @ServiceConnection
    public GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7.2-alpine"))
                .withExposedPorts(6379)
                .withEnv("REDIS_PASSWORD", "test_redis_2025")
                .waitingFor(Wait.forLogMessage(".*Ready to accept connections.*", 1))
                .withStartupTimeout(Duration.ofMinutes(1));
    }
    */
}