package cn.xu.domain.article.model.valobj;

import lombok.Getter;

/**
 * 文章排序类型值对象
 * 封装文章列表的排序逻辑，确保排序类型的合法性和一致性
 */
@Getter
public enum SortType {
    /**
     * 最新发布
     */
    NEWEST("newest", "最新"),
    
    /**
     * 最热文章（按热度公式计算）
     */
    HOTTEST("hottest", "最热"),
    
    /**
     * 最多评论
     */
    MOST_COMMENTED("most_commented", "最多评论"),
    
    /**
     * 最多收藏
     */
    MOST_BOOKMARKED("most_bookmarked", "最多收藏"),
    
    /**
     * 最多点赞
     */
    MOST_LIKED("most_liked", "最多点赞"),
    
    /**
     * 最受欢迎（按浏览量）
     */
    POPULAR("popular", "最受欢迎");

    private final String code;
    private final String description;

    SortType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 获取排序代码
     * @return 排序代码
     */
    public String getCode() {
        return code;
    }

    /**
     * 根据代码获取排序类型
     * @param code 排序代码
     * @return 排序类型，如果未找到则返回默认值HOTTEST
     */
    public static SortType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return HOTTEST;
        }
        
        for (SortType sortType : SortType.values()) {
            if (sortType.getCode().equals(code)) {
                return sortType;
            }
        }
        
        // 默认返回最热排序
        return HOTTEST;
    }
}