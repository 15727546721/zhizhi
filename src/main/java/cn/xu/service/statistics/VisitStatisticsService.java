package cn.xu.service.statistics;

import cn.xu.common.constants.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 访问统计服务
 * 
 * <p>使用 Redis HyperLogLog 实现 UV 统计，String 实现 PV 统计</p>
 * <p>HyperLogLog 特点：内存占用极小（约12KB），误差率约0.81%</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VisitStatisticsService {

    private final StringRedisTemplate redisTemplate;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    
    /** 每日统计数据保留天数 */
    private static final int DAILY_STATS_EXPIRE_DAYS = 90;

    /**
     * 记录访问
     * 
     * @param visitorId 访客标识（可以是用户ID、IP、或组合）
     */
    public void recordVisit(String visitorId) {
        try {
            String today = LocalDate.now().format(DATE_FORMATTER);
            
            // 记录每日UV（HyperLogLog）
            String uvKey = RedisKeyConstants.STATS_UV_DAILY + today;
            redisTemplate.opsForHyperLogLog().add(uvKey, visitorId);
            redisTemplate.expire(uvKey, DAILY_STATS_EXPIRE_DAYS, TimeUnit.DAYS);
            
            // 记录总UV（HyperLogLog）
            redisTemplate.opsForHyperLogLog().add(RedisKeyConstants.STATS_UV_TOTAL, visitorId);
            
            // 记录每日PV
            String pvKey = RedisKeyConstants.STATS_PV_DAILY + today;
            redisTemplate.opsForValue().increment(pvKey);
            redisTemplate.expire(pvKey, DAILY_STATS_EXPIRE_DAYS, TimeUnit.DAYS);
            
            // 记录总PV
            redisTemplate.opsForValue().increment(RedisKeyConstants.STATS_PV_TOTAL);
            
        } catch (Exception e) {
            log.warn("记录访问统计失败: visitorId={}", visitorId, e);
        }
    }

    /**
     * 获取今日UV
     */
    public long getTodayUV() {
        try {
            String today = LocalDate.now().format(DATE_FORMATTER);
            String key = RedisKeyConstants.STATS_UV_DAILY + today;
            Long count = redisTemplate.opsForHyperLogLog().size(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("获取今日UV失败", e);
            return 0L;
        }
    }

    /**
     * 获取昨日UV
     */
    public long getYesterdayUV() {
        try {
            String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            String key = RedisKeyConstants.STATS_UV_DAILY + yesterday;
            Long count = redisTemplate.opsForHyperLogLog().size(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("获取昨日UV失败", e);
            return 0L;
        }
    }

    /**
     * 获取指定日期的UV
     */
    public long getUVByDate(LocalDate date) {
        try {
            String dateStr = date.format(DATE_FORMATTER);
            String key = RedisKeyConstants.STATS_UV_DAILY + dateStr;
            Long count = redisTemplate.opsForHyperLogLog().size(key);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("获取指定日期UV失败: date={}", date, e);
            return 0L;
        }
    }

    /**
     * 获取总UV
     */
    public long getTotalUV() {
        try {
            Long count = redisTemplate.opsForHyperLogLog().size(RedisKeyConstants.STATS_UV_TOTAL);
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("获取总UV失败", e);
            return 0L;
        }
    }

    /**
     * 获取今日PV
     */
    public long getTodayPV() {
        try {
            String today = LocalDate.now().format(DATE_FORMATTER);
            String key = RedisKeyConstants.STATS_PV_DAILY + today;
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            log.warn("获取今日PV失败", e);
            return 0L;
        }
    }

    /**
     * 获取昨日PV
     */
    public long getYesterdayPV() {
        try {
            String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
            String key = RedisKeyConstants.STATS_PV_DAILY + yesterday;
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            log.warn("获取昨日PV失败", e);
            return 0L;
        }
    }

    /**
     * 获取指定日期的PV
     */
    public long getPVByDate(LocalDate date) {
        try {
            String dateStr = date.format(DATE_FORMATTER);
            String key = RedisKeyConstants.STATS_PV_DAILY + dateStr;
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            log.warn("获取指定日期PV失败: date={}", date, e);
            return 0L;
        }
    }

    /**
     * 获取总PV
     */
    public long getTotalPV() {
        try {
            String value = redisTemplate.opsForValue().get(RedisKeyConstants.STATS_PV_TOTAL);
            return value != null ? Long.parseLong(value) : 0L;
        } catch (Exception e) {
            log.warn("获取总PV失败", e);
            return 0L;
        }
    }

    /**
     * 获取近N天的UV趋势
     * 
     * @param days 天数
     * @return UV列表（从最早到最近）
     */
    public List<Long> getUVTrend(int days) {
        List<Long> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            trend.add(getUVByDate(date));
        }
        return trend;
    }

    /**
     * 获取近N天的PV趋势
     * 
     * @param days 天数
     * @return PV列表（从最早到最近）
     */
    public List<Long> getPVTrend(int days) {
        List<Long> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            trend.add(getPVByDate(date));
        }
        return trend;
    }

    /**
     * 获取近N天的日期列表
     * 
     * @param days 天数
     * @return 日期列表（MM-dd格式）
     */
    public List<String> getDateLabels(int days) {
        List<String> labels = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            labels.add(date.format(formatter));
        }
        return labels;
    }
}
