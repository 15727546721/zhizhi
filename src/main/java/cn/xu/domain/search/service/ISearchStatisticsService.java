package cn.xu.domain.search.service;

import java.util.List;

/**
 * 搜索统计服务接口
 */
public interface ISearchStatisticsService {
    
    void recordSearch(String keyword, long resultCount, boolean hasResults);
    
    SearchStatistics getSearchStatistics(String date);
    
    List<HotKeyword> getHotKeywordsWithCount(int limit);
    
    List<String> getHotKeywords(int limit);
    
    List<String> getSearchSuggestions(String keyword, int limit);
    
    interface SearchStatistics {
        String getDate();
        long getTotalSearches();
        long getSuccessfulSearches();
        long getEmptySearches();
        double getSuccessRate();
    }
    
    interface HotKeyword {
        String getKeyword();
        long getCount();
    }
}

