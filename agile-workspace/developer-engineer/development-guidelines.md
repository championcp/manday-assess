# 软件规模评估系统 - 开发规范与实施指南

## 一、开发环境搭建指南

### 1. 环境依赖要求

#### 开发环境基础要求
```
操作系统：
- Windows 10+ 或 macOS 10.15+ 或 Ubuntu 18.04+
- 内存：至少 8GB，推荐 16GB
- 硬盘：至少 50GB 可用空间

必备软件：
- Git 2.30+
- Docker Desktop 4.0+
- Docker Compose 2.0+
- Visual Studio Code 或 IntelliJ IDEA

前端开发环境：
- Node.js 18.x LTS
- npm 8.x 或 yarn 1.22+
- Vue CLI 5.x

后端开发环境：
- JDK 17 (推荐 OpenJDK 或 Oracle JDK)
- Maven 3.8+
- PostgreSQL 14+ (可用Docker)
- Redis 7+ (可用Docker)
```

#### 开发工具配置

**VS Code插件推荐：**
```json
{
  "recommendations": [
    "Vue.volar",
    "Vue.vscode-typescript-vue-plugin", 
    "bradlc.vscode-tailwindcss",
    "esbenp.prettier-vscode",
    "dbaeumer.vscode-eslint",
    "ms-vscode.vscode-typescript-next",
    "humao.rest-client"
  ]
}
```

**IntelliJ IDEA插件推荐：**
```
必装插件：
- Vue.js
- Docker
- Kubernetes
- SonarLint
- Checkstyle-IDEA
- FindBugs-IDEA
- Spring Assistant
```

### 2. 项目初始化步骤

#### 环境搭建脚本
```bash
#!/bin/bash
# setup-dev-environment.sh

echo "开始搭建开发环境..."

# 1. 检查必备软件
echo "检查环境依赖..."
command -v git >/dev/null 2>&1 || { echo "Git 未安装" >&2; exit 1; }
command -v docker >/dev/null 2>&1 || { echo "Docker 未安装" >&2; exit 1; }
command -v node >/dev/null 2>&1 || { echo "Node.js 未安装" >&2; exit 1; }
command -v java >/dev/null 2>&1 || { echo "Java 未安装" >&2; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "Maven 未安装" >&2; exit 1; }

# 2. 启动数据库服务
echo "启动数据库服务..."
cd docker-dev
docker-compose up -d postgres redis minio
cd ..

# 3. 安装前端依赖
echo "安装前端依赖..."
cd frontend
npm install
cd ..

# 4. 安装后端依赖
echo "安装后端依赖..."
cd backend
mvn dependency:resolve
cd ..

# 5. 初始化数据库
echo "初始化数据库..."
cd backend
mvn flyway:migrate -Dflyway.profiles=development
cd ..

echo "开发环境搭建完成！"
echo "前端开发服务器: npm run dev (端口 3000)"
echo "后端开发服务器: mvn spring-boot:run (端口 8080)"
```

#### Docker开发环境配置
```yaml
# docker-dev/docker-compose.yml
version: '3.8'

services:
  postgres:
    image: postgres:14-alpine
    container_name: assessment-postgres-dev
    environment:
      POSTGRES_DB: assessment_dev
      POSTGRES_USER: dev_user
      POSTGRES_PASSWORD: dev_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_dev_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    networks:
      - dev-network

  redis:
    image: redis:7-alpine
    container_name: assessment-redis-dev
    ports:
      - "6379:6379"
    volumes:
      - redis_dev_data:/data
    networks:
      - dev-network

  minio:
    image: minio/minio:latest
    container_name: assessment-minio-dev
    environment:
      MINIO_ROOT_USER: minioadmin
      MINIO_ROOT_PASSWORD: minioadmin123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - minio_dev_data:/data
    command: server /data --console-address ":9001"
    networks:
      - dev-network

  mailhog:
    image: mailhog/mailhog:latest
    container_name: assessment-mailhog-dev
    ports:
      - "1025:1025"  # SMTP
      - "8025:8025"  # Web UI
    networks:
      - dev-network

volumes:
  postgres_dev_data:
  redis_dev_data:
  minio_dev_data:

networks:
  dev-network:
    driver: bridge
```

## 二、编码规范标准

### 1. 前端编码规范

