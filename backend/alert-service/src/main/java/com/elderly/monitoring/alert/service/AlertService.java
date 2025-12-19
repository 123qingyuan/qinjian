package main.java.com.elderly.monitoring.alert.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.com.elderly.monitoring.alert.entity.*;
import main.java.com.elderly.monitoring.alert.repository.AlertRuleRepository;
import main.java.com.elderly.monitoring.alert.repository.AlertRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 预警系统服务实现类
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Service
@Transactional
public class AlertService {

    @Autowired
    private AlertRuleRepository alertRuleRepository;

    @Autowired
    private AlertRecordRepository alertRecordRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 创建预警规则
     */
    public AlertRule createAlertRule(AlertRule alertRule) {
        // 检查规则名称是否已存在
        boolean exists = alertRuleRepository.existsByUserIdAndRuleName(
            alertRule.getUserId(), alertRule.getRuleName());
        if (exists) {
            throw new RuntimeException("预警规则名称已存在");
        }
        
        alertRule.setCreatedAt(LocalDateTime.now());
        return alertRuleRepository.save(alertRule);
    }

    /**
     * 更新预警规则
     */
    public AlertRule updateAlertRule(Long ruleId, AlertRule alertRule) {
        AlertRule existingRule = alertRuleRepository.findById(ruleId)
            .orElseThrow(() -> new RuntimeException("预警规则不存在"));
        
        existingRule.setRuleName(alertRule.getRuleName());
        existingRule.setConditionConfig(alertRule.getConditionConfig());
        existingRule.setAlertLevel(alertRule.getAlertLevel());
        existingRule.setNotificationMethods(alertRule.getNotificationMethods());
        existingRule.setIsActive(alertRule.getIsActive());
        existingRule.setDescription(alertRule.getDescription());
        
        return alertRuleRepository.save(existingRule);
    }

    /**
     * 删除预警规则
     */
    public void deleteAlertRule(Long ruleId) {
        alertRuleRepository.deleteById(ruleId);
    }

    /**
     * 获取用户的预警规则列表
     */
    public List<AlertRule> getUserAlertRules(Long userId) {
        return alertRuleRepository.findByUserIdAndIsActiveTrue(userId);
    }

    /**
     * 获取用户的预警记录
     */
    public List<AlertRecord> getUserAlertRecords(Long userId) {
        return alertRecordRepository.findByUserIdOrderByTriggeredAtDesc(userId);
    }

    /**
     * 处理预警记录
     */
    public AlertRecord resolveAlert(Long recordId, Long resolvedBy, String resolutionNotes) {
        AlertRecord record = alertRecordRepository.findById(recordId)
            .orElseThrow(() -> new RuntimeException("预警记录不存在"));
        
        record.setStatus(AlertStatus.RESOLVED);
        record.setResolvedAt(LocalDateTime.now());
        record.setResolvedBy(resolvedBy);
        record.setResolutionNotes(resolutionNotes);
        
        return alertRecordRepository.save(record);
    }

    /**
     * 检查预警规则并触发预警
     */
    @Scheduled(fixedDelay = 30000) // 每30秒检查一次
    public void checkAlertRules() {
        List<AlertRule> activeRules = alertRuleRepository.findAll();
        
        for (AlertRule rule : activeRules) {
            if (rule.getIsActive()) {
                checkAndTriggerAlert(rule);
            }
        }
    }

    /**
     * 检查单个预警规则
     */
    private void checkAndTriggerAlert(AlertRule rule) {
        try {
            Map<String, Object> conditionConfig = objectMapper.readValue(
                rule.getConditionConfig(), Map.class);
            
            // 模拟数据检查（实际应该从监控服务获取实时数据）
            Double currentValue = generateMockSensorValue(rule.getAlertType());
            Double threshold = (Double) conditionConfig.get("threshold");
            String operator = (String) conditionConfig.get("operator");
            
            boolean shouldTrigger = evaluateCondition(currentValue, threshold, operator);
            
            if (shouldTrigger) {
                triggerAlert(rule, currentValue, threshold);
            }
            
        } catch (JsonProcessingException e) {
            System.err.println("解析预警规则配置失败: " + e.getMessage());
        }
    }

