package main.java.com.elderly.monitoring.notification.service;

import main.java.com.elderly.monitoring.notification.entity.NotificationSetting;
import main.java.com.elderly.monitoring.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 通知设置服务实现类
 * 
 * @author System
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;

    /**
     * 获取用户通知设置
     */
    public Optional<NotificationSetting> getUserSetting(Long userId, NotificationSetting.NotificationType type) {
        return notificationSettingRepository.findByUserIdAndType(userId, type);
    }

    /**
     * 获取用户所有通知设置
     */
    public List<NotificationSetting> getUserSettings(Long userId) {
        return notificationSettingRepository.findByUserId(userId);
    }

    /**
     * 创建或更新用户通知设置
     */
    @Transactional
    public NotificationSetting saveUserSetting(NotificationSetting setting) {
        log.info("保存用户通知设置: userId={}, type={}, enabled={}", 
                setting.getUserId(), setting.getType(), setting.getEnabled());
        
        Optional<NotificationSetting> existingOpt = notificationSettingRepository
                .findByUserIdAndType(setting.getUserId(), setting.getType());
        
        if (existingOpt.isPresent()) {
            NotificationSetting existing = existingOpt.get();
            // 更新现有设置
            existing.setEnabled(setting.getEnabled());
            existing.setSystemEnabled(setting.getSystemEnabled());
            existing.setEmailEnabled(setting.getEmailEnabled());
            existing.setSmsEnabled(setting.getSmsEnabled());
            existing.setPushEnabled(setting.getPushEnabled());
            existing.setWechatEnabled(setting.getWechatEnabled());
            existing.setVoiceEnabled(setting.getVoiceEnabled());
            existing.setEmailAddress(setting.getEmailAddress());
            existing.setPhoneNumber(setting.getPhoneNumber());
            existing.setWechatOpenid(setting.getWechatOpenid());
            existing.setPushToken(setting.getPushToken());
            existing.setQuietHoursStart(setting.getQuietHoursStart());
            existing.setQuietHoursEnd(setting.getQuietHoursEnd());
            existing.setWeekendQuiet(setting.getWeekendQuiet());
            existing.setMinPriority(setting.getMinPriority());
            existing.setBatchInterval(setting.getBatchInterval());
            existing.setMaxDailyNotifications(setting.getMaxDailyNotifications());
            
            return notificationSettingRepository.save(existing);
        } else {
            // 创建新设置
            return notificationSettingRepository.save(setting);
        }
    }

    /**
     * 初始化用户默认通知设置
     */
    @Transactional
    public List<NotificationSetting> initUserDefaultSettings(Long userId) {
        log.info("初始化用户默认通知设置: userId={}", userId);
        
        NotificationSetting.NotificationType[] types = NotificationSetting.NotificationType.values();
        List<NotificationSetting> settings = new java.util.ArrayList<>();
        
        for (NotificationSetting.NotificationType type : types) {
            Optional<NotificationSetting> existingOpt = notificationSettingRepository
                    .findByUserIdAndType(userId, type);
            
            if (existingOpt.isEmpty()) {
                NotificationSetting setting = new NotificationSetting();
                setting.setUserId(userId);
                setting.setType(type);
                setting.setEnabled(true);
                setting.setSystemEnabled(true);
                setting.setEmailEnabled(false);
                setting.setSmsEnabled(false);
                setting.setPushEnabled(false);
                setting.setWechatEnabled(false);
                setting.setVoiceEnabled(false);
                setting.setMinPriority(NotificationSetting.NotificationPriority.NORMAL);
                setting.setBatchInterval(0);
                setting.setMaxDailyNotifications(50);
                
                settings.add(setting);
            }
        }
        
        if (!settings.isEmpty()) {
            return notificationSettingRepository.saveAll(settings);
        }
        
        return java.util.Collections.emptyList();
    }

    /**
     * 批量更新用户通知设置
     */
    @Transactional
    public List<NotificationSetting> batchUpdateUserSettings(Long userId, List<NotificationSetting> settings) {
        log.info("批量更新用户通知设置: userId={}, count={}", userId, settings.size());
        
        List<NotificationSetting> updatedSettings = new java.util.ArrayList<>();
        
        for (NotificationSetting setting : settings) {
            setting.setUserId(userId);
            updatedSettings.add(saveUserSetting(setting));
        }
        
        return updatedSettings;
    }

    /**
     * 启用/禁用通知类型
     */
    @Transactional
    public boolean toggleNotificationType(Long userId, NotificationSetting.NotificationType type, boolean enabled) {
        Optional<NotificationSetting> settingOpt = notificationSettingRepository
                .findByUserIdAndType(userId, type);
        
        if (settingOpt.isEmpty()) {
            log.warn("通知设置不存在: userId={}, type={}", userId, type);
            return false;
        }
        
        NotificationSetting setting = settingOpt.get();
        setting.setEnabled(enabled);
        notificationSettingRepository.save(setting);
        
        log.info("更新通知类型状态: userId={}, type={}, enabled={}", userId, type, enabled);
        return true;
    }

    /**
     * 更新通知联系方式
     */
    @Transactional
    public boolean updateContactInfo(Long userId, String emailAddress, String phoneNumber, 
                                   String wechatOpenid, String pushToken) {
        List<NotificationSetting> settings = notificationSettingRepository.findByUserId(userId);
        
        for (NotificationSetting setting : settings) {
            boolean updated = false;
            
            if (emailAddress != null && !emailAddress.equals(setting.getEmailAddress())) {
                setting.setEmailAddress(emailAddress);
                updated = true;
            }
            
            if (phoneNumber != null && !phoneNumber.equals(setting.getPhoneNumber())) {
                setting.setPhoneNumber(phoneNumber);
                updated = true;
            }
            
            if (wechatOpenid != null && !wechatOpenid.equals(setting.getWechatOpenid())) {
                setting.setWechatOpenid(wechatOpenid);
                updated = true;
            }
            
            if (pushToken != null && !pushToken.equals(setting.getPushToken())) {
                setting.setPushToken(pushToken);
                updated = true;
            }
            
            if (updated) {
                notificationSettingRepository.save(setting);
            }
        }
        
        log.info("更新用户联系方式: userId={}", userId);
        return true;
    }

    /**
     * 更新免打扰设置
     */
    @Transactional
    public boolean updateQuietHours(Long userId, String quietHoursStart, String quietHoursEnd, 
                                   boolean weekendQuiet) {
        List<NotificationSetting> settings = notificationSettingRepository.findByUserId(userId);
        
        for (NotificationSetting setting : settings) {
            setting.setQuietHoursStart(quietHoursStart);
            setting.setQuietHoursEnd(quietHoursEnd);
            setting.setWeekendQuiet(weekendQuiet);
            notificationSettingRepository.save(setting);
        }
        
        log.info("更新免打扰设置: userId={}, start={}, end={}, weekend={}", 
                userId, quietHoursStart, quietHoursEnd, weekendQuiet);
        return true;
    }

    /**
     * 检查用户是否启用了特定类型的通知
     */
    public boolean isNotificationEnabled(Long userId, NotificationSetting.NotificationType type) {
        return notificationSettingRepository.isSystemNotificationEnabled(userId, type);
    }

    /**
     * 检查用户是否启用了邮件通知
     */
    public boolean isEmailNotificationEnabled(Long userId, NotificationSetting.NotificationType type) {
        return notificationSettingRepository.isEmailNotificationEnabled(userId, type);
    }

    /**
     * 检查用户是否启用了短信通知
     */
    public boolean isSmsNotificationEnabled(Long userId, NotificationSetting.NotificationType type) {
        return notificationSettingRepository.isSmsNotificationEnabled(userId, type);
    }

    /**
     * 检查用户是否启用了推送通知
     */
    public boolean isPushNotificationEnabled(Long userId, NotificationSetting.NotificationType type) {
        return notificationSettingRepository.isPushNotificationEnabled(userId, type);
    }

    /**
     * 获取用户启用的通知方式
     */
    public List<main.java.com.elderly.monitoring.notification.entity.Notification.SendMethod> 
            getEnabledNotificationMethods(Long userId, NotificationSetting.NotificationType type) {
        
        Optional<NotificationSetting> settingOpt = notificationSettingRepository
                .findEnabledByUserIdAndType(userId, type);
        
        if (settingOpt.isPresent()) {
            return settingOpt.get().getEnabledMethods();
        }
        
        // 返回默认的系统内通知
        return java.util.Arrays.asList(main.java.com.elderly.monitoring.notification.entity.Notification.SendMethod.SYSTEM);
    }

    /**
     * 获取所有启用邮件通知的设置
     */
    public List<NotificationSetting> getEmailEnabledSettings() {
        return notificationSettingRepository.findEmailEnabledSettings();
    }

    /**
     * 获取所有启用短信通知的设置
     */
    public List<NotificationSetting> getSmsEnabledSettings() {
        return notificationSettingRepository.findSmsEnabledSettings();
    }

    /**
     * 获取所有启用推送通知的设置
     */
    public List<NotificationSetting> getPushEnabledSettings() {
        return notificationSettingRepository.findPushEnabledSettings();
    }

    /**
     * 获取所有启用微信通知的设置
     */
    public List<NotificationSetting> getWechatEnabledSettings() {
        return notificationSettingRepository.findWechatEnabledSettings();
    }

    /**
     * 获取所有启用语音通知的设置
     */
    public List<NotificationSetting> getVoiceEnabledSettings() {
        return notificationSettingRepository.findVoiceEnabledSettings();
    }

    /**
     * 删除用户通知设置
     */
    @Transactional
    public boolean deleteUserSetting(Long userId, NotificationSetting.NotificationType type) {
        Optional<NotificationSetting> settingOpt = notificationSettingRepository
                .findByUserIdAndType(userId, type);
        
        if (settingOpt.isEmpty()) {
            return false;
        }
        
        notificationSettingRepository.delete(settingOpt.get());
        log.info("删除用户通知设置: userId={}, type={}", userId, type);
        return true;
    }

    /**
     * 删除用户所有通知设置
     */
    @Transactional
    public int deleteUserSettings(Long userId) {
        List<NotificationSetting> settings = notificationSettingRepository.findByUserId(userId);
        notificationSettingRepository.deleteAll(settings);
        log.info("删除用户所有通知设置: userId={}, count={}", userId, settings.size());
        return settings.size();
    }

    /**
     * 获取通知设置统计
     */
    public NotificationSettingStats getNotificationSettingStats() {
        long totalSettings = notificationSettingRepository.count();
        long enabledSettings = notificationSettingRepository.count().longValue(); // TODO: 需要实现countEnabled方法
        
        return NotificationSettingStats.builder()
                .totalSettings(totalSettings)
                .enabledSettings(enabledSettings)
                .build();
    }

    /**
     * 通知设置统计信息
     */
    @lombok.Builder
    @lombok.Data
    public static class NotificationSettingStats {
        private Long totalSettings;
        private Long enabledSettings;
        private Long emailEnabledSettings;
        private Long smsEnabledSettings;
        private Long pushEnabledSettings;
        private Long wechatEnabledSettings;
        private Long voiceEnabledSettings;
    }

    /**
     * 重置用户通知设置为默认值
     */
    @Transactional
    public List<NotificationSetting> resetUserSettingsToDefault(Long userId) {
        log.info("重置用户通知设置为默认值: userId={}", userId);
        
        // 删除现有设置
        deleteUserSettings(userId);
        
        // 创建默认设置
        return initUserDefaultSettings(userId);
    }

    /**
     * 复制通知设置
     */
    @Transactional
    public boolean copyUserSettings(Long fromUserId, Long toUserId) {
        log.info("复制用户通知设置: from={}, to={}", fromUserId, toUserId);
        
        List<NotificationSetting> fromSettings = notificationSettingRepository.findByUserId(fromUserId);
        List<NotificationSetting> toSettings = new java.util.ArrayList<>();
        
        for (NotificationSetting setting : fromSettings) {
            NotificationSetting newSetting = new NotificationSetting();
            newSetting.setUserId(toUserId);
            newSetting.setType(setting.getType());
            newSetting.setEnabled(setting.getEnabled());
            newSetting.setSystemEnabled(setting.getSystemEnabled());
            newSetting.setEmailEnabled(setting.getEmailEnabled());
            newSetting.setSmsEnabled(setting.getSmsEnabled());
            newSetting.setPushEnabled(setting.getPushEnabled());
            newSetting.setWechatEnabled(setting.getWechatEnabled());
            newSetting.setVoiceEnabled(setting.getVoiceEnabled());
            newSetting.setEmailAddress(setting.getEmailAddress());
            newSetting.setPhoneNumber(setting.getPhoneNumber());
            newSetting.setWechatOpenid(setting.getWechatOpenid());
            newSetting.setPushToken(setting.getPushToken());
            newSetting.setQuietHoursStart(setting.getQuietHoursStart());
            newSetting.setQuietHoursEnd(setting.getQuietHoursEnd());
            newSetting.setWeekendQuiet(setting.getWeekendQuiet());
            newSetting.setMinPriority(setting.getMinPriority());
            newSetting.setBatchInterval(setting.getBatchInterval());
            newSetting.setMaxDailyNotifications(setting.getMaxDailyNotifications());
            
            toSettings.add(newSetting);
        }
        
        if (!toSettings.isEmpty()) {
            notificationSettingRepository.saveAll(toSettings);
        }
        
        return true;
    }
}