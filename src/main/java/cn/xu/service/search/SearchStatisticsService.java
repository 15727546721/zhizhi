package cn.xu.service.search;

import java.util.List;

/**
 * 搜索统计服务接口
 * <p>定义搜索统计的通用方法</p>

 */
public interface SearchStatisticsService {
    
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