#### TypeScript/Vue 3编码规范
```typescript
// 1. 组件命名：使用PascalCase
// ✅ 正确
export default defineComponent({
  name: 'FunctionPointCalculator'
});

// ❌ 错误
export default defineComponent({
  name: 'functionPointCalculator'
});

// 2. 变量命名：使用camelCase
// ✅ 正确
const calculationResult = ref<CalculationResult>();
const isCalculating = ref(false);

// ❌ 错误
const calculation_result = ref<CalculationResult>();
const IsCalculating = ref(false);

// 3. 常量命名：使用SCREAMING_SNAKE_CASE
// ✅ 正确
const MAX_FUNCTION_POINTS = 1000;
const CALCULATION_TIMEOUT = 30000;

// ❌ 错误
const maxFunctionPoints = 1000;
const calculationTimeout = 30000;

// 4. 类型定义：使用PascalCase，接口以I开头
// ✅ 正确
interface ICalculationRequest {
  projectId: number;
  calculationType: CalculationType;
  data: CalculationData;
}

type CalculationStatus = 'pending' | 'processing' | 'completed' | 'error';

// 5. 函数定义：必须明确返回类型
// ✅ 正确
const calculateFunctionPoints = (data: ILFData): Promise<CalculationResult> => {
  return calculationApi.calculate(data);
};

// ❌ 错误
const calculateFunctionPoints = (data) => {
  return calculationApi.calculate(data);
};

// 6. 组件Props定义：使用TypeScript接口
interface Props {
  projectId: number;
  readonly?: boolean;
  onCalculationComplete?: (result: CalculationResult) => void;
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false
});

// 7. 组合式函数命名：以use开头
const useCalculation = () => {
  const isLoading = ref(false);
  const error = ref<string | null>(null);
  
  const calculate = async (data: CalculationData): Promise<CalculationResult> => {
    isLoading.value = true;
    error.value = null;
    
    try {
      const result = await calculationApi.calculate(data);
      return result;
    } catch (err) {
      error.value = err instanceof Error ? err.message : '计算失败';
      throw err;
    } finally {
      isLoading.value = false;
    }
  };
  
  return {
    isLoading: readonly(isLoading),
    error: readonly(error),
    calculate
  };
};

// 8. 错误处理：统一的错误处理模式
const handleApiError = (error: unknown): string => {
  if (error instanceof ApiError) {
    return error.message;
  }
  if (error instanceof Error) {
    return error.message;
  }
  return '未知错误';
};
```

#### CSS/SCSS编码规范
```scss
// 1. 类命名：使用kebab-case
// ✅ 正确
.function-point-calculator {
  .calculation-form {
    .form-group {
      margin-bottom: 1rem;
      
      .form-label {
        font-weight: 500;
        color: var(--text-primary);
      }
      
      .form-input {
        border: 1px solid var(--border-color);
        border-radius: 4px;
        
        &:focus {
          border-color: var(--primary-color);
          box-shadow: 0 0 0 3px var(--primary-color-alpha);
        }
        
        &.error {
          border-color: var(--error-color);
        }
      }
    }
  }
}

// 2. 变量命名：使用kebab-case
:root {
  --primary-color: #1e3a8a;
  --primary-color-alpha: rgba(30, 58, 138, 0.1);
  --secondary-color: #1e40af;
  --text-primary: #111827;
  --text-secondary: #374151;
  --border-color: #e5e7eb;
  --error-color: #dc2626;
  --success-color: #16a34a;
  --warning-color: #d97706;
}

// 3. 媒体查询：使用混入
@mixin mobile {
  @media (max-width: 768px) {
    @content;
  }
}

@mixin tablet {
  @media (min-width: 769px) and (max-width: 1024px) {
    @content;
  }
}

@mixin desktop {
  @media (min-width: 1025px) {
    @content;
  }
}

// 使用示例
.responsive-layout {
  display: grid;
  grid-template-columns: 1fr;
  
  @include tablet {
    grid-template-columns: 1fr 1fr;
  }
  
  @include desktop {
    grid-template-columns: 1fr 2fr 1fr;
  }
}
```

### 2. 后端编码规范

