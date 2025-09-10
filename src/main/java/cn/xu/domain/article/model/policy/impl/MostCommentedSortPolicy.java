package cn.xu.domain.article.model.policy.impl;

import cn.xu.domain.article.model.policy.ArticleSortPolicy;
import cn.xu.domain.article.model.valobj.SortType;

/**
 * 最多评论排序策略实现
 */
public class MostCommentedSortPolicy implements ArticleSortPolicy {
    
    @Override
    public SortType getSortType() {
        return SortType.MOST_COMMENTED;
    }
    
    @Override
    public String getOrderByClause() {
        return "comment_count DESC, create_time DESC";
    }
}