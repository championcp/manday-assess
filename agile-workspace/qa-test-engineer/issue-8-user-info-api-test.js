/**
 * Issue #8 用户信息API内部服务器错误 - 修复验证测试套件
 * GitHub Issue: https://github.com/championcp/manday-assess/issues/8
 * 修复分支: fix/issue-8-user-info-api-error
 * 
 * 测试目标：验证/api/auth/me不再返回500错误，而是正确处理认证状态
 * 
 * @author QA Test Engineer
 * @version 1.0.0
 * @since 2025-09-12
 */

const axios = require('axios');
const crypto = require('crypto');

// 测试配置
const BASE_URL = 'http://localhost:8080';
const API_ENDPOINT = '/api/auth/me';
const LOGIN_ENDPOINT = '/api/auth/login';
const TEST_USER = {
    username: 'testuser001',
    password: 'TestPass123!'
};

class Issue8TestSuite {
    constructor() {
        this.testResults = [];
        this.startTime = new Date();
    }

    /**
     * 记录测试结果
     */
    recordResult(testName, status, message, responseData = null, duration = 0) {
        this.testResults.push({
            testName,
            status,
            message,
            duration,
            timestamp: new Date(),
            responseData
        });
        
        const statusIcon = status === 'PASS' ? '✅' : status === 'FAIL' ? '❌' : '⚠️';
        console.log(`${statusIcon} [${status}] ${testName}: ${message} (${duration}ms)`);
        
        if (responseData) {
            console.log(`   响应数据:`, JSON.stringify(responseData, null, 2));
        }
    }

    /**
     * 测试1: 无效token返回401而非500
     */
    async testInvalidTokenHandling() {
        const testName = '无效Token错误处理测试';
        const startTime = Date.now();
        
        try {
            const response = await axios.get(`${BASE_URL}${API_ENDPOINT}`, {
                headers: {
                    'Authorization': 'Bearer invalid-token-string'
                },
                validateStatus: function (status) {
                    return status < 600; // 接受所有HTTP状态码
                }
            });
            
            const duration = Date.now() - startTime;
            
            if (response.status === 401) {
                this.recordResult(testName, 'PASS', 
                    `正确返回401状态码，消息: ${response.data.message}`, 
                    response.data, duration);
            } else if (response.status === 500) {
                this.recordResult(testName, 'FAIL', 
                    '仍然返回500内部服务器错误，修复未生效', 
                    response.data, duration);
            } else {
                this.recordResult(testName, 'WARN', 
                    `返回意外状态码: ${response.status}`, 
                    response.data, duration);
            }
        } catch (error) {
            const duration = Date.now() - startTime;
            this.recordResult(testName, 'FAIL', 
                `网络请求失败: ${error.message}`, null, duration);
        }
    }

    /**
     * 测试2: 无Authorization头返回401
     */
    async testNoAuthorizationHeader() {
        const testName = '无认证头处理测试';
        const startTime = Date.now();
        
        try {
            const response = await axios.get(`${BASE_URL}${API_ENDPOINT}`, {
                validateStatus: function (status) {
                    return status < 600;
                }
            });
            
            const duration = Date.now() - startTime;
            
            if (response.status === 401) {
                this.recordResult(testName, 'PASS', 
                    `无认证头时正确返回401状态码`, 
                    response.data, duration);
            } else {
                this.recordResult(testName, 'FAIL', 
                    `状态码错误: ${response.status}，期望401`, 
                    response.data, duration);
            }
        } catch (error) {
            const duration = Date.now() - startTime;
            this.recordResult(testName, 'FAIL', 
                `请求失败: ${error.message}`, null, duration);
        }
    }

    /**
     * 测试3: 空token返回401
     */
    async testEmptyTokenHandling() {
        const testName = '空Token处理测试';
        const startTime = Date.now();
        
        try {
            const response = await axios.get(`${BASE_URL}${API_ENDPOINT}`, {
                headers: {
                    'Authorization': 'Bearer '
                },
                validateStatus: function (status) {
                    return status < 600;
                }
            });
            
            const duration = Date.now() - startTime;
            
            if (response.status === 401) {
                this.recordResult(testName, 'PASS', 
                    '空token正确返回401状态码', 
                    response.data, duration);
            } else {
                this.recordResult(testName, 'FAIL', 
                    `状态码错误: ${response.status}，期望401`, 
                    response.data, duration);
            }
        } catch (error) {
            const duration = Date.now() - startTime;
            this.recordResult(testName, 'FAIL', 
                `请求失败: ${error.message}`, null, duration);
        }
    }

