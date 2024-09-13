package cn.xu.domain.article.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ArticleEntity {
    private Long id;
    private String title;
    private String content;
    private String author;

    private Long categoryId;
    private String categoryName;

    private Long tagId;
    private String tagName;

    private String createTime;
    private String updateTime;

}
