package main.java.com.elderly.monitoring.device.repository;

import main.java.com.elderly.monitoring.device.entity.Device;
import main.java.com.elderly.monitoring.device.entity.DeviceStatus;
import main.java.com.elderly.monitoring.device.entity.DeviceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 设备数据访问接口
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    /**
     * 根据设备ID查找设备
     * 
     * @param deviceId 设备ID
     * @return 设备信息
     */
    Optional<Device> findByDeviceId(String deviceId);

    /**
     * 检查设备ID是否存在
     * 
     * @param deviceId 设备ID
     * @return 是否存在
     */
    boolean existsByDeviceId(String deviceId);

    /**
     * 根据用户ID查找设备
     * 
     * @param userId 用户ID
     * @return 设备列表
     */
    List<Device> findByUserId(Long userId);

    /**
     * 根据设备类型查找设备
     * 
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<Device> findByDeviceType(DeviceType deviceType);

    /**
     * 根据设备状态查找设备
     * 
     * @param status 设备状态
     * @return 设备列表
     */
    List<Device> findByStatus(DeviceStatus status);

    /**
     * 根据在线状态查找设备
     * 
     * @param isOnline 是否在线
     * @return 设备列表
     */
    List<Device> findByIsOnline(Boolean isOnline);

    /**
     * 根据用户ID和设备类型查找设备
     * 
     * @param userId 用户ID
     * @param deviceType 设备类型
     * @return 设备列表
     */
    List<Device> findByUserIdAndDeviceType(Long userId, DeviceType deviceType);

    /**
     * 根据用户ID和在线状态查找设备
     * 
     * @param userId 用户ID
     * @param isOnline 是否在线
     * @return 设备列表
     */
    List<Device> findByUserIdAndIsOnline(Long userId, Boolean isOnline);

    /**
     * 根据设备名称模糊查询设备
     * 
     * @param deviceName 设备名称
     * @param pageable 分页参数
     * @return 设备分页列表
     */
    @Query("SELECT d FROM Device d WHERE d.deviceName LIKE %:deviceName% AND d.deleted = false")
    Page<Device> findByDeviceNameContaining(@Param("deviceName") String deviceName, Pageable pageable);

    /**
     * 根据多条件查询设备
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
    @Query("SELECT d FROM Device d WHERE " +
           "(:deviceId IS NULL OR d.deviceId LIKE %:deviceId%) AND " +
           "(:deviceName IS NULL OR d.deviceName LIKE %:deviceName%) AND " +
           "(:deviceType IS NULL OR d.deviceType = :deviceType) AND " +
           "(:status IS NULL OR d.status = :status) AND " +
           "(:userId IS NULL OR d.userId = :userId) AND " +
           "(:isOnline IS NULL OR d.isOnline = :isOnline) AND " +
           "d.deleted = false")
    Page<Device> findByMultipleConditions(
            @Param("deviceId") String deviceId,
            @Param("deviceName") String deviceName,
            @Param("deviceType") DeviceType deviceType,
            @Param("status") DeviceStatus status,
            @Param("userId") Long userId,
            @Param("isOnline") Boolean isOnline,
            Pageable pageable);

    /**
     * 查找所有未删除的设备
     * 
     * @param pageable 分页参数
     * @return 设备分页列表
     */
    @Query("SELECT d FROM Device d WHERE d.deleted = false ORDER BY d.createdAt DESC")
    Page<Device> findAllActiveDevices(Pageable pageable);

    /**
     * 统计各类型设备数量
     * 
     * @return 统计结果
     */
    @Query("SELECT d.deviceType, COUNT(d) FROM Device d WHERE d.deleted = false GROUP BY d.deviceType")
    List<Object[]> countDevicesByType();

    /**
     * 统计各状态设备数量
     * 
     * @return 统计结果
     */
    @Query("SELECT d.status, COUNT(d) FROM Device d WHERE d.deleted = false GROUP BY d.status")
    List<Object[]> countDevicesByStatus();

    /**
     * 统计在线/离线设备数量
     * 
     * @return 统计结果
     */
    @Query("SELECT d.isOnline, COUNT(d) FROM Device d WHERE d.deleted = false GROUP BY d.isOnline")
    List<Object[]> countDevicesByOnlineStatus();

    /**
     * 查找低电量设备
     * 
     * @param threshold 电量阈值
     * @return 设备列表
     */
    @Query("SELECT d FROM Device d WHERE d.batteryLevel <= :threshold AND d.deleted = false AND d.isOnline = true")
    List<Device> findLowBatteryDevices(@Param("threshold") Integer threshold);

    /**
     * 查找需要维护的设备
     * 
     * @param date 当前日期
     * @return 设备列表
     */
    @Query("SELECT d FROM Device d WHERE d.nextMaintenanceDate <= :date AND d.deleted = false AND d.status != 'MAINTENANCE'")
    List<Device> findDevicesNeedingMaintenance(@Param("date") LocalDateTime date);

    /**
     * 查找长时间未心跳的设备
     * 
     * @param threshold 时间阈值
     * @return 设备列表
     */
    @Query("SELECT d FROM Device d WHERE d.lastHeartbeat < :threshold AND d.deleted = false AND d.isOnline = true")
    List<Device> findDevicesWithMissedHeartbeat(@Param("threshold") LocalDateTime threshold);

    /**
     * 更新设备在线状态
     * 
     * @param deviceId 设备ID
     * @param isOnline 在线状态
     * @param lastHeartbeat 最后心跳时间
     * @return 更新行数
     */
    @Query("UPDATE Device d SET d.isOnline = :isOnline, d.lastHeartbeat = :lastHeartbeat WHERE d.deviceId = :deviceId")
    int updateDeviceOnlineStatus(@Param("deviceId") String deviceId, 
                                 @Param("isOnline") Boolean isOnline, 
                                 @Param("lastHeartbeat") LocalDateTime lastHeartbeat);

    /**
     * 更新设备位置信息
     * 
     * @param deviceId 设备ID
     * @param latitude 纬度
     * @param longitude 经度
     * @param locationAddress 位置地址
     * @return 更新行数
     */
    @Query("UPDATE Device d SET d.latitude = :latitude, d.longitude = :longitude, d.locationAddress = :locationAddress WHERE d.deviceId = :deviceId")
    int updateDeviceLocation(@Param("deviceId") String deviceId,
                             @Param("latitude") Double latitude,
                             @Param("longitude") Double longitude,
                             @Param("locationAddress") String locationAddress);

    /**
     * 更新设备电量信息
     * 
     * @param deviceId 设备ID
     * @param batteryLevel 电量水平
     * @return 更新行数
     */
    @Query("UPDATE Device d SET d.batteryLevel = :batteryLevel WHERE d.deviceId = :deviceId")
    int updateDeviceBattery(@Param("deviceId") String deviceId, @Param("batteryLevel") Integer batteryLevel);

    /**
     * 根据品牌查找设备
     * 
     * @param brand 品牌
     * @return 设备列表
     */
    List<Device> findByBrand(String brand);

    /**
     * 根据MAC地址查找设备
     * 
     * @param macAddress MAC地址
     * @return 设备信息
     */
    Optional<Device> findByMacAddress(String macAddress);

    /**
     * 查找指定时间范围内的设备
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 设备分页列表
     */
    @Query("SELECT d FROM Device d WHERE d.createdAt BETWEEN :startTime AND :endTime AND d.deleted = false")
    Page<Device> findDevicesByTimeRange(@Param("startTime") LocalDateTime startTime,
                                       @Param("endTime") LocalDateTime endTime,
                                       Pageable pageable);
}