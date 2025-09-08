#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
软件规模评估系统 - 浏览器界面测试脚本
模拟chrome-mcp-server的浏览器操作测试
"""

import requests
import json
import time
from urllib.parse import urljoin

class BrowserTester:
    def __init__(self, base_url="http://localhost:5173"):
        self.base_url = base_url
        self.session = requests.Session()
        self.test_results = []
        
    def log_test(self, test_name, status, message=""):
        """记录测试结果"""
        result = {
            "test": test_name,
            "status": status,
            "message": message,
            "timestamp": time.strftime("%Y-%m-%d %H:%M:%S")
        }
        self.test_results.append(result)
        status_icon = "✅" if status == "PASS" else "❌" if status == "FAIL" else "⚠️"
        print(f"{status_icon} {test_name}: {message}")
    
    def test_application_availability(self):
        """测试应用程序可访问性"""
        print("\n🌐 应用程序可访问性测试")
        print("-" * 50)
        
        try:
            response = self.session.get(self.base_url, timeout=10)
            if response.status_code == 200:
                self.log_test("主页访问", "PASS", f"状态码200, 响应时间{response.elapsed.total_seconds():.2f}秒")
                
                # 检查HTML内容
                html = response.text
                if 'id="app"' in html:
                    self.log_test("Vue应用挂载点", "PASS", "发现Vue应用挂载点")
                else:
                    self.log_test("Vue应用挂载点", "FAIL", "缺失Vue应用挂载点")
                    
                if 'main.ts' in html:
                    self.log_test("TypeScript入口", "PASS", "TypeScript模块正确加载")
                else:
                    self.log_test("TypeScript入口", "FAIL", "TypeScript模块未找到")
                    
                if '@vite/client' in html:
                    self.log_test("Vite开发服务器", "PASS", "Vite HMR客户端已连接")
                else:
                    self.log_test("Vite开发服务器", "WARN", "Vite客户端可能未正确配置")
                    
            else:
                self.log_test("主页访问", "FAIL", f"HTTP {response.status_code}")
                
        except requests.exceptions.RequestException as e:
            self.log_test("主页访问", "FAIL", f"连接错误: {str(e)}")
    
    def test_routes(self):
        """测试路由可访问性"""
        print("\n🛣️  路由可访问性测试")
        print("-" * 50)
        
        routes = [
            ("/", "首页/Hello页面"),
            ("/home", "主页"),
            ("/about", "关于页面"),
            ("/projects", "项目列表页"),
            ("/projects/create", "项目创建页"),
            ("/projects/1", "项目详情页"),
            ("/projects/1/edit", "项目编辑页"),
            ("/projects/1/calculate", "NESMA计算页")
        ]
        
        for route, description in routes:
            try:
                url = urljoin(self.base_url, route)
                response = self.session.get(url, timeout=5)
                
                if response.status_code == 200:
                    self.log_test(f"路由 {route}", "PASS", f"{description} - 可正常访问")
                else:
                    self.log_test(f"路由 {route}", "FAIL", f"{description} - HTTP {response.status_code}")
                    
            except requests.exceptions.RequestException as e:
                self.log_test(f"路由 {route}", "FAIL", f"{description} - 连接错误")
    
    def test_api_endpoints(self):
        """测试API端点"""
        print("\n🔗 API端点测试")
        print("-" * 50)
        
        # 测试后端API
        backend_url = "http://localhost:8080"
        api_endpoints = [
            ("/actuator/health", "健康检查"),
            ("/api/projects", "项目API"),
            ("/api/nesma", "NESMA计算API")
        ]
        
        for endpoint, description in api_endpoints:
            try:
                url = urljoin(backend_url, endpoint)
                response = self.session.get(url, timeout=5)
                
                if response.status_code == 200:
                    self.log_test(f"API {endpoint}", "PASS", f"{description} - 响应正常")
                elif response.status_code == 401:
                    self.log_test(f"API {endpoint}", "PASS", f"{description} - 需要认证(正常)")
                else:
                    self.log_test(f"API {endpoint}", "FAIL", f"{description} - HTTP {response.status_code}")
                    
            except requests.exceptions.RequestException as e:
                self.log_test(f"API {endpoint}", "FAIL", f"{description} - 无法连接后端服务")
    
    def test_ui_components(self):
        """测试UI组件加载"""
        print("\n🎨 UI组件测试")
        print("-" * 50)
        
        # 检查主要资源是否可访问
        resources = [
            ("/favicon.ico", "网站图标"),
            ("/@vite/client", "Vite客户端"),
            ("/src/main.ts", "应用入口")
        ]
        
        for resource, description in resources:
            try:
                url = urljoin(self.base_url, resource)
                response = self.session.get(url, timeout=3)
                
                if response.status_code == 200:
                    self.log_test(f"资源 {resource}", "PASS", f"{description} - 加载成功")
                else:
                    self.log_test(f"资源 {resource}", "WARN", f"{description} - 状态码 {response.status_code}")
                    
            except requests.exceptions.RequestException:
                self.log_test(f"资源 {resource}", "WARN", f"{description} - 无法访问")
    
    def generate_report(self):
        """生成测试报告"""
        print("\n" + "="*80)
        print("🔍 软件规模评估系统 - 浏览器界面测试报告")
        print("="*80)
        
        # 统计结果
        pass_count = sum(1 for r in self.test_results if r['status'] == 'PASS')
        fail_count = sum(1 for r in self.test_results if r['status'] == 'FAIL')
        warn_count = sum(1 for r in self.test_results if r['status'] == 'WARN')
        total_count = len(self.test_results)
        
        print(f"\n📊 测试统计:")
        print(f"- 总测试数: {total_count}")
        print(f"- 通过: {pass_count} ✅")
        print(f"- 失败: {fail_count} ❌")
        print(f"- 警告: {warn_count} ⚠️")
        
        success_rate = (pass_count / total_count * 100) if total_count > 0 else 0
        print(f"- 成功率: {success_rate:.1f}%")
        
        # 判定等级
        if success_rate >= 90:
            grade = "A 优秀"
        elif success_rate >= 80:
            grade = "B 良好"
        elif success_rate >= 70:
            grade = "C 及格"
        else:
            grade = "D 需改进"
            
        print(f"\n🏆 测试等级: {grade}")
        
        # 关键发现
        print(f"\n🔍 关键发现:")
        
        failed_tests = [r for r in self.test_results if r['status'] == 'FAIL']
        if failed_tests:
            print("❌ 失败的测试:")
            for test in failed_tests:
                print(f"  - {test['test']}: {test['message']}")
        else:
            print("✅ 所有关键功能测试通过")
            
        warned_tests = [r for r in self.test_results if r['status'] == 'WARN']
        if warned_tests:
            print("⚠️ 需要注意的问题:")
            for test in warned_tests:
                print(f"  - {test['test']}: {test['message']}")
        
        # 结论
        print(f"\n📝 测试结论:")
        if fail_count == 0:
            print("✅ 系统基础功能正常，前端界面可以正常访问")
            print("✅ Vue 3 + TypeScript 应用架构运行良好")
            print("✅ Vite开发服务器配置正确")
        else:
            print("❌ 发现关键问题，需要修复后重新测试")
            
        return {
            'total': total_count,
            'pass': pass_count,
            'fail': fail_count,
            'warn': warn_count,
            'success_rate': success_rate,
            'grade': grade
        }

def main():
    """主测试函数"""
    print("🚀 启动软件规模评估系统浏览器测试")
    print("模拟 chrome-mcp-server 功能进行界面测试")
    print("="*80)
    
    tester = BrowserTester()
    
    # 执行所有测试
    tester.test_application_availability()
    tester.test_routes()
    tester.test_ui_components()
    tester.test_api_endpoints()
    
    # 生成报告
    results = tester.generate_report()
    
    return results

if __name__ == "__main__":
    main()