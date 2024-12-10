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
    private String introduction;
    private String content;
    private String coverUrl;
    private Long authorId;
    private String status; // 0:草稿、1:发布、2:下架、3:待审核、4:审核不通过
    private Long viewCount;
    private Long favoritesCount;
    private Long likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
