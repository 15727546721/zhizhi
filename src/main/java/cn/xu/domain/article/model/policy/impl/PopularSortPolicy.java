package cn.xu.domain.article.model.policy.impl;

import cn.xu.domain.article.model.policy.ArticleSortPolicy;
import cn.xu.domain.article.model.valobj.SortType;

/**
 * 最受欢迎排序策略实现（按浏览量）
 */
public class PopularSortPolicy implements ArticleSortPolicy {
    
    @Override
    public SortType getSortType() {
        return SortType.POPULAR;
    }
    
    @Override
    public String getOrderByClause() {
        return "view_count DESC, create_time DESC";
    }
}