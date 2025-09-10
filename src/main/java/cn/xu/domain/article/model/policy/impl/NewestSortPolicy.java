package cn.xu.domain.article.model.policy.impl;

import cn.xu.domain.article.model.policy.ArticleSortPolicy;
import cn.xu.domain.article.model.valobj.SortType;

/**
 * 最新排序策略实现
 */
public class NewestSortPolicy implements ArticleSortPolicy {
    
    @Override
    public SortType getSortType() {
        return SortType.NEWEST;
    }
    
    @Override
    public String getOrderByClause() {
        return "create_time DESC";
    }
}