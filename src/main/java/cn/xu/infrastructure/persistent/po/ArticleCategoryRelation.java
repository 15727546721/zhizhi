package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 文章分类关系表
 *
 * @TableName article_category_relation
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleCategoryRelation implements Serializable {
    /**
     * 文章-分类关系表主键
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 分类ID
     */
    private Long categoryId;

}