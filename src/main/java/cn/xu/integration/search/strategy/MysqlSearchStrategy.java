package cn.xu.integration.search.strategy;

import cn.xu.common.ResponseCode;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Post;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.service.search.ISearchStrategy;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * MySQL搜索策略实现（ES降级兜底）
 * <p>使用场景：</p>
 * <ul>
 *   <li>Elasticsearch服务不可用时自动降级</li>
 *   <li>ES查询失败时的兜底方案</li>
 *   <li>开发环境未启动ES时的备用方案</li>
 * </ul>

 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlSearchStrategy implements ISearchStrategy {

    private final PostMapper postMapper;

    @Override
    public Page<Post> search(String keyword, Pageable pageable) {
        return search(keyword, null, pageable);
    }

    @Override
    public Page<Post> search(String keyword, SearchFilter filter, Pageable pageable) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return new PageImpl<>(java.util.Collections.emptyList(), pageable, 0);
            }
            
            String escapedKeyword = escapeLikeKeyword(keyword.trim());
            int offset = (int) pageable.getOffset();
            int size = pageable.getPageSize();
            
            // 简单搜索（无过滤条件）
            if (filter == null || !hasFilters(filter)) {
                List<Post> posts = postMapper.searchPosts(escapedKeyword, offset, size);
                Long total = postMapper.countSearchResults(escapedKeyword);
                log.debug("[搜索] MySQL搜索完成 - keyword: {}, total: {}", escapedKeyword, total);
                return new PageImpl<>(posts, pageable, total != null ? total : 0);
            }
            
            // 高级搜索（带过滤条件）
            String sortBy = extractSortBy(filter);
            
            List<Post> posts = postMapper.searchPostsWithFilters(
                escapedKeyword, 
                filter.getStartTime(), 
                filter.getEndTime(), 
                sortBy, 
                offset, 
                size
            );
            
            Long total = postMapper.countSearchResultsWithFilters(
                escapedKeyword, 
                filter.getStartTime(), 
                filter.getEndTime()
            );
            
            log.debug("[搜索] MySQL高级搜索完成 - keyword: {}, sortBy: {}, total: {}", 
                escapedKeyword, sortBy, total);
            
            return new PageImpl<>(posts, pageable, total != null ? total : 0);
            
        } catch (Exception e) {
            log.error("[搜索] MySQL搜索失败 - keyword: {}", keyword, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "MySQL搜索失败: " + e.getMessage());
        }
    }

    /**
     * 转义LIKE查询的特殊字符
     */
    private String escapeLikeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        // 先转义反斜杠，再转义%和_
        return keyword.replace("\\", "\\\\")
                      .replace("%", "\\%")
                      .replace("_", "\\_");
    }

    /**
     * 检查是否有过滤条件
     */
    private boolean hasFilters(SearchFilter filter) {
        return filter.getStartTime() != null ||
               filter.getEndTime() != null ||
               filter.getSortOption() != null;
    }

    /**
     * 提取排序方式
     */
    private String extractSortBy(SearchFilter filter) {
        if (filter.getSortOption() == null) {
            return "time";
        }
        
        switch (filter.getSortOption()) {
            case HOT:
                return "hot";
            case COMMENT:
                return "comment";
            case LIKE:
                return "like";
            default:
                return "time";
        }
    }

    @Override
    public boolean isAvailable() {
        // MySQL总是可用（数据库是必需的）
        return true;
    }

    @Override
    public String getStrategyName() {
        return "mysql";
    }
}

