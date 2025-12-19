package main.java.com.elderly.monitoring.notification.controller;

import main.java.com.elderly.monitoring.notification.entity.Notification;
import main.java.com.elderly.monitoring.notification.entity.NotificationSetting;
import main.java.com.elderly.monitoring.notification.service.NotificationService;
import main.java.com.elderly.monitoring.notification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 通知控制器
 * 
 * @author System
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSettingService notificationSettingService;

    /**
     * 获取通知列表
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getNotifications(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Notification.NotificationType type,
            @RequestParam(required = false) Notification.NotificationStatus status,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) Notification.NotificationPriority priority,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : null;
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : null;
            
            Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            
            Page<Notification> notifications = notificationService.searchNotifications(
                    userId, type, status, isRead, priority, start, end, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", notifications);
            response.put("message", "获取通知列表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取通知列表失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取通知列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取通知详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNotificationById(@PathVariable Long id) {
        try {
            Optional<Notification> notificationOpt = notificationService.getNotificationById(id);
            
            if (notificationOpt.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "通知不存在");
                return ResponseEntity.notFound().build();
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", notificationOpt.get());
            response.put("message", "获取通知详情成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取通知详情失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取通知详情失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 创建通知
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Notification notification) {
        try {
            Notification createdNotification = notificationService.createNotification(notification);
            
            if (createdNotification == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "创建通知失败，可能是不满足用户通知设置要求");
                return ResponseEntity.badRequest().body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdNotification);
            response.put("message", "创建通知成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("创建通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "创建通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量创建通知
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> createNotifications(@RequestBody List<Notification> notifications) {
        try {
            List<Notification> createdNotifications = notificationService.createNotifications(notifications);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createdNotifications);
            response.put("message", "批量创建通知成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量创建通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量创建通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 发送通知
     */
    @PostMapping("/{id}/send")
    public ResponseEntity<Map<String, Object>> sendNotification(@PathVariable Long id) {
        try {
            boolean success = notificationService.sendNotification(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "通知发送成功" : "通知发送失败");
            
            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("发送通知失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "发送通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 标记通知为已读
     */
    @PatchMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        try {
            boolean success = notificationService.markAsRead(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "标记已读成功" : "标记已读失败");
            
            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("标记通知为已读失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "标记已读失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量标记通知为已读
     */
    @PatchMapping("/mark-read")
    public ResponseEntity<Map<String, Object>> markMultipleAsRead(@RequestBody List<Long> ids) {
        try {
            int updated = notificationService.markMultipleAsRead(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updated);
            response.put("message", "批量标记已读成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量标记通知为已读失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量标记已读失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id) {
        try {
            boolean success = notificationService.deleteNotification(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除通知成功" : "删除通知失败");
            
            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("删除通知失败: id={}", id, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "删除通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量删除通知
     */
    @DeleteMapping("/batch")
    public ResponseEntity<Map<String, Object>> deleteNotifications(@RequestBody List<Long> ids) {
        try {
            int deleted = notificationService.deleteNotifications(ids);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", deleted);
            response.put("message", "批量删除通知成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量删除通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量删除通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户未读通知数量
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(@RequestParam Long userId) {
        try {
            Long count = notificationService.getUnreadCount(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", count);
            response.put("message", "获取未读通知数量成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取未读通知数量失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取未读通知数量失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取用户通知列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserNotifications(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Notification> notifications = notificationService.getUserNotifications(userId, pageable);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", notifications);
            response.put("message", "获取用户通知列表成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户通知列表失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户通知列表失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 获取通知统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getNotificationStatistics(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            LocalDateTime start = startTime != null ? LocalDateTime.parse(startTime) : 
                    LocalDateTime.now().minusDays(30);
            LocalDateTime end = endTime != null ? LocalDateTime.parse(endTime) : LocalDateTime.now();
            
            List<Object[]> userStats = null;
            List<Object[]> systemStats = null;
            
            if (userId != null) {
                userStats = notificationService.getUserNotificationStats(userId, start, end);
            } else {
                systemStats = notificationService.getSystemNotificationStats(start, end);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", new HashMap<String, Object>() {{
                put("userStats", userStats);
                put("systemStats", systemStats);
                put("startTime", start);
                put("endTime", end);
            }});
            response.put("message", "获取通知统计成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取通知统计失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取通知统计失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 处理过期通知
     */
    @PostMapping("/expire")
    public ResponseEntity<Map<String, Object>> expireNotifications() {
        try {
            int count = notificationService.expireNotifications();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", count);
            response.put("message", "处理过期通知成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("处理过期通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "处理过期通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 清理旧通知
     */
    @PostMapping("/cleanup")
    public ResponseEntity<Map<String, Object>> cleanupOldNotifications() {
        try {
            int count = notificationService.cleanupOldNotifications();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", count);
            response.put("message", "清理旧通知成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("清理旧通知失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "清理旧通知失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // ==================== 通知设置相关接口 ====================

    /**
     * 获取用户通知设置
     */
    @GetMapping("/settings/{userId}")
    public ResponseEntity<Map<String, Object>> getUserNotificationSettings(@PathVariable Long userId) {
        try {
            List<NotificationSetting> settings = notificationSettingService.getUserSettings(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", settings);
            response.put("message", "获取用户通知设置成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("获取用户通知设置失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "获取用户通知设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 保存用户通知设置
     */
    @PostMapping("/settings")
    public ResponseEntity<Map<String, Object>> saveUserNotificationSetting(@RequestBody NotificationSetting setting) {
        try {
            NotificationSetting savedSetting = notificationSettingService.saveUserSetting(setting);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedSetting);
            response.put("message", "保存通知设置成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("保存用户通知设置失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "保存通知设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 批量保存用户通知设置
     */
    @PostMapping("/settings/batch")
    public ResponseEntity<Map<String, Object>> batchSaveUserNotificationSettings(
            @RequestParam Long userId, @RequestBody List<NotificationSetting> settings) {
        try {
            List<NotificationSetting> savedSettings = notificationSettingService
                    .batchUpdateUserSettings(userId, settings);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", savedSettings);
            response.put("message", "批量保存通知设置成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("批量保存用户通知设置失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "批量保存通知设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 初始化用户默认通知设置
     */
    @PostMapping("/settings/{userId}/init")
    public ResponseEntity<Map<String, Object>> initUserDefaultSettings(@PathVariable Long userId) {
        try {
            List<NotificationSetting> settings = notificationSettingService.initUserDefaultSettings(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", settings);
            response.put("message", "初始化默认通知设置成功");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("初始化用户默认通知设置失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "初始化默认通知设置失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 更新联系方式
     */
    @PostMapping("/settings/{userId}/contact")
    public ResponseEntity<Map<String, Object>> updateContactInfo(
            @PathVariable Long userId,
            @RequestBody Map<String, String> contactInfo) {
        try {
            String emailAddress = contactInfo.get("emailAddress");
            String phoneNumber = contactInfo.get("phoneNumber");
            String wechatOpenid = contactInfo.get("wechatOpenid");
            String pushToken = contactInfo.get("pushToken");
            
            boolean success = notificationSettingService.updateContactInfo(
                    userId, emailAddress, phoneNumber, wechatOpenid, pushToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "更新联系方式成功" : "更新联系方式失败");
            
            return success ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("更新联系方式失败: userId={}", userId, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "更新联系方式失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 测试通知发送
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> testNotification(@RequestBody Map<String, Object> testParams) {
        try {
            Long userId = Long.valueOf(testParams.get("userId").toString());
            String title = testParams.get("title").toString();
            String content = testParams.get("content").toString();
            Notification.SendMethod sendMethod = Notification.SendMethod.valueOf(
                    testParams.get("sendMethod").toString());
            
            // 创建测试通知
            Notification testNotification = new Notification();
            testNotification.setUserId(userId);
            testNotification.setType(Notification.NotificationType.SYSTEM_ALERT);
            testNotification.setTitle(title);
            testNotification.setContent(content);
            testNotification.setPriority(Notification.NotificationPriority.NORMAL);
            testNotification.setSendMethod(sendMethod);
            
            Notification createdNotification = notificationService.createNotification(testNotification);
            
            if (createdNotification == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "创建测试通知失败");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 发送通知
            boolean sendSuccess = notificationService.sendNotification(createdNotification.getId());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", sendSuccess);
            response.put("data", createdNotification);
            response.put("message", sendSuccess ? "测试通知发送成功" : "测试通知发送失败");
            
            return sendSuccess ? ResponseEntity.ok(response) : ResponseEntity.badRequest().body(response);
            
        } catch (Exception e) {
            log.error("测试通知发送失败", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "测试通知发送失败: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}