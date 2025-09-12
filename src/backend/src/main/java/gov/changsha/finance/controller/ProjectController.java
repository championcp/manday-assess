package gov.changsha.finance.controller;

import gov.changsha.finance.dto.ApiResponse;
import gov.changsha.finance.dto.response.ProjectResponse;
import gov.changsha.finance.entity.SimpleProject;
import gov.changsha.finance.repository.SimpleProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 项目管理控制器
 * 提供项目的CRUD操作和查询功能
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private SimpleProjectRepository projectRepository;

    /**
     * 获取项目列表（分页）
     * 需要项目查看权限
     */
    @GetMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'ASSESSOR')")
    public ApiResponse<Map<String, Object>> getProjectList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        
        try {
            // 记录访问日志
            String currentUser = getCurrentUsername();
            logger.info("用户 {} 请求项目列表, current={}, size={}, keyword={}, status={}", 
                       currentUser, current, size, keyword, status);
            
            // 构建分页对象
            Pageable pageable = PageRequest.of(current - 1, size, 
                Sort.by(Sort.Direction.DESC, "createdAt"));
            
            // 根据条件查询项目
            Page<SimpleProject> projectPage;
            if ((keyword != null && !keyword.trim().isEmpty()) || (status != null && !status.trim().isEmpty())) {
                // 有筛选条件时使用条件查询
                logger.debug("使用条件查询 - keyword={}, status={}", keyword, status);
                projectPage = projectRepository.findProjectsWithFilters(keyword, status, pageable);
            } else {
                // 无筛选条件时查询所有项目
                logger.debug("使用普通查询");
                projectPage = projectRepository.findByDeletedFalse(pageable);
            }
            
            // 转换为响应DTO
            List<Map<String, Object>> records = projectPage.getContent().stream()
                .map(this::convertToListItem)
                .collect(Collectors.toList());
            
            // 构建分页响应
            Map<String, Object> result = new HashMap<>();
            result.put("records", records);
            result.put("total", projectPage.getTotalElements());
            result.put("current", current);
            result.put("size", size);
            result.put("pages", projectPage.getTotalPages());
            
            logger.info("用户 {} 成功获取项目列表，共 {} 条记录", currentUser, projectPage.getTotalElements());
            return ApiResponse.success("获取项目列表成功", result);
            
        } catch (Exception e) {
            logger.error("获取项目列表失败", e);
            return ApiResponse.error("获取项目列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取项目详情
     * 需要项目查看权限
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER', 'ASSESSOR')")
    public ApiResponse<ProjectResponse> getProject(@PathVariable Long id) {
        try {
            String currentUser = getCurrentUsername();
            logger.info("用户 {} 请求项目详情, id={}", currentUser, id);
            
            SimpleProject project = projectRepository.findByIdAndDeletedFalse(id);
            if (project == null) {
                logger.warn("用户 {} 请求的项目不存在: id={}", currentUser, id);
                return ApiResponse.error("项目不存在");
            }
            
            ProjectResponse response = convertToResponse(project);
            logger.info("用户 {} 成功获取项目详情: {}", currentUser, project.getName());
            return ApiResponse.success("获取项目详情成功", response);
            
        } catch (Exception e) {
            logger.error("获取项目详情失败, id=" + id, e);
            return ApiResponse.error("获取项目详情失败: " + e.getMessage());
        }
    }

    /**
     * 创建项目
     * 需要项目创建权限（仅管理员和项目经理）
     */
    @PostMapping("")
    @PreAuthorize("hasAnyRole('ADMIN', 'PROJECT_MANAGER')")
    public ApiResponse<ProjectResponse> createProject(@RequestBody Map<String, Object> request) {
        try {
            String currentUser = getCurrentUsername();
            String projectName = (String) request.get("projectName");
            logger.info("用户 {} 请求创建项目: {}", currentUser, projectName);
            
            // 验证必要参数
            if (projectName == null || projectName.trim().isEmpty()) {
                logger.warn("用户 {} 创建项目失败：项目名称不能为空", currentUser);
                return ApiResponse.error("项目名称不能为空");
            }
            
            // 创建新项目
            SimpleProject project = new SimpleProject();
            project.setName(projectName.trim());
            project.setDescription((String) request.get("description"));
            project.setProjectCode(generateProjectCode());
            project.setStatus("DRAFT");
            project.setComplexityLevel("MEDIUM");
            project.setCreatedBy(1L); // TODO: 使用实际当前用户ID
            project.setUpdatedBy(1L); // TODO: 使用实际当前用户ID
            project.setCreatedAt(LocalDateTime.now());
            project.setUpdatedAt(LocalDateTime.now());
            project.setDeleted(false);
            project.setCurrentVersion(1);
            project.setTotalFunctionPoints(BigDecimal.ZERO);
            project.setTotalDevelopmentHours(BigDecimal.ZERO);
            project.setTotalCostEstimate(BigDecimal.ZERO);
            
            project = projectRepository.save(project);
            
            ProjectResponse response = convertToResponse(project);
            logger.info("用户 {} 成功创建项目: {} (ID: {})", currentUser, project.getName(), project.getId());
            return ApiResponse.success("创建项目成功", response);
            
        } catch (Exception e) {
            logger.error("创建项目失败", e);
            return ApiResponse.error("创建项目失败: " + e.getMessage());
        }
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public ApiResponse<ProjectResponse> updateProject(@PathVariable Long id, 
                                                     @RequestBody Map<String, Object> request) {
        SimpleProject project = projectRepository.findByIdAndDeletedFalse(id);
        if (project == null) {
            return ApiResponse.error("项目不存在");
        }
        
        // 更新项目信息
        if (request.containsKey("projectName")) {
            project.setName((String) request.get("projectName"));
        }
        if (request.containsKey("description")) {
            project.setDescription((String) request.get("description"));
        }
        project.setUpdatedAt(LocalDateTime.now());
        project.setUpdatedBy(1L); // 默认用户ID
        
        project = projectRepository.save(project);
        
        ProjectResponse response = convertToResponse(project);
        return ApiResponse.success("更新项目成功", response);
    }

    /**
     * 删除项目（软删除）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> deleteProject(@PathVariable Long id) {
        SimpleProject project = projectRepository.findByIdAndDeletedFalse(id);
        if (project == null) {
            return ApiResponse.error("项目不存在");
        }
        
        project.setDeleted(true);
        project.setUpdatedAt(LocalDateTime.now());
        project.setUpdatedBy(1L); // 默认用户ID
        
        projectRepository.save(project);
        
        return ApiResponse.success("删除项目成功");
    }

    // 私有辅助方法

    /**
     * 转换为列表项目格式（前端ProjectListView需要的格式）
     */
    private Map<String, Object> convertToListItem(SimpleProject project) {
        Map<String, Object> item = new HashMap<>();
        item.put("id", project.getId());
        item.put("projectCode", project.getProjectCode());
        item.put("projectName", project.getName());
        item.put("description", project.getDescription());
        item.put("status", project.getStatus());
        item.put("totalFunctionPoints", project.getTotalFunctionPoints());
        item.put("estimatedCost", project.getTotalCostEstimate());
        item.put("createdBy", "系统用户"); // 简化处理
        item.put("createTime", project.getCreatedAt() != null ? 
            project.getCreatedAt().toString().replace("T", " ") : "");
        return item;
    }

    /**
     * 转换为详细响应格式
     */
    private ProjectResponse convertToResponse(SimpleProject project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setProjectCode(project.getProjectCode());
        response.setProjectName(project.getName());
        response.setProjectDescription(project.getDescription());
        response.setProjectStatus(project.getStatus());
        response.setBudgetAmount(project.getBudgetAmount());
        response.setCreatedAt(project.getCreatedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        response.setCreatedBy(project.getCreatedBy());
        response.setUpdatedBy(project.getUpdatedBy());
        response.setVersion(project.getCurrentVersion());
        response.setTotalFunctionPoints(project.getTotalFunctionPoints() != null ? 
            project.getTotalFunctionPoints().intValue() : 0);
        return response;
    }

    /**
     * 生成项目编号
     */
    private String generateProjectCode() {
        String yearMonth = java.time.format.DateTimeFormatter.ofPattern("yyyyMM")
            .format(LocalDateTime.now());
        long count = projectRepository.count() + 1;
        return String.format("PROJ-%s-%03d", yearMonth, count);
    }
    
    /**
     * 获取当前登录用户名
     */
    private String getCurrentUsername() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                return ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            }
            return principal.toString();
        } catch (Exception e) {
            logger.debug("获取当前用户名失败", e);
            return "unknown";
        }
    }
    
    /**
     * 获取当前用户权限信息
     */
    private String getCurrentUserAuthorities() {
        try {
            return SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(", "));
        } catch (Exception e) {
            logger.debug("获取当前用户权限失败", e);
            return "no_authorities";
        }
    }
}