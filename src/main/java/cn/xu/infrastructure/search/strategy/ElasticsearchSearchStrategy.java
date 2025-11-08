package cn.xu.infrastructure.search.strategy;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.search.model.policy.ISearchStrategy;
import cn.xu.domain.search.model.valobj.SearchFilter;
import cn.xu.infrastructure.persistent.read.elastic.model.PostIndex;
import cn.xu.infrastructure.persistent.read.elastic.repository.PostElasticRepository;
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
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchSearchStrategy implements ISearchStrategy {

    private final PostElasticRepository postElasticRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private boolean available = false;

    @PostConstruct
    public void init() {
        try {
            postElasticRepository.count();
            available = true;
            log.info("Elasticsearch搜索策略初始化成功");
        } catch (Exception e) {
            available = false;
            log.warn("Elasticsearch搜索策略初始化失败: {}", e.getMessage());
        }
    }

    @Override
    public Page<PostEntity> search(String keyword, Pageable pageable) {
        return search(keyword, null, pageable);
    }

    @Override
    public Page<PostEntity> search(String keyword, SearchFilter filter, Pageable pageable) {
        if (!available) {
            throw new RuntimeException("Elasticsearch服务不可用");
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
                if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
                    List<String> typeCodes = filter.getTypes().stream()
                            .map(PostType::getCode)
                            .collect(Collectors.toList());
                    boolQuery.filter(QueryBuilders.termsQuery("type", typeCodes));
                }
                
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
            
            List<PostEntity> postEntities = searchHits.getSearchHits().stream()
                    .map(SearchHit::getContent)
                    .map(this::toPostEntity)
                    .collect(Collectors.toList());
            
            return new PageImpl<>(postEntities, pageable, searchHits.getTotalHits());
            
        } catch (Exception e) {
            log.error("Elasticsearch搜索失败: keyword={}", keyword, e);
            try {
                Page<PostIndex> indexPage = postElasticRepository.searchByTitleAndDescription(keyword, pageable);
                List<PostEntity> postEntities = indexPage.getContent().stream()
                        .map(this::toPostEntity)
                        .collect(Collectors.toList());
                return new PageImpl<>(postEntities, pageable, indexPage.getTotalElements());
            } catch (Exception fallbackException) {
                log.error("Elasticsearch简单搜索也失败: keyword={}", keyword, fallbackException);
                available = false;
                throw new RuntimeException("Elasticsearch搜索失败: " + e.getMessage(), e);
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

    private PostEntity toPostEntity(PostIndex index) {
        return PostEntity.builder()
                .id(index.getId())
                .title(index.getTitle() != null ? new PostTitle(index.getTitle()) : null)
                .description(index.getDescription())
                .coverUrl(index.getCoverUrl())
                .userId(index.getUserId())
                .categoryId(index.getCategoryId())
                .viewCount(index.getViewCount() != null ? index.getViewCount() : 0L)
                .favoriteCount(index.getFavoriteCount() != null ? index.getFavoriteCount() : 0L)
                .commentCount(index.getCommentCount() != null ? index.getCommentCount() : 0L)
                .likeCount(index.getLikeCount() != null ? index.getLikeCount() : 0L)
                .shareCount(index.getShareCount() != null ? index.getShareCount() : 0L)
                .isFeatured(index.getIsFeatured() != null ? index.getIsFeatured() : false)
                .createTime(index.getPublishTime())
                .updateTime(index.getUpdateTime() != null ? index.getUpdateTime() : index.getPublishTime())
                .status(cn.xu.domain.post.model.valobj.PostStatus.PUBLISHED)
                .type(index.getType() != null ? PostType.fromCode(index.getType()) : PostType.POST)
                .build();
    }
}

