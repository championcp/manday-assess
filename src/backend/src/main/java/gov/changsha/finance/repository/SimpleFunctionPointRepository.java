package gov.changsha.finance.repository;

import gov.changsha.finance.entity.SimpleFunctionPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 简化功能点数据访问接口
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */
@Repository
public interface SimpleFunctionPointRepository extends JpaRepository<SimpleFunctionPoint, Long> {

    /**
     * 根据项目ID查找功能点（未删除）
     */
    List<SimpleFunctionPoint> findByProjectIdAndDeletedAtIsNull(Long projectId);

    /**
     * 根据项目ID查找所有功能点（包括已删除）
     */
    List<SimpleFunctionPoint> findByProjectId(Long projectId);

    /**
     * 根据功能点类型查找功能点
     */
    List<SimpleFunctionPoint> findByFpTypeAndDeletedAtIsNull(String fpType);

    /**
     * 根据复杂度等级查找功能点
     */
    List<SimpleFunctionPoint> findByComplexityLevelAndDeletedAtIsNull(String complexityLevel);

    /**
     * 统计项目的功能点数量
     */
    @Query("SELECT COUNT(fp) FROM SimpleFunctionPoint fp WHERE fp.projectId = :projectId AND fp.deletedAt IS NULL")
    long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计各类型功能点数量
     */
    @Query("SELECT fp.fpType, COUNT(fp) FROM SimpleFunctionPoint fp WHERE fp.projectId = :projectId AND fp.deletedAt IS NULL GROUP BY fp.fpType")
    List<Object[]> countByFunctionPointTypeForProject(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和功能点名称查找
     */
    SimpleFunctionPoint findByProjectIdAndFpNameAndDeletedAtIsNull(Long projectId, String fpName);

    /**
     * 根据状态查找功能点
     */
    List<SimpleFunctionPoint> findByStatusAndDeletedAtIsNull(String status);

    /**
     * 获取项目功能点的总分值
     */
    @Query("SELECT COALESCE(SUM(fp.calculatedFpValue), 0) FROM SimpleFunctionPoint fp WHERE fp.projectId = :projectId AND fp.deletedAt IS NULL")
    java.math.BigDecimal getTotalFunctionPointValueByProjectId(@Param("projectId") Long projectId);

    /**
     * 软删除项目的所有功能点
     */
    @Query("UPDATE SimpleFunctionPoint fp SET fp.deletedAt = CURRENT_TIMESTAMP WHERE fp.projectId = :projectId")
    int softDeleteByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据复杂度级别统计功能点
     */
    @Query("SELECT fp.complexityLevel, COUNT(fp), COALESCE(SUM(fp.calculatedFpValue), 0) FROM SimpleFunctionPoint fp WHERE fp.projectId = :projectId AND fp.deletedAt IS NULL GROUP BY fp.complexityLevel")
    List<Object[]> getComplexityStatsForProject(@Param("projectId") Long projectId);

    /**
     * 获取项目中最新的功能点
     */
    @Query("SELECT fp FROM SimpleFunctionPoint fp WHERE fp.projectId = :projectId AND fp.deletedAt IS NULL ORDER BY fp.createdAt DESC")
    List<SimpleFunctionPoint> findLatestFunctionPointsByProject(@Param("projectId") Long projectId, org.springframework.data.domain.Pageable pageable);
}