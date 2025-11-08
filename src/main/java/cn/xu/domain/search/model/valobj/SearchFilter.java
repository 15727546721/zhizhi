package cn.xu.domain.search.model.valobj;

import cn.xu.domain.post.model.valobj.PostType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 搜索筛选条件值对象
 * 封装搜索时的筛选条件
 */
@Data
@Builder
public class SearchFilter {
    
    /**
     * 帖子类型列表（支持多个类型筛选）
     */
    private List<PostType> types;
    
    /**
     * 时间范围开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 时间范围结束时间
     */
    private LocalDateTime endTime;
    
    /**
     * 排序方式
     */
    private SortOption sortOption;
    
    /**
     * 排序方式枚举
     */
    public enum SortOption {
        /**
         * 最新发布（按创建时间倒序）
         */
        TIME,
        /**
         * 最热（按热度分数倒序）
         */
        HOT,
        /**
         * 评论最多（按评论数倒序）
         */
        COMMENT,
        /**
         * 点赞最多（按点赞数倒序）
         */
        LIKE
    }
}

