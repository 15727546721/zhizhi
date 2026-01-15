package cn.xu.task;

import cn.xu.cache.core.RedisKeyManager;
import cn.xu.cache.core.RedisOperations;
import cn.xu.integration.search.strategy.ElasticsearchSearchStrategy;
import cn.xu.model.entity.Post;
import cn.xu.service.post.PostQueryService;
import cn.xu.support.util.PostHotScoreCacheHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashSet;
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

    private final RedisOperations redisOps;
    private final PostQueryService postQueryService;
    @Autowired(required = false)
    private ElasticsearchSearchStrategy esStrategy;
    private final PostHotScoreCacheHelper hotScoreHelper;

    @Scheduled(cron = "0 */5 * * * ?")
    public void syncToElastic() {
        if (esStrategy == null) {
            log.debug("Elasticsearch服务不可用，跳过热度同步任务");
            return;
        }

        String pattern = RedisKeyManager.postHotCacheKey(0L).replace("0", "*");
        Set<String> keys = scanKeys(pattern);
        if (keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            try {
                String[] parts = key.split(":");
                if (parts.length < 3) {
                    log.warn("Redis key格式异常: {}", key);
                    continue;
                }
                
                String idPart = parts[2];
                if (!idPart.matches("\\d+")) {
                    continue;
                }
                Long postId = Long.parseLong(idPart);

                Map<Object, Object> map = redisOps.hGetAll(key);
                if (map.isEmpty()) {
                    log.warn("热度缓存为空，跳过同步，key={}", key);
                    continue;
                }

                int like = parseIntSafe(map.get("like"));
                int collect = parseIntSafe(map.get("collect"));
                int comment = parseIntSafe(map.get("comment"));

                Post post = postQueryService.getById(postId).orElse(null);
                    
                if (post == null) {
                    log.warn("帖子不存在，跳过同步，postId={}", postId);
                    continue;
                }

                esStrategy.indexPost(post);
                hotScoreHelper.clearHotData(postId);

                log.info("帖子[{}]热度已同步至Elasticsearch", postId);

            } catch (Exception e) {
                log.error("同步帖子热度失败，key={}", key, e);
            }
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void decayHotComments() {
        String pattern = RedisKeyManager.commentHotDecayKey() + ":*";
        Set<String> keys = scanKeys(pattern);
        for (String zsetKey : keys) {
            Set<ZSetOperations.TypedTuple<Object>> hotComments = redisOps.zRangeWithScores(zsetKey, 0, -1);

            if (hotComments != null) {
                for (ZSetOperations.TypedTuple<Object> tuple : hotComments) {
                    double oldScore = tuple.getScore();
                    double newScore = oldScore * 0.98;
                    redisOps.zAdd(zsetKey, Objects.requireNonNull(tuple.getValue()), newScore);

                    if (newScore < 5) {
                        redisOps.zRemove(zsetKey, tuple.getValue());
                    }
                }
            }
        }
    }

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
    
    private Set<String> scanKeys(String pattern) {
        return redisOps.scan(pattern, 100);
    }
}