package cn.xu.service.statistics;

import cn.xu.cache.core.RedisOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 帖子浏览量统计服务
 * <p>记录每日帖子浏览量快照，用于计算环比</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostViewStatisticsService {

    private final RedisOperations redisOps;

    private static final String KEY_PREFIX = "stats:post:view:";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int SNAPSHOT_EXPIRE_DAYS = 7; // 保留7天快照

    /**
     * 记录帖子当日浏览量快照
     */
    public void recordDailySnapshot(Long postId, long viewCount) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = KEY_PREFIX + today;
        redisOps.hSet(key, postId.toString(), viewCount);
        redisOps.expire(key, SNAPSHOT_EXPIRE_DAYS * 24 * 3600);
    }

    /**
     * 批量记录帖子当日浏览量快照
     */
    public void recordDailySnapshots(Map<Long, Long> postViewCounts) {
        if (postViewCounts == null || postViewCounts.isEmpty()) {
            return;
        }
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = KEY_PREFIX + today;
        
        Map<String, Object> data = new HashMap<>();
        postViewCounts.forEach((postId, viewCount) -> data.put(postId.toString(), viewCount));
        redisOps.hSetAll(key, data);
        redisOps.expire(key, SNAPSHOT_EXPIRE_DAYS * 24 * 3600);
    }

    /**
     * 获取帖子昨日浏览量
     */
    public long getYesterdayViewCount(Long postId) {
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        String key = KEY_PREFIX + yesterday;
        Object value = redisOps.hGet(key, postId.toString());
        if (value == null) {
            return 0;
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * 批量获取帖子昨日浏览量
     */
    public Map<Long, Long> getYesterdayViewCounts(List<Long> postIds) {
        String yesterday = LocalDate.now().minusDays(1).format(DATE_FORMATTER);
        String key = KEY_PREFIX + yesterday;
        
        Map<Long, Long> result = new HashMap<>();
        Map<Object, Object> data = redisOps.hGetAll(key);
        
        for (Long postId : postIds) {
            Object value = data.get(postId.toString());
            if (value != null) {
                try {
                    result.put(postId, Long.parseLong(value.toString()));
                } catch (NumberFormatException e) {
                    result.put(postId, 0L);
                }
            } else {
                result.put(postId, 0L);
            }
        }
        return result;
    }

    /**
     * 计算环比增长率
     * @return 增长率百分比，如 15.5 表示增长15.5%
     */
    public double calculateGrowthRate(long todayCount, long yesterdayCount) {
        if (yesterdayCount == 0) {
            return todayCount > 0 ? 100.0 : 0.0;
        }
        return ((double) (todayCount - yesterdayCount) / yesterdayCount) * 100;
    }
}
