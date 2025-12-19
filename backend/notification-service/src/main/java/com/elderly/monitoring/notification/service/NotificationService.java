package main.java.com.elderly.monitoring.notification.service;

import main.java.com.elderly.monitoring.notification.entity.Notification;
import main.java.com.elderly.monitoring.notification.entity.NotificationSetting;
import main.java.com.elderly.monitoring.notification.repository.NotificationRepository;
import main.java.com.elderly.monitoring.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 通知服务实现类
 * 
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    /**
     * 创建通知
     */
    @Transactional
    public Notification createNotification(Notification notification) {
        log.info("创建通知: userId={}, type={}, title={}", 
                notification.getUserId(), notification.getType(), notification.getTitle());
        
        // 检查用户通知设置
        Optional<NotificationSetting> settingOpt = notificationSettingRepository
                .findEnabledByUserIdAndType(notification.getUserId(), 
                        NotificationSetting.NotificationType.valueOf(notification.getType().name()));
        
        if (settingOpt.isPresent()) {
            NotificationSetting setting = settingOpt.get();
            
            // 检查优先级是否满足要求
            if (!setting.isPrioritySufficient(notification.getPriority())) {
                log.warn("通知优先级不满足用户设置要求: userId={}, notificationPriority={}, minPriority={}", 
                        notification.getUserId(), notification.getPriority(), setting.getMinPriority());
                return null;
            }
            
            // 检查是否在免打扰时间内
            if (setting.isInQuietHours() && !notification.isUrgent()) {
                log.info("通知在免打扰时间内，延迟发送: userId={}", notification.getUserId());
                notification.setStatus(Notification.NotificationStatus.PENDING);
            } else {
                notification.setStatus(Notification.NotificationStatus.PENDING);
            }
        }
        
        return notificationRepository.save(notification);
    }

    /**
     * 批量创建通知
     */
    @Transactional
    public List<Notification> createNotifications(List<Notification> notifications) {
        log.info("批量创建通知: count={}", notifications.size());
        return notificationRepository.saveAll(notifications);
    }

    /**
     * 发送通知
     */
    @Transactional
    public boolean sendNotification(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            log.warn("通知不存在: id={}", notificationId);
            return false;
        }
        
        Notification notification = notificationOpt.get();
        
        try {
            log.info("发送通知: id={}, userId={}, type={}", 
                    notification.getId(), notification.getUserId(), notification.getType());
            
            // 根据发送方式发送通知
            boolean success = false;
            switch (notification.getSendMethod()) {
                case SYSTEM:
                    success = sendSystemNotification(notification);
                    break;
                case EMAIL:
                    success = sendEmailNotification(notification);
                    break;
                case SMS:
                    success = sendSmsNotification(notification);
                    break;
                case PUSH:
                    success = sendPushNotification(notification);
                    break;
                case WECHAT:
                    success = sendWechatNotification(notification);
                    break;
                case VOICE:
                    success = sendVoiceNotification(notification);
                    break;
                default:
                    log.warn("不支持的通知发送方式: {}", notification.getSendMethod());
                    return false;
            }
            
            if (success) {
                notification.markAsSent();
                notificationRepository.save(notification);
                log.info("通知发送成功: id={}", notification.getId());
                return true;
            } else {
                notification.markAsFailed("发送失败");
                notificationRepository.save(notification);
                log.error("通知发送失败: id={}", notification.getId());
                return false;
            }
            
        } catch (Exception e) {
            log.error("发送通知异常: id={}", notificationId, e);
            notification.markAsFailed("发送异常: " + e.getMessage());
            notificationRepository.save(notification);
            return false;
        }
    }

    /**
     * 发送系统内通知
     */
    private boolean sendSystemNotification(Notification notification) {
        // 系统内通知直接标记为已送达
        notification.markAsDelivered();
        log.info("系统内通知已送达: id={}", notification.getId());
        return true;
    }

    /**
     * 发送邮件通知
     */
    private boolean sendEmailNotification(Notification notification) {
        try {
            // TODO: 集成邮件发送服务
            log.info("发送邮件通知: to={}, title={}, content={}", 
                    "user@example.com", notification.getTitle(), notification.getContent());
            
            // 模拟发送成功
            Thread.sleep(100);
            return true;
            
        } catch (Exception e) {
            log.error("发送邮件通知失败: id={}", notification.getId(), e);
            return false;
        }
    }

    /**
     * 发送短信通知
     */
    private boolean sendSmsNotification(Notification notification) {
        try {
            // TODO: 集成短信发送服务
            log.info("发送短信通知: to={}, content={}", 
                    "13800138000", notification.getContent());
            
            // 模拟发送成功
            Thread.sleep(100);
            return true;
            
        } catch (Exception e) {
            log.error("发送短信通知失败: id={}", notification.getId(), e);
            return false;
        }
    }

    /**
     * 发送推送通知
     */
    private boolean sendPushNotification(Notification notification) {
        try {
            // TODO: 集成推送服务
            log.info("发送推送通知: token={}, title={}, content={}", 
                    "push_token", notification.getTitle(), notification.getContent());
            
            // 模拟发送成功
            Thread.sleep(100);
            return true;
            
        } catch (Exception e) {
            log.error("发送推送通知失败: id={}", notification.getId(), e);
            return false;
        }
    }

    /**
     * 发送微信通知
     */
    private boolean sendWechatNotification(Notification notification) {
        try {
            // TODO: 集成微信通知服务
            log.info("发送微信通知: openid={}, title={}, content={}", 
                    "wechat_openid", notification.getTitle(), notification.getContent());
            
            // 模拟发送成功
            Thread.sleep(100);
            return true;
            
        } catch (Exception e) {
            log.error("发送微信通知失败: id={}", notification.getId(), e);
            return false;
        }
    }

    /**
     * 发送语音通知
     */
    private boolean sendVoiceNotification(Notification notification) {
        try {
            // TODO: 集成语音通知服务
            log.info("发送语音通知: phone={}, content={}", 
                    "13800138000", notification.getContent());
            
            // 模拟发送成功
            Thread.sleep(200);
            return true;
            
        } catch (Exception e) {
            log.error("发送语音通知失败: id={}", notification.getId(), e);
            return false;
        }
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public boolean markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            log.warn("通知不存在: id={}", notificationId);
            return false;
        }
        
        Notification notification = notificationOpt.get();
        notification.markAsRead();
        notificationRepository.save(notification);
        
        log.info("通知已标记为已读: id={}", notificationId);
        return true;
    }

    /**
     * 批量标记通知为已读
     */
    @Transactional
    public int markMultipleAsRead(List<Long> notificationIds) {
        int updated = notificationRepository.markAsRead(notificationIds, 
                LocalDateTime.now(), Notification.NotificationStatus.READ);
        log.info("批量标记通知为已读: count={}", updated);
        return updated;
    }

    /**
     * 删除通知
     */
    @Transactional
    public boolean deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)) {
            log.warn("通知不存在: id={}", notificationId);
            return false;
        }
        
        notificationRepository.deleteById(notificationId);
        log.info("通知已删除: id={}", notificationId);
        return true;
    }

    /**
     * 批量删除通知
     */
    @Transactional
    public int deleteNotifications(List<Long> notificationIds) {
        List<Notification> notifications = notificationRepository.findAllById(notificationIds);
        notificationRepository.deleteAll(notifications);
        log.info("批量删除通知: count={}", notifications.size());
        return notifications.size();
    }

    /**
     * 获取用户通知列表
     */
    public Page<Notification> getUserNotifications(Long userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    /**
     * 获取用户未读通知数量
     */
    public Long getUnreadCount(Long userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    /**
     * 获取待发送的通知
     */
    public List<Notification> getPendingNotifications() {
        return notificationRepository.findByStatusOrderByCreatedAtAsc(Notification.NotificationStatus.PENDING);
    }

    /**
     * 获取需要重试的通知
     */
    public List<Notification> getRetryableNotifications() {
        LocalDateTime retryTime = LocalDateTime.now().minusMinutes(5); // 5分钟后重试
        return notificationRepository.findRetryableNotifications(retryTime);
    }

    /**
     * 处理过期通知
     */
    @Transactional
    public int expireNotifications() {
        List<Notification> expiredNotifications = notificationRepository
                .findExpiredNotifications(LocalDateTime.now(), Notification.NotificationStatus.READ);
        
        int updated = 0;
        for (Notification notification : expiredNotifications) {
            notification.setStatus(Notification.NotificationStatus.EXPIRED);
            notificationRepository.save(notification);
            updated++;
        }
        
        log.info("处理过期通知: count={}", updated);
        return updated;
    }

    /**
     * 清理旧的通知
     */
    @Transactional
    public int cleanupOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        int deleted = notificationRepository.deleteOldReadNotifications(thirtyDaysAgo);
        log.info("清理旧通知: count={}", deleted);
        return deleted;
    }

    /**
     * 获取用户通知统计
     */
    public List<Object[]> getUserNotificationStats(Long userId, LocalDateTime startTime, LocalDateTime endTime) {
        return notificationRepository.getUserNotificationStats(userId, startTime, endTime);
    }

    /**
     * 获取系统通知统计
     */
    public List<Object[]> getSystemNotificationStats(LocalDateTime startTime, LocalDateTime endTime) {
        return notificationRepository.getSystemNotificationStats(startTime, endTime);
    }

    /**
     * 根据多个条件查询通知
     */
    public Page<Notification> searchNotifications(Long userId, 
                                                  Notification.NotificationType type,
                                                  Notification.NotificationStatus status,
                                                  Boolean isRead,
                                                  Notification.NotificationPriority priority,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime,
                                                  Pageable pageable) {
        return notificationRepository.findByMultipleConditions(userId, type, status, isRead, 
                priority, startTime, endTime, pageable);
    }

    /**
     * 获取通知详情
     */
    public Optional<Notification> getNotificationById(Long notificationId) {
        return notificationRepository.findById(notificationId);
    }

    /**
     * 更新通知状态
     */
    @Transactional
    public boolean updateNotificationStatus(Long notificationId, Notification.NotificationStatus status) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isEmpty()) {
            return false;
        }
        
        Notification notification = notificationOpt.get();
        notification.setStatus(status);
        notificationRepository.save(notification);
        
        log.info("更新通知状态: id={}, status={}", notificationId, status);
        return true;
    }
}