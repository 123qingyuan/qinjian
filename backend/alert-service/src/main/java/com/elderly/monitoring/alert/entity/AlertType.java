package main.java.com.elderly.monitoring.alert.entity;

/**
 * 预警类型枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum AlertType {
    TEMPERATURE("温度预警"),
    HUMIDITY("湿度预警"),
    MOTION("运动预警"),
    DOOR("门磁预警"),
    HEART_RATE("心率预警"),
    BLOOD_PRESSURE("血压预警"),
    FALL("跌倒预警"),
    LOCATION("位置预警"),
    DEVICE_OFFLINE("设备离线预警"),
    BATTERY_LOW("电量不足预警");

    private final String description;

    AlertType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}