#### Java编码规范
```java
/**
 * 1. 类命名：使用PascalCase
 * 类注释：必须包含类的用途、作者、创建时间
 * 
 * @author Developer Team
 * @since 1.0.0
 * @description 功能点计算服务实现类，负责NESMA方法的功能点计算
 */
@Service
@Transactional(readOnly = true)
@Slf4j
public class FunctionPointCalculationServiceImpl implements FunctionPointCalculationService {
    
    // 2. 常量命名：使用SCREAMING_SNAKE_CASE
    private static final int MAX_COMPLEXITY_LEVEL = 3;
    private static final String DEFAULT_CALCULATION_TYPE = "NESMA";
    private static final BigDecimal PRECISION_SCALE = new BigDecimal("0.01");
    
    // 3. 字段命名：使用camelCase
    private final FunctionPointRepository functionPointRepository;
    private final CalculationEngine calculationEngine;
    private final ValidationService validationService;
    private final AuditService auditService;
    
    /**
     * 构造函数：使用构造函数注入
     */
    public FunctionPointCalculationServiceImpl(
            FunctionPointRepository functionPointRepository,
            CalculationEngine calculationEngine,
            ValidationService validationService,
            AuditService auditService) {
        this.functionPointRepository = functionPointRepository;
        this.calculationEngine = calculationEngine;
        this.validationService = validationService;
        this.auditService = auditService;
    }
    
    /**
     * 4. 方法命名：使用camelCase，动词开头
     * 方法注释：必须包含参数说明、返回值说明、异常说明
     * 
     * @param request 计算请求数据
     * @return 计算结果
     * @throws ValidationException 当请求数据验证失败时抛出
     * @throws CalculationException 当计算过程出现错误时抛出
     */
    @Override
    @Transactional
    public CalculationResult calculateFunctionPoints(CalculationRequest request) 
            throws ValidationException, CalculationException {
        
        // 5. 方法体：单一职责，每个方法不超过50行
        log.info("开始计算功能点，项目ID: {}, 计算类型: {}", 
                request.getProjectId(), request.getCalculationType());
        
        // 6. 参数验证：使用专门的验证服务
        ValidationResult validation = validationService.validateCalculationRequest(request);
        if (!validation.isValid()) {
            throw new ValidationException("请求参数验证失败: " + validation.getErrorMessage());
        }
        
        // 7. 业务逻辑：清晰的步骤划分
        try {
            // 获取项目数据
            ProjectData projectData = getProjectData(request.getProjectId());
            
            // 执行计算
            CalculationResult result = performCalculation(request, projectData);
            
            // 保存结果
            saveCalculationResult(result);
            
            // 记录审计日志
            auditService.recordCalculation(request.getProjectId(), result);
            
            log.info("功能点计算完成，项目ID: {}, 总功能点: {}", 
                    request.getProjectId(), result.getTotalFunctionPoints());
            
            return result;
            
        } catch (Exception e) {
            log.error("功能点计算失败，项目ID: {}", request.getProjectId(), e);
            throw new CalculationException("计算过程发生错误: " + e.getMessage(), e);
        }
    }
    
    /**
     * 8. 私有方法：清晰的职责划分
     */
    private ProjectData getProjectData(Long projectId) throws DataNotFoundException {
        return functionPointRepository.findProjectDataById(projectId)
                .orElseThrow(() -> new DataNotFoundException("项目数据不存在: " + projectId));
    }
    
    private CalculationResult performCalculation(CalculationRequest request, ProjectData projectData) 
            throws CalculationException {
        
        // 9. 计算逻辑：使用BigDecimal确保精度
        BigDecimal totalPoints = BigDecimal.ZERO;
        
        // 计算内部逻辑文件功能点
        if (request.getIlfData() != null) {
            BigDecimal ilfPoints = calculationEngine.calculateILF(request.getIlfData());
            totalPoints = totalPoints.add(ilfPoints);
        }
        
        // 计算外部接口文件功能点
        if (request.getEifData() != null) {
            BigDecimal eifPoints = calculationEngine.calculateEIF(request.getEifData());
            totalPoints = totalPoints.add(eifPoints);
        }
        
        // 其他计算逻辑...
        
        return CalculationResult.builder()
                .projectId(request.getProjectId())
                .totalFunctionPoints(totalPoints.intValue())
                .calculationDetail(buildCalculationDetail(request))
                .calculatedAt(LocalDateTime.now())
                .calculatedBy(SecurityUtils.getCurrentUserId())
                .build();
    }
    
    private void saveCalculationResult(CalculationResult result) {
        FunctionPointCalculation entity = CalculationMapper.toEntity(result);
        functionPointRepository.save(entity);
    }
}

/**
 * 10. 异常类定义：明确的异常层次结构
 */
@Getter
public class CalculationException extends BusinessException {
    
    private final String errorCode;
    
    public CalculationException(String message) {
        super(message);
        this.errorCode = "CALC_ERROR";
    }
    
    public CalculationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CALC_ERROR";
    }
    
    public CalculationException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
```

