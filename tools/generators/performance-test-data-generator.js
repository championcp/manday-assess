/**
 * 性能测试数据生成器
 * 用于生成大量功能点数据，测试系统性能极限
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */

const axios = require('axios');
const fs = require('fs');
const path = require('path');

class PerformanceTestDataGenerator {
    constructor() {
        this.baseURL = 'http://localhost:8080';
        this.testResults = [];
        
        // 功能点类型配置
        this.functionPointTypes = ['ILF', 'EIF', 'EI', 'EO', 'EQ'];
        this.complexityLevels = ['LOW', 'MEDIUM', 'HIGH'];
        
        // 生成的最大数据量配置
        this.testScenarios = [
            { name: '小规模项目', functionPointCount: 50, projectCount: 10 },
            { name: '中等规模项目', functionPointCount: 200, projectCount: 5 },
            { name: '大规模项目', functionPointCount: 500, projectCount: 3 },
            { name: '超大规模项目', functionPointCount: 1000, projectCount: 2 },
            { name: '极限规模项目', functionPointCount: 2000, projectCount: 1 }
        ];
        
        console.log('性能测试数据生成器初始化完成');
    }
    
    /**
     * 生成随机功能点数据
     */
    generateRandomFunctionPoint(index) {
        const type = this.functionPointTypes[Math.floor(Math.random() * this.functionPointTypes.length)];
        
        // 根据功能点类型生成合适的复杂度数据
        let detCount, retCount, ftrCount;
        
        if (type === 'ILF' || type === 'EIF') {
            detCount = Math.floor(Math.random() * 80) + 1;  // 1-80
            retCount = Math.floor(Math.random() * 10) + 1;  // 1-10
            ftrCount = null;
        } else {
            detCount = Math.floor(Math.random() * 30) + 1;  // 1-30
            ftrCount = Math.floor(Math.random() * 6) + 1;   // 1-6
            retCount = null;
        }
        
        return {
            functionName: `性能测试功能点_${type}_${index}`,
            functionDescription: `自动生成的${type}类型功能点，用于性能测试`,
            functionPointType: type,
            detCount: detCount,
            retCount: retCount,
            ftrCount: ftrCount
        };
    }
    
    /**
     * 生成测试项目数据
     */
    generateTestProject(scenario, projectIndex) {
        const projectCode = `PERF_TEST_${scenario.name.replace(/[^a-zA-Z0-9]/g, '')}_${projectIndex}`;
        const functionPoints = [];
        
        // 生成指定数量的功能点
        for (let i = 0; i < scenario.functionPointCount; i++) {
            functionPoints.push(this.generateRandomFunctionPoint(i + 1));
        }
        
        return {
            projectCode: projectCode,
            projectName: `${scenario.name}性能测试项目_${projectIndex}`,
            projectDescription: `包含${scenario.functionPointCount}个功能点的性能测试项目`,
            projectType: 'INFORMATION_SYSTEM',
            priority: 'HIGH',
            budgetAmount: Math.floor(Math.random() * 5000000) + 1000000, // 100万-600万
            departmentName: '财政评审中心',
            projectManagerName: '性能测试管理员',
            contactPhone: '13800138000',
            contactEmail: 'perf.test@changsha.gov.cn',
            functionPoints: functionPoints
        };
    }
    
    /**
     * 创建项目并获取项目ID
     */
    async createProject(projectData) {
        try {
            console.log(`创建项目: ${projectData.projectName}`);
            
            const response = await axios.post(`${this.baseURL}/api/projects`, {
                projectCode: projectData.projectCode,
                projectName: projectData.projectName,
                projectDescription: projectData.projectDescription,
                projectType: projectData.projectType,
                priority: projectData.priority,
                budgetAmount: projectData.budgetAmount,
                departmentName: projectData.departmentName,
                projectManagerName: projectData.projectManagerName,
                contactPhone: projectData.contactPhone,
                contactEmail: projectData.contactEmail
            });
            
            if (response.data && response.data.success) {
                return response.data.data.id;
            } else {
                throw new Error('创建项目失败: ' + JSON.stringify(response.data));
            }
        } catch (error) {
            console.error('创建项目异常:', error.message);
            throw error;
        }
    }
    
