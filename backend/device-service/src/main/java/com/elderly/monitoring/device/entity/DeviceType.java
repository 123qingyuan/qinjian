package main.java.com.elderly.monitoring.device.entity;

/**
 * 设备类型枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum DeviceType {
    WEARABLE("可穿戴设备"),
    FALL_DETECTOR("跌倒检测器"),
    HEART_RATE_MONITOR("心率监测器"),
    BLOOD_PRESSURE_MONITOR("血压监测仪"),
    GLUCOSE_METER("血糖仪"),
    SMART_WATCH("智能手表"),
    SMART_BRACELET("智能手环"),
    GPS_TRACKER("GPS定位器"),
    EMERGENCY_BUTTON("紧急按钮"),
    MOTION_SENSOR("运动传感器"),
    DOOR_SENSOR("门窗传感器"),
    BED_SENSOR("床铺传感器"),
    SMART_SCALE("智能体重秤"),
    SMART_MIRROR("智能镜"),
    VOICE_ASSISTANT("语音助手"),
    MEDICATION_REMINDER("用药提醒器"),
    SMOKE_DETECTOR("烟雾报警器"),
    GAS_DETECTOR("燃气报警器"),
    TEMPERATURE_SENSOR("温度传感器"),
    HUMIDITY_SENSOR("湿度传感器"),
    LIGHT_SENSOR("光照传感器"),
    CAMERA("摄像头"),
    SMART_LOCK("智能门锁"),
    SMART_SPEAKER("智能音箱");

    private final String description;

    DeviceType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}