/**
 * Issue #9 项目管理API权限修复验证测试
 * 
 * QA Test Engineer - Issue #9 权限配置错误修复验证
 * 
 * 测试目标：验证管理员用户对项目管理API的权限修复
 * GitHub Issue: https://github.com/championcp/manday-assess/issues/9
 * 修复分支: fix/issue-9-project-api-permissions
 * 
 * @author QA Test Engineer
 * @date 2025-09-12
 */

const axios = require('axios');
const fs = require('fs');

// 测试配置
const config = {
    baseURL: 'http://localhost:8080/api',
    timeout: 30000,
    headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
    }
};

// 创建axios实例
const api = axios.create(config);

// 测试用户凭据 - 从环境变量获取，避免硬编码安全敏感信息
const testUsers = {
    admin: {
        username: process.env.TEST_ADMIN_USER || 'admin',
        password: process.env.TEST_ADMIN_PASS || 'default_admin_pass',
        role: 'ADMIN',
        description: '系统管理员 - 应该拥有所有项目管理权限'
    },
    projectManager: {
        username: process.env.TEST_PM_USER || 'pm',
        password: process.env.TEST_PM_PASS || 'default_pm_pass',  
        role: 'PROJECT_MANAGER',
        description: '项目经理 - 应该能创建和管理项目'
    },
    assessor: {
        username: process.env.TEST_ASSESSOR_USER || 'assessor',
        password: process.env.TEST_ASSESSOR_PASS || 'default_assessor_pass',
        role: 'ASSESSOR', 
        description: '评估人员 - 应该只能查看项目'
    },
    user: {
        username: process.env.TEST_USER_USER || 'user',
        password: process.env.TEST_USER_PASS || 'default_user_pass',
        role: 'USER',
        description: '普通用户 - 不应该能访问项目API'
    }
};

// 测试结果记录
let testResults = {
    timestamp: new Date().toISOString(),
    issue: 'Issue #9: 项目管理API权限配置错误',
    branch: 'fix/issue-9-project-api-permissions',
    totalTests: 0,
    passedTests: 0,
    failedTests: 0,
    results: []
};

/**
 * 记录测试结果
 */
function logTestResult(testName, status, details, user = null) {
    testResults.totalTests++;
    if (status === 'PASS') {
        testResults.passedTests++;
    } else {
        testResults.failedTests++;
    }
    
    const result = {
        testName,
        status,
        details,
        user: user ? `${user.username} (${user.role})` : null,
        timestamp: new Date().toISOString()
    };
    
    testResults.results.push(result);
    console.log(`${status === 'PASS' ? '✅' : '❌'} ${testName}: ${details}`);
}

/**
 * 用户登录获取JWT Token
 */
async function loginUser(user) {
    try {
        console.log(`\n🔐 尝试登录用户: ${user.username} (${user.role})`);
        
        const response = await api.post('/auth/login', {
            username: user.username,
            password: user.password
        });

        if (response.data && response.data.token) {
            console.log(`✅ ${user.username} 登录成功`);
            return response.data.token;
        } else {
            console.log(`❌ ${user.username} 登录失败: 未返回token`);
            return null;
        }
    } catch (error) {
        console.log(`❌ ${user.username} 登录失败: ${error.response?.data?.message || error.message}`);
        return null;
    }
}

/**
 * 测试项目列表API访问权限
 */
async function testProjectListAccess(user, token) {
    try {
        const response = await api.get('/projects', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.status === 200) {
            logTestResult(
                `${user.role}用户访问项目列表`,
                'PASS',
                `状态码: ${response.status}, 成功访问项目列表API`,
                user
            );
            return true;
        } else {
            logTestResult(
                `${user.role}用户访问项目列表`,
                'FAIL',
                `预期200状态码，实际: ${response.status}`,
                user
            );
            return false;
        }
    } catch (error) {
        const status = error.response?.status;
        if (user.role === 'USER' && status === 403) {
            // 普通用户被拒绝是正确的
            logTestResult(
                `${user.role}用户访问项目列表`,
                'PASS',
                `状态码: ${status} (Forbidden) - 普通用户正确被拒绝访问`,
                user
            );
            return true;
        } else if (['ADMIN', 'PROJECT_MANAGER', 'ASSESSOR'].includes(user.role) && status === 403) {
            // 这些用户被拒绝是错误的
            logTestResult(
                `${user.role}用户访问项目列表`,
                'FAIL',
                `状态码: ${status} (Forbidden) - ${user.role}用户不应该被拒绝访问`,
                user
            );
            return false;
        } else {
            logTestResult(
                `${user.role}用户访问项目列表`,
                'FAIL', 
                `请求失败: ${error.response?.data?.message || error.message}`,
                user
            );
            return false;
        }
    }
}