    /**
     * 批量创建功能点
     */
    async createFunctionPoints(projectId, functionPoints) {
        try {
            console.log(`为项目${projectId}创建${functionPoints.length}个功能点`);
            
            const batchSize = 50; // 每批处理50个功能点
            const batches = [];
            
            for (let i = 0; i < functionPoints.length; i += batchSize) {
                batches.push(functionPoints.slice(i, i + batchSize));
            }
            
            for (let batchIndex = 0; batchIndex < batches.length; batchIndex++) {
                const batch = batches[batchIndex];
                console.log(`处理第${batchIndex + 1}/${batches.length}批功能点`);
                
                const createPromises = batch.map(fp => {
                    return axios.post(`${this.baseURL}/api/projects/${projectId}/function-points`, fp);
                });
                
                await Promise.all(createPromises);
                
                // 避免过载，每批间隔500ms
                if (batchIndex < batches.length - 1) {
                    await new Promise(resolve => setTimeout(resolve, 500));
                }
            }
            
            console.log(`项目${projectId}的功能点创建完成`);
        } catch (error) {
            console.error('批量创建功能点异常:', error.message);
            throw error;
        }
    }
    
    /**
     * 执行NESMA计算并测量性能
     */
    async performCalculationTest(projectId, expectedFunctionPointCount) {
        try {
            console.log(`开始测试项目${projectId}的计算性能 (预期${expectedFunctionPointCount}个功能点)`);
            
            const startTime = Date.now();
            
            const response = await axios.post(`${this.baseURL}/api/nesma/calculate/${projectId}`);
            
            const endTime = Date.now();
            const duration = endTime - startTime;
            
            if (response.data && response.data.success) {
                const result = response.data.data;
                
                const testResult = {
                    projectId: projectId,
                    functionPointCount: expectedFunctionPointCount,
                    calculationDuration: duration,
                    totalFunctionPoints: result.totalFunctionPoints,
                    adjustedFunctionPoints: result.adjustedFunctionPoints,
                    estimatedPersonMonths: result.estimatedPersonMonths,
                    estimatedCost: result.estimatedCost,
                    timestamp: new Date().toISOString()
                };
                
                this.testResults.push(testResult);
                
                console.log(`计算完成 - 耗时: ${duration}ms, 总功能点: ${result.totalFunctionPoints}`);
                return testResult;
            } else {
                throw new Error('计算失败: ' + JSON.stringify(response.data));
            }
        } catch (error) {
            console.error('计算性能测试异常:', error.message);
            
            const failedResult = {
                projectId: projectId,
                functionPointCount: expectedFunctionPointCount,
                calculationDuration: -1,
                error: error.message,
                timestamp: new Date().toISOString()
            };
            
            this.testResults.push(failedResult);
            return failedResult;
        }
    }
    
    /**
     * 执行完整的性能测试流程
     */
    async runPerformanceTests() {
        console.log('开始执行性能测试套件');
        console.log('测试场景:', this.testScenarios);
        
        for (const scenario of this.testScenarios) {
            console.log(`\n=== 执行测试场景: ${scenario.name} ===`);
            
            for (let i = 1; i <= scenario.projectCount; i++) {
                try {
                    console.log(`\n--- 测试项目 ${i}/${scenario.projectCount} ---`);
                    
                    // 生成测试项目数据
                    const projectData = this.generateTestProject(scenario, i);
                    
                    // 创建项目
                    const projectId = await this.createProject(projectData);
                    
                    // 创建功能点
                    await this.createFunctionPoints(projectId, projectData.functionPoints);
                    
                    // 执行计算性能测试
                    const testResult = await this.performCalculationTest(projectId, scenario.functionPointCount);
                    
                    console.log(`项目${projectId}测试完成: ${testResult.calculationDuration}ms`);
                    
                } catch (error) {
                    console.error(`测试项目${i}失败:`, error.message);
                }
            }
        }
        
        await this.generatePerformanceReport();
    }
    
    /**
     * 生成性能测试报告
     */
    async generatePerformanceReport() {
        console.log('\n=== 生成性能测试报告 ===');
        
        const report = {
            testSummary: {
                totalTests: this.testResults.length,
                successfulTests: this.testResults.filter(r => r.calculationDuration > 0).length,
                failedTests: this.testResults.filter(r => r.calculationDuration < 0).length,
                totalFunctionPointsTested: this.testResults.reduce((sum, r) => sum + r.functionPointCount, 0),
                generatedAt: new Date().toISOString()
            },
            testResults: this.testResults,
            performanceAnalysis: this.analyzePerformance()
        };
        
        // 保存报告到文件
        const reportPath = path.join(__dirname, 'performance_test_report.json');
        fs.writeFileSync(reportPath, JSON.stringify(report, null, 2));
        
        // 生成CSV格式的结果
        const csvPath = path.join(__dirname, 'performance_test_results.csv');
        this.generateCSVReport(csvPath);
        
        console.log(`性能测试报告已生成:`);
        console.log(`- JSON报告: ${reportPath}`);
        console.log(`- CSV结果: ${csvPath}`);
        
        this.printSummary(report);
    }
    
