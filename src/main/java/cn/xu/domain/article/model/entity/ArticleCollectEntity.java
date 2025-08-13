package cn.xu.domain.article.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleCollectEntity {
    private Long id;
    private Long userId;
    private Long articleId;
    private int status;
    private LocalDateTime createTime;
}