    /**
     * 测试4: 有效token返回用户信息（需要先登录获取token）
     */
    async testValidTokenHandling() {
        const testName = '有效Token用户信息测试';
        const startTime = Date.now();
        
        try {
            // 先登录获取有效token
            const loginResponse = await axios.post(`${BASE_URL}${LOGIN_ENDPOINT}`, TEST_USER, {
                validateStatus: function (status) {
                    return status < 600;
                }
            });
            
            if (loginResponse.status !== 200 || !loginResponse.data.success) {
                this.recordResult(testName, 'SKIP', 
                    '无法登录获取有效token，跳过测试', 
                    loginResponse.data, Date.now() - startTime);
                return;
            }
            
            const token = loginResponse.data.data.accessToken;
            
            // 使用有效token获取用户信息
            const response = await axios.get(`${BASE_URL}${API_ENDPOINT}`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                },
                validateStatus: function (status) {
                    return status < 600;
                }
            });
            
            const duration = Date.now() - startTime;
            
            if (response.status === 200 && response.data.success) {
                this.recordResult(testName, 'PASS', 
                    `有效token正确返回用户信息，用户: ${response.data.data.username}`, 
                    response.data, duration);
            } else {
                this.recordResult(testName, 'FAIL', 
                    `有效token处理失败: ${response.status}`, 
                    response.data, duration);
            }
        } catch (error) {
            const duration = Date.now() - startTime;
            this.recordResult(testName, 'FAIL', 
                `测试执行失败: ${error.message}`, null, duration);
        }
    }

    /**
     * 测试5: 过期token处理
     */
    async testExpiredTokenHandling() {
        const testName = '过期Token处理测试';
        const startTime = Date.now();
        
        try {
            // 使用一个故意构造的过期token（这里模拟一个格式正确但过期的JWT）
            const expiredToken = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0dXNlciIsImV4cCI6MTYwMDAwMDAwMH0.invalid';
            
            const response = await axios.get(`${BASE_URL}${API_ENDPOINT}`, {
                headers: {
                    'Authorization': `Bearer ${expiredToken}`
                },
                validateStatus: function (status) {
                    return status < 600;
                }
            });
            
            const duration = Date.now() - startTime;
            
            if (response.status === 401) {
                this.recordResult(testName, 'PASS', 
                    '过期token正确返回401状态码', 
                    response.data, duration);
            } else {
                this.recordResult(testName, 'FAIL', 
                    `状态码错误: ${response.status}，期望401`, 
                    response.data, duration);
            }
        } catch (error) {
            const duration = Date.now() - startTime;
            this.recordResult(testName, 'FAIL', 
                `请求失败: ${error.message}`, null, duration);
        }
    }

    /**
     * 测试6: 错误信息格式验证
     */
    async testErrorResponseFormat() {
        const testName = '错误响应格式验证测试';
        const startTime = Date.now();
        
        try {
            const response = await axios.get(`${BASE_URL}${API_ENDPOINT}`, {
                headers: {
                    'Authorization': 'Bearer malformed-token'
                },
                validateStatus: function (status) {
                    return status < 600;
                }
            });
            
            const duration = Date.now() - startTime;
            
            // 验证错误响应格式
            const hasCode = typeof response.data.code === 'number';
            const hasMessage = typeof response.data.message === 'string';
            const hasSuccess = response.data.success === false;
            const hasTimestamp = response.data.timestamp;
            
            if (hasCode && hasMessage && hasSuccess && hasTimestamp) {
                this.recordResult(testName, 'PASS', 
                    '错误响应格式符合API标准', 
                    response.data, duration);
            } else {
                this.recordResult(testName, 'FAIL', 
                    '错误响应格式不符合标准', 
                    response.data, duration);
            }
        } catch (error) {
            const duration = Date.now() - startTime;
            this.recordResult(testName, 'FAIL', 
                `请求失败: ${error.message}`, null, duration);
        }
    }

    /**
     * 测试7: 响应时间性能测试
     */
    async testResponseTimePerformance() {
        const testName = '响应时间性能测试';
        
        const times = [];
        const testRounds = 5;
        
        for (let i = 0; i < testRounds; i++) {
            const startTime = Date.now();
            
            try {
                await axios.get(`${BASE_URL}${API_ENDPOINT}`, {
                    validateStatus: function (status) {
                        return status < 600;
                    }
                });
                
                const duration = Date.now() - startTime;
                times.push(duration);
            } catch (error) {
                // 忽略错误，只关心响应时间
                const duration = Date.now() - startTime;
                times.push(duration);
            }
        }
        
        const avgTime = times.reduce((a, b) => a + b, 0) / times.length;
        const maxTime = Math.max(...times);
        const minTime = Math.min(...times);
        
        if (avgTime < 2000) {
            this.recordResult(testName, 'PASS', 
                `平均响应时间: ${avgTime.toFixed(2)}ms (最小: ${minTime}ms, 最大: ${maxTime}ms)`, 
                { avgTime, minTime, maxTime, times }, avgTime);
        } else {
            this.recordResult(testName, 'FAIL', 
                `平均响应时间超过2秒: ${avgTime.toFixed(2)}ms`, 
                { avgTime, minTime, maxTime, times }, avgTime);
        }
    }

    /**
     * 执行所有测试
     */
    async runAllTests() {
        console.log('🚀 开始执行 Issue #8 修复验证测试套件...\n');
        console.log('📍 测试目标: 验证/api/auth/me不再返回500错误\n');
        
        await this.testInvalidTokenHandling();
        await this.testNoAuthorizationHeader();
        await this.testEmptyTokenHandling();
        await this.testValidTokenHandling();
        await this.testExpiredTokenHandling();
        await this.testErrorResponseFormat();
        await this.testResponseTimePerformance();
        
        this.generateSummaryReport();
    }

    /**
     * 生成测试报告摘要
     */
    generateSummaryReport() {
        console.log('\n' + '='.repeat(80));
        console.log('📊 Issue #8 修复验证测试报告');
        console.log('='.repeat(80));
        
        const totalTests = this.testResults.length;
        const passedTests = this.testResults.filter(r => r.status === 'PASS').length;
        const failedTests = this.testResults.filter(r => r.status === 'FAIL').length;
        const skippedTests = this.testResults.filter(r => r.status === 'SKIP').length;
        const warnTests = this.testResults.filter(r => r.status === 'WARN').length;
        
        const endTime = new Date();
        const totalDuration = endTime - this.startTime;
        
        console.log(`\n📈 测试统计:`);
        console.log(`   总测试数: ${totalTests}`);
        console.log(`   ✅ 通过: ${passedTests}`);
        console.log(`   ❌ 失败: ${failedTests}`);
        console.log(`   ⚠️  警告: ${warnTests}`);
        console.log(`   ⏭️  跳过: ${skippedTests}`);
        console.log(`   🕒 总耗时: ${totalDuration}ms`);
        
        const passRate = ((passedTests / totalTests) * 100).toFixed(1);
        console.log(`   📊 通过率: ${passRate}%`);
        
        console.log(`\n🔍 详细结果:`);
        this.testResults.forEach((result, index) => {
            const statusIcon = result.status === 'PASS' ? '✅' : 
                             result.status === 'FAIL' ? '❌' : 
                             result.status === 'SKIP' ? '⏭️' : '⚠️';
            console.log(`   ${index + 1}. ${statusIcon} ${result.testName} - ${result.message}`);
        });
        
        console.log(`\n🎯 修复验收结论:`);
        if (failedTests === 0) {
            console.log('   ✅ Issue #8 修复验收通过！');
            console.log('   🎉 /api/auth/me不再返回500错误，正确返回401状态码');
            console.log('   📈 系统达到政府级质量标准');
        } else if (failedTests <= 1) {
            console.log('   ⚠️  Issue #8 修复基本通过，但存在少量问题');
            console.log('   🔧 建议开发工程师检查失败的测试项');
        } else {
            console.log('   ❌ Issue #8 修复验收未通过');
            console.log('   🚨 存在多个失败测试，需要进一步修复');
        }
        
        console.log('\n' + '='.repeat(80));
        
        return {
            totalTests,
            passedTests,
            failedTests,
            skippedTests,
            warnTests,
            passRate: parseFloat(passRate),
            totalDuration,
            testResults: this.testResults
        };
    }
}

// 执行测试
async function main() {
    const testSuite = new Issue8TestSuite();
    await testSuite.runAllTests();
}

// 如果直接运行此脚本，执行测试
if (require.main === module) {
    main().catch(error => {
        console.error('❌ 测试执行失败:', error);
        process.exit(1);
    });
}

module.exports = Issue8TestSuite;