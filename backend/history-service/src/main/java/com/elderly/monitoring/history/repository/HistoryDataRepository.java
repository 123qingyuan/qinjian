package main.java.com.elderly.monitoring.history.repository;

import main.java.com.elderly.monitoring.history.entity.HistoryData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 历史数据访问接口
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Repository
public interface HistoryDataRepository extends JpaRepository<HistoryData, Long> {

    /**
     * 根据用户ID查询历史数据
     */
    List<HistoryData> findByUserIdOrderByRecordedAtDesc(Long userId);

    /**
     * 根据用户ID和数据类型查询历史数据
     */
    List<HistoryData> findByUserIdAndDataTypeOrderByRecordedAtDesc(Long userId, String dataType);

    /**
     * 根据用户ID和设备ID查询历史数据
     */
    List<HistoryData> findByUserIdAndDeviceIdOrderByRecordedAtDesc(Long userId, String deviceId);

    /**
     * 根据用户ID和时间范围查询历史数据
     */
    @Query("SELECT h FROM HistoryData h WHERE h.userId = :userId AND h.recordedAt BETWEEN :startTime AND :endTime ORDER BY h.recordedAt DESC")
    List<HistoryData> findByUserIdAndTimeRange(@Param("userId") Long userId, 
                                               @Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 根据用户ID、数据类型和时间范围查询历史数据
     */
    @Query("SELECT h FROM HistoryData h WHERE h.userId = :userId AND h.dataType = :dataType AND h.recordedAt BETWEEN :startTime AND :endTime ORDER BY h.recordedAt DESC")
    List<HistoryData> findByUserIdAndDataTypeAndTimeRange(@Param("userId") Long userId, 
                                                       @Param("dataType") String dataType,
                                                       @Param("startTime") LocalDateTime startTime, 
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户数据量
     */
    @Query("SELECT COUNT(h) FROM HistoryData h WHERE h.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计各类型数据量
     */
    @Query("SELECT h.dataType, COUNT(h) FROM HistoryData h WHERE h.userId = :userId AND h.recordedAt >= :since GROUP BY h.dataType")
    List<Object[]> countByDataTypeSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * 获取异常数据
     */
    @Query("SELECT h FROM HistoryData h WHERE h.userId = :userId AND h.isAbnormal = true ORDER BY h.recordedAt DESC")
    List<HistoryData> findAbnormalDataByUserId(@Param("userId") Long userId);

    /**
     * 获取最新数据
     */
    @Query("SELECT h FROM HistoryData h WHERE h.userId = :userId AND h.dataType = :dataType ORDER BY h.recordedAt DESC")
    List<HistoryData> findLatestByUserIdAndDataType(@Param("userId") Long userId, @Param("dataType") String dataType);

    /**
     * 计算数据统计信息
     */
    @Query("SELECT h.dataType, AVG(h.dataValue), MIN(h.dataValue), MAX(h.dataValue), COUNT(h) FROM HistoryData h WHERE h.userId = :userId AND h.recordedAt >= :since GROUP BY h.dataType")
    List<Object[]> getDataStatistics(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * 获取健康趋势数据
     */
    @Query("SELECT DATE(h.recordedAt) as date, h.dataType, AVG(h.dataValue) as avgValue FROM HistoryData h WHERE h.userId = :userId AND h.recordedAt >= :since GROUP BY DATE(h.recordedAt), h.dataType ORDER BY date DESC")
    List<Object[]> getHealthTrendData(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * 分页查询历史数据
     */
    @Query("SELECT h FROM HistoryData h WHERE h.userId = :userId ORDER BY h.recordedAt DESC")
    List<HistoryData> findByUserIdOrderByRecordedAtDesc(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
}