package cn.xu.application.task;

import cn.xu.common.utils.PostHotScoreCacheHelper;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.cache.RedisKeyManager;
import cn.xu.infrastructure.persistent.po.Post;
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
                // 注意：这里我们需要获取PostEntity，然后手动构建Post PO对象
                // 因为领域层不应该直接暴露PO对象
                PostEntity postEntity = postRepository.findById(postId)
                    .map(aggregate -> aggregate.getPostEntity())
                    .orElse(null);
                    
                if (postEntity == null) {
                    log.warn("帖子不存在，跳过同步，postId={}", postId);
                    continue;
                }

                // 手动构建Post PO对象
                Post post = new Post();
                post.setId(postEntity.getId());
                post.setUserId(postEntity.getUserId());
                post.setTitle(postEntity.getTitleValue());
                post.setDescription(postEntity.getDescription());
                post.setContent(postEntity.getContentValue());
                post.setCoverUrl(postEntity.getCoverUrl());
                post.setStatus(postEntity.getStatusCode()); // 直接使用getStatusCode方法
                post.setCategoryId(postEntity.getCategoryId());
                post.setViewCount((postEntity.getViewCount() == null ? 0 : postEntity.getViewCount()) + like); // 累加最新热度数值
                post.setLikeCount((postEntity.getLikeCount() == null ? 0 : postEntity.getLikeCount()) + collect);
                post.setCollectCount((postEntity.getCollectCount() == null ? 0 : postEntity.getCollectCount()) + comment);
                post.setCommentCount(postEntity.getCommentCount() == null ? 0 : postEntity.getCommentCount());
                post.setCreateTime(postEntity.getCreateTime());
                post.setUpdateTime(postEntity.getUpdateTime());
                post.setPublishTime(postEntity.getPublishTime());

                // 同步到ES
                postElasticService.indexPost(post);

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