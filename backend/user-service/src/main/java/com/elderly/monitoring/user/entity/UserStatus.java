package main.java.com.elderly.monitoring.user.entity;

/**
 * 用户状态枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum UserStatus {
    ACTIVE("激活"),
    INACTIVE("未激活"),
    SUSPENDED("暂停"),
    DELETED("已删除");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}