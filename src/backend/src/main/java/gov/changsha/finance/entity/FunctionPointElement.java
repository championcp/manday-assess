package gov.changsha.finance.entity;

import javax.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

/**
 * 功能点元素实体类
 * 存储功能点的数据元素、记录类型等详细信息
 * 
 * @author 开发团队
 * @version 1.0.0
 * @since 2025-09-03
 */
@Entity
@Table(name = "function_point_elements", indexes = {
    @Index(name = "idx_function_point_elements_function_point_id", columnList = "function_point_id"),
    @Index(name = "idx_function_point_elements_element_type", columnList = "element_type"),
    @Index(name = "idx_function_point_elements_name", columnList = "element_name")
})
public class FunctionPointElement extends BaseEntity {

    /**
     * 功能点ID
     */
    @Column(name = "function_point_id", nullable = false)
    private Long functionPointId;

    /**
     * 元素名称
     */
    @Column(name = "element_name", nullable = false, length = 200)
    private String elementName;

    /**
     * 元素描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 元素类型：DATA_ELEMENT-数据元素, RECORD_ELEMENT-记录元素, FILE_REFERENCE-文件引用
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "element_type", nullable = false, length = 20)
    private ElementType elementType;

    /**
     * 数据类型
     */
    @Column(name = "data_type", length = 50)
    private String dataType;

    /**
     * 数据长度
     */
    @Column(name = "data_length")
    private Integer dataLength;

    /**
     * 是否为主键
     */
    @Column(name = "is_primary_key", nullable = false, columnDefinition = "boolean default false")
    private Boolean isPrimaryKey = false;

    /**
     * 是否必填
     */
    @Column(name = "is_required", nullable = false, columnDefinition = "boolean default false")
    private Boolean isRequired = false;

    /**
     * 是否唯一
     */
    @Column(name = "is_unique", nullable = false, columnDefinition = "boolean default false")
    private Boolean isUnique = false;

    /**
     * 默认值
     */
    @Column(name = "default_value", length = 200)
    private String defaultValue;

    /**
     * 验证规则
     */
    @Column(name = "validation_rules", columnDefinition = "TEXT")
    private String validationRules;

    /**
     * 业务规则
     */
    @Column(name = "business_rules", columnDefinition = "TEXT")
    private String businessRules;

    /**
     * 数据来源
     */
    @Column(name = "data_source", length = 200)
    private String dataSource;

    /**
     * 引用的文件或表
     */
    @Column(name = "referenced_file", length = 200)
    private String referencedFile;

    /**
     * 排序序号
     */
    @Column(name = "sort_order", nullable = false, columnDefinition = "integer default 0")
    private Integer sortOrder = 0;

    /**
     * 元素状态：ACTIVE-有效, INACTIVE-无效, DEPRECATED-已废弃
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ElementStatus status = ElementStatus.ACTIVE;

    /**
     * 备注信息
     */
    @Column(name = "remarks", length = 500)
    private String remarks;

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
     * 关联功能点
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_point_id", referencedColumnName = "id", insertable = false, updatable = false)
    private FunctionPoint functionPoint;

    /**
     * 元素类型枚举
     */
    public enum ElementType {
        /** 数据元素 */
        DATA_ELEMENT,
        /** 记录元素类型 */
        RECORD_ELEMENT,
        /** 文件类型引用 */
        FILE_REFERENCE
    }

    /**
     * 元素状态枚举
     */
    public enum ElementStatus {
        /** 有效状态 */
        ACTIVE,
        /** 无效状态 */
        INACTIVE,
        /** 已废弃状态 */
        DEPRECATED
    }

    // 构造函数
    public FunctionPointElement() {}

    public FunctionPointElement(Long functionPointId, String elementName, ElementType elementType) {
        this.functionPointId = functionPointId;
        this.elementName = elementName;
        this.elementType = elementType;
    }

    public FunctionPointElement(Long functionPointId, String elementName, String description, ElementType elementType) {
        this.functionPointId = functionPointId;
        this.elementName = elementName;
        this.description = description;
        this.elementType = elementType;
    }

    // Getters and Setters
    public Long getFunctionPointId() {
        return functionPointId;
    }

