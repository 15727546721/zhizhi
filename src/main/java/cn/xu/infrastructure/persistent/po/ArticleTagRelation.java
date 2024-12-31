package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 存储文章与标签之间的多对多关系
 *
 * @TableName article_tag_relation
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleTagRelation implements Serializable {
    /**
     * 文章标签关联ID
     */
    private Long id;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 标签ID
     */
    private Long tagId;
}
