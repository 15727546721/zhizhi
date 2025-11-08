package cn.xu.domain.search.model.policy;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.search.model.valobj.SearchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 搜索策略接口
 */
public interface ISearchStrategy {
    
    Page<PostEntity> search(String keyword, Pageable pageable);
    
    default Page<PostEntity> search(String keyword, SearchFilter filter, Pageable pageable) {
        return search(keyword, pageable);
    }
    
    boolean isAvailable();
    
    String getStrategyName();
}

