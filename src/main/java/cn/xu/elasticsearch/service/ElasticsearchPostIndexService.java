package cn.xu.elasticsearch.service;

import cn.xu.elasticsearch.converter.PostIndexConverter;
import cn.xu.elasticsearch.core.ElasticsearchOperations;
import cn.xu.elasticsearch.model.PostIndex;
import cn.xu.elasticsearch.repository.PostElasticRepository;
import cn.xu.model.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Elasticsearch 帖子索引服务
 * <p>负责帖子的索引管理：索引、更新、删除</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchPostIndexService {

    private final ElasticsearchOperations esOps;
    private final PostElasticRepository postElasticRepository;

    /**
     * 索引单个帖子
     */
    public void indexPost(Post post) {
        if (post == null || !Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
            return;
        }
        try {
            PostIndex index = PostIndexConverter.from(post);
            esOps.save(index);
            log.debug("[ES] 索引帖子成功 - postId: {}", post.getId());
        } catch (Exception e) {
            log.warn("[ES] 索引帖子失败 - postId: {}", post.getId(), e);
        }
    }

    /**
     * 索引帖子（带重试机制）
     */
    public boolean indexPostWithRetry(Post post) {
        return indexPostWithRetry(post, 3);
    }

    /**
     * 索引帖子（带重试机制）
     */
    private boolean indexPostWithRetry(Post post, int maxRetries) {
        if (post == null || !Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
            return false;
        }
        long retryDelayMs = 500;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                PostIndex index = PostIndexConverter.from(post);
                esOps.save(index);
                log.debug("[ES] 索引帖子成功 - postId: {}, 尝试次数: {}", post.getId(), attempt);
                return true;
            } catch (Exception e) {
                if (attempt < maxRetries) {
                    long delayNanos = retryDelayMs * attempt * 1_000_000L;
                    java.util.concurrent.locks.LockSupport.parkNanos(delayNanos);
                    if (Thread.interrupted()) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                } else {
                    log.error("[ES] 索引帖子失败（重试{}次）- postId: {}", maxRetries, post.getId(), e);
                    return false;
                }
            }
        }
        return false;
    }

    /**
     * 更新已索引的帖子
     */
    public void updateIndexedPost(Post post) {
        try {
            if (post == null) return;
            if (!Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
                removeIndexedPost(post.getId());
                return;
            }
            PostIndex index = PostIndexConverter.from(post);
            esOps.save(index);
        } catch (Exception e) {
            log.warn("[ES] 更新帖子索引失败 - postId: {}", post != null ? post.getId() : null, e);
        }
    }

    /**
     * 删除帖子索引
     */
    public void removeIndexedPost(Long postId) {
        try {
            esOps.delete(String.valueOf(postId), PostIndex.class);
        } catch (Exception e) {
            log.warn("[ES] 删除索引失败 - postId: {}", postId, e);
        }
    }

    /**
     * 获取热度排行
     */
    public Page<PostIndex> getHotRank(String rankType, Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = switch (rankType) {
            case "day" -> now.minusDays(1);
            case "week" -> now.minusWeeks(1);
            case "month" -> now.minusMonths(1);
            default -> now.minusDays(1);
        };
        return postElasticRepository.findByPublishTimeBetweenOrderByHotScoreDesc(start, now, pageable);
    }

    /**
     * 获取索引总数
     */
    public long count() {
        try {
            return postElasticRepository.count();
        } catch (Exception e) {
            log.error("[ES] 获取索引总数失败", e);
            return 0L;
        }
    }

    /**
     * 检查 ES 是否可用
     */
    public boolean isAvailable() {
        return esOps.isAvailable();
    }
}