    /**
     * 评估条件
     */
    private boolean evaluateCondition(Double currentValue, Double threshold, String operator) {
        switch (operator) {
            case ">":
                return currentValue > threshold;
            case "<":
                return currentValue < threshold;
            case ">=":
                return currentValue >= threshold;
            case "<=":
                return currentValue <= threshold;
            case "==":
                return currentValue.equals(threshold);
            case "!=":
                return !currentValue.equals(threshold);
            default:
                return false;
        }
    }

    /**
     * 触发预警
     */
    private void triggerAlert(AlertRule rule, Double triggerValue, Double thresholdValue) {
        AlertRecord record = new AlertRecord(
            rule.getId(), 
            rule.getDeviceId(), 
            rule.getUserId(), 
            rule.getAlertType(), 
            rule.getAlertLevel()
        );
        
        record.setTriggerValue(triggerValue);
        record.setThresholdValue(thresholdValue);
        record.setAlertMessage(generateAlertMessage(rule, triggerValue, thresholdValue));
        record.setLocationAddress("模拟位置");
        record.setLatitude(39.9042 + Math.random() * 0.1);
        record.setLongitude(116.4074 + Math.random() * 0.1);
        
        alertRecordRepository.save(record);
        
        // 发送通知
        sendNotifications(rule, record);
    }

    /**
     * 生成预警消息
     */
    private String generateAlertMessage(AlertRule rule, Double triggerValue, Double thresholdValue) {
        return String.format("%s预警: %s的值%.2f%s了阈值%.2f", 
            rule.getAlertType().getDescription(),
            rule.getDeviceId(),
            triggerValue,
            "超过",
            thresholdValue);
    }

    /**
     * 发送通知
     */
    private void sendNotifications(AlertRule rule, AlertRecord record) {
        if (rule.getNotificationMethods() != null) {
            for (NotificationMethod method : rule.getNotificationMethods()) {
                switch (method) {
                    case POPUP:
                        // 发送弹窗通知
                        System.out.println("发送弹窗通知: " + record.getAlertMessage());
                        break;
                    case SMS:
                        // 发送短信通知
                        System.out.println("发送短信通知: " + record.getAlertMessage());
                        break;
                    case EMAIL:
                        // 发送邮件通知
                        System.out.println("发送邮件通知: " + record.getAlertMessage());
                        break;
                    case APP_PUSH:
                        // 发送APP推送
                        System.out.println("发送APP推送: " + record.getAlertMessage());
                        break;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * 生成模拟传感器数值
     */
    private Double generateMockSensorValue(AlertType alertType) {
        Random random = new Random();
        switch (alertType) {
            case TEMPERATURE:
                return 20.0 + random.nextDouble() * 15.0; // 20-35度
            case HUMIDITY:
                return 40.0 + random.nextDouble() * 40.0; // 40-80%
            case HEART_RATE:
                return 60.0 + random.nextDouble() * 40.0; // 60-100 bpm
            case BLOOD_PRESSURE:
                return 90.0 + random.nextDouble() * 40.0; // 90-130 mmHg
            default:
                return random.nextDouble() * 100.0;
        }
    }

    /**
     * 获取预警统计信息
     */
    public Map<String, Object> getAlertStatistics(Long userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(30); // 最近30天
        
        Long pendingCount = alertRecordRepository.countByUserIdAndStatus(userId, AlertStatus.PENDING);
        Long todayCount = alertRecordRepository.countTodayAlerts(userId);
        List<Object[]> typeStats = alertRecordRepository.countByAlertTypeSince(userId, since);
        
        return Map.of(
            "pendingCount", pendingCount,
            "todayCount", todayCount,
            "typeStatistics", typeStats
        );
    }
}