package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArticleTag implements Serializable {
    /**
     * 文章标签关联的唯一标识符
     */
    private Long id;

    /**
     * 文章的唯一标识符
     */
    private Long articleId;

    /**
     * 标签的唯一标识符
     */
    private Long tagId;
}
