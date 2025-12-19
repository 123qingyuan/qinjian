package main.java.com.elderly.monitoring.device.entity;

/**
 * 设备状态枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum DeviceStatus {
    ONLINE("在线"),
    OFFLINE("离线"),
    MALFUNCTION("故障"),
    MAINTENANCE("维护中"),
    LOW_BATTERY("低电量"),
    DAMAGED("损坏"),
    LOST("丢失"),
    DELETED("已删除");

    private final String description;

    DeviceStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}