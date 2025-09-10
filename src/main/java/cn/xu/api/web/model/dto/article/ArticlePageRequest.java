package cn.xu.api.web.model.dto.article;

import lombok.Data;

@Data
public class ArticlePageRequest {
    /**
     * 页码
     */
    private Integer pageNo = 1;

    /**
     * 页面大小
     */
    private Integer pageSize = 10;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 排序方式
     */
    private String sortBy = "hottest";
}