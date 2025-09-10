package cn.xu.api.system.model.dto.article;

import lombok.Data;

@Data
public class ArticleRequest {

    /**
     * 当前页码
     */
    private Integer pageNo = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签ID
     */
    private Long tagId;
    
    /**
     * 排序方式
     * newest: 最新
     * hottest: 最热
     * most_commented: 最多评论
     * most_bookmarked: 最多收藏
     * most_liked: 最多点赞
     * popular: 最受欢迎（浏览量）
     */
    private String sortBy = "hottest";
}