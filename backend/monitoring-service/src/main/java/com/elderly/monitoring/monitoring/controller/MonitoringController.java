package com.elderly.monitoring.monitoring.controller;

import com.elderly.monitoring.monitoring.entity.MonitoringData;
import com.elderly.monitoring.monitoring.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 实时监控控制器
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*")
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 获取实时监控数据
     */
    @GetMapping("/realtime")
    public ResponseEntity<?> getRealTimeData(
            @RequestParam(required = false) String deviceId,
            @RequestParam(required = false) String dataType) {
        try {
            List<MonitoringData> data = monitoringService.getRealTimeData(deviceId, dataType);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", data,
                "message", "获取实时数据成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取实时数据失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 上传监控数据
     */
    @PostMapping("/data")
    public ResponseEntity<?> uploadMonitoringData(@RequestBody MonitoringData monitoringData) {
        try {
            monitoringService.saveMonitoringData(monitoringData);
            
            // 通过WebSocket推送实时数据
            messagingTemplate.convertAndSend("/topic/monitoring", monitoringData);
            
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "数据上传成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "数据上传失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取最新监控数据
     */
    @GetMapping("/latest/{userId}")
    public ResponseEntity<?> getLatestData(@PathVariable String userId) {
        try {
            List<MonitoringData> data = monitoringService.getLatestDataByUser(userId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", data,
                "message", "获取最新数据成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取最新数据失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取历史监控数据
     */
    @GetMapping("/history")
    public ResponseEntity<?> getHistoryData(
            @RequestParam String deviceId,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            List<MonitoringData> data = monitoringService.getHistoryData(
                deviceId, startTime, endTime);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", data,
                "message", "获取历史数据成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取历史数据失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取设备状态
     */
    @GetMapping("/device-status")
    public ResponseEntity<?> getDeviceStatus() {
        try {
            List<Map<String, Object>> deviceStatus = monitoringService.getDeviceStatus();
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", deviceStatus,
                "message", "获取设备状态成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取设备状态失败: " + e.getMessage()
            ));
        }
    }

    /**
     * WebSocket消息处理 - 实时数据推送
     */
    @MessageMapping("/monitoring-data")
    @SendTo("/topic/monitoring")
    public MonitoringData handleMonitoringData(MonitoringData data) {
        monitoringService.saveMonitoringData(data);
        return data;
    }
}