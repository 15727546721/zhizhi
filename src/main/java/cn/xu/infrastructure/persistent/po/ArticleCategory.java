package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 
 * @TableName article_category
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleCategory implements Serializable {
    /**
     * 文章-分类关系表主键
     */
    private Long id;

    /**
     * 文章id
     */
    private Long articleId;

    /**
     * 分类id
     */
    private Long categoryId;

}