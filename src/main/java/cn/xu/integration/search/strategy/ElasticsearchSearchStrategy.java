package cn.xu.integration.search.strategy;

import cn.xu.common.ResponseCode;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Post;
import cn.xu.repository.read.elastic.model.PostIndex;
import cn.xu.repository.read.elastic.repository.PostElasticRepository;
import cn.xu.service.search.ElasticsearchIndexManager;
import cn.xu.service.search.SearchStrategy;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch搜索策略实现
 * <p>职责:</p>
 * <ul>
 *   <li>ES全文检索（搜索）</li>
 *   <li>帖子索引管理（增删改）</li>
 *   <li>热度排行查询</li>
 * </ul>
 
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
            
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            BoolQueryBuilder shouldQuery = QueryBuilders.boolQuery();
            
            shouldQuery.should(QueryBuilders.multiMatchQuery(keyword, "title", "description")
                    .field("title", 2.0f)
                    .field("description", 1.0f)
                    .type(org.elasticsearch.index.query.MultiMatchQueryBuilder.Type.BEST_FIELDS)
                    .operator(org.elasticsearch.index.query.Operator.OR));
            
            String trimmedKeyword = keyword.trim();
            if (trimmedKeyword.length() > 0 && trimmedKeyword.length() <= 20) {
                shouldQuery.should(QueryBuilders.wildcardQuery("title", "*" + trimmedKeyword + "*").boost(0.5f));
                shouldQuery.should(QueryBuilders.wildcardQuery("description", "*" + trimmedKeyword + "*").boost(0.3f));
            }
            
            shouldQuery.minimumShouldMatch(1);
            boolQuery.must(shouldQuery);
            
            if (filter != null) {
                if (filter.getStartTime() != null || filter.getEndTime() != null) {
                    RangeQueryBuilder rangeQuery = QueryBuilders.rangeQuery("publishTime");
                    if (filter.getStartTime() != null) {
                        rangeQuery.gte(filter.getStartTime());
                    }
                    if (filter.getEndTime() != null) {
                        rangeQuery.lte(filter.getEndTime());
                    }
                    boolQuery.filter(rangeQuery);
                }
            }
            
            NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                    .withQuery(boolQuery)
                    .withPageable(pageable);
            
            org.elasticsearch.search.sort.SortBuilder<?> sortBuilder;
            if (filter != null && filter.getSortOption() != null) {
                switch (filter.getSortOption()) {
                    case TIME:
                        sortBuilder = SortBuilders.fieldSort("publishTime").order(SortOrder.DESC);
                        break;
                    case HOT:
                        sortBuilder = SortBuilders.fieldSort("hotScore").order(SortOrder.DESC);
                        break;
                    case COMMENT:
                        sortBuilder = SortBuilders.fieldSort("commentCount").order(SortOrder.DESC);
                        break;
                    case LIKE:
                        sortBuilder = SortBuilders.fieldSort("likeCount").order(SortOrder.DESC);
                        break;
                    default:
                        sortBuilder = SortBuilders.fieldSort("publishTime").order(SortOrder.DESC);
                        break;
                }
            } else {
                sortBuilder = SortBuilders.fieldSort("publishTime").order(SortOrder.DESC);
            }
            queryBuilder.withSorts(sortBuilder);
            
            NativeSearchQuery searchQuery = queryBuilder.build();
            SearchHits<PostIndex> searchHits = elasticsearchOperations.search(searchQuery, PostIndex.class);
            
            List<Post> postEntities = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::toPost)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(postEntities, pageable, searchHits.getTotalHits());
            
        } catch (Exception e) {
            log.error("[ES] Elasticsearch搜索失败 - keyword: {}", keyword, e);
            try {
                Page<PostIndex> indexPage = postElasticRepository.searchByTitleAndDescription(keyword, pageable);
                List<Post> postEntities = indexPage.getContent().stream()
                        .map(this::toPost)
                        .collect(Collectors.toList());
                return new PageImpl<>(postEntities, pageable, indexPage.getTotalElements());
            } catch (Exception fallbackException) {
                log.error("[ES] Elasticsearch简单搜索也失败 - keyword: {}", keyword, fallbackException);
                available = false;
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "Elasticsearch搜索失败: " + e.getMessage());
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

    // ==================== 索引管理功能（原PostElasticService） ====================

    /**
     * 索引帖子
     */
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
    
    /**
     * 索引帖子（带重试）
     */
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
                    // 指数退避重试策略
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
     * 更新索引（未发布的帖子会被删除）
     */
    public void updateIndexedPost(Post post) {
        try {
            if (post == null) {
                return;
            }
            
            // 如果帖子未发布，从索引中删除
            if (!Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
                removeIndexedPost(post.getId());
                log.debug("[ES] 帖子未发布，从索引中删除 - postId: {}", post.getId());
                return;
            }
            
            // 更新索引
            PostIndex index = cn.xu.repository.read.elastic.service.PostIndexConverter.from(post);
            postElasticRepository.save(index);
            log.debug("[ES] 更新帖子索引成功 - postId: {}", post.getId());
        } catch (Exception e) {
            log.warn("[ES] 更新帖子索引失败 - postId: {}", post != null ? post.getId() : null, e);
        }
    }

    /**
     * 删除索引
     */
    public void removeIndexedPost(Long postId) {
        try {
            postElasticRepository.deleteById(postId);
            log.debug("[ES] 删除索引成功 - postId: {}", postId);
        } catch (Exception e) {
            log.warn("[ES] 删除索引失败 - postId: {}", postId, e);
        }
    }

    /**
     * 获取热度排行（日榜、周榜、月榜）
     */
    public Page<Post> getHotRank(String rankType, Pageable pageable) {
        try {
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime start;
            switch (rankType) {
                case "day":
                    start = now.minusDays(1);
                    break;
                case "week":
                    start = now.minusWeeks(1);
                    break;
                case "month":
                    start = now.minusMonths(1);
                    break;
                default:
                    start = now.minusDays(1);
            }
            Page<PostIndex> indexPage = postElasticRepository.findByPublishTimeBetweenOrderByHotScoreDesc(
                start, now, pageable);
            
            List<Post> posts = indexPage.getContent().stream()
                .map(this::toPost)
                .collect(Collectors.toList());
            
            return new PageImpl<>(posts, pageable, indexPage.getTotalElements());
        } catch (Exception e) {
            log.error("[ES] 获取热度排行失败 - rankType: {}", rankType, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "Elasticsearch服务不可用");
        }
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
}
