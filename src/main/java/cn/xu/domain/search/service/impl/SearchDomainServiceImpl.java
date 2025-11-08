package cn.xu.domain.search.service.impl;

import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.search.model.policy.ISearchStrategy;
import cn.xu.domain.search.model.policy.SearchStrategyFactory;
import cn.xu.domain.search.model.valobj.SearchFilter;
import cn.xu.domain.search.service.ISearchDomainService;
import cn.xu.domain.search.service.ISearchStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * 搜索领域服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchDomainServiceImpl implements ISearchDomainService {
    
    private final SearchStrategyFactory searchStrategyFactory;
    private final ISearchStatisticsService searchStatisticsService;
    
    @Override
    public Page<PostEntity> executeSearch(String keyword, SearchFilter filter, Pageable pageable) {
        try {
            ISearchStrategy strategy = searchStrategyFactory.getStrategy();
            return strategy.search(keyword, filter, pageable);
        } catch (Exception e) {
            log.error("搜索失败，尝试使用MySQL兜底策略: keyword={}", keyword, e);
            try {
                ISearchStrategy fallbackStrategy = searchStrategyFactory.getStrategy("mysql");
                if (fallbackStrategy != null && fallbackStrategy.isAvailable()) {
                    return fallbackStrategy.search(keyword, filter, pageable);
                }
            } catch (Exception fallbackException) {
                log.error("MySQL兜底策略也失败: keyword={}", keyword, fallbackException);
            }
            throw new RuntimeException("搜索失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void recordSearch(String keyword, long resultCount, boolean hasResults) {
        try {
            searchStatisticsService.recordSearch(keyword, resultCount, hasResults);
        } catch (Exception e) {
            log.warn("记录搜索统计失败: keyword={}", keyword, e);
        }
    }
}

