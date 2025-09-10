/**
 * API功能验证测试
 * 验证新实现的NESMA API和功能点管理API
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */

const axios = require('axios');

class APIValidationTest {
    constructor() {
        this.baseURL = 'http://localhost:8080';
        this.testProject = null;
        console.log('API功能验证测试工具初始化');
    }
    
    /**
     * 测试项目创建API
     */
    async testProjectCreation() {
        console.log('\n=== 测试项目创建API ===');
        
        const projectData = {
            projectName: `API测试项目_${Date.now()}`,
            description: '用于API功能验证的测试项目',
            projectType: 'INFORMATION_SYSTEM'
        };
        
        try {
            const response = await axios.post(`${this.baseURL}/api/projects`, projectData);
            
            if (response.data && response.data.code === 200) {
                this.testProject = response.data.data;
                console.log(`✅ 项目创建成功 - ID: ${this.testProject.id}, 编号: ${this.testProject.projectCode}`);
                return this.testProject;
            } else {
                console.log(`❌ 项目创建失败: ${JSON.stringify(response.data)}`);
                return null;
            }
        } catch (error) {
            console.log(`❌ 项目创建异常: ${error.message}`);
            return null;
        }
    }
    
    /**
     * 测试功能点批量创建API
     */
    async testFunctionPointBatchCreation(projectId, count = 5) {
        console.log(`\n=== 测试功能点批量创建API (${count}个) ===`);
        
        const functionPointTypes = ['ILF', 'EIF', 'EI', 'EO', 'EQ'];
        const functionPointsData = [];
        
        for (let i = 1; i <= count; i++) {
            const type = functionPointTypes[Math.floor(Math.random() * functionPointTypes.length)];
            
            const functionPoint = {
                functionName: `测试功能点_${i}`,
                functionDescription: `第${i}个API测试功能点`,
                functionPointType: type,
                detCount: Math.floor(Math.random() * 20) + 1,
                retCount: (type === 'ILF' || type === 'EIF') ? Math.floor(Math.random() * 5) + 1 : null,
                ftrCount: (type !== 'ILF' && type !== 'EIF') ? Math.floor(Math.random() * 3) + 1 : null
            };
            
            functionPointsData.push(functionPoint);
        }
        
        try {
            const startTime = Date.now();
            const response = await axios.post(`${this.baseURL}/api/function-points/project/${projectId}/batch`, functionPointsData);
            const duration = Date.now() - startTime;
            
            if (response.data && response.data.code === 200) {
                const createdPoints = response.data.data;
                console.log(`✅ 批量创建功能点成功 - 数量: ${createdPoints.length}, 耗时: ${duration}ms`);
                return createdPoints;
            } else {
                console.log(`❌ 批量创建功能点失败: ${JSON.stringify(response.data)}`);
                return null;
            }
        } catch (error) {
            console.log(`❌ 批量创建功能点异常: ${error.message}`);
            return null;
        }
    }
    
    /**
     * 测试获取功能点列表API
     */
    async testGetFunctionPoints(projectId) {
        console.log('\n=== 测试获取功能点列表API ===');
        
        try {
            const response = await axios.get(`${this.baseURL}/api/function-points/project/${projectId}`);
            
            if (response.data && response.data.code === 200) {
                const functionPoints = response.data.data;
                console.log(`✅ 获取功能点列表成功 - 数量: ${functionPoints.length}`);
                
                // 显示功能点统计
                const typeCount = {};
                functionPoints.forEach(fp => {
                    typeCount[fp.functionPointType] = (typeCount[fp.functionPointType] || 0) + 1;
                });
                
                console.log('功能点类型分布:', typeCount);
                return functionPoints;
            } else {
                console.log(`❌ 获取功能点列表失败: ${JSON.stringify(response.data)}`);
                return null;
            }
        } catch (error) {
            console.log(`❌ 获取功能点列表异常: ${error.message}`);
            return null;
        }
    }
    
    /**
     * 测试NESMA计算API
     */
    async testNESMACalculation(projectId) {
        console.log('\n=== 测试NESMA计算API ===');
        
        try {
            const startTime = Date.now();
            const response = await axios.post(`${this.baseURL}/api/nesma/calculate/${projectId}`);
            const duration = Date.now() - startTime;
            
            if (response.data && response.data.code === 200) {
                const result = response.data.data;
                console.log(`✅ NESMA计算成功 - 耗时: ${duration}ms`);
                console.log(`   总功能点: ${result.totalFunctionPoints}`);
                console.log(`   调整后功能点: ${result.adjustedFunctionPoints}`);
                console.log(`   估算人月: ${result.estimatedPersonMonths}`);
                console.log(`   估算成本: ${result.estimatedCost}元`);
                return result;
            } else {
                console.log(`❌ NESMA计算失败: ${JSON.stringify(response.data)}`);
                return null;
            }
        } catch (error) {
            console.log(`❌ NESMA计算异常: ${error.message}`);
            return null;
        }
    }
    
