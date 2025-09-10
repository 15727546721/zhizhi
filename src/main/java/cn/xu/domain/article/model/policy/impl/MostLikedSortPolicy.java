package cn.xu.domain.article.model.policy.impl;

import cn.xu.domain.article.model.policy.ArticleSortPolicy;
import cn.xu.domain.article.model.valobj.SortType;

/**
 * 最多点赞排序策略实现
 */
public class MostLikedSortPolicy implements ArticleSortPolicy {
    
    @Override
    public SortType getSortType() {
        return SortType.MOST_LIKED;
    }
    
    @Override
    public String getOrderByClause() {
        return "like_count DESC, create_time DESC";
    }
}