    public void setFunctionPointId(Long functionPointId) {
        this.functionPointId = functionPointId;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Integer getDataLength() {
        return dataLength;
    }

    public void setDataLength(Integer dataLength) {
        this.dataLength = dataLength;
    }

    public Boolean getIsPrimaryKey() {
        return isPrimaryKey;
    }

    public void setIsPrimaryKey(Boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    public Boolean getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(Boolean isRequired) {
        this.isRequired = isRequired;
    }

    public Boolean getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Boolean isUnique) {
        this.isUnique = isUnique;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(String validationRules) {
        this.validationRules = validationRules;
    }

    public String getBusinessRules() {
        return businessRules;
    }

    public void setBusinessRules(String businessRules) {
        this.businessRules = businessRules;
    }

    public String getDataSource() {
        return dataSource;
    }

    public void setDataSource(String dataSource) {
        this.dataSource = dataSource;
    }

    public String getReferencedFile() {
        return referencedFile;
    }

    public void setReferencedFile(String referencedFile) {
        this.referencedFile = referencedFile;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public ElementStatus getStatus() {
        return status;
    }

    public void setStatus(ElementStatus status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public FunctionPoint getFunctionPoint() {
        return functionPoint;
    }

    public void setFunctionPoint(FunctionPoint functionPoint) {
        this.functionPoint = functionPoint;
    }

    // 业务方法
    
    /**
     * 检查元素是否有效
     */
    public boolean isActive() {
        return status == ElementStatus.ACTIVE;
    }

    /**
     * 检查是否为数据元素
     */
    public boolean isDataElement() {
        return elementType == ElementType.DATA_ELEMENT;
    }

    /**
     * 检查是否为记录元素
     */
    public boolean isRecordElement() {
        return elementType == ElementType.RECORD_ELEMENT;
    }

    /**
     * 检查是否为文件引用
     */
    public boolean isFileReference() {
        return elementType == ElementType.FILE_REFERENCE;
    }

    /**
     * 检查是否为关键字段（主键、必填或唯一）
     */
    public boolean isKeyField() {
        return Boolean.TRUE.equals(isPrimaryKey) || 
               Boolean.TRUE.equals(isRequired) || 
               Boolean.TRUE.equals(isUnique);
    }

    /**
     * 废弃元素
     */
    public void deprecate() {
        this.status = ElementStatus.DEPRECATED;
    }

    /**
     * 激活元素
     */
    public void activate() {
        this.status = ElementStatus.ACTIVE;
    }

    /**
     * 停用元素
     */
    public void deactivate() {
        this.status = ElementStatus.INACTIVE;
    }

    /**
     * 获取元素类型显示名称
     */
    public String getElementTypeDisplayName() {
        switch (elementType) {
            case DATA_ELEMENT: return "数据元素";
            case RECORD_ELEMENT: return "记录元素类型";
            case FILE_REFERENCE: return "文件类型引用";
            default: return elementType.name();
        }
    }

    /**
     * 获取状态显示名称
     */
    public String getStatusDisplayName() {
        switch (status) {
            case ACTIVE: return "有效";
            case INACTIVE: return "无效";
            case DEPRECATED: return "已废弃";
            default: return status.name();
        }
    }

    /**
     * 获取完整数据类型描述（包含长度）
     */
    public String getFullDataType() {
        if (dataType == null) return "";
        if (dataLength != null && dataLength > 0) {
            return dataType + "(" + dataLength + ")";
        }
        return dataType;
    }

    /**
     * 获取约束描述
     */
    public String getConstraintDescription() {
        StringBuilder sb = new StringBuilder();
        if (Boolean.TRUE.equals(isPrimaryKey)) {
            sb.append("主键 ");
        }
        if (Boolean.TRUE.equals(isRequired)) {
            sb.append("必填 ");
        }
        if (Boolean.TRUE.equals(isUnique)) {
            sb.append("唯一 ");
        }
        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "FunctionPointElement{" +
                "id=" + getId() +
                ", functionPointId=" + functionPointId +
                ", elementName='" + elementName + '\'' +
                ", elementType=" + elementType +
                ", dataType='" + dataType + '\'' +
                ", dataLength=" + dataLength +
                ", isPrimaryKey=" + isPrimaryKey +
                ", isRequired=" + isRequired +
                ", isUnique=" + isUnique +
                ", status=" + status +
                ", sortOrder=" + sortOrder +
                '}';
    }
}