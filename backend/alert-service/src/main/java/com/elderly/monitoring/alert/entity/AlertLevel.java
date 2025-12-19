package main.java.com.elderly.monitoring.alert.entity;

/**
 * 预警等级枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum AlertLevel {
    LOW("低级预警", 1),
    MEDIUM("中级预警", 2),
    HIGH("高级预警", 3),
    CRITICAL("紧急预警", 4);

    private final String description;
    private final int priority;

    AlertLevel(String description, int priority) {
        this.description = description;
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public int getPriority() {
        return priority;
    }
}