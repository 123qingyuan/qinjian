package main.java.com.elderly.monitoring.device.service;

import main.java.com.elderly.monitoring.device.entity.Device;
import main.java.com.elderly.monitoring.device.entity.DeviceStatus;
import main.java.com.elderly.monitoring.device.entity.DeviceType;
import main.java.com.elderly.monitoring.device.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 设备服务类
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Service
@Transactional
public class DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Value("${device.heartbeat.interval:30000}")
    private long heartbeatInterval;

    @Value("${device.heartbeat.timeout:90000}")
    private long heartbeatTimeout;

    @Value("${device.battery.low-threshold:20}")
    private int lowBatteryThreshold;

    /**
     * 创建设备
     * 
     * @param device 设备信息
     * @return 创建的设备
     */
    public Device createDevice(Device device) {
        // 检查设备ID是否已存在
        if (deviceRepository.existsByDeviceId(device.getDeviceId())) {
            throw new RuntimeException("设备ID已存在");
        }

        // 设置默认值
        if (device.getStatus() == null) {
            device.setStatus(DeviceStatus.OFFLINE);
        }
        if (device.getIsOnline() == null) {
            device.setIsOnline(false);
        }

        // 设置安装时间
        if (device.getInstallationDate() == null) {
            device.setInstallationDate(LocalDateTime.now());
        }

        return deviceRepository.save(device);
    }

    /**
     * 根据ID查找设备
     * 
     * @param id 设备ID
     * @return 设备信息
     */
    @Transactional(readOnly = true)
    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    /**
     * 根据设备ID查找设备
     * 
     * @param deviceId 设备ID
     * @return 设备信息
     */
    @Transactional(readOnly = true)
    public Optional<Device> findByDeviceId(String deviceId) {
        return deviceRepository.findByDeviceId(deviceId);
    }

    /**
     * 更新设备信息
     * 
     * @param id 设备ID
     * @param updatedDevice 更新的设备信息
     * @return 更新后的设备信息
     */
    public Device updateDevice(Long id, Device updatedDevice) {
        Optional<Device> existingDeviceOpt = deviceRepository.findById(id);
        if (!existingDeviceOpt.isPresent()) {
            throw new RuntimeException("设备不存在");
        }

        Device existingDevice = existingDeviceOpt.get();

        // 检查设备ID是否被其他设备使用
        if (!existingDevice.getDeviceId().equals(updatedDevice.getDeviceId()) 
                && deviceRepository.existsByDeviceId(updatedDevice.getDeviceId())) {
            throw new RuntimeException("设备ID已存在");
        }

        // 更新设备信息
        existingDevice.setDeviceId(updatedDevice.getDeviceId());
        existingDevice.setDeviceName(updatedDevice.getDeviceName());
        existingDevice.setDeviceType(updatedDevice.getDeviceType());
        existingDevice.setStatus(updatedDevice.getStatus());
        existingDevice.setUserId(updatedDevice.getUserId());
        existingDevice.setUserName(updatedDevice.getUserName());
        existingDevice.setModel(updatedDevice.getModel());
        existingDevice.setBrand(updatedDevice.getBrand());
        existingDevice.setVersion(updatedDevice.getVersion());
        existingDevice.setMacAddress(updatedDevice.getMacAddress());
        existingDevice.setIpAddress(updatedDevice.getIpAddress());
        existingDevice.setBatteryLevel(updatedDevice.getBatteryLevel());
        existingDevice.setSignalStrength(updatedDevice.getSignalStrength());
        existingDevice.setLatitude(updatedDevice.getLatitude());
        existingDevice.setLongitude(updatedDevice.getLongitude());
        existingDevice.setLocationAddress(updatedDevice.getLocationAddress());
        existingDevice.setFirmwareVersion(updatedDevice.getFirmwareVersion());
        existingDevice.setInstallationDate(updatedDevice.getInstallationDate());
        existingDevice.setLastMaintenanceDate(updatedDevice.getLastMaintenanceDate());
        existingDevice.setNextMaintenanceDate(updatedDevice.getNextMaintenanceDate());
        existingDevice.setConfiguration(updatedDevice.getConfiguration());

        return deviceRepository.save(existingDevice);
    }

    /**
     * 删除设备（软删除）
     * 
     * @param id 设备ID
     * @return 是否删除成功
     */
    public boolean deleteDevice(Long id) {
        Optional<Device> deviceOpt = deviceRepository.findById(id);
        if (!deviceOpt.isPresent()) {
            return false;
        }

        Device device = deviceOpt.get();
        device.setDeleted(true);
        device.setStatus(DeviceStatus.DELETED);
        device.setIsOnline(false);
        deviceRepository.save(device);
        
        return true;
    }

    /**
     * 分页查询设备
     * 
     * @param pageable 分页参数
     * @return 设备分页列表
     */
    @Transactional(readOnly = true)
    public Page<Device> findAllDevices(Pageable pageable) {
        return deviceRepository.findAllActiveDevices(pageable);
    }

    /**
     * 根据用户ID查找设备
     * 
     * @param userId 用户ID
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    public List<Device> findByUserId(Long userId) {
        return deviceRepository.findByUserId(userId);
    }

    /**
     * 根据设备类型查找设备
     * 
     * @param deviceType 设备类型
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    public List<Device> findByDeviceType(DeviceType deviceType) {
        return deviceRepository.findByDeviceType(deviceType);
    }

    /**
     * 根据设备状态查找设备
     * 
     * @param status 设备状态
     * @return 设备列表
     */
    @Transactional(readOnly = true)
    public List<Device> findByStatus(DeviceStatus status) {
        return deviceRepository.findByStatus(status);
    }

    /**
     * 多条件查询设备
     * 
     * @param deviceId 设备ID
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param status 设备状态
     * @param userId 用户ID
     * @param isOnline 是否在线
     * @param pageable 分页参数
     * @return 设备分页列表
     */
    @Transactional(readOnly = true)
    public Page<Device> searchDevices(String deviceId, String deviceName, DeviceType deviceType, 
                                     DeviceStatus status, Long userId, Boolean isOnline, Pageable pageable) {
        return deviceRepository.findByMultipleConditions(deviceId, deviceName, deviceType, status, userId, isOnline, pageable);
    }

    /**
     * 更新设备在线状态
     * 
     * @param deviceId 设备ID
     * @param isOnline 在线状态
     * @return 是否更新成功
     */
    public boolean updateDeviceOnlineStatus(String deviceId, boolean isOnline) {
        LocalDateTime now = LocalDateTime.now();
        int updatedRows = deviceRepository.updateDeviceOnlineStatus(deviceId, isOnline, now);
        
        if (updatedRows > 0 && isOnline) {
            // 如果设备上线，自动更新设备状态为在线
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                device.setStatus(DeviceStatus.ONLINE);
                deviceRepository.save(device);
            }
        }
        
        return updatedRows > 0;
    }

    /**
     * 更新设备心跳
     * 
     * @param deviceId 设备ID
     * @return 是否更新成功
     */
    public boolean updateDeviceHeartbeat(String deviceId) {
        return updateDeviceOnlineStatus(deviceId, true);
    }

    /**
     * 更新设备位置信息
     * 
     * @param deviceId 设备ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param locationAddress 位置地址
     * @return 是否更新成功
     */
    public boolean updateDeviceLocation(String deviceId, Double latitude, Double longitude, String locationAddress) {
        return deviceRepository.updateDeviceLocation(deviceId, latitude, longitude, locationAddress) > 0;
    }

    /**
     * 更新设备电量信息
     * 
     * @param deviceId 设备ID
     * @param batteryLevel 电量水平
     * @return 是否更新成功
     */
    public boolean updateDeviceBattery(String deviceId, Integer batteryLevel) {
        boolean success = deviceRepository.updateDeviceBattery(deviceId, batteryLevel) > 0;
        
        if (success) {
            // 检查电量状态
            Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
            if (deviceOpt.isPresent()) {
                Device device = deviceOpt.get();
                if (batteryLevel <= lowBatteryThreshold) {
                    device.setStatus(DeviceStatus.LOW_BATTERY);
                    deviceRepository.save(device);
                }
            }
        }
        
        return success;
    }

    /**
     * 检查设备心跳超时
     * 
     * @return 超时的设备列表
     */
    @Transactional(readOnly = true)
    public List<Device> checkHeartbeatTimeout() {
        LocalDateTime threshold = LocalDateTime.now().minusMillis(heartbeatTimeout);
        return deviceRepository.findDevicesWithMissedHeartbeat(threshold);
    }

    /**
     * 处理心跳超时的设备
     * 
     * @return 处理的设备数量
     */
    public int handleHeartbeatTimeout() {
        List<Device> timeoutDevices = checkHeartbeatTimeout();
        int count = 0;
        
        for (Device device : timeoutDevices) {
            device.setIsOnline(false);
            device.setStatus(DeviceStatus.OFFLINE);
            deviceRepository.save(device);
            count++;
        }
        
        return count;
    }

    /**
     * 绑定设备到用户
     * 
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @param userName 用户名称
     * @return 是否绑定成功
     */
    public boolean bindDeviceToUser(String deviceId, Long userId, String userName) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (!deviceOpt.isPresent()) {
            return false;
        }

        Device device = deviceOpt.get();
        device.setUserId(userId);
        device.setUserName(userName);
        deviceRepository.save(device);
        
        return true;
    }

    /**
     * 解绑设备
     * 
     * @param deviceId 设备ID
     * @return 是否解绑成功
     */
    public boolean unbindDevice(String deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findByDeviceId(deviceId);
        if (!deviceOpt.isPresent()) {
            return false;
        }

        Device device = deviceOpt.get();
        device.setUserId(null);
        device.setUserName(null);
        deviceRepository.save(device);
        
        return true;
    }

    /**
     * 获取设备统计信息
     * 
     * @return 统计信息
     */
    @Transactional(readOnly = true)
    public DeviceStatistics getDeviceStatistics() {
        List<Object[]> typeStats = deviceRepository.countDevicesByType();
        List<Object[]> statusStats = deviceRepository.countDevicesByStatus();
        List<Object[]> onlineStats = deviceRepository.countDevicesByOnlineStatus();
        
        DeviceStatistics statistics = new DeviceStatistics();
        
        // 处理类型统计
        for (Object[] stat : typeStats) {
            DeviceType type = (DeviceType) stat[0];
            Long count = (Long) stat[1];
            statistics.addTypeCount(type, count);
            statistics.setTotalDevices(statistics.getTotalDevices() + count);
        }
        
        // 处理状态统计
        for (Object[] stat : statusStats) {
            DeviceStatus status = (DeviceStatus) stat[0];
            Long count = (Long) stat[1];
            statistics.addStatusCount(status, count);
        }
        
        // 处理在线状态统计
        for (Object[] stat : onlineStats) {
            Boolean isOnline = (Boolean) stat[0];
            Long count = (Long) stat[1];
            if (isOnline) {
                statistics.setOnlineCount(count);
            } else {
                statistics.setOfflineCount(count);
            }
        }
        
        return statistics;
    }

    /**
     * 获取低电量设备列表
     * 
     * @return 低电量设备列表
     */
    @Transactional(readOnly = true)
    public List<Device> getLowBatteryDevices() {
        return deviceRepository.findLowBatteryDevices(lowBatteryThreshold);
    }

    /**
     * 获取需要维护的设备列表
     * 
     * @return 需要维护的设备列表
     */
    @Transactional(readOnly = true)
    public List<Device> getDevicesNeedingMaintenance() {
        return deviceRepository.findDevicesNeedingMaintenance(LocalDateTime.now());
    }

    /**
     * 设备统计信息类
     */
    public static class DeviceStatistics {
        private Long totalDevices = 0L;
        private Long wearableCount = 0L;
        private Long fallDetectorCount = 0L;
        private Long heartRateMonitorCount = 0L;
        private Long bpMonitorCount = 0L;
        private Long glucoseMeterCount = 0L;
        private Long smartWatchCount = 0L;
        private Long gpsTrackerCount = 0L;
        private Long emergencyButtonCount = 0L;
        private Long onlineCount = 0L;
        private Long offlineCount = 0L;
        private Long onlineStatusCount = 0L;
        private Long offlineStatusCount = 0L;
        private Long malfunctionCount = 0L;
        private Long maintenanceCount = 0L;
        private Long lowBatteryCount = 0L;

        // Getter和Setter方法
        public Long getTotalDevices() { return totalDevices; }
        public void setTotalDevices(Long totalDevices) { this.totalDevices = totalDevices; }

        public Long getWearableCount() { return wearableCount; }
        public void setWearableCount(Long wearableCount) { this.wearableCount = wearableCount; }

        public Long getFallDetectorCount() { return fallDetectorCount; }
        public void setFallDetectorCount(Long fallDetectorCount) { this.fallDetectorCount = fallDetectorCount; }

        public Long getHeartRateMonitorCount() { return heartRateMonitorCount; }
        public void setHeartRateMonitorCount(Long heartRateMonitorCount) { this.heartRateMonitorCount = heartRateMonitorCount; }

        public Long getBpMonitorCount() { return bpMonitorCount; }
        public void setBpMonitorCount(Long bpMonitorCount) { this.bpMonitorCount = bpMonitorCount; }

        public Long getGlucoseMeterCount() { return glucoseMeterCount; }
        public void setGlucoseMeterCount(Long glucoseMeterCount) { this.glucoseMeterCount = glucoseMeterCount; }

        public Long getSmartWatchCount() { return smartWatchCount; }
        public void setSmartWatchCount(Long smartWatchCount) { this.smartWatchCount = smartWatchCount; }

        public Long getGpsTrackerCount() { return gpsTrackerCount; }
        public void setGpsTrackerCount(Long gpsTrackerCount) { this.gpsTrackerCount = gpsTrackerCount; }

        public Long getEmergencyButtonCount() { return emergencyButtonCount; }
        public void setEmergencyButtonCount(Long emergencyButtonCount) { this.emergencyButtonCount = emergencyButtonCount; }

        public Long getOnlineCount() { return onlineCount; }
        public void setOnlineCount(Long onlineCount) { this.onlineCount = onlineCount; }

        public Long getOfflineCount() { return offlineCount; }
        public void setOfflineCount(Long offlineCount) { this.offlineCount = offlineCount; }

        public Long getOnlineStatusCount() { return onlineStatusCount; }
        public void setOnlineStatusCount(Long onlineStatusCount) { this.onlineStatusCount = onlineStatusCount; }

        public Long getOfflineStatusCount() { return offlineStatusCount; }
        public void setOfflineStatusCount(Long offlineStatusCount) { this.offlineStatusCount = offlineStatusCount; }

        public Long getMalfunctionCount() { return malfunctionCount; }
        public void setMalfunctionCount(Long malfunctionCount) { this.malfunctionCount = malfunctionCount; }

        public Long getMaintenanceCount() { return maintenanceCount; }
        public void setMaintenanceCount(Long maintenanceCount) { this.maintenanceCount = maintenanceCount; }

        public Long getLowBatteryCount() { return lowBatteryCount; }
        public void setLowBatteryCount(Long lowBatteryCount) { this.lowBatteryCount = lowBatteryCount; }

        public void addTypeCount(DeviceType type, Long count) {
            switch (type) {
                case WEARABLE:
                    wearableCount = count;
                    break;
                case FALL_DETECTOR:
                    fallDetectorCount = count;
                    break;
                case HEART_RATE_MONITOR:
                    heartRateMonitorCount = count;
                    break;
                case BLOOD_PRESSURE_MONITOR:
                    bpMonitorCount = count;
                    break;
                case GLUCOSE_METER:
                    glucoseMeterCount = count;
                    break;
                case SMART_WATCH:
                    smartWatchCount = count;
                    break;
                case GPS_TRACKER:
                    gpsTrackerCount = count;
                    break;
                case EMERGENCY_BUTTON:
                    emergencyButtonCount = count;
                    break;
            }
        }

        public void addStatusCount(DeviceStatus status, Long count) {
            switch (status) {
                case ONLINE:
                    onlineStatusCount = count;
                    break;
                case OFFLINE:
                    offlineStatusCount = count;
                    break;
                case MALFUNCTION:
                    malfunctionCount = count;
                    break;
                case MAINTENANCE:
                    maintenanceCount = count;
                    break;
                case LOW_BATTERY:
                    lowBatteryCount = count;
                    break;
            }
        }
    }
}