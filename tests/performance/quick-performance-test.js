/**
 * 快速性能基准测试
 * 用于快速评估当前系统的基础性能
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */

const axios = require('axios');

class QuickPerformanceTest {
    constructor() {
        this.baseURL = 'http://localhost:8080';
        console.log('快速性能测试工具初始化');
    }
    
    /**
     * 测试API响应时间
     */
    async testAPIResponseTime(endpoint, data = null, method = 'GET') {
        try {
            const startTime = Date.now();
            
            let response;
            if (method === 'GET') {
                response = await axios.get(`${this.baseURL}${endpoint}`);
            } else if (method === 'POST') {
                response = await axios.post(`${this.baseURL}${endpoint}`, data);
            }
            
            const endTime = Date.now();
            const duration = endTime - startTime;
            
            return {
                endpoint: endpoint,
                method: method,
                duration: duration,
                success: response.status >= 200 && response.status < 300,
                statusCode: response.status,
                dataSize: JSON.stringify(response.data).length
            };
        } catch (error) {
            return {
                endpoint: endpoint,
                method: method,
                duration: -1,
                success: false,
                error: error.message
            };
        }
    }
    
    /**
     * 创建测试项目（小规模）
     */
    async createTestProject() {
        const projectData = {
            projectCode: `QUICK_PERF_${Date.now()}`,
            projectName: '快速性能测试项目',
            description: '用于快速性能测试的小规模项目',
            projectType: 'INFORMATION_SYSTEM',
            priority: 'HIGH',
            budgetAmount: 1000000,
            departmentName: '财政评审中心',
            projectManagerName: '测试管理员',
            contactPhone: '13800138000',
            contactEmail: 'test@changsha.gov.cn'
        };
        
        try {
            const response = await axios.post(`${this.baseURL}/api/projects`, projectData);
            if (response.data && response.data.code === 200) {
                return response.data.data.id;
            } else {
                throw new Error('创建项目失败: ' + JSON.stringify(response.data));
            }
        } catch (error) {
            console.error('创建测试项目失败:', error.response?.data || error.message);
            return null;
        }
    }
    
    /**
     * 为项目添加功能点
     */
    async addFunctionPointsToProject(projectId, count = 10) {
        const functionPointTypes = ['ILF', 'EIF', 'EI', 'EO', 'EQ'];
        const results = [];
        
        for (let i = 1; i <= count; i++) {
            const type = functionPointTypes[Math.floor(Math.random() * functionPointTypes.length)];
            
            const functionPoint = {
                functionName: `测试功能点_${i}`,
                functionDescription: `第${i}个测试功能点`,
                functionPointType: type,
                detCount: Math.floor(Math.random() * 20) + 1,
                retCount: type === 'ILF' || type === 'EIF' ? Math.floor(Math.random() * 5) + 1 : null,
                ftrCount: type !== 'ILF' && type !== 'EIF' ? Math.floor(Math.random() * 3) + 1 : null
            };
            
            try {
                const startTime = Date.now();
                const response = await axios.post(`${this.baseURL}/api/projects/${projectId}/function-points`, functionPoint);
                const duration = Date.now() - startTime;
                
                results.push({
                    index: i,
                    duration: duration,
                    success: response.data && response.data.success
                });
            } catch (error) {
                results.push({
                    index: i,
                    duration: -1,
                    success: false,
                    error: error.message
                });
            }
        }
        
        return results;
    }
    
    /**
     * 测试NESMA计算性能
     */
    async testNESMACalculation(projectId) {
        try {
            console.log(`测试项目${projectId}的NESMA计算性能...`);
            
            const startTime = Date.now();
            const response = await axios.post(`${this.baseURL}/api/nesma/calculate/${projectId}`);
            const duration = Date.now() - startTime;
            
            if (response.data && response.data.success) {
                return {
                    projectId: projectId,
                    duration: duration,
                    success: true,
                    totalFunctionPoints: response.data.data.totalFunctionPoints,
                    adjustedFunctionPoints: response.data.data.adjustedFunctionPoints
                };
            } else {
                return {
                    projectId: projectId,
                    duration: duration,
                    success: false,
                    error: '计算返回失败状态'
                };
            }
        } catch (error) {
            return {
                projectId: projectId,
                duration: -1,
                success: false,
                error: error.message
            };
        }
    }
    
