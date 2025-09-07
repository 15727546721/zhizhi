package cn.xu.infrastructure.cache;

import cn.xu.domain.comment.model.valueobject.CommentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 评论缓存仓储
 * 专门处理评论相关的缓存操作，遵循DDD原则
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class CommentCacheRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final int DEFAULT_CACHE_TTL = 600; // 10分钟
    private static final int EMPTY_RESULT_TTL = 30;   // 30秒

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
                        .map(this::safeParseCommentId)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            log.error("从Redis获取热门评论ID失败: key={}", redisKey, e);
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
            
            // 设置过期时间
            redisTemplate.expire(redisKey, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
            
            log.debug("缓存热门评论排行成功: key={}, size={}", redisKey, commentScores.size());
        } catch (Exception e) {
            log.error("缓存热门评论排行失败: key={}", redisKey, e);
        }
    }

    /**
     * 缓存空结果，防止缓存穿透
     * @param commentType 评论类型
     * @param targetId 目标ID
     */
    public void cacheEmptyResult(CommentType commentType, Long targetId) {
        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId) + ":empty";
        
        try {
            redisTemplate.opsForValue().set(redisKey, "1", EMPTY_RESULT_TTL, TimeUnit.SECONDS);
            log.debug("缓存空结果成功: key={}", redisKey);
        } catch (Exception e) {
            log.error("缓存空结果失败: key={}", redisKey, e);
        }
    }

    /**
     * 检查是否为空结果缓存
     * @param commentType 评论类型
     * @param targetId 目标ID
     * @return 是否为空结果
     */
    public boolean isEmptyResultCached(CommentType commentType, Long targetId) {
        String redisKey = RedisKeyManager.commentHotRankKey(commentType, targetId) + ":empty";
        
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(redisKey));
        } catch (Exception e) {
            log.error("检查空结果缓存失败: key={}", redisKey, e);
            return false;
        }
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
            
            log.info("清理无效缓存数据成功: key={}, 清理数量={}", redisKey, invalidIds.size());
        } catch (Exception e) {
            log.error("清理无效缓存数据失败: key={}", redisKey, e);
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
        
        try {
            redisTemplate.opsForValue().increment(redisKey, increment);
            log.debug("更新评论计数成功: key={}, increment={}", redisKey, increment);
        } catch (Exception e) {
            log.error("更新评论计数失败: key={}, increment={}", redisKey, increment, e);
        }
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
            log.error("更新热度排行失败: key={}, commentId={}, score={}", redisKey, commentId, hotScore, e);
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
            log.error("从热度排行移除评论失败: key={}, commentId={}", redisKey, commentId, e);
        }
    }

    /**
     * 安全解析评论ID
     * @param id Redis中的ID对象
     * @return 解析后的Long类型ID，解析失败返回null
     */
    private Long safeParseCommentId(Object id) {
        if (id == null) {
            return null;
        }
        
        try {
            return Long.parseLong(id.toString());
        } catch (NumberFormatException e) {
            log.warn("Redis中存在无效的评论ID: {}", id);
            return null;
        }
    }
}