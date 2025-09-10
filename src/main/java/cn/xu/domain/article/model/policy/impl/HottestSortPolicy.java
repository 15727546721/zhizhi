package cn.xu.domain.article.model.policy.impl;

import cn.xu.domain.article.model.policy.ArticleSortPolicy;
import cn.xu.domain.article.model.valobj.SortType;

/**
 * 最热排序策略实现
 */
public class HottestSortPolicy implements ArticleSortPolicy {
    
    @Override
    public SortType getSortType() {
        return SortType.HOTTEST;
    }
    
    @Override
    public String getOrderByClause() {
        return "(like_count + comment_count * 2) DESC, create_time DESC";
    }
}