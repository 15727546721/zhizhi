package cn.xu.integration.search.strategy;

import cn.xu.elasticsearch.service.ElasticsearchPostIndexService;
import cn.xu.elasticsearch.service.ElasticsearchPostSearchService;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Post;
import cn.xu.service.search.ElasticsearchIndexManager;
import cn.xu.service.search.SearchStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Elasticsearch 搜索策略实现
 * <p>委托给 ElasticsearchPostSearchService 和 ElasticsearchPostIndexService</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.elasticsearch.enabled", havingValue = "true", matchIfMissing = false)
public class ElasticsearchSearchStrategy implements SearchStrategy, ElasticsearchIndexManager {

    private final ElasticsearchPostSearchService postSearchService;
    private final ElasticsearchPostIndexService postIndexService;

    // ==================== SearchStrategy 接口实现 ====================

    @Override
    public Page<Post> search(String keyword, Pageable pageable) {
        return postSearchService.search(keyword, pageable);
    }

    @Override
    public Page<Post> search(String keyword, SearchFilter filter, Pageable pageable) {
        return postSearchService.search(keyword, filter, pageable);
    }

    @Override
    public boolean isAvailable() {
        return postSearchService.isAvailable();
    }

    @Override
    public String getStrategyName() {
        return postSearchService.getStrategyName();
    }

    // ==================== ElasticsearchIndexManager 接口实现 ====================

    @Override
    public void indexPost(Post post) {
        postIndexService.indexPost(post);
    }

    @Override
    public boolean indexPostWithRetry(Post post) {
        return postIndexService.indexPostWithRetry(post);
    }

    @Override
    public void updateIndexedPost(Post post) {
        postIndexService.updateIndexedPost(post);
    }

    @Override
    public void removeIndexedPost(Long postId) {
        postIndexService.removeIndexedPost(postId);
    }

    @Override
    public long count() {
        return postIndexService.count();
    }

    // ==================== 热度排行（扩展功能）====================

    public Page<Post> getHotRank(String rankType, Pageable pageable) {
        try {
            var indexPage = postIndexService.getHotRank(rankType, pageable);
            List<Post> posts = indexPage.getContent().stream()
                    .map(cn.xu.elasticsearch.converter.PostIndexConverter::toPost)
                    .collect(Collectors.toList());
            return new PageImpl<>(posts, pageable, indexPage.getTotalElements());
        } catch (Exception e) {
            log.error("[ES] 获取热度排行失败 - rankType: {}", rankType, e);
            return new PageImpl<>(java.util.Collections.emptyList(), pageable, 0);
        }
    }
}
