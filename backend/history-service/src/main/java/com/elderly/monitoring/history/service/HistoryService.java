package main.java.com.elderly.monitoring.history.service;

import main.java.com.elderly.monitoring.history.entity.HistoryData;
import main.java.com.elderly.monitoring.history.repository.HistoryDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 历史数据服务实现类
 * 
 * @author Elderly Monitoring Team
 * @version 1.0.0
 */
@Service
@Transactional
public class HistoryService {

    @Autowired
    private HistoryDataRepository historyDataRepository;

    /**
     * 保存历史数据
     */
    public HistoryData saveHistoryData(HistoryData historyData) {
        if (historyData.getRecordedAt() == null) {
            historyData.setRecordedAt(LocalDateTime.now());
        }
        return historyDataRepository.save(historyData);
    }

    /**
     * 批量保存历史数据
     */
    public List<HistoryData> saveHistoryDataBatch(List<HistoryData> historyDataList) {
        String batchId = UUID.randomUUID().toString();
        for (HistoryData data : historyDataList) {
            if (data.getRecordedAt() == null) {
                data.setRecordedAt(LocalDateTime.now());
            }
            data.setBatchId(batchId);
        }
        return historyDataRepository.saveAll(historyDataList);
    }

    /**
     * 根据用户ID查询历史数据
     */
    public List<HistoryData> getHistoryDataByUserId(Long userId) {
        return historyDataRepository.findByUserIdOrderByRecordedAtDesc(userId);
    }

    /**
     * 根据用户ID和数据类型查询历史数据
     */
    public List<HistoryData> getHistoryDataByUserIdAndType(Long userId, String dataType) {
        return historyDataRepository.findByUserIdAndDataTypeOrderByRecordedAtDesc(userId, dataType);
    }

    /**
     * 根据用户ID和时间范围查询历史数据
     */
    public List<HistoryData> getHistoryDataByTimeRange(Long userId, String startTime, String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        return historyDataRepository.findByUserIdAndTimeRange(userId, start, end);
    }

    /**
     * 根据用户ID、数据类型和时间范围查询历史数据
     */
    public List<HistoryData> getHistoryDataByComplexQuery(
            Long userId, String dataType, String startTime, String endTime) {
        LocalDateTime start = LocalDateTime.parse(startTime);
        LocalDateTime end = LocalDateTime.parse(endTime);
        return historyDataRepository.findByUserIdAndDataTypeAndTimeRange(userId, dataType, start, end);
    }

    /**
     * 分页查询历史数据
     */
    public Map<String, Object> getHistoryDataByPage(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<HistoryData> data = historyDataRepository.findByUserIdOrderByRecordedAtDesc(userId, pageable);
        
        Long totalCount = historyDataRepository.countByUserId(userId);
        Long totalPages = (totalCount + size - 1) / size;
        
        return Map.of(
            "data", data,
            "currentPage", page + 1,
            "pageSize", size,
            "totalCount", totalCount,
            "totalPages", totalPages
        );
    }

    /**
     * 获取用户数据统计信息
     */
    public Map<String, Object> getStatistics(Long userId) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        Long totalDataCount = historyDataRepository.countByUserId(userId);
        List<Object[]> typeStatistics = historyDataRepository.countByDataTypeSince(userId, thirtyDaysAgo);
        List<HistoryData> abnormalData = historyDataRepository.findAbnormalDataByUserId(userId);
        
        Map<String, Long> typeStatsMap = typeStatistics.stream()
            .collect(Collectors.toMap(
                arr -> arr[0].toString(),
                arr -> (Long) arr[1]
            ));
        
