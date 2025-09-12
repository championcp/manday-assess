package gov.changsha.finance.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * VAF调整因子实体
 * 支持NESMA标准的14个技术复杂度因子
 * 
 * @author Developer Engineer
 * @version 1.0.0
 * @since 2025-09-03
 */
@Entity
@Table(name = "vaf_factors")
public class VafFactor extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    /**
     * 技术复杂度因子类型
     * TF1-TF14对应NESMA标准的14个因子
     */
    @Column(name = "factor_type", nullable = false, length = 10)
    private String factorType;
    
    /**
     * 因子名称
     */
    @Column(name = "factor_name", nullable = false, length = 100)
    private String factorName;
    
    /**
     * 影响程度评分 (0-5分)
     * 0: 无影响
     * 1: 偶然影响  
     * 2: 一定影响
     * 3: 平均影响
     * 4: 重要影响
     * 5: 关键影响
     */
    @Column(name = "influence_score", nullable = false, precision = 2, scale = 0)
    private Integer influenceScore;
    
    /**
     * 因子权重 (用于计算VAF)
     * 标准权重为1，特殊情况可调整
     */
    @Column(name = "weight", nullable = false, precision = 4, scale = 2)
    private BigDecimal weight;
    
    /**
     * 评估说明
     */
    @Column(name = "assessment_notes", length = 500)
    private String assessmentNotes;
    
    // 构造函数
    public VafFactor() {
        this.weight = BigDecimal.ONE; // 默认权重为1
        this.influenceScore = 0; // 默认无影响
    }
    
    public VafFactor(Project project, String factorType, String factorName) {
        this();
        this.project = project;
        this.factorType = factorType;
        this.factorName = factorName;
    }
    
    // Getter和Setter方法
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Project getProject() {
        return project;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }
    
    public String getFactorType() {
        return factorType;
    }
    
    public void setFactorType(String factorType) {
        this.factorType = factorType;
    }
    
    public String getFactorName() {
        return factorName;
    }
    
    public void setFactorName(String factorName) {
        this.factorName = factorName;
    }
    
    public Integer getInfluenceScore() {
        return influenceScore;
    }
    
    public void setInfluenceScore(Integer influenceScore) {
        if (influenceScore < 0 || influenceScore > 5) {
            throw new IllegalArgumentException("影响程度评分必须在0-5之间");
        }
        this.influenceScore = influenceScore;
    }
    
    public BigDecimal getWeight() {
        return weight;
    }
    
    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }
    
    public String getAssessmentNotes() {
        return assessmentNotes;
    }
    
    public void setAssessmentNotes(String assessmentNotes) {
        this.assessmentNotes = assessmentNotes;
    }
    
    /**
     * 计算该因子对VAF的贡献值
     * 贡献值 = 影响评分 × 权重
     */
    public BigDecimal calculateContribution() {
        return new BigDecimal(this.influenceScore).multiply(this.weight);
    }
    
    @Override
    public String toString() {
        return "VafFactor{" +
                "id=" + id +
                ", factorType='" + factorType + '\'' +
                ", factorName='" + factorName + '\'' +
                ", influenceScore=" + influenceScore +
                ", weight=" + weight +
                '}';
    }
}