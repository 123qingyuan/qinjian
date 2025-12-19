package main.java.com.elderly.monitoring.monitoring.repository;

import main.java.com.elderly.monitoring.monitoring.entity.MonitoringData;
import main.java.com.elderly.monitoring.monitoring.entity.DataType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 监控数据访问接口
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Repository
public interface MonitoringDataRepository extends JpaRepository<MonitoringData, Long> {

    /**
     * 根据设备ID和时间戳查询数据
     */
    List<MonitoringData> findByDeviceIdAndTimestampAfter(String deviceId, LocalDateTime timestamp);

    /**
     * 根据数据类型和时间戳查询数据
     */
    List<MonitoringData> findByDataTypeAndTimestampAfter(DataType dataType, LocalDateTime timestamp);

    /**
     * 根据设备ID、数据类型和时间戳查询数据
     */
    List<MonitoringData> findByDeviceIdAndDataTypeAndTimestampAfter(
        String deviceId, DataType dataType, LocalDateTime timestamp);

    /**
     * 根据时间戳查询数据
     */
    List<MonitoringData> findByTimestampAfter(LocalDateTime timestamp);

    /**
     * 根据设备ID和时间范围查询数据
     */
    List<MonitoringData> findByDeviceIdAndTimestampBetween(
        String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据用户ID查询最新数据
     */
    @Query("SELECT m FROM MonitoringData m WHERE m.userId = :userId ORDER BY m.createdAt DESC")
    List<MonitoringData> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);

    /**
     * 获取最新的N条数据
     */
    List<MonitoringData> findTop10ByOrderByCreatedAtDesc();

    /**
     * 根据设备ID查询最新数据
     */
    @Query("SELECT m FROM MonitoringData m WHERE m.deviceId = :deviceId ORDER BY m.createdAt DESC")
    List<MonitoringData> findLatestByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 统计异常数据数量
     */
    @Query("SELECT COUNT(m) FROM MonitoringData m WHERE m.isAbnormal = true")
    Long countAbnormalData();

    /**
     * 根据数据类型统计数据
     */
    @Query("SELECT m.dataType, COUNT(m) FROM MonitoringData m GROUP BY m.dataType")
    List<Object[]> countByDataType();
}