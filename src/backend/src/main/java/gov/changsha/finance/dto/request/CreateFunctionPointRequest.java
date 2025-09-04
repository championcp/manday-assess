package gov.changsha.finance.dto.request;

import javax.validation.constraints.*;

import java.io.Serializable;

/**
 * 创建功能点请求DTO
 * 
 * @author system
 * @since 1.0.0
 */
public class CreateFunctionPointRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 功能点编号
     */
    @NotBlank(message = "功能点编号不能为空")
    @Size(max = 50, message = "功能点编号长度不能超过50个字符")
    @Pattern(regexp = "^[A-Z0-9-_]+$", message = "功能点编号只能包含大写字母、数字、横线和下划线")
    private String functionPointCode;

    /**
     * 功能点名称
     */
    @NotBlank(message = "功能点名称不能为空")
    @Size(max = 200, message = "功能点名称长度不能超过200个字符")
    private String functionPointName;

    /**
     * 功能点描述
     */
    @Size(max = 2000, message = "功能点描述长度不能超过2000个字符")
    private String functionPointDescription;

    /**
     * 功能点类型
     * 可选值: ILF(内部逻辑文件), EIF(外部接口文件), EI(外部输入), EO(外部输出), EQ(外部查询)
     */
    @NotBlank(message = "功能点类型不能为空")
    @Pattern(regexp = "^(ILF|EIF|EI|EO|EQ)$", 
             message = "功能点类型必须是ILF、EIF、EI、EO或EQ之一")
    private String functionPointType;

    /**
     * 数据元素类型数量（DET）
     */
    @NotNull(message = "数据元素类型数量不能为空")
    @Min(value = 1, message = "数据元素类型数量至少为1")
    @Max(value = 1000, message = "数据元素类型数量不能超过1000")
    private Integer detCount;

    /**
     * 记录元素类型数量（RET）- 适用于ILF和EIF
     */
    @Min(value = 0, message = "记录元素类型数量不能为负数")
    @Max(value = 1000, message = "记录元素类型数量不能超过1000")
    private Integer retCount;

    /**
     * 文件类型引用数量（FTR）- 适用于EI、EO和EQ
     */
    @Min(value = 0, message = "文件类型引用数量不能为负数")
    @Max(value = 1000, message = "文件类型引用数量不能超过1000")
    private Integer ftrCount;

    /**
     * 所属项目ID
     */
    @NotNull(message = "项目ID不能为空")
    @Positive(message = "项目ID必须为正数")
    private Long projectId;

    /**
     * 备注信息
     */
    @Size(max = 2000, message = "备注信息长度不能超过2000个字符")
    private String remarks;

    // 构造方法
    public CreateFunctionPointRequest() {}

    public CreateFunctionPointRequest(String functionPointCode, String functionPointName, 
                                    String functionPointType, Long projectId) {
        this.functionPointCode = functionPointCode;
        this.functionPointName = functionPointName;
        this.functionPointType = functionPointType;
        this.projectId = projectId;
    }

    // Getter和Setter方法
    public String getFunctionPointCode() {
        return functionPointCode;
    }

    public void setFunctionPointCode(String functionPointCode) {
        this.functionPointCode = functionPointCode;
    }

    public String getFunctionPointName() {
        return functionPointName;
    }

    public void setFunctionPointName(String functionPointName) {
        this.functionPointName = functionPointName;
    }

    public String getFunctionPointDescription() {
        return functionPointDescription;
    }

    public void setFunctionPointDescription(String functionPointDescription) {
        this.functionPointDescription = functionPointDescription;
    }

    public String getFunctionPointType() {
        return functionPointType;
    }

    public void setFunctionPointType(String functionPointType) {
        this.functionPointType = functionPointType;
    }

    public Integer getDetCount() {
        return detCount;
    }

    public void setDetCount(Integer detCount) {
        this.detCount = detCount;
    }

    public Integer getRetCount() {
        return retCount;
    }

    public void setRetCount(Integer retCount) {
        this.retCount = retCount;
    }

    public Integer getFtrCount() {
        return ftrCount;
    }

    public void setFtrCount(Integer ftrCount) {
        this.ftrCount = ftrCount;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    // 业务方法

    /**
     * 验证DET和RET/FTR的组合是否有效
     * 根据功能点类型验证对应的计数字段
     */
    public boolean isValidCounts() {
        if (detCount == null || detCount <= 0) {
            return false;
        }

        switch (functionPointType) {
            case "ILF":
            case "EIF":
                // ILF和EIF需要RET数量，FTR应为0
                return retCount != null && retCount > 0 && 
                       (ftrCount == null || ftrCount == 0);
            case "EI":
            case "EO":
            case "EQ":
                // EI、EO和EQ需要FTR数量，RET应为0
                return ftrCount != null && ftrCount > 0 && 
                       (retCount == null || retCount == 0);
            default:
                return false;
        }
    }

    /**
     * 自动计算复杂度等级
     * 根据DET和RET/FTR的数量计算复杂度等级
     */
    public String calculateComplexityLevel() {
        if (!isValidCounts()) {
            return "LOW"; // 默认返回低复杂度
        }

        switch (functionPointType) {
            case "ILF":
                return calculateIlfComplexity(detCount, retCount);
            case "EIF":
                return calculateEifComplexity(detCount, retCount);
            case "EI":
                return calculateEiComplexity(detCount, ftrCount);
            case "EO":
                return calculateEoComplexity(detCount, ftrCount);
            case "EQ":
                return calculateEqComplexity(detCount, ftrCount);
            default:
                return "LOW";
        }
    }

    /**
     * 计算ILF复杂度
     */
    private String calculateIlfComplexity(int det, int ret) {
        if (ret == 1) {
            if (det <= 19) return "LOW";
            if (det <= 50) return "MEDIUM";
            return "HIGH";
        } else if (ret <= 5) {
            if (det <= 20) return "LOW";
            if (det <= 50) return "MEDIUM";
            return "HIGH";
        } else {
            if (det <= 15) return "MEDIUM";
            return "HIGH";
        }
    }

    /**
     * 计算EIF复杂度
     */
    private String calculateEifComplexity(int det, int ret) {
        return calculateIlfComplexity(det, ret); // EIF与ILF使用相同规则
    }

    /**
     * 计算EI复杂度
     */
    private String calculateEiComplexity(int det, int ftr) {
        if (ftr <= 1) {
            if (det <= 4) return "LOW";
            if (det <= 15) return "MEDIUM";
            return "HIGH";
        } else if (ftr <= 2) {
            if (det <= 5) return "LOW";
            if (det <= 15) return "MEDIUM";
            return "HIGH";
        } else {
            if (det <= 4) return "MEDIUM";
            return "HIGH";
        }
    }

    /**
     * 计算EO复杂度
     */
    private String calculateEoComplexity(int det, int ftr) {
        if (ftr <= 1) {
            if (det <= 5) return "LOW";
            if (det <= 19) return "MEDIUM";
            return "HIGH";
        } else if (ftr <= 2) {
            if (det <= 5) return "LOW";
            if (det <= 19) return "MEDIUM";
            return "HIGH";
        } else {
            if (det <= 4) return "MEDIUM";
            return "HIGH";
        }
    }

    /**
     * 计算EQ复杂度
     */
    private String calculateEqComplexity(int det, int ftr) {
        return calculateEoComplexity(det, ftr); // EQ与EO使用相同规则
    }

    @Override
    public String toString() {
        return "CreateFunctionPointRequest{" +
                "functionPointCode='" + functionPointCode + '\'' +
                ", functionPointName='" + functionPointName + '\'' +
                ", functionPointType='" + functionPointType + '\'' +
                ", detCount=" + detCount +
                ", retCount=" + retCount +
                ", ftrCount=" + ftrCount +
                ", projectId=" + projectId +
                '}';
    }
}