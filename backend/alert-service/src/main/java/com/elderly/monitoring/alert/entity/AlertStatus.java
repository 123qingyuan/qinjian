package main.java.com.elderly.monitoring.alert.entity;

/**
 * 预警状态枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum AlertStatus {
    PENDING("待处理"),
    PROCESSING("处理中"),
    RESOLVED("已解决"),
    IGNORED("已忽略"),
    FALSE_POSITIVE("误报");

    private final String description;

    AlertStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}