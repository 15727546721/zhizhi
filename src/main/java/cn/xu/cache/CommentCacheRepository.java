package cn.xu.cache;

import cn.xu.common.constants.CommentType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论缓存仓储
 * <p>处理评论相关的缓存操作</p>
 * <p>继承BaseCacheRepository复用通用方法，减少重复代码</p>
 
 */
@Slf4j
@Repository
public class CommentCacheRepository extends BaseCacheRepository {
    
    /** 默认缓存TTL：10分钟 */
    private static final int DEFAULT_CACHE_TTL = 600;
    /** 空结果缓存TTL：30秒 */
    private static final int EMPTY_RESULT_TTL = 30;

    /**
     * 获取热门评论ID列表
     * @param commentType 评论类型
     * @param targetId 目标ID
     * @param start 开始位置
     * @param end 结束位置
     * @return 评论ID列表
     */
    public List<Long> getHotCommentIds(CommentType commentType, Long targetId, int start, int end) {
        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId);
        
        try {
            Set<Object> commentIds = redisTemplate.opsForZSet().reverseRange(redisKey, start, end);
            
            if (commentIds != null && !commentIds.isEmpty()) {
                return commentIds.stream()
                        .map(this::convertToLong)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("[缓存] 获取热门评论ID失败 - key: {}", redisKey, e);
        }
        
        return Collections.emptyList();
    }

    /**
     * 缓存热门评论排行
     * @param commentType 评论类型
     * @param targetId 目标ID
     * @param commentScores 评论ID和分数的映射
     */
    public void cacheHotCommentRank(CommentType commentType, Long targetId, Map<Long, Double> commentScores) {
        if (commentScores == null || commentScores.isEmpty()) {
            cacheEmptyResult(commentType, targetId);
            return;
        }

        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId);
        
        try {
            // 批量插入新数据（增量更新）
            commentScores.forEach((commentId, score) -> {
                if (score != null && commentId != null) {
                    redisTemplate.opsForZSet().add(redisKey, commentId.toString(), score);
                }
            });
            
            expire(redisKey, DEFAULT_CACHE_TTL);
            log.debug("缓存热门评论排行成功: key={}, size={}", redisKey, commentScores.size());
        } catch (Exception e) {
            log.error("[缓存] 缓存热门评论排行失败 - key: {}", redisKey, e);
        }
    }

    /**
     * 缓存空结果，防止缓存穿透
     * @param commentType 评论类型
     * @param targetId 目标ID
     */
    public void cacheEmptyResult(CommentType commentType, Long targetId) {
        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId) + ":empty";
        setValue(redisKey, "1", EMPTY_RESULT_TTL);
    }

    /**
     * 检查是否为空结果缓存
     * @param commentType 评论类型
     * @param targetId 目标ID
     * @return 是否为空结果
     */
    public boolean isEmptyResultCached(CommentType commentType, Long targetId) {
        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId) + ":empty";
        return hasKey(redisKey);
    }

    /**
     * 清理无效的缓存数据
     * @param commentType 评论类型
     * @param targetId 目标ID
     * @param invalidIds 无效的评论ID列表
     */
    public void cleanupInvalidCacheData(CommentType commentType, Long targetId, List<Long> invalidIds) {
        if (invalidIds == null || invalidIds.isEmpty()) {
            return;
        }
        
        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId);
        
        try {
            // 批量删除无效的ID
            for (Long invalidId : invalidIds) {
                redisTemplate.opsForZSet().remove(redisKey, invalidId.toString());
            }
            
            log.info("[缓存] 清理无效数据成功 - key: {}, count: {}", redisKey, invalidIds.size());
        } catch (Exception e) {
            log.error("[缓存] 清理无效数据失败 - key: {}", redisKey, e);
        }
    }

    /**
     * 更新评论计数
     * @param targetType 目标类型
     * @param targetId 目标ID
     * @param increment 增量
     */
    public void updateCommentCount(int targetType, Long targetId, long increment) {
        String redisKey = RedisKeyManager.commentCountKey(targetType, targetId);
        incrementCount(redisKey, increment, DEFAULT_CACHE_TTL);
    }

    /**
     * 更新热度排行缓存中的单个评论
     * @param commentType 评论类型
     * @param targetId 目标ID
     * @param commentId 评论ID
     * @param hotScore 热度分数
     */
    public void updateHotRank(CommentType commentType, Long targetId, Long commentId, double hotScore) {
        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId);
        
        try {
            redisTemplate.opsForZSet().add(redisKey, commentId, hotScore);
            log.debug("更新热度排行成功: key={}, commentId={}, score={}", redisKey, commentId, hotScore);
        } catch (Exception e) {
            log.error("[缓存] 更新热度排行失败 - key: {}, commentId: {}", redisKey, commentId, e);
        }
    }

    /**
     * 从热度排行中移除评论
     * @param commentType 评论类型
     * @param targetId 目标ID
     * @param commentId 评论ID
     */
    public void removeHotRank(CommentType commentType, Long targetId, Long commentId) {
        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId);
        
        try {
            redisTemplate.opsForZSet().remove(redisKey, commentId.toString());
            log.debug("从热度排行移除评论成功: key={}, commentId={}", redisKey, commentId);
        } catch (Exception e) {
            log.error("[缓存] 移除热度排行失败 - key: {}, commentId: {}", redisKey, commentId, e);
        }
    }
}