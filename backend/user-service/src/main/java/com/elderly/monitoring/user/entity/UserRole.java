package main.java.com.elderly.monitoring.user.entity;

/**
 * 用户角色枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum UserRole {
    ADMIN("管理员"),
    FAMILY_MEMBER("家庭成员"),
    CAREGIVER("护工"),
    DOCTOR("医生"),
    ELDERLY("老人");

    private final String description;

    UserRole(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}