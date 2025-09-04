package gov.changsha.finance.entity;

import javax.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import java.math.BigDecimal;

/**
 * NESMA配置实体类
 * 存储项目特定的NESMA计算配置和参数
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Entity
@Table(name = "nesma_configurations", indexes = {
    @Index(name = "idx_nesma_config_project_id", columnList = "project_id")
})
public class NesmaConfiguration extends BaseEntity {

    /**
     * 项目ID（一对一关系）
     */
    @Column(name = "project_id", nullable = false, unique = true)
    private Long projectId;

    /**
     * NESMA版本：V2_1-2.1版, V3_0-3.0版
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "nesma_version", nullable = false, length = 10)
    private NesmaVersion nesmaVersion = NesmaVersion.V2_1;

    /**
     * 计算方法：INDICATIVE-指示性, ESTIMATED-估算性, DETAILED-详细性
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_method", nullable = false, length = 20)
    private CalculationMethod calculationMethod = CalculationMethod.DETAILED;

    /**
     * 应用类型：BUSINESS_APPLICATION-业务应用, SYSTEM_SOFTWARE-系统软件, EMBEDDED_SOFTWARE-嵌入式软件
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "application_type", nullable = false, length = 30)
    private ApplicationType applicationType = ApplicationType.BUSINESS_APPLICATION;

    /**
     * 开发平台：WEB-Web应用, DESKTOP-桌面应用, MOBILE-移动应用, MAINFRAME-大型机
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "platform_type", length = 20)
    private PlatformType platformType = PlatformType.WEB;

    /**
     * 是否使用VAF（价值调整因子）
     */
    @Column(name = "use_vaf", nullable = false, columnDefinition = "boolean default true")
    private Boolean useVaf = true;

    /**
     * 数据通信复杂度 (0-5)
     */
    @Column(name = "data_communication", nullable = false, columnDefinition = "integer default 3")
    private Integer dataCommunication = 3;

    /**
     * 分布式数据处理复杂度 (0-5)
     */
    @Column(name = "distributed_processing", nullable = false, columnDefinition = "integer default 0")
    private Integer distributedProcessing = 0;

    /**
     * 性能要求复杂度 (0-5)
     */
    @Column(name = "performance", nullable = false, columnDefinition = "integer default 3")
    private Integer performance = 3;

    /**
     * 运行环境配置复杂度 (0-5)
     */
    @Column(name = "heavily_used_configuration", nullable = false, columnDefinition = "integer default 3")
    private Integer heavilyUsedConfiguration = 3;

    /**
     * 事务处理频率复杂度 (0-5)
     */
    @Column(name = "transaction_rate", nullable = false, columnDefinition = "integer default 3")
    private Integer transactionRate = 3;

    /**
     * 在线数据录入复杂度 (0-5)
     */
    @Column(name = "online_data_entry", nullable = false, columnDefinition = "integer default 3")
    private Integer onlineDataEntry = 3;

    /**
     * 最终用户效率复杂度 (0-5)
     */
    @Column(name = "end_user_efficiency", nullable = false, columnDefinition = "integer default 3")
    private Integer endUserEfficiency = 3;

    /**
     * 在线更新复杂度 (0-5)
     */
    @Column(name = "online_update", nullable = false, columnDefinition = "integer default 3")
    private Integer onlineUpdate = 3;

    /**
     * 复杂处理逻辑复杂度 (0-5)
     */
    @Column(name = "complex_processing", nullable = false, columnDefinition = "integer default 3")
    private Integer complexProcessing = 3;

    /**
     * 代码可重用性复杂度 (0-5)
     */
    @Column(name = "reusability", nullable = false, columnDefinition = "integer default 3")
    private Integer reusability = 3;

    /**
     * 安装便利性复杂度 (0-5)
     */
    @Column(name = "installation_ease", nullable = false, columnDefinition = "integer default 3")
    private Integer installationEase = 3;

    /**
     * 操作便利性复杂度 (0-5)
     */
    @Column(name = "operational_ease", nullable = false, columnDefinition = "integer default 3")
    private Integer operationalEase = 3;

    /**
     * 多站点支持复杂度 (0-5)
     */
    @Column(name = "multiple_sites", nullable = false, columnDefinition = "integer default 0")
    private Integer multipleSites = 0;

    /**
     * 变更便利性复杂度 (0-5)
     */
    @Column(name = "facilitate_change", nullable = false, columnDefinition = "integer default 3")
    private Integer facilitateChange = 3;

    /**
     * 计算出的VAF值
     */
    @Column(name = "vaf_value", precision = 5, scale = 3)
    private BigDecimal vafValue;

    /**
     * 自定义调整因子
     */
    @Column(name = "custom_adjustment_factor", precision = 5, scale = 3, columnDefinition = "decimal(5,3) default 1.000")
    private BigDecimal customAdjustmentFactor = BigDecimal.ONE;

    /**
     * 配置描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 配置状态：ACTIVE-启用, INACTIVE-禁用
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ConfigurationStatus status = ConfigurationStatus.ACTIVE;

    /**
     * 创建人
     */
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    /**
     * 更新人
     */
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 关联项目
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Project project;

    /**
     * NESMA版本枚举
     */
    public enum NesmaVersion {
        /** NESMA 2.1版本 */
        V2_1,
        /** NESMA 3.0版本 */
        V3_0
    }

    /**
     * 计算方法枚举
     */
    public enum CalculationMethod {
        /** 指示性计算 */
        INDICATIVE,
        /** 估算性计算 */
        ESTIMATED,
        /** 详细性计算 */
        DETAILED
    }

    /**
     * 应用类型枚举
     */
    public enum ApplicationType {
        /** 业务应用 */
        BUSINESS_APPLICATION,
        /** 系统软件 */
        SYSTEM_SOFTWARE,
        /** 嵌入式软件 */
        EMBEDDED_SOFTWARE
    }

    /**
     * 平台类型枚举
     */
    public enum PlatformType {
        /** Web应用 */
        WEB,
        /** 桌面应用 */
        DESKTOP,
        /** 移动应用 */
        MOBILE,
        /** 大型机 */
        MAINFRAME,
        /** 云平台 */
        CLOUD
    }

    /**
     * 配置状态枚举
     */
    public enum ConfigurationStatus {
        /** 启用状态 */
        ACTIVE,
        /** 禁用状态 */
        INACTIVE
    }

    // 构造函数
    public NesmaConfiguration() {}

    public NesmaConfiguration(Long projectId) {
        this.projectId = projectId;
        // 设置默认的VAF因子值 (一般为3)
        initializeDefaultVafFactors();
    }

    // Getters and Setters
    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public NesmaVersion getNesmaVersion() {
        return nesmaVersion;
    }

    public void setNesmaVersion(NesmaVersion nesmaVersion) {
        this.nesmaVersion = nesmaVersion;
    }

    public CalculationMethod getCalculationMethod() {
        return calculationMethod;
    }

    public void setCalculationMethod(CalculationMethod calculationMethod) {
        this.calculationMethod = calculationMethod;
    }

    public ApplicationType getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(ApplicationType applicationType) {
        this.applicationType = applicationType;
    }

    public PlatformType getPlatformType() {
        return platformType;
    }

    public void setPlatformType(PlatformType platformType) {
        this.platformType = platformType;
    }

    public Boolean getUseVaf() {
        return useVaf;
    }

    public void setUseVaf(Boolean useVaf) {
        this.useVaf = useVaf;
    }

    public Integer getDataCommunication() {
        return dataCommunication;
    }

    public void setDataCommunication(Integer dataCommunication) {
        this.dataCommunication = dataCommunication;
    }

    public Integer getDistributedProcessing() {
        return distributedProcessing;
    }

    public void setDistributedProcessing(Integer distributedProcessing) {
        this.distributedProcessing = distributedProcessing;
    }

    public Integer getPerformance() {
        return performance;
    }

    public void setPerformance(Integer performance) {
        this.performance = performance;
    }

    public Integer getHeavilyUsedConfiguration() {
        return heavilyUsedConfiguration;
    }

    public void setHeavilyUsedConfiguration(Integer heavilyUsedConfiguration) {
        this.heavilyUsedConfiguration = heavilyUsedConfiguration;
    }

    public Integer getTransactionRate() {
        return transactionRate;
    }

    public void setTransactionRate(Integer transactionRate) {
        this.transactionRate = transactionRate;
    }

    public Integer getOnlineDataEntry() {
        return onlineDataEntry;
    }

    public void setOnlineDataEntry(Integer onlineDataEntry) {
        this.onlineDataEntry = onlineDataEntry;
    }

    public Integer getEndUserEfficiency() {
        return endUserEfficiency;
    }

    public void setEndUserEfficiency(Integer endUserEfficiency) {
        this.endUserEfficiency = endUserEfficiency;
    }

    public Integer getOnlineUpdate() {
        return onlineUpdate;
    }

    public void setOnlineUpdate(Integer onlineUpdate) {
        this.onlineUpdate = onlineUpdate;
    }

    public Integer getComplexProcessing() {
        return complexProcessing;
    }

    public void setComplexProcessing(Integer complexProcessing) {
        this.complexProcessing = complexProcessing;
    }

    public Integer getReusability() {
        return reusability;
    }

    public void setReusability(Integer reusability) {
        this.reusability = reusability;
    }

    public Integer getInstallationEase() {
        return installationEase;
    }

    public void setInstallationEase(Integer installationEase) {
        this.installationEase = installationEase;
    }

    public Integer getOperationalEase() {
        return operationalEase;
    }

    public void setOperationalEase(Integer operationalEase) {
        this.operationalEase = operationalEase;
    }

    public Integer getMultipleSites() {
        return multipleSites;
    }

    public void setMultipleSites(Integer multipleSites) {
        this.multipleSites = multipleSites;
    }

    public Integer getFacilitateChange() {
        return facilitateChange;
    }

    public void setFacilitateChange(Integer facilitateChange) {
        this.facilitateChange = facilitateChange;
    }

    public BigDecimal getVafValue() {
        return vafValue;
    }

    public void setVafValue(BigDecimal vafValue) {
        this.vafValue = vafValue;
    }

    public BigDecimal getCustomAdjustmentFactor() {
        return customAdjustmentFactor;
    }

    public void setCustomAdjustmentFactor(BigDecimal customAdjustmentFactor) {
        this.customAdjustmentFactor = customAdjustmentFactor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ConfigurationStatus getStatus() {
        return status;
    }

    public void setStatus(ConfigurationStatus status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    // 业务方法
    
    /**
     * 初始化默认VAF因子值
     */
    private void initializeDefaultVafFactors() {
        this.dataCommunication = 3;
        this.distributedProcessing = 0;
        this.performance = 3;
        this.heavilyUsedConfiguration = 3;
        this.transactionRate = 3;
        this.onlineDataEntry = 3;
        this.endUserEfficiency = 3;
        this.onlineUpdate = 3;
        this.complexProcessing = 3;
        this.reusability = 3;
        this.installationEase = 3;
        this.operationalEase = 3;
        this.multipleSites = 0;
        this.facilitateChange = 3;
    }

    /**
     * 计算VAF值
     * VAF = (GSC × 0.01) + 0.65
     * 其中GSC = 所有GSC因子的总和
     */
    public BigDecimal calculateVaf() {
        if (!useVaf) {
            this.vafValue = BigDecimal.ONE;
            return this.vafValue;
        }

        int gscSum = dataCommunication + distributedProcessing + performance + 
                    heavilyUsedConfiguration + transactionRate + onlineDataEntry +
                    endUserEfficiency + onlineUpdate + complexProcessing + 
                    reusability + installationEase + operationalEase + 
                    multipleSites + facilitateChange;

        // VAF = (GSC × 0.01) + 0.65
        BigDecimal vaf = new BigDecimal(gscSum)
                .multiply(new BigDecimal("0.01"))
                .add(new BigDecimal("0.65"));

        this.vafValue = vaf;
        return vaf;
    }

    /**
     * 获取最终调整因子（VAF × 自定义调整因子）
     */
    public BigDecimal getFinalAdjustmentFactor() {
        BigDecimal vaf = getVafValue() != null ? getVafValue() : calculateVaf();
        return vaf.multiply(customAdjustmentFactor != null ? customAdjustmentFactor : BigDecimal.ONE);
    }

    /**
     * 验证VAF因子值范围（0-5）
     */
    public boolean isValidVafFactor(Integer factor) {
        return factor != null && factor >= 0 && factor <= 5;
    }

    /**
     * 验证所有VAF因子
     */
    public boolean validateAllVafFactors() {
        return isValidVafFactor(dataCommunication) &&
               isValidVafFactor(distributedProcessing) &&
               isValidVafFactor(performance) &&
               isValidVafFactor(heavilyUsedConfiguration) &&
               isValidVafFactor(transactionRate) &&
               isValidVafFactor(onlineDataEntry) &&
               isValidVafFactor(endUserEfficiency) &&
               isValidVafFactor(onlineUpdate) &&
               isValidVafFactor(complexProcessing) &&
               isValidVafFactor(reusability) &&
               isValidVafFactor(installationEase) &&
               isValidVafFactor(operationalEase) &&
               isValidVafFactor(multipleSites) &&
               isValidVafFactor(facilitateChange);
    }

    /**
     * 检查配置是否可用
     */
    public boolean isActive() {
        return status == ConfigurationStatus.ACTIVE;
    }

    /**
     * 获取GSC总分
     */
    public int getGscTotal() {
        return dataCommunication + distributedProcessing + performance + 
               heavilyUsedConfiguration + transactionRate + onlineDataEntry +
               endUserEfficiency + onlineUpdate + complexProcessing + 
               reusability + installationEase + operationalEase + 
               multipleSites + facilitateChange;
    }

    @Override
    public String toString() {
        return "NesmaConfiguration{" +
                "id=" + getId() +
                ", projectId=" + projectId +
                ", nesmaVersion=" + nesmaVersion +
                ", calculationMethod=" + calculationMethod +
                ", applicationType=" + applicationType +
                ", platformType=" + platformType +
                ", useVaf=" + useVaf +
                ", vafValue=" + vafValue +
                ", customAdjustmentFactor=" + customAdjustmentFactor +
                ", status=" + status +
                '}';
    }
}