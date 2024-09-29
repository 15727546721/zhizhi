package cn.xu.api.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleListResponse {
    private Long id;
    private String title;
    private String content;
    private String author;
    private String createTime;
}
