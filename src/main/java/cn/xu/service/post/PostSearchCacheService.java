package cn.xu.service.post;

import cn.xu.cache.core.RedisKeyManager;
import cn.xu.cache.core.DistributedLock;
import cn.xu.cache.core.RedisOperations;
import cn.xu.common.ResponseCode;
import cn.xu.event.core.BaseEvent.EventAction;
import cn.xu.event.events.PostEvent;
import cn.xu.integration.search.strategy.ElasticsearchSearchStrategy;
import cn.xu.model.entity.Post;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 帖子搜索缓存服务
 * 监听帖子事件，异步同步ES索引或清理缓存
 *
 * 主要功能:
 * - 监听帖子创建、更新、删除等事件
 * - 同步Elasticsearch索引
 * - 清理相关的缓存
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostSearchCacheService {

    private final PostQueryService postQueryService;
    private final RedisOperations redisOps;

    // 可选依赖 - setter注入
    private ElasticsearchSearchStrategy esStrategy;
    private DistributedLock distributedLock;

    // ==================== Setter注入 ====================

    @Autowired(required = false)
    public void setEsStrategy(ElasticsearchSearchStrategy esStrategy) {
        this.esStrategy = esStrategy;
    }

    @Autowired(required = false)
    public void setDistributedLock(DistributedLock distributedLock) {
        this.distributedLock = distributedLock;
    }

    // 异步处理帖子事件
    @Async
    @EventListener
    public void handlePostEvent(PostEvent event) {
        try {
            if (event.getAction() == EventAction.CREATE ||
                    event.getAction() == EventAction.UPDATE) {
                syncElasticsearchIndexAsync(event.getPostId());
                String title = event.getTitle();
                if (title != null) {
                    clearRelatedSearchCacheWithLock(title, null);
                }
            } else if (event.getAction() == EventAction.DELETE) {
                try {
                    if (esStrategy != null) {
                        esStrategy.removeIndexedPost(event.getPostId());
                    }
                    if (distributedLock != null) {
                        distributedLock.executeWithLock("cache:clear:all:lock", 10, TimeUnit.SECONDS,
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

                Optional<Post> postOpt = postQueryService.getById(postId);
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
                    // 使用指数退避策略，延迟重试
                    long delayNanos = retryDelayMs * attempt * 1_000_000L;
                    java.util.concurrent.locks.LockSupport.parkNanos(delayNanos);
                    if (Thread.interrupted()) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                } else {
                    log.error("ES索引失败: postId={}", postId, e);
                    recordFailedIndexTask(postId);
                }
            }
        }
    }

    private void recordFailedIndexTask(Long postId) {
        try {
            String failedTasksKey = "es:index:failed:tasks";
            redisOps.sAdd(failedTasksKey, postId.toString());
            redisOps.expire(failedTasksKey, 24 * 3600);
        } catch (Exception e) {
            log.warn("记录失败的ES索引任务失败: postId={}", postId, e);
        }
    }

    private void clearRelatedSearchCache(String title) {
        clearRelatedSearchCache(title, null);
    }

    private void clearRelatedSearchCacheWithLock(String title, String description) {
        if (distributedLock != null) {
            String lockKey = "cache:clear:lock:" + (title != null ? title.hashCode() : 0) +
                    (description != null ? description.hashCode() : 0);
            distributedLock.executeWithLock(lockKey, 5, TimeUnit.SECONDS,
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

            Set<String> uniqueKeywords = new HashSet<>(keywords);
            for (String keyword : uniqueKeywords) {
                invalidateSearchCache(keyword);
            }
        } catch (Exception e) {
            log.warn("清理缓存失败: title={}", title, e);
        }
    }

    private void invalidateSearchCache(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        try {
            String keyPrefix = RedisKeyManager.postSearchKeyPrefix(keyword.trim());
            String pattern = keyPrefix + "*";
            Set<String> keys = scanKeys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisOps.delete(keys);
            }
        } catch (Exception e) {
            log.warn("清理缓存失败: keyword={}", keyword, e);
        }
    }

    private void clearAllSearchCache() {
        try {
            Set<String> keys = scanKeys("post:search:*");
            if (keys != null && !keys.isEmpty()) {
                redisOps.delete(keys);
            }
        } catch (Exception e) {
            log.error("清理所有搜索缓存失败", e);
        }
    }

    private Set<String> scanKeys(String pattern) {
        try {
            return redisOps.scan(pattern, 100);
        } catch (Exception e) {
            log.warn("SCAN操作失败，尝试使用KEYS操作: pattern={}", pattern, e);
            try {
                return redisOps.keys(pattern);
            } catch (Exception fallbackException) {
                log.error("KEYS操作失败: pattern={}", pattern, fallbackException);
                return new HashSet<>();
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
