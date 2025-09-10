package cn.xu.domain.article.model.policy;

import cn.xu.domain.article.model.valobj.SortType;

/**
 * 文章排序策略接口
 * 定义不同排序方式的处理策略
 */
public interface ArticleSortPolicy {
    
    /**
     * 获取排序类型
     * @return 排序类型
     */
    SortType getSortType();
    
    /**
     * 获取排序SQL片段
     * @return SQL排序片段
     */
    String getOrderByClause();
}