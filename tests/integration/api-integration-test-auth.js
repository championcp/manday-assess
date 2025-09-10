/**
 * API集成测试工具（支持JWT认证）
 * 完整测试系统集成和API功能
 * 
 * @author 系统测试工程师
 * @version 1.0.0
 * @since 2025-09-10
 */

const axios = require('axios');

class AuthenticatedAPITest {
    constructor() {
        this.baseURL = 'http://localhost:8080';
        this.accessToken = null;
        this.refreshToken = null;
        this.testResults = [];
        console.log('🔐 带认证的API集成测试工具初始化完成');
    }
    
    /**
     * 获取授权头
     */
    getAuthHeaders() {
        return this.accessToken ? {
            'Authorization': `Bearer ${this.accessToken}`,
            'Content-Type': 'application/json'
        } : {
            'Content-Type': 'application/json'
        };
    }
    
    /**
     * 测试用户登录获取JWT令牌
     */
    async testLogin() {
        console.log('\n=== 1. 测试用户登录认证 ===');
        
        // 使用默认的测试用户凭据
        const loginData = {
            username: 'admin',
            password: 'admin123'
        };
        
        try {
            const startTime = Date.now();
            const response = await axios.post(`${this.baseURL}/api/auth/login`, loginData);
            const duration = Date.now() - startTime;
            
            if (response.data && response.data.code === 200) {
                const authData = response.data.data;
                this.accessToken = authData.accessToken;
                this.refreshToken = authData.refreshToken;
                
                console.log('✅ 用户登录成功');
                console.log(`   用户名: ${authData.username}`);
                console.log(`   真实姓名: ${authData.realName || '未设置'}`);
                console.log(`   部门: ${authData.department || '未设置'}`);
                console.log(`   职位: ${authData.position || '未设置'}`);
                console.log(`   令牌长度: ${this.accessToken.length}字符`);
                console.log(`   响应时间: ${duration}ms`);
                
                return { success: true, duration, user: authData };
            } else {
                console.log(`❌ 登录失败: ${JSON.stringify(response.data)}`);
                return { success: false, error: response.data };
            }
        } catch (error) {
            console.log(`❌ 登录请求异常: ${error.message}`);
            if (error.response) {
                console.log(`   HTTP状态: ${error.response.status}`);
                console.log(`   错误详情: ${JSON.stringify(error.response.data)}`);
            }
            return { success: false, error: error.message };
        }
    }
    
    /**
     * 测试获取当前用户信息
     */
    async testGetCurrentUser() {
        console.log('\n=== 2. 测试获取用户信息API ===');
        
        try {
            const startTime = Date.now();
            const response = await axios.get(`${this.baseURL}/api/auth/me`, {
                headers: this.getAuthHeaders()
            });
            const duration = Date.now() - startTime;
            
            if (response.data && response.data.code === 200) {
                const userData = response.data.data;
                console.log('✅ 获取用户信息成功');
                console.log(`   用户ID: ${userData.id}`);
                console.log(`   用户名: ${userData.username}`);
                console.log(`   权限数量: ${userData.permissions ? userData.permissions.length : 0}`);
                console.log(`   角色数量: ${userData.roles ? userData.roles.length : 0}`);
                console.log(`   响应时间: ${duration}ms`);
                
                return { success: true, duration, user: userData };
            } else {
                console.log(`❌ 获取用户信息失败: ${JSON.stringify(response.data)}`);
                return { success: false, error: response.data };
            }
        } catch (error) {
            console.log(`❌ 获取用户信息异常: ${error.message}`);
            return { success: false, error: error.message };
        }
    }
    
