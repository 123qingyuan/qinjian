package main.java.com.elderly.monitoring.monitoring.entity;

/**
 * 监控数据类型枚举
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
public enum DataType {
    HEART_RATE("心率", "bpm"),
    BLOOD_PRESSURE_SYSTOLIC("收缩压", "mmHg"),
    BLOOD_PRESSURE_DIASTOLIC("舒张压", "mmHg"),
    BLOOD_OXYGEN("血氧饱和度", "%"),
    BLOOD_SUGAR("血糖", "mmol/L"),
    BODY_TEMPERATURE("体温", "°C"),
    RESPIRATORY_RATE("呼吸频率", "次/分钟"),
    STEPS("步数", "步"),
    CALORIES("卡路里", "kcal"),
    SLEEP_DURATION("睡眠时长", "小时"),
    SLEEP_QUALITY("睡眠质量", "分"),
    ACTIVITY_LEVEL("活动水平", "分"),
    FALL_DETECTION("跌倒检测", "状态"),
    EMERGENCY_BUTTON("紧急按钮", "状态"),
    LOCATION("位置", "坐标"),
    BATTERY_LEVEL("电量", "%"),
    SIGNAL_STRENGTH("信号强度", "dBm"),
    DEVICE_STATUS("设备状态", "状态"),
    MOVEMENT("运动", "状态"),
    INACTIVITY("静止时长", "分钟"),
    POSTURE("姿势", "状态"),
    STRESS_LEVEL("压力水平", "分"),
    ANXIETY_LEVEL("焦虑水平", "分"),
    MOOD("情绪", "状态"),
    WEIGHT("体重", "kg"),
    HEIGHT("身高", "cm"),
    BMI("身体质量指数", "kg/m²"),
    BODY_FAT("体脂率", "%"),
    MUSCLE_MASS("肌肉量", "kg"),
    WATER_CONTENT("水分含量", "%"),
    BONE_MASS("骨量", "kg"),
    PROTEIN("蛋白质", "g"),
    SODIUM("钠", "mg"),
    POTASSIUM("钾", "mg"),
    CHOLESTEROL("胆固醇", "mmol/L"),
    TRIGLYCERIDES("甘油三酯", "mmol/L"),
    HDL_CHOLESTEROL("高密度脂蛋白", "mmol/L"),
    LDL_CHOLESTEROL("低密度脂蛋白", "mmol/L"),
    URIC_ACID("尿酸", "μmol/L"),
    CREATININE("肌酐", "μmol/L"),
    UREA_NITROGEN("尿素氮", "mmol/L"),
    ALBUMIN("白蛋白", "g/L"),
    GLOBULIN("球蛋白", "g/L"),
    HEMOGLOBIN("血红蛋白", "g/L"),
    RED_BLOOD_CELLS("红细胞", "10^12/L"),
    WHITE_BLOOD_CELLS("白细胞", "10^9/L"),
    PLATELETS("血小板", "10^9/L"),
    HEMATOCRIT("红细胞压积", "%"),
    MEAN_CORPUSCULAR_VOLUME("平均红细胞体积", "fL"),
    MEAN_CORPUSCULAR_HEMOGLOBIN("平均红细胞血红蛋白", "pg"),
    MEAN_CORPUSCULAR_HEMOGLOBIN_CONCENTRATION("平均红细胞血红蛋白浓度", "g/L");

    private final String description;
    private final String defaultUnit;

    DataType(String description, String defaultUnit) {
        this.description = description;
        this.defaultUnit = defaultUnit;
    }

    public String getDescription() {
        return description;
    }

    public String getDefaultUnit() {
        return defaultUnit;
    }
}