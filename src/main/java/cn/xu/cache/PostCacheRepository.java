package cn.xu.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子缓存仓储
 * <p>处理帖子相关的缓存操作</p>
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码</p>
 
 */
@Slf4j
@Repository
public class PostCacheRepository extends BaseCacheRepository {

    /**
     * 获取热门帖子ID列表
     * @param start 开始位置
     * @param end 结束位置
     * @return 帖子ID列表
     */
    public List<Long> getHotPostIds(int start, int end) {
        String redisKey = RedisKeyManager.postHotRankKey();
        Set<Object> postIds = redisOps.zReverseRange(redisKey, start, end);
        
        if (!postIds.isEmpty()) {
            return postIds.stream()
                    .map(this::convertToLong)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    /**
     * 缓存热门帖子排行
     * @param postScores 帖子ID和分数的映射
     */
    public void cacheHotPostRank(Map<Long, Double> postScores) {
        if (postScores == null || postScores.isEmpty()) {
            cacheEmptyResult();
            return;
        }

        String redisKey = RedisKeyManager.postHotRankKey();
        deleteCache(redisKey); // 先清空旧数据
        
        // 批量插入新数据
        postScores.forEach((postId, score) -> {
            if (score != null && postId != null) {
                redisOps.zAdd(redisKey, postId.toString(), score);
            }
        });
        
        expire(redisKey, RedisKeyManager.POST_HOT_RANK_TTL);
        log.debug("缓存热门帖子排行: key={}, size={}", redisKey, postScores.size());
    }

    /**
     * 缓存空结果，防止缓存穿透
     */
    public void cacheEmptyResult() {
        String redisKey = RedisKeyManager.postHotRankKey() + ":empty";
        setValue(redisKey, "1", RedisKeyManager.EMPTY_RESULT_TTL);
    }

    /**
     * 检查是否为空结果缓存
     * @return 是否为空结果
     */
    public boolean isEmptyResultCached() {
        String redisKey = RedisKeyManager.postHotRankKey() + ":empty";
        return hasKey(redisKey);
    }

    /**
     * 清理无效的缓存数据
     * @param invalidIds 无效的帖子ID列表
     */
    public void cleanupInvalidCacheData(List<Long> invalidIds) {
        if (invalidIds == null || invalidIds.isEmpty()) {
            return;
        }
        
        String redisKey = RedisKeyManager.postHotRankKey();
        // 批量删除无效的ID
        Object[] values = invalidIds.stream().map(String::valueOf).toArray();
        redisOps.zRemove(redisKey, values);
        log.info("[缓存] 清理无效数据: key={}, count={}", redisKey, invalidIds.size());
    }

    /**
     * 获取帖子的热度分数
     * @param postId 帖子ID
     * @return 热度分数，不存在返回null
     */
    public Double getHotScore(Long postId) {
        String redisKey = RedisKeyManager.postHotRankKey();
        return redisOps.zScore(redisKey, postId.toString());
    }

    /**
     * 获取热度排行的总数量
     * @return 排行总数量
     */
    public Long getHotRankSize() {
        String redisKey = RedisKeyManager.postHotRankKey();
        return redisOps.zSize(redisKey);
    }
}