#### 数据库编码规范
```sql
-- 1. 表命名：使用snake_case，复数形式
CREATE TABLE function_point_calculations (
    -- 2. 主键命名：统一使用id
    id BIGSERIAL PRIMARY KEY,
    
    -- 3. 外键命名：使用表名_id格式
    project_id BIGINT NOT NULL REFERENCES projects(id),
    
    -- 4. 字段命名：使用snake_case，具有描述性
    calculation_type VARCHAR(10) NOT NULL COMMENT '计算类型',
    module_name VARCHAR(100) NOT NULL COMMENT '模块名称',
    complexity_level VARCHAR(10) NOT NULL COMMENT '复杂度等级',
    data_elements INTEGER COMMENT '数据元素项',
    record_types INTEGER COMMENT '记录元素类型',
    file_types INTEGER COMMENT '文件类型',
    function_points INTEGER NOT NULL COMMENT '功能点数',
    
    -- 5. JSON字段：用于存储复杂数据结构
    calculation_detail JSONB COMMENT '计算详情JSON数据',
    
    -- 6. 时间戳字段：统一命名
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
    
    -- 7. 约束命名：使用描述性名称
    CONSTRAINT chk_complexity_level 
        CHECK (complexity_level IN ('LOW', 'MEDIUM', 'HIGH')),
    CONSTRAINT chk_function_points_positive 
        CHECK (function_points >= 0)
);

-- 8. 索引命名：使用idx_表名_字段名格式
CREATE INDEX idx_fp_calculations_project_id 
    ON function_point_calculations(project_id);
CREATE INDEX idx_fp_calculations_type 
    ON function_point_calculations(calculation_type);
CREATE INDEX idx_fp_calculations_created_at 
    ON function_point_calculations(created_at);

-- 9. 视图命名：使用v_开头
CREATE VIEW v_calculation_summary AS
SELECT 
    p.id AS project_id,
    p.project_name,
    COUNT(fpc.id) AS calculation_count,
    SUM(fpc.function_points) AS total_function_points,
    MAX(fpc.created_at) AS last_calculation_date
FROM projects p
LEFT JOIN function_point_calculations fpc ON p.id = fpc.project_id
GROUP BY p.id, p.project_name;

-- 10. 存储过程命名：使用sp_开头
CREATE OR REPLACE FUNCTION sp_calculate_project_summary(
    p_project_id BIGINT
) RETURNS TABLE (
    calculation_type VARCHAR(10),
    total_points INTEGER,
    calculation_count INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        fpc.calculation_type,
        SUM(fpc.function_points)::INTEGER AS total_points,
        COUNT(fpc.id)::INTEGER AS calculation_count
    FROM function_point_calculations fpc
    WHERE fpc.project_id = p_project_id
    GROUP BY fpc.calculation_type;
END;
$$ LANGUAGE plpgsql;
```

## 三、代码质量控制

### 1. 静态代码分析

#### 前端质量检查配置
```json
// package.json - scripts配置
{
  "scripts": {
    "lint": "eslint src --ext .vue,.js,.jsx,.ts,.tsx",
    "lint:fix": "eslint src --ext .vue,.js,.jsx,.ts,.tsx --fix",
    "type-check": "vue-tsc --noEmit",
    "format": "prettier --write src/**/*.{vue,js,jsx,ts,tsx,css,scss,md}",
    "quality-check": "npm run lint && npm run type-check && npm run test:unit"
  }
}

// .eslintrc.js - ESLint配置
module.exports = {
  extends: [
    '@vue/typescript/recommended',
    '@vue/prettier',
    'plugin:vue/vue3-recommended'
  ],
  rules: {
    // TypeScript相关规则
    '@typescript-eslint/no-unused-vars': ['error', { argsIgnorePattern: '^_' }],
    '@typescript-eslint/explicit-function-return-type': 'warn',
    '@typescript-eslint/no-explicit-any': 'error',
    '@typescript-eslint/prefer-nullish-coalescing': 'error',
    '@typescript-eslint/prefer-optional-chain': 'error',
    
    // Vue相关规则
    'vue/component-name-in-template-casing': ['error', 'PascalCase'],
    'vue/prop-name-casing': ['error', 'camelCase'],
    'vue/component-definition-name-casing': ['error', 'PascalCase'],
    'vue/custom-event-name-casing': ['error', 'camelCase'],
    'vue/no-unused-components': 'error',
    'vue/no-unused-vars': 'error',
    
    // 通用规则
    'no-console': 'warn',
    'no-debugger': 'error',
    'prefer-const': 'error',
    'no-var': 'error'
  }
};

// jest.config.js - 测试配置
module.exports = {
  preset: '@vue/cli-plugin-unit-jest/presets/typescript-and-babel',
  testMatch: [
    '<rootDir>/src/**/__tests__/**/*.spec.{js,jsx,ts,tsx}',
    '<rootDir>/tests/unit/**/*.spec.{js,jsx,ts,tsx}'
  ],
  collectCoverageFrom: [
    'src/**/*.{js,jsx,ts,tsx,vue}',
    '!src/main.ts',
    '!src/router/index.ts',
    '!**/*.d.ts'
  ],
  coverageThreshold: {
    global: {
      branches: 80,
      functions: 80,
      lines: 80,
      statements: 80
    }
  }
};
```