/**
 * 测试项目创建API权限
 */
async function testProjectCreateAccess(user, token) {
    const projectData = {
        projectName: `${user.role}权限测试项目`,
        description: `Issue #9 权限修复验证 - ${user.description}`,
        projectType: 'WEB_APPLICATION'
    };

    try {
        const response = await api.post('/projects', projectData, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.status === 200 || response.status === 201) {
            if (['ADMIN', 'PROJECT_MANAGER'].includes(user.role)) {
                logTestResult(
                    `${user.role}用户创建项目`,
                    'PASS',
                    `状态码: ${response.status}, ${user.role}成功创建项目`,
                    user
                );
                return response.data.data?.id || true;
            } else {
                logTestResult(
                    `${user.role}用户创建项目`,
                    'FAIL',
                    `状态码: ${response.status} - ${user.role}用户不应该能创建项目`,
                    user
                );
                return false;
            }
        }
    } catch (error) {
        const status = error.response?.status;
        if (['ASSESSOR', 'USER'].includes(user.role) && status === 403) {
            // 评估人员和普通用户被拒绝是正确的
            logTestResult(
                `${user.role}用户创建项目`,
                'PASS',
                `状态码: ${status} (Forbidden) - ${user.role}用户正确被拒绝创建项目`,
                user
            );
            return true;
        } else if (['ADMIN', 'PROJECT_MANAGER'].includes(user.role) && status === 403) {
            // 这是Issue #9的核心问题
            logTestResult(
                `${user.role}用户创建项目`,
                'FAIL',
                `状态码: ${status} (Forbidden) - Issue #9核心问题：${user.role}用户不应该被拒绝创建项目`,
                user
            );
            return false;
        } else {
            logTestResult(
                `${user.role}用户创建项目`,
                'FAIL',
                `请求失败: ${error.response?.data?.message || error.message}`,
                user
            );
            return false;
        }
    }
}

/**
 * 测试项目详情查看权限
 */
