package cn.xu.service.column;

import cn.xu.model.entity.ColumnStatistics;
import cn.xu.repository.ColumnRepository;
import cn.xu.repository.ColumnPostRepository;
import cn.xu.repository.ColumnSubscriptionRepository;
import cn.xu.repository.ColumnStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专栏统计服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ColumnStatisticsService {
    
    private final ColumnStatisticsRepository statisticsRepository;
    private final ColumnRepository columnRepository;
    private final ColumnSubscriptionRepository subscriptionRepository;
    private final ColumnPostRepository columnPostRepository;
    
    /**
     * 每天凌晨1点统计前一天的数据
     */
    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void generateDailyStatistics() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        log.info("开始生成专栏统计数据: {}", yesterday);
        
        List<Long> columnIds = columnRepository.findAllColumnIds();
        
        for (Long columnId : columnIds) {
            try {
                generateStatisticsForColumn(columnId, yesterday);
            } catch (Exception e) {
                log.error("生成专栏统计失败: columnId={}", columnId, e);
            }
        }
        
        log.info("专栏统计数据生成完成，共处理 {} 个专栏", columnIds.size());
    }
    
    /**
     * 生成单个专栏的统计数据
     */
    @Transactional
    public void generateStatisticsForColumn(Long columnId, LocalDate date) {
        ColumnStatistics statistics = new ColumnStatistics();
        statistics.setColumnId(columnId);
        statistics.setStatDate(date);
        
        // 统计阅读量（从文章阅读量汇总）
        Integer viewCount = columnPostRepository.sumViewCountByColumnId(columnId);
        statistics.setViewCount(viewCount != null ? viewCount : 0);
        
        // 统计订阅数
        Integer subscribeCount = subscriptionRepository.countByColumnId(columnId);
        statistics.setSubscribeCount(subscribeCount != null ? subscribeCount : 0);
        
        // 统计文章数
        Integer postCount = columnPostRepository.countByColumnId(columnId);
        statistics.setPostCount(postCount != null ? postCount : 0);
        
        statisticsRepository.save(statistics);
        log.debug("专栏统计数据已保存: columnId={}, date={}", columnId, date);
    }
    
    /**
     * 获取专栏统计趋势
     */
    public Map<String, Object> getStatisticsTrend(Long columnId, Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        List<ColumnStatistics> statistics = statisticsRepository
                .findByColumnIdAndDateRange(columnId, startDate, endDate);
        
        // 转换为图表数据格式
        List<String> dates = statistics.stream()
                .map(s -> s.getStatDate().toString())
                .collect(Collectors.toList());
        
        List<Integer> viewCounts = statistics.stream()
                .map(ColumnStatistics::getViewCount)
                .collect(Collectors.toList());
        
        List<Integer> subscribeCounts = statistics.stream()
                .map(ColumnStatistics::getSubscribeCount)
                .collect(Collectors.toList());
        
        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("viewCounts", viewCounts);
        result.put("subscribeCounts", subscribeCounts);
        
        return result;
    }
    
    /**
     * 获取专栏统计摘要
     */
    public Map<String, Object> getStatisticsSummary(Long columnId) {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        LocalDate weekAgo = today.minusDays(7);
        
        ColumnStatistics todayStats = statisticsRepository.findByColumnIdAndDate(columnId, today);
        ColumnStatistics yesterdayStats = statisticsRepository.findByColumnIdAndDate(columnId, yesterday);
        List<ColumnStatistics> weekStats = statisticsRepository
                .findByColumnIdAndDateRange(columnId, weekAgo, today);
        
        Map<String, Object> summary = new HashMap<>();
        
        // 今日数据
        if (todayStats != null) {
            summary.put("todayViews", todayStats.getViewCount());
            summary.put("todaySubscribes", todayStats.getSubscribeCount());
        } else {
            summary.put("todayViews", 0);
            summary.put("todaySubscribes", 0);
        }
        
        // 昨日对比
        if (yesterdayStats != null) {
            int viewGrowth = todayStats != null ? 
                    todayStats.getViewCount() - yesterdayStats.getViewCount() : 0;
            int subscribeGrowth = todayStats != null ?
                    todayStats.getSubscribeCount() - yesterdayStats.getSubscribeCount() : 0;
            
            summary.put("viewGrowth", viewGrowth);
            summary.put("subscribeGrowth", subscribeGrowth);
        }
        
        // 近7天总计
        int weekTotalViews = weekStats.stream()
                .mapToInt(ColumnStatistics::getViewCount)
                .sum();
        int weekTotalSubscribes = weekStats.stream()
                .mapToInt(ColumnStatistics::getSubscribeCount)
                .sum();
        
        summary.put("weekTotalViews", weekTotalViews);
        summary.put("weekTotalSubscribes", weekTotalSubscribes);
        
        return summary;
    }
}