#### 后端质量检查配置
```xml
<!-- pom.xml - Maven插件配置 -->
<plugins>
    <!-- Checkstyle插件 -->
    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-checkstyle-plugin</artifactId>
        <version>3.1.2</version>
        <configuration>
            <configLocation>checkstyle.xml</configLocation>
            <encoding>UTF-8</encoding>
            <consoleOutput>true</consoleOutput>
            <failsOnError>true</failsOnError>
        </configuration>
        <executions>
            <execution>
                <id>validate</id>
                <phase>validate</phase>
                <goals>
                    <goal>check</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

    <!-- SpotBugs插件 -->
    <plugin>
        <groupId>com.github.spotbugs</groupId>
        <artifactId>spotbugs-maven-plugin</artifactId>
        <version>4.7.3.0</version>
        <configuration>
            <effort>Max</effort>
            <threshold>Low</threshold>
            <xmlOutput>true</xmlOutput>
        </configuration>
        <executions>
            <execution>
                <id>analyze-compile</id>
                <phase>compile</phase>
                <goals>
                    <goal>check</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

    <!-- JaCoCo代码覆盖率插件 -->
    <plugin>
        <groupId>org.jacoco</groupId>
        <artifactId>jacoco-maven-plugin</artifactId>
        <version>0.8.8</version>
        <executions>
            <execution>
                <goals>
                    <goal>prepare-agent</goal>
                </goals>
            </execution>
            <execution>
                <id>report</id>
                <phase>test</phase>
                <goals>
                    <goal>report</goal>
                </goals>
            </execution>
            <execution>
                <id>check</id>
                <phase>test</phase>
                <goals>
                    <goal>check</goal>
                </goals>
                <configuration>
                    <rules>
                        <rule>
                            <element>CLASS</element>
                            <limits>
                                <limit>
                                    <counter>LINE</counter>
                                    <value>COVEREDRATIO</value>
                                    <minimum>80%</minimum>
                                </limit>
                            </limits>
                        </rule>
                    </rules>
                </configuration>
            </execution>
        </executions>
    </plugin>
</plugins>
```

### 2. 单元测试规范

#### 前端测试规范
```typescript
// tests/unit/composables/useCalculation.spec.ts
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useCalculation } from '@/composables/useCalculation';
import * as calculationApi from '@/api/calculation';

// Mock API
vi.mock('@/api/calculation');
const mockedCalculationApi = vi.mocked(calculationApi);

describe('useCalculation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('calculate', () => {
    it('应该成功执行计算并返回结果', async () => {
      // Arrange
      const mockData = {
        projectId: 1,
        calculationType: 'ILF',
        data: { /* 测试数据 */ }
      };
      
      const mockResult = {
        functionPoints: 100,
        calculationDetail: { /* 计算详情 */ }
      };

      mockedCalculationApi.calculate.mockResolvedValue(mockResult);

      // Act
      const { calculate, isLoading } = useCalculation();
      const result = await calculate(mockData);

      // Assert
      expect(mockedCalculationApi.calculate).toHaveBeenCalledWith(mockData);
      expect(result).toEqual(mockResult);
      expect(isLoading.value).toBe(false);
    });

    it('应该正确处理计算错误', async () => {
      // Arrange
      const mockData = { /* 测试数据 */ };
      const mockError = new Error('计算失败');
      
      mockedCalculationApi.calculate.mockRejectedValue(mockError);

      // Act & Assert
      const { calculate, error } = useCalculation();
      
      await expect(calculate(mockData)).rejects.toThrow('计算失败');
      expect(error.value).toBe('计算失败');
    });
  });
});

// tests/unit/components/FunctionPointCalculator.spec.ts
import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import FunctionPointCalculator from '@/components/business/FunctionPointCalculator.vue';

describe('FunctionPointCalculator', () => {
  it('应该正确渲染计算表单', () => {
    // Arrange & Act
    const wrapper = mount(FunctionPointCalculator, {
      props: {
        projectId: 1
      }
    });

    // Assert
    expect(wrapper.find('.function-point-calculator').exists()).toBe(true);
    expect(wrapper.find('.calculation-form').exists()).toBe(true);
    expect(wrapper.find('[data-testid="calculate-button"]').exists()).toBe(true);
  });

  it('应该在表单提交时触发计算', async () => {
    // Arrange
    const mockCalculate = vi.fn();
    const wrapper = mount(FunctionPointCalculator, {
      props: {
        projectId: 1,
        onCalculate: mockCalculate
      }
    });

    // Act
    await wrapper.find('[data-testid="calculate-button"]').trigger('click');

    // Assert
    expect(mockCalculate).toHaveBeenCalled();
  });
});
```