    /**
     * 测试批量计算API
     */
    async testBatchCalculation(projectIds) {
        console.log(`\n=== 测试批量计算API (${projectIds.length}个项目) ===`);
        
        try {
            const startTime = Date.now();
            const response = await axios.post(`${this.baseURL}/api/nesma/batch-calculate`, {
                projectIds: projectIds
            });
            const duration = Date.now() - startTime;
            
            if (response.data && response.data.code === 200) {
                const result = response.data.data;
                console.log(`✅ 批量计算完成 - 耗时: ${duration}ms`);
                console.log(`   总项目数: ${result.totalProjects}`);
                console.log(`   成功: ${result.successCount}, 失败: ${result.failureCount}`);
                return result;
            } else {
                console.log(`❌ 批量计算失败: ${JSON.stringify(response.data)}`);
                return null;
            }
        } catch (error) {
            console.log(`❌ 批量计算异常: ${error.message}`);
            return null;
        }
    }
    
    /**
     * 运行完整的API验证测试
     */
    async runValidationTests() {
        console.log('=== 开始API功能验证测试 ===');
        
        const results = {
            timestamp: new Date().toISOString(),
            tests: []
        };
        
        // 1. 测试项目创建
        const project = await this.testProjectCreation();
        results.tests.push({
            name: '项目创建API',
            success: project !== null,
            projectId: project ? project.id : null
        });
        
        if (!project) {
            console.log('❌ 项目创建失败，终止测试');
            return results;
        }
        
        // 2. 测试功能点批量创建
        const functionPoints = await this.testFunctionPointBatchCreation(project.id, 10);
        results.tests.push({
            name: '功能点批量创建API',
            success: functionPoints !== null,
            count: functionPoints ? functionPoints.length : 0
        });
        
        if (!functionPoints) {
            console.log('❌ 功能点创建失败，跳过后续测试');
            return results;
        }
        
        // 3. 测试获取功能点列表
        const retrievedFunctionPoints = await this.testGetFunctionPoints(project.id);
        results.tests.push({
            name: '获取功能点列表API',
            success: retrievedFunctionPoints !== null,
            count: retrievedFunctionPoints ? retrievedFunctionPoints.length : 0
        });
        
        // 4. 测试NESMA计算
        const calculationResult = await this.testNESMACalculation(project.id);
        results.tests.push({
            name: 'NESMA计算API',
            success: calculationResult !== null,
            totalFunctionPoints: calculationResult ? calculationResult.totalFunctionPoints : null
        });
        
        // 5. 测试批量计算
        const batchResult = await this.testBatchCalculation([project.id]);
        results.tests.push({
            name: '批量计算API',
            success: batchResult !== null,
            successCount: batchResult ? batchResult.successCount : 0
        });
        
        // 生成测试摘要
        this.generateTestSummary(results);
        
        return results;
    }
    
    /**
     * 生成测试摘要
     */
    generateTestSummary(results) {
        console.log('\n=== API功能验证测试摘要 ===');
        console.log(`测试时间: ${results.timestamp}`);
        
        const totalTests = results.tests.length;
        const successfulTests = results.tests.filter(test => test.success).length;
        const failedTests = totalTests - successfulTests;
        
        console.log(`总测试数: ${totalTests}`);
        console.log(`成功: ${successfulTests}, 失败: ${failedTests}`);
        console.log(`成功率: ${Math.round((successfulTests / totalTests) * 100)}%`);
        
        console.log('\n详细结果:');
        results.tests.forEach((test, index) => {
            const status = test.success ? '✅' : '❌';
            console.log(`${index + 1}. ${status} ${test.name}`);
        });
        
        if (successfulTests === totalTests) {
            console.log('\n🎉 所有API功能验证通过！可以进行性能优化工作。');
        } else {
            console.log('\n⚠️ 部分API测试失败，需要先修复基础功能。');
        }
    }
}

// 执行API验证测试
async function main() {
    try {
        const tester = new APIValidationTest();
        await tester.runValidationTests();
    } catch (error) {
        console.error('API验证测试执行失败:', error.message);
        process.exit(1);
    }
}

// 检查是否作为脚本直接运行
if (require.main === module) {
    main();
}

module.exports = APIValidationTest;