    /**
     * 运行完整的快速性能测试
     */
    async runQuickTest() {
        console.log('\n=== 开始快速性能基准测试 ===\n');
        
        const results = {
            timestamp: new Date().toISOString(),
            apiTests: [],
            projectCreation: null,
            functionPointCreation: [],
            nesmaCalculation: null
        };
        
        // 1. 测试基础API响应时间
        console.log('1. 测试基础API响应时间...');
        const apiEndpoints = [
            { endpoint: '/actuator/health', method: 'GET' },
            { endpoint: '/actuator/metrics', method: 'GET' },
            { endpoint: '/api/projects', method: 'GET' }
        ];
        
        for (const api of apiEndpoints) {
            const result = await this.testAPIResponseTime(api.endpoint, null, api.method);
            results.apiTests.push(result);
            console.log(`- ${api.method} ${api.endpoint}: ${result.success ? result.duration + 'ms' : 'FAILED'}`);
        }
        
        // 2. 测试项目创建性能
        console.log('\n2. 测试项目创建性能...');
        const startCreateProject = Date.now();
        const projectId = await this.createTestProject();
        const createProjectDuration = Date.now() - startCreateProject;
        
        results.projectCreation = {
            duration: createProjectDuration,
            success: projectId !== null,
            projectId: projectId
        };
        
        console.log(`- 项目创建: ${projectId ? createProjectDuration + 'ms' : 'FAILED'}`);
        
        if (!projectId) {
            console.log('项目创建失败，终止测试');
            return results;
        }
        
        // 3. 测试功能点创建性能
        console.log('\n3. 测试功能点创建性能...');
        const functionPointResults = await this.addFunctionPointsToProject(projectId, 10);
        results.functionPointCreation = functionPointResults;
        
        const successfulFP = functionPointResults.filter(r => r.success);
        const avgFPDuration = successfulFP.length > 0 ? 
            successfulFP.reduce((sum, r) => sum + r.duration, 0) / successfulFP.length : -1;
        
        console.log(`- 功能点创建: ${successfulFP.length}/10成功, 平均${Math.round(avgFPDuration)}ms`);
        
        // 4. 测试NESMA计算性能
        console.log('\n4. 测试NESMA计算性能...');
        const nesmaResult = await this.testNESMACalculation(projectId);
        results.nesmaCalculation = nesmaResult;
        
        console.log(`- NESMA计算: ${nesmaResult.success ? nesmaResult.duration + 'ms' : 'FAILED'}`);
        if (nesmaResult.success) {
            console.log(`  总功能点: ${nesmaResult.totalFunctionPoints}, 调整后: ${nesmaResult.adjustedFunctionPoints}`);
        }
        
        // 5. 输出测试摘要
        console.log('\n=== 快速性能测试摘要 ===');
        console.log(`测试时间: ${results.timestamp}`);
        console.log(`API响应测试: ${results.apiTests.filter(r => r.success).length}/${results.apiTests.length}成功`);
        console.log(`项目创建: ${results.projectCreation.success ? results.projectCreation.duration + 'ms' : '失败'}`);
        console.log(`功能点创建: 平均${Math.round(avgFPDuration)}ms`);
        console.log(`NESMA计算: ${nesmaResult.success ? nesmaResult.duration + 'ms' : '失败'}`);
        
        // 6. 性能评估
        this.evaluatePerformance(results);
        
        return results;
    }
    
    /**
     * 评估性能并提供优化建议
     */
    evaluatePerformance(results) {
        console.log('\n=== 性能评估和优化建议 ===');
        
        const issues = [];
        const recommendations = [];
        
        // API响应时间评估
        const slowAPIs = results.apiTests.filter(r => r.success && r.duration > 1000);
        if (slowAPIs.length > 0) {
            issues.push('API响应时间过慢 (>1秒)');
            recommendations.push('优化API响应性能，添加缓存机制');
        }
        
        // 项目创建性能评估
        if (results.projectCreation.success && results.projectCreation.duration > 2000) {
            issues.push('项目创建耗时过长 (>2秒)');
            recommendations.push('优化数据库写入性能，考虑批量操作');
        }
        
        // 功能点创建性能评估
        const avgFPTime = results.functionPointCreation.filter(r => r.success)
            .reduce((sum, r) => sum + r.duration, 0) / results.functionPointCreation.length;
        if (avgFPTime > 500) {
            issues.push('功能点创建性能较低 (>500ms/个)');
            recommendations.push('优化功能点批量创建，使用事务优化');
        }
        
        // NESMA计算性能评估
        if (results.nesmaCalculation.success && results.nesmaCalculation.duration > 1000) {
            issues.push('NESMA计算响应时间较慢 (>1秒)');
            recommendations.push('优化计算算法，添加计算结果缓存');
        }
        
        if (issues.length === 0) {
            console.log('✅ 当前系统性能良好，无明显瓶颈');
        } else {
            console.log('⚠️ 发现的性能问题:');
            issues.forEach((issue, index) => {
                console.log(`${index + 1}. ${issue}`);
            });
            
            console.log('\n💡 优化建议:');
            recommendations.forEach((rec, index) => {
                console.log(`${index + 1}. ${rec}`);
            });
        }
        
        console.log('\n下一步: 执行大数据量性能测试以验证系统极限');
    }
}

// 执行快速性能测试
async function main() {
    try {
        const tester = new QuickPerformanceTest();
        await tester.runQuickTest();
    } catch (error) {
        console.error('快速性能测试失败:', error.message);
        process.exit(1);
    }
}

// 检查是否作为脚本直接运行
if (require.main === module) {
    main();
}

module.exports = QuickPerformanceTest;