#### 后端测试规范
```java
// src/test/java/com/government/assessment/service/FunctionPointCalculationServiceTest.java

/**
 * 功能点计算服务单元测试
 * 
 * 测试命名规范：
 * - 测试方法名格式：should_ExpectedBehavior_When_StateUnderTest
 * - 测试类名格式：ClassNameTest
 */
@ExtendWith(MockitoExtension.class)
class FunctionPointCalculationServiceTest {
    
    @Mock
    private FunctionPointRepository functionPointRepository;
    
    @Mock
    private CalculationEngine calculationEngine;
    
    @Mock
    private ValidationService validationService;
    
    @Mock
    private AuditService auditService;
    
    @InjectMocks
    private FunctionPointCalculationServiceImpl calculationService;
    
    private CalculationRequest validRequest;
    private ProjectData mockProjectData;
    
    @BeforeEach
    void setUp() {
        // 准备测试数据
        validRequest = CalculationRequest.builder()
            .projectId(1L)
            .calculationType("ILF")
            .ilfData(createMockILFData())
            .build();
            
        mockProjectData = createMockProjectData();
    }
    
    @Test
    @DisplayName("应该成功计算功能点并返回结果")
    void should_ReturnCalculationResult_When_ValidRequestProvided() {
        // Given
        ValidationResult validValidation = ValidationResult.valid();
        CalculationResult expectedResult = createMockCalculationResult();
        
        when(validationService.validateCalculationRequest(validRequest))
            .thenReturn(validValidation);
        when(functionPointRepository.findProjectDataById(1L))
            .thenReturn(Optional.of(mockProjectData));
        when(calculationEngine.calculateILF(any(ILFData.class)))
            .thenReturn(BigDecimal.valueOf(100));
            
        // When
        CalculationResult actualResult = calculationService.calculateFunctionPoints(validRequest);
        
        // Then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getTotalFunctionPoints()).isEqualTo(100);
        assertThat(actualResult.getProjectId()).isEqualTo(1L);
        
        // 验证方法调用
        verify(validationService).validateCalculationRequest(validRequest);
        verify(functionPointRepository).findProjectDataById(1L);
        verify(calculationEngine).calculateILF(any(ILFData.class));
        verify(auditService).recordCalculation(1L, actualResult);
    }
    
    @Test
    @DisplayName("应该在验证失败时抛出ValidationException")
    void should_ThrowValidationException_When_RequestValidationFails() {
        // Given
        ValidationResult invalidValidation = ValidationResult.invalid("请求参数无效");
        when(validationService.validateCalculationRequest(validRequest))
            .thenReturn(invalidValidation);
            
        // When & Then
        ValidationException exception = assertThrows(
            ValidationException.class,
            () -> calculationService.calculateFunctionPoints(validRequest)
        );
        
        assertThat(exception.getMessage()).contains("请求参数验证失败");
        
        // 验证没有执行后续操作
        verify(functionPointRepository, never()).findProjectDataById(anyLong());
        verify(calculationEngine, never()).calculateILF(any());
    }
    
    @Test
    @DisplayName("应该在项目不存在时抛出DataNotFoundException")
    void should_ThrowDataNotFoundException_When_ProjectNotExists() {
        // Given
        ValidationResult validValidation = ValidationResult.valid();
        when(validationService.validateCalculationRequest(validRequest))
            .thenReturn(validValidation);
        when(functionPointRepository.findProjectDataById(1L))
            .thenReturn(Optional.empty());
            
        // When & Then
        DataNotFoundException exception = assertThrows(
            DataNotFoundException.class,
            () -> calculationService.calculateFunctionPoints(validRequest)
        );
        
        assertThat(exception.getMessage()).contains("项目数据不存在: 1");
    }
    
    // 辅助方法
    private ILFData createMockILFData() {
        return ILFData.builder()
            .moduleName("用户管理")
            .dataElements(10)
            .recordTypes(3)
            .build();
    }
    
    private ProjectData createMockProjectData() {
        return ProjectData.builder()
            .id(1L)
            .projectName("测试项目")
            .projectCode("TEST001")
            .build();
    }
    
    private CalculationResult createMockCalculationResult() {
        return CalculationResult.builder()
            .projectId(1L)
            .totalFunctionPoints(100)
            .calculatedAt(LocalDateTime.now())
            .build();
    }
}

// 集成测试示例
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class CalculationControllerIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:14-alpine")
        .withDatabaseName("assessment_test")
        .withUsername("test")
        .withPassword("test");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void should_CreateCalculation_When_ValidRequestProvided() {
        // 集成测试实现
    }
}
```

## 四、Git工作流规范

### 1. 分支管理策略

#### GitFlow分支模型实施
```bash
# 1. 主分支设置
git branch -M main                    # 主分支（生产环境）
git checkout -b develop               # 开发分支

# 2. 功能开发流程
git checkout develop
git pull origin develop
git checkout -b feature/function-point-calculation
# 开发功能...
git add .
git commit -m "feat(calculation): 实现功能点计算核心逻辑"
git push origin feature/function-point-calculation
# 创建PR到develop分支

# 3. 发布流程
git checkout develop
git pull origin develop
git checkout -b release/v1.0.0
# 发布准备工作...
git commit -m "chore(release): 准备v1.0.0发布"
git checkout main
git merge release/v1.0.0
git tag -a v1.0.0 -m "发布版本v1.0.0"
git push origin main --tags

# 4. 热修复流程
git checkout main
git pull origin main
git checkout -b hotfix/critical-bug-fix
# 修复bug...
git commit -m "fix(calculation): 修复功能点计算精度问题"
git checkout main
git merge hotfix/critical-bug-fix
git checkout develop
git merge hotfix/critical-bug-fix
```

