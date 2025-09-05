package gov.changsha.finance.repository;

import gov.changsha.finance.entity.Project;
import gov.changsha.finance.entity.ProjectStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 项目数据访问接口
 * 
 * @author system
 * @since 1.0.0
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    /**
     * 根据项目编号查找项目
     */
    Optional<Project> findByProjectCode(String projectCode);

    /**
     * 检查项目编号是否存在
     */
    boolean existsByProjectCode(String projectCode);

    /**
     * 根据项目状态查找项目
     */
    List<Project> findByProjectStatus(String projectStatus);

    /**
     * 根据项目类型查找项目
     */
    List<Project> findByProjectType(String projectType);

    /**
     * 根据部门ID查找项目
     */
    List<Project> findByDepartmentId(Long departmentId);

    /**
     * 根据项目负责人ID查找项目
     */
    List<Project> findByProjectManagerId(Long projectManagerId);

    /**
     * 根据创建时间范围查找项目
     */
    List<Project> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 根据项目名称模糊查找项目
     */
    @Query("SELECT p FROM Project p WHERE p.projectName LIKE %:projectName%")
    List<Project> findByProjectNameContaining(@Param("projectName") String projectName);

    /**
     * 分页查询项目（支持条件筛选）
     */
    @Query("SELECT p FROM Project p WHERE " +
           "(:projectStatus IS NULL OR p.projectStatus = :projectStatus) AND " +
           "(:projectType IS NULL OR p.projectType = :projectType) AND " +
           "(:departmentId IS NULL OR p.departmentId = :departmentId) AND " +
           "(:projectName IS NULL OR p.projectName LIKE %:projectName%) AND " +
           "p.deletedAt IS NULL " +
           "ORDER BY p.createdAt DESC")
    Page<Project> findProjectsWithFilters(@Param("projectStatus") String projectStatus,
                                         @Param("projectType") String projectType,
                                         @Param("departmentId") Long departmentId,
                                         @Param("projectName") String projectName,
                                         Pageable pageable);

    /**
     * 统计各状态项目数量
     */
    @Query("SELECT p.projectStatus, COUNT(p) FROM Project p WHERE p.deletedAt IS NULL GROUP BY p.projectStatus")
    List<Object[]> countProjectsByStatus();

    /**
     * 统计各类型项目数量
     */
    @Query("SELECT p.projectType, COUNT(p) FROM Project p WHERE p.deletedAt IS NULL GROUP BY p.projectType")
    List<Object[]> countProjectsByType();

    /**
     * 统计部门项目数量
     */
    @Query("SELECT p.departmentId, p.departmentName, COUNT(p) FROM Project p WHERE p.deletedAt IS NULL GROUP BY p.departmentId, p.departmentName")
    List<Object[]> countProjectsByDepartment();

    /**
     * 查找即将到期的项目（在指定天数内到期）
     */
    @Query("SELECT p FROM Project p WHERE p.endDate BETWEEN :startDate AND :endDate AND p.deletedAt IS NULL")
    List<Project> findProjectsExpiringBetween(@Param("startDate") LocalDateTime startDate, 
                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 查找有功能点的项目
     */
    @Query("SELECT DISTINCT p FROM Project p INNER JOIN p.functionPoints fp WHERE fp.deletedAt IS NULL AND p.deletedAt IS NULL")
    List<Project> findProjectsWithFunctionPoints();

    /**
     * 查找有计算结果的项目
     */
    @Query("SELECT DISTINCT p FROM Project p INNER JOIN p.calculationResults cr WHERE p.deletedAt IS NULL")
    List<Project> findProjectsWithCalculationResults();

    /**
     * 软删除项目
     */
    @Query("UPDATE Project p SET p.deletedAt = :deletedAt, p.deletedBy = :deletedBy WHERE p.id = :id")
    int softDeleteById(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt, @Param("deletedBy") Long deletedBy);

    /**
     * 恢复已删除的项目
     */
    @Query("UPDATE Project p SET p.deletedAt = NULL, p.deletedBy = NULL WHERE p.id = :id")
    int restoreById(@Param("id") Long id);

}