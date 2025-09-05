package gov.changsha.finance.repository;

import gov.changsha.finance.entity.SimpleProject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 简化项目数据访问接口
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Repository
public interface SimpleProjectRepository extends JpaRepository<SimpleProject, Long> {

    /**
     * 查找未删除的项目（分页）
     */
    Page<SimpleProject> findByDeletedFalse(Pageable pageable);

    /**
     * 根据ID查找未删除的项目
     */
    SimpleProject findByIdAndDeletedFalse(Long id);
}