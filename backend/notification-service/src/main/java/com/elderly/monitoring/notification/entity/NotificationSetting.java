package main.java.com.elderly.monitoring.notification.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 通知设置实体类
 * 
 * @author System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Entity
@Table(name = "notification_settings", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_type", columnList = "type")
})
public class NotificationSetting {

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
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    /**
     * 系统内通知
     */
    @Column(name = "system_enabled", nullable = false)
    private Boolean systemEnabled = true;

    /**
     * 邮件通知
     */
    @Column(name = "email_enabled")
    private Boolean emailEnabled = false;

    /**
     * 短信通知
     */
    @Column(name = "sms_enabled")
    private Boolean smsEnabled = false;

    /**
     * 推送通知
     */
    @Column(name = "push_enabled")
    private Boolean pushEnabled = false;

    /**
     * 微信通知
     */
    @Column(name = "wechat_enabled")
    private Boolean wechatEnabled = false;

    /**
     * 语音通知
     */
    @Column(name = "voice_enabled")
    private Boolean voiceEnabled = false;

    /**
     * 邮件地址
     */
    @Column(name = "email_address", length = 100)
    private String emailAddress;

    /**
     * 手机号码
     */
    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    /**
     * 微信OpenID
     */
    @Column(name = "wechat_openid", length = 100)
    private String wechatOpenid;

    /**
     * 推送Token
     */
    @Column(name = "push_token", length = 500)
    private String pushToken;

    /**
     * 免打扰开始时间
     */
    @Column(name = "quiet_hours_start")
    private String quietHoursStart;

    /**
     * 免打扰结束时间
     */
    @Column(name = "quiet_hours_end")
    private String quietHoursEnd;

    /**
     * 周末免打扰
     */
    @Column(name = "weekend_quiet")
    private Boolean weekendQuiet = false;

    /**
     * 最小通知优先级
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "min_priority")
    private NotificationPriority minPriority = NotificationPriority.NORMAL;

    /**
     * 批量通知间隔（分钟）
     */
    @Column(name = "batch_interval")
    private Integer batchInterval = 0;

    /**
     * 最大每日通知数
     */
    @Column(name = "max_daily_notifications")
    private Integer maxDailyNotifications = 50;

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
     * 创建前回调
     */
    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        
        if (this.enabled == null) {
            this.enabled = true;
        }
        
        if (this.systemEnabled == null) {
            this.systemEnabled = true;
        }
        
        if (this.weekendQuiet == null) {
            this.weekendQuiet = false;
        }
        
        if (this.minPriority == null) {
            this.minPriority = NotificationPriority.NORMAL;
        }
        
        if (this.batchInterval == null) {
            this.batchInterval = 0;
        }
        
        if (this.maxDailyNotifications == null) {
            this.maxDailyNotifications = 50;
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
     * 检查是否在免打扰时间内
     */
    public boolean isInQuietHours() {
        if (quietHoursStart == null || quietHoursEnd == null) {
            return false;
        }
        
        // 简化实现，实际应该考虑当前时间和周末设置
        return false;
    }

    /**
     * 检查优先级是否满足最小要求
     */
    public boolean isPrioritySufficient(Notification.NotificationPriority priority) {
        if (minPriority == null) {
            return true;
        }
        
        return priority.ordinal() >= minPriority.ordinal();
    }

    /**
     * 获取启用的通知方式
     */
    public java.util.List<Notification.SendMethod> getEnabledMethods() {
        java.util.List<Notification.SendMethod> methods = new java.util.ArrayList<>();
        
        if (Boolean.TRUE.equals(systemEnabled)) {
            methods.add(Notification.SendMethod.SYSTEM);
        }
        if (Boolean.TRUE.equals(emailEnabled) && emailAddress != null) {
            methods.add(Notification.SendMethod.EMAIL);
        }
        if (Boolean.TRUE.equals(smsEnabled) && phoneNumber != null) {
            methods.add(Notification.SendMethod.SMS);
        }
        if (Boolean.TRUE.equals(pushEnabled) && pushToken != null) {
            methods.add(Notification.SendMethod.PUSH);
        }
        if (Boolean.TRUE.equals(wechatEnabled) && wechatOpenid != null) {
            methods.add(Notification.SendMethod.WECHAT);
        }
        if (Boolean.TRUE.equals(voiceEnabled) && phoneNumber != null) {
            methods.add(Notification.SendMethod.VOICE);
        }
        
        return methods;
    }
}