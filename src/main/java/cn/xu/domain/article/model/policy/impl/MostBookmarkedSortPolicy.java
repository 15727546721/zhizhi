package cn.xu.domain.article.model.policy.impl;

import cn.xu.domain.article.model.policy.ArticleSortPolicy;
import cn.xu.domain.article.model.valobj.SortType;

/**
 * 最多收藏排序策略实现
 */
public class MostBookmarkedSortPolicy implements ArticleSortPolicy {
    
    @Override
    public SortType getSortType() {
        return SortType.MOST_BOOKMARKED;
    }
    
    @Override
    public String getOrderByClause() {
        return "collect_count DESC, create_time DESC";
    }
}