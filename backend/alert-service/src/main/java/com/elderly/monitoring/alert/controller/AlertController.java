package main.java.com.elderly.monitoring.alert.controller;

import main.java.com.elderly.monitoring.alert.entity.AlertRecord;
import main.java.com.elderly.monitoring.alert.entity.AlertRule;
import main.java.com.elderly.monitoring.alert.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 预警系统控制器
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertService alertService;

    /**
     * 创建预警规则
     */
    @PostMapping("/rules")
    public ResponseEntity<?> createAlertRule(@RequestBody AlertRule alertRule) {
        try {
            AlertRule createdRule = alertService.createAlertRule(alertRule);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", createdRule,
                "message", "预警规则创建成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "创建预警规则失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取用户的预警规则列表
     */
    @GetMapping("/rules")
    public ResponseEntity<?> getUserAlertRules(@RequestParam Long userId) {
        try {
            List<AlertRule> rules = alertService.getUserAlertRules(userId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", rules,
                "message", "获取预警规则成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取预警规则失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 更新预警规则
     */
    @PutMapping("/rules/{ruleId}")
    public ResponseEntity<?> updateAlertRule(
            @PathVariable Long ruleId, 
            @RequestBody AlertRule alertRule) {
        try {
            AlertRule updatedRule = alertService.updateAlertRule(ruleId, alertRule);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", updatedRule,
                "message", "预警规则更新成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "更新预警规则失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 删除预警规则
     */
    @DeleteMapping("/rules/{ruleId}")
    public ResponseEntity<?> deleteAlertRule(@PathVariable Long ruleId) {
        try {
            alertService.deleteAlertRule(ruleId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "预警规则删除成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "删除预警规则失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取用户的预警记录
     */
    @GetMapping("/records")
    public ResponseEntity<?> getUserAlertRecords(@RequestParam Long userId) {
        try {
            List<AlertRecord> records = alertService.getUserAlertRecords(userId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", records,
                "message", "获取预警记录成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取预警记录失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 处理预警记录
     */
    @PutMapping("/records/{recordId}/resolve")
    public ResponseEntity<?> resolveAlert(
            @PathVariable Long recordId,
            @RequestBody Map<String, Object> request) {
        try {
            Long resolvedBy = Long.valueOf(request.get("resolvedBy").toString());
            String resolutionNotes = request.get("resolutionNotes") != null ? 
                request.get("resolutionNotes").toString() : "";
            
            AlertRecord resolvedAlert = alertService.resolveAlert(recordId, resolvedBy, resolutionNotes);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", resolvedAlert,
                "message", "预警处理成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "处理预警失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取预警统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getAlertStatistics(@RequestParam Long userId) {
        try {
            Map<String, Object> statistics = alertService.getAlertStatistics(userId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", statistics,
                "message", "获取预警统计成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取预警统计失败: " + e.getMessage()
            ));
        }
    }
}