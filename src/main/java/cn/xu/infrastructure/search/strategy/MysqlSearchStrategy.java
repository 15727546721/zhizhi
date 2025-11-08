package cn.xu.infrastructure.search.strategy;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.domain.search.model.policy.ISearchStrategy;
import cn.xu.domain.search.model.valobj.SearchFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MySQL搜索策略实现
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MysqlSearchStrategy implements ISearchStrategy {

    private final IPostRepository postRepository;
    private final boolean available = true;

    @Override
    public Page<PostEntity> search(String keyword, Pageable pageable) {
        return search(keyword, null, pageable);
    }

    @Override
    public Page<PostEntity> search(String keyword, SearchFilter filter, Pageable pageable) {
        try {
            String escapedKeyword = escapeLikeKeyword(keyword);
            int offset = (int) pageable.getOffset();
            int size = pageable.getPageSize();
            
            if (filter == null || (filter.getTypes() == null && filter.getStartTime() == null && filter.getSortOption() == null)) {
                List<PostEntity> posts = postRepository.searchByTitle(escapedKeyword, offset, size);
                long total = postRepository.countSearchByTitle(escapedKeyword);
                return new PageImpl<>(posts, pageable, total);
            }
            
            List<String> typeCodes = null;
            if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
                typeCodes = filter.getTypes().stream()
                        .map(PostType::getCode)
                        .collect(Collectors.toList());
            }
            
            String sortBy = "time";
            if (filter.getSortOption() != null) {
                switch (filter.getSortOption()) {
                    case HOT:
                        sortBy = "hot";
                        break;
                    case COMMENT:
                        sortBy = "comment";
                        break;
                    case LIKE:
                        sortBy = "like";
                        break;
                    default:
                        sortBy = "time";
                        break;
                }
            }
            
            List<PostEntity> posts = postRepository.searchByTitleWithFilters(
                    escapedKeyword, typeCodes, filter.getStartTime(), filter.getEndTime(), 
                    sortBy, offset, size);
            long total = postRepository.countSearchByTitleWithFilters(
                    escapedKeyword, typeCodes, filter.getStartTime(), filter.getEndTime());
            
            return new PageImpl<>(posts, pageable, total);
        } catch (Exception e) {
            log.error("MySQL搜索失败: keyword={}", keyword, e);
            throw new RuntimeException("MySQL搜索失败: " + e.getMessage(), e);
        }
    }

    private String escapeLikeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        // 先转义反斜杠，再转义%和_
        return keyword.replace("\\", "\\\\")
                      .replace("%", "\\%")
                      .replace("_", "\\_");
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getStrategyName() {
        return "mysql";
    }
}

