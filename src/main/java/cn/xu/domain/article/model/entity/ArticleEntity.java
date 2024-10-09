package cn.xu.domain.article.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArticleEntity {
    private Long id;
    private String title;
    private String description;
    private String content;
    private String coverUrl;
    private Long authorId;
    private Long categoryId;
    private String status; // 0:草稿、1:发布、2:下架、3:待审核、4:审核不通过
    private String deleted; // 0:未删除, 1:已删除
    private String commentsEnabled;
    private String isTop;
    private Long viewCount;
    private Long favoritesCount;
    private Long likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 业务逻辑方法
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (title.length() > 255) {
            throw new IllegalArgumentException("Title cannot exceed 255 characters");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("Category ID cannot be null");
        }
    }
}
