package cn.xu.domain.article.command;

import lombok.Data;

import java.util.List;

/**
 * 发布文章命令
 */
@Data
public class PublishArticleCommand {
    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章封面URL
     */
    private String coverUrl;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章描述
     */
    private String description;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 标签ID列表
     */
    private List<Long> tagIds;

    /**
     * 发布者ID
     */
    private Long userId;
} 