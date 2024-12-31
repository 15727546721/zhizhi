package cn.xu.domain.article.task;

import cn.xu.domain.article.repository.IArticleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class DataSyncTask {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private IArticleRepository articleRepository;

    private static final String LIKE_COUNT_PATTERN = "article:like:*:count";
    private static final long SYNC_LOCK_TIMEOUT = 30L; // 30秒

    @Scheduled(fixedRate = 5000) // 每5秒执行一次
    public void syncDataToDatabase() {
        String syncLockKey = "article:sync:lock";

        // 尝试获取同步锁
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(syncLockKey, "1", SYNC_LOCK_TIMEOUT, TimeUnit.SECONDS);
        if (Boolean.FALSE.equals(acquired)) {
            log.debug("其他实例正在执行同步任务");
            return;
        }

        try {
            // 扫描所有需要同步的计数器
            Set<String> keys = scanKeys(LIKE_COUNT_PATTERN);
            if (keys.isEmpty()) {
                return;
            }

            // 批量获取点赞数
            Map<Long, Long> likeCounts = new HashMap<>();
            for (String key : keys) {
                try {
                    Long articleId = extractArticleId(key);
                    Object count = redisTemplate.opsForValue().get(key);
                    if (articleId != null && count != null) {
                        likeCounts.put(articleId, Long.parseLong(count.toString()));
                    }
                } catch (Exception e) {
                    log.error("处理key{}时出错", key, e);
                }
            }

            if (!likeCounts.isEmpty()) {
                // 批量更新数据库
                articleRepository.batchUpdateArticleLikeCount(likeCounts);
                log.info("同步{}篇文章的点赞数到数据库", likeCounts.size());

                // 删除已同步的计数器
                redisTemplate.delete(keys);
            }

        } catch (Exception e) {
            log.error("同步数据到数据库时出错", e);
        } finally {
            // 释放同步锁
            redisTemplate.delete(syncLockKey);
        }
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        redisTemplate.execute((RedisCallback<Void>) connection -> {
            try (Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(pattern).count(100).build())) {
                while (cursor.hasNext()) {
                    keys.add(new String(cursor.next()));
                }
            } catch (Exception e) {
                log.error("扫描Redis键时出错", e);
            }
            return null;
        });
        return keys;
    }

    private Long extractArticleId(String key) {
        try {
            String[] parts = key.split(":");
            if (parts.length >= 3) {
                return Long.parseLong(parts[2]);
            }
        } catch (NumberFormatException e) {
            log.error("解析文章ID时出错，key={}", key, e);
        }
        return null;
    }
}
