package cn.xu.integration.search.strategy;

import cn.xu.common.ResponseCode;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Post;
import cn.xu.repository.read.elastic.model.PostIndex;
import cn.xu.repository.read.elastic.repository.PostElasticRepository;
import cn.xu.service.search.ElasticsearchIndexManager;
import cn.xu.service.search.SearchStrategy;
import cn.xu.support.exception.BusinessException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch搜索策略实现（Spring Boot 3.x 版本）
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchSearchStrategy implements SearchStrategy, ElasticsearchIndexManager {

    private final PostElasticRepository postElasticRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private boolean available = false;

    @PostConstruct
    public void init() {
        try {
            postElasticRepository.count();
            available = true;
            log.info("[ES] Elasticsearch搜索策略初始化成功");
        } catch (Exception e) {
            available = false;
            log.warn("[ES] Elasticsearch搜索策略初始化失败: {}", e.getMessage());
        }
    }

    @Override
    public Page<Post> search(String keyword, Pageable pageable) {
        return search(keyword, null, pageable);
    }

    @Override
    public Page<Post> search(String keyword, SearchFilter filter, Pageable pageable) {
        if (!available) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "Elasticsearch搜索服务不可用");
        }

        try {
            long totalCount = postElasticRepository.count();
            if (totalCount == 0) {
                return new PageImpl<>(new java.util.ArrayList<>(), pageable, 0);
            }
            
            if (keyword == null || keyword.trim().isEmpty()) {
                return new PageImpl<>(new java.util.ArrayList<>(), pageable, 0);
            }

            // 构建查询
            Query query = Query.of(q -> q
                .bool(b -> {
                    // 多字段匹配
                    b.must(m -> m
                        .multiMatch(mm -> mm
                            .query(keyword)
                            .fields("title^2", "description")
                        )
                    );
                    
                    // 时间范围过滤
                    if (filter != null) {
                        if (filter.getStartTime() != null) {
                            b.filter(f -> f.range(r -> r.field("publishTime").gte(co.elastic.clients.json.JsonData.of(filter.getStartTime()))));
                        }
                        if (filter.getEndTime() != null) {
                            b.filter(f -> f.range(r -> r.field("publishTime").lte(co.elastic.clients.json.JsonData.of(filter.getEndTime()))));
                        }
                    }
                    return b;
                })
            );

            // 确定排序字段
            String sortField = "publishTime";
            if (filter != null && filter.getSortOption() != null) {
                switch (filter.getSortOption()) {
                    case TIME: sortField = "publishTime"; break;
                    case HOT: sortField = "hotScore"; break;
                    case COMMENT: sortField = "commentCount"; break;
                    case LIKE: sortField = "likeCount"; break;
                    default: sortField = "publishTime";
                }
            }
            
            final String finalSortField = sortField;
            NativeQuery searchQuery = NativeQuery.builder()
                    .withQuery(query)
                    .withPageable(pageable)
                    .withSort(s -> s.field(f -> f.field(finalSortField).order(SortOrder.Desc)))
                    .build();
            
            SearchHits<PostIndex> searchHits = elasticsearchOperations.search(searchQuery, PostIndex.class);
            
            List<Post> postEntities = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::toPost)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(postEntities, pageable, searchHits.getTotalHits());
            
        } catch (Exception e) {
            log.error("[ES] Elasticsearch搜索失败 - keyword: {}", keyword, e);
            // 降级到 Repository 查询
            try {
                Page<PostIndex> indexPage = postElasticRepository.searchByTitleAndDescription(keyword, pageable);
                List<Post> postEntities = indexPage.getContent().stream()
                        .map(this::toPost)
                        .collect(Collectors.toList());
                return new PageImpl<>(postEntities, pageable, indexPage.getTotalElements());
            } catch (Exception fallbackException) {
                log.error("[ES] Elasticsearch简单搜索也失败 - keyword: {}", keyword, fallbackException);
                available = false;
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "搜索失败，请稍后重试");
            }
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getStrategyName() {
        return "elasticsearch";
    }

    private Post toPost(PostIndex index) {
        return Post.builder()
                .id(index.getId())
                .title(index.getTitle())
                .description(index.getDescription())
                .coverUrl(index.getCoverUrl())
                .userId(index.getUserId())
                .viewCount(index.getViewCount() != null ? index.getViewCount() : 0L)
                .favoriteCount(index.getFavoriteCount() != null ? index.getFavoriteCount() : 0L)
                .commentCount(index.getCommentCount() != null ? index.getCommentCount() : 0L)
                .likeCount(index.getLikeCount() != null ? index.getLikeCount() : 0L)
                .shareCount(index.getShareCount() != null ? index.getShareCount() : 0L)
                .isFeatured(index.getIsFeatured() != null && index.getIsFeatured() ? 1 : 0)
                .createTime(index.getPublishTime())
                .updateTime(index.getUpdateTime() != null ? index.getUpdateTime() : index.getPublishTime())
                .status(Post.STATUS_PUBLISHED)
                .build();
    }

    // ==================== 索引管理功能 ====================

    public void indexPost(Post post) {
        if (post == null || !Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
            return;
        }
        try {
            PostIndex index = cn.xu.repository.read.elastic.service.PostIndexConverter.from(post);
            postElasticRepository.save(index);
            log.debug("[ES] 索引帖子成功 - postId: {}", post.getId());
        } catch (Exception e) {
            log.warn("[ES] 索引帖子失败 - postId: {}", post.getId(), e);
        }
    }
    
    public boolean indexPostWithRetry(Post post) {
        return indexPostWithRetry(post, 3);
    }
    
    private boolean indexPostWithRetry(Post post, int maxRetries) {
        if (post == null || !Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
            return false;
        }
        long retryDelayMs = 500;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                PostIndex index = cn.xu.repository.read.elastic.service.PostIndexConverter.from(post);
                postElasticRepository.save(index);
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

    public void updateIndexedPost(Post post) {
        try {
            if (post == null) return;
            if (!Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
                removeIndexedPost(post.getId());
                return;
            }
            PostIndex index = cn.xu.repository.read.elastic.service.PostIndexConverter.from(post);
            postElasticRepository.save(index);
        } catch (Exception e) {
            log.warn("[ES] 更新帖子索引失败 - postId: {}", post != null ? post.getId() : null, e);
        }
    }

    public void removeIndexedPost(Long postId) {
        try {
            postElasticRepository.deleteById(postId);
        } catch (Exception e) {
            log.warn("[ES] 删除索引失败 - postId: {}", postId, e);
        }
    }

    public Page<Post> getHotRank(String rankType, Pageable pageable) {
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime start = switch (rankType) {
                case "day" -> now.minusDays(1);
                case "week" -> now.minusWeeks(1);
                case "month" -> now.minusMonths(1);
                default -> now.minusDays(1);
            };
            Page<PostIndex> indexPage = postElasticRepository.findByPublishTimeBetweenOrderByHotScoreDesc(start, now, pageable);
            List<Post> posts = indexPage.getContent().stream().map(this::toPost).collect(Collectors.toList());
            return new PageImpl<>(posts, pageable, indexPage.getTotalElements());
        } catch (Exception e) {
            log.error("[ES] 获取热度排行失败 - rankType: {}", rankType, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "Elasticsearch服务不可用");
        }
    }
    
    public long count() {
        try {
            return postElasticRepository.count();
        } catch (Exception e) {
            log.error("[ES] 获取索引总数失败", e);
            return 0L;
        }
    }
}