### 2. 提交消息规范

#### Conventional Commits规范
```bash
# 提交消息格式
<type>(<scope>): <subject>

<body>

<footer>

# 类型说明
feat:     新功能
fix:      错误修复
docs:     文档更新
style:    代码格式调整（不影响功能）
refactor: 代码重构（不修复错误也不添加功能）
test:     添加或修改测试
chore:    构建过程或辅助工具的变动
perf:     性能优化
ci:       CI/CD配置变更

# 示例
feat(calculation): 实现NESMA功能点计算引擎

- 添加ILF、EIF、EI、EO、EQ五种类型的计算逻辑
- 实现复杂度分析和功能点映射
- 添加计算结果验证机制

Closes #123

fix(auth): 修复JWT令牌过期处理逻辑

修复用户令牌过期后无法正确跳转到登录页面的问题

Breaking Change: 修改了认证拦截器的响应格式
```

### 3. Code Review流程

#### Pull Request模板
```markdown
<!-- .github/pull_request_template.md -->

## 变更概述
简要描述本次变更的内容和目标

## 变更类型
- [ ] 新功能 (feat)
- [ ] 错误修复 (fix)
- [ ] 代码重构 (refactor)
- [ ] 性能优化 (perf)
- [ ] 文档更新 (docs)
- [ ] 测试相关 (test)
- [ ] 构建相关 (chore)

## 测试计划
- [ ] 单元测试已通过
- [ ] 集成测试已通过
- [ ] 手动测试已完成
- [ ] 性能测试已完成（如适用）

## 安全检查
- [ ] 没有引入安全漏洞
- [ ] 敏感信息已正确处理
- [ ] 输入验证已实现
- [ ] 权限控制已实现

## 兼容性检查
- [ ] 不包含破坏性变更
- [ ] 数据库迁移脚本已准备
- [ ] API接口向后兼容
- [ ] 配置文件已更新

## 性能影响
- [ ] 不影响系统性能
- [ ] 已进行性能测试
- [ ] 已优化数据库查询
- [ ] 已考虑缓存策略

## 文档更新
- [ ] API文档已更新
- [ ] 用户文档已更新
- [ ] 开发文档已更新
- [ ] 部署文档已更新

## 检查清单
- [ ] 代码符合项目编码规范
- [ ] 已添加必要的单元测试
- [ ] 已添加必要的集成测试
- [ ] 已添加必要的注释
- [ ] 已移除调试代码和日志
- [ ] 已更新版本号（如适用）

## 相关Issue
Closes #
Fixes #
Related to #

## 截图（如适用）
如果变更涉及UI，请添加截图

## 其他说明
其他需要说明的内容
```

#### Review检查要点
```markdown
# Code Review检查清单

## 功能性检查
- [ ] 代码实现是否符合需求规格
- [ ] 边界条件是否正确处理
- [ ] 错误处理是否完善
- [ ] 返回值是否正确

## 代码质量检查
- [ ] 代码是否符合编码规范
- [ ] 方法和变量命名是否清晰
- [ ] 代码结构是否合理
- [ ] 是否有重复代码

## 性能检查
- [ ] 是否有性能问题
- [ ] 数据库查询是否优化
- [ ] 缓存使用是否合理
- [ ] 内存使用是否合理

## 安全检查
- [ ] 输入验证是否充分
- [ ] 权限控制是否正确
- [ ] 敏感信息是否泄露
- [ ] SQL注入防护是否到位

## 测试覆盖
- [ ] 单元测试覆盖率是否足够
- [ ] 测试用例是否充分
- [ ] 集成测试是否完整
- [ ] 边界测试是否覆盖

## 文档完整性
- [ ] 代码注释是否充分
- [ ] API文档是否更新
- [ ] 变更日志是否记录
- [ ] 部署说明是否更新
```

## 五、部署和运维规范

### 1. 环境管理

#### 环境配置标准
```yaml
# config/application-development.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/assessment_dev
    username: dev_user
    password: dev_password
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

  jpa:
    show-sql: true
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

logging:
  level:
    com.government.assessment: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

```yaml
# config/application-production.yml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT}
    password: ${REDIS_PASSWORD}
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 16
        max-idle: 16
        min-idle: 2

  jpa:
    show-sql: false
    hibernate:
      ddl-auto: none
    properties:
      hibernate:
        format_sql: false
        use_sql_comments: false

logging:
  level:
    com.government.assessment: INFO
    root: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /var/log/assessment/application.log
    max-file-size: 100MB
    max-history: 30

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
```

### 2. 监控和日志

#### 日志记录规范
```java
/**
 * 日志记录最佳实践
 */
