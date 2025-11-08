package cn.xu.application.task;

import cn.xu.common.utils.PostHotScoreCacheHelper;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.persistent.read.elastic.service.PostElasticService;
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
 * 定时任务同步热度到 Elasticsearch
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HotScoreSyncTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final IPostRepository postRepository; // 使用领域层接口而不是基础设施层实现类
    @Autowired(required = false) // 设置为非必需，允许Elasticsearch不可用
    private PostElasticService postElasticService;
    private final PostHotScoreCacheHelper hotScoreHelper;

    @Scheduled(cron = "0 */5 * * * ?") // 每5分钟执行一次
    public void syncToElastic() {
        // 检查Elasticsearch是否可用
        if (postElasticService == null) {
            log.debug("Elasticsearch服务不可用，跳过热度同步任务");
            return;
        }

        Set<String> keys = redisTemplate.keys(RedisKeyManager.postHotCacheKey(0L).replace("0", "*"));
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
                Long postId = Long.parseLong(parts[2]);

                Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
                if (map.isEmpty()) {
                    log.warn("热度缓存为空，跳过同步，key={}", key);
                    continue;
                }

                int like = parseIntSafe(map.get("like"));
                int collect = parseIntSafe(map.get("collect"));
                int comment = parseIntSafe(map.get("comment"));

                // 查询数据库帖子实体
                PostEntity postEntity = postRepository.findById(postId)
                    .map(aggregate -> aggregate.getPostEntity())
                    .orElse(null);
                    
                if (postEntity == null) {
                    log.warn("帖子不存在，跳过同步，postId={}", postId);
                    continue;
                }

                // 注意：这里我们直接使用PostEntity，不再需要构建Post PO对象
                // 但是我们需要更新热度数值（从Redis缓存中读取的热度增量）
                // 由于PostEntity是不可变的值对象，我们需要重新构建它
                // 但实际上，ES索引会使用PostIndexConverter.from()，它会从PostEntity中读取数据
                // 所以这里我们只需要确保PostEntity是最新的即可
                
                // 同步到ES（PostElasticService会使用PostIndexConverter.from()来转换）
                // 注意：这里的热度数值（like, collect, comment）是从Redis缓存中读取的增量值
                // 但PostEntity中的值可能已经更新过了，所以这里直接同步PostEntity即可
                // ES索引的热度分数会在PostIndexConverter中重新计算
                postElasticService.indexPost(postEntity);

                // 清理缓存，避免重复同步
                hotScoreHelper.clearHotData(postId);

                log.info("帖子[{}]热度已同步至Elasticsearch", postId);

            } catch (Exception e) {
                log.error("同步帖子热度失败，key={}", key, e);
            }
        }
    }

    /**
     * 衰减热度 & 清理冷评论
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void decayHotComments() {
        Set<String> keys = redisTemplate.keys(RedisKeyManager.commentHotDecayKey() + ":*");
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
            log.warn("热度字段转换失败，值={}，默认返回0", obj);
            return 0;
        }
    }
}