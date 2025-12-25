package cn.xu.service.search;

import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 搜索策略接口
 * <p>定义搜索策略的通用方法</p>
 */
public interface SearchStrategy {
    
    Page<Post> search(String keyword, Pageable pageable);
    
    default Page<Post> search(String keyword, SearchFilter filter, Pageable pageable) {
        return search(keyword, pageable);
    }
    
    boolean isAvailable();
    
    String getStrategyName();
}
