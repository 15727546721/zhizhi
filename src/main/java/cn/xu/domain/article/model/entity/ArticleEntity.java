package cn.xu.domain.article.model.entity;

import cn.xu.domain.article.model.valobj.ArticleStatus;
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
    private Long categoryId;
    private String title;
    private String description;
    private String content;
    private String coverUrl;
    private Long userId;
    private Long viewCount;
    private Long collectCount;
    private Long commentCount;
    private Long likeCount;
    private ArticleStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
