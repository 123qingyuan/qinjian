package main.java.com.elderly.monitoring.alert.repository;

import main.java.com.elderly.monitoring.alert.entity.AlertRecord;
import main.java.com.elderly.monitoring.alert.entity.AlertStatus;
import main.java.com.elderly.monitoring.alert.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 预警记录数据访问接口
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Repository
public interface AlertRecordRepository extends JpaRepository<AlertRecord, Long> {

    /**
     * 根据用户ID查询预警记录
     */
    List<AlertRecord> findByUserIdOrderByTriggeredAtDesc(Long userId);

    /**
     * 根据用户ID和状态查询预警记录
     */
    List<AlertRecord> findByUserIdAndStatusOrderByTriggeredAtDesc(Long userId, AlertStatus status);

    /**
     * 根据设备ID查询预警记录
     */
    List<AlertRecord> findByDeviceIdOrderByTriggeredAtDesc(String deviceId);

    /**
     * 根据预警类型查询预警记录
     */
    List<AlertRecord> findByAlertTypeOrderByTriggeredAtDesc(AlertType alertType);

    /**
     * 根据用户ID和时间范围查询预警记录
     */
    @Query("SELECT a FROM AlertRecord a WHERE a.userId = :userId AND a.triggeredAt BETWEEN :startTime AND :endTime ORDER BY a.triggeredAt DESC")
    List<AlertRecord> findByUserIdAndTimeRange(@Param("userId") Long userId, 
                                               @Param("startTime") LocalDateTime startTime, 
                                               @Param("endTime") LocalDateTime endTime);

    /**
     * 统计待处理的预警数量
     */
    @Query("SELECT COUNT(a) FROM AlertRecord a WHERE a.userId = :userId AND a.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") AlertStatus status);

    /**
     * 统计各类型的预警数量
     */
    @Query("SELECT a.alertType, COUNT(a) FROM AlertRecord a WHERE a.userId = :userId AND a.triggeredAt >= :since GROUP BY a.alertType")
    List<Object[]> countByAlertTypeSince(@Param("userId") Long userId, @Param("since") LocalDateTime since);

    /**
     * 获取最近的预警记录
     */
    List<AlertRecord> findTop10ByUserIdOrderByTriggeredAtDesc(Long userId);

    /**
     * 根据规则ID查询预警记录
     */
    List<AlertRecord> findByRuleIdOrderByTriggeredAtDesc(Long ruleId);

    /**
     * 统计今日预警数量
     */
    @Query("SELECT COUNT(a) FROM AlertRecord a WHERE a.userId = :userId AND DATE(a.triggeredAt) = CURRENT_DATE")
    Long countTodayAlerts(@Param("userId") Long userId);
}