async function testProjectViewAccess(user, token, projectId = 1) {
    try {
        const response = await api.get(`/projects/${projectId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (response.status === 200) {
            logTestResult(
                `${user.role}用户查看项目详情`,
                'PASS',
                `状态码: ${response.status}, 成功查看项目详情`,
                user
            );
            return true;
        } else if (response.status === 404) {
            // 项目不存在是可以接受的，但不应该是403权限错误
            logTestResult(
                `${user.role}用户查看项目详情`,
                'PASS',
                `状态码: ${response.status} (Not Found) - 项目不存在，但权限验证通过`,
                user
            );
            return true;
        }
    } catch (error) {
        const status = error.response?.status;
        if (user.role === 'USER' && status === 403) {
            logTestResult(
                `${user.role}用户查看项目详情`,
                'PASS',
                `状态码: ${status} (Forbidden) - 普通用户正确被拒绝查看`,
                user
            );
            return true;
        } else if (['ADMIN', 'PROJECT_MANAGER', 'ASSESSOR'].includes(user.role) && status === 403) {
            logTestResult(
                `${user.role}用户查看项目详情`,
                'FAIL',
                `状态码: ${status} (Forbidden) - ${user.role}用户不应该被拒绝查看项目`,
                user
            );
            return false;
        } else if (status === 404) {
            // 项目不存在，但权限通过了
            logTestResult(
                `${user.role}用户查看项目详情`,
                'PASS',
                `状态码: ${status} (Not Found) - 项目不存在，但权限验证通过`,
                user
            );
            return true;
        } else {
            logTestResult(
                `${user.role}用户查看项目详情`,
                'FAIL',
                `请求失败: ${error.response?.data?.message || error.message}`,
                user
            );
            return false;
        }
    }
}

/**
 * 测试无认证访问拒绝
 */
async function testUnauthenticatedAccess() {
    console.log(`\n🔒 测试未认证访问拒绝...`);
    
    try {
        // 测试无token访问项目列表
        await api.get('/projects');
        logTestResult(
            '未认证用户访问项目列表',
            'FAIL',
            '未认证用户不应该能访问项目列表'
        );
    } catch (error) {
        if (error.response?.status === 401) {
            logTestResult(
                '未认证用户访问项目列表',
                'PASS',
                '状态码: 401 (Unauthorized) - 正确拒绝未认证访问'
            );
        } else {
            logTestResult(
                '未认证用户访问项目列表',
                'FAIL',
                `预期401状态码，实际: ${error.response?.status || error.message}`
            );
        }
    }

    try {
        // 测试无token创建项目
        await api.post('/projects', {
            projectName: '未认证测试项目'
        });
        logTestResult(
            '未认证用户创建项目',
            'FAIL',
            '未认证用户不应该能创建项目'
        );
    } catch (error) {
        if (error.response?.status === 401) {
            logTestResult(
                '未认证用户创建项目',
                'PASS',
                '状态码: 401 (Unauthorized) - 正确拒绝未认证访问'
            );
        } else {
            logTestResult(
                '未认证用户创建项目',
                'FAIL',
                `预期401状态码，实际: ${error.response?.status || error.message}`
            );
        }
    }
}

/**
 * 测试用户权限边界
 */
async function testUserPermissionBoundaries() {
    console.log(`\n🛡️  开始权限边界测试...`);
    
    for (const userKey in testUsers) {
        const user = testUsers[userKey];
        console.log(`\n📋 测试用户: ${user.username} (${user.role})`);
        console.log(`   描述: ${user.description}`);
        
        // 登录用户
        const token = await loginUser(user);
        if (!token) {
            logTestResult(
                `${user.role}用户登录`,
                'FAIL',
                '登录失败，无法进行后续权限测试',
                user
            );
            continue;
        }

        // 测试项目列表访问
        await testProjectListAccess(user, token);
        
        // 测试项目创建权限
        await testProjectCreateAccess(user, token);
        
        // 测试项目详情查看
        await testProjectViewAccess(user, token);
        
        await new Promise(resolve => setTimeout(resolve, 1000)); // 避免请求过快
    }
}

/**
 * 生成测试报告
 */
function generateTestReport() {
    console.log(`\n📊 ==================== Issue #9 权限修复测试报告 ====================`);
    console.log(`🎯 测试目标: 验证管理员用户对项目管理API的权限修复`);
    console.log(`📅 测试时间: ${testResults.timestamp}`);
    console.log(`🔧 修复分支: ${testResults.branch}`);
    console.log(`📈 测试统计:`);
    console.log(`   总测试数: ${testResults.totalTests}`);
    console.log(`   通过: ${testResults.passedTests} ✅`);
    console.log(`   失败: ${testResults.failedTests} ❌`);
    console.log(`   成功率: ${((testResults.passedTests / testResults.totalTests) * 100).toFixed(1)}%`);

    // 核心修复验证
    const coreIssueTests = testResults.results.filter(r => 
        (r.user && r.user.includes('ADMIN')) && r.testName.includes('创建项目')
    );
    
    const adminCreateTest = coreIssueTests.find(t => t.user.includes('ADMIN'));
    console.log(`\n🎯 Issue #9 核心问题验证:`);
    if (adminCreateTest) {
        if (adminCreateTest.status === 'PASS') {
            console.log(`   ✅ 管理员用户项目创建权限: 已修复`);
            console.log(`   📝 说明: 管理员用户现在可以正常创建项目，不再返回403错误`);
        } else {
            console.log(`   ❌ 管理员用户项目创建权限: 仍有问题`);
            console.log(`   📝 说明: ${adminCreateTest.details}`);
        }
    }

    // 保存详细测试报告
    const reportFile = `/Users/chengpeng/traeWorkspace/manday-assess/agile-workspace/qa-test-engineer/issue-9-test-report.md`;
    const reportContent = generateMarkdownReport();
    fs.writeFileSync(reportFile, reportContent, 'utf8');
    
    console.log(`\n📄 详细测试报告已保存: ${reportFile}`);
    console.log(`===============================================================\n`);
    
    return testResults.failedTests === 0;
}

/**
 * 生成Markdown格式测试报告
 */
function generateMarkdownReport() {
    const successRate = ((testResults.passedTests / testResults.totalTests) * 100).toFixed(1);
    
    let markdown = `# Issue #9 项目管理API权限修复测试报告

**QA Test Engineer测试报告**

---

## 🎯 测试概述

- **GitHub Issue:** https://github.com/championcp/manday-assess/issues/9
- **问题描述:** 管理员账户无法访问项目创建、详情等API，返回403权限错误
- **修复分支:** fix/issue-9-project-api-permissions
- **测试时间:** ${testResults.timestamp}
- **测试人员:** QA Test Engineer

## 📊 测试统计

| 指标 | 数值 |
|------|------|
| 总测试数 | ${testResults.totalTests} |
| 通过测试 | ${testResults.passedTests} ✅ |
| 失败测试 | ${testResults.failedTests} ❌ |
| **成功率** | **${successRate}%** |

## 🔍 核心问题验证

`;

    // 分析核心问题
    const adminTests = testResults.results.filter(r => r.user && r.user.includes('ADMIN'));
    const adminCreateTest = adminTests.find(t => t.testName.includes('创建项目'));
    
    if (adminCreateTest) {
        if (adminCreateTest.status === 'PASS') {
            markdown += `### ✅ Issue #9 核心问题已解决

**管理员用户项目创建权限:** 修复成功

- **测试结果:** ${adminCreateTest.details}
- **验证状态:** 通过 ✅
- **说明:** 管理员用户现在可以正常创建项目，不再出现403权限错误

`;
        } else {
            markdown += `### ❌ Issue #9 核心问题仍存在

**管理员用户项目创建权限:** 修复失败

- **测试结果:** ${adminCreateTest.details} 
- **验证状态:** 失败 ❌
- **说明:** 管理员用户仍然无法创建项目，需要进一步调查修复

`;
        }
    }

    // 权限矩阵测试结果
    markdown += `## 🛡️ 权限矩阵验证结果

| 用户角色 | 项目列表 | 项目创建 | 项目查看 | 符合预期 |
|----------|----------|----------|----------|----------|
`;

    for (const userKey in testUsers) {
        const user = testUsers[userKey];
        const userTests = testResults.results.filter(r => r.user && r.user.includes(user.role));
        
        const listTest = userTests.find(t => t.testName.includes('项目列表'));
        const createTest = userTests.find(t => t.testName.includes('创建项目'));
        const viewTest = userTests.find(t => t.testName.includes('项目详情'));
        
        const listResult = listTest ? (listTest.status === 'PASS' ? '✅' : '❌') : '❓';
        const createResult = createTest ? (createTest.status === 'PASS' ? '✅' : '❌') : '❓';
        const viewResult = viewTest ? (viewTest.status === 'PASS' ? '✅' : '❌') : '❓';
        
        const allPassed = [listTest, createTest, viewTest].every(t => t && t.status === 'PASS');
        const overallResult = allPassed ? '✅ 是' : '❌ 否';
        
        markdown += `| ${user.role} | ${listResult} | ${createResult} | ${viewResult} | ${overallResult} |\n`;
    }

    // 详细测试结果
    markdown += `\n## 📋 详细测试结果\n\n`;
    
    testResults.results.forEach((result, index) => {
        const status = result.status === 'PASS' ? '✅' : '❌';
        const userInfo = result.user ? ` (${result.user})` : '';
        
        markdown += `### ${index + 1}. ${result.testName}${userInfo}\n\n`;
        markdown += `- **状态:** ${status} ${result.status}\n`;
        markdown += `- **详情:** ${result.details}\n`;
        markdown += `- **时间:** ${result.timestamp}\n\n`;
    });

    // 修复效果评估
    markdown += `## 📈 修复效果评估\n\n`;
    
    if (testResults.failedTests === 0) {
        markdown += `### ✅ 修复完全成功\n\n`;
        markdown += `- 所有权限测试均通过\n`;
        markdown += `- Issue #9 核心问题已完全解决\n`;
        markdown += `- 用户权限边界配置正确\n`;
        markdown += `- **建议:** 可以安全合并到主分支\n\n`;
    } else if (testResults.passedTests > testResults.failedTests) {
        markdown += `### ⚠️ 修复基本成功，存在细节问题\n\n`;
        markdown += `- 大部分权限测试通过\n`;
        markdown += `- 仍有 ${testResults.failedTests} 个问题需要解决\n`;
        markdown += `- **建议:** 修复剩余问题后再合并\n\n`;
    } else {
        markdown += `### ❌ 修复不完全，需要进一步调查\n\n`;
        markdown += `- ${testResults.failedTests} 个测试失败\n`;
        markdown += `- Issue #9 核心问题可能未完全解决\n`;
        markdown += `- **建议:** 重新检查权限配置和代码修复\n\n`;
    }

    // QA验收建议
    markdown += `## 🎯 QA验收建议\n\n`;
    
    if (testResults.failedTests === 0) {
        markdown += `### ✅ 验收通过\n\n`;
        markdown += `**验收结论:** Issue #9 修复质量优秀，满足所有验收标准\n\n`;
        markdown += `**验收标准达成情况:**\n`;
        markdown += `- ✅ 管理员能正常创建项目（不再返回403）\n`;
        markdown += `- ✅ 管理员能查看项目详情\n`;
        markdown += `- ✅ 权限配置与用户角色正确匹配\n`;
        markdown += `- ✅ 其他角色权限不受影响\n`;
        markdown += `- ✅ 权限验证逻辑清晰且安全\n\n`;
        markdown += `**后续建议:**\n`;
        markdown += `- 可以安全合并到master分支\n`;
        markdown += `- 建议在生产环境进行最终验证\n`;
        markdown += `- 更新相关技术文档\n\n`;
    } else {
        markdown += `### ❌ 验收不通过\n\n`;
        markdown += `**验收结论:** Issue #9 修复仍存在问题，需要进一步完善\n\n`;
        markdown += `**需要解决的问题:**\n`;
        testResults.results.filter(r => r.status === 'FAIL').forEach(result => {
            markdown += `- ❌ ${result.testName}: ${result.details}\n`;
        });
        markdown += `\n**后续建议:**\n`;
        markdown += `- 暂缓合并到master分支\n`;
        markdown += `- Developer Engineer需要进一步修复问题\n`;
        markdown += `- 修复完成后重新进行QA测试\n\n`;
    }

    markdown += `---\n\n`;
    markdown += `**测试签名:** QA Test Engineer  \n`;
    markdown += `**测试完成时间:** ${new Date().toISOString()}  \n`;
    markdown += `**质量评级:** ${testResults.failedTests === 0 ? 'A级（优秀）' : testResults.passedTests > testResults.failedTests ? 'B级（良好）' : 'C级（需要改进）'}  \n`;

    return markdown;
}

/**
 * 主测试函数
 */
async function runTests() {
    console.log('🚀 开始 Issue #9 项目管理API权限修复验证测试...\n');
    console.log('📋 测试计划:');
    console.log('   1. 测试未认证访问拒绝');
    console.log('   2. 测试不同用户角色的权限边界');
    console.log('   3. 重点验证管理员用户的项目管理权限');
    console.log('   4. 生成详细测试报告\n');

    try {
        // 测试未认证访问
        await testUnauthenticatedAccess();
        
        // 测试用户权限边界
        await testUserPermissionBoundaries();
        
        // 生成测试报告
        const allTestsPassed = generateTestReport();
        
        if (allTestsPassed) {
            console.log('🎉 Issue #9 权限修复验证 - 所有测试通过！');
            process.exit(0);
        } else {
            console.log('⚠️ Issue #9 权限修复验证 - 存在测试失败，需要进一步修复');
            process.exit(1);
        }
        
    } catch (error) {
        console.error('❌ 测试执行过程中发生错误:', error);
        logTestResult(
            '测试执行异常',
            'FAIL',
            `测试执行异常: ${error.message}`
        );
        generateTestReport();
        process.exit(1);
    }
}

// 启动测试
if (require.main === module) {
    runTests();
}

module.exports = {
    runTests,
    testUsers,
    config
};