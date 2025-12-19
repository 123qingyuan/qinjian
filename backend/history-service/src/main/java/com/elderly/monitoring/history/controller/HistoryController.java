package main.java.com.elderly.monitoring.history.controller;

import main.java.com.elderly.monitoring.history.entity.HistoryData;
import main.java.com.elderly.monitoring.history.service.HistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 历史数据控制器
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@RestController
@RequestMapping("/api/history")
@CrossOrigin(origins = "*")
public class HistoryController {

    @Autowired
    private HistoryService historyService;

    /**
     * 获取用户历史数据
     */
    @GetMapping("/data")
    public ResponseEntity<?> getHistoryData(
            @RequestParam Long userId,
            @RequestParam(required = false) String dataType,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {
        try {
            List<HistoryData> data;
            
            if (dataType != null && !dataType.isEmpty() && startTime != null && endTime != null) {
                data = historyService.getHistoryDataByComplexQuery(userId, dataType, startTime, endTime);
            } else if (dataType != null && !dataType.isEmpty()) {
                data = historyService.getHistoryDataByUserIdAndType(userId, dataType);
            } else if (startTime != null && endTime != null) {
                data = historyService.getHistoryDataByTimeRange(userId, startTime, endTime);
            } else {
                data = historyService.getHistoryDataByUserId(userId);
            }
            
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
     * 分页获取历史数据
     */
    @GetMapping("/data/page")
    public ResponseEntity<?> getHistoryDataByPage(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Map<String, Object> result = historyService.getHistoryDataByPage(userId, page, size);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", result,
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
     * 获取数据统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getDataStatistics(@RequestParam Long userId) {
        try {
            Map<String, Object> statistics = historyService.getStatistics(userId);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", statistics,
                "message", "获取数据统计成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取数据统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取趋势分析数据
     */
    @GetMapping("/trend")
    public ResponseEntity<?> getTrendAnalysis(
            @RequestParam Long userId,
            @RequestParam String dataType) {
        try {
            Map<String, Object> trendData = historyService.getTrendAnalysis(userId, dataType);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", trendData,
                "message", "获取趋势分析成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "获取趋势分析失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 生成健康报告
     */
    @GetMapping("/health-report")
    public ResponseEntity<?> generateHealthReport(
            @RequestParam Long userId,
            @RequestParam(defaultValue = "WEEKLY") String reportType) {
        try {
            Map<String, Object> healthReport = historyService.generateHealthReport(userId, reportType);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", healthReport,
                "message", "生成健康报告成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "生成健康报告失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 导出历史数据
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportHistoryData(
            @RequestParam Long userId,
            @RequestParam(required = false) String dataType,
            @RequestParam String startTime,
            @RequestParam String endTime) {
        try {
            String csvData = historyService.exportHistoryData(userId, dataType, startTime, endTime);
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/csv; charset=utf-8")
                .header("Content-Disposition", "attachment; filename=\"history_data.csv\"")
                .body(csvData.getBytes());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "导出数据失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 上传历史数据（批量导入）
     */
    @PostMapping("/data/batch")
    public ResponseEntity<?> uploadHistoryDataBatch(@RequestBody List<HistoryData> historyDataList) {
        try {
            List<HistoryData> savedData = historyService.saveHistoryDataBatch(historyDataList);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", savedData,
                "message", "批量保存历史数据成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "批量保存失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 生成模拟数据（用于测试）
     */
    @PostMapping("/generate-mock-data")
    public ResponseEntity<?> generateMockData(@RequestParam Long userId) {
        try {
            List<HistoryData> mockData = historyService.generateMockHistoryData(userId);
            List<HistoryData> savedData = historyService.saveHistoryDataBatch(mockData);
            return ResponseEntity.ok(Map.of(
                "code", 200,
                "data", savedData,
                "message", "生成模拟数据成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "code", 400,
                "message", "生成模拟数据失败: " + e.getMessage()
            ));
        }
    }
}