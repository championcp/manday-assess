package gov.changsha.finance.repository;

import gov.changsha.finance.entity.FunctionPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 功能点数据访问接口
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-09
 */
@Repository
public interface FunctionPointRepository extends JpaRepository<FunctionPoint, Long> {

    /**
     * 根据项目ID查找功能点（未删除）
     */
    List<FunctionPoint> findByProjectIdAndDeletedAtIsNull(Long projectId);

    /**
     * 根据项目ID查找所有功能点（包括已删除）
     */
    List<FunctionPoint> findByProjectId(Long projectId);

    /**
     * 根据功能点类型查找功能点
     */
    List<FunctionPoint> findByFunctionPointTypeAndDeletedAtIsNull(String functionPointType);

    /**
     * 根据复杂度等级查找功能点
     */
    List<FunctionPoint> findByComplexityLevelAndDeletedAtIsNull(String complexityLevel);

    /**
     * 统计项目的功能点数量
     */
    @Query("SELECT COUNT(fp) FROM FunctionPoint fp WHERE fp.project.id = :projectId AND fp.deletedAt IS NULL")
    long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 统计各类型功能点数量
     */
    @Query("SELECT fp.functionPointType, COUNT(fp) FROM FunctionPoint fp WHERE fp.project.id = :projectId AND fp.deletedAt IS NULL GROUP BY fp.functionPointType")
    List<Object[]> countByFunctionPointTypeForProject(@Param("projectId") Long projectId);

    /**
     * 根据项目ID和功能点名称查找
     */
    FunctionPoint findByProjectIdAndFunctionPointNameAndDeletedAtIsNull(Long projectId, String functionPointName);

    /**
     * 查找指定复杂度范围的功能点
     */
    @Query("SELECT fp FROM FunctionPoint fp WHERE fp.project.id = :projectId AND fp.deletedAt IS NULL AND " +
           "((fp.functionPointType IN ('ILF', 'EIF') AND fp.detCount >= :minDet AND fp.detCount <= :maxDet) OR " +
           "(fp.functionPointType IN ('EI', 'EO', 'EQ') AND fp.detCount >= :minDet AND fp.detCount <= :maxDet))")
    List<FunctionPoint> findByProjectIdAndComplexityRange(@Param("projectId") Long projectId,
                                                         @Param("minDet") Integer minDet,
                                                         @Param("maxDet") Integer maxDet);

    /**
     * 软删除项目的所有功能点
     */
    @Query("UPDATE FunctionPoint fp SET fp.deletedAt = CURRENT_TIMESTAMP WHERE fp.project.id = :projectId")
    int softDeleteByProjectId(@Param("projectId") Long projectId);
}