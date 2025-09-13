# Issue #15: Flyway与PostgreSQL 15.12版本兼容性问题

## 问题描述

在执行Issue #14完整业务流程验收测试时，发现后端服务无法启动，根本原因是Flyway数据库迁移工具与PostgreSQL 15.12版本不兼容。

## 错误信息

```
Caused by: org.flywaydb.core.api.FlywayException: Unsupported Database: PostgreSQL 15.12
	at org.flywaydb.core.internal.database.DatabaseTypeRegister.lambda$getDatabaseTypeForConnection$7(DatabaseTypeRegister.java:122)
	at java.base/java.util.Optional.orElseThrow(Optional.java:403)
	at org.flywaydb.core.internal.database.DatabaseTypeRegister.getDatabaseTypeForConnection(DatabaseTypeRegister.java:122)
	at org.flywaydb.core.internal.jdbc.JdbcConnectionFactory.<init>(JdbcConnectionFactory.java:77)
	at org.flywaydb.core.FlywayExecutor.execute(FlywayExecutor.java:136)
	at org.flywaydb.core.Flyway.migrate(Flyway.java:167)
```

## 技术分析

### 当前环境
- **PostgreSQL版本**: 15.12 (Docker: postgres:15-alpine)
- **Flyway版本**: 当前项目中使用的版本不支持PostgreSQL 15.12
- **Spring Boot版本**: 3.5.0

### 问题影响
1. **阻塞性**: 后端服务完全无法启动
2. **严重程度**: P0 (最高优先级)
3. **影响范围**: 整个应用无法运行，验收测试无法进行

## 解决方案

### 方案一: 升级Flyway版本 (推荐)

在 `src/backend/pom.xml` 中添加或更新Flyway依赖：

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
    <version>9.22.3</version>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
    <version>9.22.3</version>
</dependency>
```

### 方案二: 降级PostgreSQL版本

在 `deployment/dev/docker-compose.dev.yml` 中：

```yaml
postgres:
  image: postgres:14-alpine  # 从15-alpine改为14-alpine
  # 其他配置保持不变
```

### 方案三: 禁用Flyway (临时方案)

在 `src/backend/src/main/resources/application-dev.properties` 中：

```properties
spring.flyway.enabled=false
```

**注意**: 这是临时方案，会跳过数据库迁移，可能导致数据结构问题。

## 推荐实施步骤

1. **立即修复** (采用方案一)
   ```bash
   cd src/backend
   # 更新pom.xml添加新的Flyway依赖
   ./mvnw dependency:resolve
   ./mvnw spring-boot:run -Dspring.profiles.active=dev -DskipTests
   ```

2. **验证修复**
   ```bash
   # 检查8080端口是否可访问
   curl http://localhost:8080/api/health
   ```

3. **重新执行验收测试**
   - 后端服务正常启动后
   - 重新执行Issue #14的完整验收测试

## 次要问题

同时发现测试编译错误需要修复：

### 文件: `Issue9ProjectApiPermissionTest.java`
**错误**: `andExpected` 方法找不到符号

**可能原因**:
1. Spring Test框架版本不兼容
2. 导入语句缺失
3. 方法名拼写错误

**建议检查**:
```java
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
// 确保使用 andExpect 而不是 andExpected
.andExpect(status().isOk())
```

## 验证标准

修复完成后，必须满足以下条件：

1. ✅ 后端服务正常启动 (8080端口可访问)
2. ✅ 数据库连接正常
3. ✅ Flyway迁移成功执行
4. ✅ 单元测试编译通过
5. ✅ 健康检查端点响应正常

## 优先级

**P0 - 阻塞性**: 必须立即修复，否则无法进行任何验收测试。

---

**创建人**: QA Test Engineer  
**创建时间**: 2025-09-13  
**相关Issue**: #14 (权限修复后完整业务流程验收测试)  
**影响**: 阻塞整个系统启动和验收测试