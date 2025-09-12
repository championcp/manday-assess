package gov.changsha.finance.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 功能点历史记录实体类 - 占位符
 * 对应数据库表: function_point_history
 * 
 * @author system
 * @since 1.0.0
 */
@Entity
@Table(name = "function_point_history")
public class FunctionPointHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "function_point_id", nullable = false)
    private Long functionPointId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_point_id", insertable = false, updatable = false)
    private FunctionPoint functionPoint;

    @Column(name = "operation_type", nullable = false, length = 20)
    private String operationType;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    // 构造方法
    public FunctionPointHistory() {}

    // Getter和Setter方法
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFunctionPointId() {
        return functionPointId;
    }

    public void setFunctionPointId(Long functionPointId) {
        this.functionPointId = functionPointId;
    }

    public FunctionPoint getFunctionPoint() {
        return functionPoint;
    }

    public void setFunctionPoint(FunctionPoint functionPoint) {
        this.functionPoint = functionPoint;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }
}