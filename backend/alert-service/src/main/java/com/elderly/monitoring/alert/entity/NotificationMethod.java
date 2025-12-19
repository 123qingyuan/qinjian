package main.java.com.elderly.monitoring.alert.entity;

/**
 * 通知方式枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum NotificationMethod {
    POPUP("弹窗通知"),
    SMS("短信通知"),
    EMAIL("邮件通知"),
    WECHAT("微信通知"),
    APP_PUSH("APP推送"),
    PHONE_CALL("电话通知");

    private final String description;

    NotificationMethod(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}