package cn.xu.task;

import cn.xu.cache.RedisKeyManager;
import cn.xu.integration.search.strategy.ElasticsearchSearchStrategy;
import cn.xu.model.entity.Post;
import cn.xu.service.post.PostService;
import cn.xu.support.util.PostHotScoreCacheHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 定时任务同步热度分到Elasticsearch
 * 
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotScoreSyncTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final PostService postService;
    @Autowired(required = false) // 设置为非必需，允许Elasticsearch不可用
    private ElasticsearchSearchStrategy esStrategy;
    private final PostHotScoreCacheHelper hotScoreHelper;

    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    public void syncToElastic() {
        // 检查Elasticsearch是否可用
        if (esStrategy == null) {
            log.debug("Elasticsearch服务不可用，跳过热度同步任务");
            return;
        }

        // 使用 SCAN 替代 KEYS，避免阻塞Redis
        String pattern = RedisKeyManager.postHotCacheKey(0L).replace("0", "*");
        Set<String> keys = scanKeys(pattern);
        if (keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                // 解析帖子ID，避免数组越界异常
                // key格式为: post:hot:{postId}
                String[] parts = key.split(":");
                if (parts.length < 3) {
                    log.warn("Redis key格式异常: {}", key);
                    continue;
                }
                
                // 跳过非数字ID的key（如 post:hot:ranking）
                String idPart = parts[2];
                if (!idPart.matches("\\d+")) {
                    continue;
                }
                Long postId = Long.parseLong(idPart);

                Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
                if (map.isEmpty()) {
                    log.warn("热度缓存为空，跳过同步，key={}", key);
                    continue;
                }

                int like = parseIntSafe(map.get("like"));
                int collect = parseIntSafe(map.get("collect"));
                int comment = parseIntSafe(map.get("comment"));

                // 查询数据库帖子
                Post post = postService.getPostById(postId).orElse(null);
                    
                if (post == null) {
                    log.warn("帖子不存在，跳过同步，postId={}", postId);
                    continue;
                }

                // 同步到ES
                esStrategy.indexPost(post);

                // 清理缓存，避免重复同步
                hotScoreHelper.clearHotData(postId);

                log.info("帖子[{}]热度已同步至Elasticsearch", postId);

            } catch (Exception e) {
                log.error("同步帖子热度失败，key={}", key, e);
            }
        }
    }

    /**
     * 衰减热度 & 清理低评评论
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void decayHotComments() {
        // 使用 SCAN 替代 KEYS，避免阻塞Redis
        String pattern = RedisKeyManager.commentHotDecayKey() + ":*";
        Set<String> keys = scanKeys(pattern);
        for (String zsetKey : keys) {
            Set<ZSetOperations.TypedTuple<Object>> hotComments = redisTemplate.opsForZSet()
                    .rangeWithScores(zsetKey, 0, -1);

            if (hotComments != null) {
                for (ZSetOperations.TypedTuple<Object> tuple : hotComments) {
                    double oldScore = tuple.getScore();
                    double newScore = oldScore * 0.98; // 衰减 2%
                    redisTemplate.opsForZSet().add(zsetKey, Objects.requireNonNull(tuple.getValue()), newScore);

                    // 清理过低的
                    if (newScore < 5) {
                        redisTemplate.opsForZSet().remove(zsetKey, tuple.getValue());
                    }
                }
            }
        }
    }

    /**
     * 安全转换整数，防止空指针和格式异常
     */
    private int parseIntSafe(Object obj) {
        if (obj == null) {
            return 0;
        }
        try {
            return Integer.parseInt(String.valueOf(obj));
        } catch (NumberFormatException e) {
            log.warn("热度字段转换失败，值:{}，默认返回0", obj);
            return 0;
        }
    }
    
    /**
     * 使用 SCAN 命令扫描 Redis 键，避免 KEYS 命令阻塞
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new java.util.HashSet<>();
        redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Object>) connection -> {
            org.springframework.data.redis.core.Cursor<byte[]> cursor = connection.scan(
                org.springframework.data.redis.core.ScanOptions.scanOptions()
                    .match(pattern)
                    .count(100)
                    .build()
            );
            while (cursor.hasNext()) {
                keys.add(new String(cursor.next()));
            }
            cursor.close();
            return null;
        });
        return keys;
    }
}