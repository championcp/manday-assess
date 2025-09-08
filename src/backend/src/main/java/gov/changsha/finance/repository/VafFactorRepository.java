package gov.changsha.finance.repository;

import gov.changsha.finance.entity.VafFactor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * VAF因子数据访问接口
 * 用于VAF（Value Adjustment Factor）因子的数据库操作
 * 
 * 功能：
 * 1. VAF因子的CRUD操作
 * 2. 按项目查询VAF因子
 * 3. VAF因子验证和统计
 * 4. 支持政府项目审计要求
 * 
 * @author QA Test Engineer
 * @version 1.0.0
 * @since 2025-09-04
 */
@Repository
public interface VafFactorRepository extends JpaRepository<VafFactor, Long> {

    /**
     * 根据项目ID查找所有VAF因子
     * 用于获取特定项目的所有技术复杂度因子
     */
    List<VafFactor> findByProjectId(Long projectId);

    /**
     * 根据项目ID和因子类型查找VAF因子
     * 用于查找特定项目的特定因子（如TF01、TF02等）
     */
    Optional<VafFactor> findByProjectIdAndFactorType(Long projectId, String factorType);

    /**
     * 根据因子类型查找所有VAF因子
     * 用于横向比较不同项目的同一类型因子
     */
    List<VafFactor> findByFactorType(String factorType);

    /**
     * 统计项目的VAF因子总数
     * 用于验证项目是否有完整的14个标准因子
     */
    @Query("SELECT COUNT(vf) FROM VafFactor vf WHERE vf.project.id = :projectId")
    long countByProjectId(@Param("projectId") Long projectId);

    /**
     * 计算项目VAF因子总评分
     * 用于计算技术复杂度调整因子
     */
    @Query("SELECT SUM(vf.influenceScore) FROM VafFactor vf WHERE vf.project.id = :projectId")
    Integer sumInfluenceScoresByProjectId(@Param("projectId") Long projectId);

    /**
     * 查找评分在指定范围内的VAF因子
     * 用于数据验证和质量检查
     */
    @Query("SELECT vf FROM VafFactor vf WHERE vf.influenceScore BETWEEN :minScore AND :maxScore")
    List<VafFactor> findByInfluenceScoreBetween(@Param("minScore") Integer minScore, 
                                               @Param("maxScore") Integer maxScore);

    /**
     * 查找有无效评分的VAF因子
     * 用于数据完整性检查（评分应该在0-5范围内）
     */
    @Query("SELECT vf FROM VafFactor vf WHERE vf.influenceScore < 0 OR vf.influenceScore > 5")
    List<VafFactor> findWithInvalidScores();

    /**
     * 根据项目ID删除所有VAF因子
     * 用于项目删除时的级联清理
     */
    void deleteByProjectId(Long projectId);

    /**
     * 检查项目是否有完整的14个VAF因子
     * 政府项目要求必须有完整的标准因子集
     */
    @Query("SELECT COUNT(DISTINCT vf.factorType) FROM VafFactor vf WHERE vf.project.id = :projectId")
    long countDistinctFactorTypesByProjectId(@Param("projectId") Long projectId);

    /**
     * 查找缺少的VAF因子类型
     * 用于识别项目缺少的标准因子
     */
    @Query(value = "SELECT ft.factorType FROM " +
           "(SELECT 'TF01' as factorType UNION SELECT 'TF02' UNION SELECT 'TF03' UNION SELECT 'TF04' UNION " +
           "SELECT 'TF05' UNION SELECT 'TF06' UNION SELECT 'TF07' UNION SELECT 'TF08' UNION " +
           "SELECT 'TF09' UNION SELECT 'TF10' UNION SELECT 'TF11' UNION SELECT 'TF12' UNION " +
           "SELECT 'TF13' UNION SELECT 'TF14') ft " +
           "WHERE ft.factorType NOT IN " +
           "(SELECT vf.factor_type FROM vaf_factors vf WHERE vf.project_id = :projectId)", 
           nativeQuery = true)
    List<String> findMissingFactorTypesByProjectId(@Param("projectId") Long projectId);

    /**
     * 按创建时间降序查找VAF因子
     * 用于审计追踪和历史记录查询
     */
    @Query("SELECT vf FROM VafFactor vf WHERE vf.project.id = :projectId ORDER BY vf.createdAt DESC")
    List<VafFactor> findByProjectIdOrderByCreateTimeDesc(@Param("projectId") Long projectId);

    /**
     * 查找最近更新的VAF因子
     * 用于审计和变更追踪
     */
    @Query("SELECT vf FROM VafFactor vf WHERE vf.project.id = :projectId ORDER BY vf.updatedAt DESC")
    List<VafFactor> findByProjectIdOrderByUpdateTimeDesc(@Param("projectId") Long projectId);

    /**
     * 统计各评分等级的因子数量
     * 用于项目质量分析和统计报告
     */
    @Query("SELECT vf.influenceScore, COUNT(vf) FROM VafFactor vf WHERE vf.project.id = :projectId " +
           "GROUP BY vf.influenceScore ORDER BY vf.influenceScore")
    List<Object[]> countFactorsByScoreForProject(@Param("projectId") Long projectId);
}