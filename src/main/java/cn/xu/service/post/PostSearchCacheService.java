package cn.xu.service.post;

import cn.xu.cache.RedisKeyManager;
import cn.xu.common.ResponseCode;
import cn.xu.event.post.PostEvent;
import cn.xu.integration.search.strategy.ElasticsearchSearchStrategy;
import cn.xu.model.entity.Post;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.RedisLock;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Optional;
import java.util.Set;

/**
 * 帖子搜索缓存服务
 * 
 * <p>监听帖子事件，同步更新Elasticsearch搜索索引，提供搜索功能支持
 * 
 * @author xu
 * @since 2025-11-26
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostSearchCacheService {

    @Autowired(required = false)
    private ElasticsearchSearchStrategy esStrategy;
    
    @Resource
    private PostService postService;
    
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    
    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private RedisLock redisLock;

    @Async
    @EventListener
    public void handlePostEvent(PostEvent event) {
        try {
            if (event.getEventType() == PostEvent.PostEventType.CREATED || 
                event.getEventType() == PostEvent.PostEventType.UPDATED) {
                syncElasticsearchIndexAsync(event.getPostId());
                String title = event.getTitle();
                String description = event.getDescription();
                if (title != null || description != null) {
                    clearRelatedSearchCacheWithLock(title, description);
                }
            } else if (event.getEventType() == PostEvent.PostEventType.DELETED) {
                // 删除事件处理
                try {
                    if (esStrategy != null) {
                        esStrategy.removeIndexedPost(event.getPostId());
                    }
                    // 清除全部搜索缓存
                    if (redisLock != null) {
                        redisLock.executeWithLock("cache:clear:all:lock", 10, java.util.concurrent.TimeUnit.SECONDS, 
                            () -> clearAllSearchCache());
                    } else {
                        clearAllSearchCache();
                    }
                } catch (Exception ex) {
                    log.error("删除ES索引失败: postId={}", event.getPostId(), ex);
                }
            }
        } catch (Exception e) {
            log.error("处理帖子事件失败: postId={}, eventType={}", event.getPostId(), event.getEventType(), e);
        }
    }

    private void syncElasticsearchIndexAsync(Long postId) {
        syncElasticsearchIndexWithRetry(postId);
    }
    
    private void syncElasticsearchIndexWithRetry(Long postId) {
        int maxRetries = 3;
        long retryDelayMs = 1000;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                if (esStrategy == null) {
                    return;
                }
                
                Optional<Post> postOpt = postService.getPostById(postId);
                if (postOpt.isPresent()) {
                    Post post = postOpt.get();
                    if (post != null && Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
                        boolean success = esStrategy.indexPostWithRetry(post);
                        if (success) {
                            return;
                        } else {
                            recordFailedIndexTask(postId);
                            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "ES索引失败: postId=" + postId);
                        }
                    }
                }
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(retryDelayMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    log.error("同步ES索引失败: postId={}", postId, e);
                    recordFailedIndexTask(postId);
                }
            }
        }
    }
    
    private void recordFailedIndexTask(Long postId) {
        try {
            String failedTasksKey = "es:index:failed:tasks";
            redisTemplate.opsForSet().add(failedTasksKey, postId.toString());
            redisTemplate.expire(failedTasksKey, 24, java.util.concurrent.TimeUnit.HOURS);
        } catch (Exception e) {
            log.warn("记录失败的ES索引任务失败: postId={}", postId, e);
        }
    }

    private void clearRelatedSearchCache(String title) {
        clearRelatedSearchCache(title, null);
    }
    
    private void clearRelatedSearchCacheWithLock(String title, String description) {
        if (redisLock != null) {
            String lockKey = "cache:clear:lock:" + (title != null ? title.hashCode() : 0) + 
                            (description != null ? description.hashCode() : 0);
            redisLock.executeWithLock(lockKey, 5, java.util.concurrent.TimeUnit.SECONDS, 
                () -> clearRelatedSearchCache(title, description));
        } else {
            clearRelatedSearchCache(title, description);
        }
    }
    
    private void clearRelatedSearchCache(String title, String description) {
        if ((title == null || title.trim().isEmpty()) && 
            (description == null || description.trim().isEmpty())) {
            return;
        }
        
        try {
            java.util.List<String> keywords = new java.util.ArrayList<>();
            
            if (title != null && !title.trim().isEmpty()) {
                String normalizedTitle = cn.xu.integration.search.util.SearchKeywordNormalizer.normalize(title);
                if (!normalizedTitle.isEmpty()) {
                    keywords.add(normalizedTitle);
                    java.util.List<String> extractedKeywords = 
                        cn.xu.integration.search.util.SearchKeywordNormalizer.extractKeywords(normalizedTitle);
                    for (String keyword : extractedKeywords) {
                        if (keyword.length() >= 2 && keyword.length() <= 20) {
                            keywords.add(keyword);
                        }
                    }
                }
            }
            
            if (description != null && !description.trim().isEmpty()) {
                String normalizedDesc = cn.xu.integration.search.util.SearchKeywordNormalizer.normalize(description);
                if (!normalizedDesc.isEmpty()) {
                    java.util.List<String> extractedKeywords = 
                        cn.xu.integration.search.util.SearchKeywordNormalizer.extractKeywords(normalizedDesc);
                    for (String keyword : extractedKeywords) {
                        if (keyword.length() >= 2 && keyword.length() <= 20) {
                            keywords.add(keyword);
                        }
                    }
                }
            }
            
            java.util.Set<String> uniqueKeywords = new java.util.HashSet<>(keywords);
            for (String keyword : uniqueKeywords) {
                invalidateSearchCache(keyword);
            }
        } catch (Exception e) {
            log.warn("清除搜索缓存失败: title={}", title, e);
        }
    }

    private void invalidateSearchCache(String keyword) {
        if (keyword == null || keyword.trim().isEmpty() || redisTemplate == null) {
            return;
        }
        
        try {
            String keyPrefix = RedisKeyManager.postSearchKeyPrefix(keyword.trim());
            String pattern = keyPrefix + "*";
            Set<String> keys = scanKeys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.warn("清除搜索缓存失败: keyword={}", keyword, e);
        }
    }

    private void clearAllSearchCache() {
        if (redisTemplate == null) {
            return;
        }
        
        try {
            Set<String> keys = scanKeys("post:search:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        } catch (Exception e) {
            log.error("清除所有搜索缓存失败", e);
        }
    }
    
    private Set<String> scanKeys(String pattern) {
        try {
            return redisTemplate.execute((org.springframework.data.redis.core.RedisCallback<Set<String>>) connection -> {
                Set<String> keys = new java.util.HashSet<>();
                try (org.springframework.data.redis.core.Cursor<byte[]> cursor = connection.scan(
                        org.springframework.data.redis.core.ScanOptions.scanOptions()
                                .match(pattern)
                                .count(100)
                                .build())) {
                    while (cursor.hasNext()) {
                        byte[] keyBytes = cursor.next();
                        if (keyBytes != null) {
                            keys.add(new String(keyBytes, java.nio.charset.StandardCharsets.UTF_8));
                        }
                    }
                }
                return keys;
            });
        } catch (Exception e) {
            log.warn("SCAN命令执行失败，回退到KEYS命令: pattern={}", pattern, e);
            try {
                return redisTemplate.keys(pattern);
            } catch (Exception fallbackException) {
                log.error("KEYS命令也失败: pattern={}", pattern, fallbackException);
                return new java.util.HashSet<>();
            }
        }
    }

    public void invalidateSearchCacheManually(String keyword) {
        invalidateSearchCache(keyword);
    }

    public void clearAllSearchCacheManually() {
        clearAllSearchCache();
    }
}