    /**
     * 测试项目管理API
     */
    async testProjectAPIs() {
        console.log('\n=== 3. 测试项目管理API ===');
        
        const projectData = {
            projectName: `集成测试项目_${Date.now()}`,
            description: '用于API集成测试的测试项目',
            projectType: 'INFORMATION_SYSTEM'
        };
        
        try {
            // 创建项目
            const startTime = Date.now();
            const createResponse = await axios.post(
                `${this.baseURL}/api/projects`, 
                projectData,
                { headers: this.getAuthHeaders() }
            );
            const createDuration = Date.now() - startTime;
            
            if (createResponse.data && createResponse.data.code === 200) {
                const project = createResponse.data.data;
                console.log('✅ 项目创建成功');
                console.log(`   项目ID: ${project.id}`);
                console.log(`   项目编号: ${project.projectCode}`);
                console.log(`   创建时间: ${createDuration}ms`);
                
                // 获取项目列表
                const listStartTime = Date.now();
                const listResponse = await axios.get(
                    `${this.baseURL}/api/projects`, 
                    { headers: this.getAuthHeaders() }
                );
                const listDuration = Date.now() - listStartTime;
                
                if (listResponse.data && listResponse.data.code === 200) {
                    const projects = listResponse.data.data;
                    console.log('✅ 获取项目列表成功');
                    console.log(`   项目总数: ${projects.length}`);
                    console.log(`   查询时间: ${listDuration}ms`);
                    
                    return { 
                        success: true, 
                        project, 
                        createDuration, 
                        listDuration,
                        totalProjects: projects.length 
                    };
                } else {
                    console.log(`❌ 获取项目列表失败: ${JSON.stringify(listResponse.data)}`);
                    return { success: false, project, error: listResponse.data };
                }
            } else {
                console.log(`❌ 项目创建失败: ${JSON.stringify(createResponse.data)}`);
                return { success: false, error: createResponse.data };
            }
        } catch (error) {
            console.log(`❌ 项目API测试异常: ${error.message}`);
            return { success: false, error: error.message };
        }
    }
    
    /**
     * 测试功能点管理API
     */
    async testFunctionPointAPIs(projectId) {
        console.log('\n=== 4. 测试功能点管理API ===');
        
        // 准备测试数据
        const functionPointsData = [
            {
                functionName: '用户登录功能',
                functionDescription: '用户通过用户名和密码登录系统',
                functionPointType: 'EI',
                detCount: 3,
                ftrCount: 1
            },
            {
                functionName: '项目信息存储',
                functionDescription: '存储项目基本信息和配置数据',
                functionPointType: 'ILF',
                detCount: 8,
                retCount: 2
            },
            {
                functionName: '项目列表查询',
                functionDescription: '查询和显示项目列表信息',
                functionPointType: 'EO',
                detCount: 5,
                ftrCount: 2
            }
        ];
        
        try {
            // 批量创建功能点
            const createStartTime = Date.now();
            const createResponse = await axios.post(
                `${this.baseURL}/api/simple-function-points/project/${projectId}/batch`,
                functionPointsData,
                { headers: this.getAuthHeaders() }
            );
            const createDuration = Date.now() - createStartTime;
            
            if (createResponse.data && createResponse.data.code === 200) {
                const createdFunctionPoints = createResponse.data.data;
                console.log('✅ 批量创建功能点成功');
                console.log(`   创建数量: ${createdFunctionPoints.length}`);
                console.log(`   创建时间: ${createDuration}ms`);
                
                // 获取功能点列表
                const listStartTime = Date.now();
                const listResponse = await axios.get(
                    `${this.baseURL}/api/simple-function-points/project/${projectId}`,
                    { headers: this.getAuthHeaders() }
                );
                const listDuration = Date.now() - listStartTime;
                
                if (listResponse.data && listResponse.data.code === 200) {
                    const functionPoints = listResponse.data.data;
                    console.log('✅ 获取功能点列表成功');
                    console.log(`   功能点总数: ${functionPoints.length}`);
                    console.log(`   查询时间: ${listDuration}ms`);
                    
                    // 统计功能点类型
                    const typeStats = {};
                    functionPoints.forEach(fp => {
                        typeStats[fp.functionPointType] = (typeStats[fp.functionPointType] || 0) + 1;
                    });
                    console.log('   类型分布:', typeStats);
                    
                    return {
                        success: true,
                        createdCount: createdFunctionPoints.length,
                        totalCount: functionPoints.length,
                        createDuration,
                        listDuration,
                        typeStats
                    };
                } else {
                    console.log(`❌ 获取功能点列表失败: ${JSON.stringify(listResponse.data)}`);
                    return { success: false, error: listResponse.data };
                }
            } else {
                console.log(`❌ 批量创建功能点失败: ${JSON.stringify(createResponse.data)}`);
                return { success: false, error: createResponse.data };
            }
        } catch (error) {
            console.log(`❌ 功能点API测试异常: ${error.message}`);
            return { success: false, error: error.message };
        }
    }
    
