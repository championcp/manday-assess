package gov.changsha.finance.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ILF详细信息实体类 - 占位符
 * 对应数据库表: ilf_details
 * 
 * @author system
 * @since 1.0.0
 */
@Entity
@Table(name = "ilf_details")
public class IlfDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "function_point_id", nullable = false, insertable = false, updatable = false)
    private Long functionPointId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_point_id")
    private FunctionPoint functionPoint;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 构造方法
    public IlfDetail() {}

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}