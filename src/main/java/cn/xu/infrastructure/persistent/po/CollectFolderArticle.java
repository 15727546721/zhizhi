package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏夹文章关联持久化对象
 *
 * @TableName collect_folder_article
 */
@Data
public class CollectFolderArticle implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 收藏夹ID
     */
    private Long folderId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;

    private static final long serialVersionUID = 1L;
}