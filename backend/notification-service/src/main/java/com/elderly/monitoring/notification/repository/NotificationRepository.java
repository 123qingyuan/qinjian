package main.java.com.elderly.monitoring.notification.repository;

import main.java.com.elderly.monitoring.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知数据访问接口
 * 
 * @author System
 * @since 1.0.0
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * 根据用户ID分页查询通知
     */
    Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 根据用户ID和状态分页查询通知
     */
    Page<Notification> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, Notification.NotificationStatus status, Pageable pageable);

    /**
     * 根据用户ID和类型分页查询通知
     */
    Page<Notification> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, Notification.NotificationType type, Pageable pageable);

    /**
     * 根据用户ID和是否已读分页查询通知
     */
    Page<Notification> findByUserIdAndIsReadOrderByCreatedAtDesc(Long userId, Boolean isRead, Pageable pageable);

    /**
     * 根据用户ID查询未读通知数量
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    Long countUnreadByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和状态查询通知数量
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.status = :status")
    Long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Notification.NotificationStatus status);

    /**
     * 根据状态查询通知列表
     */
    List<Notification> findByStatusOrderByCreatedAtAsc(Notification.NotificationStatus status);

    /**
     * 根据状态和重试次数查询通知列表
     */
    @Query("SELECT n FROM Notification n WHERE n.status = :status AND n.retryCount < n.maxRetryCount ORDER BY n.createdAt ASC")
    List<Notification> findByStatusAndRetryable(@Param("status") Notification.NotificationStatus status);

    /**
     * 查询过期的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.expireAt IS NOT NULL AND n.expireAt < :now AND n.status != :status")
    List<Notification> findExpiredNotifications(@Param("now") LocalDateTime now, @Param("status") Notification.NotificationStatus status);

    /**
     * 根据优先级查询通知
     */
    List<Notification> findByPriorityAndStatusOrderByCreatedAtAsc(Notification.NotificationPriority priority, Notification.NotificationStatus status);

    /**
     * 根据关联信息查询通知
     */
    List<Notification> findByRelatedIdAndRelatedTypeOrderByCreatedAtDesc(String relatedId, String relatedType);

    /**
     * 根据用户ID和时间范围查询通知
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.createdAt BETWEEN :startTime AND :endTime ORDER BY n.createdAt DESC")
    List<Notification> findByUserIdAndTimeRange(@Param("userId") Long userId, 
                                                @Param("startTime") LocalDateTime startTime, 
                                                @Param("endTime") LocalDateTime endTime);

    /**
     * 批量标记通知为已读
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt, n.status = :status WHERE n.id IN :ids")
    int markAsRead(@Param("ids") List<Long> ids, @Param("readAt") LocalDateTime readAt, @Param("status") Notification.NotificationStatus status);

    /**
     * 批量更新通知状态
     */
    @Modifying
    @Query("UPDATE Notification n SET n.status = :status WHERE n.id IN :ids")
    int updateStatus(@Param("ids") List<Long> ids, @Param("status") Notification.NotificationStatus status);

    /**
     * 根据发送方式查询待发送的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.sendMethod = :sendMethod AND n.status IN :statuses ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findBySendMethodAndStatusIn(@Param("sendMethod") Notification.SendMethod sendMethod, 
                                                   @Param("statuses") List<Notification.NotificationStatus> statuses);

    /**
     * 查询用户在指定时间范围内的通知统计
     */
    @Query("SELECT n.type, COUNT(n), SUM(CASE WHEN n.isRead = true THEN 1 ELSE 0 END) " +
           "FROM Notification n WHERE n.userId = :userId AND n.createdAt BETWEEN :startTime AND :endTime " +
           "GROUP BY n.type")
    List<Object[]> getUserNotificationStats(@Param("userId") Long userId, 
                                           @Param("startTime") LocalDateTime startTime, 
                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 查询系统通知统计
     */
    @Query("SELECT n.status, COUNT(n) FROM Notification n WHERE n.createdAt BETWEEN :startTime AND :endTime GROUP BY n.status")
    List<Object[]> getSystemNotificationStats(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 查询需要重试的通知
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'FAILED' AND n.retryCount < n.maxRetryCount AND n.updatedAt < :retryTime ORDER BY n.createdAt ASC")
    List<Notification> findRetryableNotifications(@Param("retryTime") LocalDateTime retryTime);

    /**
     * 根据优先级和创建时间查询待发送通知
     */
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.sendMethod = :sendMethod ORDER BY n.priority DESC, n.createdAt ASC")
    List<Notification> findPendingNotificationsByMethod(@Param("sendMethod") Notification.SendMethod sendMethod);

    /**
     * 查询用户最近的未读紧急通知
     */
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.priority = 'URGENT' ORDER BY n.createdAt DESC")
    List<Notification> findUrgentUnreadByUserId(@Param("userId") Long userId);

    /**
     * 删除指定时间之前的已读通知
     */
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.isRead = true AND n.readAt < :beforeTime")
    int deleteOldReadNotifications(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查询用户每日通知数量统计
     */
    @Query("SELECT DATE(n.createdAt), COUNT(n) FROM Notification n WHERE n.userId = :userId AND n.createdAt >= :startDate GROUP BY DATE(n.createdAt) ORDER BY DATE(n.createdAt)")
    List<Object[]> getDailyNotificationCount(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);

    /**
     * 根据多个条件查询通知
     */
    @Query("SELECT n FROM Notification n WHERE " +
           "(:userId IS NULL OR n.userId = :userId) AND " +
           "(:type IS NULL OR n.type = :type) AND " +
           "(:status IS NULL OR n.status = :status) AND " +
           "(:isRead IS NULL OR n.isRead = :isRead) AND " +
           "(:priority IS NULL OR n.priority = :priority) AND " +
           "n.createdAt BETWEEN :startTime AND :endTime " +
           "ORDER BY n.createdAt DESC")
    Page<Notification> findByMultipleConditions(@Param("userId") Long userId,
                                                @Param("type") Notification.NotificationType type,
                                                @Param("status") Notification.NotificationStatus status,
                                                @Param("isRead") Boolean isRead,
                                                @Param("priority") Notification.NotificationPriority priority,
                                                @Param("startTime") LocalDateTime startTime,
                                                @Param("endTime") LocalDateTime endTime,
                                                Pageable pageable);
}