    /**
     * 测试NESMA计算API
     */
    async testNESMACalculationAPIs(projectId) {
        console.log('\n=== 5. 测试NESMA计算API ===');
        
        try {
            // 单项目计算
            const calcStartTime = Date.now();
            const calcResponse = await axios.post(
                `${this.baseURL}/api/nesma/calculate/${projectId}`,
                {},
                { headers: this.getAuthHeaders() }
            );
            const calcDuration = Date.now() - calcStartTime;
            
            if (calcResponse.data && calcResponse.data.code === 200) {
                const result = calcResponse.data.data;
                console.log('✅ NESMA单项目计算成功');
                console.log(`   总功能点: ${result.totalFunctionPoints}`);
                console.log(`   调整后功能点: ${result.adjustedFunctionPoints}`);
                console.log(`   估算人月: ${result.estimatedPersonMonths}`);
                console.log(`   估算成本: ${result.estimatedCost}元`);
                console.log(`   计算时间: ${calcDuration}ms`);
                
                // 批量计算测试（单个项目）
                const batchStartTime = Date.now();
                const batchResponse = await axios.post(
                    `${this.baseURL}/api/nesma/batch-calculate`,
                    { projectIds: [projectId] },
                    { headers: this.getAuthHeaders() }
                );
                const batchDuration = Date.now() - batchStartTime;
                
                if (batchResponse.data && batchResponse.data.code === 200) {
                    const batchResult = batchResponse.data.data;
                    console.log('✅ NESMA批量计算成功');
                    console.log(`   处理项目数: ${batchResult.totalProjects}`);
                    console.log(`   成功: ${batchResult.successCount}, 失败: ${batchResult.failureCount}`);
                    console.log(`   批量计算时间: ${batchDuration}ms`);
                    
                    return {
                        success: true,
                        singleResult: result,
                        batchResult,
                        calcDuration,
                        batchDuration
                    };
                } else {
                    console.log(`❌ NESMA批量计算失败: ${JSON.stringify(batchResponse.data)}`);
                    return { success: false, singleResult: result, error: batchResponse.data };
                }
            } else {
                console.log(`❌ NESMA单项目计算失败: ${JSON.stringify(calcResponse.data)}`);
                return { success: false, error: calcResponse.data };
            }
        } catch (error) {
            console.log(`❌ NESMA计算API测试异常: ${error.message}`);
            return { success: false, error: error.message };
        }
    }
    
    /**
     * 测试系统健康状态API
     */
    async testHealthCheck() {
        console.log('\n=== 6. 测试系统健康检查 ===');
        
        try {
            const startTime = Date.now();
            const response = await axios.get(`${this.baseURL}/actuator/health`);
            const duration = Date.now() - startTime;
            
            if (response.data && response.data.status === 'UP') {
                console.log('✅ 系统健康检查通过');
                console.log(`   状态: ${response.data.status}`);
                console.log(`   响应时间: ${duration}ms`);
                
                // 如果有详细信息，显示关键组件状态
                if (response.data.components) {
                    const components = response.data.components;
                    Object.keys(components).forEach(component => {
                        const status = components[component].status;
                        const icon = status === 'UP' ? '✅' : '❌';
                        console.log(`   ${icon} ${component}: ${status}`);
                    });
                }
                
                return { success: true, duration, status: response.data };
            } else {
                console.log(`❌ 系统健康检查失败: ${JSON.stringify(response.data)}`);
                return { success: false, error: response.data };
            }
        } catch (error) {
            console.log(`❌ 健康检查异常: ${error.message}`);
            return { success: false, error: error.message };
        }
    }
    
