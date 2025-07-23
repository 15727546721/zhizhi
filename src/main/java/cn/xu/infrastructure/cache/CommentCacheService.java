package cn.xu.infrastructure.cache;

import cn.xu.application.query.comment.dto.CommentDTO;
import cn.xu.application.query.comment.dto.CommentWithRepliesDTO;
import cn.xu.infrastructure.cache.keys.CommentCacheKeys;
import cn.xu.infrastructure.common.response.PageResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCacheService {
    private final RedisTemplate<String, Object> redisTemplate;

    // 带预览回复的评论缓存（5分钟过期）
    public List<CommentWithRepliesDTO> getCommentsWithPreview(
            Integer targetType, Long targetId,
            Integer pageNo, Integer pageSize,
            Supplier<List<CommentWithRepliesDTO>> loader) {

        String cacheKey = CommentCacheKeys.previewCommentsKey(
                targetType, targetId, pageNo, pageSize);

        return getFromCache(cacheKey, loader, 5, TimeUnit.MINUTES);
    }

    // 评论回复分页缓存（10分钟过期）
    public PageResponse<List<CommentDTO>> getCommentReplies(
            Long commentId, Integer pageNo, Integer pageSize,
            Supplier<PageResponse<List<CommentDTO>>> loader) {

        String cacheKey = CommentCacheKeys.commentRepliesKey(
                commentId, pageNo, pageSize);

        return getFromCache(cacheKey, loader, 10, TimeUnit.MINUTES);
    }


    // 更新缓存版本（使旧缓存失效）
    public void refreshCacheVersion() {
        redisTemplate.opsForValue().set(
                CommentCacheKeys.commentsLastUpdateKey(),
                System.currentTimeMillis()
        );
    }

    // 通用缓存获取方法
    private <T> T getFromCache(String key,
                               Supplier<T> loader,
                               long timeout,
                               TimeUnit timeUnit) {
        try {
            // 1. 尝试从缓存读取
            Object cached = redisTemplate.opsForValue().get(key);
            if (cached != null) {
                return (T) cached; // 依赖GenericJackson2JsonRedisSerializer的反序列化
            }

            // 2. 缓存未命中则加载数据
            T result = loader.get();
            if (result != null) {
                redisTemplate.opsForValue().set(
                        key,
                        result, // 自动序列化
                        timeout,
                        timeUnit
                );
            }
            return result;
        } catch (Exception e) {
            log.error("Redis操作失败[key={}]，降级查询数据库", key, e);
            return loader.get();
        }
    }
}