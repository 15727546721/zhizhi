package cn.xu.domain.article.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleTagEntity {
    /**
     * 文章的唯一标识符
     */
    private Long articleId;

    /**
     * 标签的唯一标识符
     */
    private Long tagId;
}
