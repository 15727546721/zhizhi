package cn.xu.model.vo.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 聚合搜索响应VO
 * 
 * <p>包含帖子、用户、标签三种类型的搜索结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregateSearchVO {
    
    /**
     * 搜索关键词
     */
    private String keyword;
    
    /**
     * 帖子搜索结果
     */
    private SearchResultGroup<PostSearchItem> posts;
    
    /**
     * 用户搜索结果
     */
    private SearchResultGroup<UserSearchItem> users;
    
    /**
     * 标签搜索结果
     */
    private SearchResultGroup<TagSearchItem> tags;
    
    /**
     * 总耗时（毫秒）
     */
    private Long costTime;
    
    /**
     * 搜索结果分组
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResultGroup<T> {
        /**
         * 结果列表
         */
        private List<T> list;
        
        /**
         * 总数量
         */
        private Long total;
        
        /**
         * 是否还有更多
         */
        private Boolean hasMore;
    }
    
    /**
     * 帖子搜索项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PostSearchItem {
        /**
         * 帖子ID
         */
        private Long id;
        
        /**
         * 标题
         */
        private String title;
        
        /**
         * 描述/摘要
         */
        private String description;
        
        /**
         * 封面图
         */
        private String coverUrl;
        
        /**
         * 作者ID
         */
        private Long userId;
        
        /**
         * 作者昵称
         */
        private String authorName;
        
        /**
         * 作者头像
         */
        private String authorAvatar;
        
        /**
         * 浏览次数
         */
        private Long viewCount;
        
        /**
         * 点赞次数
         */
        private Long likeCount;
        
        /**
         * 评论次数
         */
        private Long commentCount;
        
        /**
         * 标签列表
         */
        private List<String> tags;
        
        /**
         * 创建时间
         */
        private String createTime;
    }
    
    /**
     * 用户搜索项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserSearchItem {
        /**
         * 用户ID
         */
        private Long id;
        
        /**
         * 用户名
         */
        private String username;
        
        /**
         * 昵称
         */
        private String nickname;
        
        /**
         * 头像
         */
        private String avatar;
        
        /**
         * 描述
         */
        private String description;
        
        /**
         * 粉丝数量
         */
        private Long fansCount;
        
        /**
         * 帖子数量
         */
        private Long postCount;
        
        /**
         * 是否官方账号
         */
        private Boolean isOfficial;
    }
    
    /**
     * 标签搜索项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TagSearchItem {
        /**
         * 标签ID
         */
        private Long id;
        
        /**
         * 标签名
         */
        private String name;
        
        /**
         * 标签描述
         */
        private String description;
        
        /**
         * 使用次数
         */
        private Integer usageCount;
        
        /**
         * 是否推荐
         */
        private Boolean isRecommended;
    }
}