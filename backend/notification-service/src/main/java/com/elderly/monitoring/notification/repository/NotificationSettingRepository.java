package main.java.com.elderly.monitoring.notification.repository;

import main.java.com.elderly.monitoring.notification.entity.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 通知设置数据访问接口
 * 
 * @author System
 * @since 1.0.0
 */
@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    /**
     * 根据用户ID和通知类型查询设置
     */
    Optional<NotificationSetting> findByUserIdAndType(Long userId, NotificationSetting.NotificationType type);

    /**
     * 根据用户ID查询所有设置
     */
    List<NotificationSetting> findByUserId(Long userId);

    /**
     * 根据用户ID查询启用的设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.enabled = true")
    List<NotificationSetting> findEnabledByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和通知类型查询启用的设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.type = :type AND ns.enabled = true")
    Optional<NotificationSetting> findEnabledByUserIdAndType(@Param("userId") Long userId, 
                                                             @Param("type") NotificationSetting.NotificationType type);

    /**
     * 根据用户ID和邮件地址查询设置
     */
    List<NotificationSetting> findByUserIdAndEmailAddressNotNull(Long userId);

    /**
     * 根据用户ID和手机号码查询设置
     */
    List<NotificationSetting> findByUserIdAndPhoneNumberNotNull(Long userId);

    /**
     * 根据用户ID和微信OpenID查询设置
     */
    List<NotificationSetting> findByUserIdAndWechatOpenidNotNull(Long userId);

    /**
     * 根据用户ID和推送Token查询设置
     */
    List<NotificationSetting> findByUserIdAndPushTokenNotNull(Long userId);

    /**
     * 查询启用邮件通知的设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.emailEnabled = true AND ns.emailAddress IS NOT NULL")
    List<NotificationSetting> findEmailEnabledSettings();

    /**
     * 查询启用短信通知的设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.smsEnabled = true AND ns.phoneNumber IS NOT NULL")
    List<NotificationSetting> findSmsEnabledSettings();

    /**
     * 查询启用推送通知的设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.pushEnabled = true AND ns.pushToken IS NOT NULL")
    List<NotificationSetting> findPushEnabledSettings();

    /**
     * 查询启用微信通知的设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.wechatEnabled = true AND ns.wechatOpenid IS NOT NULL")
    List<NotificationSetting> findWechatEnabledSettings();

    /**
     * 查询启用语音通知的设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.voiceEnabled = true AND ns.phoneNumber IS NOT NULL")
    List<NotificationSetting> findVoiceEnabledSettings();

    /**
     * 根据通知类型查询启用的设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.type = :type AND ns.enabled = true")
    List<NotificationSetting> findEnabledByType(@Param("type") NotificationSetting.NotificationType type);

    /**
     * 根据用户ID和最小优先级查询设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.enabled = true AND ns.minPriority <= :priority")
    List<NotificationSetting> findByUserIdAndMinPriority(@Param("userId") Long userId, 
                                                        @Param("priority") NotificationSetting.NotificationPriority priority);

    /**
     * 检查用户是否对特定类型的通知启用系统内通知
     */
    @Query("SELECT COUNT(ns) > 0 FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.type = :type AND ns.enabled = true AND ns.systemEnabled = true")
    boolean isSystemNotificationEnabled(@Param("userId") Long userId, @Param("type") NotificationSetting.NotificationType type);

    /**
     * 检查用户是否对特定类型的通知启用邮件通知
     */
    @Query("SELECT COUNT(ns) > 0 FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.type = :type AND ns.enabled = true AND ns.emailEnabled = true AND ns.emailAddress IS NOT NULL")
    boolean isEmailNotificationEnabled(@Param("userId") Long userId, @Param("type") NotificationSetting.NotificationType type);

    /**
     * 检查用户是否对特定类型的通知启用短信通知
     */
    @Query("SELECT COUNT(ns) > 0 FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.type = :type AND ns.enabled = true AND ns.smsEnabled = true AND ns.phoneNumber IS NOT NULL")
    boolean isSmsNotificationEnabled(@Param("userId") Long userId, @Param("type") NotificationSetting.NotificationType type);

    /**
     * 检查用户是否对特定类型的通知启用推送通知
     */
    @Query("SELECT COUNT(ns) > 0 FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.type = :type AND ns.enabled = true AND ns.pushEnabled = true AND ns.pushToken IS NOT NULL")
    boolean isPushNotificationEnabled(@Param("userId") Long userId, @Param("type") NotificationSetting.NotificationType type);

    /**
     * 查询用户的默认通知设置（如果特定类型没有设置）
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.type = 'REMINDER' AND ns.enabled = true")
    Optional<NotificationSetting> findDefaultSettings(@Param("userId") Long userId);

    /**
     * 根据多个条件查询设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE " +
           "(:userId IS NULL OR ns.userId = :userId) AND " +
           "(:type IS NULL OR ns.type = :type) AND " +
           "(:enabled IS NULL OR ns.enabled = :enabled) AND " +
           "(:systemEnabled IS NULL OR ns.systemEnabled = :systemEnabled) AND " +
           "(:emailEnabled IS NULL OR ns.emailEnabled = :emailEnabled) AND " +
           "(:smsEnabled IS NULL OR ns.smsEnabled = :smsEnabled) AND " +
           "(:pushEnabled IS NULL OR ns.pushEnabled = :pushEnabled)")
    List<NotificationSetting> findByMultipleConditions(@Param("userId") Long userId,
                                                        @Param("type") NotificationSetting.NotificationType type,
                                                        @Param("enabled") Boolean enabled,
                                                        @Param("systemEnabled") Boolean systemEnabled,
                                                        @Param("emailEnabled") Boolean emailEnabled,
                                                        @Param("smsEnabled") Boolean smsEnabled,
                                                        @Param("pushEnabled") Boolean pushEnabled);

    /**
     * 统计用户的通知设置数量
     */
    @Query("SELECT COUNT(ns) FROM NotificationSetting ns WHERE ns.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);

    /**
     * 统计启用的通知设置数量
     */
    @Query("SELECT COUNT(ns) FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.enabled = true")
    Long countEnabledByUserId(@Param("userId") Long userId);

    /**
     * 查询需要批量处理的通知设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.enabled = true AND ns.batchInterval > 0")
    List<NotificationSetting> findBatchEnabledSettings();

    /**
     * 查询设置了免打扰时间的用户设置
     */
    @Query("SELECT ns FROM NotificationSetting ns WHERE ns.userId = :userId AND ns.quietHoursStart IS NOT NULL AND ns.quietHoursEnd IS NOT NULL")
    List<NotificationSetting> findQuietHoursSettingsByUserId(@Param("userId") Long userId);
}