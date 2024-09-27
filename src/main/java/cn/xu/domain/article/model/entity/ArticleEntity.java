package cn.xu.domain.article.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ArticleEntity {
    private Long id;
    private String title;
    private String content;
    private String coverUrl;
    private String status;
    private Long authorId;
    private Long categoryId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    class CategoryEntity {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }

    class TagEntity {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime createTime;
        private LocalDateTime updateTime;
    }
    // 业务逻辑方法
    public void validate() {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Title cannot be null or empty");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (title.length() > 100) { // 假设标题不能超过100字符
            throw new IllegalArgumentException("Title cannot exceed 100 characters");
        }
        // 其他验证规则可以在这里添加
    }
}
