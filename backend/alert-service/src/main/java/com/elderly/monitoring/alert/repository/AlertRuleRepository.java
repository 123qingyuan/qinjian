package main.java.com.elderly.monitoring.alert.repository;

import main.java.com.elderly.monitoring.alert.entity.AlertRule;
import main.java.com.elderly.monitoring.alert.entity.AlertType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 预警规则数据访问接口
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    /**
     * 根据用户ID查询预警规则
     */
    List<AlertRule> findByUserIdAndIsActiveTrue(Long userId);

    /**
     * 根据设备ID查询预警规则
     */
    List<AlertRule> findByDeviceIdAndIsActiveTrue(String deviceId);

    /**
     * 根据用户ID和设备ID查询预警规则
     */
    List<AlertRule> findByUserIdAndDeviceIdAndIsActiveTrue(Long userId, String deviceId);

    /**
     * 根据预警类型查询预警规则
     */
    List<AlertRule> findByAlertTypeAndIsActiveTrue(AlertType alertType);

    /**
     * 根据用户ID和预警类型查询预警规则
     */
    List<AlertRule> findByUserIdAndAlertTypeAndIsActiveTrue(Long userId, AlertType alertType);

    /**
     * 统计用户的预警规则数量
     */
    @Query("SELECT COUNT(r) FROM AlertRule r WHERE r.userId = :userId AND r.isActive = true")
    Long countByUserIdAndIsActive(@Param("userId") Long userId);

    /**
     * 检查是否已存在相同名称的规则
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM AlertRule r WHERE r.userId = :userId AND r.ruleName = :ruleName")
    boolean existsByUserIdAndRuleName(@Param("userId") Long userId, @Param("ruleName") String ruleName);
}