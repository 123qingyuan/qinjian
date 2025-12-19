package main.java.com.elderly.monitoring.monitoring.service;

import com.elderly.monitoring.monitoring.entity.MonitoringData;
import com.elderly.monitoring.monitoring.repository.MonitoringDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 实时监控服务实现类
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Service
public class MonitoringService {

    @Autowired
    private MonitoringDataRepository monitoringDataRepository;

    /**
     * 获取实时监控数据
     */
    public List<MonitoringData> getRealTimeData(String deviceId, String dataType) {
        // 获取最近5分钟的数据
        LocalDateTime fiveMinutesAgo = LocalDateTime.now().minusMinutes(5);
        
        if (deviceId != null && !deviceId.isEmpty() && dataType != null && !dataType.isEmpty()) {
            return monitoringDataRepository.findByDeviceIdAndDataTypeAndTimestampAfter(
                deviceId, dataType, fiveMinutesAgo);
        } else if (deviceId != null && !deviceId.isEmpty()) {
            return monitoringDataRepository.findByDeviceIdAndTimestampAfter(deviceId, fiveMinutesAgo);
        } else if (dataType != null && !dataType.isEmpty()) {
            return monitoringDataRepository.findByDataTypeAndTimestampAfter(dataType, fiveMinutesAgo);
        } else {
            return monitoringDataRepository.findByTimestampAfter(fiveMinutesAgo);
        }
    }

    /**
     * 保存监控数据
     */
    public MonitoringData saveMonitoringData(MonitoringData monitoringData) {
        if (monitoringData.getTimestamp() == null) {
            monitoringData.setTimestamp(LocalDateTime.now());
        }
        return monitoringDataRepository.save(monitoringData);
    }

    /**
     * 根据用户ID获取最新数据
     */
    public List<MonitoringData> getLatestDataByUser(String userId) {
        // 这里需要根据用户ID关联设备ID，为简化示例，直接返回最新数据
        return monitoringDataRepository.findTop10ByOrderByTimestampDesc();
    }

    /**
     * 获取历史监控数据
     */
    public List<MonitoringData> getHistoryData(String deviceId, String startTime, String endTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startTime, formatter);
        LocalDateTime end = LocalDateTime.parse(endTime, formatter);
        
        return monitoringDataRepository.findByDeviceIdAndTimestampBetween(deviceId, start, end);
    }

    /**
     * 获取设备状态
     */
    public List<Map<String, Object>> getDeviceStatus() {
        List<Map<String, Object>> deviceStatus = new ArrayList<>();
        
        // 模拟设备状态数据
        Map<String, Object> device1 = new HashMap<>();
        device1.put("deviceId", "CAM001");
        device1.put("deviceName", "客厅摄像头");
        device1.put("location", "客厅");
        device1.put("status", "ONLINE");
        device1.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        device1.put("deviceType", "CAMERA");
        deviceStatus.add(device1);
        
        Map<String, Object> device2 = new HashMap<>();
        device2.put("deviceId", "CAM002");
        device2.put("deviceName", "卧室摄像头");
        device2.put("location", "主卧室");
        device2.put("status", "ONLINE");
        device2.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        device2.put("deviceType", "CAMERA");
        deviceStatus.add(device2);
        
        Map<String, Object> device3 = new HashMap<>();
        device3.put("deviceId", "TEMP001");
        device3.put("deviceName", "客厅温度传感器");
        device3.put("location", "客厅");
        device3.put("status", "ONLINE");
        device3.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        device3.put("deviceType", "SENSOR");
        deviceStatus.add(device3);
        
        Map<String, Object> device4 = new HashMap<>();
        device4.put("deviceId", "HUM001");
        device4.put("deviceName", "客厅湿度传感器");
        device4.put("location", "客厅");
        device4.put("status", "ONLINE");
        device4.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        device4.put("deviceType", "SENSOR");
        deviceStatus.add(device4);
        
        Map<String, Object> device5 = new HashMap<>();
        device5.put("deviceId", "MOTION001");
        device5.put("deviceName", "客厅运动传感器");
        device5.put("location", "客厅");
        device5.put("status", "WARNING");
        device5.put("lastUpdate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        device5.put("deviceType", "SENSOR");
        deviceStatus.add(device5);
        
        return deviceStatus;
    }

    /**
     * 生成模拟传感器数据
     */
    public List<MonitoringData> generateMockSensorData() {
        List<MonitoringData> mockData = new ArrayList<>();
        Random random = new Random();
        
        // 温度数据
        MonitoringData tempData = new MonitoringData();
        tempData.setId(UUID.randomUUID().toString());
        tempData.setDeviceId("TEMP001");
        tempData.setDeviceName("客厅温度传感器");
        tempData.setDataType("TEMPERATURE");
        tempData.setValue(22.0 + random.nextDouble() * 6.0);
        tempData.setUnit("°C");
        tempData.setStatus("NORMAL");
        tempData.setTimestamp(LocalDateTime.now());
        mockData.add(tempData);
        
        // 湿度数据
        MonitoringData humidityData = new MonitoringData();
        humidityData.setId(UUID.randomUUID().toString());
        humidityData.setDeviceId("HUM001");
        humidityData.setDeviceName("客厅湿度传感器");
        humidityData.setDataType("HUMIDITY");
        humidityData.setValue(60.0 + random.nextDouble() * 20.0);
        humidityData.setUnit("%");
        humidityData.setStatus("NORMAL");
        humidityData.setTimestamp(LocalDateTime.now());
        mockData.add(humidityData);
        
        // 运动数据
        MonitoringData motionData = new MonitoringData();
        motionData.setId(UUID.randomUUID().toString());
        motionData.setDeviceId("MOTION001");
        motionData.setDeviceName("客厅运动传感器");
        motionData.setDataType("MOTION");
        motionData.setValue(random.nextBoolean() ? 1.0 : 0.0);
        motionData.setUnit("");
        motionData.setStatus(random.nextBoolean() ? "NORMAL" : "WARNING");
        motionData.setTimestamp(LocalDateTime.now());
        mockData.add(motionData);
        
        return mockData;
    }
}