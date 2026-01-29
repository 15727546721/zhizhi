package cn.xu.elasticsearch.service;

import cn.xu.common.ResponseCode;
import cn.xu.elasticsearch.converter.PostIndexConverter;
import cn.xu.elasticsearch.core.ElasticsearchOperations;
import cn.xu.elasticsearch.model.PostIndex;
import cn.xu.elasticsearch.repository.PostElasticRepository;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Post;
import cn.xu.support.exception.BusinessException;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 帖子搜索服务
 * <p>负责帖子的 ES 搜索功能</p>
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchPostSearchService {

    private final cn.xu.elasticsearch.core.ElasticsearchOperations esOps;
    private final PostElasticRepository postElasticRepository;
    private boolean available = false;

    public ElasticsearchPostSearchService(
            cn.xu.elasticsearch.core.ElasticsearchOperations esOps,
            PostElasticRepository postElasticRepository) {
        this.esOps = esOps;
        this.postElasticRepository = postElasticRepository;
    }

    @PostConstruct
    public void init() {
        try {
            postElasticRepository.count();
            available = true;
            log.info("[ES] 帖子搜索服务初始化成功");
        } catch (Exception e) {
            available = false;
            log.warn("[ES] 帖子搜索服务初始化失败: {}", e.getMessage());
        }
    }

    /**
     * 搜索帖子（基础版本）
     */
    public Page<Post> search(String keyword, Pageable pageable) {
        return search(keyword, null, pageable);
    }

    /**
     * 搜索帖子（带过滤条件）
     */
    public Page<Post> search(String keyword, SearchFilter filter, Pageable pageable) {
        if (!available) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "Elasticsearch 搜索服务不可用");
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

            // 使用 ElasticsearchOperations 进行搜索
            Page<PostIndex> indexPage = esOps.searchPage(query, pageable, sortField, SortOrder.Desc, PostIndex.class);

            List<Post> postEntities = indexPage.getContent().stream()
                    .map(PostIndexConverter::toPost)
                    .collect(Collectors.toList());

            return new PageImpl<>(postEntities, pageable, indexPage.getTotalElements());

        } catch (Exception e) {
            log.error("[ES] Elasticsearch 搜索失败 - keyword: {}", keyword, e);
            // 降级到 Repository 查询
            try {
                Page<PostIndex> indexPage = postElasticRepository.searchByTitleAndDescription(keyword, pageable);
                List<Post> postEntities = indexPage.getContent().stream()
                        .map(PostIndexConverter::toPost)
                        .collect(Collectors.toList());
                return new PageImpl<>(postEntities, pageable, indexPage.getTotalElements());
            } catch (Exception fallbackException) {
                log.error("[ES] Elasticsearch 简单搜索也失败 - keyword: {}", keyword, fallbackException);
                available = false;
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "搜索失败，请稍后重试");
            }
        }
    }

    /**
     * 检查服务是否可用
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * 获取策略名称
     */
    public String getStrategyName() {
        return "elasticsearch";
    }
}