@Slf4j
@Service
public class ExampleService {
    
    public void processCalculation(CalculationRequest request) {
        // 1. 记录方法入口，包含关键参数
        log.info("开始处理计算请求 - 项目ID: {}, 计算类型: {}", 
                request.getProjectId(), request.getCalculationType());
        
        try {
            // 2. 记录关键步骤
            log.debug("验证计算请求参数");
            validateRequest(request);
            
            log.debug("执行功能点计算");
            CalculationResult result = performCalculation(request);
            
            log.debug("保存计算结果");
            saveResult(result);
            
            // 3. 记录成功结果
            log.info("计算处理完成 - 项目ID: {}, 功能点数: {}, 耗时: {}ms", 
                    request.getProjectId(), result.getFunctionPoints(), 
                    System.currentTimeMillis() - startTime);
                    
        } catch (ValidationException e) {
            // 4. 记录业务异常，级别为WARN
            log.warn("计算请求验证失败 - 项目ID: {}, 错误: {}", 
                    request.getProjectId(), e.getMessage());
            throw e;
            
        } catch (Exception e) {
            // 5. 记录系统异常，级别为ERROR，包含堆栈信息
            log.error("计算处理失败 - 项目ID: {}", request.getProjectId(), e);
            throw new CalculationException("计算处理失败", e);
        }
    }
}

/**
 * 审计日志记录切面
 */
@Aspect
@Component
@Slf4j
public class AuditLogAspect {
    
    @Around("@annotation(AuditLog)")
    public Object auditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String userId = SecurityUtils.getCurrentUserId();
        String ipAddress = RequestUtils.getClientIpAddress();
        
        // 记录操作开始
        log.info("审计日志 - 用户: {}, IP: {}, 操作: {}, 参数: {}", 
                userId, ipAddress, methodName, Arrays.toString(args));
        
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录操作成功
            log.info("审计日志 - 用户: {}, 操作: {} 成功, 耗时: {}ms", 
                    userId, methodName, duration);
            
            return result;
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // 记录操作失败
            log.error("审计日志 - 用户: {}, 操作: {} 失败, 耗时: {}ms, 错误: {}", 
                    userId, methodName, duration, e.getMessage());
            
            throw e;
        }
    }
}
```

### 3. 性能监控

#### 监控指标配置
```java
/**
 * 自定义监控指标
 */
@Component
public class CustomMetricsCollector {
    
    private final MeterRegistry meterRegistry;
    private final Counter calculationCounter;
    private final Timer calculationTimer;
    private final Gauge activeUsersGauge;
    
    public CustomMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // 计算次数统计
        this.calculationCounter = Counter.builder("calculations.total")
            .description("功能点计算总次数")
            .tag("type", "function_point")
            .register(meterRegistry);
            
        // 计算耗时统计
        this.calculationTimer = Timer.builder("calculations.duration")
            .description("功能点计算耗时")
            .register(meterRegistry);
            
        // 在线用户数统计
        this.activeUsersGauge = Gauge.builder("users.active")
            .description("当前在线用户数")
            .register(meterRegistry, this, CustomMetricsCollector::getActiveUsersCount);
    }
    
    public void recordCalculation() {
        calculationCounter.increment();
    }
    
    public void recordCalculationTime(long duration) {
        calculationTimer.record(duration, TimeUnit.MILLISECONDS);
    }
    
    private double getActiveUsersCount(CustomMetricsCollector collector) {
        // 实现获取在线用户数的逻辑
        return 0.0;
    }
}

/**
 * 健康检查配置
 */
@Component
public class CustomHealthIndicator implements HealthIndicator {
    
    private final DatabaseHealthService databaseHealthService;
    private final RedisHealthService redisHealthService;
    
    @Override
    public Health health() {
        Health.Builder healthBuilder = new Health.Builder();
        
        try {
            // 检查数据库连接
            if (!databaseHealthService.isHealthy()) {
                return healthBuilder.down()
                    .withDetail("database", "数据库连接失败")
                    .build();
            }
            
            // 检查Redis连接
            if (!redisHealthService.isHealthy()) {
                return healthBuilder.down()
                    .withDetail("redis", "Redis连接失败")
                    .build();
            }
            
            // 检查计算引擎
            if (!isCalculationEngineHealthy()) {
                return healthBuilder.down()
                    .withDetail("calculation_engine", "计算引擎异常")
                    .build();
            }
            
            return healthBuilder.up()
                .withDetail("database", "正常")
                .withDetail("redis", "正常")
                .withDetail("calculation_engine", "正常")
                .build();
                
        } catch (Exception e) {
            return healthBuilder.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private boolean isCalculationEngineHealthy() {
        // 实现计算引擎健康检查逻辑
        return true;
    }
}
```

这个开发规范与实施指南为团队提供了完整的开发标准和最佳实践，确保代码质量和项目的可维护性。