        return Map.of(
            "totalDataCount", totalDataCount,
            "typeStatistics", typeStatsMap,
            "abnormalDataCount", abnormalData.size(),
            "abnormalData", abnormalData
        );
    }

    /**
     * 获取数据趋势分析
     */
    public Map<String, Object> getTrendAnalysis(Long userId, String dataType) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<Object[]> trendData = historyDataRepository.getHealthTrendData(userId, sevenDaysAgo);
        
        Map<String, List<Double>> dailyValues = new HashMap<>();
        Set<String> dates = new HashSet<>();
        
        for (Object[] row : trendData) {
            String date = row[0].toString();
            String type = row[1].toString();
            Double avgValue = (Double) row[2];
            
            if (type.equals(dataType)) {
                dailyValues.computeIfAbsent(date, k -> new ArrayList<>()).add(avgValue);
            }
            dates.add(date);
        }
        
        List<Map<String, Object>> trendList = dailyValues.entrySet().stream()
            .map(entry -> Map.of(
                "date", entry.getKey(),
                "value", entry.getValue().get(0) // 取平均值
            ))
            .sorted((a, b) -> a.get("date").toString().compareTo(b.get("date").toString()))
            .collect(Collectors.toList());
        
        return Map.of(
            "trendData", trendList,
            "dateRange", Map.of(
                "start", sevenDaysAgo.toLocalDate().toString(),
                "end", LocalDateTime.now().toLocalDate().toString()
            )
        );
    }

    /**
     * 获取健康报告数据
     */
    public Map<String, Object> generateHealthReport(Long userId, String reportType) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        
        List<Object[]> statistics = historyDataRepository.getDataStatistics(userId, thirtyDaysAgo);
        Map<String, Object> statsMap = new HashMap<>();
        
        for (Object[] row : statistics) {
            String type = row[0].toString();
            Map<String, Object> statInfo = Map.of(
                "average", row[1],
                "minimum", row[2],
                "maximum", row[3],
                "count", row[4]
            );
            statsMap.put(type, statInfo);
        }
        
        List<HistoryData> recentAbnormalData = historyDataRepository.findAbnormalDataByUserId(userId)
            .stream()
            .limit(10)
            .collect(Collectors.toList());
        
        // 模拟健康评分
        int healthScore = calculateHealthScore(statsMap, recentAbnormalData.size());
        
        return Map.of(
            "reportType", reportType,
            "generatedAt", LocalDateTime.now().toString(),
            "healthScore", healthScore,
            "healthGrade", getHealthGrade(healthScore),
            "statistics", statsMap,
            "abnormalDataCount", recentAbnormalData.size(),
            "recentAbnormalData", recentAbnormalData,
            "recommendations", generateRecommendations(statsMap, recentAbnormalData.size())
        );
    }

    /**
     * 导出历史数据
     */
    public String exportHistoryData(Long userId, String dataType, String startTime, String endTime) {
        List<HistoryData> exportData;
        
        if (dataType != null && !dataType.isEmpty()) {
            exportData = getHistoryDataByComplexQuery(userId, dataType, startTime, endTime);
        } else {
            exportData = getHistoryDataByTimeRange(userId, startTime, endTime);
        }
        
        // 构建CSV格式的数据
        StringBuilder csvBuilder = new StringBuilder();
        csvBuilder.append("记录时间,设备ID,数据类型,数值,单位,位置,是否异常\n");
        
        for (HistoryData data : exportData) {
            csvBuilder.append(String.format("%s,%s,%s,%.2f,%s,%s,%s\n",
                data.getRecordedAt(),
                data.getDeviceId(),
                data.getDataType(),
                data.getDataValue(),
                data.getDataUnit() != null ? data.getDataUnit() : "",
                data.getLocationAddress() != null ? data.getLocationAddress() : "",
                data.getIsAbnormal() ? "是" : "否"
            ));
        }
        
        return csvBuilder.toString();
    }

    /**
     * 计算健康评分
     */
    private int calculateHealthScore(Map<String, Object> statistics, int abnormalCount) {
        int baseScore = 100;
        int deduction = abnormalCount * 5; // 每个异常数据扣5分
        
        // 根据数据波动性调整评分
        statistics.forEach((type, stats) -> {
            Map<String, Object> statInfo = (Map<String, Object>) stats;
            Double avg = (Double) statInfo.get("average");
            Double max = (Double) statInfo.get("maximum");
            Double min = (Double) statInfo.get("minimum");
            
            if (avg != null && max != null && min != null) {
                double volatility = (max - min) / avg;
                if (volatility > 0.3) {
                    deduction += 10; // 高波动性额外扣分
                }
            }
        });
        
        return Math.max(0, baseScore - deduction);
    }

    /**
     * 获取健康等级
     */
    private String getHealthGrade(int score) {
        if (score >= 90) return "优秀";
        if (score >= 80) return "良好";
        if (score >= 70) return "中等";
        if (score >= 60) return "一般";
        return "需要关注";
    }

    /**
     * 生成健康建议
     */
    private List<String> generateRecommendations(Map<String, Object> statistics, int abnormalCount) {
        List<String> recommendations = new ArrayList<>();
        
        if (abnormalCount > 5) {
            recommendations.add("建议尽快进行健康检查，异常数据较多。");
        }
        
        statistics.forEach((type, stats) -> {
            Map<String, Object> statInfo = (Map<String, Object>) stats;
            Double avg = (Double) statInfo.get("average");
            
            if ("TEMPERATURE".equals(type) && avg != null && avg > 37.5) {
                recommendations.add("平均体温偏高，建议注意休息和饮水。");
            }
            
            if ("HEART_RATE".equals(type) && avg != null && avg > 100) {
                recommendations.add("平均心率偏高，建议减少运动量并及时就医检查。");
            }
        });
        
        if (recommendations.isEmpty()) {
            recommendations.add("健康状况良好，请继续保持良好的生活习惯。");
        }
        
        return recommendations;
    }

    /**
     * 生成模拟历史数据
     */
    public List<HistoryData> generateMockHistoryData(Long userId) {
        List<HistoryData> mockData = new ArrayList<>();
        Random random = new Random();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 0; i < 100; i++) {
            LocalDateTime timestamp = now.minusDays(i);
            
            // 温度数据
            HistoryData tempData = new HistoryData();
            tempData.setUserId(userId);
            tempData.setDeviceId("TEMP001");
            tempData.setDataType("TEMPERATURE");
            tempData.setDataValue(36.0 + random.nextDouble() * 2.0);
            tempData.setDataUnit("°C");
            tempData.setIsAbnormal(random.nextDouble() < 0.1); // 10%概率异常
            tempData.setSourceSystem("MONITORING_SYSTEM");
            tempData.setDataQuality("GOOD");
            tempData.setLocationAddress("家");
            tempData.setRecordedAt(timestamp.minusHours(random.nextInt(24)));
            mockData.add(tempData);
            
            // 心率数据
            HistoryData heartRateData = new HistoryData();
            heartRateData.setUserId(userId);
            heartRateData.setDeviceId("HEART001");
            heartRateData.setDataType("HEART_RATE");
            heartRateData.setDataValue(60 + random.nextDouble() * 40.0);
            heartRateData.setDataUnit("bpm");
            heartRateData.setIsAbnormal(random.nextDouble() < 0.05); // 5%概率异常
            heartRateData.setSourceSystem("MONITORING_SYSTEM");
            heartRateData.setDataQuality("GOOD");
            heartRateData.setLocationAddress("家");
            heartRateData.setRecordedAt(timestamp.minusHours(random.nextInt(24)));
            mockData.add(heartRateData);
        }
        
        return mockData;
    }
}