    /**
     * 分析性能数据
     */
    analyzePerformance() {
        const successfulResults = this.testResults.filter(r => r.calculationDuration > 0);
        
        if (successfulResults.length === 0) {
            return { error: '没有成功的测试结果可分析' };
        }
        
        const durations = successfulResults.map(r => r.calculationDuration);
        const functionPointCounts = successfulResults.map(r => r.functionPointCount);
        
        return {
            averageDuration: durations.reduce((a, b) => a + b, 0) / durations.length,
            minDuration: Math.min(...durations),
            maxDuration: Math.max(...durations),
            maxFunctionPoints: Math.max(...functionPointCounts),
            performanceByScale: this.analyzePerformanceByScale(successfulResults)
        };
    }
    
    /**
     * 按规模分析性能
     */
    analyzePerformanceByScale(results) {
        const scaleAnalysis = {};
        
        this.testScenarios.forEach(scenario => {
            const scaleResults = results.filter(r => r.functionPointCount === scenario.functionPointCount);
            
            if (scaleResults.length > 0) {
                const durations = scaleResults.map(r => r.calculationDuration);
                scaleAnalysis[scenario.name] = {
                    functionPointCount: scenario.functionPointCount,
                    testCount: scaleResults.length,
                    averageDuration: durations.reduce((a, b) => a + b, 0) / durations.length,
                    minDuration: Math.min(...durations),
                    maxDuration: Math.max(...durations),
                    meetsTarget: durations.every(d => d < 5000) // 5秒目标
                };
            }
        });
        
        return scaleAnalysis;
    }
    
    /**
     * 生成CSV格式报告
     */
    generateCSVReport(filePath) {
        const csvHeader = 'ProjectId,FunctionPointCount,CalculationDuration,TotalFunctionPoints,AdjustedFunctionPoints,EstimatedPersonMonths,EstimatedCost,Timestamp,Success\n';
        
        const csvRows = this.testResults.map(result => {
            return [
                result.projectId || '',
                result.functionPointCount || 0,
                result.calculationDuration || -1,
                result.totalFunctionPoints || 0,
                result.adjustedFunctionPoints || 0,
                result.estimatedPersonMonths || 0,
                result.estimatedCost || 0,
                result.timestamp || '',
                result.calculationDuration > 0 ? 'TRUE' : 'FALSE'
            ].join(',');
        });
        
        fs.writeFileSync(filePath, csvHeader + csvRows.join('\n'));
    }
    
    /**
     * 打印测试摘要
     */
    printSummary(report) {
        console.log('\n=== 性能测试摘要 ===');
        console.log(`总测试数: ${report.testSummary.totalTests}`);
        console.log(`成功测试: ${report.testSummary.successfulTests}`);
        console.log(`失败测试: ${report.testSummary.failedTests}`);
        console.log(`测试功能点总数: ${report.testSummary.totalFunctionPointsTested}`);
        
        if (report.performanceAnalysis.averageDuration) {
            console.log(`\n性能指标:`);
            console.log(`- 平均计算时间: ${Math.round(report.performanceAnalysis.averageDuration)}ms`);
            console.log(`- 最短计算时间: ${report.performanceAnalysis.minDuration}ms`);
            console.log(`- 最长计算时间: ${report.performanceAnalysis.maxDuration}ms`);
            console.log(`- 最大功能点数: ${report.performanceAnalysis.maxFunctionPoints}`);
            
            console.log(`\n按规模分析:`);
            Object.entries(report.performanceAnalysis.performanceByScale).forEach(([scale, data]) => {
                console.log(`- ${scale}: ${data.functionPointCount}功能点, 平均${Math.round(data.averageDuration)}ms, 目标达成: ${data.meetsTarget ? '是' : '否'}`);
            });
        }
    }
}

// 执行性能测试
async function main() {
    try {
        const generator = new PerformanceTestDataGenerator();
        await generator.runPerformanceTests();
    } catch (error) {
        console.error('性能测试执行失败:', error.message);
        process.exit(1);
    }
}

// 检查是否作为脚本直接运行
if (require.main === module) {
    main();
}

module.exports = PerformanceTestDataGenerator;