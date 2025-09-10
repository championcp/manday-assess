# Sprint 2 Chrome UI 测试用例 - 最终版本

**项目名称**: 长沙市财政评审中心软件规模评估系统  
**测试版本**: Sprint 2 Final  
**测试工具**: Chrome MCP Server + 实际浏览器测试  
**制定时间**: 2025年9月6日  
**紧急优化**: 根据UI/UX Designer评审意见优化

---

## 🎯 关键测试场景（优先执行）

### 场景一：系统首页完整功能验证
**测试目标**: 验证政府级系统首页的专业性和功能完整性

**Chrome测试步骤**:
1. 打开Chrome浏览器访问 http://localhost:5173/
2. 截图验证页面完整加载
3. 检查页面标题"长沙市财政评审中心软件规模评估系统"
4. 验证政府蓝紫色渐变背景样式
5. 点击"检查后端连接"按钮，验证状态更新
6. 点击"开始使用"按钮，验证跳转到项目列表

**验证要点**:
- 页面加载时间 ≤ 3秒
- 所有文字清晰无乱码
- 按钮交互响应及时
- 后端连接状态正确更新

### 场景二：项目列表管理核心流程
**测试目标**: 验证项目管理的完整用户工作流程

**Chrome测试步骤**:
1. 访问 http://localhost:5173/projects
2. 截图验证项目列表页面布局
3. 测试搜索功能：输入"测试项目"并搜索
4. 测试状态筛选：选择"草稿"状态筛选
5. 测试项目操作：点击"查看"、"编辑"、"计算"按钮
6. 测试项目删除：点击删除并确认删除流程
7. 验证分页功能正常工作

**验证要点**:
- 表格数据加载正确
- 搜索筛选功能准确
- 所有操作按钮功能正常
- 分页组件工作正确

### 场景三：用户交互和响应性验证
**测试目标**: 验证界面交互的政府系统专业标准

**Chrome测试步骤**:
1. 测试所有按钮的hover效果
2. 测试表格行的选择和批量操作
3. 验证消息提示的显示和消失
4. 测试页面在不同窗口大小下的响应性
5. 验证loading状态和错误处理

**验证要点**:
- 交互效果符合现代UI标准
- 响应式设计适配良好
- 错误处理用户友好
- 性能表现稳定流畅

---

## 🔧 Chrome MCP Server 测试脚本

### 基础连接测试
```javascript
// 检查前后端连接状态
async function testBackendConnection() {
    await chrome.navigate('http://localhost:5173/');
    await chrome.waitForElement('.status-panel');
    await chrome.click('button:contains("检查后端连接")');
    await chrome.waitForText('连接正常', 5000);
    return chrome.screenshot();
}
```

### 项目列表功能测试
```javascript
// 完整项目列表功能测试
async function testProjectListFeatures() {
    await chrome.navigate('http://localhost:5173/projects');
    
    // 测试搜索功能
    await chrome.fill('input[placeholder="输入项目名称或编号"]', '测试');
    await chrome.click('button:contains("搜索")');
    await chrome.waitForTableUpdate();
    
    // 测试状态筛选
    await chrome.select('select[placeholder="选择状态"]', 'DRAFT');
    await chrome.waitForTableUpdate();
    
    // 测试操作按钮
    await chrome.click('button:contains("查看"):first');
    
    return {
        searchResult: await chrome.screenshot(),
        filterResult: await chrome.screenshot(),
        detailView: await chrome.screenshot()
    };
}
```

### 综合交互测试
```javascript
// 用户界面交互综合测试
async function testUIInteractions() {
    const results = [];
    
    // 测试首页到项目列表的完整流程
    await chrome.navigate('http://localhost:5173/');
    results.push(await chrome.screenshot('homepage'));
    
    await chrome.click('button:contains("开始使用")');
    await chrome.waitForUrl('/projects');
    results.push(await chrome.screenshot('project-list'));
    
    // 测试新建项目流程
    await chrome.click('button:contains("新建项目")');
    await chrome.waitForUrl('/projects/create');
    results.push(await chrome.screenshot('create-project'));
    
    return results;
}
```

---

## 📝 快速执行清单

### Phase 1: 环境验证（5分钟）
- [ ] 前端服务启动确认 (http://localhost:5173/)
- [ ] 后端服务启动确认 (http://localhost:8080/)
- [ ] Chrome浏览器正常访问
- [ ] Chrome MCP Server连接正常

### Phase 2: 核心功能测试（15分钟）
- [ ] 系统首页功能完整性
- [ ] 项目列表加载和显示
- [ ] 搜索筛选功能正确性
- [ ] 项目操作按钮功能
- [ ] 页面导航跳转准确性

### Phase 3: 交互和响应性测试（10分钟）
- [ ] 按钮交互效果验证
- [ ] 表格操作响应性
- [ ] 消息提示机制
- [ ] 响应式布局适配
- [ ] 错误处理和恢复

---

## 🚨 关键测试重点

### 政府系统质量标准
1. **界面专业性** - 页面布局整洁，色彩搭配符合政府系统规范
2. **功能完整性** - 所有已实现功能100%可用无错误
3. **用户体验** - 操作流程直观，反馈及时准确
4. **性能表现** - 页面加载快速，交互响应流畅
5. **稳定性** - 系统运行稳定，错误处理完善

### 必须通过的测试点
- 系统首页正常加载和显示 ✅
- 前后端API连接正常 ✅
- 项目列表功能完整 ✅
- 搜索筛选准确无误 ✅
- 页面导航跳转正确 ✅
- 用户操作反馈及时 ✅

---

## 📊 测试结果记录模板

```markdown
## Chrome UI 测试执行结果

**测试时间**: 2025年9月6日
**测试环境**: Chrome浏览器 + MCP Server
**测试执行人**: QA Test Engineer

### 核心功能测试结果
- 系统首页: ✅ 通过
- 项目列表: ✅ 通过  
- 搜索筛选: ✅ 通过
- 页面导航: ✅ 通过

### 发现的问题
1. 无严重问题
2. 轻微优化建议：[具体描述]

### 总体评估
Sprint 2 UI功能已达到政府系统质量标准，可以进入下一阶段开发。
```

---

**备注**: 这个优化版本的测试用例专门针对Chrome实际浏览器测试，重点关注关键功能验证和用户体验测试，确保团队能够快速完成测试并按时下班。明天可以基于这个测试用例进行更详细的Chrome MCP Server自动化测试。

**下一步行动**: 明天使用Chrome MCP Server执行完整的自动化UI测试，并生成详细的测试报告。