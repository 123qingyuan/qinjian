package main.java.com.elderly.monitoring.notification.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 通知实体类
 * 
 * @author System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_type", columnList = "type"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "created_at"),
    @Index(name = "idx_read_at", columnList = "read_at")
})
public class Notification {

    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /**
     * 通知类型
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;

    /**
     * 通知标题
     */
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    /**
     * 通知内容
     */
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    /**
     * 通知状态
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private NotificationStatus status;

    /**
     * 优先级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private NotificationPriority priority;

    /**
     * 发送方式
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "send_method")
    private SendMethod sendMethod;

    /**
     * 关联ID（如设备ID、预警记录ID等）
     */
    @Column(name = "related_id")
    private String relatedId;

    /**
     * 关联类型
     */
    @Column(name = "related_type", length = 50)
    private String relatedType;

    /**
     * 是否已读
     */
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    /**
     * 阅读时间
     */
    @Column(name = "read_at")
    private LocalDateTime readAt;

    /**
     * 发送时间
     */
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    /**
     * 到期时间
     */
    @Column(name = "expire_at")
    private LocalDateTime expireAt;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 最大重试次数
     */
    @Column(name = "max_retry_count")
    private Integer maxRetryCount = 3;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 扩展数据（JSON格式）
     */
    @Column(name = "extra_data", columnDefinition = "TEXT")
    private String extraData;

    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 创建者ID
     */
    @Column(name = "created_by")
    private Long createdBy;

    /**
     * 更新者ID
     */
    @Column(name = "updated_by")
    private Long updatedBy;

    /**
     * 通知类型枚举
     */
    public enum NotificationType {
        SYSTEM_ALERT("系统预警"),
        HEALTH_ALERT("健康预警"),
        DEVICE_ALERT("设备预警"),
        MAINTENANCE("维护通知"),
        REMINDER("提醒通知"),
        ANNOUNCEMENT("公告通知"),
        SECURITY("安全通知"),
        REPORT("报告通知");

        private final String description;

        NotificationType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 通知状态枚举
     */
    public enum NotificationStatus {
        PENDING("待发送"),
        SENDING("发送中"),
        SENT("已发送"),
        DELIVERED("已送达"),
        READ("已读"),
        FAILED("发送失败"),
        EXPIRED("已过期");

        private final String description;

        NotificationStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 通知优先级枚举
     */
    public enum NotificationPriority {
        LOW("低"),
        NORMAL("普通"),
        HIGH("高"),
        URGENT("紧急");

        private final String description;

        NotificationPriority(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 发送方式枚举
     */
    public enum SendMethod {
        SYSTEM("系统内"),
        EMAIL("邮件"),
        SMS("短信"),
        PUSH("推送通知"),
        WECHAT("微信"),
        VOICE("语音");

        private final String description;

        SendMethod(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 创建前回调
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        
        if (this.status == null) {
            this.status = NotificationStatus.PENDING;
        }
        
        if (this.priority == null) {
            this.priority = NotificationPriority.NORMAL;
        }
        
        if (this.isRead == null) {
            this.isRead = false;
        }
        
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        
        if (this.maxRetryCount == null) {
            this.maxRetryCount = 3;
        }
    }

    /**
     * 更新前回调
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
        this.status = NotificationStatus.READ;
    }

    /**
     * 标记为已发送
     */
    public void markAsSent() {
        this.sentAt = LocalDateTime.now();
        this.status = NotificationStatus.SENT;
    }

    /**
     * 标记为已送达
     */
    public void markAsDelivered() {
        this.status = NotificationStatus.DELIVERED;
    }

    /**
     * 标记为发送失败
     */
    public void markAsFailed(String errorMessage) {
        this.status = NotificationStatus.FAILED;
        this.errorMessage = errorMessage;
        this.retryCount++;
    }

    /**
     * 检查是否已过期
     */
    public boolean isExpired() {
        return expireAt != null && LocalDateTime.now().isAfter(expireAt);
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return retryCount < maxRetryCount && status == NotificationStatus.FAILED;
    }

    /**
     * 检查是否为紧急通知
     */
    public boolean isUrgent() {
        return priority == NotificationPriority.URGENT;
    }
}