    /**
     * 运行完整的集成测试套件
     */
    async runIntegrationTests() {
        console.log('🚀 开始完整API集成测试');
        console.log('==================================');
        
        const overallStartTime = Date.now();
        let project = null;
        
        // 1. 登录认证
        const loginResult = await this.testLogin();
        this.testResults.push({ name: '用户登录认证', result: loginResult });
        
        if (!loginResult.success) {
            console.log('\n❌ 登录失败，无法继续测试');
            return this.generateTestSummary();
        }
        
        // 2. 用户信息验证
        const userResult = await this.testGetCurrentUser();
        this.testResults.push({ name: '获取用户信息', result: userResult });
        
        // 3. 项目管理API测试
        const projectResult = await this.testProjectAPIs();
        this.testResults.push({ name: '项目管理API', result: projectResult });
        
        if (projectResult.success) {
            project = projectResult.project;
            
            // 4. 功能点管理API测试
            const fpResult = await this.testFunctionPointAPIs(project.id);
            this.testResults.push({ name: '功能点管理API', result: fpResult });
            
            // 5. NESMA计算API测试
            if (fpResult.success) {
                const nesmaResult = await this.testNESMACalculationAPIs(project.id);
                this.testResults.push({ name: 'NESMA计算API', result: nesmaResult });
            }
        }
        
        // 6. 系统健康检查
        const healthResult = await this.testHealthCheck();
        this.testResults.push({ name: '系统健康检查', result: healthResult });
        
        const overallDuration = Date.now() - overallStartTime;
        
        return this.generateTestSummary(overallDuration);
    }
    
    /**
     * 生成测试摘要报告
     */
    generateTestSummary(totalDuration = 0) {
        console.log('\n========================================');
        console.log('📊 API集成测试摘要报告');
        console.log('========================================');
        
        const totalTests = this.testResults.length;
        const successfulTests = this.testResults.filter(test => test.result.success).length;
        const failedTests = totalTests - successfulTests;
        const successRate = Math.round((successfulTests / totalTests) * 100);
        
        console.log(`\n📈 测试统计:`);
        console.log(`   总测试数: ${totalTests}`);
        console.log(`   成功: ${successfulTests}`);
        console.log(`   失败: ${failedTests}`);
        console.log(`   成功率: ${successRate}%`);
        console.log(`   总耗时: ${totalDuration}ms`);
        
        console.log(`\n📋 详细结果:`);
        this.testResults.forEach((test, index) => {
            const status = test.result.success ? '✅' : '❌';
            const duration = test.result.duration ? ` (${test.result.duration}ms)` : '';
            console.log(`   ${index + 1}. ${status} ${test.name}${duration}`);
        });
        
        console.log(`\n🎯 质量评估:`);
        if (successRate === 100) {
            console.log('   🌟 优秀 - 所有集成测试通过！系统可进入性能优化阶段');
        } else if (successRate >= 80) {
            console.log('   🟡 良好 - 大部分功能正常，需要修复个别问题');
        } else {
            console.log('   🔴 需要改进 - 存在较多问题，建议优先修复');
        }
        
        console.log(`\n📄 测试报告时间: ${new Date().toISOString()}`);
        
        return {
            totalTests,
            successfulTests,
            failedTests,
            successRate,
            totalDuration,
            results: this.testResults
        };
    }
}

// 执行集成测试
async function main() {
    try {
        const tester = new AuthenticatedAPITest();
        const summary = await tester.runIntegrationTests();
        
        // 如果成功率较高，显示下一步建议
        if (summary.successRate >= 80) {
            console.log('\n🔄 建议的下一步操作:');
            console.log('   1. 执行性能压力测试');
            console.log('   2. 运行安全扫描验证');
            console.log('   3. 开始Chrome UI端到端测试');
            console.log('   4. 准备用户验收测试环境');
        }
        
        // 根据测试结果设置退出码
        process.exit(summary.successRate === 100 ? 0 : 1);
        
    } catch (error) {
        console.error('❌ 集成测试执行失败:', error.message);
        process.exit(1);
    }
}

// 检查是否作为脚本直接运行
if (require.main === module) {
    main();
}

module.exports = AuthenticatedAPITest;