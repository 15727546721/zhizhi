package cn.xu.domain.article.model.policy;

import cn.xu.domain.article.model.policy.impl.*;
import cn.xu.domain.article.model.valobj.SortType;

import java.util.EnumMap;
import java.util.Map;

/**
 * 文章排序策略工厂
 * 负责创建和管理不同的排序策略实现
 */
public class ArticleSortPolicyFactory {
    
    private static final Map<SortType, ArticleSortPolicy> POLICY_MAP = new EnumMap<>(SortType.class);
    
    static {
        // 初始化所有排序策略
        POLICY_MAP.put(SortType.NEWEST, new NewestSortPolicy());
        POLICY_MAP.put(SortType.HOTTEST, new HottestSortPolicy());
        POLICY_MAP.put(SortType.MOST_COMMENTED, new MostCommentedSortPolicy());
        POLICY_MAP.put(SortType.MOST_BOOKMARKED, new MostBookmarkedSortPolicy());
        POLICY_MAP.put(SortType.MOST_LIKED, new MostLikedSortPolicy());
        POLICY_MAP.put(SortType.POPULAR, new PopularSortPolicy());
    }
    
    /**
     * 根据排序类型获取对应的排序策略
     * @param sortType 排序类型
     * @return 排序策略实现
     */
    public static ArticleSortPolicy getPolicy(SortType sortType) {
        ArticleSortPolicy policy = POLICY_MAP.get(sortType);
        if (policy == null) {
            // 默认返回最热排序策略
            return POLICY_MAP.get(SortType.HOTTEST);
        }
        return policy;
    }
    
    /**
     * 根据排序代码获取对应的排序策略
     * @param sortCode 排序代码
     * @return 排序策略实现
     */
    public static ArticleSortPolicy getPolicyByCode(String sortCode) {
        SortType sortType = SortType.fromCode(sortCode);
        return getPolicy(sortType);
    }
}