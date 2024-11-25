package cn.xu.api.dto.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class ArticleResponse {
    private Long id;
    private String title;
    private String coverUrl;
    private String description;
    private String content;
    private String isTop;
    private String status;
    private String commentEnabled;
}
