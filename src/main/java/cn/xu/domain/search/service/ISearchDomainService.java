package cn.xu.domain.search.service;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.search.model.valobj.SearchFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 搜索领域服务接口
 */
public interface ISearchDomainService {
    
    Page<PostEntity> executeSearch(String keyword, SearchFilter filter, Pageable pageable);
    
    void recordSearch(String keyword, long resultCount, boolean hasResults);
}

